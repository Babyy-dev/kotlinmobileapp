-- Demo credit/debit transactions for existing users.
-- This script inserts two transactions per demo user and updates wallet balances.

CREATE EXTENSION IF NOT EXISTS pgcrypto;

DO $$
DECLARE
  base_ms bigint := (extract(epoch from now()) * 1000)::bigint;
  target record;
  current_balance bigint;
  credit_amount bigint := 5000;
  debit_amount bigint := 1000;
  balance_after bigint;
  idx int := 0;
  credit_ts bigint;
  debit_ts bigint;
BEGIN
  FOR target IN
    SELECT id, username
    FROM users
    WHERE username IN (
      'master',
      'reseller',
      'reseller2',
      'agency',
      'agency_beta',
      'user',
      'user_alpha',
      'user_beta',
      'user_gamma'
    )
  LOOP
    idx := idx + 1;
    credit_ts := base_ms + (idx * 1000);
    debit_ts := credit_ts + 500;

    SELECT balance_coins INTO current_balance
    FROM wallet_coins
    WHERE user_id = target.id;

    IF current_balance IS NULL THEN
      current_balance := 0;
    END IF;

    balance_after := current_balance + credit_amount;
    INSERT INTO coin_transactions (id, user_id, type, amount, balance_after, created_at)
    VALUES (gen_random_uuid(), target.id, 'CREDIT', credit_amount, balance_after, credit_ts);

    balance_after := balance_after - debit_amount;
    IF balance_after < 0 THEN
      balance_after := 0;
    END IF;

    INSERT INTO coin_transactions (id, user_id, type, amount, balance_after, created_at)
    VALUES (gen_random_uuid(), target.id, 'DEBIT', debit_amount, balance_after, debit_ts);

    INSERT INTO wallet_coins (user_id, balance_coins, updated_at)
    VALUES (target.id, balance_after, debit_ts)
    ON CONFLICT (user_id) DO UPDATE
      SET balance_coins = EXCLUDED.balance_coins,
          updated_at = EXCLUDED.updated_at;
  END LOOP;
END $$;
