# ADMIN DASHBOARD – Unclear Points

## 1. Exact list of games to configure, and their IDs/names

### Answer

### 1.1 Mini-games – mandatory initial scope

The initial package must contain at least **10 mini-games**, preferably acquired as a bundle (or individually, if necessary).

**Mandatory games in the initial package:**

- growth game (plant growth / bean growth)
- egg smash / gacha egg

**Critical note:**
You must confirm whether the available mini-games allow **RTP and House Edge configuration per game**.

If the provider uses fixed probabilities, this must be reported immediately, as economic adjustability is an essential requirement.

Games must have **stable backend IDs**, regardless of the display name used in the frontend  
(e.g.: `bean_growth`, `egg_smash`, `slot_x`, `crash_x`, etc.).

**Table (stable IDs – cited examples):**

| Display name | Backend ID (example) |
| ------------ | -------------------- |
| Bean Growth  | bean_growth          |
| Egg Smash    | egg_smash            |
| Slot         | slot_x               |
| Crash        | crash_x              |

---

## 2. Required RTP / House Edge ranges and validation rules

### Answer

### 1.2 RTP and House Edge

**Mandatory parameters:**

- global default value: RTP **70%** / House Edge **30%**
- possibility of override per game
- possibility of override per user type (qualification)

**Validation:**

- minimum RTP: **60%**
- maximum RTP: **90%**

These values must be persisted in the backend and applied in real time, without requiring a new deploy.

**Table (ranges and validation):**

| Parameter                  | Value |
| -------------------------- | ----- |
| Default RTP                | 70%   |
| Default House Edge         | 30%   |
| Minimum RTP                | 60%   |
| Maximum RTP                | 90%   |
| Per-game override          | Yes   |
| Per-qualification override | Yes   |

---

## 3. Which per-user “qualification” types exist (VIP tiers, roles, etc.)

### Answer

### 1.3 User qualification

**Account types:**

- normal
- VIP
- agency (role assigned to a user; not every user is an agency)
- reseller

**VIP system (automatic, based on played volume):**

- VIP1: ≥ USD 500 played
- VIP2: ≥ USD 1000
- VIP3: ≥ USD 1500
- VIP4: ≥ USD 2000
- VIP5 stars: ≥ USD 3000

**Adornment duration:**

- VIP1 to VIP4: 10 days
- VIP5 stars: 30 days

VIP is primarily visual, but must exist as a technical qualification to allow differentiated rules if needed.

**Table (VIP):**

| Level      | Played volume | Duration |
| ---------- | ------------- | -------- |
| VIP1       | ≥ USD 500     | 10 days  |
| VIP2       | ≥ USD 1000    | 10 days  |
| VIP3       | ≥ USD 1500    | 10 days  |
| VIP4       | ≥ USD 2000    | 10 days  |
| VIP5 stars | ≥ USD 3000    | 30 days  |

---

## 4. Locking rules logic

### Answer

### 1.4 Withdrawal rules and locking (locking rules)

**Mandatory minimum rules:**

- do not allow withdrawals below USD 10
- allow withdrawals only in multiples of USD 10

**Admin dashboard must allow configuration of intelligent locks, including:**

- withdrawal cooldown
- minimum turnover before withdrawal
- limits per period
- blocks per user, agency, or reseller
- blocking of specific actions (withdrawals, gifts, mini-games)

**Blocked without it**

- Persisting admin configs to backend
- Enforcing locks in gameplay, rooms, or gifting

**Answer:**
All parameters above (RTP, House Edge, qualifications, withdrawal rules, and locks) must:

- be persisted in the backend
- be applied in a centralized way
- directly impact mini-games, gifts, rooms, and withdrawals

**Needed format**

- Objective list of settings + rules (table preferred)
- 1–2 reference screenshots for admin screens (optional)

**Answer:**
Rules and parameters are described objectively above, with tables where applicable.

Any visual element not described textually must strictly follow the reference app.

---

# RESELLER DASHBOARD – Unclear Points

## 5. Internal management

### Answer

### 2.1 Structure

There are no sub-resellers.

The reseller can register employees/sellers, responsible for selling coins directly to end users (clients).

**Existing entities:**

| Entity   | Description                                     |
| -------- | ----------------------------------------------- |
| Reseller | Overall responsible for operations and payments |
| Seller   | Employee/seller registered by the reseller      |
| Client   | End user who purchases coins                    |

---

## 6. Seller limits

### Answer

### 2.2 Limits

The reseller dashboard must allow configuration of:

- total coin limit per seller
- daily coin limit per seller

---

## 7. Sales tracking fields

### Answer

### 2.3 Sales tracking

**Minimum fields:**

- sale_id
- seller_id
- buyer_id
- amount
- currency
- timestamp
- destination account

**Table (sales tracking):**

| Field               | Required |
| ------------------- | -------- |
| sale_id             | Yes      |
| seller_id           | Yes      |
| buyer_id            | Yes      |
| amount              | Yes      |
| currency            | Yes      |
| timestamp           | Yes      |
| destination account | Yes      |

---

## 8. Payment proof capture

### Answer

### 2.4 Payment proofs

The reseller must attach payment proofs (image or PDF) for payments made to hosts/users.

**Minimum metadata:**

- amount
- date
- beneficiary
- optional note

The admin must have full access to these proofs for audit purposes.

**Blocked without it**

- Accurate limits enforcement
- Sales reports and proof flows tied to backend storage

**Answer:**
Without the limits, tracking, and proofs described above, it is not possible to:

- correctly enforce blocks
- validate payments
- audit reseller operations

**Needed format**

- Flow list + required fields

**Answer:**
The required flows and fields are fully described above, with no dependency on implicit behavior.

---

# AGENCY DASHBOARD – Unclear Points

## 9. Management actions

### Answer

### 3. Agency (inside the app)

Agency management must exist inside the app, not in an external dashboard.

**Minimum functionalities:**

- view agency rooms
- view linked hosts
- view withdrawable diamonds per host
- total withdrawable diamonds
- automatic agency commission calculation

---

## 10. Team structure

### Answer

The agency structure is implicit:

- agency linked to registered users
- hosts linked to the agency
- centralized control handled by the app itself

There is no requirement for a complex hierarchy at this stage.

---

## 11. Commission rules

### Answer

**Mandatory commission rule:**

- for every USD 10 reached by users registered in the agency, the agency receives USD 2.2

Historical reports are not required at this stage.

---

# GIFT SYSTEM – Specific Confirmation Needed

## 12. Catalog

### Answer

### 4.2 Gift types

There are two types:

- multiplier gifts
- unique gifts

The complete value lists are defined in item 4.3.

---

## 13. Pricing

### Answer

All values are defined in app coins.

### A) Multiplier gifts

(Exact tables preserved)

Conversion upon receiving:

- recipient receives **10%** of the sent value in diamonds

### B) Unique gifts

Conversion upon receiving:

- recipient receives **100%** of the gift value in diamonds

---

## 14. Animations

### Answer

Functional logic must strictly follow the reference app.

No direct visual copying is allowed.

A complete visual restyling is mandatory.

---

## 15. Logic

### Answer

### 4.1 Gift targets

- seated users: all, self only, or manual selection
- observers: only unique gifts and only to seated users

### 4.3 Value and conversion logic

- multiplier gifts: 10% conversion to diamonds, multiplication controlled by RTP / House Edge
- unique gifts: 100% conversion

---

# Final Reference

This document is the **final reference** for:

- gift catalog
- pricing
- visual variations
- backend IDs mapping

If any implementation question arises, ask before making assumptions.
