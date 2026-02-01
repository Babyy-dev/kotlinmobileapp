Potalive mini games are real-time interactive games connected to live streams. Viewers don’t download a separate game — they play inside the streaming session using coins, points, or gifts.
The goal is:
Increase viewer engagement
Increase watch time
Increase in-app purchases
Create competition between viewers

Overall Architecture (How It Works Internally)
Here’s the basic structure:
Viewer App
↓
Game UI Layer
↓
Realtime Game Server (WebSocket)
↓
Database + Wallet System
↓
Streamer Session
Everything is synchronized in real-time.

Mini Game Logic Flow (Step By Step)
Let’s break down the logic.

1️⃣ Game Trigger (Start Condition)
Games start in 3 ways:
• Streamer manually starts game
• System auto-launches after timer
• Event-based trigger (gift target reached)
Example:
If viewers >= 50
AND stream duration >= 10 min
→ enable mini game

2️⃣ Player Entry Logic
When viewer taps “Join Game”:
System checks:
IF user_logged_in = true
AND coins >= minimum_entry_fee
AND game_status = OPEN
THEN allow join
Then:
• Coins deducted
• Player added to game room
• Player assigned unique session ID

3️⃣ Real-Time Game Engine (Core Logic)
Potalive uses WebSocket or Firebase Realtime / Socket.IO to sync players.
Every action sends packets like:
UserID
GameID
ActionType
Timestamp
Example:
Click button → send to server
Server validates → broadcast result to all players
This prevents cheating.

4️⃣ Game Type Logic Examples
Here are typical mini-games used:

🎯 Lucky Draw / Spin Wheel
Logic:
Random number generator (server side)
Probability weights assigned
Result calculated
Animation plays locally
Reward credited
Important:
👉 Result is calculated BEFORE animation
👉 Animation is only visual

⚔ Battle / PvP Game
Logic:
Each player has power points
Actions update score
Highest score at end wins
Timer-based:
Game duration = 30 seconds
After timer → calculate winner

🎁 Gift-Based Game
Used a lot in live streaming:
Logic:
User sends gift
Gift value converted to points
Points add to game score
Top contributors win rewards
This is monetization core.

🎮 Reaction / Tap Speed Game
Logic:
Count taps per second
Validate via server
Prevent spam (rate limit)
Rank players

5️⃣ Anti-Cheat Protection
Important part.
Server checks:
• Click speed limit
• Request frequency
• Duplicate packets
• Invalid sessions
Example:
If tap_rate > 15/sec
→ ignore extra inputs

6️⃣ Reward Distribution Logic
After game ends:
System runs:
Sort players by score
Pick winners
Calculate reward pool
Distribute coins/items
Update wallet
Wallet updates are atomic transactions:
No partial updates allowed.

7️⃣ Streamer Integration Logic
Streamer receives:
• Commission from entry fees
• Bonus from gifts
• Increased ranking
Logic:
Total pot = entry fees + gifts
Streamer cut = 20% – 40%
Winner pool = remaining %

Why Potalive Uses Mini Games
Business logic:
Purpose
Benefit
Increase retention
Users stay longer
Boost revenue
Coins & gifts
Community interaction
Competition
Streamer motivation
Higher earnings

Technical Stack Usually Used (Behind Scenes)
Most apps like Potalive use:
Backend:
• Node.js
• Firebase
• Redis
• Socket.IO
Database:
• PostgreSQL / MongoDB
Realtime:
• WebSocket
• Firebase Realtime DB
Payments:
• In-app purchases
• Virtual wallet system

Simple Example Logic (Pseudo Code)
Here is simplified logic:
Start Game
Open Join Window (10 seconds)

For each join:
deduct coins
add player

Start Timer (30 seconds)

While timer running:
receive actions
update scores

End Game

Calculate winner
Distribute rewards
Close room

Important Thing To Understand
These games are:
NOT true "client-side games"
They are server-driven event games
Meaning:
• Server controls results
• App only shows animation
• Logic is centralized
This prevents hacks.
