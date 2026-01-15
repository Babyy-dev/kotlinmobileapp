# Kappa Backend

## Local Run
1. Start dependencies:
   - docker-compose up -d
2. Run the server:
   - ./gradlew -p backend run

## Default Users
- master / password123
- reseller / password123
- agency / password123
- user / password123

## Base URL
- http://localhost:8080/api/

LiveKit uses the dev keys from `livekit.yaml` and defaults to `ws://10.0.2.2:7880` for the Android emulator.
