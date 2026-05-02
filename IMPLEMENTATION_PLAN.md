# Intelligent Momentum Scalping Bot

Canonical implementation plan for a greenfield build.


## Purpose

Build an autonomous intraday momentum-trading system for Indian equities that:

- watches `Nifty 500 + BankNifty` names in real time,
- detects upward momentum spikes,
- validates risk before entry,
- executes and manages positions intraday only,
- surfaces status through a React admin portal,
- sends operator alerts through Telegram,
- supports both `PAPER` and `LIVE` broker execution through one shared workflow.

## Final V1 Scope

- Strategy direction: `LONG_ONLY`
- Market scope: `NSE EQ` symbols drawn from `Nifty 500 + BankNifty`
- Data source: `Zerodha Kite WebSocket` for live ticks
- Broker integration: `Zerodha Kite Connect` for live execution
- Admin UI: `React + Vite + Tailwind`
- Persistence: `PostgreSQL`
- Migrations: `Flyway`
- Alerts: `Telegram Bot API`
- Execution modes:
  - `PAPER`: default mode for all development and validation
  - `LIVE`: supported in the design, but only enabled by startup config after paper validation
- Time boundary: no overnight positions; all positions are force-closed by `15:15 IST`

## Non-Goals For V1

- Short selling
- Options or futures trading
- Multi-account execution
- Historical backtesting engine
- ML-based signal generation
- Broker-neutral abstraction beyond `PAPER` and Zerodha `LIVE`
- Cloud deployment automation
- Static-IP provisioning and broker IP allowlisting

Note: `chat.md` correctly highlights static IP as an operational requirement for serious production trading. That is a pre-live hosting concern, not part of this v1 code artifact.

## Normalized Design Decisions

### 1. Detection is real-time, not cron-based

The original note asks whether spike detection should be scheduled or provide intimation. For v1, spike detection is real-time via WebSocket ticks. Scheduled jobs are used only for:

- instrument master refresh,
- news polling,
- end-of-day hard close,
- daily summary generation,
- operational reset tasks.

### 2. V1 uses an in-memory ring buffer, not Redis

`chat.md` mentions Redis as one possible rolling-window store, while `chat_with_claude.md` later shifts to `ConcurrentHashMap + ArrayDeque`. The canonical v1 design uses the in-process ring-buffer approach because:

- it is lower latency,
- it avoids another moving part in a greenfield repo,
- the tick-processing pipeline already runs in one JVM,
- the system is single-node for v1,
- the required sliding windows are small and bounded.

Redis can be introduced later when replay, scaling, or multi-process coordination becomes necessary.

### 3. Universe-level liquidity filtering is the v1 implementation

The original rough strategy asks for liquidity filtering. V1 enforces this through universe curation instead of order-book analysis:

- only `Nifty 500 + BankNifty` constituents are eligible,
- only `NSE EQ` instruments are eligible,
- instruments below `Rs 50` are excluded,
- blacklisted symbols are excluded,
- symbols missing from the daily Kite instrument master are excluded.

V1 does **not** implement spread-based or market-depth-based rejection because that would require additional order book assumptions not present in the current rough design.

### 4. `PAPER` and `LIVE` share one contract

Broker behavior must be abstracted behind a single `BrokerGateway` interface so the same risk, execution, and position lifecycle code paths run in both modes.

- `PAPER` is the default `broker.mode`.
- `LIVE` is selected only through startup configuration.
- Admin APIs may pause/resume trading, but may not switch `PAPER` to `LIVE` at runtime.

### 5. Partial exits require a leg-level audit trail

The draft schema only had a single `trade_log` row. That is insufficient for partial exits. V1 therefore uses:

- `trade_log`: one row per position lifecycle
- `trade_leg_log`: one row per executed entry/exit leg

This is mandatory for correct PnL, history, and verification.

## Strategy Specification

### Strategy Intent

The system is a momentum spike trader. It does not buy every price jump. A valid entry requires:

- price acceleration,
- volume expansion,
- continuation confirmation,
- a directional micro-trend,
- no immediate reversal/noise pattern,
- passing all risk gates.

### Market Universe Rules

