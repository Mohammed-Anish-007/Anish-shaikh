# Jarvis security report

This document lists every security protection in Jarvis and how each one is **tested automatically on every build**.

## How it is tested

| Layer | What runs | Where |
|---|---|---|
| Unit security tests | `app/src/test/.../SecurityTest.kt`: voice lock, brute-force guard, zip-slip/zip-bomb, hostile inputs | GitHub Actions, every push |
| Command-parser tests | `CommandParserTest.kt`: 100+ commands, plus checks that background chatter never becomes a call, message or toggle | GitHub Actions, every push |
| Supply-chain check | `scripts/fetch_models.sh` verifies each bundled model against the SHA-256 pinned in `models.sha256` | GitHub Actions, the build fails on mismatch |
| APK audit | `scripts/security_audit.py` inspects the **final built APK**: debuggable flag, permissions, backups, exported components, signature | GitHub Actions; the result is published as `security-audit.txt` next to the APK |

## Protections

### 1. Data never leaves the phone
- **No INTERNET permission.** It is force-removed from the final manifest (`tools:node="remove"`), so even a library cannot add it. Android itself blocks every network socket for the app. *Tested by the APK audit.*
- Speech models are **bundled inside the APK**. Nothing is downloaded at runtime.
- No analytics, ads, crash-reporting or tracking SDKs. The only dependencies are `androidx.core`, `vosk-android` and `jna`.
- **Backups are disabled**: `allowBackup=false`, plus `dataExtractionRules` and `fullBackupContent` that exclude everything. Jarvis data is therefore never copied to Google Drive or to a new phone. *Tested by the APK audit.*

### 2. Voice lock (only the owner)
- Every command is checked against the owner's voice-print (Vosk x-vector, cosine similarity) in a single function: `VoiceMath.isOwner`.
- The following are always rejected: audio shorter than about 0.4 s, missing or malformed vectors, and NaN or infinity values. *Tested.*
- The suggested threshold never drops below 0.35, even with poor enrollment. *Tested.*
- **Brute-force lock-out**: 3 wrong voices within 60 s close the voice lock for 2 minutes. Background TV or chatter does not count towards it; only attempts that address Jarvis do. *Tested.*

### 3. Stored data is encrypted
- The voice-print is encrypted with **AES-256-GCM**, using a key generated inside the **Android Keystore** (hardware-backed where available). The key cannot be exported, so a copied file is useless. An old plain-text profile is migrated automatically.
- No audio is ever written to disk. The log, notifications and contacts are held in memory only.

### 4. App hardening
- The **release build is not debuggable**, so nobody can attach a debugger or read private files over USB (`run-as`). *Tested by the APK audit.*
- **Exported components**: the accessibility service and notification listener can only be bound by the Android system (`BIND_*` permissions). The boot receiver only accepts protected system broadcasts. The launcher activity takes no input. Everything else is not exported. *Tested by the APK audit.*
- All `PendingIntent`s are `FLAG_IMMUTABLE`.
- `FLAG_SECURE` on all Jarvis screens: no screenshots, no screen recording, and no preview in recent apps.
- **Phone-lock confirmation** (PIN, pattern or fingerprint) is required before opening Settings, re-recording the voice, or running typed commands. Typed commands skip the voice lock, so they need the owner's phone lock instead.
- Model archives are unpacked with **zip-slip and zip-bomb protection**. *Tested.*
- Cleartext network traffic is disabled, and `targetSdk 34` applies modern Android rules.

## Known limits (honest)
- **Voice imitation and recordings**: an offline voice lock is not bank-grade. A high-quality recording of the owner's voice could pass it. Keep a screen lock on the phone.
- **Signing key**: until the `JARVIS_KEYSTORE_*` secrets are added to GitHub, releases are signed with the public key in this repo. Because the repo is public, someone could build a fake "update". **Only install Jarvis from this repo's Releases page.** The audit shows a ⚠️ warning until a private key is configured (see the README).
- **Root / custom ROMs**: on a rooted phone, other root apps can read anything. The Keystore encryption still protects the voice-print key on most devices.
