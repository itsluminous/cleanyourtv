package com.kupraki.traktshelf;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/** Account, Sync now, Default player, Sign out. */
public final class SettingsActivity extends Activity {
    private final Handler handler = new Handler(Looper.getMainLooper());
    private LinearLayout list;
    private Auth auth;
    private TextView syncValue;
    private final Runnable tick = this::updateSync;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        Ui.applyFont(findViewById(android.R.id.content));
        list = findViewById(R.id.list);
        auth = new Auth(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        build();
    }

    @Override
    protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
    }

    private void build() {
        list.removeAllViews();
        boolean signedIn = auth.signedIn();
        row(getString(R.string.account), signedIn ? (auth.username() != null ? auth.username() : "Trakt") : getString(R.string.not_signed_in),
                v -> {
                    if (!signedIn) startActivity(new Intent(this, SignInActivity.class));
                });
        syncValue = row(getString(R.string.sync_now), "", v -> {
            Sync.runNow(this, null);
            updateSync();
        });
        updateSync();
        List<Players.Player> players = Players.installedPlayers(this);
        row(getString(R.string.default_player), playerLabel(players), v -> {
            String cur = Players.defaultPlayer(this);
            int at = -1;
            for (int i = 0; i < players.size(); i++) if (players.get(i).id.equals(cur)) at = i;
            // Cycle: ask, then each installed player.
            String next = at + 1 < players.size() ? players.get(at + 1).id : "ask";
            if (cur.equals("ask") && !players.isEmpty()) next = players.get(0).id;
            Players.setDefaultPlayer(this, next);
            ((TextView) v.findViewById(R.id.rowValue)).setText(playerLabel(players));
        });
        if (signedIn) row(getString(R.string.sign_out), "", v -> {
            auth.signOut();
            Feed feed = new Feed();
            feed.save(this);
            getContentResolver().notifyChange(HeroProvider.ITEMS, null);
            Sync.runNow(this, null);
            build();
        });
        View first = list.getChildAt(0);
        if (first != null) first.requestFocus();
    }

    private String playerLabel(List<Players.Player> players) {
        String cur = Players.defaultPlayer(this);
        for (Players.Player p : players) if (p.id.equals(cur)) return p.label;
        return getString(R.string.player_ask);
    }

    private void updateSync() {
        handler.removeCallbacks(tick);
        syncValue.setVisibility(View.VISIBLE);
        if (Sync.isRunning()) {
            String p = Sync.progress();
            syncValue.setText(p == null ? getString(R.string.syncing) : p);
            handler.postDelayed(tick, 700);
        } else {
            Feed feed = Feed.load(this);
            String when = feed.syncedAt == 0 ? getString(R.string.never) : DateUtils.getRelativeTimeSpanString(feed.syncedAt).toString();
            syncValue.setText(getString(R.string.last_sync, when) + (feed.lastError != null ? " · " + feed.lastError : ""));
        }
    }

    private TextView row(String name, String value, View.OnClickListener click) {
        View v = LayoutInflater.from(this).inflate(R.layout.settings_row, list, false);
        Ui.applyFont(v);
        ((TextView) v.findViewById(R.id.rowName)).setText(name);
        TextView val = v.findViewById(R.id.rowValue);
        val.setText(value);
        val.setVisibility(value.isEmpty() ? View.GONE : View.VISIBLE);
        v.setOnClickListener(click);
        list.addView(v);
        return val;
    }
}
