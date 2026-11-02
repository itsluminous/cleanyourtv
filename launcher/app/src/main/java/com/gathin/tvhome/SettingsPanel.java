package com.gathin.tvhome;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * settings_panel: Launcher settings as a side panel in the style of Android TV's own Settings, a dark sheet on the
 * right over Home, with a title, a section caption, and rows of icon, title and the current value. A row with
 * several values opens a second page in the same panel (a list with the current one marked); Back returns.
 * Everything is drawn in code so the panel costs no layout resource.
 */
final class SettingsPanel {
    private static final int PANEL_BG = 0xFF1B1E22, ROW_FOCUS = 0xFF2F3338, CAPTION = 0xFF4C8DF5, SUBTITLE = 0xFFA7ADB4;
    private static final int[] DOCK_OPACITIES = {100, 70, 50, 30};
    private static final String[] CLOCK_FORMATS = {"system", "12", "24"};

    private final Activity activity;
    private final Config config;
    private final SharedPreferences choices;
    private final Runnable changed;
    private final float density;
    private final LinearLayout list;
    private final TextView title;
    private final TextView caption;
    private final ScrollView scroller;
    private Runnable backHandler;

    SettingsPanel(Activity activity, Config config, SharedPreferences choices, Runnable changed) {
        this.activity = activity;
        this.config = config;
        this.choices = choices;
        this.changed = changed;
        density = activity.getResources().getDisplayMetrics().density;

        FrameLayout root = new FrameLayout(activity);
        LinearLayout panel = new LinearLayout(activity);
        panel.setOrientation(LinearLayout.VERTICAL);
        panel.setBackgroundColor(PANEL_BG);
        panel.setClickable(true);
        boolean rtl = activity.getResources().getBoolean(R.bool.rtl_menu_bar);
        root.addView(panel, new FrameLayout.LayoutParams(dp(360), -1, rtl ? Gravity.START : Gravity.END));

        title = text(activity.getString(R.string.launcher_settings), 26, Color.WHITE, true);
        title.setPadding(dp(40), dp(36), dp(40), dp(22));
        panel.addView(title, new LinearLayout.LayoutParams(-1, -2));
        View divider = new View(activity);
        divider.setBackgroundColor(0xFF2A2E33);
        panel.addView(divider, new LinearLayout.LayoutParams(-1, dp(1)));

        scroller = new ScrollView(activity);
        scroller.setVerticalScrollBarEnabled(false);
        scroller.setOverScrollMode(View.OVER_SCROLL_NEVER);
        list = new LinearLayout(activity);
        list.setOrientation(LinearLayout.VERTICAL);
        list.setPadding(0, dp(8), 0, dp(24));
        scroller.addView(list, new FrameLayout.LayoutParams(-1, -2));
        panel.addView(scroller, new LinearLayout.LayoutParams(-1, 0, 1));
        caption = text("", 14, CAPTION, true);
        caption.setPadding(dp(40), dp(14), dp(40), dp(10));

        activity.setContentView(root);
        showMain();
    }

    /** True when the panel was on a sub-page and went back to the main one; false lets the activity finish. */
    boolean onBack() {
        if (backHandler == null) return false;
        Runnable back = backHandler;
        backHandler = null;
        back.run();
        return true;
    }

    // ---- pages -------------------------------------------------------------------------------------

