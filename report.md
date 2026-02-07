KAPPA - FUNCTIONAL PARITY CHECKLIST (INVESTOR VIEW)
Purpose of this document:
This is a feature-by-feature parity checklist against the reference app (Potalive).
- This is NOT a development plan
- This is NOT technical documentation
- This is an objective status checklist
Each item is marked as:
- Not started
- In progress
- Completed and fully functional
This document answers one question only:
"Does this feature work exactly like the reference app - yes or no?"

1. AUTHENTICATION & SESSION
   [Code] Phone number input field - Completed
   [Code] Country code selector (ISO + dial code) - Completed
   [Test] OTP / password validation works correctly - In progress
   [Test] Login completes successfully - In progress
   [Test] Session persists after app restart - In progress
   [Test] Invalid login handled without crash - In progress
   [Test] Is this section fully functional? - No

AUTH FLOW TEST CHECKLIST (RUN ON DEBUG APK)
Step 1: Password login (initial screen)
- Enter username + password
- Expect: success -> role dashboard or onboarding
- Expect: invalid credentials -> error message, no crash

Step 2: Toggle to phone login
- Tap "Use phone no. instead"
- Select country code (flag + dial code)
- Enter phone and request OTP
- Expect: OTP sent message
- Enter OTP and verify
- Expect: login success -> role dashboard or onboarding

Step 3: Signup (username/email/password/phone)
- Fill username/email/password
- Select country code + phone
- Tap "Sign up"
- Expect: OTP required message and OTP section appears
- Verify OTP
- Expect: account activated + login success

Step 4: Phone validation checks
- Short number -> blocked with error
- Too long number -> blocked with error
- Change dial code manually -> flag updates to match

Step 5: Session persistence
- Close app and reopen
- Expect: session still active, no relogin

Step 6: Guest login
- Tap "Continue as Guest"
- Expect: enters app without crash

2. HOME / MAIN SCREEN (POPULAR)
   [Test] Room list loads from real backend - In progress
   [Test] Room cards display correct data (name, agency, activity) - In progress
   [Test] Rotating banner loads dynamically - Completed
   [Test] Banner clickable and redirects correctly - Not started
   [Code] Banner configurable from Admin Dashboard - Completed
   [Test] Popular mini-games (top 3) load correctly - In progress
   [Test] Country filter works correctly - In progress
   [Test] Continent filter works correctly - In progress
   [Test] Search rooms by name - In progress
   [Test] Search rooms by ID - In progress
   [Test] Search users / agencies - In progress
   [Test] Tapping a room opens it without crash - In progress
   [Test] Is this section fully functional? - No

3. BOTTOM NAVIGATION
   [Test] Rooms tab works - Completed
   [Test] Messages tab works - In progress
   [Test] My tab works - Not started
   [Test] Navigation persists state correctly - Not started
   [Test] Is this section fully functional? - No

4. MESSAGES & SYSTEM NOTIFICATIONS
   [Test] Private chat opens correctly - In progress
   [Test] Messages sent and received in real time - In progress
   [Code] System messages auto-generated (recharge, invite, approval) - Completed
   [Test] System messages cannot be sent manually - Not started
   [Test] Message history loads correctly - Not started
   [Test] Is this section fully functional? - No

5. ROOM ENTRY & AUDIO
   [Test] User can enter a room - In progress
   [Test] Audio connects successfully - In progress
   [Test] Join with mic ON / MUTED choice works - In progress
   [Test] No audio crash on enter/exit - Not started
   [Test] Room header shows agency name/icon/room ID - Completed
   [Test] Is this section fully functional? - No

6. SEATS & SYNCHRONIZATION
   [Test] Seats load correctly (up to 28) - In progress
   [Test] Seat status syncs across users - In progress
   [Test] Free seats allow instant seating - In progress
   [Test] Blocked seats require approval - In progress
   [Test] Seat request notification works - Not started
   [Test] Admin can remove a user from seat - Not started
   [Test] Is this section fully functional? - No

7. ROOM CHAT
   [Test] Chat loads in real time - In progress
   [Test] Messages sync correctly - In progress
   [Test] @mention works - Not started
   [Test] Automatic translation works both ways - Not started
   [Test] No crash on high message volume - Not started
   [Test] Is this section fully functional? - No