- Universe source files:
  - `nifty500_symbols.txt`
  - `banknifty_symbols.txt`
- Instrument master source: daily download from Kite instruments CSV
- Inclusion criteria:
  - exchange is `NSE`
  - instrument type is `EQ`
  - current listed price is `>= Rs 50`
  - symbol is present in either universe file
  - symbol is not blacklisted
- Special token:
  - always subscribe to Nifty 50 index token `256265` for market-health monitoring

### Entry Signal Rules

All thresholds are configurable. Initial defaults:

| Key | Default |
| --- | --- |
| `signal_price_change_pct` | `0.5` |
| `signal_time_window_sec` | `30` |
| `signal_volume_multiplier` | `2.0` |
| `signal_confirmation_ticks` | `3` |
| `signal_min_ticks` | `10` |
| `signal_cooldown_sec` | `60` |

Signal is emitted only if all conditions pass:

1. The symbol has at least `signal_min_ticks` in local history.
2. Price acceleration over the configured window is `>= signal_price_change_pct`.
3. Current volume versus rolling average volume is `>= signal_volume_multiplier`.
4. The last `signal_confirmation_ticks` ticks are consecutively higher.
5. The micro-trend over the last 5 ticks is `UP` with at least 4 advancing ticks.
6. The pattern is not a noisy spike followed by immediate reversal.
7. The symbol is not inside its per-symbol cooldown window.
8. Current time is between `09:15 IST` and `15:20 IST`.

Strength classification:

- `WEAK`: signal barely clears thresholds
- `MODERATE`: price acceleration `>= 0.7%`
- `STRONG`: price acceleration `>= 1.0%`

### Noise Rejection Rules

V1 noise detection uses the pattern from the draft blueprint:

- detect a sharp positive jump on one tick,
- followed immediately by a meaningful reversal on the next tick,
- reject the signal if the pattern matches a spike-and-fade signature.

This preserves the original intent from `chat.md` to avoid buying fake breakouts, low-quality pumps, and instantly reversing candles.

### Risk Gates

Every `Signal` must pass all gates before becoming a `TradeIntent`:

1. `trading_enabled` is `true`
2. strategy direction is `LONG_ONLY`
3. system time is within trading hours
4. daily loss breaker is not tripped
5. market-fall breaker is not active
6. symbol does not already have an open position
7. open positions are below `max_open_positions`
8. available capital is at least `Rs 5,000`
9. `PositionSizer` returns a quantity `>= 1`

### Position Sizing Rules

Initial defaults:

| Key | Default |
| --- | --- |
| `max_daily_capital` | `100000` |
| `max_open_positions` | `5` |
| `max_capital_per_trade_pct` | `10.0` |
| `max_daily_loss_pct` | `3.0` |

Sizing logic:

- max notional per trade = `availableCapital * max_capital_per_trade_pct / 100`
- quantity = `floor(maxNotional / lastTradedPrice)`
- reject if quantity `< 1`
- estimated charges are calculated before intent is accepted

### Entry Execution Rules

- Order side: `BUY`
- Product: `MIS`
- Order type: `MARKET`
- Exchange: `NSE`

BUY retry behavior:

- maximum 2 retries after initial failure,
- 500 ms wait between retries,
- before retrying, confirm that momentum has not decayed by more than `0.3%` below trigger price,
- if momentum is lost, abandon the buy.

### Exit Rules

Initial defaults:

| Key | Default |
| --- | --- |
| `stop_loss_pct` | `0.8` |
| `target1_pct` | `1.0` |
| `target2_pct` | `2.0` |
| `trailing_stop_activation_pct` | `1.5` |
| `trailing_stop_distance_pct` | `0.5` |
| `market_fall_threshold_pct` | `1.5` |

Exit behavior:

- stop loss: exit 100% when current price falls to stop price
- target 1: sell 40% of original quantity
- target 2: sell another 40% of original quantity
- trailing stop: after activation, sell all remaining quantity if price falls below dynamic trail
- market-fall exit: exit all positions if Nifty 50 drops `>= 1.5%` from day open
- daily-loss exit: exit all positions if realized PnL drops beyond daily breaker
- manual exit: operator-triggered immediate exit
- hard close: force exit all positions at `15:15 IST`