    private void showMain() {
        list.removeAllViews();
        title.setText(R.string.launcher_settings);
        caption.setText(R.string.settings);
        list.addView(caption, new LinearLayout.LayoutParams(-1, -2));
        View first = row(R.drawable.ic_settings, activity.getString(R.string.tv_settings), null,
                v -> open(SystemSettings.intent(activity, android.provider.Settings.ACTION_SETTINGS)));
        row(R.drawable.ic_swap_vert, activity.getString(R.string.reorder), null, v -> {
            activity.setResult(Activity.RESULT_OK, new Intent().putExtra(LauncherSettingsActivity.EXTRA_REORDER, true));
            activity.finish();
        });

        TextView layoutCaption = text(activity.getString(R.string.launcher_settings), 14, CAPTION, true);
        layoutCaption.setPadding(dp(40), dp(22), dp(40), dp(10));
        list.addView(layoutCaption, new LinearLayout.LayoutParams(-1, -2));

        int columns = choices.getInt("columns", config.columns);
        row(R.drawable.ic_apps, activity.getString(R.string.columns_label), String.valueOf(columns), v -> {
            String[] labels = {"4", "5", "6", "7"};
            choose(activity.getString(R.string.columns_label), labels, columns - 4, which ->
                    save(e -> e.putInt("columns", which + 4)));
        });
        if (config.dockRow) {
            int rows = choices.getInt(Config.ROWS_BELOW_DOCK_KEY, config.rowsBelowDock);
            row(R.drawable.ic_apps_pill, activity.getString(R.string.rows_below_dock_label), String.valueOf(rows), v -> {
                String[] labels = {"0", "1", "2", "3"};
                choose(activity.getString(R.string.rows_below_dock_label), labels, rows, which ->
                        save(e -> e.putInt(Config.ROWS_BELOW_DOCK_KEY, which)));
            });
            int opacity = choices.getInt(Config.DOCK_OPACITY_KEY, config.dockOpacity);
            row(R.drawable.ic_picture, activity.getString(R.string.dock_opacity_label), opacity + "%", v -> {
                String[] labels = new String[DOCK_OPACITIES.length];
                int checked = 0;
                for (int i = 0; i < labels.length; i++) {
                    labels[i] = DOCK_OPACITIES[i] + "%";
                    if (DOCK_OPACITIES[i] == opacity) checked = i;
                }
                choose(activity.getString(R.string.dock_opacity_label), labels, checked, which ->
                        save(e -> e.putInt(Config.DOCK_OPACITY_KEY, DOCK_OPACITIES[which])));
            });
        }
        String clock = choices.getString(Config.CLOCK_FORMAT_KEY, config.clockFormat);
        String[] clockLabels = {activity.getString(R.string.clock_format_system),
                activity.getString(R.string.clock_format_12), activity.getString(R.string.clock_format_24)};
        int clockIndex = Math.max(0, java.util.Arrays.asList(CLOCK_FORMATS).indexOf(clock));
        row(R.drawable.ic_livetv, activity.getString(R.string.dream_name), clockLabels[clockIndex], v ->
                choose(activity.getString(R.string.dream_name), clockLabels, clockIndex, which ->
                        save(e -> e.putString(Config.CLOCK_FORMAT_KEY, CLOCK_FORMATS[which]))));
        row(R.drawable.ic_picture, activity.getString(R.string.wallpaper_label), wallpaperLabel(), v -> showWallpaper());

        boolean animations = choices.getBoolean("animations", config.animations);
        boolean advanced = animations && choices.getBoolean("advanced_effects", config.advancedEffects);
        row(R.drawable.ic_swap_vert, plainLabel(R.string.animations_state), onOff(animations), v ->
                save(e -> e.putBoolean("animations", !animations).putBoolean("advanced_effects", !animations && advanced)));
        View advancedRow = row(R.drawable.ic_swap_vert, activity.getString(R.string.advanced_effects), onOff(advanced), v -> {
            if (advanced) save(e -> e.putBoolean("advanced_effects", false));
            else new android.app.AlertDialog.Builder(activity)
                    .setTitle(R.string.advanced_effects)
                    .setMessage(R.string.advanced_effects_warning)
                    .setPositiveButton(R.string.enable, (d, w) -> save(e -> e.putBoolean("advanced_effects", true)))
                    .setNegativeButton(android.R.string.cancel, null)
                    .show();
        });
        advancedRow.setEnabled(animations);
        advancedRow.setAlpha(animations ? 1f : 0.5f);
        if (config.openOnStartChoice) {
            row(R.drawable.ic_input, activity.getString(R.string.open_on_start_label), openOnStartLabel(), v -> showOpenOnStart());
        }
        View follow = row(R.drawable.ic_lock_open, activity.getString(R.string.follow_theme),
                activity.getString(R.string.follow_theme_hint), v -> {
            choices.edit().remove("columns").remove(Config.ROWS_BELOW_DOCK_KEY).remove("animations")
                    .remove("advanced_effects").remove(Config.WALLPAPER_CHOICE_KEY)
                    .remove(Config.DOCK_OPACITY_KEY).remove(Config.CLOCK_FORMAT_KEY).remove(OpenOnStart.KEY).commit();
            changed.run();
            showMain();
        });
        follow.setEnabled(hasOnTvChoices());
        follow.setAlpha(hasOnTvChoices() ? 1f : 0.5f);
        row(R.drawable.ic_home_settings, activity.getString(R.string.home_settings),
                activity.getString(R.string.home_settings_help), v -> open(new Intent(activity, HomeSettingsActivity.class)));
        showBackupRows();
        backHandler = null;
        scroller.scrollTo(0, 0);
        first.requestFocus();
    }