8. GIFT SYSTEM
   [Test] Gift panel opens correctly - In progress
   [Test] Gift list loads from backend - In progress
   [Test] Gift categories shown - Not started
   [Test] Seated users list shown in gift panel - Not started
   [Test] Send gift to self works - Not started
   [Test] Send gift to all seated users works - Not started
   [Test] Send gift to selected users works - Not started
   [Test] Coins deducted correctly - In progress
   [Test] Diamonds generated correctly - In progress
   [Test] Multiplier logic works exactly per rules - Not started
   [Test] Is this section fully functional? - No

9. ROCKET (ROOM RETENTION SYSTEM)
   [Code] Hidden room coin counter exists (backend) - Completed
   [Test] Counter increments correctly - Not started
   [Test] Milestones trigger rewards - Not started
   [Test] Rewards appear in Reward tab - Not started
   [Test] Users cannot see the counter - Not started
   [Test] Is this section fully functional? - No

10. REWARD TAB
    [Code] Rewards tab exists - Completed
    [Test] Decorative rewards displayed - Not started
    [Test] Event rewards displayed - Not started
    [Test] Reward history persists - Not started
    [Test] Is this section fully functional? - No

11. MY MENU (USER PANEL)
    [Test] Wallet shows real coin balance - Not started
    [Test] Wallet shows real diamond balance - Not started
    [Test] Recharge screen opens - Not started
    [Test] Backpack (item wallet) works - Not started
    [Test] VIP screen works - Not started
    [Test] Star Path (weekly progression) works - Not started
    [Test] Is this section fully functional? - No

12. GOOGLE PLAY BILLING (MANDATORY)
    [Code] Google Play Billing integrated - Completed
    [Code] Product IDs configured correctly - Completed
    [Test] Purchase flow completes successfully - Not started
    [Code] Backend receipt validation implemented - Completed
    [Test] Coins credited only after validation - In progress
    [Test] Failure handling (cancel / error) works - Not started
    [Test] No fake credits possible - Not started
    [Test] Is this section fully functional? - No

13. AGENCY CENTER
    [Test] Agency overview loads correctly - In progress
    [Test] Manage Agency section works - Not started
    [Test] Members list loads correctly - Not started
    [Test] Revenue calculated correctly - Not started
    [Test] Withdrawals request flow works - Not started
    [Test] Exchange USD -> coins works - Not started
    [Test] Star Path (agency) works - Not started
    [Test] Is this section fully functional? - No

14. ADMIN DASHBOARD (USER MASTER)
    [Test] Banner upload works - Not started
    [Test] Banner enable/disable works - In progress
    [Code] Banner order & duration control - Completed
    [Code] Economy parameters configurable - Completed
    [Test] Audit logs accessible - In progress
    [Test] Is this section fully functional? - No

15. STABILITY & PUBLISHING READINESS
    [Code] Authoritative backend only (no fake data) - Completed
    [Code] All fake / mock data removed - Completed
    [Code] Immutable audit logs - Completed
    [Test] No critical crash flows - Not started
    [Test] Meets Google Play policy - Not started
    [Test] Is the app publish-ready? - No

IMAGE & ASSET CHECKLIST (POTALIVE-STYLE)
Global
App logo (adaptive) - Missing
Splash screen - Missing
Country flag icons (ISO) - Missing
Home / Rooms
Room card thumbnails (640x360) - Missing
Rotating banner images - Missing
Mini-game icons - Missing
Room
Seat icons (empty / occupied / blocked) - Missing
Gift animation assets - Missing
Room banner / background images - Missing
VIP badges - Missing
UI Elements
Buttons (primary / secondary) - Missing
Badges / pills - Missing
Notification icons - Missing
Dashboards
Admin dashboard icons - Missing
Agency center icons - Missing

FINAL STATUS SUMMARY
Fully functional sections: 0
Partially implemented sections: Most core UI
Missing / blocking sections: Economy, Billing, Rewards, Retention, Admin
This checklist represents the current factual parity status versus the reference app.
No interpretation. No promises. Only observable functionality.

KAPPA - COMPLETE IMAGE & ASSET CHECKLIST (POTALIVE PARITY)
Purpose:
This document lists all visual assets required to reach full visual and functional parity with the reference app (Potalive).
- This is NOT a design guide
- This is NOT an art direction document
- This is a production checklist of required assets
All items listed here must exist as real assets, not placeholders.

1. GLOBAL / APP-LEVEL ASSETS
   App logo (adaptive, foreground + background)
   logo.png (1024x1024 px)
   logo_foreground.png (432x432 px)
   logo_background.png (432x432 px)
   Splash screen background
   splash_bg.png (1080x1920 px)
   Loading / placeholder background
   loading_bg.png (1080x1920 px)
   Country flags (ISO full set)
   flag_xx.png (48x48 px)
   Used in: login, filters, room cards, country selectors

