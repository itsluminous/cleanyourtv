package com.kupraki.traktshelf;

import android.content.Context;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.StateListDrawable;
import android.util.TypedValue;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/** The look: Outfit, frosted pills, rating chips. The same language as the launcher: thin large titles, glass over slate. */
final class Ui {
    static final int BG = 0xFF101828;
    static final int ACCENT = 0xFFED1C24;
    private static Typeface outfit;

    private Ui() {
    }

    static Typeface font(Context c) {
        if (outfit == null) outfit = c.getResources().getFont(R.font.outfit);
        return outfit;
    }

    static float dp(Context c, float dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, c.getResources().getDisplayMetrics());
    }

    /** A frosted pill: translucent white slab with a hairline, brighter and white-edged while focused. */
    static StateListDrawable pill(Context c, float radiusDp) {
        float r = dp(c, radiusDp);
        GradientDrawable rest = new GradientDrawable();
        rest.setColor(0x1AFFFFFF);
        rest.setStroke((int) dp(c, 1), 0x33FFFFFF);
        rest.setCornerRadius(r);
        GradientDrawable focus = new GradientDrawable();
        focus.setColor(0x40FFFFFF);
        focus.setStroke((int) dp(c, 2), 0xE6FFFFFF);
        focus.setCornerRadius(r);
        StateListDrawable s = new StateListDrawable();
        s.addState(new int[]{android.R.attr.state_focused}, focus);
        s.addState(new int[]{}, rest);
        return s;
    }

    /** One rating chip: "IMDb 8.7", the border in the source's colour. Null value: no chip. */
    static TextView chip(Context c, String label, String value, int border) {
        if (value == null || value.isEmpty()) return null;
        TextView t = new TextView(c);
        t.setTypeface(font(c), Typeface.NORMAL);
        t.setTextSize(TypedValue.COMPLEX_UNIT_SP, 13);
        t.setTextColor(0xFFFFFFFF);
        t.setText(label + "  " + value);
        t.setLetterSpacing(0.02f);
        int ph = (int) dp(c, 12), pv = (int) dp(c, 6);
        t.setPadding(ph, pv, ph, pv);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(0x1AFFFFFF);
        bg.setStroke((int) dp(c, 1), border);
        bg.setCornerRadius(dp(c, 999));
        t.setBackground(bg);
        return t;
    }

    static List<TextView> ratingChips(Context c, Item it) {
        List<TextView> out = new ArrayList<>();
        TextView imdb = chip(c, "IMDb", it.imdbRating, 0x99F5C518);
        TextView rt = chip(c, "RT", it.rtRating, 0x99FA320A);
        TextView trakt = chip(c, "Trakt", it.traktPercent(), 0xA6ED1C24);
        if (imdb != null) out.add(imdb);
        if (rt != null) out.add(rt);
        if (trakt != null) out.add(trakt);
        return out;
    }

    static void applyFont(View v) {
        if (v instanceof TextView) {
            TextView t = (TextView) v;
            Typeface current = t.getTypeface();
            t.setTypeface(font(v.getContext()), current == null ? Typeface.NORMAL : current.getStyle());
        } else if (v instanceof android.view.ViewGroup) {
            android.view.ViewGroup g = (android.view.ViewGroup) v;
            for (int i = 0; i < g.getChildCount(); i++) applyFont(g.getChildAt(i));
        }
    }
}
