# Release Checklist

## Store Assets
- App name, short description, full description prepared.
- Feature graphic (landscape) prepared.
- Screenshots: phone portrait/landscape, tablet if required.
- App icon (512x512), adaptive icon if required.
- Privacy policy URL ready.

## Versioning & Signing
- Update `versionCode`/`versionName`.
- Release keystore created and stored securely.
- Signing configs set for release.
- `minSdk`/`targetSdk` verified with store requirements.

## Build & Verification
- Release build (`assembleRelease`) succeeds.
- Proguard/R8 rules verified; no critical warnings.
- APK/AAB size checked and acceptable.
- Install/upgrade on clean devices (Android 10+).

## Core Flows QA
- Login/Signup/OTP/Guest flows.
- Onboarding country/profile completion.
- Rooms list filter + join/leave.
- Room chat + seats + moderation.
- Gift send + overlay.
- Dashboards: Admin/Agency/Reseller.
- Mini-game join/action/reward.

## Performance & Stability
- Cold start time checked.
- No ANR/crash in key flows.
- Memory usage stable in rooms/gifts.
- Network offline handling verified.

## Compliance & Policies
- Permissions minimized and justified.
- Content moderation guidelines documented.
- Data retention policy documented.

## Release Ops
- Backend endpoints pointed to production.
- Analytics and crash reporting enabled.
- Monitoring dashboards configured.
- Rollout plan (staged rollout %) defined.