2. HEADERS & GLOBAL BACKGROUNDS
   Top header art (primary)
   potalive_top_header.png (632x170 px)
   Used in: Home, Rooms, Inbox, Profile, Admin, Agency, Reseller
   Tab header background
   potalive_header.png (697x161 px)
   Behind tab labels (Rooms / Home)
   Tab header preview (optional)
   potalive_header_preview.png (697x161 px)
   Bottom navigation art band
   potalive_bottom_nav.png (1127x330 px)

3. HOME / MAIN SCREEN ASSETS
   Room card background
   room_card_bg.png (640x360 px)
   Room card thumbnail (dynamic, real images)
   room_thumb_xxx.jpg (640x360 px)
   Room activity badge
   badge_live.png (96x96 px)
   badge_hot.png (96x96 px)
   Search icon
   ic_search.png (96x96 px)
   Notification / bell icon
   ic_notification.png (96x96 px)
   My room shortcut icon
   ic_my_room.png (96x96 px)

4. ROTATING BANNER (HOME)
   Banner image
   banner_xxx.jpg (1080x360 px)
   Banner overlay gradient
   banner_overlay.png (1080x360 px)
   Banner indicator dots
   ic_banner_dot_active.png (24x24 px)
   ic_banner_dot_inactive.png (24x24 px)

5. ROOM - CORE UI ASSETS
   Room background images
   room_bg_xxx.jpg (1080x1920 px)
   Room header ornament
   ic_room_ornament.png (140x140 px)
   Room section icons
   ic_room_audio.png (170x170 px)
   ic_room_seats.png (158x158 px)
   ic_room_chat.png (176x176 px)
   ic_room_gifts.png (158x158 px)
   ic_room_tools.png (192x192 px)
   Seat states
   seat_empty.png (128x128 px)
   seat_occupied.png (128x128 px)
   seat_blocked.png (128x128 px)
   Seat request indicator
   ic_seat_request.png (96x96 px)

6. ROOM CHAT & MESSAGE ASSETS
   Chat bubble (self)
   chat_bubble_self.png (stretchable)
   Chat bubble (other)
   chat_bubble_other.png (stretchable)
   System message bubble
   chat_bubble_system.png (stretchable)
   @mention highlight background
   mention_highlight.png (stretchable)

7. GIFT SYSTEM VISUALS
   Gift icons (static)
   gift_xxx.png (128x128 px)
   Gift categories icons
   gift_cat_fixed.png (128x128 px)
   gift_cat_multiplier.png (128x128 px)
   Gift animation sprites
   gift_burst.png (sprite sheet)
   gift_coin.png (sprite sheet)
   gift_box.png (sprite sheet)
   Multiplier visual effects
   fx_multiplier_2x.png
   fx_multiplier_5x.png
   fx_multiplier_10x.png
   fx_multiplier_100x.png
   fx_multiplier_250x.png
   fx_multiplier_500x.png
   fx_multiplier_1000x.png

8. GLOBAL MULTIPLIER (100x+) OVERLAY
   Overlay background
   global_win_bg.png (1080x360 px)
   Animated highlight frame
   global_win_frame.png (1080x360 px)
   Icon / flare effects
   fx_global_glow.png
   fx_global_particles.png

9. USER PROFILE & VIP ASSETS
   Profile frame (normal)
   profile_frame_default.png (256x256 px)
   Profile frame (VIP)
   profile_frame_vip.png (256x256 px)
   VIP badges / levels
   vip_1.png -> vip_n.png (96x96 px)
   Ranking icons
   rank_gold.png (96x96 px)
   rank_silver.png (96x96 px)
   rank_bronze.png (96x96 px)

10. MY MENU / WALLET / STAR PATH
    Wallet icons
    ic_coins.png (96x96 px)
    ic_diamonds.png (96x96 px)
    Backpack item slot
    item_slot.png (128x128 px)
    Star Path background
    starpath_bg.png (1080x1920 px)
    Star Path milestones
    star_locked.png (96x96 px)
    star_unlocked.png (96x96 px)

11. AGENCY CENTER ASSETS
    Agency header background
    agency_header.png (1080x360 px)
    Agency stat icons
    ic_members.png (96x96 px)
    ic_revenue.png (96x96 px)
    ic_withdraw.png (96x96 px)
    ic_exchange.png (96x96 px)