SELL retry behavior:

- retry forever until broker success,
- wait `1000 ms` between attempts for the first 10 failures,
- wait `500 ms` between attempts after that,
- send critical Telegram alert every 3rd failure.

### News Behavior

News is advisory plus protective, not predictive, in v1.

- Poll NSE corporate announcements every 2 minutes during market hours.
- Poll Google News RSS for the top 20 active symbols every 2 minutes during market hours.
- Classify headlines as `BULLISH`, `BEARISH`, or `NEUTRAL` using keyword matching.
- Persist all processed headlines to `news_log`.
- If bearish news is detected for a symbol with an open position:
  - send Telegram alert
  - show it in admin UI
  - do not auto-exit solely on news in v1

## System Architecture

### Technology Stack

| Area | Choice |
| --- | --- |
| Language | Java 17+ |
| Backend | Spring Boot 3.x |
| Build | Maven |
| Database | PostgreSQL |
| DB migration | Flyway |
| Market data | Zerodha Kite WebSocket |
| Live broker | Zerodha Kite Connect REST |
| Paper broker | in-app `PaperBrokerGateway` |
| UI | React + Vite + Tailwind |
| Alerts | Telegram Bot API |
| Logging | SLF4J + Logback |
| Test DB | H2 for automated tests |

### Target Project Layout

```text
algo-trader/
  pom.xml
  src/main/java/com/algotrader/
    config/
    auth/
    alert/
    websocket/
    store/
    universe/
    signal/
    risk/
    broker/
    execution/
    position/
    news/
    reporting/
    admin/
    controller/
    model/
  src/main/resources/
    application.yml
    db/migration/
    universe/
  src/test/

algo-trader-ui/
  package.json
  src/
    api/
    pages/
    components/
```

### Component Responsibilities

| Component | Responsibility |
| --- | --- |
| `KiteAuthService` | generate login URL and exchange request token |
| `TickerService` | connect/reconnect WebSocket and manage subscriptions |
| `TickPipeline` | bounded tick queue between producer and consumer |
| `TickProcessor` | store ticks, invoke signal engine, invoke position manager |
| `TickDataStore` | bounded in-memory rolling history per instrument |
| `InstrumentMasterService` | fetch and parse Kite instruments CSV |
| `StockUniverseService` | build active subscription universe from index lists and blacklist |
| `MomentumIndicator` | pure calculations for acceleration, volume, trend, noise |
| `SignalDetectionEngine` | emit normalized `Signal` events |
| `SignalBus` | bounded queue between signal and risk stages |
| `RiskEngine` | gate every signal before execution |
| `MarketHealthMonitor` | detect Nifty-based halt condition |
| `BrokerGateway` | execution contract for paper/live brokers |
| `OrderExecutionService` | consume `TradeIntent` and place broker orders |
| `PositionManager` | open-position state and exit logic |
| `TradeLogService` | persist position summary and execution legs |
| `NewsDaemon` | fetch, classify, and persist news |
| `AdminController` | runtime operations and config updates |
| `DailySummaryService` | aggregate day metrics and send report |

### End-To-End Data Flow

```text
Kite WebSocket
  -> TickerService
  -> TickPipeline (ArrayBlockingQueue<Tick>, capacity 50_000)
  -> TickProcessor
     -> TickDataStore (ring buffer, 120 ticks per token)
     -> SignalDetectionEngine
        -> SignalBus (ArrayBlockingQueue<Signal>, capacity 1_000)
           -> RiskEngine
              -> ExecutionQueue (ArrayBlockingQueue<TradeIntent>, capacity 1_000)
                 -> OrderExecutionService
                    -> BrokerGateway (PAPER or LIVE)
                    -> PositionManager
                    -> TradeLogService

Parallel:
- NewsDaemon -> NewsSentimentClassifier -> news_log -> Telegram/Admin UI
- DailySummaryService -> daily_summary -> Telegram/Admin UI
- AdminController -> system_config updates -> ConfigRefreshService
```

### Concurrency Model

