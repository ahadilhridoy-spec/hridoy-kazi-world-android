# HRIDOY KAZI WORLD — Android MVP 0.3.0

This is an Android Studio source project, not a compiled APK.

## Included in this build
- Native Android Java app shell
- Supabase email/password sign-in and sign-up (confirmation settings may require email verification)
- Authenticated reads from the selected `shawon-tech-accounts` Supabase project
- Initial dashboard reads product count, purchase quantities, sales quantities and verified wholesaler-settlement payments
- No demo or fabricated transactions are inserted

## Build
1. Open this folder in Android Studio (Giraffe or newer recommended).
2. Install Android SDK Platform 35 and a compatible JDK 17.
3. Sync Gradle.
4. Select Build > Build Bundle(s) / APK(s) > Build APK(s).
5. Debug APK output: `app/build/outputs/apk/debug/app-debug.apk`.

## Supabase configuration
The project URL and **publishable** client key are configured in `app/build.gradle`. The publishable key is intended for client applications; never place a Supabase `service_role` or secret key in an Android app. Database access is controlled by RLS.

## Important limitations
- No create/edit transaction screens yet.
- The current dashboard stock number is a simple total of all purchase item quantities minus all sales item quantities; it does not yet filter by product or account for returns/adjustments.
- Wholesaler debt balance, customer ledgers, supplier ledgers, account balances, invoice allocations, audit triggers, backup, AI assistant and reports are not yet implemented in the app.
- Payment rows marked `verified` are counted only; this app does not verify payment proof itself.
- The app has not been compiled or installed in this environment. Test with a non-production account and sample data before entering real business records.

## Build APK using GitHub Actions (no PC required)

1. Upload this project folder to a GitHub repository named `hridoy-kazi-world-android`.
2. Keep the repository private.
3. Open the repository's **Actions** tab.
4. Select **Build HRIDOY KAZI WORLD APK** and tap **Run workflow** (or push to `main`).
5. When the run finishes successfully, open the run and download the `HRIDOY-KAZI-WORLD-debug-apk` artifact.

The workflow builds a debug APK. It is not a Play Store release build and has not been run until GitHub Actions executes it.
