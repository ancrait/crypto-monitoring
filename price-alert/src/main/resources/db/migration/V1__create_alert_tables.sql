CREATE TABLE IF NOT EXISTS user_alerts (
    id BIGSERIAL PRIMARY KEY,
    chat_id BIGINT NOT NULL,
    symbol VARCHAR(10) NOT NULL,
    target_price DECIMAL(20, 8) NOT NULL,
    direction VARCHAR(5) NOT NULL CHECK (direction IN ('ABOVE', 'BELOW')),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    triggered BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_user_alerts_chat_id ON user_alerts (chat_id);
CREATE INDEX IF NOT EXISTS idx_user_alerts_symbol ON user_alerts (symbol);
CREATE INDEX IF NOT EXISTS idx_user_alerts_enabled ON user_alerts (enabled);


