# 🙏 Gita Darshan — Build & Install Guide
### For Redmi Note 10 (mojito) | Project Infinity X | Android 16 | KernelSU

---

## What This App Does
- Runs a background service that detects when you press power to turn screen off
- Uses KernelSU root to briefly wake the AMOLED display
- Shows a beautiful saffron/gold glowing verse card (pure black background — AMOLED-friendly)
- Displays one random verse from all 18 chapters (135 verses) in English + Bengali
- Dismisses automatically after your chosen time (10–60 seconds) or on tap
- Auto-starts on every boot

---

## STEP 1 — Install Android Studio (one-time setup)
You need Android Studio to build the APK. It's free.

1. Download from: https://developer.android.com/studio
2. Install it on your laptop
3. On first launch, let it download the SDK (takes ~10 min)

> **Alternative without Android Studio:** Ask someone to build it for you or use a cloud builder like GitHub Actions — see "GitHub Actions Build" section at the bottom.

---

## STEP 2 — Open the Project
1. Extract `GitaDarshan.zip` to a folder on your laptop
2. Open Android Studio → `File → Open` → select the `GitaDarshan` folder
3. Wait for Gradle sync to finish (2–5 min first time)

---

## STEP 3 — Build the APK
1. In Android Studio, click: `Build → Build Bundle(s) / APK(s) → Build APK(s)`
2. Wait for build to complete
3. Click **"locate"** in the popup, OR find it at:
   ```
   GitaDarshan/app/build/outputs/apk/debug/app-debug.apk
   ```

---

## STEP 4 — Install via ADB (using your platform-tools)
Since you already have platform-tools, this is easy!

### Connect your phone:
```bash
# On your laptop, open a terminal/command prompt in your platform-tools folder

# Enable USB Debugging on phone:
# Settings → About Phone → tap "Build Number" 7 times
# Settings → Developer Options → Enable USB Debugging

# Connect phone via USB, then:
adb devices
# Should show your device
```

### Install the APK:
```bash
adb install -r path\to\app-debug.apk
# Example (Windows):
adb install -r C:\Users\YourName\GitaDarshan\app\build\outputs\apk\debug\app-debug.apk
```

---

## STEP 5 — First Launch Setup (on your phone)

1. Open **Gita Darshan** from app drawer
2. Grant **"Display over other apps"** permission when asked
3. Grant **Notification** permission
4. **IMPORTANT — Grant Root in KernelSU:**
   - Open KernelSU app
   - Go to **Superuser** tab
   - Find "Gita Darshan" and tap it → set to **Allow**
5. Toggle the switch **ON** in the app
6. Tap **Settings** to adjust display duration

---

## STEP 6 — Test It
1. Make sure the toggle is ON
2. Press your power button to turn screen off
3. Wait ~0.5 seconds
4. Screen should wake up and show a Gita verse!
5. Tap anywhere or wait for timer to dismiss

---

## Troubleshooting

| Problem | Fix |
|---------|-----|
| Screen doesn't wake | Check KernelSU → Superuser → allow Gita Darshan |
| App crashes on launch | Grant overlay permission in Settings → Apps → Gita Darshan → Permissions |
| Verse doesn't show after boot | Go to Settings → Apps → Gita Darshan → Battery → set to "Unrestricted" |
| Notification shows but no verse | Disable battery optimization for the app |
| Build fails in Android Studio | Make sure Android SDK 35 is installed: SDK Manager → SDK Platforms → API 35 |

---

## Optional — Disable Battery Optimization (Recommended)
For reliable boot-start and screen-off detection:

```
Settings → Battery → Battery Optimization
→ All apps → Gita Darshan → Don't optimize
```

Also in **Project Infinity X ROM:**
```
Settings → Developer Options → Aggressive Battery (turn OFF for this app)
```

---

## Build Without Android Studio (GitHub Actions)

If you don't want to install Android Studio:

1. Create a free GitHub account
2. Upload the `GitaDarshan` folder as a new repository
3. Create `.github/workflows/build.yml`:

```yaml
name: Build APK
on: [push]
jobs:
  build:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      - uses: actions/setup-java@v3
        with:
          java-version: '17'
          distribution: 'temurin'
      - name: Build APK
        run: |
          chmod +x gradlew
          ./gradlew assembleDebug
      - uses: actions/upload-artifact@v3
        with:
          name: GitaDarshan-APK
          path: app/build/outputs/apk/debug/app-debug.apk
```

4. Push to GitHub → go to Actions tab → download the APK artifact

---

## File Structure
```
GitaDarshan/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/com/gitadarshan/app/
│   │   │   ├── MainActivity.kt          ← Main screen with toggle
│   │   │   ├── SettingsActivity.kt      ← Timer settings
│   │   │   ├── VerseActivity.kt         ← The verse display screen
│   │   │   ├── VerseOverlayService.kt   ← Background service (heart of the app)
│   │   │   ├── BootReceiver.kt          ← Auto-start on boot
│   │   │   └── data/
│   │   │       ├── GitaVerse.kt         ← Data models
│   │   │       └── GitaRepository.kt    ← Loads verses from JSON
│   │   └── res/
│   │       ├── layout/                  ← UI layouts
│   │       ├── raw/gita_verses.json     ← All 135 verses (18 chapters)
│   │       ├── drawable/                ← Saffron glow card backgrounds
│   │       └── values/                  ← Colors, themes, strings
│   └── build.gradle.kts
├── build.gradle.kts
└── settings.gradle.kts
```

---

## Verses Included
- **18 Chapters** | **135 key verses**
- Every major verse is included (2.47, 18.66, 9.26, 4.7, 4.8, and many more)
- Both **English** (simple modern translation) and **Bengali** for every verse
- Verses are picked randomly so you'll rarely see the same one twice in a row

---

*ॐ  Made with devotion for Redmi Note 10 (mojito) | Project Infinity X | KernelSU*
