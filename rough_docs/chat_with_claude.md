
Claude finished the response

Yeah, I want to plan a strategy or a plan mode that I will give to AI to implement the code. So the idea is, find the stock which is rising, okay, suddenly. It's like there is a sudden spike in the stock. So, okay, so the best would be if I don't have to even provide the names of the stock or the li

pasted

13:51

The user prompt is empty, so I cannot provide a summary
The user prompt is empty, so I cannot provide a summary
Claude's response was interrupted
You said: dont give me implementation , give me tengable testable verifyable stage/phase phase wise super detailed implemenattaion design document that i can provide to …

Yeah, I want to plan a strategy or a plan mode that I will give to AI to implement the code. So the idea is, find the stock which is rising, okay, suddenly. It's like there is a sudden spike in the stock. So, okay, so the best would be if I don't have to even provide the names of the stock or the li

pasted

dont give me implementation , give me tengable testable verifyable stage/phase phase wise super detailed implemenattaion design document that i can provide to claude-code or similar ai agent to build this for me

13:53

Architected phased implementation blueprint with testable verification criteria per stage
Architected phased implementation blueprint with testable verification criteria per stage
The user wants a comprehensive, phased implementation design document - not generic, but super detailed and verifiable - that they can hand to Claude Code or any AI coding agent. Each phase should be testable and verifiable before moving to the next.

Let me create this as a proper .docx document. This will be very detailed.

Key phases I'll cover:

Project Foundation & Infrastructure Setup
Kite WebSocket Integration & Real-time Data Pipeline
Stock Universe Management
Signal Detection Engine (Momentum/Spike Detection)
Risk Management Engine
Trade Execution Engine
News Daemon
Admin Portal
Daily Summary & Self-improvement Module
Integration & End-to-End Testing
Each phase needs: objective, exact files/classes to create, APIs to use, verification criteria, what to test.

Let me write this as a docx.

Show less

Full algo trading design document generator
gen_doc.js

Remove old file
bash

rm /home/claude/gen_doc.js
Output

exit code 0

Full algo trading design document generator
gen_doc.js
Done
You said: give me the prepared doc
give me the prepared doc

14:20


You are out of free messages until 6:50 PM
give me the prepared doc

Claude is AI and can make mistakes. Please double-check responses.
Gen doc · JS
Copy

const {
  Document, Packer, Paragraph, TextRun, Table, TableRow, TableCell,
  HeadingLevel, AlignmentType, BorderStyle, WidthType, ShadingType,
  LevelFormat, Header, Footer, TabStopType, PageNumber
} = require('docx');
const fs = require('fs');
 
const BLUE = "1F4E79"; const LT_BLUE = "D6E4F0"; const MED_BLUE = "2E75B6";
const GREEN_D = "375623"; const LT_GREEN = "E2EFDA"; const RED_D = "C00000";
const DARK = "1A1A1A"; const GRAY = "595959"; const WHITE = "FFFFFF";
const LT_GRAY = "F5F5F5"; const YELLOW_BG = "FFF9E6"; const ORANGE_D = "BF5700";
const PURPLE = "4B0082"; const LT_PURPLE = "F3E8FF";
 
const CB = { style: BorderStyle.SINGLE, size: 1, color: "AAAAAA" };
const AB = { top: CB, bottom: CB, left: CB, right: CB };
 
const f = (text, opts = {}) => new TextRun({ text, font: "Calibri", size: 22, color: DARK, ...opts });
const fb = (text, color = DARK) => f(text, { bold: true, color });
const fh = (text, size, color, bold = true) => new TextRun({ text, font: "Calibri", size, color, bold });
 
const P = (runs, after = 80) => new Paragraph({ spacing: { after }, children: Array.isArray(runs) ? runs : [f(runs)] });
const sp = () => new Paragraph({ children: [f("")], spacing: { after: 60 } });
 
const h1 = (text) => new Paragraph({
  spacing: { before: 480, after: 120 },
  border: { bottom: { style: BorderStyle.SINGLE, size: 12, color: MED_BLUE } },
  children: [fh("  " + text, 34, BLUE, true)]
});
const h2 = (text) => new Paragraph({ spacing: { before: 280, after: 100 }, children: [fh(text, 28, MED_BLUE, true)] });
const h3 = (text) => new Paragraph({ spacing: { before: 180, after: 80 }, children: [fh(text, 24, ORANGE_D, true)] });
const h4 = (text) => new Paragraph({ spacing: { before: 120, after: 60 }, children: [fh(text, 22, GREEN_D, true)] });
 
const bul = (text, level = 0) => new Paragraph({
  numbering: { reference: "bul", level },
  spacing: { after: 50 },
  children: [f(text)]
});
const bulMixed = (label, text) => new Paragraph({
  numbering: { reference: "bul", level: 0 },
  spacing: { after: 50 },
  children: [fb(label + ": ", MED_BLUE), f(text)]
});
const num = (text) => new Paragraph({
  numbering: { reference: "num", level: 0 },
  spacing: { after: 60 },
  children: [f(text)]
});
 
const codeBlock = (lines) => new Table({
  width: { size: 9360, type: WidthType.DXA }, columnWidths: [9360],
  rows: [new TableRow({ children: [new TableCell({
    borders: AB, width: { size: 9360, type: WidthType.DXA },
    shading: { fill: "1E1E1E", type: ShadingType.CLEAR },
    margins: { top: 120, bottom: 120, left: 200, right: 200 },
    children: lines.map(l => new Paragraph({ spacing: { after: 0 },
      children: [new TextRun({ text: l, font: "Courier New", size: 18, color: "D4D4D4" })] }))
  })]})],
});
 
const infoBox = (label, text, fill, labelColor) => new Table({
  width: { size: 9360, type: WidthType.DXA }, columnWidths: [9360],
  rows: [new TableRow({ children: [new TableCell({
    borders: AB, width: { size: 9360, type: WidthType.DXA },
    shading: { fill, type: ShadingType.CLEAR },
    margins: { top: 100, bottom: 100, left: 180, right: 180 },
    children: [new Paragraph({ spacing: { after: 0 },
      children: [new TextRun({ text: label + " ", bold: true, font: "Calibri", size: 22, color: labelColor }),
                 new TextRun({ text, font: "Calibri", size: 22, color: DARK })] })]
  })]})],
});
 
const twoCol = (rows, w1 = 2800) => {
  const w2 = 9360 - w1;
  return new Table({
    width: { size: 9360, type: WidthType.DXA }, columnWidths: [w1, w2],
    rows: rows.map((r, i) => new TableRow({ children: [
      new TableCell({ borders: AB, width: { size: w1, type: WidthType.DXA },
        shading: { fill: i === 0 ? LT_BLUE : LT_GRAY, type: ShadingType.CLEAR },
        margins: { top: 80, bottom: 80, left: 120, right: 120 },
        children: [new Paragraph({ spacing:{after:0}, children: [i===0 ? fb(r[0],BLUE) : fb(r[0])] })] }),
      new TableCell({ borders: AB, width: { size: w2, type: WidthType.DXA },
        shading: { fill: i === 0 ? LT_BLUE : WHITE, type: ShadingType.CLEAR },
        margins: { top: 80, bottom: 80, left: 120, right: 120 },
        children: [new Paragraph({ spacing:{after:0}, children: [i===0 ? fb(r[1],BLUE) : f(r[1])] })] }),
    ]}))
  });
};
 
const threeCol = (rows, w1=2400, w2=3500) => {
  const w3 = 9360 - w1 - w2;
  return new Table({
    width: { size: 9360, type: WidthType.DXA }, columnWidths: [w1, w2, w3],
    rows: rows.map((r, i) => new TableRow({ children: [
      new TableCell({ borders: AB, width: { size: w1, type: WidthType.DXA },
        shading: { fill: i===0 ? LT_BLUE : LT_GRAY, type: ShadingType.CLEAR },
        margins: { top:80, bottom:80, left:120, right:120 },
        children: [new Paragraph({ spacing:{after:0}, children: [i===0?fb(r[0],BLUE):fb(r[0])] })] }),
      new TableCell({ borders: AB, width: { size: w2, type: WidthType.DXA },
        shading: { fill: i===0 ? LT_BLUE : WHITE, type: ShadingType.CLEAR },
        margins: { top:80, bottom:80, left:120, right:120 },
        children: [new Paragraph({ spacing:{after:0}, children: [i===0?fb(r[1],BLUE):f(r[1])] })] }),
      new TableCell({ borders: AB, width: { size: w3, type: WidthType.DXA },
        shading: { fill: i===0 ? LT_BLUE : WHITE, type: ShadingType.CLEAR },
        margins: { top:80, bottom:80, left:120, right:120 },
        children: [new Paragraph({ spacing:{after:0}, children: [i===0?fb(r[2],BLUE):f(r[2])] })] }),
    ]}))
  });
};
 
const phaseHeader = (num, title, fill = MED_BLUE) => new Table({
  width: { size: 9360, type: WidthType.DXA }, columnWidths: [9360],
  rows: [new TableRow({ children: [new TableCell({
    borders: AB, width: { size: 9360, type: WidthType.DXA },
    shading: { fill, type: ShadingType.CLEAR },
    margins: { top: 120, bottom: 120, left: 200, right: 200 },
    children: [new Paragraph({ spacing:{after:0}, children: [fh(`PHASE ${num}: ${title}`, 30, WHITE, true)] })]
  })]})],
});
 
const verifyBox = (items) => new Table({
  width: { size: 9360, type: WidthType.DXA }, columnWidths: [9360],
  rows: [
    new TableRow({ children: [new TableCell({
      borders: AB, width: { size: 9360, type: WidthType.DXA },
      shading: { fill: GREEN_D, type: ShadingType.CLEAR },
      margins: { top:80, bottom:80, left:180, right:180 },
      children: [new Paragraph({ spacing:{after:0}, children: [fh("VERIFICATION CHECKLIST  (Phase is complete only when all pass)", 22, WHITE, true)] })]
    })]}),
    ...items.map(item => new TableRow({ children: [new TableCell({
      borders: AB, width: { size: 9360, type: WidthType.DXA },
      shading: { fill: LT_GREEN, type: ShadingType.CLEAR },
      margins: { top:60, bottom:60, left:180, right:180 },
      children: [new Paragraph({ spacing:{after:0}, children: [f("[ ]  " + item, { color: GREEN_D })] })]
    })]}))
  ]
});
 
