# Milestones

## Phase 2A - Android + Backend
Milestones: 2

### Milestone 2A.1 - Core Android + Backend Integration
**Goal**
- Replace mock data with live backend while keeping navigation and UI stable.

**Scope (Delivered in this milestone)**

Backend
- User authentication system (login, logout, token-based sessions)
- User roles implemented (User, Agency, Reseller, Admin)
- Coin system implemented (balance, credit, debit)
- Backend APIs deployed and reachable (auth, users, coins)
- Persistent storage (database) with real data
- Consistent API error responses (status codes + message)

Android
- Android app connected to backend (no mock data)
- Login screen authenticates against backend
- User session persists across app restarts
- Home screen displays real user data from backend: username, role, coin balance
- Bottom navigation fully functional with real data
- Error handling for failed login and API errors
- Loading states for login and home data fetch

Integration and Docs
- API contract documented and shared with Android team
- Test accounts available for QA

**Acceptance Tests (Objective and Verifiable)**
This milestone is considered complete when all tests below pass:

Authentication Test
- Enter valid credentials -> login succeeds and token is stored
- Enter invalid credentials -> error message shown; no session created
- Logout -> token cleared and user returned to login

Session Persistence Test
- App restart -> user remains logged in and lands on Home
- Expired token -> user prompted to log in again

Backend Connectivity Test
- Disable internet -> app shows connection error
- Re-enable internet -> app recovers automatically

Real Data Test
- Coin balance shown in Android matches backend database value
- Changing coin balance in backend reflects in Android after refresh
- Backend user role (User / Agency / Reseller) is correctly shown in app

API Error Handling Test
- Backend returns 4xx/5xx -> user sees non-blocking error message
- 401 unauthorized -> user is logged out or prompted to re-authenticate

No Mock Data
- No Fake repositories are used
- All displayed data comes from backend APIs

If any test fails, the milestone is not approved.

### Milestone 2A.2 - Feature Completion + Android Production Readiness
**Scope (Delivered in this milestone)**

Android
- Audio rooms fully implemented and usable
- Join and leave audio rooms
- Real-time audio communication works between users
- Final UI assets applied (icons, images, branding)
- All screens implemented (no placeholders)
- App runs without crashes in production build

Backend
- Audio room management (create, join, leave)
- User presence tracking
- Coin usage logic linked to audio rooms (if applicable)
- Backend stable under multiple simultaneous users

**Acceptance Tests (Objective and Verifiable)**
This milestone is considered complete when all tests below pass:

Audio Room Test
- Two Android devices can join the same room
- Users can hear each other clearly
- Leaving a room disconnects audio immediately

Stability Test
- App runs for 30 minutes in audio room without crash
- Background -> foreground does not break audio

UI Completion Test
- No placeholder images or dummy text remain
- All screens visually complete and consistent

Production Build Test
- Release APK builds successfully
- App installs and runs on a clean device
- No critical errors in Logcat

Parity Test (Android vs Reference App)
- All Android features match the reference app (Pota)
- No missing core functionality

## Phase 2B - iOS + Finalization
Milestones: 2

### Milestone 2B.1 - Core iOS + Backend Integration
**Scope (Delivered in this milestone)**

iOS
- iOS app built using same backend as Android
- Authentication working on iOS
- User roles and coin balance displayed correctly
- Home screen shows real backend data
- Session persistence on app restart
- Bottom navigation functional

**Acceptance Tests (Objective and Verifiable)**
- Login with valid credentials succeeds
- Login with invalid credentials fails with error
- User remains logged in after app restart
- Coin balance matches backend database
- Same backend account works on Android and iOS

### Milestone 2B.2 - iOS Feature Completion + Store Readiness
**Scope (Delivered in this milestone)**

iOS
- Audio rooms fully operational on iOS
- Feature parity with Android and reference app
- Final UI assets applied
- Final QA on both platforms
- Store-ready builds prepared

**Acceptance Tests (Objective and Verifiable)**
- Android and iOS users can join the same audio room
- Audio works cross-platform (Android <-> iOS)
- No crashes during 30-minute usage session
- iOS app builds successfully in Release mode
- App passes basic App Store validation checks
- Android and iOS features are identical

**Store Publication (Assistance Only)**
- Acceptance confirmation
- Signed APK / AAB provided
- Signed IPA provided
- Store submission checklists completed
- I perform final submission using own accounts