    /**
     * backup_restore: the Backup &amp; restore section. Back up writes Backup.export to files/backup/ (reachable as
     * /sdcard/Android/data/&lt;package&gt;/files/backup over adb, no permission needed); Restore from file lists that
     * folder, a file adb pushed there included; Import from another launcher shows only while the same launcher under
     * its other package name is installed and reads its backup provider. Each restore asks first and then closes the
     * panel, so Home rebuilds with the restored settings.
     */
    private void showBackupRows() {
        String pkg = activity.getPackageName();
        TextView backupCaption = text(activity.getString(R.string.backup_section), 14, CAPTION, true);
        backupCaption.setPadding(dp(40), dp(22), dp(40), dp(10));
        list.addView(backupCaption, new LinearLayout.LayoutParams(-1, -2));
        row(R.drawable.ic_lock, activity.getString(R.string.backup_now), activity.getString(R.string.backup_now_hint, pkg), v -> {
            try {
                File out = Backup.saveToFile(activity);
                toast(activity.getString(R.string.backup_saved, "Android/data/" + pkg + "/files/" + Backup.BACKUP_DIR + "/" + out.getName()));
            } catch (Exception e) {
                toast(activity.getString(R.string.backup_failed, String.valueOf(e.getMessage())));
            }
        });
        row(R.drawable.ic_lock_open, activity.getString(R.string.restore_file), activity.getString(R.string.restore_file_hint), v -> {
            List<File> files = Backup.backups(activity);
            if (files.isEmpty()) { toast(activity.getString(R.string.restore_file_none)); return; }
            String[] labels = new String[files.size()];
            for (int i = 0; i < labels.length; i++) labels[i] = files.get(i).getName();
            choose(activity.getString(R.string.restore_file), labels, -1, which -> confirmRestore(labels[which], () -> Backup.read(files.get(which))));
        });
        String other = Backup.otherLauncher(activity);
        if (other != null) {
            row(R.drawable.ic_input, activity.getString(R.string.restore_import), activity.getString(R.string.restore_import_hint, other),
                    v -> confirmRestore(other, () -> Backup.readFrom(activity, other)));
        }
        TextView note = text(activity.getString(R.string.backup_readable_note, pkg), 13, SUBTITLE, false);
        note.setPadding(dp(40), dp(6), dp(40), dp(8));
        list.addView(note, new LinearLayout.LayoutParams(-1, -2));
    }

    /** What a restore reads: a file or the other launcher's provider. */
    private interface Source { org.json.JSONObject read() throws Exception; }

    private void confirmRestore(String from, Source source) {
        new android.app.AlertDialog.Builder(activity)
                .setTitle(R.string.restore_confirm_title)
                .setMessage(activity.getString(R.string.restore_confirm_message, from))
                .setPositiveButton(R.string.restore_button, (d, w) -> {
                    try {
                        Backup.restore(activity, source.read());
                    } catch (Exception e) {
                        toast(activity.getString(R.string.restore_failed, String.valueOf(e.getMessage())));
                        showMain();
                        return;
                    }
                    toast(activity.getString(R.string.restore_done));
                    // Home rebuilds on resume: the settings revision moved (Backup.restore).
                    activity.setResult(Activity.RESULT_OK);
                    activity.finish();
                })
                .setNegativeButton(android.R.string.cancel, (d, w) -> showMain())
                .show();
    }

    private void toast(String message) {
        android.widget.Toast.makeText(activity, message, android.widget.Toast.LENGTH_LONG).show();
    }

    /** A second page: the title names the setting, one row per value, the current one marked; Back returns. */
    private void choose(String heading, String[] labels, int checked, Consumer<Integer> onPick) {
        list.removeAllViews();
        title.setText(heading);
        View focus = null;
        for (int i = 0; i < labels.length; i++) {
            final int which = i;
            View r = row(radio(i == checked), labels[i], null, v -> {
                if (which != checked) onPick.accept(which);
                showMain();
            });
            if (i == checked) focus = r;
        }
        backHandler = this::showMain;
        scroller.scrollTo(0, 0);
        (focus != null ? focus : list.getChildAt(0)).requestFocus();
    }

