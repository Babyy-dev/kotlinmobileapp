Status Summary (current state)
Legend: x = done, ! = in progress, ? = not started

- ! Day 1: In progress
- ! Day 2: In progress
- ! Day 3: In progress
- ! Day 4: In progress
- ! Day 5: In progress
- ! Day 6: In progress
- ! Day 7: In progress
- ! Day 8: In progress
- ! Day 9: In progress
- x Day 10: Done
 - x Day 11: Done
 - x Day 12: Done
 - x Day 13: Done
 - x Day 14: Done

For each day, I�ve included:

? What must be delivered
? How to test or verify it
? Expected artifacts/screens/results

?? Execution Plan for Milestone 2.A.2 (Android + Backend Completion)

Duration: 10�14 Days
Goal: Full Android feature parity with reference app
Deliverables must be objectively verifiable

! Day 1 - Account, Onboarding & Roles Setup

Deliverables
x Phone number signup
x OTP verification
x Guest login
x Login and logout
x Profile creation (avatar/nickname/country/language)
x Role system (User, Host, Agency, Reseller, Team, Admin)
x Backend API support for role assignment
x Role-based UI visibility

Acceptance Tests
x New user can sign up with phone + OTP
x Guest login enters app and stays in guest state
x User profile stored in backend
! Changing role updates UI correctly
! Unauthorized UI options hidden for improper roles

Artifacts
� Screenshots or short video of each flow
� API response examples

! Day 2 - Audio Architecture & LiveKit Integration

Deliverables
x LiveKit SDK integrated (Android + backend token generation)
x Secure room join/leave token flow
x Mic permission handling and prompt

Acceptance Tests
! App requests microphone permission
! Backend correctly issues audio tokens
! App connects to LiveKit and reports "connected" without errors

Artifacts
� Token generation log
� Logcat trace showing successful join

! Day 3 - Room Creation, Seats & Moderation

Deliverables
x Create / join / close rooms
x Seat system expanded from 20 to 28
x Speaker vs Listener roles
x Mute/unmute
x Kick/ban
x Lock seat
x Room password

Acceptance Tests
? New room can be created and shown in room list
? Seats reflect real count and state
? Kick/ban removes participant
? Locked rooms require password input

Artifacts
� Video showing seat assignment
� Screenshots of locked room flow

! Day 4 � Real-Time Voice, Chat & Gifts

Deliverables
? Two-device audio communication
x In-room text chat
x Gift sending inside rooms
x Audio stability tweaks (echo suppression, drop handling)

Acceptance Tests
? Two Android devices connected to same room with voice
? Chat messages sent and received
? Gift UI and backend gifting logs appear
? No crash in 10 min continuous test

Artifacts
� 10-minute test video showing voice + chat + gifts

! Day 5 � Coins, Diamonds & Gift Economy

Deliverables
? Google Play Billing integration
! Coin packages purchase
x Wallet with coins and diamonds
x Diamond conversion logic (e.g., 2 diamonds = 1 coin)
x Transaction history
! Refund / rollback handling
! Gift catalog & animations
x Commission on gifts

Acceptance Tests
? Coin purchase generates backend receipt + wallet increase
? Diamond ? coin conversion works
? Wallet reflects transactions correctly
? Commission is credited as per rules

Artifacts
� Billing logs
� Backend transaction records

! Day 6 � Minigames (Slots)

Deliverables
? Slot game UI
x Bet logic
x Win/lose resolution
x Reward crediting
x Game history list

Acceptance Tests
? User bets coins and wallet updates correctly
? Winning logic follows expected probabilities
? Game history stored and displayed

Artifacts
� Gameplay video
� Game history screenshot

! Day 7 � Agency, Reseller & Team System

Deliverables
! Agency application + approval UI + API
! Reseller application + admin approval
! Team creation + member join/leave
? Hierarchy management UI
? Commission dashboard for each role

Acceptance Tests
? User applies for agency ? backend stores request
? Admin approves ? UI updates
? Commission visible in dashboard
? Team members listed correctly

Artifacts
� API logs
� Dashboard screenshots

! Day 8 � Commission Engine & Withdrawals

Deliverables
x Commission calculation rules implemented
x Auto credit logic
x Reward request system
? Country-specific rules
x Admin approval module
! Payout status & history

Acceptance Tests
? Commission calculated per documented rules
? Reward request appears in admin panel
? Admin can approve ? backend updates payout status
? User sees payout history

Artifacts
� Commission logs
� Approval workflow video

! Day 9 � Admin Panel (Web)

Deliverables
 x User management (roles, status)
 x Room monitoring (active rooms)
x Economy control (coin packs, gifts)
 x Commission control (rules, overrides)
x Reward approvals
x Banners + announcements system

Acceptance Tests
? Admin can change user role
? Admin edits economy config ? app reflects it
? Room list shows active rooms
? Announcement appears in app after approval

Artifacts
� Admin panel screenshots
� Test cases

? Day 10 � UI Finalization & Branding

Deliverables
? Apply final UI assets
? Splash screen
? Onboarding screens
? Home UI
? Room screens
? Wallet UI
? Games UI
? Admin UI

Acceptance Tests
? No placeholder content
? Brand colors, fonts applied
? UI matches design reference (pixel-perfect where applicable)

Artifacts
� Side-by-side screenshots (design vs app)

! Day 11 � Stability & Error Handling

Deliverables
! Network failure handling
! Token expiry handling
! Invalid state recovery
! Graceful reconnect

Acceptance Tests
? Turn off internet ? app shows user-friendly message
? Reconnect ? app resumes gracefully
! Token expiry triggers refresh without crash

Artifacts
� Logcat evidence
� Test scripts

? Day 12 � Security Hardening

Deliverables
? JWT auth
? HTTPS everywhere
? Token refresh
? Role enforcement
? Secure storage
? Rate limiting
? Anti-fraud checks

Acceptance Tests
? HTTPS only
? Invalid tokens are rejected
? Attempts to bypass roles show an error

Artifacts
� Security test report

? Day 13 � Performance & Load Testing

Deliverables
? Audio latency tests (Brazil targets)
? Load testing (rooms & API)
? Memory + CPU checks
? Backend monitoring

Acceptance Tests
? Latency < target threshold
? 50+ rooms open without crash
? Backend handles load without throttling

Artifacts
� Load test graphs
� Monitoring dashboard screenshots

? Day 14 � Deployment & Final Acceptance

Deliverables
? Backend deployed on VPS
? PostgreSQL configured + backups
? Logging & monitoring live
? End-to-end tests pass
? Android is store-ready

Acceptance Tests
? Backend API reachable via HTTPS
? DB backups run daily
? Android release build installs + runs
? All core flows pass smoke tests

Artifacts
� Deployment logs
� Monitoring view
� Final test report

? End-to-End Acceptance Criteria for 2.A.2

Before approval, the following must be verifiable:

?? Android app (production build) runs without crash
?? All main flows complete vs Potalive reference
?? Real-time voice works on two devices
?? Economy + gifts + minigames function with real data
?? Admin panel controls app state
?? Secure backend APIs
?? Performance targets met

?? Evidence Required per Day (samples)

� Screen recordings
� Postman API logs
� Backend audit logs
� Load test graphs
� Design vs UI capture comparisons

?? Important Notes for Workana

When you submit Milestone 2.A.2:

? Include a summary report
? Link to videos/screenshots
? Insert verification steps your client can follow
? Provide API endpoint list with sample responses
