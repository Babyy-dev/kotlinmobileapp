# Kappa Test Report (Manual)

Environment
- Device: Android Emulator (Pixel 7)
- Build: debug and release APKs
- Backend: local server connected to VPS DB

Test Cases
1) Valid login
- Result: PASS
- Notes: Login with seeded account works.

2) Invalid login
- Result: FAIL (message copy)
- Notes: Previously showed "Session expired"; fix applied to show "Invalid credentials". Retest required.

3) Session persistence after restart
- Result: PASS

4) Offline handling and recovery
- Result: FAIL (message copy)
- Notes: Previously showed "An unexpected error occurred"; fix applied to show "No internet connection". Retest required.

5) Coin balance matches DB
- Result: PASS

6) Role display
- Result: PASS (Admin/Agency/Reseller/User labels)

Artifacts
- Debug APK: app/build/outputs/apk/debug/app-debug.apk
- Release APK: app/build/outputs/apk/release/app-release.apk
