CREATE TABLE IF NOT EXISTS trade_log (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(50) NOT NULL,
    instrument_token BIGINT NOT NULL,
    broker_mode VARCHAR(20) NOT NULL,
    signal_strength VARCHAR(20) NOT NULL,
    entry_price_vwap NUMERIC(19, 4) NOT NULL,
    exit_price_vwap NUMERIC(19, 4),
    total_quantity INTEGER NOT NULL,
    remaining_quantity INTEGER NOT NULL,
    entry_time TIMESTAMP NOT NULL,
    exit_time TIMESTAMP,
    final_exit_reason VARCHAR(50),
    gross_pnl NUMERIC(19, 4) NOT NULL DEFAULT 0,
    charges NUMERIC(19, 4) NOT NULL DEFAULT 0,
    net_pnl NUMERIC(19, 4) NOT NULL DEFAULT 0,
    status VARCHAR(20) NOT NULL
);

CREATE TABLE IF NOT EXISTS trade_leg_log (
    id BIGSERIAL PRIMARY KEY,
    trade_id BIGINT NOT NULL REFERENCES trade_log(id),
    leg_type VARCHAR(20) NOT NULL,
    reason VARCHAR(50) NOT NULL,
    order_id VARCHAR(100) NOT NULL,
    quantity INTEGER NOT NULL,
    price NUMERIC(19, 4) NOT NULL,
    charges NUMERIC(19, 4) NOT NULL DEFAULT 0,
    broker_mode VARCHAR(20) NOT NULL,
    executed_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS daily_summary (
    id BIGSERIAL PRIMARY KEY,
    trade_date DATE NOT NULL UNIQUE,
    total_trades INTEGER NOT NULL DEFAULT 0,
    winning_trades INTEGER NOT NULL DEFAULT 0,
    losing_trades INTEGER NOT NULL DEFAULT 0,
    gross_pnl NUMERIC(19, 4) NOT NULL DEFAULT 0,
    total_charges NUMERIC(19, 4) NOT NULL DEFAULT 0,
    net_pnl NUMERIC(19, 4) NOT NULL DEFAULT 0,
    capital_used NUMERIC(19, 4) NOT NULL DEFAULT 0,
    notes TEXT
);

CREATE TABLE IF NOT EXISTS system_config (
    config_key VARCHAR(100) PRIMARY KEY,
    config_value VARCHAR(200) NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

INSERT INTO system_config (config_key, config_value, updated_at)
VALUES
    ('strategy_direction', 'LONG_ONLY', CURRENT_TIMESTAMP),
    ('trading_enabled', 'true', CURRENT_TIMESTAMP),
    ('max_daily_capital', '100000', CURRENT_TIMESTAMP),
    ('max_open_positions', '5', CURRENT_TIMESTAMP),
    ('max_capital_per_trade_pct', '10.0', CURRENT_TIMESTAMP),
    ('max_daily_loss_pct', '3.0', CURRENT_TIMESTAMP),
    ('stop_loss_pct', '0.8', CURRENT_TIMESTAMP),
    ('target1_pct', '1.0', CURRENT_TIMESTAMP),
    ('target2_pct', '2.0', CURRENT_TIMESTAMP),
    ('trailing_stop_activation_pct', '1.5', CURRENT_TIMESTAMP),
    ('trailing_stop_distance_pct', '0.5', CURRENT_TIMESTAMP),
    ('signal_price_change_pct', '0.5', CURRENT_TIMESTAMP),
    ('signal_time_window_sec', '30', CURRENT_TIMESTAMP),
    ('signal_volume_multiplier', '2.0', CURRENT_TIMESTAMP),
    ('signal_confirmation_ticks', '3', CURRENT_TIMESTAMP),
    ('signal_min_ticks', '10', CURRENT_TIMESTAMP),
    ('signal_cooldown_sec', '60', CURRENT_TIMESTAMP),
    ('market_fall_threshold_pct', '1.5', CURRENT_TIMESTAMP);