12. ADMIN DASHBOARD ASSETS
    Admin menu icons
    ic_admin_banner.png (96x96 px)
    ic_admin_users.png (96x96 px)
    ic_admin_agencies.png (96x96 px)
    Toggle / switch graphics
    toggle_on.png
    toggle_off.png

13. COMMON UI ELEMENTS
    Primary button background
    btn_potalive_teal.png (697x161 px)
    Secondary button background
    btn_secondary.png (697x161 px)
    Dialog background
    dialog_bg.png (stretchable)
    Divider / separator
    divider.png (stretchable)

FINAL CHECK
No placeholder images allowed
No duplicated assets with different names
All assets optimized (WebP where possible)
All assets copyright-safe
This checklist represents visual parity completeness with the reference app.

14-DAY EXECUTION PLAN (INTERNAL - BASED ON CURRENT PARITY STATUS)
This plan is derived directly from the Functional Parity Checklist and the assistant's latest implementation report.
- This section is NOT for investors
- This is an internal execution reference
- Scope is strictly limited to achieving parity + publication readiness

DAY 1 - AUTHENTICATION & SESSION STABILITY
- Finalize phone number input validation (E.164)
- Finalize country code selector (full ISO list)
- Implement session persistence (token refresh)
- Handle invalid login / network failure safely
Target status:
- Authentication section -> In progress -> Completed

DAY 2 - REAL BACKEND DATA ENFORCEMENT
- Remove all fake / seeded frontend data
- Force all lists to load from backend APIs
- Add loading + empty states (no placeholders)
Target status:
- Authoritative backend -> Completed

DAY 3 - HOME SCREEN PARITY
- Finalize room list loading
- Finalize room card data binding
- Stabilize search (rooms / users / agencies)
- Ensure room tap opens without crash
Target status:
- Home / Main Screen -> In progress -> Completed

DAY 4 - ROTATING BANNER + ADMIN CONTROL
- Implement banner upload in Admin Dashboard
- Implement enable / disable toggle
- Implement order & duration control
- Bind banner dynamically to Home screen
Target status:
- Rotating Banner -> Completed

DAY 5 - ROOM ENTRY & AUDIO STABILITY
- Stabilize room enter / exit lifecycle
- Finalize join with mic ON / MUTED flow
- Prevent audio crashes on reconnect
Target status:
- Room entry & audio -> In progress -> Completed

DAY 6 - SEATS & SYNCHRONIZATION
- Finalize 28-seat synchronization
- Implement seat request notifications
- Implement admin seat removal
Target status:
- Seats & sync -> In progress -> Completed

DAY 7 - ROOM CHAT COMPLETION
- Implement @mention logic
- Implement automatic translation (send + receive)
- Stress-test chat under load
Target status:
- Room chat -> In progress -> Completed

DAY 8 - GIFT PANEL & TARGETING
- Implement gift categories
- Display seated users in gift panel
- Implement send to self / all / selected
Target status:
- Gift UI -> In progress -> Completed

DAY 9 - MULTIPLIER & ECONOMY LOGIC
- Implement RTP multiplier logic
- Ensure multipliers affect coins only
- Verify diamond generation rules
Target status:
- Gift logic -> In progress -> Completed

DAY 10 - ROCKET (ROOM RETENTION SYSTEM)
- Implement backend room coin counter
- Increment on gifts & minigames
- Trigger reward milestones
- Verify invisibility to users
Target status:
- Rocket system -> Completed

DAY 11 - MY MENU (USER PANEL)
- Wallet (coins & diamonds)
- Recharge screen (UI only)
- Backpack (Item Wallet)
- VIP screen
- Star Path (user)
Target status:
- My Menu -> Not started -> Completed

DAY 12 - GOOGLE PLAY BILLING (CRITICAL)
- Integrate Google Play Billing
- Configure product IDs
- Implement backend receipt validation
- Credit coins only after validation
- Handle cancel / failure cases
Target status:
- Billing -> Completed & Store-safe

DAY 13 - AGENCY CENTER COMPLETION
- Manage Agency section
- Members + revenue
- Withdrawal flow
- Exchange USD -> coins
- Agency Star Path
Target status:
- Agency Center -> In progress -> Completed

DAY 14 - GLOBAL EFFECTS, ADMIN & FINAL QA
- Implement global 100x+ overlay
- Clickable redirect to room
- Admin audit logs access
- Full regression test
- Economy integrity verification
- Crash-free test pass
- Google Play policy checklist
Target status:
- App -> Publish-ready
