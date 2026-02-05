MANDATORY PROJECT ALIGNMENT & TECHNICAL ADJUSTMENTS
KAPPA APP — Development Directive

1. Project Phases (Remain Unchanged)
   Phase 2A: Android + Backend
   Phase 2B: iOS
   New Critical Directive (Effective Immediately)
   The Android application will be published on the Google Play Store as soon as it is ready, in order to mitigate the financial impact caused by project delays.
   Therefore, Android MUST NOT be delivered as an intermediate build.
   Direct Implication
   Android must be fully finalized prior to publication, meaning:
   Stable
   Fully functional
   Free of crashes
   With complete end-to-end user flows
   The following are strictly unacceptable:
   Placeholders
   Features marked as “in progress”
   “To be adjusted later” implementations
   Impact on Document Interpretation
   From this point forward, any issue resulting in:
   Inaccessible games
   Rooms causing application crashes
   Missing verification flows (phone/email)
   Free-text language input instead of selectable language
   Incomplete critical features
   will be considered a Release Blocker, and no longer a “phase-pending item.”

REQUIRED ADJUSTMENTS TO DOCUMENT
KAPPA_Full_Requirements_Phase2A_2B.docx

1. Financial Scope Clarification
   External cryptocurrency wallets with automatic withdrawals are out of scope.
   The app may store payment destination data only (e.g., PIX keys, USDT TRC20 addresses) for operational payout purposes handled externally by resellers.
   Direct monetary transfers or withdrawals of real money are out of scope.
   The app may support in-app purchases of virtual coins via credit card, strictly for entertainment purposes, with no direct cash-out or monetary conversion inside the app.

2. Languages & Real-Time Audio Stability
   Supported languages are English, Portuguese, and Spanish.
   Users must select one of the supported languages.
   Free-text language input is not allowed.
   The real-time audio SDK must meet production stability requirements.
   Application crashes during room access are not acceptable and block release readiness.

3. Agency Owner – Functional Definition DONE
   The Agency Owner:
   Manages an agency, its rooms, and its hosts.
   May create and manage rooms.
   May manage linked hosts and users.
   May view the agency’s total withdrawable balance.
   May view individual production per linked host.
   May track agency commission performance.

4. Role Assignment DONE
   Assignment of roles such as Reseller or Agency Owner is subject to Admin approval.
   A user may hold one or multiple roles only if explicitly approved by the Admin.

5. VIP Qualification HALF DONE HALF LEFT
   VIP is a dynamic qualification based on user activity and volume.
   It does not grant administrative permissions and may expire according to defined rules.

6. Rule Precedence DONE
   For economic actions, the most restrictive rule applies.
   For reporting and visualization purposes, the most permissive role applies.

7. Agency Visibility Requirements DONE
   Agency Users must be able to view:
   Total withdrawable balance of the agency
   Individual production per host linked to the agency

8. Reward Eligibility HALF DONE HALF LEFT
   Eligible entities for rewards include individual users/hosts and agencies.
   Resellers are not eligible for rewards within the system.

9. Withdrawable Balance Source DONE
   Withdrawable balances are derived from diamonds accumulated through gifts and activities, according to internal conversion rules defined by the Admin.

10. Observer Restrictions DONE
    Observers (non-seated users) may only send fixed/unique gifts and only to seated users.
    Multipliable gifts are restricted to seated users.

11. Gift Conversion Rules DONE
    Multipliable gifts grant 10% of the sent coin value as diamonds.
    Fixed/unique gifts grant 100% of the sent coin value as diamonds.

12. RTP / House Edge Override Structure DONE
    The system must allow RTP/House Edge overrides at three hierarchical levels:
    Level 1 – Global (Default)
    Example: RTP 70% / House Edge 30%
    Level 2 – By Gift Value Range DONE
    Gifts up to 100 coins → RTP X
    Gifts 125–500 coins → RTP Y
    High-value gifts → RTP Z
    Level 3 – By Qualification DONE
    Normal
    VIP (any level)
    Agency / Host (if applicable)
    Important Rule:
    Overrides always replace the higher-level configuration.
    The most specific configuration prevails.
    Example:
    Global = 70%
    Range 100–500 = 75%
    VIP = 80%
    A VIP user within that range uses 80%.

