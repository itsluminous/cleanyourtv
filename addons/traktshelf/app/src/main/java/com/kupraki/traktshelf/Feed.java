package com.kupraki.traktshelf;

import android.content.Context;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The rows Home shows and the provider serves, on disk as files/feed.json, plus files/meta.json: every item
 * ever enriched, keyed by kind:trakt id, so a sync refetches TMDB and OMDb only after seven days.
 */
final class Feed {
    private static final String TAG = "TraktShelf.Feed";
    static final String UP_NEXT = "up_next", REC_SHOWS = "rec_shows", REC_MOVIES = "rec_movies",
            WATCHLIST = "watchlist", TRENDING = "trending", HISTORY = "history";
    /** Row order on Home. */
    static final String[] ROW_ORDER = {UP_NEXT, REC_SHOWS, REC_MOVIES, WATCHLIST, TRENDING, HISTORY};
    static final long META_TTL_MS = 7L * 86_400_000L;

    static final class Row {
        final String key;
        final List<Item> items = new ArrayList<>();

        Row(String key) {
            this.key = key;
        }

        int titleRes() {
            switch (key) {
                case UP_NEXT:
                    return R.string.row_up_next;
                case REC_SHOWS:
                    return R.string.row_rec_shows;
                case REC_MOVIES:
                    return R.string.row_rec_movies;
                case WATCHLIST:
                    return R.string.row_watchlist;
                case HISTORY:
                    return R.string.row_history;
                default:
                    return R.string.row_trending;
            }
        }
    }

    final LinkedHashMap<String, Row> rows = new LinkedHashMap<>();
    long syncedAt;
    String lastError;

    static File file(Context c) {
        return new File(c.getFilesDir(), "feed.json");
    }

    static File metaFile(Context c) {
        return new File(c.getFilesDir(), "meta.json");
    }

    static Feed load(Context c) {
        Feed f = new Feed();
        File file = file(c);
        if (!file.isFile()) return f;
        try {
            JSONObject j = new JSONObject(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
            f.syncedAt = j.optLong("synced_at");
            f.lastError = Item.opt(j, "last_error");
            JSONObject rows = j.optJSONObject("rows");
            if (rows != null) {
                for (String key : ROW_ORDER) {
                    JSONArray a = rows.optJSONArray(key);
                    if (a == null) continue;
                    Row r = new Row(key);
                    for (int i = 0; i < a.length(); i++) r.items.add(Item.fromJson(a.getJSONObject(i)));
                    f.rows.put(key, r);
                }
            }
        } catch (IOException | JSONException e) {
            Log.w(TAG, "feed.json unreadable: " + e.getMessage());
        }
        return f;
    }

    void save(Context c) {
        try {
            JSONObject rows = new JSONObject();
            for (Row r : this.rows.values()) {
                JSONArray a = new JSONArray();
                for (Item it : r.items) a.put(it.toJson());
                rows.put(r.key, a);
            }
            JSONObject j = new JSONObject().put("synced_at", syncedAt).put("last_error", lastError).put("rows", rows);
            writeAtomic(file(c), j.toString());
        } catch (IOException | JSONException e) {
            Log.w(TAG, "feed.json not saved: " + e.getMessage());
        }
    }

    /** Every item once, in row order (the provider's order); the first row an item appears in wins. */
    List<Item> flattened() {
        Map<String, Item> seen = new LinkedHashMap<>();
        for (String key : ROW_ORDER) {
            Row r = rows.get(key);
            if (r == null) continue;
            for (Item it : r.items) if (!seen.containsKey(it.key())) seen.put(it.key(), it);
        }
        return new ArrayList<>(seen.values());
    }

    Item find(String kind, long traktId) {
        for (Row r : rows.values()) for (Item it : r.items) if (it.traktId == traktId && it.kind.equals(kind)) return it;
        return null;
    }

    /** Items for the launcher's carousel: a few of each row, Up Next first, only those with a backdrop on disk. */
    List<Item> heroItems(Context c, int max) {
        int[] quota = {4, 3, 3, 2, 3, 0};
        List<Item> out = new ArrayList<>();
        Map<String, Boolean> seen = new HashMap<>();
        File img = Images.dir(c);
        for (int pass = 0; pass < 2 && out.size() < max; pass++) {
            for (int i = 0; i < ROW_ORDER.length; i++) {
                Row r = rows.get(ROW_ORDER[i]);
                if (r == null) continue;
                int take = pass == 0 ? quota[i] : Integer.MAX_VALUE;
                for (Item it : r.items) {
                    if (take <= 0 || out.size() >= max) break;
                    if (seen.containsKey(it.key()) || it.backdropUrl == null || !new File(img, it.backdropFile()).isFile()) continue;
                    seen.put(it.key(), Boolean.TRUE);
                    out.add(it);
                    take--;
                }
            }
        }
        return out;
    }

    // ---- metadata cache ---------------------------------------------------------------------------

    static Map<String, Item> loadMeta(Context c) {
        Map<String, Item> out = new HashMap<>();
        File file = metaFile(c);
        if (!file.isFile()) return out;
        try {
            JSONObject j = new JSONObject(new String(Files.readAllBytes(file.toPath()), StandardCharsets.UTF_8));
            JSONArray a = j.optJSONArray("items");
            if (a != null) for (int i = 0; i < a.length(); i++) {
                Item it = Item.fromJson(a.getJSONObject(i));
                out.put(it.key(), it);
            }
        } catch (IOException | JSONException e) {
            Log.w(TAG, "meta.json unreadable: " + e.getMessage());
        }
        return out;
    }

    static void saveMeta(Context c, Map<String, Item> meta) {
        try {
            // Drop entries older than 30 days, so the file tracks what the account still shows.
            long cutoff = System.currentTimeMillis() - 30L * 86_400_000L;
            JSONArray a = new JSONArray();
            for (Item it : meta.values()) if (it.enrichedAt >= cutoff) a.put(it.toJson());
            writeAtomic(metaFile(c), new JSONObject().put("items", a).toString());
        } catch (IOException | JSONException e) {
            Log.w(TAG, "meta.json not saved: " + e.getMessage());
        }
    }

    static void writeAtomic(File file, String text) throws IOException {
        File tmp = new File(file.getPath() + ".tmp");
        try (FileOutputStream out = new FileOutputStream(tmp)) {
            out.write(text.getBytes(StandardCharsets.UTF_8));
            out.getFD().sync();
        }
        if (!tmp.renameTo(file)) throw new IOException("rename failed");
    }

    static List<Item> none() {
        return Collections.emptyList();
    }
}
