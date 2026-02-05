# KAPPA Technical Build & Parity Guide

This guide aligns build steps with `fullfunctional.md` and includes:
- Done / Not Done parity checklist
- How to complete what is missing (daily plan)
- Full image checklist by page

---

## A) Build Instructions

### Prerequisites
- Windows 10/11
- Android Studio (latest stable)
- JDK 17
- Android SDK + Build Tools

### Android App (Debug)
```
./gradlew :app:assembleDebug
```
APK output:
```
app/build/outputs/apk/debug/app-debug.apk
```

### Android App (Release)
1) Configure signing in `app/build.gradle.kts`
2) Build:
```
./gradlew :app:assembleRelease
```

### Backend (Ktor)
```
./gradlew -p backend assemble
./gradlew -p backend run
```

### API Base URL
Update Retrofit base URL in app config (if needed).
If using local backend from a device, use machine IP (not `localhost`).

### Game Socket (Socket.IO)
Set `BuildConfig.GAME_WS_URL`. If empty, local mock engine is used.

---

## B) Functional Parity Checklist (from `fullfunctional.md`)

Legend: DONE / PARTIAL / NOT DONE

### 1. Authentication
- DONE Phone + password login flow (phone input + E.164 formatting)
- DONE Country code selector on login (ISO list + dial codes)
- DONE Redirect to main screen after login
Notes:
- Phone login supports country code selector and E.164 formatting.

### 2. Main Screen (Popular default)
- DONE Tabs exist (Meu/Popular/Postagens) with active state
- DONE Rotating banner (dynamic list + auto-rotate)
- DONE Mini‑games top 3 section (popular list)
- DONE Room filters include region/continent grouping
- DONE Top right icons (my room, notifications, search)
- DONE Search across rooms/users/agencies by name or ID
Notes:
- Banners, mini‑games, and search are backed by API endpoints (fake backend seeded).

### 3. Bottom Navigation
- DONE Rooms / Messages / My tabs wired

### 4. Messages Module
- DONE Messages + Friends tabs working
- PARTIAL Family tab present (needs final spec decision)
- DONE Messaging allowed with friends (basic)
- PARTIAL Messaging with room users not fully enforced

### 5. Audio Room Core
- DONE Room header shows agency name/icon + room ID + favorite + minimize
- DONE Seat request on locked seats (request action + system message)
- DONE Join with mic ON or MUTED choice (pre-connect dialog)
- DONE Chat + Rewards tabs (reward stream)
- DONE Bottom action icons (seat/chat/gift/tools)

### 6. Gift System
- ⚠ Gift list + send works, but categories & seated list in panel not implemented
- ⚠ Send logic (self/all/selected) not fully implemented
- ⚠ Multipliable vs fixed gift logic not fully implemented (RTP multiplier)
How to complete:
- Add gift categories and seated user list to gift panel.
- Implement send targets (self/all/selected) in request schema.
- Add RTP multiplier logic for multipliable gifts only.

### 7. Hidden Room Coin Counter
- ❌ Not implemented
How to complete:
- Add server-side counter per room.
- Increment on gift play and mini‑game spend.
- Trigger Rocket Event rules from backend.

### 8. In‑Room User Profile
- ❌ Not implemented (tap seat to show full profile)
How to complete:
- Add profile sheet on seat tap with required fields.

### 9. “My” Menu (User Panel)
- ❌ Wallet, Recharge, Backpack, Settings, VIP, Star Path not implemented
How to complete:
- Implement new screens + API endpoints for wallet, recharge, inventory, VIP, and star path.

### 10. User Types & Permissions
- ⚠ Admin/Agency/Reseller roles exist
- ❌ Agency owner room management features not implemented
How to complete:
- Add role‑gated actions in room settings (rename, background, lock seats, admins).

### 11. Agency Center (Agency only)
- ⚠ Basic agency tools wired
- ❌ Full modules (anchor management, join requests, invite anchor, rules, revenue, history, withdrawals)
How to complete:
- Add missing modules + backend endpoints and link from Agency Center UI.

### 12. Global 100x+ Effect
- ❌ Not implemented
How to complete:
- Add global event overlay on main screen and room deep link.

---

## C) Daily Plan to Complete Missing Items

Day 1
- Implement login by phone + country code selector.
- Update auth backend and client validation.

Day 2
- Implement admin‑controlled rotating banner with image upload and deep link config.
- Wire dynamic banner to main screen.

Day 3
- Implement top‑right action icons: my room, notifications, search.
- Build search across rooms/users/agencies by name/ID.

Day 4
- Implement Popular mini‑games “top 3” based on stats or admin priority.
- Add filtering for region/continent; sort by activity.

Day 5
- Add room top area: agency name/icon, room ID, favorite, minimize, exit.
- Add join options: mic ON/MUTED choice.

Day 6
- Implement seat request flow when locked.
- Add in‑room user profile (tap seat).

Day 7
- Implement Rewards tab in chat + system messages.
- Add bottom action icons (seat request, PM, settings).

Day 8
- Gift panel: seated users list + categories.
- Sending modes: self/all/selected.

Day 9
- Multipliable gifts: RTP multiplier logic.
- Fixed gifts: no multiplier.

Day 10
- Hidden room coin counter (server‑validated) + rocket trigger hooks.

Day 11
- “My” menu modules (Wallet, Recharge, Backpack, Settings, VIP, Star Path).

Day 12
- Agency center full modules (anchor mgmt, join requests, invite, rules, revenue, history, withdrawals).

Day 13
- Agency owner room management: name, background, lock seats, add admins.

Day 14
- Global 100x+ effect on main screen with clickable room redirect.
- QA pass for all updated flows.

---

## D) Full Image Checklist (Per Requirement Page)

### Login
- Country selector flag icons (per country code list)
- Background art (login screen)
- App logo

### Main Screen (Rooms)
- Rotating banner images (multiple, admin upload)
- Mini‑game icons (top 3)
- Region/country filter chips background
- Top‑right action icons (my room / notifications / search)

### Messages
- Avatar placeholders
- Status indicators (online/offline)
- Badge icons (VIP etc.)

### Audio Room
- Agency icon
- Room header background
- Seat icons (empty/occupied/locked)
- Bottom action bar icons (chat, seat request, PM, settings, mini‑games, gifts)
- Rewards tab decoration

### Gifts
- Gift category icons
- Gift item thumbnails
- Multipliable/Fixed labels
- Send button image

### “My” Menu
- Wallet icon
- Recharge icon
- Backpack icon
- Settings icon
- VIP badge assets
- Star Path assets

### Agency Center
- Agency badge/logo
- Modules icons (anchor, requests, invite, rules, revenue, history, withdrawals)

### Global 100x Effect
- Special animation asset
- Trophy/100x badge

---

## E) Notes
- This guide aligns *functional parity* only (UI theme can vary).
- Items marked ❌ are mandatory for launch parity.
