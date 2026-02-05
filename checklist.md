# Checklist

Legend
- x = done
- ! = in progress
- ? = not started

## Page Plans (Detailed)

### 1) Rooms (Home for live rooms)
Target
- x Potalive-style rooms listing with tabs (Meu/Popular/Postagens), filters by country, and high-visibility room cards.

Current
- ! UI styled with art background and tabs. Rooms list is wired to fake data.

Missing / Improve
- ? Country chips + filtering logic tied to API.
- ? Real room images, counts, badges.
- ? Seat count parity + correct room join flow.

Plan
- Wire country filter to rooms API.
- Update room card model to include images, badges, country.
- Enforce seat count + join rules and lock handling.

Estimate
- 5 days.

Deliverable
- Rooms screen with country filters, correct room cards, and working join flow.

---

### 2) Room Detail (Audio/Seats/Chat/Gifts/Tools)
Target
- x Fully interactive live room with real-time updates and gift flow parity.

Current
- ! Sections separated; UI shells exist; LiveKit connection in place; fake data for seats/messages/gifts.

Missing / Improve
- ? Real-time seat and chat sync.
- ? Gift animations/overlay pipeline.
- ? Moderation tools (mute/kick/ban) fully functional.

Plan
- Add room WebSocket channel; sync seats/messages/gifts.
- Implement gift overlay animation + receipts.
- Wire moderation actions to server.

Estimate
- 9 days.

Deliverable
- Live room with realtime seats/chat, gift overlay, and working moderation tools.

---

### 3) Inbox / Home (Mensagem/Amigos/Familia)
Target
- x Potalive-style inbox with live message list, search, and working family flows.

Current
- ! UI styled; inbox/friends/family now wired to backend.

Missing / Improve
- ! Message threads wired; unread counts still missing.
- ! Friend list API wired; search still missing.
- ! Family create/join/manage/members wired; rooms list depends on family-linked rooms.

Plan
- Wire inbox API, search, unread badges.
- Implement friends API list and profile actions.
- Implement family endpoints and flows.

Estimate
- 6 days.

Deliverable
- Inbox with real threads, friends list, and full family flows.

---

### 4) Profile
Target
- x Profile summary, edit flow, avatar upload, role tools access.

Current
- ! UI exists; edit/upload flows are local.

Missing / Improve
- ! Persisted profile updates, avatar validation, and error handling.

Plan
- Wire profile endpoints and add validation.

Estimate
- 3 days.

Deliverable
- Profile edit and avatar upload fully persisted with validation.

---

### 5) Admin Dashboard (role-gated)
Target
- x RTP/House Edge config, per-game & per-user rules, intelligent locking, audit log.

Current
- ! UI and local state only.

Missing / Improve
- ? Real persistence and validation.
- ? Audit history, role-based access.

Plan
- Wire admin endpoints.
- Add validation + error feedback.
- Add audit log list.

Estimate
- 6 days.

Deliverable
- Admin dashboard with live configs and audit log.

---

### 6) Agency Tools (role-gated)
Target
- x Full agency management: applications, teams, commissions, hosts, rooms.

Current
- ! UI shells only.

Missing / Improve
- ? Real lists/actions; approvals; commissions calculation.

Plan
- Wire endpoints for applications/teams/commissions.
- Implement approval flows and summaries.

Estimate
- 6 days.

Deliverable
- Agency dashboard with working approvals, teams, and commission summaries.

---

### 7) Reseller Tools (role-gated)
Target
- x Seller management, limits, sales tracking, payment proof capture.

Current
- ! UI shells only.

Missing / Improve
- ? Real seller list/limits, sales/receipt flow, upload handling.

Plan
- Wire endpoints; implement add/update sellers and limits.
- Implement payment proof upload + review.

Estimate
- 6 days.

Deliverable
- Reseller dashboard with seller limits, sales tracking, and proof uploads.

---

### 8) Mini-Game Detail (role + room gated)
Target
- x Real-time game play tied to room, server-authoritative state.

Current
- ! Hub/detail UI present, realtime backend + Socket.IO + session validation wired.

Missing / Improve
- ! Gift_play coin debit is wired; reward distribution + entry fee handling pending.

Plan
- Build game server + client sync + anti-cheat.

