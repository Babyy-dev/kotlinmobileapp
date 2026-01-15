-- Demo accounts and balances for Kappa roles.
-- Password for all demo accounts: password123

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
  now_ms bigint := (extract(epoch from now()) * 1000)::bigint;
  agency_alpha uuid;
  agency_beta uuid;
BEGIN
  SELECT id INTO agency_alpha FROM agencies WHERE name = 'Kappa Agency Alpha' LIMIT 1;
  IF agency_alpha IS NULL THEN
    agency_alpha := gen_random_uuid();
    INSERT INTO agencies (id, name, owner_user_id, commission_value_usd, commission_block_diamonds, status, created_at)
    VALUES (agency_alpha, 'Kappa Agency Alpha', gen_random_uuid(), 2.20, 620000, 'active', now_ms);
  END IF;

  SELECT id INTO agency_beta FROM agencies WHERE name = 'Kappa Agency Beta' LIMIT 1;
  IF agency_beta IS NULL THEN
    agency_beta := gen_random_uuid();
    INSERT INTO agencies (id, name, owner_user_id, commission_value_usd, commission_block_diamonds, status, created_at)
    VALUES (agency_beta, 'Kappa Agency Beta', gen_random_uuid(), 2.20, 620000, 'active', now_ms);
  END IF;

  INSERT INTO users (id, username, email, password_hash, role, agency_id, status, created_at)
  VALUES (gen_random_uuid(), 'master', 'master@kappa.app', crypt('password123', gen_salt('bf')), 'MASTER', NULL, 'active', now_ms)
  ON CONFLICT (username) DO NOTHING;

  INSERT INTO users (id, username, email, password_hash, role, agency_id, status, created_at)
  VALUES (gen_random_uuid(), 'reseller', 'reseller@kappa.app', crypt('password123', gen_salt('bf')), 'RESELLER', agency_alpha, 'active', now_ms)
  ON CONFLICT (username) DO NOTHING;

  INSERT INTO users (id, username, email, password_hash, role, agency_id, status, created_at)
  VALUES (gen_random_uuid(), 'reseller2', 'reseller2@kappa.app', crypt('password123', gen_salt('bf')), 'RESELLER', agency_beta, 'active', now_ms)
  ON CONFLICT (username) DO NOTHING;

  INSERT INTO users (id, username, email, password_hash, role, agency_id, status, created_at)
  VALUES (gen_random_uuid(), 'agency', 'agency@kappa.app', crypt('password123', gen_salt('bf')), 'AGENCY_OWNER', agency_alpha, 'active', now_ms)
  ON CONFLICT (username) DO NOTHING;

  INSERT INTO users (id, username, email, password_hash, role, agency_id, status, created_at)
  VALUES (gen_random_uuid(), 'agency_beta', 'agency_beta@kappa.app', crypt('password123', gen_salt('bf')), 'AGENCY_OWNER', agency_beta, 'active', now_ms)
  ON CONFLICT (username) DO NOTHING;

  INSERT INTO users (id, username, email, password_hash, role, agency_id, status, created_at)
  VALUES (gen_random_uuid(), 'user', 'user@kappa.app', crypt('password123', gen_salt('bf')), 'USER', agency_alpha, 'active', now_ms)
  ON CONFLICT (username) DO NOTHING;

  INSERT INTO users (id, username, email, password_hash, role, agency_id, status, created_at)
  VALUES (gen_random_uuid(), 'user_alpha', 'user_alpha@kappa.app', crypt('password123', gen_salt('bf')), 'USER', agency_alpha, 'active', now_ms)
  ON CONFLICT (username) DO NOTHING;

  INSERT INTO users (id, username, email, password_hash, role, agency_id, status, created_at)
  VALUES (gen_random_uuid(), 'user_beta', 'user_beta@kappa.app', crypt('password123', gen_salt('bf')), 'USER', agency_beta, 'active', now_ms)
  ON CONFLICT (username) DO NOTHING;

  INSERT INTO users (id, username, email, password_hash, role, agency_id, status, created_at)
  VALUES (gen_random_uuid(), 'user_gamma', 'user_gamma@kappa.app', crypt('password123', gen_salt('bf')), 'USER', agency_beta, 'active', now_ms)
  ON CONFLICT (username) DO NOTHING;

  INSERT INTO wallet_coins (user_id, balance_coins, updated_at)
  SELECT id, 1000000, now_ms FROM users WHERE username = 'master'
  ON CONFLICT (user_id) DO UPDATE SET balance_coins = EXCLUDED.balance_coins, updated_at = EXCLUDED.updated_at;

  INSERT INTO wallet_coins (user_id, balance_coins, updated_at)
  SELECT id, 400000, now_ms FROM users WHERE username = 'reseller'
  ON CONFLICT (user_id) DO UPDATE SET balance_coins = EXCLUDED.balance_coins, updated_at = EXCLUDED.updated_at;

  INSERT INTO wallet_coins (user_id, balance_coins, updated_at)
  SELECT id, 300000, now_ms FROM users WHERE username = 'reseller2'
  ON CONFLICT (user_id) DO UPDATE SET balance_coins = EXCLUDED.balance_coins, updated_at = EXCLUDED.updated_at;

  INSERT INTO wallet_coins (user_id, balance_coins, updated_at)
  SELECT id, 200000, now_ms FROM users WHERE username = 'agency'
  ON CONFLICT (user_id) DO UPDATE SET balance_coins = EXCLUDED.balance_coins, updated_at = EXCLUDED.updated_at;

  INSERT INTO wallet_coins (user_id, balance_coins, updated_at)
  SELECT id, 180000, now_ms FROM users WHERE username = 'agency_beta'
  ON CONFLICT (user_id) DO UPDATE SET balance_coins = EXCLUDED.balance_coins, updated_at = EXCLUDED.updated_at;

  INSERT INTO wallet_coins (user_id, balance_coins, updated_at)
  SELECT id, 2500, now_ms FROM users WHERE username = 'user'
  ON CONFLICT (user_id) DO UPDATE SET balance_coins = EXCLUDED.balance_coins, updated_at = EXCLUDED.updated_at;

  INSERT INTO wallet_coins (user_id, balance_coins, updated_at)
  SELECT id, 8000, now_ms FROM users WHERE username = 'user_alpha'
  ON CONFLICT (user_id) DO UPDATE SET balance_coins = EXCLUDED.balance_coins, updated_at = EXCLUDED.updated_at;

  INSERT INTO wallet_coins (user_id, balance_coins, updated_at)
  SELECT id, 12000, now_ms FROM users WHERE username = 'user_beta'
  ON CONFLICT (user_id) DO UPDATE SET balance_coins = EXCLUDED.balance_coins, updated_at = EXCLUDED.updated_at;

  INSERT INTO wallet_coins (user_id, balance_coins, updated_at)
  SELECT id, 6000, now_ms FROM users WHERE username = 'user_gamma'
  ON CONFLICT (user_id) DO UPDATE SET balance_coins = EXCLUDED.balance_coins, updated_at = EXCLUDED.updated_at;

  IF EXISTS (SELECT 1 FROM users WHERE username = 'agency') THEN
    UPDATE agencies
    SET owner_user_id = (SELECT id FROM users WHERE username = 'agency')
    WHERE id = agency_alpha;
  END IF;

  IF EXISTS (SELECT 1 FROM users WHERE username = 'agency_beta') THEN
    UPDATE agencies
    SET owner_user_id = (SELECT id FROM users WHERE username = 'agency_beta')
    WHERE id = agency_beta;
  END IF;

  IF NOT EXISTS (SELECT 1 FROM rooms WHERE name = 'Alpha Lounge') THEN
    INSERT INTO rooms (id, agency_id, name, seat_mode, status, created_at)
    VALUES (gen_random_uuid(), agency_alpha, 'Alpha Lounge', 'FREE', 'active', now_ms);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM rooms WHERE name = 'Alpha Stage') THEN
    INSERT INTO rooms (id, agency_id, name, seat_mode, status, created_at)
    VALUES (gen_random_uuid(), agency_alpha, 'Alpha Stage', 'BLOCKED', 'active', now_ms);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM rooms WHERE name = 'Beta Lounge') THEN
    INSERT INTO rooms (id, agency_id, name, seat_mode, status, created_at)
    VALUES (gen_random_uuid(), agency_beta, 'Beta Lounge', 'FREE', 'active', now_ms);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM rooms WHERE name = 'Beta Stage') THEN
    INSERT INTO rooms (id, agency_id, name, seat_mode, status, created_at)
    VALUES (gen_random_uuid(), agency_beta, 'Beta Stage', 'BLOCKED', 'active', now_ms);
  END IF;
END $$;
