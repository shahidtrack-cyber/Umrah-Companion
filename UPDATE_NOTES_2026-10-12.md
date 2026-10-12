# Umrah Companion — integrated update

## Updated
- Fixed JavaScript/Android reminder bridge mismatch (`AndroidReminder`) and numeric alarm IDs.
- Added native Android location bridge for file-based WebView (GPS permission required).
- Connected automatic prayer times to GPS coordinates, with manual-city fallback.
- Added settings page to change dashboard language preference, prayer method, location mode and notifications.
- Fixed prayer countdown to use the destination city's timezone and refresh when that city's date changes.
- Added optional daily prayer alerts scheduled for the current day (requires Android notification permission); user-created reminders remain supported.
- Added Qibla bearing in degrees from current GPS position; **not** a live sensor compass.
- Transport current location and family location sharing now use the native location bridge where available.
- Preserved localStorage keys, onboarding, original splash, fonts, other modules and GitHub build workflow.

## Known limits / needs device testing
- Prayer times require internet. GPS requires device Location permission. Verify with local mosque.
- Prayer notifications currently schedule for the fetched day only; alarms are not automatically restored after reboot, and exact delivery depends on Android battery/alarm restrictions.
- Full bilingual translation is not implemented (selected dashboard labels only).
- Live sensor Qibla compass, offline routable maps, community backend, family live tracking and direct ride bookings require further work/services.
- APK was **not** compiled here; run GitHub Actions build and test on an Android device.