- WebSocket callback thread must never block on analysis or I/O.
- Tick ingestion uses non-blocking `offer` into a bounded queue.
- Tick processing runs on a dedicated daemon consumer thread.
- Signal-to-risk transition uses a separate bounded queue.
- Execution uses a separate bounded queue.
- Telegram sending is asynchronous.
- News polling runs on scheduled threads and must not share the tick-processing hot path.

### Queue Boundaries And Back Pressure

| Queue | Capacity | Producer | Consumer | Overload behavior |
| --- | --- | --- | --- | --- |
| `TickPipeline` | `50_000` | WebSocket callbacks | tick processor thread | drop tick and log warning |
| `SignalBus` | `1_000` | signal detection | risk engine consumer | log and drop if saturated |
| `ExecutionQueue` | `1_000` | risk engine | execution consumer | log and reject intent if saturated |

Rationale:

- missing a few ticks is preferable to stalling the WebSocket thread,
- signal or execution queue saturation is an operational fault and must be visible,
- bounded queues prevent unbounded memory growth during broker or CPU degradation.

### Failure Handling

- WebSocket disconnect:
  - reconnect with delays `1s, 2s, 4s, 8s, 16s, 30s`
  - reset backoff after successful reconnect
  - re-subscribe to current universe after reconnect
- broker buy failure:
  - retry at most twice
  - abandon if momentum has already faded
- broker sell failure:
  - retry until success
  - raise Telegram alerts repeatedly
- news fetch failure:
  - log error, continue next cycle
- Telegram failure:
  - log and swallow; never break trading path
- config refresh failure:
  - keep last known in-memory config and surface error in logs

### Why The Ring Buffer Is Correct For V1

Use `ConcurrentHashMap<Long, ArrayDeque<Tick>>` capped at 120 ticks per symbol because:

- the processing windows are short,
- the working set is bounded,
- the pipeline is single-process,
- the design is simpler to test than external Redis state,
- it preserves low latency and deterministic behavior.

## Public Contracts

### Runtime Configuration

#### `application.yml`

These values are startup-only and are not operator-editable through admin APIs:

| Key | Purpose |
| --- | --- |
| `kite.api-key` | Zerodha app key |
| `kite.api-secret` | Zerodha app secret |
| `kite.access-token` | active session token |
| `spring.datasource.url` | PostgreSQL connection |
| `spring.datasource.username` | DB user |
| `spring.datasource.password` | DB password |
| `telegram.bot-token` | Telegram bot token |
| `telegram.chat-id` | Telegram destination |
| `admin.api-key` | admin REST auth key |
| `broker.mode` | `PAPER` or `LIVE`; default `PAPER` |
| `universe.refresh-cron` | daily refresh schedule |
| `universe.min-price` | price floor, default `50.0` |
| `universe.blacklist` | static blacklist |
| `tick.queue.capacity` | default `50000` |
| `signal.queue.capacity` | default `1000` |
| `execution.queue.capacity` | default `1000` |
| `market.hours.start` | `09:15` |
| `market.signal-cutoff` | `15:20` |
| `market.hard-close` | `15:15` |
| `report.summary-time` | `15:45` |

#### `system_config`

These values are runtime-editable through admin APIs and reloaded in memory after update:

| Key | Default |
| --- | --- |
| `strategy_direction` | `LONG_ONLY` |
| `trading_enabled` | `true` |
| `max_daily_capital` | `100000` |
| `max_open_positions` | `5` |
| `max_capital_per_trade_pct` | `10.0` |
| `max_daily_loss_pct` | `3.0` |
| `stop_loss_pct` | `0.8` |
| `target1_pct` | `1.0` |
| `target2_pct` | `2.0` |
| `trailing_stop_activation_pct` | `1.5` |
| `trailing_stop_distance_pct` | `0.5` |
| `signal_price_change_pct` | `0.5` |
| `signal_time_window_sec` | `30` |
| `signal_volume_multiplier` | `2.0` |
| `signal_confirmation_ticks` | `3` |
| `signal_min_ticks` | `10` |
| `signal_cooldown_sec` | `60` |
| `market_fall_threshold_pct` | `1.5` |

