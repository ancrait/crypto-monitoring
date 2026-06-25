CREATE TABLE IF NOT EXISTS crypto_prices (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(10) NOT NULL,
    price_usd DECIMAL(20, 8) NOT NULL,
    source VARCHAR(20) NOT NULL DEFAULT 'BINANCE',
    timestamp TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_crypto_prices_symbol_timestamp
    ON crypto_prices (symbol, timestamp DESC);