13. Gift Catalog Requirements DONE
    The gift catalog must explicitly distinguish between multipliable gifts and fixed/unique gifts.
    Each gift must define its conversion rule (coins to diamonds), contain exactly three visual variations, and be identifiable by a stable unique ID.
    Items with aggressive or negative symbolism (e.g., bombs, chests) are not allowed.

14. Separation of Multipliers DONE
    The system must clearly separate:
    Send Quantity Multiplier (1x, 3x, 5x, 10x, 25x, 50x, 75x, 100x)
    Multipliable Gift Game Multiplier (e.g., 250x)
    Coin consumption formula:
    Recipients × Quantity × Unit Gift Value
    For multipliable gifts:
    Each seated recipient receives diamonds equal to 10% × unit gift value × quantity.
    If awarded, the sender receives coin return equal to unit gift value × quantity × game multiplier.
    Coin return applies only to the sender.
    The quantity multiplier represents one single game round, not multiple independent rounds.

15. Gift Sending Rules DONE
    Both multipliable and fixed/unique gifts may be sent:
    To one or more seated users (including the sender if seated).
    To all seated users via a dedicated “send to all seated users” action.
    Observers:
    May only send gifts to seated users.
    May not send gifts to themselves.
    May not use the “send to all seated” action.

16. Administrative Control Over Gifts DONE
    The Admin must have full control over the gift system, including:
    Enabling/disabling gifts
    Adjusting gift prices
    Configuring RTP/House Edge at all hierarchy levels
    Defining override precedence
    Blocking gifts per user/agency/reseller
    Monitoring usage and returns
    Maintaining complete audit logs
    Gift animations must be fully re-stylized and must not replicate assets from the reference app.
    Visual assets will be provided by the project team.
    The developer must specify technical asset requirements in advance.

FINAL OBSERVATIONS
Economic Definitions (RTP & House Edge)
RTP (Return to Player) represents the theoretical percentage returned to users over time.
House Edge represents the percentage retained by the system.
RTP + House Edge = 100%
These are long-term statistical values and do not represent gambling or financial guarantees.

MISSING ITEMS — MANDATORY FUNCTIONAL DESCRIPTION
The absence of explicit mention of the following features does not exempt the developer from implementing them:
Rocket (equivalent to the “Bomb” feature in the reference app)
Stellar Race (progression path)
Item Wallet
These systems are central to user retention and must be implemented fully.

ROCKET — Collective Room Event
The Rocket is an automatic collective event triggered inside a room based on the total amount of coins spent in gifts within a 24-hour cycle.
Each room maintains a hidden counter.
The counter resets every 24 hours.
The first Rocket triggers at 1,300,000 coins.
Subsequent Rockets trigger at each additional multiple of 1,300,000 coins.
Each Rocket contains exactly 28 rewards.
The first three are coin rewards; the remaining are cosmetic or visual items.
Distribution may be random.
Rocket rewards escalate per level (1 through 7) as previously defined.
The Rocket:
Is not a traditional gift
Does not affect rankings
Must be validated via backend
Must generate audit logs

STELLAR RACE — Weekly Progression System
Weekly reset:
Starts Sunday 00:00:00
Ends Saturday 23:59:59
Rewards must be manually claimed within the active period
Failure to claim results in forfeiture
Exists for both users and agencies.
User thresholds and rewards escalate across five levels with VIP status durations as specified.
Agency thresholds include both total diamonds and minimum qualifying members.

ITEM WALLET
The Item Wallet stores non-monetary items obtained within the app.
Items are persistent and backend-validated.
Users may activate/deactivate cosmetic items.
Items do not directly affect economic balance.
All usage must generate logs.

FINAL STATEMENT
Rocket, Stellar Race, and Item Wallet:
Are mandatory features
Are core to the retention model
Must be fully implemented as described
Cannot be excluded due to prior omission in documentation