### Database Schema

#### `trade_log`

One row per trade lifecycle.

| Column | Notes |
| --- | --- |
| `id` | primary key |
| `symbol` | trading symbol |
| `instrument_token` | instrument token |
| `broker_mode` | `PAPER` or `LIVE` |
| `signal_strength` | `WEAK`, `MODERATE`, `STRONG` |
| `entry_price_vwap` | weighted average entry price |
| `exit_price_vwap` | weighted average exit price after closure |
| `total_quantity` | original bought quantity |
| `remaining_quantity` | zero on close |
| `entry_time` | first filled entry timestamp |
| `exit_time` | final close timestamp |
| `final_exit_reason` | last closing reason |
| `gross_pnl` | before charges |
| `charges` | aggregated charges |
| `net_pnl` | after charges |
| `status` | `OPEN`, `CLOSING`, `CLOSED`, `FAILED_EXIT` |

#### `trade_leg_log`

One row per executed entry or exit leg.

| Column | Notes |
| --- | --- |
| `id` | primary key |
| `trade_id` | FK to `trade_log` |
| `leg_type` | `ENTRY` or `EXIT` |
| `reason` | `BUY`, `TARGET_1`, `TARGET_2`, `STOP_LOSS`, `TRAILING_STOP`, `EOD_CLOSE`, `MANUAL_EXIT`, `MARKET_FALL`, `DAILY_LOSS_LIMIT` |
| `order_id` | broker or synthetic order id |
| `quantity` | executed quantity |
| `price` | executed price |
| `charges` | leg-level charges |
| `broker_mode` | `PAPER` or `LIVE` |
| `executed_at` | fill timestamp |

#### `daily_summary`

One row per trading day.

| Column | Notes |
| --- | --- |
| `id` | primary key |
| `trade_date` | unique day |
| `total_trades` | count of closed positions |
| `winning_trades` | positive net PnL trades |
| `losing_trades` | negative net PnL trades |
| `gross_pnl` | aggregate gross PnL |
| `total_charges` | aggregate charges |
| `net_pnl` | aggregate net PnL |
| `capital_used` | peak or total deployed capital for the day |
| `notes` | generated analysis notes |

#### `system_config`

| Column | Notes |
| --- | --- |
| `key` | primary key |
| `value` | string value |
| `updated_at` | last update timestamp |

#### `news_log`

| Column | Notes |
| --- | --- |
| `id` | primary key |
| `symbol` | nullable for market-wide news |
| `headline` | stored headline |
| `source` | `NSE` or `GOOGLE_NEWS` |
| `sentiment` | `BULLISH`, `BEARISH`, `NEUTRAL` |
| `confidence` | 0.0 to 1.0 |
| `published_at` | source publish timestamp when available |
| `processed_at` | ingestion timestamp |

### REST API Contract

#### Health

- `GET /api/health`
  - returns backend health, current broker mode, and Kite connectivity status

#### Universe

- `GET /api/universe`
- `POST /api/universe/refresh`
- `POST /api/universe/blacklist/{symbol}`

#### Signals

- `GET /api/signals/recent`
- `GET /api/signals/stats`

#### Risk

- `GET /api/risk/status`

#### Positions

- `GET /api/positions`
- `GET /api/positions/history`
- `POST /api/positions/{symbol}/exit`

#### Admin

All `/api/admin/**` routes require header `X-Admin-Key`.

- `GET /api/admin/config`
- `POST /api/admin/config`
- `POST /api/admin/trading/pause`
- `POST /api/admin/trading/resume`
- `POST /api/admin/exit-all`
- `GET /api/admin/dashboard`
- `GET /api/admin/news/recent`

### Core Domain Types

| Type | Purpose |
| --- | --- |
| `Tick` | normalized market tick used internally |
| `UniverseStock` | symbol metadata and universe membership |
| `Signal` | detected opportunity before risk validation |
| `TradeIntent` | approved execution request after risk gates |
| `Position` | mutable open-position lifecycle state |
| `TradeLog` | position-level persisted summary |
| `TradeLegLog` | execution-leg persisted audit row |
| `DailySummary` | day-level aggregation |
| `NewsLog` | persisted news event |

