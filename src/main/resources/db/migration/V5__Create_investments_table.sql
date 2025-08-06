-- Create investments table
CREATE TABLE investments (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    symbol VARCHAR(20),
    name VARCHAR(200) NOT NULL,
    type VARCHAR(30) NOT NULL CHECK (type IN ('STOCK', 'ETF', 'MUTUAL_FUND', 'BOND', 'SAVINGS_ACCOUNT', 'TERM_DEPOSIT', 'CRYPTOCURRENCY', 'REAL_ESTATE', 'OTHER')),
    quantity DECIMAL(15,6) NOT NULL CHECK (quantity > 0),
    purchase_price DECIMAL(15,2) NOT NULL CHECK (purchase_price > 0),
    current_price DECIMAL(15,2),
    purchase_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_investments_user_id ON investments(user_id);
CREATE INDEX idx_investments_symbol ON investments(symbol);
CREATE INDEX idx_investments_type ON investments(type);
CREATE INDEX idx_investments_purchase_date ON investments(purchase_date);

-- Create composite indexes
CREATE INDEX idx_investments_user_type ON investments(user_id, type);
CREATE INDEX idx_investments_user_symbol ON investments(user_id, symbol);