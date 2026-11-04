# The launcher

[Back to the README](../README.md#the-launcher)

A small native home screen for Android TV: **about 572 KB** upstream, about 615 KB with this fork's switches,
plain Java, built for TVs with 1 GB of RAM. No blur, no video, no network permission, no background services.
This page has the details the README leaves out: settings, languages, remote keys and building it yourself.
This fork installs it as `com.itsluminous.tvhome` (see [Build it yourself](#build-it-yourself)); the fork's own
switches and the Backup & restore section are summarised in the [README](../README.md#switches-added-in-this-fork). Every key a theme can
set is in [`themes/README.md`](../themes/README.md#keys), and the opt-in switches with their
screenshots are in [`docs/switches.md`](switches.md).

## Which TVs

The **TV** needs Android 8.0 (API 26) or newer: `minSdk` is a compile-time floor in
[`launcher/app/build.gradle`](../launcher/app/build.gradle), and the install script asks the TV for
`ro.build.version.sdk` before it builds, so an older set is told both numbers instead of failing at
`pm install` (see [Troubleshooting](TROUBLESHOOTING.md) for what is left on such a set).

## Build it yourself

It needs JDK 17 and Android SDK platform 34. On macOS: `brew install openjdk@17 android-commandlinetools`
and `sdkmanager "platforms;android-34" "build-tools;34.0.0"`. On Linux the install script finds
`~/Android/Sdk` and the `java` on your PATH.

Another package name: `LAUNCHER_PACKAGE=<id>` builds and installs the launcher under that `applicationId`
(`launcher/build-and-install.sh`, `tools/theme.sh`, `tools/theme.ps1` and `tools/theme_restart.py` all read it;
`launcher/build-and-install.ps1` still hard-codes `com.gathin.tvhome`), alongside a `com.gathin.tvhome` already
on the TV. This fork's TV runs `LAUNCHER_PACKAGE=com.itsluminous.tvhome`. The Java package stays `com.gathin.tvhome`, so components read
`<id>/.MainActivity` and `<id>/com.gathin.tvhome.ClockDream`; the module-card contract strings do not change.
Launcher settings → Backup & restore → Import from another launcher copies the old package's settings, app
order, hidden apps, dock, card approvals and theme into the new one.

## Configure

[`config.xml`](../launcher/app/src/main/res/values/config.xml) sets:

- the dock order, hidden apps and the Live TV package;
- columns (tiles shrink from 5 up) and rows under the dock;
- an optional VPN app for the network pill;
- `reduced_motion`, for TVs whose GPU cannot keep up.

Themes set the same keys plus a background colour and a wallpaper.

## Launcher settings and the default home

Click **Settings** for the TV's own settings. Hold OK on it for its menu:

- **Launcher settings**. With `launcher_settings_button` a click opens those instead, and the menu
  leaves the row out.
- **Native settings**, where the TV ships a settings app of its own beside Android's - Xiaomi China,
  TCL/FFALCON and Fire OS are in `settings_fallback_components` - because that app holds pages
  Android's has no row for, such as Xiaomi's Account & Security with the ADB switch.
- **Module cards**, with `module_cards`.
- **Edit dock**, with `dock_row` (left out, with **Add to dock**, while `shelf_every_row` is on, which
  `uniform_rows` turns on by itself: every row then behaves like the dock).
- **Reorder apps**: OK picks an app up, the arrows move it, Back places it, and Back again finishes.
  With `tile_menu` (on by default since 3.3), holding OK on a tile offers **Move**, which picks that app up straight away. Leaving it
  no longer recreates Home.
- **Restart…** while `HomeKeyService` runs, which opens the system power menu (a launcher may not
  restart the TV itself).

The launcher's own page has these rows, top to bottom:

- **Reorder apps**, only with `launcher_settings_button`, the same as the menu's.
- **Apps per row** 4, 5, 6 or 7 (with `uniform_rows` the first row too takes that many); **Rows below dock** 0 to 3,
  how many rows of tiles stay visible under the dock band; **Dock band opacity** 100%, 70%, 50% or 30%. The last two
  only do anything while the dock is its own row (`dock_row`, on by default), and both are left out, on the page and
  in the panel, while `uniform_rows` is on, since there is no dock then. In the side panel (`settings_panel`) with
  `shelf_every_row` on, where no row is the dock, the rows row reads **Rows shown below the first row**, with the line
  "How many rows sit under the first row at rest; more rows scroll up on Down"; the key and values are the same.
- **Clock**: **System** follows the TV's 12- or 24-hour setting; **12-hour** or **24-hour** sets the launcher's
  clocks (Home and the clock screensaver) to that, whatever the TV uses (`clock_format` in a theme).
- **Wallpaper**: one picture from the theme's collection (or its `wallpaper.jpg`), **None**, or **Theme
  default**. A pick holds that picture and stops the rotation until you choose Theme default, press
  Follow theme, or push the theme again. The side panel has no Wallpaper row (a TV has no file picker; a
  theme push or a restore sets the wallpaper).
- **Animations** on or off: the one switch for the motion. Off is still, with no tile zoom, lift or glow; On runs
  the basic motion and, when the theme sets `advanced_effects` (tile lift, icon zoom and smooth ambient-colour
  transitions; they may use more memory), those too. There is no Advanced effects row: the effects follow the theme
  key under this switch, and an `advanced_effects` choice an older launcher saved is dropped on load and on restore.
- **Open on start**, only with `open_on_start_choice`: Off, or an app that opens instead of Home the first
  time Home comes up after the TV starts. Once per boot, so Home and Back still reach the launcher.
- **Hero banner height**: Compact (60%), Short (80%), Normal (100%) or Tall (120%), the top shelf's height;
  a shorter shelf lifts the dock and shows more rows, and the shelf's text and spacing shrink with it so the
  dots stay clear of the first row (`hero_height` in a theme). The row and its picker show the
  height in force: a saved choice, else the theme's `hero_height`, else with the default `0` (auto) Normal (100%)
  while the Trakt carousel is on and Short (80%) without it. A theme height outside the four joins the picker as
  its own row ("70%"). There is no Theme default entry; **Follow theme** (the full-screen page) or a theme push
  hands the choice back to the theme.
- **Widgets** (side panel): a tick list of **Memory**, **CPU** and **Storage**, the ring gauges beside the date
  (`widgets` in a theme: a percent ring, the percent inside, a caps label under it); each tick is saved as it is made,
  the row names the gauges that are on ("Memory, CPU, Storage", or None), and the page shows the set in force, the
  theme's until you tick one. An empty tick list is a saved none, so a theme's gauges can all be turned off; a theme
  push hands the choice back to the theme.
- **Trakt recommendations**: On or Off, the Trakt shelf companion's carousel on the top shelf (`hero_feed` in a
  theme); the row and its picker show the state in force, the theme's until you pick one. Off leaves the launcher exactly as with `hero_feed: false`: the shelf names the
  focused app and the companion is never read. The row is always there, and says when the Trakt shelf app is
  not installed.
- **Backup & restore**: **Back up settings** writes every preference, the app order, hidden apps, the dock,
  module-card approvals and the theme to one JSON file under `/sdcard/Android/data/<package>/files/backup/`;
  **Restore from file** lists that folder and replaces everything from the file you pick; **Import from another
  launcher** copies the settings of the same launcher installed under another package name. The read-only
  provider `content://<package>.backup/settings` exports the same JSON over ADB.
- **Follow theme (reset on-TV choices)**, which clears every choice above. Pushing or resetting a theme
  clears them too (every push writes a new `theme_revision`, and the launcher drops its saved choices when that
  changes), and so does **Restore from file**; the side panel (`settings_panel`) has no Follow theme row and relies
  on those two.
- **Choose default home** opens Android's home picker, and **TV system settings** the TV's own settings
  page.

**Change the default home:** Launcher settings and the optional Default-home tile
(`show_home_settings_shortcut`) open Android's home picker. Choose the original launcher to stop using
CleanYourTV as Home, or CleanYourTV to return. The original launcher must be enabled; if the install
script disabled it, run the [restore script](../README.md#restoring-your-tv) first. Some TVs provide no
home picker.

## Languages

English, German, Danish, Spanish, Latin American Spanish, Catalan, French, Russian, Serbian (Latin), Polish, Czech, Italian, Portuguese,
Turkish, Arabic, Hebrew, Simplified Chinese, Traditional Chinese (Taiwan), Thai, Finnish, Vietnamese,
Bangla, Indonesian, Norwegian Bokmål, Swedish, Dutch, Uzbek (Latin), Ukrainian, Korean and Asturian. The launcher follows the TV's language. Android 13+ can set it for the launcher
alone: `tools/tv.py 'cmd locale set-app-locales com.gathin.tvhome --locales ca'`.

Hebrew mirrors the layout:

- menu buttons on the left, date and clock on the right with the greeting above;
- dock and app rows starting on the right, with matching remote navigation;
- the input selector's icons and labels on the right;
- dates with the month prefix ב (for example, 19 באוקטובר).

| ![Hebrew Home: the buttons on the left, the greeting, clock and date on the right](hebrew-home.png) | ![Dock and grid: both rows start on the right](hebrew-apps.png) |
|---|---|
| ![The Inputs menu: source names and icons on the right, what is not connected on the left](hebrew-inputs.png) | ![The Clock screensaver, the date under the time, right of centre](hebrew-screensaver.png) |

The Clock screensaver right-aligns its date and time. Russian covers every screen, including race
weekends, festival greetings and the Shamsi date. Other languages keep their original layouts.

## Module cards

Any TV app can declare its own tile (label, caption, glow). The launcher asks you once per app, keeps it
hidden by default, and remembers the answer for that app's signing key and card name. Hold OK on Settings
and choose Module cards to change your mind. How to make one: [`docs/modules.md`](modules.md); the
contract: [`docs/specs/card.md`](specs/card.md). Ready-made ones are in [`addons/`](../addons/), and
[cleanyour.tv/addons](https://cleanyour.tv/addons) shows the themes, switches and add-ons together.

## Clock screensaver

Pick "Clock" in the TV's screensaver settings, or over ADB:

```bash
tools/tv.py 'settings put secure screensaver_components com.gathin.tvhome/.ClockDream; settings put secure screensaver_enabled 1'
```

## Remote keys

- An optional accessibility service keeps the remote's **Input** button working on Google TV
  (`INPUT_KEY_SERVICE=1` at install).
- It can bring back other buttons only the stock launcher answered, such as the **YouTube** button
  (`app_keys` and `input_key_keycodes`, in config.xml or a theme; `HOME_KEY_SERVICE=1` reads `app_keys` too).
- A `hold:` prefix on an `app_keys` entry gives that button a second app: a normal press opens the
  unprefixed app, holding for half a second opens the hold one (by keycode or `hold:scan:...`). An entry can
  name an activity (`package/.Activity`), such as an add-on's quick bar, instead of the app's launcher entry.
  A `front:` prefix sets what a tap opens while that button's own app is already in front (`HomeKeyService`
  only).
- In place of an app, an `app_keys` entry can name an action: `back`, `home`, `recents`, `mute`, `sleep`
  (standby, Android 9 and later), `screensaver`, `settings`, `wifi`, `library` (Home with the App library
  open, for a remote's Apps button; needs `app_library`) or `ignore` (the button does nothing), so a spare
  colour button can mute the TV or, held, put it in standby.
- `HOME_KEY_SERVICE=1` also fixes sticks whose Home key sends ALL_APPS, and opens Settings from the remote's
  **gear** button, even one Android does not map (scancodes or keycodes in config.xml).
- With `settings_key_panel` the gear opens a [small quick settings panel](quick-settings-panel.jpg)
  over Home or any app: TV settings, Display, Network, Apps and Launcher settings.

The full `app_keys` format is in the [`themes/README.md` keys table](../themes/README.md#keys).

## Navigation and artwork

Up and Down move one row. The top buttons never fall through to tiles, and Down from them returns to the
dock tile you came from. The last focused app is remembered, and installing or removing an app refreshes
the tiles while Home is open. App banners load at xhdpi or better, so low-density TV UIs do not downsample
them before the focus zoom.
