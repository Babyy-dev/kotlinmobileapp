# Day 1-7 Work Report

## Day 1 - Project Setup and Architecture Lock
Status: Done

Tasks completed
- Reviewed Phase 2A.1 and information.md rules.
- Locked non-negotiables: backend authoritative, no client-side economy logic, coins are spending unit, roles enforced on backend.
- Backend: Ktor (Kotlin) + PostgreSQL.
- Android project wired for backend integration.

Deliverables
- Backend runs locally with Docker services.
- Database connected and seeded.
- Android project opens and builds in Android Studio.

Notes
- Backend framework used is Ktor instead of Spring Boot.

---

## Day 2 - Authentication System (Backend + Android)
Status: Done

Tasks completed
- Signup API: POST /api/auth/signup
- Login API: POST /api/auth/login
- JWT token system with refresh tokens.
- Android login and signup screens connected to backend.
- Tokens stored via DataStore.

Deliverables
- Valid login works; invalid login returns error.
- User data comes from backend.
- No mock authentication in use.

---

## Day 3 - User Roles and Session Persistence
Status: Done

Tasks completed
- Role system enforced on backend and mapped in Android.
- Session persistence via stored access token.
- Role displayed on Home and Profile screens.

Deliverables
- App restart keeps user logged in (splash checks token).
- Role displayed correctly and comes from backend.

---

## Day 4 - Coin System and Database Sync
Status: Done

Tasks completed
- Coin wallet API: balance, credit, debit.
- Android shows real coin balance from backend.
- Manual refresh triggers new backend fetch.

Deliverables
- Coin balance matches database.
- Changing DB value reflects in app after refresh.
- No fake repositories.

---

## Day 5 - Backend API Stability and Error Handling
Status: Done

Tasks completed
- Consistent API error responses.
- Android handles no internet and server errors.
- Auto-recover on reconnect for Home and Rooms.

Deliverables
- Offline shows connection error.
- Reconnect triggers auto-refresh.
- App does not crash on network errors.

---

## Day 6 - Navigation and Real Data Integration
Status: Done

Tasks completed
- Bottom navigation wired for Home, Rooms, Profile.
- Home shows username, role, coin balance from backend.
- Rooms list uses real backend data.
- Profile uses real backend data.

Deliverables
- No mock data or placeholder values.
- All screens use backend APIs.

---

## Day 7 - Final Testing and Acceptance Validation
Status: In progress

Checklist
- [x] Valid login works.
- [x] Invalid login fails with error.
- [x] Session persists after restart.
- [x] Offline handling works and auto-recovers.
- [x] Coin balance matches database and refresh works.
- [x] Role display is correct.

Progress
- Debug APK built: app/build/outputs/apk/debug/app-debug.apk
- Emulator test: login and signup confirmed working.

Notes
- Error message copy updated for invalid login and offline errors; re-test required.
- Fake repositories removed; all data flows from backend APIs.

Artifacts to produce
- [x] Test report (run locally): test_report.md.
- [x] Release APK or AAB.
- [ ] Backend deployed.

Build note
- Debug build succeeded after configuring Android SDK.
- LiveKit `connect` is deprecated in SDK 1.2.2 (warning only).
