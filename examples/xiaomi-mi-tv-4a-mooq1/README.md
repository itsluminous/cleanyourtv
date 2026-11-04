# Xiaomi Mi TV 4A (MiTV-MOOQ1, tarzan): the itsluminous fork's own set

This folder is the setup behind [itsluminous/cleanyourtv](https://github.com/itsluminous/cleanyourtv), a fork
of CleanYourTV: one Mi TV 4A on Android TV 10, SDK 29, firmware `QTG3.200305.006.2591`, 1 GB of RAM. The
list, the launcher theme and the companion app are all in the repository, so the same TV can be set up again
from a clean checkout. The upstream folder for this model is [`examples/xiaomi-mi-tv-4a`](../xiaomi-mi-tv-4a/);
this list was derived from it and from the [MOOQ0](../xiaomi-mitv-mooq0/debloat.txt) list on the same QTG3
firmware line, with one owner's checklist answers applied.

| File | What it is |
|---|---|
| `debloat.txt` | The 27 packages that were disabled, in the five batches they went in, with the state the TV was found in at the top. |
| `config.json` | The launcher theme, a copy of [`themes/mooq1-frosted/config.json`](../../themes/mooq1-frosted/config.json): no dock (`uniform_rows`), six columns in every row, the frosted slate wallpaper, the Trakt carousel on the top shelf. |

```bash
TV_HOST=<tv ip> tools/disable.sh examples/xiaomi-mi-tv-4a-mooq1/debloat.txt      # apply
TV_HOST=<tv ip> tools/restore.sh examples/xiaomi-mi-tv-4a-mooq1/debloat.txt      # undo
```

## What was disabled

Everything with `pm disable-user --user 0`, nothing uninstalled, applied one batch at a time with a screenshot
and a check of Home between batches. It survived a reboot: `pm list packages -d` was identical before and after.

| Batch | Packages | Why |
|---|---|---|
| 1 | `com.xiaomi.statistic`, `com.google.android.feedback`, `com.google.android.tv.bugreportsender`, `com.xm.webcontent`, `com.xiaomi.floatingframe`, `android.autoinstalls.config.xiaomi.amelie` | Xiaomi and Google telemetry, promo web content, the floating promo overlay, the partner auto-install config |
| 2 | `com.xiaomi.mitv.tvmanager`, `com.google.android.tvrecommendations`, `com.google.android.leanbacklauncher.recommendations`, `com.google.android.leanbacklauncher`, `fusion.android.tv.demo`, `com.duokan.factorytest` | TV Manager, the stock launcher's recommendation plumbing, the retail demo loop, and the factory-test app, which claims HOME at priority 0 (disabled so Home can never land on it) |
| 3 | `com.xiaomi.mitv.handbook`, `com.mitv.tvlock`, `com.xiaomo.tv.milegal`, `com.mstar.android.tv.disclaimercustomization`, `com.mediatek.wwtv.webview`, `com.xiaomi.mimusic2`, `com.amazon.music.tv`, `com.sonyliv`, `com.xiaomitv.sonyliv`, `com.hungama.movies.xtv` | Preloads this owner does not use: the handbook, TV lock, legal and disclaimer screens, the vendor webview, Mi Music, Amazon Music, both SonyLIV builds, Hungama |
| 4 | `com.xiaomi.mitv.mediaexplorer`, `com.mitv.videoplayer`, `com.mitv.gallery` | The USB media players; VLC does that job here |
| 5 | `com.mitv.dream`, `com.google.android.backdrop` | Both screensavers, after `screensaver_components` was pointed at the launcher's clock screensaver |

## Kept, and why

| Kept | Why |
|---|---|
| Prime Video, JioHotstar, Xiaomi TV+ (`com.mitv.tvhome.mitvplus`), Play Games, Play Store | By choice. Xiaomi TV+ also hosts a `LiveTVInputService`. |
| Google Assistant (`com.google.android.katniss`), Chromecast (`com.google.android.apps.mediashell`), Mi Cast (`com.mitv.milinkservice`), Miracast (`com.xiaomi.mitv.smartshare`, `com.xiaomi.wfdsinkhelperservice`) | By choice: the voice remote and casting from phones. |
| `com.xiaomi.mitv.updateservice` | Firmware updates stay on, as the repo prefers. |
| `com.mediatek.tvinput`, `com.mediatek.tv.agent`, `com.mediatek.wwtv.tvcenter`, `mitv.service`, `com.xiaomi.mitv.res`, `com.mediatek.hotkey.dispatcher`, `com.mediatek.wwtv.setupwizard`, `com.google.android.tv.remote.service`, Gboard, Play services | The TV needs them: HDMI 1-3, AV, the tuner, the remote's hotkeys, the keyboard, the phone remote. Each was read with `dumpsys package` before it was left alone. |
| `co.sensara.tv.mitv.res` | A resource-only package (no activities, services or permissions): nothing to gain. |
| `mediatek.factorymenu.ui`, `com.mediatek.tv.factory` | Factory menus: never launched, never disabled. |

Found already off before this list ran, so not on it: the stock Google launcher, setupwraith, Netflix, YouTube,
Play Movies, YouTube Music, the Netflix observer and Cricfy were disabled; PatchWall, Mi channels, Mi analytics
and `com.xiaomi.android.tvsetup.partnercustomizer` had been removed for user 0 earlier. That last one holds
`BleRcActivity`, the Bluetooth remote pairing screen: `pm install-existing com.xiaomi.android.tvsetup.partnercustomizer`
brings it back. The saved Wi-Fi network belongs to `android.uid.system`, not setupwraith.

Home was owned by a third-party launcher (LtvLauncher, `com.leanbitlab.ltvL`) before this work. It was not
disabled, only hidden from the grid with `hidden_apps`, so the restore leaves it as it was and Home goes back to it.

## The launcher

The fork's launcher is built from `launcher/` under the package name **`com.itsluminous.tvhome`**:

```bash
LAUNCHER_PACKAGE=com.itsluminous.tvhome TV_HOST=<tv ip> launcher/build-and-install.sh
LAUNCHER_PACKAGE=com.itsluminous.tvhome TV_HOST=<tv ip> tools/theme.sh themes/mooq1-frosted
```

The Java package inside stays `com.gathin.tvhome`, so its components read
`com.itsluminous.tvhome/com.gathin.tvhome.MainActivity` and `.../com.gathin.tvhome.ClockDream`, and the
module-card contract strings are unchanged. Home was set with `cmd package set-home-activity` and came up as
the launcher straight after `FallbackHome` across a reboot. The screensaver is the launcher's own clock:
`settings put secure screensaver_components com.itsluminous.tvhome/com.gathin.tvhome.ClockDream`.

The theme (`config.json` here, pushed from `themes/mooq1-frosted`):

| Key | Value here | Effect |
|---|---|---|
| `uniform_rows`, `columns: 6`, `dock_apps` (six) | SmartTube, Stremio, NetMirror, SportzX, TizenTube, CloudStream | No dock: every row, the first too, holds six tiles of one order, so 7 per row in the panel puts 7 in the top row. `dock_apps` only seeds the head of that order on a TV without a saved order; it also keys the saved order (`order.txt` records the `dock_apps` it was made under), so it stays in the theme. `fill_dock`, `rows_below_dock`, `dock_band` and `dock_glass` do nothing in this mode and are left out. |
| `hidden_apps` | the MediaTek TV centre, LtvLauncher, Mi Wallpaper, Mi Gallery | Off the grid, still installed; the All apps popup opens them. |
| `live_tv_package` | `com.mediatek.wwtv.tvcenter` | The Inputs pill's Live TV row. |
| `library_apps`, `app_library`, `library_button`, `library_ok_opens`, `library_columns: 6` | | The All apps popup as a six-column grid, with one non-TV app (a camera viewer) pre-added. |
| `wallpaper: true`, `background_color: #101828`, `cinematic_home`, `cinematic_art: 35`, `glass_style: glass`, `glow_style: horizon`, `app_glow`, `advanced_effects`, `focus_overshoot: false` | | The frosted slate look: a pre-blurred wallpaper (the launcher never blurs at runtime), tiles floating on it with no band, the horizon glow in the app's colour. |
| `icon_only_buttons`, `icon_only_inputs`, `pill_labels_on_focus`, `show_greeting: false`, `clock_format: system` | | Round icon-only pills that grow to their name while focused; no greeting. |
| `tile_menu`, `launcher_settings_button`, `tv_settings_first`, `settings_panel` | | Hold OK on a tile for Move / Remove from Home / App info; a click on Settings opens the side panel with TV system settings first. |
| `date_two_lines`, `widgets: ["memory", "cpu", "storage"]` | | The date beside the clock on two lines (`WEDNESDAY` over `4 NOVEMBER`), and three ring gauges to its right, the percent inside each and `RAM`, `CPU`, `STORAGE` under them: RAM used of total, the CPU (this TV's Android 10 hides `/proc/stat` from apps and has no cpuidle, so the ring is how far the cores are clocked above idle, 1150 MHz, towards their 1500 MHz top: 0% at rest, 100% at full clock), storage used of total, read every 5 s while Home is on screen. The **Widgets** tick list in the panel turns each gauge off or on. |
| `shelf_every_row`, `hero_feed`, `hero_feed_interval: 8`, `row_gap: 32` | | Every row scrolls up to the same line under the shelf (`uniform_rows` turns this on by itself; the key stays for a launcher without it); the shelf is the Trakt carousel, one title every 8 seconds; rows about 30% closer. No `hero_height`: the first row rests on the shelf's bottom, the layout's own 100% while the carousel is on and 80% once it is turned off in the panel. |

The settings panel also carries **Trakt recommendations** (On, Off: the carousel over the theme's `hero_feed`, so
this theme runs without Trakt too; the picker marks On, the theme's state, until you pick), **Hero banner height**
(Compact 60, Short 80, Normal 100, Tall 120; the picker marks the height in force, Normal (100%) with the carousel and
Short (80%) without it, no "Theme default" entry; the shelf's text shrinks with the height, so Compact keeps its dots
above the first row), **Widgets** (a tick list of Memory, CPU and Storage over the theme's `widgets`; the row names the
gauges that are on) and **Backup & restore** (Back up settings, Restore from file, Import from another launcher). With `uniform_rows` the panel has no **Rows below dock** or **Dock band opacity** row, and
the panel never shows **Follow theme** or **Wallpaper**: a choice saved on the TV is cleared by pushing the theme again
(`tools/theme.sh themes/mooq1-frosted`; every push writes a new `theme_revision`, and `Config.syncThemeChoices` drops
every saved choice when that changes) or by **Restore from file**. See the
fork's [README](../../README.md#backup--restore). The Wi-Fi pill says "Wi-Fi" rather than the network's name
because Location is off on this TV: Android 10 only hands the SSID to apps while Location is on.

## The Trakt companion

[`addons/traktshelf/`](../../addons/traktshelf/) (`com.itsluminous.traktshelf`) feeds the carousel through
`content://com.itsluminous.traktshelf.hero/items` and offers the "Trakt" module card. It is built with the
owner's own Trakt, TMDB and OMDb keys read from a git-ignored secrets file; its README says how to create them.

## After a firmware update

```bash
TV_HOST=<tv ip> tools/venv/bin/python -I tools/check.py examples/xiaomi-mi-tv-4a-mooq1/debloat.txt         # what drifted
TV_HOST=<tv ip> tools/venv/bin/python -I tools/check.py examples/xiaomi-mi-tv-4a-mooq1/debloat.txt --fix   # put it back
```

If the launcher lost Home, run `launcher/build-and-install.sh` again with `LAUNCHER_PACKAGE` set, as above.