const promptBox = (title, lines) => new Table({
  width: { size: 9360, type: WidthType.DXA }, columnWidths: [9360],
  rows: [
    new TableRow({ children: [new TableCell({
      borders: AB, width: { size: 9360, type: WidthType.DXA },
      shading: { fill: PURPLE, type: ShadingType.CLEAR },
      margins: { top:80, bottom:80, left:180, right:180 },
      children: [new Paragraph({ spacing:{after:0}, children: [fh("AI AGENT PROMPT  >>  " + title, 22, WHITE, true)] })]
    })]}),
    new TableRow({ children: [new TableCell({
      borders: AB, width: { size: 9360, type: WidthType.DXA },
      shading: { fill: LT_PURPLE, type: ShadingType.CLEAR },
      margins: { top:100, bottom:100, left:200, right:200 },
      children: lines.map(l => new Paragraph({ spacing:{after:50}, children: [f(l, { color: "2D0052", font: "Courier New", size: 19 })] }))
    })]}),
  ]
});
 
// ─────────────────────────────────────────
const children = [
 
  // COVER
  new Paragraph({ spacing:{ before:1200, after:200 }, alignment:AlignmentType.CENTER,
    children:[fh("INTELLIGENT SCALPING ALGO TRADING SYSTEM", 48, BLUE, true)] }),
  new Paragraph({ spacing:{ after:100 }, alignment:AlignmentType.CENTER,
    children:[fh("Phased Implementation Design Document", 30, MED_BLUE, false)] }),
  new Paragraph({ spacing:{ after:80 }, alignment:AlignmentType.CENTER,
    children:[fh("Indian Equity (NSE/BSE)  |  Zerodha Kite API  |  Java + Spring Boot", 24, GRAY, false)] }),
  new Paragraph({ spacing:{ after:60 }, alignment:AlignmentType.CENTER,
    children:[fh("8 Phases  |  Each Phase Independently Testable  |  Ready for Claude Code", 22, GRAY, false)] }),
  new Paragraph({ spacing:{ before:1800 }, children:[f("")] }),
 
  // ══════════════════════════
  h1("SECTION 1 — SYSTEM OVERVIEW"),
 
  h2("1.1  Objective"),
  P("An autonomous intraday algo trading bot that monitors Nifty 50 / 200 / 500, Nifty Bank, and Sensex stocks in real-time. It detects sudden upward momentum spikes, enters a BUY trade, books partial profit at defined targets, and fully exits within the same session (9:15 AM – 3:30 PM IST). No overnight positions. Zero human intervention required during the trading day."),
  sp(),
  P("A parallel news daemon reads NSE announcements and news headlines. An admin portal (React) lets the operator configure capital limits, thresholds, and strategy parameters before market open. After close, a daily summary auto-generates for review."),
  sp(),
 
  h2("1.2  Core Strategy: Intelligent Momentum Scalping"),
  twoCol([
    ["Concept", "Definition"],
    ["Entry Signal", "Price rises >= 0.5% in 30 seconds + volume spike >= 2x avg + 3 consecutive up-ticks + upward micro-trend"],
    ["Noise Filter", "Vibration (up-down-up) ignored; only sustained directional movement triggers signal"],
    ["Position Sizing", "Capital spread across multiple positions; no single stock > 10% of daily capital"],
    ["Partial Profit (Target 1)", "At +1% gain: sell 40% of position (step-selling / trailing profit)"],
    ["Partial Profit (Target 2)", "At +2% gain: sell another 40% of position"],
    ["Trailing Stop", "Activates after +1.5% gain; stops out if price falls 0.5% below peak"],
    ["Stop Loss", "Exit full position if price falls 0.8% below entry — non-negotiable"],
    ["Hard Close", "3:15 PM IST: all positions force-exited regardless of P&L"],
    ["Market-wide Exit", "If Nifty 50 index falls 1.5% from day open: exit all positions, halt new trades"],
    ["Short Sell", "Future Phase — not in current scope"],
  ]),
  sp(),
 
  h2("1.3  Technology Stack"),
  twoCol([
    ["Component", "Technology"],
    ["Language", "Java 17+"],
    ["Framework", "Spring Boot 3.x (Maven build)"],
    ["Real-time Market Data", "Zerodha Kite WebSocket (KiteTicker) — sub-100ms tick delivery"],
    ["Trade Execution", "Zerodha Kite Connect REST API"],
    ["In-memory Tick Store", "ConcurrentHashMap + ArrayDeque ring buffer (JVM heap, no Redis)"],
    ["Persistent Storage", "PostgreSQL (trade logs, daily reports, configuration)"],
    ["DB Migration", "Flyway"],
    ["News Daemon", "NSE announcements JSON API + Google News RSS"],
    ["Admin Portal UI", "React (Vite) + TailwindCSS"],
    ["Alerting", "Telegram Bot API (push alerts to phone)"],
    ["Logging", "SLF4J + Logback with async appender (never blocks trading thread)"],
  ], 2600),
  sp(),
 
  h2("1.4  Full Project Module Structure"),
  codeBlock([
    "algo-trader/                          <- Spring Boot project root",
    "  pom.xml",
    "  src/main/java/com/algotrader/",
    "    config/           KiteConfig.java, AppConfig.java, SchedulingConfig.java",
    "    websocket/        TickerService.java, TickPipeline.java, TickProcessor.java",
    "    store/            TickDataStore.java",
    "    universe/         InstrumentMasterService.java, StockUniverseService.java",
    "    signal/           MomentumIndicator.java, SignalDetectionEngine.java,",
    "                      Signal.java, SignalBus.java",
    "    risk/             RiskEngine.java, DailyCapitalTracker.java,",
    "                      PositionSizer.java, MarketHealthMonitor.java",
    "    execution/        OrderExecutionService.java, ExitOrderService.java",
    "    position/         PositionManager.java, Position.java, TradeIntent.java",
    "    news/             NewsDaemon.java, NewsSentimentClassifier.java,",
    "                      NewsSignal.java",
    "    admin/            AdminController.java, AdminAuthFilter.java",
    "    reporting/        DailySummaryService.java, TradeLogService.java",
    "    alert/            TelegramAlertService.java",
    "    model/            Tick.java, UniverseStock.java, TradeLog.java,",
    "                      DailySummary.java, NewsLog.java, SystemConfig.java",
    "    controller/       PositionController.java, SignalController.java,",
    "                      UniverseController.java, HealthController.java",
    "  src/main/resources/",
    "    application.yml",
    "    db/migration/     V1__init.sql, V2__news_log.sql",
    "    universe/         nifty500_symbols.txt, banknifty_symbols.txt",
    "  src/test/",
    "    (per-phase unit + integration tests)",
    "",
    "algo-trader-ui/                       <- React admin portal (separate folder)",
    "  package.json (Vite + React + Tailwind)",
    "  src/pages/  Dashboard, Positions, Config, History, Signals, News",
  ]),
  sp(),
 
  h2("1.5  Data Flow — End to End"),
  codeBlock([
    "Kite WebSocket",
    "   |  (tick per stock ~1/sec, up to 3000 stocks)",
    "   v",
    "TickerService.onTicks()  -->  ArrayBlockingQueue<Tick>  [capacity: 50,000]",
    "   |                          (producer-consumer decoupling)",
    "   v",
    "TickProcessorThread polls queue",
    "   |",
    "   +-> TickDataStore.store(tick)   [ring buffer: last 120 ticks per stock]",
    "   |",
    "   +-> SignalDetectionEngine.analyse(tick)",
    "   |       |  [if signal conditions met]",
    "   |       v",
    "   |   SignalBus (BlockingQueue<Signal>)",
    "   |       |",
    "   |       v",
    "   |   RiskEngine.evaluate(signal)",
    "   |       |  [all gates pass]",
    "   |       v",
    "   |   ExecutionQueue (BlockingQueue<TradeIntent>)",
    "   |       |",
    "   |       v",
    "   |   OrderExecutionService.placeBuyOrder(intent)",
    "   |       |",
    "   |       v",
    "   |   PositionManager.addPosition(position)",
    "   |",
    "   +-> PositionManager.updatePosition(tick)  [for all open positions]",
    "           |  [if stop/target/trail hit]",
    "           v",
    "       ExitOrderService.placeExitOrder()  [retries until success]",
    "           |",
    "           v",
    "       TradeLogService.closePosition()  --> PostgreSQL trade_log",
    "",
    "Parallel: NewsDaemon (every 2 min) --> NewsSentimentClassifier --> RiskEngine signal boost/block",
    "Parallel: TelegramAlertService (async) --> your phone",
    "Scheduled: DailySummaryService (3:45 PM) --> daily_summary table + Telegram",
  ]),
  sp(),
 
  // ══════════════════════════
  h1("SECTION 2 — PHASED IMPLEMENTATION"),
 
  infoBox("HOW TO USE:", "Give the 'AI AGENT PROMPT' box of each phase to Claude Code (or similar AI). Do not move to the next phase until all checkboxes in the Verification Checklist are green. Each phase builds on top of the previous.", "E8EAF6", PURPLE),
  sp(),
 
  // PHASE 1
  phaseHeader(1, "PROJECT SKELETON + KITE AUTH + DATABASE FOUNDATION"),
  sp(),
  promptBox("Phase 1", [
    "Create a new Spring Boot 3.x Maven project named 'algo-trader'. Package: com.algotrader",
    "",
    "pom.xml dependencies:",
    "  spring-boot-starter-web",
    "  spring-boot-starter-data-jpa",
    "  org.postgresql:postgresql",
    "  org.flywaydb:flyway-core",
    "  org.projectlombok:lombok",
    "  com.zerodhatech.kiteconnect:kiteconnect:3.3.0  (repo: jitpack.io)",
    "  io.micrometer:micrometer-core",
    "",
    "application.yml — add these keys with placeholder values:",
    "  kite.api-key, kite.api-secret, kite.access-token",
    "  spring.datasource.url, username, password",
    "  telegram.bot-token, telegram.chat-id",
    "  admin.api-key",
    "  trader.max-daily-capital: 100000",
    "  trader.max-open-positions: 5",
    "",
    "Create KiteConfig.java — @Configuration that builds a KiteConnect bean.",
    "  KiteConnect instance uses kite.api-key. Set access-token from kite.access-token.",
    "",
    "Create KiteAuthService.java:",
    "  generateLoginUrl() -> returns kiteConnect.getLoginURL()",
    "  exchangeToken(String requestToken) -> calls kiteConnect.generateSession(requestToken, apiSecret)",
    "    saves access token back to KiteConnect instance",
    "",
    "Create HealthController.java:",
    "  GET /api/health -> returns JSON {status: UP, kiteConnected: true/false, timestamp}",
    "",
    "Create Flyway migration V1__init.sql with exact SQL:",
    "",
    "  CREATE TABLE trade_log (",
    "    id BIGSERIAL PRIMARY KEY,",
    "    symbol VARCHAR(20) NOT NULL,",
    "    instrument_token BIGINT NOT NULL,",
    "    entry_price NUMERIC(10,2), exit_price NUMERIC(10,2),",
    "    quantity INT, pnl NUMERIC(10,2), charges NUMERIC(10,2), net_pnl NUMERIC(10,2),",
    "    entry_time TIMESTAMP, exit_time TIMESTAMP,",
    "    exit_reason VARCHAR(50), signal_type VARCHAR(30), status VARCHAR(20) DEFAULT 'OPEN'",
    "  );",
    "",
    "  CREATE TABLE daily_summary (",
    "    id BIGSERIAL PRIMARY KEY, trade_date DATE UNIQUE NOT NULL,",
    "    total_trades INT DEFAULT 0, winning_trades INT DEFAULT 0, losing_trades INT DEFAULT 0,",
    "    gross_pnl NUMERIC(10,2) DEFAULT 0, total_charges NUMERIC(10,2) DEFAULT 0,",
    "    net_pnl NUMERIC(10,2) DEFAULT 0, capital_used NUMERIC(12,2) DEFAULT 0, notes TEXT",
    "  );",
    "",
    "  CREATE TABLE system_config (",
    "    key VARCHAR(100) PRIMARY KEY, value TEXT NOT NULL, updated_at TIMESTAMP DEFAULT NOW()",
    "  );",
    "",
    "  INSERT INTO system_config VALUES",
    "    ('max_daily_capital','100000',NOW()), ('max_open_positions','5',NOW()),",
    "    ('trade_mode','LONG_ONLY',NOW()), ('trading_enabled','true',NOW()),",
    "    ('stop_loss_pct','0.8',NOW()), ('target1_pct','1.0',NOW()),",
    "    ('target2_pct','2.0',NOW()), ('max_daily_loss_pct','3.0',NOW()),",
    "    ('signal_price_change_pct','0.5',NOW()), ('signal_time_window_sec','30',NOW()),",
    "    ('signal_volume_multiplier','2.0',NOW()), ('signal_confirmation_ticks','3',NOW()),",
    "    ('market_fall_threshold_pct','1.5',NOW());",
    "",
    "Write unit test KiteAuthServiceTest: mock KiteConnect, verify generateLoginUrl is non-null.",
    "Write unit test HealthControllerTest (MockMvc): GET /api/health returns 200 + JSON body.",
  ]),
  sp(),
 
  verifyBox([
    "mvn clean install completes with zero errors and zero test failures",
    "GET /api/health returns 200 JSON: {status:UP, kiteConnected:true}",
    "Flyway runs V1__init.sql: all 3 tables exist in PostgreSQL (verify with psql)",
    "system_config has all 13 rows inserted",
    "KiteAuthServiceTest passes",
    "HealthControllerTest passes",
    "logs/algotrader.log file is created and written by Logback",
  ]),
  sp(),
 
  // PHASE 2
  phaseHeader(2, "REAL-TIME TICK PIPELINE — WEBSOCKET TO RING BUFFER TO PROCESSOR", "1F4E79"),
  sp(),
  promptBox("Phase 2", [
    "Build the real-time tick ingestion pipeline on top of Phase 1.",
    "",
    "1. Create model/Tick.java (POJO wrapping com.zerodhatech.models.Tick fields we need):",
    "   long instrumentToken, String symbol, double lastPrice, long volume,",
    "   double averagePrice, double open, double high, double low, double close,",
    "   long buyQuantity, long sellQuantity, java.time.Instant tickTime",
    "",
    "2. Create store/TickDataStore.java (@Component, thread-safe):",
    "   private final ConcurrentHashMap<Long, ArrayDeque<Tick>> store = new ConcurrentHashMap<>()",
    "   void addTick(Tick t): get or create deque for token; add tick; if size > 120 removeFirst()",
    "   Tick getLatest(long token): returns store.get(token).peekLast() or null",
    "   List<Tick> getLast(long token, int n): returns last n ticks from deque",
    "   List<Tick> getTicksInWindow(long token, int seconds): filter ticks where",
    "     tickTime >= Instant.now().minusSeconds(seconds)",
    "",
    "3. Create websocket/TickerService.java (@Service):",
    "   Inject KiteConnect and TickPipeline.",
    "   void connect(List<Long> tokens): creates KiteTicker, registers callbacks, calls connect()",
    "   KiteTicker callbacks:",
    "     onTicks(ArrayList<com.zerodhatech.models.Tick> ticks): map each to our Tick model,",
    "       offer to TickPipeline queue. If queue full: log WARN 'TICK DROPPED token=X'",
    "     onDisconnected(): log WARN, schedule reconnect attempt (exponential back-off)",
    "     onError(Exception e): log ERROR, send Telegram alert",
    "     onConnected(): log INFO 'WebSocket CONNECTED', re-subscribe tokens if reconnecting",
    "   Reconnect strategy: ScheduledExecutorService, delay sequence [1,2,4,8,16,30] seconds,",
    "     reset sequence after successful connect",
    "   void updateSubscriptions(List<Long> tokens): diff old vs new, subscribe new, unsubscribe removed",
    "",
    "4. Create websocket/TickPipeline.java (@Component):",
    "   private final ArrayBlockingQueue<Tick> queue = new ArrayBlockingQueue<>(50_000)",
    "   void offer(Tick t): non-blocking offer to queue",
    "   @PostConstruct startProcessorThread(): daemon thread, loop: Tick t = queue.take(); process(t)",
    "   @Scheduled(fixedRate=5000) logQueueDepth(): log INFO 'Tick queue depth: {queue.size()}'",
    "",
    "5. Create websocket/TickProcessor.java (@Component):",
    "   void process(Tick tick):",
    "     long start = System.nanoTime()",
    "     tickDataStore.addTick(tick)",
    "     signalDetectionEngine.onTick(tick)   // stub: just return for Phase 2",
    "     positionManager.onTick(tick)         // stub: just return for Phase 2",
    "     long elapsed = (System.nanoTime()-start)/1_000_000",
    "     if (elapsed > 50) log WARN 'Slow tick processing: {}ms token={}', elapsed, tick.token",
    "",
    "6. Create alert/TelegramAlertService.java (@Service):",
    "   Inject RestTemplate. Read telegram.bot-token and telegram.chat-id from @Value.",
    "   @Async void sendAlert(String msg): POST to https://api.telegram.org/bot{token}/sendMessage",
    "     body: {chat_id, text: msg, parse_mode: HTML}. Catch + log exceptions, never throw.",
    "   @Async void sendCritical(String msg): sendAlert('[CRITICAL] ' + msg)",
    "",
    "7. Create InstrumentLoaderService.java (temporary, Phase 3 will replace):",
    "   Returns hardcoded List<Long> of 20 Nifty50 instrument tokens for initial testing.",
    "   (Get actual tokens from https://api.kite.trade/instruments CSV for NSE)",
    "   @PostConstruct: calls tickerService.connect(getTokens())",
    "",
    "Write tests:",
    "  TickDataStoreTest:",
    "    - Add 125 ticks for token 111 -> verify size is capped at 120",
    "    - getTicksInWindow(token, 30) with mix of old/new ticks -> verify only recent returned",
    "  TickPipelineTest:",
    "    - Offer 10,000 mock ticks -> verify all 10,000 processed by processor (count via AtomicInt)",
    "    - Must complete in under 3 seconds",
    "  TelegramAlertServiceTest:",
    "    - Mock RestTemplate, call sendAlert -> verify POST URL and body are correct",
  ]),
  sp(),
  infoBox("SYSTEM DESIGN — Ring Buffer:", "TickDataStore keeps only last 120 ticks per stock. Old data is evicted automatically (O(1)). This prevents memory growing over 6.25 hours. ConcurrentHashMap ensures thread-safety between WebSocket thread (writer) and analysis thread (reader). This is the same principle as LMAX Disruptor used in high-frequency trading.", YELLOW_BG, ORANGE_D),
  sp(),
  infoBox("SYSTEM DESIGN — Back Pressure:", "If TickPipeline queue hits 50,000 items, it means the processor can't keep up. We DROP the tick (load shedding) instead of blocking the WebSocket thread. Blocking the WebSocket thread would cause the connection to lag and eventually disconnect. Dropping a few ticks is acceptable — missing a momentum is better than losing the connection entirely.", "E8F4FD", MED_BLUE),
  sp(),
 
  verifyBox([
    "Console shows tick logs: [TICK] RELIANCE 2847.50 vol=123456 at HH:mm:ss.SSS",
    "TickDataStoreTest: 125 ticks added, size stays at 120 (oldest evicted)",
    "TickDataStoreTest: getTicksInWindow returns only ticks in last N seconds",
    "TickPipelineTest: 10,000 ticks processed in under 3 seconds",
    "Queue depth log appears every 5 seconds in console",
    "Disconnect simulation: stop network -> reconnect attempts logged with back-off delays",
    "Reconnect restores tick flow automatically (ticks resume in logs)",
    "TelegramAlertServiceTest: correct POST URL and body verified",
    "Send a test Telegram message manually: verify it arrives on phone",
  ]),
  sp(),
 
  // PHASE 3
  phaseHeader(3, "STOCK UNIVERSE MANAGEMENT (NIFTY 500 + BANKNIFTY)", "375623"),
  sp(),
  promptBox("Phase 3", [
    "Replace the Phase 2 hardcoded 20-token list with a full dynamic universe manager.",
    "",
    "1. Add to application.yml:",
    "   universe.refresh-cron: '0 8 * * MON-FRI'",
    "   universe.min-price: 50.0",
    "   universe.blacklist: []",
    "",
    "2. Create universe/InstrumentMasterService.java (@Service):",
    "   @PostConstruct and @Scheduled(cron) void refresh():",
    "     GET https://api.kite.trade/instruments (returns CSV, use RestTemplate)",
    "     Parse CSV using Apache Commons CSV (add commons-csv to pom.xml)",
    "     Build Map<String,Long> symbolToToken and Map<Long,String> tokenToSymbol",
    "     CSV columns: instrument_token, exchange_token, tradingsymbol, name,",
    "       last_price, expiry, strike, tick_size, lot_size, instrument_type, segment, exchange",
    "     Filter: exchange=NSE, instrument_type=EQ, last_price >= universe.min-price",
    "   Long getToken(String symbol): returns symbolToToken.get(symbol)",
    "   String getSymbol(Long token): returns tokenToSymbol.get(token)",
    "",
    "3. Create resource files (populate with real NSE symbols, one per line):",
    "   src/main/resources/universe/nifty500_symbols.txt  (Nifty 500 NSE symbols)",
    "   src/main/resources/universe/banknifty_symbols.txt (Nifty Bank NSE symbols)",
    "   Also add a line: 256265  in a file nifty_index_token.txt (Nifty50 index token)",
    "",
    "4. Create universe/StockUniverseService.java (@Service):",
    "   @PostConstruct void loadUniverse():",
    "     Read nifty500_symbols.txt and banknifty_symbols.txt from classpath",
    "     For each symbol: resolve token via InstrumentMasterService.getToken(symbol)",
    "     Skip symbols not found (log WARN 'Symbol not found in instrument master: {}')",
    "     Skip blacklisted symbols (from universe.blacklist config)",
    "     Always add Nifty50 index token 256265 (for MarketHealthMonitor)",
    "     Build Map<Long, UniverseStock> subscribedStocks",
    "     Call tickerService.updateSubscriptions(new ArrayList<>(subscribedStocks.keySet()))",
    "   List<Long> getActiveTokens(): returns list of token keys",
    "   void blacklist(String symbol): remove from map, update subscriptions",
    "",
    "5. Create model/UniverseStock.java:",
    "   long token, String symbol, String name, String exchange, Set<String> indices",
    "",
    "6. Create controller/UniverseController.java (@RestController):",
    "   GET /api/universe: returns {count, stocks:[{token,symbol,indices}]}",
    "   POST /api/universe/refresh: calls stockUniverseService.loadUniverse()",
    "   POST /api/universe/blacklist/{symbol}: calls stockUniverseService.blacklist(symbol)",
    "",
    "Write tests:",
    "  InstrumentMasterServiceTest:",
    "    Mock RestTemplate to return 5 CSV rows. Verify symbolToToken map has correct entries.",
    "    Verify instrument_type != EQ is excluded. Verify last_price < 50 is excluded.",
    "  StockUniverseServiceTest:",
    "    Mock InstrumentMasterService. Load from test symbol file. Verify token count.",
    "    Verify blacklisted symbol is not in subscribedStocks.",
    "  UniverseControllerTest (MockMvc):",
    "    GET /api/universe returns 200 + JSON with count field.",
  ]),
  sp(),
 
  verifyBox([
    "GET /api/universe returns 400+ instruments (after real Kite instruments CSV downloaded)",
    "Every instrument has non-zero instrument_token",
    "Nifty 50 index token 256265 always present in subscribed list",
    "Blacklisting RELIANCE via POST removes it from universe and Kite subscription",
    "POST /api/universe/refresh re-loads without restarting app",
    "Stocks with last_price < 50 are excluded (InstrumentMasterServiceTest passes)",
    "No duplicate tokens in subscription list (assert set size == list size in test)",
    "Instrument master refreshes automatically at 8:00 AM (verify cron fires in test with mocked clock)",
  ]),
  sp(),
 
  // PHASE 4
  phaseHeader(4, "SIGNAL DETECTION ENGINE — MOMENTUM + SPIKE FILTER", "BF5700"),
  sp(),
  promptBox("Phase 4", [
    "Build the SignalDetectionEngine. Replace Phase 2 stub in TickProcessor.",
    "",
    "All thresholds from application.yml (signal.* prefix). Never hardcode numbers in logic.",
    "",
    "1. Create model/Signal.java (immutable, use @Value from Lombok or Java record):",
    "   long instrumentToken, String symbol, Instant signalTime, double triggerPrice,",
    "   double priceChangePct, double volumeMultiplier, String strength, // WEAK/MODERATE/STRONG",
    "   int ticksAnalysed",
    "",
    "2. Create signal/MomentumIndicator.java (@Component):",
    "   All methods are pure functions (no side effects).",
    "",
    "   double priceAcceleration(List<Tick> ticks, int windowSec):",
    "     oldest = first tick where tickTime >= now-windowSec",
    "     return (latest.lastPrice - oldest.lastPrice) / oldest.lastPrice * 100",
    "     return 0.0 if fewer than 2 ticks in window",
    "",
    "   double volumeSpike(List<Tick> ticks):",
    "     avgVol = average of ticks[0..size-4].volume  (exclude last 3 ticks for avg)",
    "     currentVol = ticks.get(ticks.size()-1).volume",
    "     return avgVol == 0 ? 0 : currentVol / avgVol",
    "",
    "   boolean consecutiveUpTicks(List<Tick> ticks, int n):",
    "     check last n ticks: each tick's lastPrice > previous tick's lastPrice",
    "     return false if fewer than n+1 ticks",
    "",
    "   String microTrendDirection(List<Tick> ticks):",
    "     look at last 5 ticks. Count ups (price[i] > price[i-1]) and downs.",
    "     if ups >= 4: return UP. if downs >= 4: return DOWN. else: SIDEWAYS",
    "",
    "   boolean isNoisySpike(List<Tick> ticks):",
    "     if size < 4: return false",
    "     prev3 = ticks.get(size-3), prev2 = ticks.get(size-2), prev1 = ticks.get(size-1)",
    "     spikeUp = (prev2.lastPrice - prev3.lastPrice) / prev3.lastPrice * 100",
    "     reversal = (prev1.lastPrice - prev2.lastPrice) / prev2.lastPrice * 100",
    "     return spikeUp >= 0.3 && reversal <= -0.2   // spike then reversal = noise",
    "",
    "3. Create signal/SignalDetectionEngine.java (@Service):",
    "   Inject TickDataStore, MomentumIndicator, SignalBus, and all signal.* config values.",
    "   ConcurrentHashMap<Long, Instant> cooldownMap  // per-symbol cooldown tracking",
    "",
    "   void onTick(Tick tick):  // called from TickProcessor",
    "     if not in market hours (9:15-15:20) return",
    "     List<Tick> ticks = tickDataStore.getLast(tick.instrumentToken, 60)",
    "     if ticks.size() < 10: return  // not enough history",
    "     if in cooldown (cooldownMap has entry < 60 seconds ago): return",
    "     double pa = momentumIndicator.priceAcceleration(ticks, signalTimeWindowSec)",
    "     double vs = momentumIndicator.volumeSpike(ticks)",
    "     boolean cup = momentumIndicator.consecutiveUpTicks(ticks, confirmationTicks)",
    "     String trend = momentumIndicator.microTrendDirection(ticks)",
    "     boolean noisy = momentumIndicator.isNoisySpike(ticks)",
    "     if pa >= priceChangePctThreshold && vs >= volumeSpikeMultiplier",
    "        && cup && trend.equals('UP') && !noisy:",
    "       String strength = pa >= 1.0 ? 'STRONG' : pa >= 0.7 ? 'MODERATE' : 'WEAK'",
    "       Signal s = new Signal(tick.token, tick.symbol, Instant.now(),",
    "                             tick.lastPrice, pa, vs, strength, ticks.size())",
    "       signalBus.publish(s)",
    "       cooldownMap.put(tick.instrumentToken, Instant.now())",
    "       log INFO '[SIGNAL] {} price+{}% vol{}x strength={}', symbol, pa, vs, strength",
    "",
    "4. Create signal/SignalBus.java (@Component):",
    "   private final ArrayBlockingQueue<Signal> queue = new ArrayBlockingQueue<>(1000)",
    "   private final List<Signal> recentSignals = Collections.synchronizedList(new ArrayList<>())",
    "   void publish(Signal s): queue.offer(s); recentSignals.add(s); trim list to 200 entries",
    "   @PostConstruct startConsumer(): daemon thread polls queue, calls riskEngine.evaluate(s) [stub]",
    "   List<Signal> getRecent(int n): return last n from recentSignals",
    "",
    "5. Create controller/SignalController.java:",
    "   GET /api/signals/recent: returns last 50 signals",
    "   GET /api/signals/stats: returns {total, todayCount, strongCount, moderateCount, weakCount}",
    "",
    "Write tests:",
    "  MomentumIndicatorTest (each method separately):",
    "    priceAcceleration: 30 ticks, price goes from 100 to 100.6 in 25 sec -> result >= 0.5",
    "    volumeSpike: avg vol = 10000, current = 22000 -> result >= 2.0",
    "    consecutiveUpTicks: 5 ticks each 0.1 higher -> returns true for n=3",
    "    consecutiveUpTicks: tick 3 dips then rises -> returns false for n=3",
    "    microTrendDirection: 4 of last 5 ticks up -> returns UP",
    "    isNoisySpike: spike up 0.4% then reversal -0.25% -> returns true",
    "    isNoisySpike: steady rise -> returns false",
    "  SignalDetectionEngineTest:",
    "    Feed tick sequence matching all 5 conditions -> verify Signal published to bus",
    "    Feed same sequence again within 60s -> verify NO second signal (cooldown)",
    "    Feed ticks during 8:00 AM (before market) -> verify no signal generated",
  ]),
  sp(),
  infoBox("SYSTEM DESIGN — Event-Driven Pipeline:", "Tick -> SignalDetectionEngine -> SignalBus -> RiskEngine -> OrderExecutionService is a classic event-driven pipeline. Each stage is decoupled via a queue. If RiskEngine is slow, signals queue in SignalBus — the tick processor never blocks. This is the same pattern as Kafka-based architectures but implemented in-process for ultra-low latency.", YELLOW_BG, ORANGE_D),
  sp(),
 
  verifyBox([
    "MomentumIndicatorTest: all 7 test cases pass",
    "SignalDetectionEngineTest: signal generated from trigger sequence, not generated in cooldown",
    "Ticks before 9:15 AM generate no signals",
    "GET /api/signals/recent returns correct JSON array",
    "Logs show [SIGNAL] entries when test ticks are injected",
    "All signal thresholds change via application.yml without code change",
    "End-to-end: real Kite ticks arrive -> some stocks trigger signal -> logged (manual market test)",
  ]),
  sp(),
 
  // PHASE 5
  phaseHeader(5, "RISK MANAGEMENT ENGINE", "7B1FA2"),
  sp(),
  promptBox("Phase 5", [
    "Build the RiskEngine that gates every Signal before it becomes an order.",
    "Replace SignalBus consumer stub from Phase 4.",
    "",
    "All limits from system_config table (read at startup, re-read if config changes via Admin API).",
    "",
    "1. Create risk/DailyCapitalTracker.java (@Service):",
    "   double capitalDeployed, double realizedPnl, int openPositionCount, int totalTradesCount",
    "   void allocate(double amount): capitalDeployed += amount",
    "   void release(double amount): capitalDeployed -= amount",
    "   void addPnl(double pnl): realizedPnl += pnl",
    "   double getAvailableCapital(): maxDailyCapital - capitalDeployed",
    "   boolean isDailyLossBreached(): realizedPnl < -(maxDailyCapital * maxDailyLossPct/100)",
    "   @Scheduled(cron='0 0 18 * * MON-FRI') reset(): set all fields to 0",
    "",
    "2. Create risk/PositionSizer.java (@Component):",
    "   PositionSize calculate(double availableCapital, double ltp, double maxPerTradePct):",
    "     double maxAmount = availableCapital * (maxPerTradePct / 100)",
    "     int qty = (int) Math.floor(maxAmount / ltp)",
    "     if qty < 1: return null (signal rejected, not enough capital)",
    "     double actualAmount = qty * ltp",
    "     double charges = estimateCharges(qty, ltp)",
    "     return new PositionSize(qty, actualAmount, charges)",
    "",
    "   double estimateCharges(int qty, double price):",
    "     double turnover = qty * price",
    "     double brokerage = 40.0  // Rs 20 each side, flat Zerodha rate",
    "     double stt = turnover * 0.00025   // 0.025% on sell side (intraday)",
    "     double exchangeCharge = turnover * 2 * 0.0000345  // both sides",
    "     double sebi = turnover * 2 * 0.000001",
    "     double gst = (brokerage + exchangeCharge + sebi) * 0.18",
    "     double stamp = turnover * 0.00003  // buy side only",
    "     return brokerage + stt + exchangeCharge + sebi + gst + stamp",
    "",
    "3. Create risk/MarketHealthMonitor.java (@Service):",
    "   Inject TickDataStore. Nifty50 index token: 256265 (always subscribed).",
    "   double niftyOpenPrice = 0 (set on first tick after 9:15 AM)",
    "   boolean isMarketFalling():",
    "     Tick latest = tickDataStore.getLatest(256265)",
    "     if niftyOpenPrice == 0 or latest == null: return false",
    "     double change = (latest.lastPrice - niftyOpenPrice)/niftyOpenPrice*100",
    "     return change <= -marketFallThresholdPct",
    "   void onTick(Tick t): if token==256265 && niftyOpenPrice==0 && after 9:15: set niftyOpenPrice",
    "",
    "4. Create risk/RiskEngine.java (@Service):",
    "   Consumes from SignalBus (replace stub in SignalBus consumer).",
    "   Inject: DailyCapitalTracker, PositionSizer, MarketHealthMonitor, OrderExecutionQueue.",
    "   Read from system_config: trading_enabled, max_open_positions, max_capital_per_trade_pct.",
    "",
    "   void evaluate(Signal signal):",
    "     Gate 1: tradingEnabled from DB -> if false: log+return",
    "     Gate 2: isDailyLossBreached() -> if true: log+alert+return",
    "     Gate 3: openPositionCount >= maxOpenPositions -> log+return",
    "     Gate 4: symbol already has open position -> log+return",
    "     Gate 5: isMarketFalling() -> log+alert+return (also trigger exit all)",
    "     Gate 6: getAvailableCapital() < 5000 -> log+return",
    "     PositionSize ps = positionSizer.calculate(availableCapital, signal.triggerPrice, maxPerTradePct)",
    "     Gate 7: ps == null (qty < 1) -> log+return",
    "     Compute: stopLossPrice = signal.triggerPrice * (1 - stopLossPct/100)",
    "     Compute: target1Price = signal.triggerPrice * (1 + target1Pct/100)",
    "     Compute: target2Price = signal.triggerPrice * (1 + target2Pct/100)",
    "     Create TradeIntent {signal, ps.qty, ps.actualAmount, stopLossPrice, target1Price, target2Price}",
    "     Publish to executionQueue",
    "     dailyCapitalTracker.allocate(ps.actualAmount)",
    "",
    "5. Add REST: GET /api/risk/status:",
    "   Returns: {capitalDeployed, availableCapital, realizedPnl, openPositions,",
    "            dailyLossBreached, marketFalling, tradingEnabled}",
    "",
    "Write tests:",
    "  PositionSizerTest:",
    "    capital=100000, maxPct=10, ltp=500 -> qty=200, amount=100000 (verify rounding)",
    "    capital=4000, ltp=500 -> qty<1 -> returns null",
    "  ChargesEstimationTest: qty=100, price=1000 -> verify each charge component formula",
    "  RiskEngineTest (mock all dependencies):",
    "    Gate 1: trading_enabled=false -> no TradeIntent published",
    "    Gate 2: dailyLoss breached -> no intent, Telegram alert sent",
    "    Gate 3: openPositions=5 -> no intent",
    "    Gate 4: duplicate symbol -> no intent",
    "    Gate 5: isMarketFalling=true -> no intent, alert sent",
    "    All gates pass: TradeIntent published with correct stopLoss and target prices",
    "  MarketHealthMonitorTest: inject Nifty ticks showing -1.6% -> isMarketFalling()==true",
    "  DailyCapitalTrackerTest: add losses until breach, verify flag; verify reset at 6PM",
  ]),
  sp(),
  infoBox("SYSTEM DESIGN — Circuit Breaker:", "dailyLossBreached and isMarketFalling are circuit breakers. When triggered, the system opens the circuit and stops accepting new signals. This prevents a bad strategy day from compounding into catastrophic loss. The circuit resets at 6 PM (no more trading until next day). Same pattern as SEBI's own market circuit breakers.", YELLOW_BG, ORANGE_D),
  sp(),
 
  verifyBox([
    "PositionSizerTest: all 2 scenarios correct",
    "ChargesEstimationTest: matches Zerodha's own brokerage calculator within 5%",
    "All 6 gate tests pass independently in RiskEngineTest",
    "MarketHealthMonitorTest: 1.6% Nifty fall -> isMarketFalling returns true",
    "DailyCapitalTrackerTest: reset verified",
    "GET /api/risk/status returns correct current state JSON",
    "End-to-end: block a signal by setting trading_enabled=false in DB, verify no order placed",
  ]),
  sp(),
 
  // PHASE 6
  phaseHeader(6, "TRADE EXECUTION ENGINE + POSITION LIFECYCLE MANAGER", "C00000"),
  sp(),
  promptBox("Phase 6", [
    "Build order execution and position lifecycle (entry -> partial exits -> full exit -> logging).",
    "",
    "1. Create model/TradeIntent.java:",
    "   Signal signal, int qty, double investmentAmount, double stopLossPrice,",
    "   double target1Price, double target2Price, Instant createdAt",
    "",
    "2. Create model/Position.java:",
    "   long instrumentToken, String symbol, double entryPrice, double currentPrice,",
    "   int totalQty, int remainingQty, double stopLossPrice, double target1Price, double target2Price,",
    "   double peakPrice, boolean target1Hit, boolean target2Hit, boolean trailingStopActive,",
    "   Instant entryTime, String status // OPEN | CLOSING | CLOSED",
    "   double unrealizedPnl(): return (currentPrice - entryPrice) * remainingQty",
    "",
    "3. Create execution/OrderExecutionService.java (@Service):",
    "   Inject KiteConnect. Rate limiter: Guava RateLimiter.create(10) (max 10 orders/sec).",
    "",
    "   void startConsumer(): daemon thread consumes from executionQueue (BlockingQueue<TradeIntent>)",
    "",
    "   String placeBuyOrder(TradeIntent intent):",
    "     rateLimiter.acquire()",
    "     OrderParams params = new OrderParams()",
    "       .tradingsymbol(intent.signal.symbol).exchange('NSE')",
    "       .orderType(OrderType.MARKET).transactionType('BUY')",
    "       .product(Product.MIS).validity(Validity.DAY).quantity(intent.qty)",
    "     try:",
    "       Order order = kiteConnect.placeOrder(params, 'regular')",
    "       log INFO '[BUY ORDER] {} qty={} orderId={}', symbol, qty, order.orderId",
    "       return order.orderId",
    "     catch (Exception e):",
    "       log ERROR '[BUY FAILED] {} attempt={} reason={}', symbol, attempt, e.getMessage()",
    "       return null",
    "",
    "   BUY retry logic:",
    "     Max 2 retries. Between retries, wait 500ms.",
    "     Before retry: check if momentum still valid:",
    "       latestPrice = tickDataStore.getLatest(token).lastPrice",
    "       if (latestPrice - intent.signal.triggerPrice)/intent.signal.triggerPrice < -0.003: abandon",
    "     If both retries fail: log WARN 'BUY abandoned - momentum lost', do not retry further",
    "",
    "   String placeSellOrder(String symbol, long token, int qty, String reason):",
    "     // EXIT IS SACRED — retry until success",
    "     int attempt = 0",
    "     while (true):",
    "       rateLimiter.acquire()",
    "       try:",
    "         place SELL MIS MARKET order",
    "         log INFO '[EXIT] {} qty={} reason={} orderId={}', symbol, qty, reason, orderId",
    "         return orderId",
    "       catch (Exception e):",
    "         attempt++",
    "         log ERROR '[EXIT FAILED] {} attempt={} reason={}', symbol, attempt, e.getMessage()",
    "         if (attempt % 3 == 0): telegramAlert.sendCritical('EXIT FAILING ' + symbol + ' attempt ' + attempt)",
    "         long delay = attempt <= 10 ? 1000 : 500   // urgent mode after 10 attempts",
    "         Thread.sleep(delay)",
    "",
    "4. Create position/PositionManager.java (@Service):",
    "   ConcurrentHashMap<Long, Position> openPositions (keyed by instrumentToken)",
    "",
    "   void addPosition(Position p): openPositions.put(p.instrumentToken, p)",
    "     dailyCapitalTracker.openPositionCount++",
    "     tradeLogService.logEntry(p)",
    "",
    "   void onTick(Tick tick):  // called from TickProcessor",
    "     Position p = openPositions.get(tick.instrumentToken)",
    "     if p == null || p.status != OPEN: return",
    "     p.currentPrice = tick.lastPrice",
    "     p.peakPrice = Math.max(p.peakPrice, p.currentPrice)",
    "",
    "     // Check stop loss",
    "     if p.currentPrice <= p.stopLossPrice:",
    "       exitPosition(p, 'STOP_LOSS', p.remainingQty)",
    "       return",
    "",
    "     // Check target 1",
    "     if !p.target1Hit && p.currentPrice >= p.target1Price:",
    "       int sellQty = (int)(p.totalQty * 0.40)",
    "       exitPosition(p, 'TARGET_1', sellQty)",
    "       p.target1Hit = true",
    "       p.remainingQty -= sellQty",
    "",
    "     // Check target 2",
    "     if p.target1Hit && !p.target2Hit && p.currentPrice >= p.target2Price:",
    "       int sellQty = (int)(p.totalQty * 0.40)",
    "       exitPosition(p, 'TARGET_2', sellQty)",
    "       p.target2Hit = true",
    "       p.remainingQty -= sellQty",
    "",
    "     // Trailing stop",
    "     double peakGainPct = (p.peakPrice - p.entryPrice)/p.entryPrice*100",
    "     if peakGainPct >= trailingStopActivationPct:",
    "       p.trailingStopActive = true",
    "       double dynStop = p.peakPrice * (1 - trailingStopDistancePct/100)",
    "       if p.currentPrice <= dynStop:",
    "         exitPosition(p, 'TRAILING_STOP', p.remainingQty)",
    "",
    "   void exitPosition(Position p, String reason, int qty):",
    "     p.status = CLOSING",
    "     orderExecutionService.placeSellOrder(p.symbol, p.instrumentToken, qty, reason)",
    "     if qty >= p.remainingQty: // full exit",
    "       p.status = CLOSED",
    "       openPositions.remove(p.instrumentToken)",
    "       dailyCapitalTracker.openPositionCount--",
    "       dailyCapitalTracker.release(p.investmentAmount)",
    "       double pnl = (p.currentPrice - p.entryPrice) * qty",
    "       dailyCapitalTracker.addPnl(pnl)",
    "       tradeLogService.logExit(p, reason)",
    "",
    "   @Scheduled(cron='15 15 * * MON-FRI') eodHardClose():",
    "     log WARN 'EOD HARD CLOSE - exiting all positions'",
    "     telegramAlert.sendAlert('EOD CLOSE triggered - exiting all open positions')",
    "     openPositions.values().forEach(p -> exitPosition(p, 'EOD_CLOSE', p.remainingQty))",
    "",
    "5. Create reporting/TradeLogService.java (@Service):",
    "   void logEntry(Position p): INSERT into trade_log (status=OPEN)",
    "   void logExit(Position p, String reason): UPDATE trade_log set exit_price, exit_time,",
    "     exit_reason, pnl, charges, net_pnl, status=CLOSED",
    "   List<TradeLog> getTodayTrades(): SELECT * FROM trade_log WHERE entry_time::date = TODAY",
    "",
    "6. Create controller/PositionController.java:",
    "   GET /api/positions: returns all open positions with unrealizedPnl",
    "   GET /api/positions/history: returns today's closed positions from trade_log",
    "   POST /api/positions/{symbol}/exit: manual override -> exitPosition for that symbol",
    "",
    "Write tests:",
    "  OrderExecutionServiceTest: mock KiteConnect, verify SELL order params (MIS, MARKET, NSE)",
    "  ExitRetryTest: mock Kite failing 4 times then succeeding; verify 4 attempts logged,",
    "    Telegram alert sent at attempt 3; order eventually placed",
    "  PositionManagerTest:",
    "    Scenario A: entry=100, stopLoss=99.2, feed tick at 99.1 -> exitPosition called with STOP_LOSS",
    "    Scenario B: entry=100, target1=101, feed tick at 101.5 -> partial sell 40% triggered",
    "    Scenario C: entry=100, peak=102, trailing active, feed tick at 101.0 -> exit TRAILING_STOP",
    "    Scenario D: target1 and target2 both hit in sequence -> 80% sold in two tranches",
    "  EodExitTest: mock clock at 15:15 -> all 3 mock open positions exited",
    "  TradeLogServiceTest: verify INSERT and UPDATE SQL correctness using H2 test DB",
  ]),
  sp(),
  infoBox("CRITICAL RULE — Exit is Sacred:", "BUY failures are acceptable (opportunity missed). SELL failures are real money loss. The placeSellOrder loop never gives up. It retries every 1 second, sends Telegram alert every 3rd failure, then switches to 500ms ultra-fast retry mode after 10 attempts. This is a deliberate infinite retry for exit orders only.", "FFE8E8", RED_D),
  sp(),
 
  h4("Exit Priority Reference"),
  threeCol([
    ["Exit Type", "Trigger Condition", "Action"],
    ["Stop Loss", "currentPrice <= stopLossPrice", "Immediate MARKET SELL, all remaining qty"],
    ["Target 1", "currentPrice >= target1Price", "MARKET SELL 40% of original qty"],
    ["Target 2", "currentPrice >= target2Price", "MARKET SELL 40% of original qty"],
    ["Trailing Stop", "price falls below peakPrice * (1-distance%)", "MARKET SELL remaining qty"],
    ["Market Fall", "Nifty50 index down >= 1.5% from open", "Exit ALL positions immediately"],
    ["EOD Hard Close", "3:15 PM IST clock", "MARKET SELL ALL open positions"],
    ["Manual Override", "POST /api/positions/{symbol}/exit", "Immediate MARKET SELL"],
    ["Daily Loss Limit", "Net P&L < -(3% of daily capital)", "Exit ALL + halt trading"],
  ], 2200, 3800),
  sp(),
 
  verifyBox([
    "OrderExecutionServiceTest: BUY and SELL order params verified (MIS, MARKET, NSE, DAY)",
    "ExitRetryTest: 4 failures + 1 success; Telegram alert at attempt 3; total 5 order calls",
    "PositionManagerTest: all 4 scenarios (stop, target1, target2+trail) pass",
    "EodExitTest: 3:15 PM cron exits all open positions",
    "trade_log has correct OPEN entry after buy, CLOSED entry after exit (H2 test DB)",
    "GET /api/positions returns unrealizedPnl = (currentPrice - entryPrice) * remainingQty",
    "POST /api/positions/RELIANCE/exit triggers exit immediately (MockMvc test)",
    "Rate limiter: 20 rapid sell orders take at least 2 seconds (unit test with timer)",
  ]),
  sp(),
 
  // PHASE 7
  phaseHeader(7, "NEWS DAEMON + ADMIN REST API + DAILY SUMMARY REPORT", "1F4E79"),
  sp(),
  promptBox("Phase 7", [
    "=== PART A: NEWS DAEMON ===",
    "",
    "1. Add Flyway V2__news_log.sql:",
    "   CREATE TABLE news_log (",
    "     id BIGSERIAL PRIMARY KEY, symbol VARCHAR(20), headline TEXT,",
    "     source VARCHAR(50), sentiment VARCHAR(10), confidence NUMERIC(4,2),",
    "     published_at TIMESTAMP, processed_at TIMESTAMP DEFAULT NOW()",
    "   );",
    "",
    "2. Create news/NewsSentimentClassifier.java (@Component):",
    "   String[] BULLISH = {'results beat','profit up','order win','record revenue',",
    "     'expansion','partnership','buyback','dividend','upgrade','outperform'}",
    "   String[] BEARISH = {'loss','downgrade','fraud','scam','probe','fine','penalty',",
    "     'recall','shutdown','resign','miss','lower guidance','below estimate'}",
    "",
    "   NewsClassification classify(String headline):",
    "     String h = headline.toLowerCase()",
    "     int bullCount = count of BULLISH phrases found in h",
    "     int bearCount = count of BEARISH phrases found in h",
    "     if bullCount == 0 && bearCount == 0: return {NEUTRAL, 0.0}",
    "     if bullCount > bearCount: return {BULLISH, min(1.0, bullCount*0.3)}",
    "     return {BEARISH, min(1.0, bearCount*0.3)}",
    "",
    "3. Create news/NewsDaemon.java (@Service):",
    "   @Scheduled(fixedRate=120_000) // every 2 minutes",
    "   void fetchAndProcess():",
    "     if not in market hours (9:00-15:35): return",
    "     fetchNseAnnouncements()",
    "     fetchGoogleNewsForTopStocks()",
    "",
    "   void fetchNseAnnouncements():",
    "     GET https://www.nseindia.com/api/corporate-announcements",
    "     Headers: User-Agent=Mozilla/5.0, Accept=application/json",
    "     Parse JSON array. For each item with field 'symbol':",
    "       if symbol in stockUniverseService.getActiveSymbols():",
    "         classify headline, save to news_log",
    "         if BEARISH && positionManager.hasOpenPosition(symbol):",
    "           log WARN '[NEWS RISK] Bearish news for open position: {}', symbol",
    "           telegramAlert.sendAlert('BEARISH NEWS: ' + symbol + ' - ' + headline)",
    "",
    "   void fetchGoogleNewsForTopStocks():",
    "     For top 20 most-active symbols only (to avoid rate limit):",
    "       GET https://news.google.com/rss/search?q={symbol}+NSE+stock+India&hl=en-IN",
    "       Parse RSS XML (use javax.xml.parsers.DocumentBuilderFactory)",
    "       Extract <title> from each <item>. Classify and save.",
    "",
    "=== PART B: ADMIN REST API ===",
    "",
    "4. Create admin/AdminAuthFilter.java (OncePerRequestFilter):",
    "   Apply to /api/admin/** paths only.",
    "   Check header X-Admin-Key matches admin.api-key from application.yml.",
    "   If mismatch: respond 401 {error: Unauthorized}",
    "",
    "5. Create admin/AdminController.java (@RestController, @RequestMapping('/api/admin')):",
    "   GET  /config: return all system_config rows as Map<String,String>",
    "   POST /config: body Map<String,String> -> update each key in system_config",
    "     Call ConfigRefreshService to propagate changes to RiskEngine in-memory values",
    "   POST /trading/pause: set system_config key trading_enabled=false",
    "   POST /trading/resume: set system_config key trading_enabled=true",
    "   POST /exit-all: call positionManager.eodHardClose() immediately",
    "   GET  /dashboard: return {openPositions, todayPnl, tradesCount, signalsCount,",
    "                    capitalDeployed, availableCapital, marketStatus, tradingEnabled}",
    "   GET  /news/recent: return last 30 news_log entries",
    "",
    "6. Create ConfigRefreshService.java (@Service):",
    "   After any system_config update, reload in-memory values in RiskEngine, SignalDetectionEngine.",
    "   This means RiskEngine and SignalDetectionEngine should read their limits from a",
    "   @RefreshScope bean or have a reloadConfig() method called explicitly.",
    "",
    "=== PART C: DAILY SUMMARY REPORT ===",
    "",
    "7. Create reporting/DailySummaryService.java:",
    "   @Scheduled(cron='45 15 * * MON-FRI')  // 3:45 PM",
    "   void generateAndSend():",
    "     List<TradeLog> trades = tradeLogService.getTodayTrades()",
    "     int total = trades.size()",
    "     int wins = trades where net_pnl > 0",
    "     int losses = trades where net_pnl < 0",
    "     double winRate = wins*100.0/total (or 0 if total=0)",
    "     double grossPnl = sum of pnl",
    "     double totalCharges = sum of charges",
    "     double netPnl = sum of net_pnl",
    "     TradeLog best = max net_pnl, worst = min net_pnl",
    "     double avgHoldingMin = avg of (exit_time - entry_time) in minutes",
    "     Map<String,Long> exitReasonBreakdown = group by exit_reason -> count",
    "",
    "     AUTO-ANALYSIS (self-improvement notes):",
    "     List<String> notes = []",
    "     if total > 0 && losses*100.0/total > 50: notes.add('High stop-loss rate (>50%) - consider raising signal threshold or widening stop loss')",
    "     if total < 3: notes.add('Low trade count - consider loosening signal thresholds')",
    "     if netPnl < 0: notes.add('Net loss day - review worst trades for pattern')",
    "     if totalCharges > netPnl*0.3: notes.add('High brokerage ratio - consider reducing trade frequency')",
    "",
    "     Save to daily_summary table. Send Telegram message with full formatted summary.",
    "",
    "   Telegram summary format:",
    "     DAILY REPORT - {date}",
    "     Trades: {total} (W:{wins} L:{losses} Rate:{winRate}%)",
    "     Net P&L: Rs {netPnl} | Charges: Rs {totalCharges}",
    "     Best: {best.symbol} Rs {best.net_pnl}",
    "     Worst: {worst.symbol} Rs {worst.net_pnl}",
    "     Exit Reasons: {breakdown}",
    "     Notes: {notes joined by '; '}",
    "",
    "Write tests:",
    "  NewsSentimentClassifierTest: 10 headlines -> verify each classified correctly",
    "  NewsDaemonTest: mock HTTP, verify NSE JSON parsed + news_log saved + bearish alert sent",
    "  AdminControllerTest (MockMvc):",
    "    No X-Admin-Key header -> 401",
    "    Wrong key -> 401",
    "    Correct key + GET /config -> 200 with map",
    "    POST /config {stop_loss_pct: 1.2} -> DB updated",
    "    POST /trading/pause -> trading_enabled=false in DB",
    "  DailySummaryServiceTest:",
    "    Mock 5 trade_log rows (3 wins, 2 losses). Verify all metrics.",
    "    Verify auto-analysis note appears when loss rate > 50%.",
    "    Verify Telegram message format contains all fields.",
  ]),
  sp(),
 
  verifyBox([
    "NewsSentimentClassifierTest: 10/10 headlines correctly classified",
    "AdminControllerTest: 401 on missing/wrong key, 200 on correct key",
    "POST /api/admin/config updates system_config in DB (verify with SELECT)",
    "POST /api/admin/trading/pause -> trading_enabled=false -> RiskEngine rejects next signal",
    "POST /api/admin/exit-all triggers immediate position exit (integration test)",
    "DailySummaryServiceTest: all 7 metrics correct (wins, losses, charges, best/worst, avg holding)",
    "Auto-analysis note appears in summary when loss rate > 50%",
    "Telegram daily summary received at 3:45 PM with all fields populated",
    "news_log table populated with entries after daemon cycle (manual verify)",
    "V2__news_log.sql Flyway migration runs cleanly on fresh DB",
  ]),
  sp(),
 
  // PHASE 8
  phaseHeader(8, "REACT ADMIN PORTAL + FULL INTEGRATION TEST", "375623"),
  sp(),
  promptBox("Phase 8", [
    "=== PART A: REACT ADMIN PORTAL ===",
    "Create project: algo-trader-ui/ using: npm create vite@latest algo-trader-ui -- --template react",
    "Add: npm install tailwindcss @tailwindcss/vite axios",
    "",
    ".env: VITE_API_BASE=http://localhost:8080  VITE_ADMIN_KEY=your-key-here",
    "",
    "Create src/api/client.js:",
    "  axios instance with baseURL=VITE_API_BASE",
    "  default header: X-Admin-Key: VITE_ADMIN_KEY",
    "",
    "Pages to create (each in src/pages/):",
    "",
    "Dashboard.jsx:",
    "  Auto-refresh every 5s. Calls GET /api/admin/dashboard.",
    "  Cards: Open Positions | Today Net P&L (green/red) | Capital Used % | Signals Today",
    "  Status bar: Trading Status (ACTIVE green / PAUSED red) + Toggle button",
    "  Market Status: NORMAL / MARKET FALLING - TRADING HALTED (red banner)",
    "  Big red button: EXIT ALL POSITIONS (confirm dialog before calling POST /admin/exit-all)",
    "",
    "Positions.jsx:",
    "  Auto-refresh every 3s. Calls GET /api/positions.",
    "  Table: Symbol | Entry Price | Current LTP | Qty | Unrealized P&L | Stop Loss | T1 | T2 | Action",
    "  P&L column: green if positive, red if negative.",
    "  Action: Force Exit button -> POST /api/positions/{symbol}/exit -> confirm first.",
    "",
    "Configuration.jsx:",
    "  Load GET /api/admin/config on mount.",
    "  Display each key as labelled input. Group into 3 sections:",
    "    Capital & Limits | Signal Parameters | Risk Parameters",
    "  Save button: POST /api/admin/config with changed values.",
    "  Show success/error toast.",
    "",
    "TradeHistory.jsx:",
    "  Load GET /api/positions/history (no auto-refresh, button to refresh).",
    "  Table: Symbol | Entry | Exit | Qty | Entry Time | Exit Time | Exit Reason | Net P&L",
    "  Net P&L: green/red. Exit reason shown as badge.",
    "",
    "Signals.jsx:",
    "  Load GET /api/signals/recent, auto-refresh every 10s.",
    "  Table: Time | Symbol | Trigger Price | Price Chg% | Vol Multiplier | Strength | Status",
    "  Strength: STRONG=green, MODERATE=yellow, WEAK=grey badge.",
    "",
    "News.jsx:",
    "  Load GET /api/admin/news/recent, refresh every 60s.",
    "  Table: Time | Symbol | Headline | Sentiment | Confidence | Source",
    "  Row colour: BULLISH=light-green, BEARISH=light-red, NEUTRAL=grey.",
    "",
    "Navbar: links to all 6 pages. Show live clock (IST). Show trading status dot.",
    "",
    "=== PART B: FULL INTEGRATION TEST ===",
    "Create AlgoTraderIntegrationTest.java in src/test/.",
    "Use: H2 in-memory DB (add h2 test dependency), @SpringBootTest, MockMvc.",
    "Mock: KiteConnect (do not make real API calls), RestTemplate for Telegram.",
    "",
    "Test flow (sequential, each step asserts before moving to next):",
    "  Step 1: Verify GET /api/health returns kiteConnected=true (mock KiteConnect)",
    "  Step 2: Verify GET /api/universe returns >= 20 instruments (use test symbol file)",
    "  Step 3: Inject 35 synthetic Tick objects for RELIANCE:",
    "    First 30 ticks: price rises from 2800 to 2820 (0.71% rise) + volume 3x avg",
    "    Last 5 ticks: all consecutively higher. tickTime spaced 1 second apart.",
    "  Step 4: Verify GET /api/signals/recent has 1 signal for RELIANCE with strength=MODERATE or STRONG",
    "  Step 5: Verify RiskEngine approved (check executionQueue has 1 TradeIntent)",
    "  Step 6: Verify mock KiteConnect.placeOrder() was called with:",
    "    tradingsymbol=RELIANCE, exchange=NSE, orderType=MARKET, transactionType=BUY, product=MIS",
    "  Step 7: Verify trade_log has 1 row for RELIANCE with status=OPEN",
    "  Step 8: Inject 5 ticks with price=2848 (>=target1Price of ~2820*1.01=2848.20):",
    "    Verify second placeOrder() call: transactionType=SELL, qty=40% of original",
    "  Step 9: Inject 5 ticks with price=2797.6 (<=stopLoss of 2800*0.992=2777.6):",
    "    Wait — actually inject stop-loss ticks: price=2775",
    "    Verify SELL order for remaining qty placed",
    "  Step 10: Verify trade_log row updated: status=CLOSED, exit_reason=STOP_LOSS, exit_price set",
    "  Step 11: Manually trigger DailySummaryService.generateAndSend()",
    "    Verify daily_summary table has 1 row with total_trades=1, losing_trades=1",
    "",
    "All 11 steps must pass. Test must complete under 15 seconds.",
  ]),
  sp(),
 
  verifyBox([
    "React app runs on localhost:5173 (npm run dev) with no console errors",
    "Dashboard shows real data from running Spring Boot backend (auto-refresh works)",
    "Toggle Trading Status: button calls API and status changes immediately",
    "EXIT ALL POSITIONS: shows confirm dialog, on confirm calls API, positions disappear",
    "Configuration: changing stop_loss_pct and saving persists to DB",
    "Force Exit on Positions page: calls API, position removed from table within 3s refresh",
    "Signals page shows entries with correct strength badges",
    "News page shows BULLISH rows in green, BEARISH in red",
    "Integration test: all 11 steps pass in sequence",
    "Integration test completes under 15 seconds",
    "No H2 DB errors; test DB isolated from main PostgreSQL",
  ]),
  sp(),
 
  // ══════════════════════════
  h1("SECTION 3 — SYSTEM DESIGN CONCEPTS EMBEDDED IN THIS PROJECT"),
 
  P("Study these concepts through the lens of this system — real-world context makes them stick:"),
  sp(),
  threeCol([
    ["Pattern / Concept", "Where It Appears In This System", "Resource"],
    ["Ring Buffer", "TickDataStore: 120-tick deque per stock, O(1) eviction", "DDIA Ch.11"],
    ["Producer-Consumer", "WebSocket thread -> ArrayBlockingQueue -> processor thread", "JCIP Ch.5"],
    ["Back Pressure + Load Shedding", "Queue full = drop tick (protect WebSocket), log WARN", "DDIA Ch.11"],
    ["Circuit Breaker", "Daily loss halt + market fall halt = circuit opens, resets at EOD", "Release It! Ch.5"],
    ["Event-Driven Architecture", "Tick -> Signal -> Risk -> Execution: each stage decoupled by queue", "Alex Xu Vol 2 Ch.3"],
    ["Exponential Back-off + Jitter", "WebSocket reconnect: 1s, 2s, 4s, 8s, 16s, 30s max", "Alex Xu Vol 1 Ch.4"],
    ["Token Bucket Rate Limiter", "Max 10 Kite orders/sec via Guava RateLimiter", "Alex Xu Vol 1 Ch.4"],
    ["Idempotency", "Signal cooldown prevents duplicate orders for same stock", "DDIA Ch.12"],
    ["Async Logging", "SLF4J async appender: log writes never block trading thread", "Java Performance"],
    ["Sliding Window", "getTicksInWindow(): always looking at last N seconds of ticks", "Alex Xu Vol 1 Ch.4"],
    ["Dead Letter Queue (concept)", "Failed BUY after 2 retries: signal abandoned (not re-queued)", "Enterprise Integration Patterns"],
    ["Saga Pattern (concept)", "Trade lifecycle: entry -> partial exit -> full exit as coordinated steps", "DDIA Ch.12"],
  ], 2400, 3600),
  sp(),
 
  // ══════════════════════════
  h1("SECTION 4 — OPERATIONAL RUNBOOK"),
 
  h2("Daily Pre-Market Checklist (8:45 – 9:10 AM IST)"),
  num("Login to Zerodha Kite web, get fresh request_token from URL after login"),
  num("POST http://localhost:8080/api/kite/auth?requestToken=XXXX (Spring Boot sets session)"),
  num("Verify: GET /api/health -> kiteConnected: true"),
  num("Open Admin Portal (localhost:5173) -> Configuration tab"),
  num("Review and adjust: max_daily_capital, max_open_positions, stop_loss_pct, signal thresholds"),
  num("Check: trading_enabled = true"),
  num("Review yesterday's daily_summary from Telegram or Trade History tab"),
  num("At 9:15 AM: system auto-starts. No manual trigger needed."),
  sp(),
 
  h2("During Market Hours"),
  bul("Monitor Telegram for: [SIGNAL], [BUY ORDER], [EXIT], [CRITICAL], [MARKET FALL] messages"),
  bul("Dashboard auto-refreshes — watch P&L and open positions count"),
  bul("If behaviour seems wrong: POST /api/admin/trading/pause -> investigate logs -> resume"),
  bul("Force-exit any position from Positions page at any time"),
  sp(),
 
  h2("Post-Market (3:45 PM+)"),
  bul("Receive Telegram daily summary with all metrics and auto-analysis notes"),
  bul("Open Trade History: review each exit_reason — high STOP_LOSS rate = signal too sensitive"),
  bul("Open Signals page: if signal count < 5 all day = thresholds too strict"),
  bul("Adjust Configuration for next day. Commit to one change at a time for clean A/B testing."),
  sp(),
 
  h2("Key Config Thresholds and Their Effect"),
  threeCol([
    ["Config Key", "Increase Effect", "Decrease Effect"],
    ["signal_price_change_pct (default 0.5)", "Fewer signals, only strong moves", "More signals, more noise"],
    ["signal_volume_multiplier (default 2.0)", "Fewer signals, very high-volume only", "More signals, lower confidence"],
    ["signal_confirmation_ticks (default 3)", "Fewer false positives, might miss fast spikes", "More entries, more noise"],
    ["stop_loss_pct (default 0.8)", "Wider stop = larger loss per trade", "Tighter stop = more stop-outs"],
    ["target1_pct (default 1.0)", "Less frequent partial exits", "Earlier partial exits"],
    ["max_daily_capital", "More total exposure", "Less total exposure"],
    ["max_open_positions", "More concurrent trades", "Fewer concurrent trades"],
  ], 2600, 3200),
  sp(),
 
  // ══════════════════════════
  h1("SECTION 5 — FUTURE PHASES"),
 
  bulMixed("Phase 9 — Short Selling", "When MarketHealthMonitor detects Nifty falling, detect downward momentum signals (all MomentumIndicator conditions inverted) and place SELL MIS orders. Hard exit all shorts by 3:15 PM. Requires same risk gates."),
  bulMixed("Phase 10 — F&O Options", "Buy CALL options on bullish momentum signals and PUT options on bearish signals. Much lower capital per trade but high leverage. Requires F&O account activation on Zerodha."),
  bulMixed("Phase 11 — ML Signal Model", "Replace rule-based MomentumIndicator with an XGBoost or LSTM model trained on historical tick data. Feed win/loss labels from trade_log as supervised training. Retrain weekly using last month's data."),
  bulMixed("Phase 12 — Multi-Account Support", "Support multiple Kite API accounts. Distribute signals across accounts (one buys RELIANCE on account A, another on account B). Useful for family members' accounts."),
  bulMixed("Phase 13 — Backtesting Engine", "Replay historical NSE tick data through the SignalDetectionEngine and PositionManager to simulate P&L before risking real capital. Required before adjusting thresholds."),
  sp(),
 
  new Paragraph({
    spacing: { before: 400, after: 100 }, alignment: AlignmentType.CENTER,
    border: { top: { style: BorderStyle.SINGLE, size: 6, color: MED_BLUE } },
    children: [fh("Build one phase at a time. Verify all checkboxes. Then proceed. Never skip a phase.", 20, GRAY, false)]
  }),
];
 