    private void showWallpaper() {
        File dir = Config.dir(activity);
        List<File> images = new ArrayList<>(WallpaperRotation.images(dir == null ? null : new File(dir, "wallpapers")));
        File single = dir == null ? null : new File(dir, Config.WALLPAPER_NAME);
        if (single != null && single.isFile()) images.add(0, single);
        String[] labels = new String[images.size() + 2];
        labels[0] = activity.getString(R.string.wallpaper_theme_default);
        for (int i = 0; i < images.size(); i++) labels[i + 1] = images.get(i).getName();
        labels[labels.length - 1] = activity.getString(R.string.wallpaper_none);
        String current = choices.getString(Config.WALLPAPER_CHOICE_KEY, null);
        int checked = current == null ? 0 : "none".equals(current) ? labels.length - 1 : 0;
        for (int i = 0; i < images.size(); i++) if (images.get(i).getName().equals(current)) checked = i + 1;
        choose(activity.getString(R.string.wallpaper_label), labels, checked, which -> save(e -> {
            if (which == 0) e.remove(Config.WALLPAPER_CHOICE_KEY);
            else if (which == labels.length - 1) e.putString(Config.WALLPAPER_CHOICE_KEY, "none");
            else e.putString(Config.WALLPAPER_CHOICE_KEY, images.get(which - 1).getName());
        }));
    }

    private void showOpenOnStart() {
        PackageManager pm = activity.getPackageManager();
        List<String[]> apps = new ArrayList<>();
        Intent tv = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        for (ResolveInfo r : pm.queryIntentActivities(tv, 0)) {
            String pkg = r.activityInfo.packageName;
            if (pkg.equals(activity.getPackageName())) continue;
            boolean seen = false;
            for (String[] a : apps) seen |= a[1].equals(pkg);
            if (!seen) apps.add(new String[]{r.loadLabel(pm).toString(), pkg});
        }
        Collections.sort(apps, (a, b) -> a[0].compareToIgnoreCase(b[0]));
        String[] labels = new String[apps.size() + 1];
        labels[0] = activity.getString(R.string.off);
        String current = choices.getString(OpenOnStart.KEY, "");
        int checked = 0;
        for (int i = 0; i < apps.size(); i++) {
            labels[i + 1] = apps.get(i)[0];
            if (apps.get(i)[1].equals(current)) checked = i + 1;
        }
        choose(activity.getString(R.string.open_on_start_label), labels, checked, which -> {
            if (which == 0) choices.edit().remove(OpenOnStart.KEY).apply();
            else {
                OpenOnStart.skipThisBoot(activity);
                choices.edit().putString(OpenOnStart.KEY, apps.get(which - 1)[1]).apply();
            }
        });
    }

    // ---- helpers -----------------------------------------------------------------------------------

    private void save(Consumer<SharedPreferences.Editor> edit) {
        SharedPreferences.Editor e = choices.edit();
        edit.accept(e);
        e.apply();
        changed.run();
    }

    /** "Animations: %1$s" as the bare "Animations": the state strings double as row labels (31 languages). */
    private String plainLabel(int stateString) {
        String s = activity.getString(stateString, "");
        int colon = s.lastIndexOf(':');
        return (colon > 0 ? s.substring(0, colon) : s).trim();
    }

    private String onOff(boolean on) { return activity.getString(on ? R.string.on : R.string.off); }

    private String wallpaperLabel() {
        String current = choices.getString(Config.WALLPAPER_CHOICE_KEY, null);
        return "none".equals(current) ? activity.getString(R.string.wallpaper_none)
                : current != null ? current : activity.getString(R.string.wallpaper_theme_default);
    }

    private String openOnStartLabel() {
        String pkg = choices.getString(OpenOnStart.KEY, "");
        if (pkg.isEmpty()) return activity.getString(R.string.off);
        try {
            PackageManager pm = activity.getPackageManager();
            return pm.getApplicationLabel(pm.getApplicationInfo(pkg, 0)).toString();
        } catch (PackageManager.NameNotFoundException e) {
            return pkg;
        }
    }

