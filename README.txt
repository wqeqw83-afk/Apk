AGENT AI — Android voice launcher project

Features:
- Russian speech input through Android speech recognition (tap microphone)
- Commands for YouTube, HAPP, Chrome, Telegram and Settings
- Dark cyan interface

BUILD APK LOCALLY:
1. Install Android Studio on a computer.
2. Open the AgentAI folder in Android Studio.
3. Allow Gradle sync and install Android SDK Platform 35 if prompted.
4. Build > Build Bundle(s) / APK(s) > Build APK(s).
5. APK appears in app/build/outputs/apk/debug/app-debug.apk

BUILD APK VIA GITHUB ACTIONS (cloud):
1. Create a GitHub repository and upload the contents of this folder to it.
2. Open the repository's Actions tab.
3. Select "Build AgentAI APK" from workflows and click "Run workflow".
4. When the run finishes successfully, open it and download artifact "AgentAI-debug-apk".
5. Extract the ZIP artifact; app-debug.apk is the installable debug APK.

Notes:
- App requests microphone permission.
- Speech recognition availability depends on the device and installed speech service.
- HAPP lookup is based on the app's displayed name or package name containing "happ"; if the HAPP variant uses another label/package identifier, adjust the matcher in MainActivity.kt.
- The GitHub workflow builds a debug APK, not a Play Store release-signed APK.
