package com.kupraki.traktshelf;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

/**
 * Where Play and Trailer go. Play routes by IMDb id: Stremio's deep link (stremio:///detail/movie/tt../tt.., checked on
 * this TV: it opens the title's streams), CloudStream's search scheme (cloudstreamsearch://title), or NetMirror's main
 * screen (it has no search intent). Trailers go to TizenTube or SmartTube as a YouTube watch URL (both declare the
 * youtube.com host); the stock YouTube app is used only when neither is installed.
 */
final class Players {
    static final String PREFS = "settings";
    static final String KEY_PLAYER = "player";   // ask | stremio | cloudstream | netmirror
    static final String STREMIO = "com.stremio.one", CLOUDSTREAM = "com.lagradost.cloudstream3", NETMIRROR = "app.netmirror.newtv";
    /** Trailers go to the first of these that is installed: TizenTube, then SmartTube's builds. */
    static final String[] TRAILER_PREFERRED = {"io.gh.reisxd.tizentube.cobalt", "org.smarttube.stable", "org.smarttube.beta", "com.teamsmart.videomanager.tv"};

    private Players() {
    }

    static final class Player {
        final String id, label, pkg;

        Player(String id, String label, String pkg) {
            this.id = id;
            this.label = label;
            this.pkg = pkg;
        }
    }

    static boolean installed(Context c, String pkg) {
        try {
            c.getPackageManager().getPackageInfo(pkg, 0);
            return true;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    static List<Player> installedPlayers(Context c) {
        List<Player> out = new ArrayList<>();
        if (installed(c, STREMIO)) out.add(new Player("stremio", "Stremio", STREMIO));
        if (installed(c, CLOUDSTREAM)) out.add(new Player("cloudstream", "CloudStream", CLOUDSTREAM));
        if (installed(c, NETMIRROR)) out.add(new Player("netmirror", "NetMirror", NETMIRROR));
        return out;
    }

    static String defaultPlayer(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_PLAYER, "ask");
    }

    static void setDefaultPlayer(Context c, String id) {
        c.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().putString(KEY_PLAYER, id).apply();
    }

    static Intent playIntent(Context c, Player p, Item it) {
        switch (p.id) {
            case "stremio": {
                String type = Item.SHOW.equals(it.kind) ? "series" : "movie";
                String id = it.imdbId != null ? it.imdbId : "tmdb:" + it.tmdbId;
                String video = id;
                if (Item.SHOW.equals(it.kind)) video = it.nextSeason > 0 ? id + ":" + it.nextSeason + ":" + it.nextNumber : null;
                String url = "stremio:///detail/" + type + "/" + id + (video != null ? "/" + video : "");
                return new Intent(Intent.ACTION_VIEW, Uri.parse(url)).setPackage(p.pkg);
            }
            case "cloudstream":
                return new Intent(Intent.ACTION_VIEW, Uri.parse("cloudstreamsearch://" + Uri.encode(it.title))).setPackage(p.pkg);
            default: {
                Intent launch = c.getPackageManager().getLeanbackLaunchIntentForPackage(p.pkg);
                if (launch == null) launch = c.getPackageManager().getLaunchIntentForPackage(p.pkg);
                return launch;
            }
        }
    }

    static void play(Activity a, Item it) {
        List<Player> players = installedPlayers(a);
        if (players.isEmpty()) {
            Toast.makeText(a, R.string.no_player, Toast.LENGTH_LONG).show();
            return;
        }
        String def = defaultPlayer(a);
        for (Player p : players) if (p.id.equals(def)) {
            start(a, playIntent(a, p, it));
            return;
        }
        if (players.size() == 1) {
            start(a, playIntent(a, players.get(0), it));
            return;
        }
        String[] names = new String[players.size()];
        for (int i = 0; i < names.length; i++) names[i] = players.get(i).label;
        new AlertDialog.Builder(a, android.R.style.Theme_Material_Dialog_NoActionBar)
                .setTitle(a.getString(R.string.play_with))
                .setItems(names, (d, which) -> start(a, playIntent(a, players.get(which), it)))
                .show();
    }

    static void trailer(Activity a, Item it) {
        if (it.trailerYoutubeId == null) {
            Toast.makeText(a, R.string.no_trailer, Toast.LENGTH_SHORT).show();
            return;
        }
        Intent view = new Intent(Intent.ACTION_VIEW, Uri.parse("https://www.youtube.com/watch?v=" + it.trailerYoutubeId));
        List<ResolveInfo> handlers = a.getPackageManager().queryIntentActivities(view, 0);
        // TizenTube when installed, SmartTube otherwise: straight there, no chooser.
        for (String pkg : TRAILER_PREFERRED) for (ResolveInfo r : handlers) if (pkg.equals(r.activityInfo.packageName)) {
            start(a, new Intent(view).setPackage(pkg));
            return;
        }
        if (handlers.size() == 1) {
            start(a, new Intent(view).setPackage(handlers.get(0).activityInfo.packageName));
        } else if (handlers.size() > 1) {
            // Neither is installed and several other apps take YouTube links: ask.
            String[] names = new String[handlers.size()];
            for (int i = 0; i < names.length; i++) names[i] = String.valueOf(handlers.get(i).loadLabel(a.getPackageManager()));
            new AlertDialog.Builder(a, android.R.style.Theme_Material_Dialog_NoActionBar)
                    .setTitle(a.getString(R.string.trailer_with))
                    .setItems(names, (d, which) -> start(a, new Intent(view).setPackage(handlers.get(which).activityInfo.packageName)))
                    .show();
        } else {
            start(a, new Intent(Intent.ACTION_VIEW, Uri.parse("vnd.youtube:" + it.trailerYoutubeId)));
        }
    }

    private static void start(Activity a, Intent i) {
        if (i == null) {
            Toast.makeText(a, R.string.no_player, Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            a.startActivity(i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK));
        } catch (ActivityNotFoundException | SecurityException e) {
            Toast.makeText(a, R.string.no_player, Toast.LENGTH_SHORT).show();
        }
    }

    static SharedPreferences prefs(Context c) {
        return c.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
    }
}