    private boolean hasOnTvChoices() {
        return choices.contains("columns") || choices.contains(Config.ROWS_BELOW_DOCK_KEY)
                || choices.contains("animations") || choices.contains("advanced_effects")
                || choices.contains(Config.WALLPAPER_CHOICE_KEY) || choices.contains(Config.DOCK_OPACITY_KEY)
                || choices.contains(Config.CLOCK_FORMAT_KEY) || choices.contains(OpenOnStart.KEY);
    }

    private void open(Intent intent) {
        try { activity.startActivity(intent); }
        catch (android.content.ActivityNotFoundException | SecurityException e) {
            android.widget.Toast.makeText(activity, R.string.not_available, android.widget.Toast.LENGTH_LONG).show();
        }
    }

    /** One settings row: a 24dp icon (or its empty slot), a title, and an optional value or hint line under it. */
    private View row(int icon, String label, String subtitle, View.OnClickListener onClick) {
        Drawable d = icon == 0 ? null : activity.getDrawable(icon);
        if (d != null) { d = d.mutate(); d.setTint(Color.WHITE); }
        return row(d, label, subtitle, onClick);
    }

    private View row(Drawable icon, String label, String subtitle, View.OnClickListener onClick) {
        LinearLayout r = new LinearLayout(activity);
        r.setOrientation(LinearLayout.HORIZONTAL);
        r.setGravity(Gravity.CENTER_VERTICAL);
        r.setPadding(dp(40), dp(subtitle == null ? 18 : 14), dp(40), dp(subtitle == null ? 18 : 14));
        r.setMinimumHeight(dp(64));
        r.setFocusable(true);
        r.setClickable(true);
        r.setBackground(rowBackground());
        r.setOnClickListener(onClick);
        ImageView iv = new ImageView(activity);
        iv.setImageDrawable(icon);
        LinearLayout.LayoutParams ip = new LinearLayout.LayoutParams(dp(24), dp(24));
        ip.setMarginEnd(dp(24));
        r.addView(iv, ip);
        LinearLayout column = new LinearLayout(activity);
        column.setOrientation(LinearLayout.VERTICAL);
        column.addView(text(label, 17, Color.WHITE, false), new LinearLayout.LayoutParams(-1, -2));
        if (subtitle != null) {
            TextView sub = text(subtitle, 13, SUBTITLE, false);
            sub.setMaxLines(2);
            LinearLayout.LayoutParams sp = new LinearLayout.LayoutParams(-1, -2);
            sp.topMargin = dp(3);
            column.addView(sub, sp);
        }
        r.addView(column, new LinearLayout.LayoutParams(0, -2, 1));
        list.addView(r, new LinearLayout.LayoutParams(-1, -2));
        return r;
    }

    /** A radio mark: a ring, filled with a dot when chosen, in the caption blue. */
    private Drawable radio(boolean on) {
        GradientDrawable ring = new GradientDrawable();
        ring.setShape(GradientDrawable.OVAL);
        ring.setSize(dp(20), dp(20));
        ring.setStroke(dp(2), on ? CAPTION : SUBTITLE);
        ring.setColor(Color.TRANSPARENT);
        if (!on) return ring;
        GradientDrawable dot = new GradientDrawable();
        dot.setShape(GradientDrawable.OVAL);
        dot.setColor(CAPTION);
        android.graphics.drawable.LayerDrawable both = new android.graphics.drawable.LayerDrawable(new Drawable[]{ring, dot});
        both.setLayerInset(1, dp(5), dp(5), dp(5), dp(5));
        return both;
    }

    private Drawable rowBackground() {
        StateListDrawable bg = new StateListDrawable();
        GradientDrawable focused = new GradientDrawable();
        focused.setColor(ROW_FOCUS);
        bg.addState(new int[]{android.R.attr.state_focused}, focused);
        bg.addState(new int[]{}, new ColorDrawable(Color.TRANSPARENT));
        return bg;
    }

    private TextView text(String value, int sp, int color, boolean bold) {
        TextView tv = new TextView(activity);
        tv.setText(value);
        tv.setTextSize(sp);
        tv.setTextColor(color);
        tv.setTypeface(bold ? Typeface.create("sans-serif-medium", Typeface.NORMAL) : Typeface.SANS_SERIF);
        tv.setIncludeFontPadding(false);
        return tv;
    }

    private int dp(int value) { return Math.round(value * density); }
}
