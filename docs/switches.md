# Switches on a TV

Every new key a theme can set (the Keys table in [`themes/README.md`](../themes/README.md)) gets a
screenshot of what it does on a real TV, and this table is where they are listed: the repo and
[cleanyour.tv/addons#switches](https://cleanyour.tv/addons#switches) both show them from here. Most
keys from before this rule still have none; a screenshot for one of them is welcome.

## Adding a key

A new key needs a screenshot. Put it in `docs/switches/<key>.jpg` (an existing screenshot in the
repo that shows the key at work also counts) and add a row below:

- From a real TV, with the key set, as a JPEG or PNG, 16:9 and no wider than 1920 px (1280×720 is
  plenty), under 300 KB.
- Show where the key changes something you can see. For a key that maps a remote button, show the
  screen the button opens; for one that changes behaviour only, show the setting's effect, such as
  the panel, the menu or the app that comes up.
- Blur anything personal: Wi-Fi names, people, addresses.

`tests/test_switches.py` checks that every new key has a row here with an image that exists and
fits the rules. A row may show more than one picture - `vpn_pill` carries the pill up and the pill at
rest - and every picture in a row is held to those rules, not only the last one. Keys from before the
rule sit on its wanted list until a screenshot arrives.

## Screenshots

| Key | Screenshot |
|---|---|
| `columns` | <img src="../themes/six-columns/screenshot.jpg" width="320" alt="columns on a TV"> |
| `glass_style` | <img src="switches/glass_style.jpg" width="320" alt="glass_style halo on a TV"> |
| `sky_intensity` | <img src="sky-sunset.jpg" width="320" alt="sky_intensity on a TV"> |
| `wallpaper_rotation_minutes` | <img src="../themes/scenes/screenshot.jpg" width="320" alt="wallpaper_rotation_minutes on a TV"> |
| `festival` | <img src="../themes/dussehra/screenshot.jpg" width="320" alt="festival on a TV"> |
| `seasonal_skin` | <img src="../themes/halloween/screenshot.jpg" width="320" alt="seasonal_skin on a TV"> |
| `settings_key_panel` | <img src="quick-settings-panel.jpg" width="320" alt="settings_key_panel on a TV"> |
| `home_idle_clock_minutes` | <img src="clock-screensaver.png" width="320" alt="home_idle_clock_minutes on a TV"> |
| `back_opens_clock` | <img src="switches/back_opens_clock.jpg" width="320" alt="back_opens_clock on a TV"> |
| `gamepad_passthrough` | <img src="switches/gamepad_passthrough.jpg" width="320" alt="gamepad_passthrough on a TV"> |
| `consume_volume_keys` | <img src="switches/consume_volume_keys.jpg" width="320" alt="consume_volume_keys on a TV"> |
| `wifi_status_warnings` | <img src="switches/wifi_status_warnings.jpg" width="320" alt="wifi_status_warnings on a TV"> |
| `top_bar_apps` | <img src="switches/top_bar_apps.jpg" width="320" alt="top_bar_apps on a TV"> |
| `show_greeting` | <img src="switches/show_greeting.jpg" width="320" alt="show_greeting on a TV"> |
| `header_white` | <img src="switches/header_white.jpg" width="320" alt="header_white on a TV"> |
| `clock_wallpaper` | <img src="switches/clock_wallpaper.jpg" width="320" alt="clock_wallpaper on a TV"> |
| `clock_wallpaper_dim` | <img src="switches/clock_wallpaper.jpg" width="320" alt="clock_wallpaper_dim on a TV"> |
| `clock_glow` | <img src="switches/clock_glow.jpg" width="320" alt="clock_glow on a TV"> |
| `open_on_start_choice` | <img src="switches/open_on_start_choice.jpg" width="320" alt="open_on_start_choice on a TV"> |
| `open_on_start_network_wait` | <img src="switches/open_on_start_network_wait.jpg" width="320" alt="open_on_start_network_wait on a TV"> |
| `open_on_start_on_wake` | <img src="switches/open_on_start_on_wake.jpg" width="320" alt="open_on_start_on_wake on a TV"> |
| `single_row` | <img src="switches/single_row.jpg" width="320" alt="single_row on a TV"> |
| `dock_band_all` | <img src="switches/single_row.jpg" width="320" alt="dock_band_all on a TV"> |
| `uniform_tiles` | <img src="switches/uniform_tiles.jpg" width="320" alt="uniform_tiles on a TV"> |
| `lunar_date` | <img src="switches/lunar_date.jpg" width="320" alt="lunar_date on a TV"> |
| `dock_band` | <img src="switches/dock_band.jpg" width="320" alt="dock_band on a TV"> |
| `show_library_pill` | <img src="switches/show_library_pill.jpg" width="320" alt="show_library_pill on a TV"> |
| `library_ok_opens` | <img src="switches/library_ok_opens.jpg" width="320" alt="library_ok_opens on a TV"> |
| `library_excluded_apps` | <img src="switches/library_excluded_apps.png" width="320" alt="library_excluded_apps on a TV: a CANDY Android TV DVB2K's App library catalogue, factory-menu packages excluded"> |
| `date_below_clock` | <img src="switches/date_below_clock.jpg" width="320" alt="date_below_clock on a TV"> |
| `locale` | <img src="switches/locale.jpg" width="320" alt="locale set to en on a TV whose system language is Vietnamese"> |
| `power_on_home` | <img src="switches/power_on_home.jpg" width="320" alt="power_on_home on a TV"> |
| `home_long_press_home` | <img src="switches/home_long_press_home.jpg" width="320" alt="home_long_press_home on a TV"> |
| `vpn_pill` | <img src="switches/vpn_pill.jpg" width="320" alt="vpn_pill on a TV"> <img src="switches/vpn_pill_rest.jpg" width="320" alt="vpn_pill at rest: the pill outlined in red, focus on a tile"> |
| `icon_only_vpn` | <img src="switches/icon_only_vpn.jpg" width="320" alt="icon_only_vpn with no VPN up: an open lock on the plain round button, between Wi-Fi and Settings"> <img src="switches/icon_only_vpn_on.jpg" width="320" alt="icon_only_vpn with a VPN up: the closed lock in a green outline, focus on a tile"> |
| `power_on_home_always` | <img src="switches/power_on_home_always.jpg" width="320" alt="power_on_home_always on a TV"> |
| `inputs_connected_only` | <img src="switches/inputs_connected_only.jpg" width="320" alt="inputs_connected_only on a TV"> |
| `inputs_cec_state_only` | <img src="switches/inputs_cec_state_only.jpg" width="320" alt="inputs_cec_state_only: Steam Deck · HDMI 1 Connected, the bare HDMI 2 with no state, on a TV"> |
| `icon_tiles` | <img src="switches/icon_tiles.jpg" width="320" alt="icon_tiles: an app drawn from its icon in the grid on a TV"> |
| `game_day` | <img src="switches/game_day.jpg" width="320" alt="game_day: Minnesota Vikings at Detroit Lions, Kickoff in 5d 13h, with four dots, under the clock on a TV"> |
| `clock_format` | <img src="switches/clock_format.jpg" width="320" alt="clock_format on a TV"> |
| `glow_snap` | <img src="switches/glow_snap.jpg" width="320" alt="glow_snap on a TV: the glow in Netflix red behind the dock"> |
| `library_columns` | <img src="switches/library_columns.jpg" width="320" alt="library_columns: the All apps popup as a six-column grid of compact cards"> |
| `pill_labels_on_focus` | <img src="switches/pill_labels_on_focus.jpg" width="320" alt="pill_labels_on_focus: the focused Library pill grown to show its name"> |
| `tv_settings_first` | <img src="switches/tv_settings_first.jpg" width="320" alt="tv_settings_first: Launcher settings with TV system settings as its first row"> |
| `settings_panel` | <img src="switches/settings_panel.jpg" width="320" alt="settings_panel: Launcher settings as a side panel over Home"> |
| `shelf_every_row` | <img src="switches/shelf_every_row.jpg" width="320" alt="shelf_every_row: the second row scrolled up under the shelf, which names its app"> |
| `uniform_rows` | <img src="switches/uniform_rows.jpg" width="320" alt="uniform_rows at 7 apps per row: seven tiles in the top row too, no dock band, the Trakt carousel above"> |
| `hero_feed` | <img src="switches/hero_feed.jpg" width="320" alt="hero_feed: the Trakt carousel on the top shelf, a title with its synopsis and rating chips over its backdrop"> |
| `hero_feed_interval` | <img src="switches/hero_feed.jpg" width="320" alt="hero_feed_interval: the same carousel, one title every N seconds"> |
| `row_gap` | <img src="../themes/mooq1-frosted/screenshot.jpg" width="320" alt="row_gap 32: the dock and the row under it about 30% closer"> |
| `hero_height` | <img src="switches/hero_height.jpg" width="320" alt="hero_height 60 (Compact): a shorter shelf, the dock lifted and a second row showing under it"> |
