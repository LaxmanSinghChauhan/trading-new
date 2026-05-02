CREATE TABLE IF NOT EXISTS news_log (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(50),
    headline VARCHAR(2000) NOT NULL,
    source VARCHAR(50) NOT NULL,
    sentiment VARCHAR(20) NOT NULL,
    confidence NUMERIC(5, 4) NOT NULL DEFAULT 0,
    published_at TIMESTAMP,
    processed_at TIMESTAMP NOT NULL
);
