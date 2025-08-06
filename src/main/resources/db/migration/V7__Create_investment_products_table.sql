-- Create investment_products table
CREATE TABLE investment_products (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    symbol VARCHAR(20) NOT NULL,
    name VARCHAR(200) NOT NULL,
    description VARCHAR(500),
    type VARCHAR(30) NOT NULL CHECK (type IN ('STOCK', 'ETF', 'MUTUAL_FUND', 'BOND', 'SAVINGS_ACCOUNT', 'TERM_DEPOSIT', 'CRYPTOCURRENCY', 'COMMODITY', 'REAL_ESTATE', 'INDEX', 'OTHER')),
    currency VARCHAR(3) NOT NULL,
    current_price DECIMAL(15,6),
    exchange VARCHAR(100),
    sector VARCHAR(50),
    region VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create unique constraint for symbol + currency per user
ALTER TABLE investment_products ADD CONSTRAINT uk_products_user_symbol_currency 
    UNIQUE (user_id, symbol, currency);

-- Create indexes for investment_products
CREATE INDEX idx_products_user_id ON investment_products(user_id);
CREATE INDEX idx_products_symbol ON investment_products(symbol);
CREATE INDEX idx_products_type ON investment_products(type);
CREATE INDEX idx_products_currency ON investment_products(currency);
CREATE INDEX idx_products_active ON investment_products(is_active);
CREATE INDEX idx_products_user_active ON investment_products(user_id, is_active);
CREATE INDEX idx_products_user_type ON investment_products(user_id, type);
CREATE INDEX idx_products_user_currency ON investment_products(user_id, currency);

-- Add exchange, sector, region indexes for filtering
CREATE INDEX idx_products_exchange ON investment_products(exchange);
CREATE INDEX idx_products_sector ON investment_products(sector);
CREATE INDEX idx_products_region ON investment_products(region);