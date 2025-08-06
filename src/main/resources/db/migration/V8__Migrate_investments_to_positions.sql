-- Rename investments table to investment_positions and update structure
RENAME TABLE investments TO investment_positions_backup;

-- Create new investment_positions table
CREATE TABLE investment_positions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    product_id BIGINT NOT NULL REFERENCES investment_products(id) ON DELETE CASCADE,
    quantity DECIMAL(15,6) NOT NULL CHECK (quantity > 0),
    purchase_price DECIMAL(15,6) NOT NULL CHECK (purchase_price > 0),
    purchase_date DATE NOT NULL,
    currency VARCHAR(3) NOT NULL,
    exchange_rate DECIMAL(15,6) DEFAULT 1.0,
    notes VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for investment_positions
CREATE INDEX idx_positions_user_id ON investment_positions(user_id);
CREATE INDEX idx_positions_product_id ON investment_positions(product_id);
CREATE INDEX idx_positions_purchase_date ON investment_positions(purchase_date);
CREATE INDEX idx_positions_currency ON investment_positions(currency);
CREATE INDEX idx_positions_user_product ON investment_positions(user_id, product_id);
CREATE INDEX idx_positions_user_currency ON investment_positions(user_id, currency);

-- Note: Data migration would need to be handled separately if there's existing data
-- For now, we'll drop the backup table since this is likely a fresh setup
DROP TABLE investment_positions_backup;