### Broker Abstraction

#### `BrokerGateway`

Required methods:

- `BrokerOrderResult placeBuy(TradeIntent intent)`
- `BrokerOrderResult placeSell(String symbol, long token, int quantity, String reason)`
- `boolean isConnected()`
- `String mode()`

#### `PaperBrokerGateway`

Deterministic simulation rules:

- fill immediately at latest `TickDataStore` last price
- generate synthetic order id like `PAPER-{uuid}`
- reject if no latest tick exists for the requested token
- calculate charges with the same estimator used by live-mode reporting

#### `KiteBrokerGateway`

- wraps Kite Connect order placement
- emits real order ids
- respects the same rate-limiter and retry policy as paper mode orchestration

## Phase-By-Phase Implementation Plan

Each phase is a stop gate. Do not start the next phase until the current phase passes all automated and manual verification criteria.

### Phase 1: Foundation, Auth, Schema, Logging

**Objective**

Create the Spring Boot skeleton, configuration model, Kite auth service, health endpoint, Flyway migrations, and baseline logging/alert infrastructure.

**Build**

- create `algo-trader/` Spring Boot 3 Maven project
- add dependencies:
  - `spring-boot-starter-web`
  - `spring-boot-starter-data-jpa`
  - `postgresql`
  - `flyway-core`
  - `lombok`
  - `micrometer-core`
  - `kiteconnect`
- create config classes:
  - `KiteConfig`
  - `AppConfig`
  - `SchedulingConfig`
- create auth service:
  - `KiteAuthService.generateLoginUrl()`
  - `KiteAuthService.exchangeToken(requestToken)`
- create `HealthController`
- create Flyway migrations:
  - `V1__init.sql`
  - include `trade_log`, `trade_leg_log`, `daily_summary`, `system_config`
- seed default `system_config` rows
- configure Logback with async appenders

**External Dependencies**

- PostgreSQL
- Zerodha credentials

**Acceptance Criteria**

- app starts with valid config
- Flyway runs cleanly on empty DB
- `/api/health` returns status JSON
- Kite auth service can generate login URL

**Automated Tests**

- `KiteAuthServiceTest`
- `HealthControllerTest`
- migration smoke test

**Manual Verification**

- inspect tables in PostgreSQL
- confirm seeded config rows exist
- confirm log file is being written

**Do Not Proceed Until**

- `mvn clean test` passes
- schema exists as designed
- `/api/health` works

### Phase 2: Tick Pipeline And Alert Infrastructure

**Objective**

Create the real-time data ingestion path from Kite WebSocket to local tick storage.

**Build**

- create `Tick`
- create `TickDataStore`
- create `TickerService`
- create `TickPipeline`
- create `TickProcessor`
- create `TelegramAlertService`
- create temporary bootstrap token loader with a small fixed test universe
- add reconnect policy and queue-depth logging

**External Dependencies**

- Kite WebSocket
- Telegram API

**Acceptance Criteria**

- ticks are received and enqueued
- queue overload does not block WebSocket thread
- latest and rolling ticks are queryable from store
- reconnect logic re-subscribes after disconnect

**Automated Tests**

- `TickDataStoreTest`
- `TickPipelineTest`
- `TelegramAlertServiceTest`

**Manual Verification**

- run against real market or replayed mock ticks
- observe queue-depth logs
- force disconnect and confirm reconnect path

**Do Not Proceed Until**

- rolling store cap is enforced
- processor keeps up with synthetic load
- reconnect behavior is proven

### Phase 3: Dynamic Universe Management

**Objective**

Replace the temporary token list with a real, refreshable tradable universe.

**Build**

- create `InstrumentMasterService`
- create `StockUniverseService`
- create `UniverseStock`
- create `UniverseController`
- add resource files:
  - `nifty500_symbols.txt`
  - `banknifty_symbols.txt`
- always include Nifty index token `256265`
- support blacklist and refresh

**External Dependencies**

- Kite instruments CSV
- `commons-csv`

**Acceptance Criteria**

