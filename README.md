# J.A.R.V.I.S – Offline Voice Assistant for Android

An Android voice assistant that runs **completely offline**, **obeys only your voice**, and keeps **all data on your phone**.

> **Hinglish summary:** Yeh app aapke phone par hi chalta hai. Internet sirf ek baar chahiye, speech models download karne ke liye. Uske baad aawaz pehchanna, aapki voice check karna aur commands chalana, sab kuch phone ke andar hi hota hai. Koi recording save nahi hoti aur koi data bahar nahi jaata.

---

## What it can do

| Say (English) | Say (Hinglish / Hindi) | What happens |
|---|---|---|
| "Jarvis, open WhatsApp" | "Jarvis, YouTube kholo" / "यूट्यूब खोलो" | Opens the app |
| "Jarvis, call Mummy" | "Rahul ko call karo" / "राहुल को कॉल करो" | Calls the contact directly |
| "Jarvis, send a message to Rahul saying I am coming" | "Rahul ko message bhejo ki main aa raha hoon" | Sends an SMS straight away |
| "Jarvis, send WhatsApp to Mummy that dinner is ready" | "Mummy ko whatsapp bhejo ki …" | Opens the WhatsApp chat and presses **Send** |
| "Jarvis, set alarm for seven thirty am" | "Subah saat baje alarm lagao" / "सुबह साढ़े छह बजे अलार्म" | Sets the alarm without opening the clock |
| "Jarvis, set a timer for five minutes" | "Do minute ka timer" | Starts a timer |
| "Jarvis, type hello how are you" | "… likho" | Types into the text box on screen |
| "Jarvis, send" | "Bhej do" | Presses the Send button on screen |
| "go back", "home", "scroll down", "recent apps", "notifications" | "wapas", "neeche" | Navigates the phone |
| "torch on / off" | "torch band karo" | Turns the flashlight on or off |
| "volume up / down" | "volume kam karo" | Changes the volume |
| "what time is it" | "kitne baje hain" | Tells you the time |
| "go to sleep" | "so jao" | Stops listening |

You can also say just **"Jarvis"**, wait for **"Yes?"**, and then give the command within 8 seconds.

---

## How "only my voice" works

1. In the app, **⑤ Record MY voice** asks you to read 5 sentences.
2. The offline Vosk **speaker model** turns your voice into a *voice-print* (128 numbers). Only these numbers are saved on the phone, never any audio.
3. For every command, Jarvis makes a voice-print of what it just heard and compares it with yours (cosine similarity).
   - If the score is above the threshold, the command runs ✓
   - If it isn't, Jarvis says *"Sorry, I only follow my owner's voice"* ✗
4. The log shows `voice=0.xx` for every command. If Jarvis rejects you too often, lower the strictness slider a little. If it accepts other people, raise it.

> ⚠️ Honest note: an offline voice lock is good protection against family, friends, the TV and so on. It is **not** bank-grade security, because a high-quality recording of your voice could fool it. Keep your phone's screen lock on too.

---

## Install (no PC needed)

1. Open this repo on GitHub, go to **Releases**, open **`jarvis-latest`**, and download **`Jarvis-Offline.apk`**.
   (You can also go to Actions, pick the latest run, and download the *Jarvis-Offline-APK* artifact.)
2. Open the APK on your phone and allow "Install unknown apps".
3. Open **Jarvis** and work through the steps from top to bottom:
   - **① Download offline models** (about 50 MB, one time, needs internet)
   - **② Grant permissions** (microphone, contacts, phone, SMS, notifications)
   - **③ Enable Jarvis Accessibility.** This is needed for typing, Send, back/home and opening apps from the background.
     - On **Android 13+**, if the switch is greyed out ("Restricted setting"): open *Settings → Apps → Jarvis → ⋮ (top right) → Allow restricted settings*, then try again.
   - **④ Display over other apps** (optional, helps on some phones such as Xiaomi, Oppo and Vivo)
   - **⑤ Record MY voice**
4. Tap **▶ Start Jarvis**. A notification shows while it is listening. Now say *"Jarvis, open YouTube"*.

### Tips
- **Battery:** On Xiaomi, Oppo, Vivo, Realme or Samsung phones, set Jarvis's battery setting to *No restrictions / Don't optimise*, or the phone may stop the service.
- **Wake word not detected?** Look at the log to see what Jarvis heard instead of "Jarvis" (for example "service"), and add that word to the **Wake words** setting.
- **Hindi model:** Choose *Hindi* in setup if you want to speak in Hindi. Contact and app names are matched by sound, so "राहुल" finds the contact "Rahul".
- **WhatsApp numbers** without a country code get **+91** added. You can change this in Settings.
- **Test without voice:** The *Run test command* box runs a command by typing. It skips the voice check and only works inside the app.

---

## Privacy

- Speech recognition runs on the phone ([Vosk](https://alphacephei.com/vosk/), Kaldi-based).
- The voice-print, settings and log are stored in the app's private storage. Backup is disabled (`allowBackup=false`).
- The only network use is downloading the models from alphacephei.com. Nothing is uploaded, ever.
- The log is kept only in memory and is cleared when the app closes.

---

## Build it yourself

Every push builds the APK in GitHub Actions (`.github/workflows/build-apk.yml`). To build locally:

```bash
gradle assembleDebug      # needs Android SDK + JDK 17, Gradle 8.9
gradle testDebugUnitTest  # command-parser unit tests
```

### Code map
| File | Purpose |
|---|---|
| `JarvisService.kt` | Foreground service: mic → Vosk → voice check → parser → executor, plus spoken replies |
| `VoiceProfile.kt` | Owner voice-print (x-vector centroid) and cosine scoring |
| `EnrollActivity.kt` | Records the owner's voice |
| `CommandParser.kt` | English, Hinglish and Hindi command understanding (unit-tested) |
| `CommandExecutor.kt` | Opens apps, calls, SMS, WhatsApp, alarms, timers, torch, volume |
| `JarvisAccessibilityService.kt` | Typing, Send button, back/home/scroll |
| `Finders.kt`, `TextUtil.kt`, `Translit.kt` | Sound-alike matching of contacts and apps (Devanagari ↔ Latin) |
| `ModelManager.kt`, `VoskEngine.kt` | One-time model download and loading |

Note: `app/debug.keystore` is a public debug key, kept so each CI build installs over the last one. Use your own key for anything you publish.
