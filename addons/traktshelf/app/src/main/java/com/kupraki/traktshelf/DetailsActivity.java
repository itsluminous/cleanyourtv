package com.kupraki.traktshelf;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;

/**
 * One title: art, synopsis, ratings, cast, and Play / Trailer / Watchlist / Mark watched. Opened from Home with the
 * item itself, or from the launcher with extras trakt_id and kind (the item is then looked up in the feed).
 */
public final class DetailsActivity extends Activity {
    static final String EXTRA_TRAKT_ID = "trakt_id", EXTRA_KIND = "kind", EXTRA_ITEM = "item";
    private final Handler handler = new Handler(Looper.getMainLooper());
    private Item item;
    private boolean onWatchlist;
    private TextView btnWatchlist;

    static Intent intent(Context c, Item it) {
        Intent i = new Intent(c, DetailsActivity.class).putExtra(EXTRA_TRAKT_ID, it.traktId).putExtra(EXTRA_KIND, it.kind);
        try {
            i.putExtra(EXTRA_ITEM, it.toJson().toString());
        } catch (JSONException ignored) {
        }
        return i;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_details);
        Ui.applyFont(findViewById(android.R.id.content));
        long traktId = getIntent().getLongExtra(EXTRA_TRAKT_ID, 0);
        String kind = getIntent().getStringExtra(EXTRA_KIND);
        Feed feed = Feed.load(this);
        if (traktId > 0 && kind != null) item = feed.find(kind, traktId);
        if (item == null && getIntent().hasExtra(EXTRA_ITEM)) {
            try {
                item = Item.fromJson(new org.json.JSONObject(getIntent().getStringExtra(EXTRA_ITEM)));
            } catch (JSONException ignored) {
            }
        }
        if (item == null) {
            Toast.makeText(this, R.string.failed, Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        Feed.Row wl = feed.rows.get(Feed.WATCHLIST);
        if (wl != null) for (Item it : wl.items) if (it.key().equals(item.key())) onWatchlist = true;

        TextView tag = findViewById(R.id.tag);
        tag.setText(item.nextEpisode != null ? getString(R.string.row_up_next) + " · " + item.nextEpisode
                : getString(Item.SHOW.equals(item.kind) ? R.string.show : R.string.movie) + (item.status != null ? " · " + item.status : "")
                + (item.certification != null ? " · " + item.certification : ""));
        ((TextView) findViewById(R.id.title)).setText(item.title);
        ((TextView) findViewById(R.id.meta)).setText(item.metaLine());
        ((TextView) findViewById(R.id.synopsis)).setText(item.overview);
        TextView tagline = findViewById(R.id.tagline);
        if (item.tagline != null && !item.tagline.isEmpty()) {
            tagline.setText(item.tagline);
            tagline.setVisibility(View.VISIBLE);
        }
        TextView castLine = findViewById(R.id.castLine);
        castLine.setText(item.cast.isEmpty() ? "" : getString(R.string.cast) + ": " + String.join(", ", item.cast));
        LinearLayout chips = findViewById(R.id.chips);
        for (TextView chip : Ui.ratingChips(this, item)) {
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.rightMargin = (int) Ui.dp(this, 8);
            chips.addView(chip, lp);
        }
        ImageView backdrop = findViewById(R.id.backdrop);
        final String file = item.backdropFile();
        final int w = (int) Ui.dp(this, 640), h = (int) Ui.dp(this, 360);
        PosterAdapter.DECODER.execute(() -> {
            Bitmap b = Images.decode(this, file, w, h);
            if (b != null) handler.post(() -> backdrop.setImageBitmap(b));
        });

        View play = findViewById(R.id.btnPlay);
        play.setOnClickListener(v -> Players.play(this, item));
        findViewById(R.id.btnTrailer).setOnClickListener(v -> Players.trailer(this, item));
        btnWatchlist = findViewById(R.id.btnWatchlist);
        btnWatchlist.setText(onWatchlist ? R.string.remove_watchlist : R.string.add_watchlist);
        btnWatchlist.setOnClickListener(v -> write(onWatchlist ? "remove" : "add"));
        TextView watched = findViewById(R.id.btnWatched);
        watched.setText(item.nextEpisode != null ? getString(R.string.mark_episode_watched, item.nextEpisode.split(" · ")[0]) : getString(R.string.mark_watched));
        watched.setOnClickListener(v -> write("watched"));
        play.requestFocus();
    }

    private void write(String what) {
        Auth auth = new Auth(this);
        if (!auth.signedIn()) {
            Toast.makeText(this, R.string.needs_sign_in, Toast.LENGTH_SHORT).show();
            return;
        }
        Trakt trakt = new Trakt(auth);
        new Thread(() -> {
            boolean ok;
            switch (what) {
                case "add":
                    ok = trakt.addToWatchlist(item);
                    break;
                case "remove":
                    ok = trakt.removeFromWatchlist(item);
                    break;
                default:
                    ok = trakt.markWatched(item);
            }
            handler.post(() -> {
                if (isDestroyed()) return;
                Toast.makeText(this, ok ? R.string.done : R.string.failed, Toast.LENGTH_SHORT).show();
                if (ok && !"watched".equals(what)) {
                    onWatchlist = "add".equals(what);
                    btnWatchlist.setText(onWatchlist ? R.string.remove_watchlist : R.string.add_watchlist);
                }
                if (ok) Sync.runNow(this, null);   // the rows reflect the change when Home is next shown
            });
        }, "trakt-write").start();
    }
}
