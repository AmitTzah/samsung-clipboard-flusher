# Clipboard Flusher

A deliberately tiny Android app: one home-screen widget, one invisible
activity, zero permissions, no network, no dependencies. Tap the widget and
it overwrites the clipboard 40 times with harmless whitespace so older items
fall out of Samsung's clipboard history.

## Download the APK

**[Download the latest APK (one click)](https://github.com/AmitTzah/samsung-clipboard-flusher/releases/latest/download/clipboard-flusher-release.apk)**
- a signed release build, no compilation needed. You can also browse all
[releases](https://github.com/AmitTzah/samsung-clipboard-flusher/releases).

Alternatively, build it yourself (see "Build from source") and copy the file
from:

```
app\build\outputs\apk\release\clipboard-flusher-release.apk
```

## Install the APK

### Option A: manual install, no computer

1. Get the APK onto the phone:
   - Copy `clipboard-flusher-release.apk` to the phone using a USB cable
     (then My Files > Downloads on the phone), or
   - Download it from the GitHub Releases page in the phone's browser, or
   - Send the file to yourself (email, Drive, messaging app) and open it.
2. Open the file, e.g. My Files > Downloads > `clipboard-flusher-release.apk`.
3. Tap **Install**. Samsung may ask "Do you want to install this app?" -
   confirm.
4. If it asks for permission to install unknown apps: tap **Settings**, allow
   "Install unknown apps" for My Files (or whichever app you opened the file
   with), go back, and tap **Install** again.
5. If Play Protect shows a warning, choose "More details" > "Install anyway".
   The app requests no permissions, so it should pass without issue.

### Option B: from a computer with adb

Enable Developer options > USB debugging on the phone, connect it via USB,
then run:

```
adb install -r clipboard-flusher-release.apk
```

Note: this APK is signed with the project's own release keystore
(`clipboard-flusher.keystore` and `keystore.properties`, both gitignored).
Keep that keystore backed up - updates must be signed with the same key, and
if it is lost you will have to uninstall the app before installing a new
build. The release APK is minified and not debuggable.

## Add and use the widget

1. Long-press an empty spot on the home screen.
2. Tap **Widgets** at the bottom.
3. Find **Clipboard Flusher** (search "flush" if needed).
4. Drag the widget onto the home screen.
5. Tap the widget whenever you want to flush. A very brief transparent flash
   (about 0.5 seconds) means it is working - that is the app writing the 40
   whitespace entries. The widget keeps no state and uses no battery between
   taps.

To verify: copy a test phrase, tap the widget, then open Samsung Keyboard's
clipboard panel (clipboard icon above the keys). The phrase should be gone
and only whitespace entries should remain.

## Does it actually help?

Yes, with caveats. Samsung Keyboard's clipboard history keeps roughly the
last 20-24 copied items. Copying 40 distinct whitespace entries pushes your
older item (a sensitive copy) out of the visible history. Each entry is a
different number of spaces (`" "`, `"  "`, ... 40 spaces) because Samsung may
merge identical consecutive copies into a single history entry.

Important limits:

- **Pinned items stay.** Anything you pinned in Samsung's clipboard panel
  will not be evicted. Unpin or delete it first.
- **It evicts from the visible history, not from Samsung's internal
  storage.** One UI manages clipboard history at the system level and keeps
  it persistently; there are user reports that even Samsung's own "clear"
  button does not fully remove items. This app guarantees the sensitive copy
  no longer appears in the history list and overwrites the current clipboard
  with spaces, but it cannot guarantee a forensic wipe of Samsung's
  clipboard database.
- **It does not delete synced copies.** If "Clipboard history across your
  devices" (Samsung account sync) is on, the sensitive text may already be
  on your other Galaxy devices. Turn sync off and clear any synced copies.
- **It does not scrub deeper storage.** Entries that other apps copied or
  stored (e.g. password managers, cloud sync) are untouched.

**Turning history off entirely is not reliably possible.** Some One UI
versions have a toggle under Settings > General management > Samsung Keyboard
settings > Clipboard ("Save clipboard history" off), but Samsung removed that
option on many recent builds and manages clipboard history at the system
level. What does exist on current Galaxy phones:

- **Clear everything:** open the keyboard's clipboard panel (clipboard icon
  above the keys) > menu > "Delete all".
- **Remove the Clipboard edge panel:** Settings > Display > Edge panels >
  deselect Clipboard.
- **Get alerts when an app reads your clipboard:** Settings > Security and
  privacy > Privacy > "Alert when clipboard accessed".
- **Last resort (advanced, at your own risk):** `adb shell pm disable-user
  --user 0 com.samsung.clipboardsaveservice` - this disables the system
  service that saves history, but it can affect normal copy/paste behavior
  and may not erase entries that are already stored. Back up first.

This widget is for when you want history on (or cannot turn it off) but still
want a one-tap cleanup. For genuinely sensitive data, avoid copying it into
the clipboard in the first place.

## Why a brief invisible activity?

Android 10+ blocks background apps from writing to the clipboard. A widget
tap alone cannot do it, so the tap opens a transparent activity for about
half a second (you may see a faint flash), writes the whitespace entries,
and closes itself. The alternative would be an accessibility service, which
is far more invasive.

## Build from source

Prerequisites: Android SDK, JDK 17 (Android Studio's bundled JBR works),
Gradle 8.12 via the wrapper.

```
.\gradlew.bat assembleRelease
```

The file to install is
`app\build\outputs\apk\release\clipboard-flusher-release.apk` (about 11 KB,
minified with R8, signed by the maintainer).

For development you can also build the debug variant:

```
.\gradlew.bat assembleDebug
```

That produces `app\build\outputs\apk\debug\clipboard-flusher-debug.apk` -
debuggable and signed with the throwaway debug key, so prefer the release
APK for everyday use.

Note: the signing keystore (`clipboard-flusher.keystore` and
`keystore.properties`) is intentionally not committed, so a release build
from a fresh clone is unsigned and cannot be installed. Use the signed APK
from the GitHub Releases page, or provide your own keystore and
`keystore.properties` before building.

## Uninstall

Remove the widget first (long-press it > Remove from Home), then Settings >
Apps > Clipboard Flusher > Uninstall.

## Privacy and permissions

- Requests no permissions (the manifest declares none).
- No network access and no data collection: there is no INTERNET permission
  and no code that uses the network.
- `allowBackup="false"` so the app cannot carry data through device backups.
- The only thing the app ever writes to the clipboard is spaces.

## License

MIT License - see [LICENSE](LICENSE).

## Similar projects

This idea is not new; the general "clean my clipboard from a widget" problem
is already solved by larger open-source apps:

- **ClipboardCleaner** (DeweyReed/ClipboardCleaner): service, widget,
  shortcut, and quick-settings-tile clipboard cleaning, using the same
  transparent-window technique on Android 10+. It clears the current
  clipboard item with a single write (`clearPrimaryClip`), so keyboard-stored
  history (like Samsung's) stays visible afterward - its own description
  says clip history stored by keyboard apps is outside its scope.
- **ClipboardVault** (TylerBuza/ClipboardVault): clipboard history manager
  that can read Samsung Keyboard's (Honeyboard) clipboard database directly.
- **Memory Guardian**: automatically clears the clipboard at set intervals.
- **LTECleanerFOSS**: general-purpose cleaner with clipboard clearing.

What this repo adds is a narrower, minimal take: zero permissions, an about
11 KB APK, and a widget whose only job is flooding Samsung's clipboard
history with whitespace entries to push older items out. If you need history
viewing, scheduling, or filtering, use one of the projects above instead.