Estimate
- 10 days.

Deliverable
- Mini-game playable in room with realtime sync and reward logic.

---

### 9) Onboarding Country/Profile
Target
- x ISO country selector with flags + validated profile completion.

Current
- x ISO list + validation + language selection enforced.

Missing / Improve
- ! Avatar upload validation + profile persistence checks.

Plan
- Verify ISO source, optimize search, enforce minimum fields.

Estimate
- 4 days.

Deliverable
- Onboarding complete with ISO country list and required fields enforced.


## Pages Not Shown Yet (Targets + Plan)
- ! Admin Dashboard: role-gated and not visible for non-admin users; complete by wiring admin endpoints and audit logs.
- ! Agency Tools: role-gated; complete by wiring agency endpoints and approvals.
- ! Reseller Tools: role-gated; complete by wiring reseller endpoints and sales/proof flows.
- ! Mini-Game Detail: hidden until room/game is opened; realtime server + anti-cheat wired; reward flow pending.

## 14-Day Daily Plan (from 2026-02-04)
Day 1 (2026-02-04)
- x Verify country ISO list + search filter; fix rendering issues.
- x Review onboarding flow: enforce required fields and avatar validation.
Deliverable
- Country list verified + onboarding validation in place.

Day 2 (2026-02-05)
- x Rooms list: country chips + filter logic; update room card model (image/badge/country).
- x Seat count parity check; adjust max seats and join rules.
Deliverable
- Rooms list filters + correct seat count rules.

Day 3 (2026-02-06)
- x Room detail: real-time seat state sync (WebSocket layer stub + client state updates).
- x Chat: realtime message sync + unread count in room.
Deliverable
- Room detail seat/chat realtime stub working end-to-end.

Day 4 (2026-02-07)
- x Gift system: catalog/pricing parity review; wire send flow to real backend (placeholder endpoints if needed).
- x Gift overlay animation prototype.
Deliverable
- Gift send flow wired + basic overlay animation.

Day 5 (2026-02-08)
- x Inbox: message list API wiring, unread badges, search behavior.
- x Friends: list + profile action stub.
Deliverable
- Inbox threads + friend list functional.

Day 6 (2026-02-09)
- x Family: create/join/manage flows; members list and rooms list wiring.
- x Validation + error states for family actions.
Deliverable
- Family create/join and members/rooms list working.

Day 7 (2026-02-10)
- x Admin dashboard: wire RTP/House Edge endpoints; validation and feedback.
- x Admin audit log list stub.
Deliverable
- Admin dashboard saves + audit list stub.

Day 8 (2026-02-11)
- x Admin: per-game/user config + locking rules wiring; add edit/delete flows.
- x Admin QA pass.
Deliverable
- Admin per-game/user configs + locking rules working.

Day 9 (2026-02-12)
- x Agency tools: applications + approvals; commission summary wiring.
- x Teams/hosts lists wired.
Deliverable
- Agency approvals + commissions + lists working.

Day 10 (2026-02-13)
- x Reseller tools: seller list + limits + sales tracking wiring.
- x Payment proof upload flow stub.
Deliverable
- Reseller limits/sales + proof upload stub.

Day 11 (2026-02-14)
- x Room detail: moderation tools (mute/kick/ban) wired.
- x Gift animation + receipts polish.
Deliverable
- Moderation tools working + improved gift overlay.

Day 12 (2026-02-15)
- x Mini-games: WebSocket server contract + client sync; reward deduction flow.
Deliverable
- Mini-game realtime contract + reward flow wired.

Day 13 (2026-02-16)
- x Performance + stability pass: crash/ANR review, logs cleanup.
- x UX polish for rooms/inbox/profile.
Deliverable
- Performance pass + UX polish applied.

Day 14 (2026-02-17)
- x End-to-end QA: login/onboarding/room/gift/dashboard flows.
- x Release checklist for store readiness.
Deliverable
- E2E QA complete + release checklist ready.

## Daily Check Items
- Build status (debug build success/fail).
- UI parity review (Rooms/RoomDetail/Inbox/Profile/Dashboards).
- Critical flows (login, onboarding, join room, gift send, dashboard access).
- Bugs discovered and fixed.
