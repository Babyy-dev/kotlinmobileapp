# KAPPA PROJECT

## First Milestone – 7-Day Execution Plan (Backend Core)

By the end of Day 7, the system must include:

- Authentication & user roles
- Coin & diamond wallets
- Gift & multiplier system
- Agency commission engine
- Reward (withdrawal) system
- Full audit logs
- Admin (User Master) controls
- No client-side economy logic

### **Day 1 – Architecture & Rules Lock**

**Goal:** Prevent incorrect implementation and future disputes.

**Tasks:**

- Review both official KAPPA documents
- Extract all non-negotiable rules:
  - Coins = spending unit
  - Diamonds = receiving unit
  - 2 diamonds = 1 coin
  - Multipliers do NOT affect diamonds
  - Commission follows user binding
  - Rewards are manual only
- Confirm tech stack:
  - Java
  - Spring Boot
  - PostgreSQL

**Deliverables:**

- `ECONOMY_RULES.md`
- `ARCHITECTURE.md`
- `MILESTONES.md`

---

### **Day 2 – Database & Project Setup**

**Goal:** Lock the economy structure at the database level.

**Tasks:**

- Create Spring Boot project
- Configure PostgreSQL
- Design tables:
  - users
  - agencies
  - wallets
  - gifts
  - rooms
  - transactions
  - commissions
  - rewards
  - logs
- Add constraints:
  - No negative balances
  - Valid conversions only

**Deliverables:**

- Database schema
- Migration scripts
- Backend runs with DB

---

### **Day 3 – Authentication & Roles**

**Goal:** Enforce hierarchy and access control.

**Tasks:**

- Implement signup & login
- JWT authentication
- User roles:
  - User Master
  - Reseller
  - Agency Owner
  - Common User
- Mandatory agency binding

**Deliverables:**

- Auth APIs
- Role-based access
- Token system

---

### **Day 4 – Wallet System (Coins & Diamonds)**

**Goal:** Lock currency behavior.

**Tasks:**

- Coin wallet
- Diamond wallet
- Conversion rule:
  - 2 diamonds = 1 coin
- Prevent negative balances
- Log every transaction

**Deliverables:**

- Wallet APIs
- Conversion engine
- Audit logs

---

### **Day 5 – Gifts & Multipliers**

**Goal:** Control diamond creation correctly.

**Tasks:**

- Individual gifts (100% diamonds)
- Group gifts
- Multiplier gifts (10% rule)
- Backend-only multiplier engine
- Probability system

**Deliverables:**

- Gift APIs
- Multiplier logic
- Diamond generation rules

---

### **Day 6 – Agency Commission & Rewards**

**Goal:** Implement money logic safely.

**Tasks:**

- Commission follows user binding
- Calculated only on diamonds
- Credited in diamonds
- Reward system:
  - 600,000 diamonds = $10
  - Minimum thresholds
  - Diamond locking
  - Status flow (Pending → Approved → Paid/Rejected)

**Deliverables:**

- Commission engine
- Reward APIs
- Admin approval flow

---

### **Day 7 – Admin, Logs & Validation**

**Goal:** Make the system auditable and acceptance-ready.

**Tasks:**

- Admin APIs:
  - View users
  - View economy
  - Approve rewards
- Immutable logs for:
  - Gifts
  - Conversions
  - Commissions
  - Rewards
- Full API testing (Postman)
- Final documentation

**Deliverables:**

- Admin access
- Audit system
- Test report
- Final docs

## 📌 Proof of Progress (Daily)

| Day | Proof Required             |
| --- | -------------------------- |
| 1   | Rule & architecture docs   |
| 2   | DB schema                  |
| 3   | Auth APIs                  |
| 4   | Wallet logic               |
| 5   | Gift + multiplier code     |
| 6   | Commission + reward system |
| 7   | Admin + logs + tests       |

No proof = no progress.

If this milestone is successful, Android and iOS integration can proceed safely.
