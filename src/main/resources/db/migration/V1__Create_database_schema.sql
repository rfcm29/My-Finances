-- MyFinances Database Schema V1
-- Complete database schema with all core tables

-- Create users table
CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    account_non_expired BOOLEAN NOT NULL DEFAULT true,
    account_non_locked BOOLEAN NOT NULL DEFAULT true,
    credentials_non_expired BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create persistent_logins table for remember-me functionality
CREATE TABLE persistent_logins (
    username VARCHAR(64) NOT NULL,
    series VARCHAR(64) PRIMARY KEY,
    token VARCHAR(64) NOT NULL,
    last_used TIMESTAMP NOT NULL
);

-- Create accounts table
CREATE TABLE accounts (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('CHECKING', 'SAVINGS', 'CREDIT_CARD', 'INVESTMENT', 'CASH', 'OTHER')),
    balance DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create transaction categories table
CREATE TABLE t_cat_transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    color VARCHAR(7),
    icon VARCHAR(50),
    type VARCHAR(20) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE')) DEFAULT 'EXPENSE',
    active BOOLEAN NOT NULL DEFAULT true,
    parent_id BIGINT REFERENCES t_cat_transactions(id) ON DELETE SET NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create transactions table
CREATE TABLE transactions (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    account_id BIGINT NOT NULL REFERENCES accounts(id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES t_cat_transactions(id) ON DELETE SET NULL,
    amount DECIMAL(15,2) NOT NULL,
    description VARCHAR(255) NOT NULL,
    notes TEXT,
    transaction_date DATE NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('INCOME', 'EXPENSE', 'TRANSFER')),
    is_recurring BOOLEAN NOT NULL DEFAULT false,
    recurring_frequency VARCHAR(20),
    receipt_url VARCHAR(500),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create budgets table
CREATE TABLE budgets (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    category_id BIGINT REFERENCES t_cat_transactions(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    amount DECIMAL(15,2) NOT NULL,
    period VARCHAR(20) NOT NULL CHECK (period IN ('WEEKLY', 'MONTHLY', 'QUARTERLY', 'YEARLY')),
    start_date DATE NOT NULL,
    end_date DATE,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create investment_products table
CREATE TABLE investment_products (
    id BIGSERIAL PRIMARY KEY,
    symbol VARCHAR(20) NOT NULL UNIQUE,
    name VARCHAR(200) NOT NULL,
    type VARCHAR(50) NOT NULL CHECK (type IN ('STOCK', 'ETF', 'FUND', 'BOND', 'CRYPTO', 'OTHER')),
    currency VARCHAR(3) NOT NULL DEFAULT 'EUR',
    exchange VARCHAR(50),
    sector VARCHAR(100),
    current_price DECIMAL(15,4),
    last_updated TIMESTAMP,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create user_investment_products table (user's watchlist)
CREATE TABLE user_investment_products (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    investment_product_id BIGINT NOT NULL REFERENCES investment_products(id) ON DELETE CASCADE,
    added_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(user_id, investment_product_id)
);

-- Create investments table (actual holdings)
CREATE TABLE investments (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    investment_product_id BIGINT NOT NULL REFERENCES investment_products(id) ON DELETE CASCADE,
    quantity DECIMAL(15,4) NOT NULL DEFAULT 0.00,
    average_price DECIMAL(15,4) NOT NULL DEFAULT 0.00,
    total_invested DECIMAL(15,2) NOT NULL DEFAULT 0.00,
    current_value DECIMAL(15,2),
    profit_loss DECIMAL(15,2),
    profit_loss_percentage DECIMAL(5,2),
    first_purchase_date DATE,
    last_purchase_date DATE,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create account categories table
CREATE TABLE t_cat_accounts (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    code VARCHAR(20) NOT NULL UNIQUE,
    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create account subcategories table
CREATE TABLE t_cat_account_subs (
    id BIGSERIAL PRIMARY KEY,
    category_id BIGINT NOT NULL REFERENCES t_cat_accounts(id) ON DELETE CASCADE,
    name VARCHAR(100) NOT NULL,
    description TEXT,
    code VARCHAR(30) NOT NULL UNIQUE,
    display_order INTEGER NOT NULL DEFAULT 0,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Add category foreign keys to accounts table
ALTER TABLE accounts 
ADD COLUMN category_id BIGINT REFERENCES t_cat_accounts(id),
ADD COLUMN subcategory_id BIGINT REFERENCES t_cat_account_subs(id);

-- Create indexes for performance
CREATE INDEX idx_users_email ON users(email);
CREATE INDEX idx_accounts_user_id ON accounts(user_id);
CREATE INDEX idx_accounts_type ON accounts(type);
CREATE INDEX idx_accounts_active ON accounts(active);
CREATE INDEX idx_accounts_user_active ON accounts(user_id, active);
CREATE INDEX idx_accounts_category_id ON accounts(category_id);
CREATE INDEX idx_accounts_subcategory_id ON accounts(subcategory_id);

CREATE INDEX idx_transaction_categories_user_id ON t_cat_transactions(user_id);
CREATE INDEX idx_transaction_categories_user_type ON t_cat_transactions(user_id, type);
CREATE INDEX idx_transaction_categories_parent_id ON t_cat_transactions(parent_id);

CREATE INDEX idx_transactions_user_id ON transactions(user_id);
CREATE INDEX idx_transactions_account_id ON transactions(account_id);
CREATE INDEX idx_transactions_category_id ON transactions(category_id);
CREATE INDEX idx_transactions_date ON transactions(transaction_date);
CREATE INDEX idx_transactions_type ON transactions(type);

CREATE INDEX idx_budgets_user_id ON budgets(user_id);
CREATE INDEX idx_budgets_category_id ON budgets(category_id);
CREATE INDEX idx_budgets_user_active ON budgets(user_id, active);
CREATE INDEX idx_budgets_period ON budgets(period);

CREATE INDEX idx_investment_products_symbol ON investment_products(symbol);
CREATE INDEX idx_investment_products_type ON investment_products(type);
CREATE INDEX idx_investment_products_active ON investment_products(active);

CREATE INDEX idx_user_investment_products_user_id ON user_investment_products(user_id);
CREATE INDEX idx_user_investment_products_product_id ON user_investment_products(investment_product_id);

CREATE INDEX idx_investments_user_id ON investments(user_id);
CREATE INDEX idx_investments_product_id ON investments(investment_product_id);
CREATE INDEX idx_investments_active ON investments(active);

CREATE INDEX idx_account_categories_code ON t_cat_accounts(code);
CREATE INDEX idx_account_subcategories_code ON t_cat_account_subs(code);
CREATE INDEX idx_account_subcategories_category_id ON t_cat_account_subs(category_id);

-- Insert default account categories
INSERT INTO t_cat_accounts (name, description, code, display_order) VALUES
('Contas Bancárias', 'Contas bancárias tradicionais', 'BANK', 1),
('Poupanças do Estado', 'Produtos de poupança do estado português', 'STATE_SAVINGS', 2),
('Crédito', 'Contas de crédito e financiamento', 'CREDIT', 3),
('Carteiras Digitais', 'Carteiras e bancos digitais', 'DIGITAL', 4),
('Dinheiro', 'Dinheiro físico e equivalentes', 'CASH', 5),
('Investimentos', 'Contas de investimento e corretagem', 'INVESTMENT', 6);

-- Insert default account subcategories
INSERT INTO t_cat_account_subs (category_id, name, description, code, display_order) VALUES
-- Bank subcategories
((SELECT id FROM t_cat_accounts WHERE code = 'BANK'), 'Conta à Ordem', 'Conta corrente bancária', 'BANK_CHECKING', 1),
((SELECT id FROM t_cat_accounts WHERE code = 'BANK'), 'Conta Poupança', 'Conta poupança bancária', 'BANK_SAVINGS', 2),
((SELECT id FROM t_cat_accounts WHERE code = 'BANK'), 'Conta Ordenado', 'Conta destinada ao recebimento de salário', 'BANK_SALARY', 3),
((SELECT id FROM t_cat_accounts WHERE code = 'BANK'), 'Conta Jovem', 'Conta bancária para jovens', 'BANK_YOUTH', 4),

-- State savings subcategories
((SELECT id FROM t_cat_accounts WHERE code = 'STATE_SAVINGS'), 'Certificados de Aforro', 'Certificados de Aforro do IGCP', 'STATE_CA', 1),
((SELECT id FROM t_cat_accounts WHERE code = 'STATE_SAVINGS'), 'Certificados do Tesouro', 'Certificados do Tesouro do IGCP', 'STATE_CT', 2),
((SELECT id FROM t_cat_accounts WHERE code = 'STATE_SAVINGS'), 'Certificados do Tesouro Poupança Mais', 'CTPM do IGCP', 'STATE_CTPM', 3),
((SELECT id FROM t_cat_accounts WHERE code = 'STATE_SAVINGS'), 'Outros Produtos IGCP', 'Outros produtos do Instituto de Gestão do Crédito Público', 'STATE_OTHER', 4),

-- Credit subcategories
((SELECT id FROM t_cat_accounts WHERE code = 'CREDIT'), 'Cartão de Crédito', 'Cartão de crédito bancário', 'CREDIT_CARD', 1),
((SELECT id FROM t_cat_accounts WHERE code = 'CREDIT'), 'Linha de Crédito', 'Linha de crédito pessoal', 'CREDIT_LINE', 2),
((SELECT id FROM t_cat_accounts WHERE code = 'CREDIT'), 'Descoberto Autorizado', 'Descoberto bancário autorizado', 'CREDIT_OVERDRAFT', 3),
((SELECT id FROM t_cat_accounts WHERE code = 'CREDIT'), 'Crédito Pessoal', 'Empréstimo pessoal', 'CREDIT_PERSONAL', 4),

-- Digital wallets subcategories
((SELECT id FROM t_cat_accounts WHERE code = 'DIGITAL'), 'MB Way', 'Carteira digital MB Way', 'DIGITAL_MBWAY', 1),
((SELECT id FROM t_cat_accounts WHERE code = 'DIGITAL'), 'PayPal', 'Carteira digital PayPal', 'DIGITAL_PAYPAL', 2),
((SELECT id FROM t_cat_accounts WHERE code = 'DIGITAL'), 'Revolut', 'Conta Revolut', 'DIGITAL_REVOLUT', 3),
((SELECT id FROM t_cat_accounts WHERE code = 'DIGITAL'), 'Outras Carteiras', 'Outras carteiras digitais', 'DIGITAL_OTHER', 4),

-- Cash subcategories
((SELECT id FROM t_cat_accounts WHERE code = 'CASH'), 'Carteira', 'Dinheiro na carteira', 'CASH_WALLET', 1),
((SELECT id FROM t_cat_accounts WHERE code = 'CASH'), 'Dinheiro em Casa', 'Dinheiro guardado em casa', 'CASH_HOME', 2),
((SELECT id FROM t_cat_accounts WHERE code = 'CASH'), 'Mealheiro', 'Poupanças em moedas/notas', 'CASH_PIGGYBANK', 3),
((SELECT id FROM t_cat_accounts WHERE code = 'CASH'), 'Outro Dinheiro', 'Outros tipos de dinheiro físico', 'CASH_OTHER', 4),

-- Investment subcategories
((SELECT id FROM t_cat_accounts WHERE code = 'INVESTMENT'), 'Corretora', 'Conta de corretagem para investimentos', 'INV_BROKER', 1),
((SELECT id FROM t_cat_accounts WHERE code = 'INVESTMENT'), 'Fundos de Investimento', 'Fundos de investimento bancários', 'INV_FUND', 2),
((SELECT id FROM t_cat_accounts WHERE code = 'INVESTMENT'), 'PPR/Reforma', 'Planos Poupança Reforma', 'INV_PENSION', 3),
((SELECT id FROM t_cat_accounts WHERE code = 'INVESTMENT'), 'Criptomoedas', 'Carteiras de criptomoedas', 'INV_CRYPTO', 4);