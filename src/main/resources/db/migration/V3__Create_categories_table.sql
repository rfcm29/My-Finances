-- Create categories table
CREATE TABLE categories (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')),
    color VARCHAR(7) NOT NULL DEFAULT '#6B7280',
    icon VARCHAR(50) NOT NULL DEFAULT 'fas fa-circle',
    parent_id BIGINT REFERENCES categories(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes
CREATE INDEX idx_categories_user_id ON categories(user_id);
CREATE INDEX idx_categories_type ON categories(type);
CREATE INDEX idx_categories_parent_id ON categories(parent_id);

-- Create composite indexes
CREATE INDEX idx_categories_user_type ON categories(user_id, type);
CREATE INDEX idx_categories_user_parent ON categories(user_id, parent_id);