const doc = new Document({
  numbering: {
    config: [
      { reference: "bul", levels: [
        { level:0, format:LevelFormat.BULLET, text:"•", alignment:AlignmentType.LEFT,
          style:{paragraph:{indent:{left:720,hanging:360}}} },
        { level:1, format:LevelFormat.BULLET, text:"–", alignment:AlignmentType.LEFT,
          style:{paragraph:{indent:{left:1080,hanging:360}}} },
      ]},
      { reference: "num", levels: [
        { level:0, format:LevelFormat.DECIMAL, text:"%1.", alignment:AlignmentType.LEFT,
          style:{paragraph:{indent:{left:720,hanging:360}}} },
      ]},
    ]
  },
  sections: [{
    properties: {
      page: {
        size: { width: 12240, height: 15840 },
        margin: { top: 1080, right: 1080, bottom: 1080, left: 1080 }
      }
    },
    headers: {
      default: new Header({ children: [new Paragraph({
        border: { bottom: { style: BorderStyle.SINGLE, size: 4, color: MED_BLUE } },
        spacing: { after: 60 },
        children: [
          fh("INTELLIGENT SCALPING ALGO TRADING SYSTEM", 18, BLUE, true),
          new TextRun({ text: "   |   Phased Implementation Design   |   For AI Agent Use", font: "Calibri", size:18, color:GRAY })
        ]
      })] })
    },
    footers: {
      default: new Footer({ children: [new Paragraph({
        border: { top: { style: BorderStyle.SINGLE, size: 4, color: MED_BLUE } },
        spacing: { before: 60 },
        tabStops: [{ type: TabStopType.RIGHT, position: 9360 }],
        children: [
          fh("NSE/BSE Intraday  |  Java + Spring Boot + Kite Connect", 18, GRAY, false),
          new TextRun({ text: "\tPage ", font: "Calibri", size: 18, color: GRAY }),
          new PageNumber()
        ]
      })] })
    },
    children
  }]
});
 
Packer.toBuffer(doc).then(buf => {
  fs.writeFileSync("/mnt/user-data/outputs/AlgoTrader_Phase_Design.docx", buf);
  console.log("Done");
});
 
You’ve hit your limit for Claude messages. Limits will reset at 6:50 PM. For higher limits, explore our Pro plan.