- active universe resolves from resource lists plus instrument master
- invalid or missing symbols are skipped and logged
- blacklist updates subscriptions without restart

**Automated Tests**

- `InstrumentMasterServiceTest`
- `StockUniverseServiceTest`
- `UniverseControllerTest`

**Manual Verification**

- refresh universe against live Kite CSV
- verify active token count is in expected range
- blacklist one symbol and observe unsubscription

**Do Not Proceed Until**

- universe endpoint is correct
- subscriptions can be refreshed without restart

### Phase 4: Signal Detection Engine

**Objective**

Turn rolling ticks into high-quality momentum signals.

**Build**

- create `Signal`
- create `MomentumIndicator`
- create `SignalDetectionEngine`
- create `SignalBus`
- create `SignalController`
- move configurable thresholds to `system_config` and/or config refresh model

**External Dependencies**

- none beyond existing tick pipeline

**Acceptance Criteria**

- signals are emitted only when all detection rules pass
- cooldown suppresses duplicate rapid-fire signals
- signals are never emitted before market open or after cutoff

**Automated Tests**

- `MomentumIndicatorTest`
- `SignalDetectionEngineTest`
- `SignalControllerTest`

**Manual Verification**

- inject synthetic rising tick streams
- observe logged signal output and recent-signal API

**Do Not Proceed Until**

- all signal calculations are correct
- duplicate suppression is proven

### Phase 5: Risk Engine And Circuit Breakers

**Objective**

Gate every signal through capital, exposure, and market-health controls before execution.

**Build**

- create `DailyCapitalTracker`
- create `PositionSizer`
- create `MarketHealthMonitor`
- create `RiskEngine`
- create `TradeIntent`
- create `RiskController` or `GET /api/risk/status`
- wire `SignalBus` consumer into `RiskEngine`

**External Dependencies**

- none beyond existing data flow

**Acceptance Criteria**

- blocked signals do not enter execution queue
- approved signals become deterministic `TradeIntent` objects
- market-fall and daily-loss breakers stop new entries

**Automated Tests**

- `PositionSizerTest`
- `ChargesEstimationTest`
- `MarketHealthMonitorTest`
- `DailyCapitalTrackerTest`
- `RiskEngineTest`

**Manual Verification**

- pause trading in config and confirm signal rejection
- simulate Nifty drop and confirm halt status

**Do Not Proceed Until**

- all gates are independently verified
- no signal reaches execution unless every gate passes

### Phase 6: Broker Execution And Position Lifecycle

**Objective**

Execute entries through the broker abstraction and manage open positions through exit.

**Build**

- create `BrokerGateway`
- create `PaperBrokerGateway`
- create `KiteBrokerGateway`
- create `BrokerOrderResult`
- create `OrderExecutionService`
- create `Position`
- create `PositionManager`
- create `TradeLogService`
- create `PositionController`
- add EOD hard-close scheduler

**External Dependencies**

- Guava rate limiter
- Kite Connect for live mode

**Acceptance Criteria**

- paper and live modes both run through the same orchestration path
- BUY retries obey limited retry rules
- SELL retries continue until success
- partial exits and final exits are both persisted correctly

**Automated Tests**

- `PaperBrokerGatewayTest`
- `KiteBrokerGatewayTest` with mocked client
- `OrderExecutionServiceTest`
- `ExitRetryTest`
- `PositionManagerTest`
- `TradeLogServiceTest`
- `EodExitTest`

**Manual Verification**

- run full trade lifecycle in paper mode from signal to close
- verify `trade_log` plus `trade_leg_log` rows
- verify manual exit endpoint

**Do Not Proceed Until**

- partial exits reconcile correctly
- exit retry path is proven
- paper mode can run a full lifecycle with no live broker dependency

### Phase 7: News, Admin Operations, Daily Summary

**Objective**

Add operator controls, advisory news monitoring, and day-end analytics.

**Build**

- add `V2__news_log.sql`
- create `NewsSentimentClassifier`
- create `NewsDaemon`
- create `AdminAuthFilter`
- create `AdminController`
- create `ConfigRefreshService`
- create `DailySummaryService`

**External Dependencies**

- NSE announcements API
- Google News RSS
- Telegram API

