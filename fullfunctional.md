KAPPA – Full Functional Specification (Structured)
This document is a re-structured version of the original functional guide.
• No data has been removed
• No behavior has been altered
• The goal is clarity, hierarchy, and implementation readiness
KAPPA must reach full functional parity with the reference application at launch.
Visual identity is NOT part of the requirement — behavior is.

1. AUTHENTICATION
   1.1 Login Screen
   The login screen contains:
   ・Country code selector
   ・Phone number input field
   ・Password input field
   ・Login button
   Authentication rules:
   ・Login is performed using phone number + password
   ・On successful authentication, the user is redirected to the Main Screen

2. MAIN SCREEN (DEFAULT ENTRY)
   The Popular tab is the initial screen after login.
   2.1 Top Menu Tabs
   2.1.1 “My” Tab
   Displays:
   ・Rooms created by the user
   ・Rooms followed by the user
   2.1.2 “Popular” Tab
   Displays:
   ・All open rooms
   ・Rooms from all agencies

2.2 Rotating Banner (Dashboard Controlled)
Position:
・Displayed below the top menu
Configuration requirements (Admin Dashboard):
・Image upload
・Redirect / deep link configuration
・Enable / disable toggle
・Display order control
・Display duration per banner
Behavior:
・Banner must be dynamic
・Changes must not require app updates

2.3 Popular Mini-Games Section
Position:
・Displayed below the rotating banner
Behavior:
・Displays the 3 most popular mini-games at the moment
・Ordering logic:
・Based on real usage statistics OR
・Admin-defined priority

2.4 Room Filtering System
Position:
・Displayed below mini-games
Available filters:
・Most popular rooms
・Rooms by region (Brazil, USA, Costa Rica, etc.)
・Country selector
・Continent selector
Sorting rules:
・Rooms with higher activity appear first
・Lower activity rooms appear afterward

2.5 Top Right Action Icons
Icons available:
・Direct access to user’s created room
・Notifications (bell icon)
・Search (magnifier icon)
Search Capabilities
Searchable entities:
・Rooms
・Users
・Agencies
・Families (can be ignored in KAPPA if not applicable)
Search parameters:
・Name
・ID

3. BOTTOM NAVIGATION MENU
   Persistent bottom menu contains:
   ・Room → returns to Popular tab
   ・Messages
   ・My

4. MESSAGES MODULE
   4.1 Tabs
   Available tabs:
   ・Messages (private conversations)
   ・Friends
   ・Family (NOT included in KAPPA)
   4.2 Messaging Rules
   Users can exchange messages with:
   ・Users met inside rooms
   ・Users added as friends

5. AUDIO ROOM – CORE FUNCTIONALITY
   The audio room is the central feature of the app.
   5.1 Room Top Area
   Displays:
   ・Agency name
   ・Agency icon
   ・Room ID
   ・Favorite room button
   ・Minimize button
   ・Exit room button

5.2 Seats System
Behavior:
・Users can sit by tapping an empty seat
・If seats are locked:
・User sends a seat request
・Room owner receives a notification

5.3 Room Entry Options
Upon entering a room, user chooses:
・Join with microphone ON
・Join with microphone MUTED
Rules:
・State can be changed at any time inside the room

5.4 Room Chat System
Two chat tabs:
5.4.1 Rewards Tab
・System-generated messages only
・Displays prizes and event notifications
5.4.2 Chat Tab
・Text chat
・Interaction between:
・Seated users
・Observers

5.5 Bottom Action Icons Inside Room
Available actions:
・Text input (keyboard)
・Seat request
・Private messages
・Settings:
・Mute room
・Disable effects / animations
・Mini-games access
・Gift sending

6. GIFT SYSTEM
   6.1 Gift Selection
   When opening gift panel:
   Displays:
   ・List of seated users
   ・Gift categories
   Gift types available in KAPPA:
   ・Multipliable gifts
   ・Fixed gifts

6.2 Sending Gifts
Sending options:
・Send to self (only if seated)
・Send to all seated users
・Send to selected recipients
Quantity rules:
・Quantity selector is enabled
・Rules are predefined by system logic

6.3 Gift Execution Logic
After selecting gift:
・Selection screen closes
・A single Send button appears
・Repeated clicking triggers game rounds
Multipliable gifts:
・Apply RTP-based multiplication rules
Fixed gifts:
・Same sending logic
・Do NOT generate multipliers

7. HIDDEN ROOM COIN COUNTER
   Each room maintains:
   ・A hidden total coins played counter
   Rules:
   ・Not visible to users
   ・Validated by backend
   ・Used to trigger Rocket Event based on predefined rules

8. USER PROFILE (INSIDE ROOM)
   When clicking on a seated user, display:
   ・Username
   ・Agency
   ・VIP status
   ・Ranking
   ・User ID

9. “MY” MENU (USER PANEL)
   Location:
   ・Bottom-right navigation
   Contains:
   ・Wallet (coins and diamonds)
   ・Recharge
   ・Backpack (item wallet)
   ・Settings
   ・VIP
   ・Star Path (weekly progression system)

10. USER TYPES & PERMISSIONS
    10.1 Regular User
    ・Standard room participation
    ・Messaging
    ・Gift sending

10.2 Agency Owner
Special permissions:
・Exclusive agency room
・Full control over room settings
Room management capabilities:
・Change room name
・Change background image
・Lock / unlock seats
・Add administrators

11. AGENCY CENTER (AGENCIES ONLY)
    Available only to agency accounts.
    Displays:
    ・Agency name
    ・Agency ID
    ・Member count
    Management modules:
    ・Anchor management
    ・Join requests
    ・Invite anchor
    ・Rules
    ・Revenue
    ・History
    ・Withdrawals

12. GLOBAL HIGH-MULTIPLIER EFFECT (100X+)
    Trigger condition:
    ・User wins a multiplier ≥ 100X
    Effect behavior:
    ・Special animated visual effect on Main Screen
    Displayed information:
    ・Username
    ・Multiplier achieved
    ・Room where it occurred
    Interaction rules:
    ・Effect is visually impactful
    ・Visible for a few seconds
    ・Clickable
    ・Click redirects user directly to the room
    Purpose:
    ・Increase engagement
    ・Provide social proof
    ・Drive traffic to active rooms

13. FINAL REQUIREMENTS
    ・All functionalities listed above are mandatory
    ・Any missing behavior breaks functional parity
    ・Implementation details are flexible
    ・Functional behavior is NOT negotiable
    KAPPA must replicate all behaviors described above at launch.
