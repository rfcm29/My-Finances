-- Create budgets table
CREATE TABLE budgets (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT NOT NULL REFERENCES categories(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    amount DECIMAL(15,2) NOT NULL CHECK (amount > 0),
    period VARCHAR(20) NOT NULL CHECK (period IN ('WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY', 'CUSTOM')),
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_budget_dates CHECK (end_date >= start_date)
);

-- Create indexes
CREATE INDEX idx_budgets_user_id ON budgets(user_id);
CREATE INDEX idx_budgets_category_id ON budgets(category_id);
CREATE INDEX idx_budgets_period ON budgets(period);
CREATE INDEX idx_budgets_dates ON budgets(start_date, end_date);
CREATE INDEX idx_budgets_active ON budgets(active);

-- Create composite indexes
CREATE INDEX idx_budgets_user_active ON budgets(user_id, active);
CREATE INDEX idx_budgets_user_period ON budgets(user_id, period);

-- Create unique constraint to prevent overlapping budgets for same category
CREATE UNIQUE INDEX idx_budgets_unique_active ON budgets(user_id, category_id, start_date, end_date) 
WHERE active = true;