**Acceptance Criteria**

- admin routes reject missing or bad keys
- config updates propagate without restart
- bearish news for open positions generates alert
- daily summary is persisted and sent

**Automated Tests**

- `NewsSentimentClassifierTest`
- `NewsDaemonTest`
- `AdminControllerTest`
- `DailySummaryServiceTest`

**Manual Verification**

- update config via API and confirm behavior changes
- pause trading and confirm next signal is rejected
- inspect generated daily summary content

**Do Not Proceed Until**

- admin auth works
- config refresh works
- daily summary metrics are correct

### Phase 8: React Admin Portal And Full Integration Test

**Objective**

Expose runtime state in a usable UI and prove end-to-end behavior with one integration suite.

**Build**

- create `algo-trader-ui/` with Vite React
- add `axios`, Tailwind, API client
- pages:
  - `Dashboard`
  - `Positions`
  - `Configuration`
  - `TradeHistory`
  - `Signals`
  - `News`
- add navbar with live IST clock and trading-status indicator
- create `AlgoTraderIntegrationTest`

**External Dependencies**

- Node/npm

**Acceptance Criteria**

- UI renders against running backend with no console errors
- config updates work from UI
- position and signal views auto-refresh
- end-to-end integration test proves one full position lifecycle

**Automated Tests**

- backend `AlgoTraderIntegrationTest`
- frontend smoke tests if the chosen toolchain adds them

**Manual Verification**

- open dashboard locally
- pause/resume trading
- verify positions and history views
- verify news and signals refresh

**Do Not Proceed Until**

- integration test passes
- operator can observe and control the paper system from UI

## Full Integration Test Contract

The backend integration test must verify this sequence with mocked broker and Telegram dependencies:

1. backend health is up
2. test universe loads successfully
3. synthetic ticks for one symbol create exactly one valid signal
4. risk engine approves exactly one `TradeIntent`
5. broker buy is placed
6. `trade_log` opens one position row
7. target-1 price movement triggers first partial exit
8. later adverse movement triggers protective remaining exit
9. `trade_leg_log` contains entry and both exit legs
10. `trade_log` closes with correct aggregate PnL and final reason
11. `DailySummaryService` produces one day summary row

This test must run in `PAPER` mode only.

## Operating Model

### Startup Sequence

1. start PostgreSQL
2. start backend in `PAPER` mode
3. verify `/api/health`
4. complete Kite auth if live data session token is needed
5. refresh instrument master and active universe
6. verify tick ingestion before market open
7. start React admin UI
8. verify dashboard visibility

### Market-Hours Behavior

- signal generation active: `09:15` to `15:20`
- news polling active: `09:00` to `15:35`
- force close all positions: `15:15`
- daily summary generation: `15:45`
- capital tracker reset: after market close, default `18:00`

### Pause/Resume Rules

- `POST /api/admin/trading/pause`:
  - blocks new entries
  - does not auto-close current positions
- `POST /api/admin/trading/resume`:
  - re-enables new entries if breakers are clear
- `POST /api/admin/exit-all`:
  - immediately exits all open positions

### `PAPER` Before `LIVE` Promotion Rule

`LIVE` mode may only be used after all of the following are true:

- all phase-level automated tests pass,
- the full integration test passes,
- at least one full market session has been observed in `PAPER` mode with real WebSocket ticks,
- admin pause/resume and exit-all have been manually verified,
- trade history and daily summary reconcile with the simulated fills.

`LIVE` mode is a startup-time operational choice, not an experimentation toggle.

### Future Phases Explicitly Excluded From V1

- short-selling strategy branch
- options/futures branch
- backtesting and replay framework
- multi-account trade allocation
- advanced news NLP
- spread/order-book filters
- distributed deployment and HA coordination
- cloud networking, static IP, and broker allowlisting automation

## Build-Ready Outcome

This file is complete only if the implementer can start building without deciding:

- the v1 scope,
- the signal rules,
- the risk gates,
- the broker mode behavior,
- the required schema,
- the required APIs,
- the project structure,
- the phase boundaries,
- the verification checkpoints.

That is the intent of this document.
