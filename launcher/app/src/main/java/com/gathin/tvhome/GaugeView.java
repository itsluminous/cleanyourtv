package com.gathin.tvhome;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Typeface;
import android.util.TypedValue;
import android.view.View;

import java.util.Locale;

/**
 * widgets: one ring gauge, drawn with Canvas arcs. A thin track ring in frost white, an arc from twelve o'clock
 * clockwise for the percent full, the percent inside the ring and a small caps label under it (RAM, CPU, STORAGE).
 * Fixed size: RING_DP across, the label's line below, so three of them always fit beside the date.
 */
public class GaugeView extends View {

    /** The ring's outer diameter, its stroke, the gap to the label and the label's line, dp. */
    static final float RING_DP = 44f, STROKE_DP = 3f, LABEL_GAP_DP = 4f, LABEL_LINE_DP = 13f;
    /** The track's frost: white at 25%. The arc and the texts are white, the label a little dimmed. */
    static final int TRACK = 0x40FFFFFF, ARC = 0xFFFFFFFF, VALUE = 0xF2FFFFFF, LABEL = 0xB3FFFFFF;

    private final Paint track = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint arc = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint value = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final Paint label = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final RectF ring = new RectF();
    private final float density;
    private int percent = -1;
    private String labelText = "";
    private String valueText = "";

    public GaugeView(Context context) {
        super(context);
        density = getResources().getDisplayMetrics().density;
        float stroke = STROKE_DP * density;
        track.setStyle(Paint.Style.STROKE);
        track.setStrokeWidth(stroke);
        track.setColor(TRACK);
        arc.setStyle(Paint.Style.STROKE);
        arc.setStrokeWidth(stroke);
        arc.setStrokeCap(Paint.Cap.ROUND);
        arc.setColor(ARC);
        Typeface medium = UiFont.get(context, R.string.font_medium);
        value.setTypeface(medium);
        value.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 11f, getResources().getDisplayMetrics()));
        value.setColor(VALUE);
        value.setTextAlign(Paint.Align.CENTER);
        label.setTypeface(medium);
        label.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 10f, getResources().getDisplayMetrics()));
        label.setLetterSpacing(0.1f);
        label.setColor(LABEL);
        label.setTextAlign(Paint.Align.CENTER);
        setWillNotDraw(false);
    }

    /** The caps label under the ring, in the TV's language's upper case. */
    void setLabel(CharSequence text) {
        String caps = text == null ? "" : text.toString().toUpperCase(Locale.getDefault());
        if (caps.equals(labelText)) return;
        labelText = caps;
        describe();
        requestLayout();   // a label wider than the ring (STORAGE) widens the gauge
        invalidate();
    }

    /** What a screen reader (and a uiautomator dump) gets for the gauge: its label and its percent. */
    private void describe() {
        setContentDescription(valueText.isEmpty() ? labelText : labelText + " " + valueText);
    }

    /** 0 to 100 fills the arc and writes the percent inside; anything else clears both (the figure cannot be read). */
    void setPercent(int p) {
        int clamped = p < 0 ? -1 : Math.min(100, p);
        if (clamped == percent) return;
        percent = clamped;
        valueText = clamped < 0 ? "" : getResources().getString(R.string.widget_percent, clamped);
        describe();
        invalidate();
    }

    int percent() {
        return percent;
    }

    /** The arc's sweep, degrees, for a percent: 3.6 per point, clockwise from twelve o'clock. */
    static float sweepDegrees(int percent) {
        return Math.max(0, Math.min(100, percent)) * 3.6f;
    }

    /** The ring's width: the gauge is at least this wide, and wider when its label is. */
    static int widthPx(float density) {
        return Math.round(RING_DP * density);
    }

    /** The gauge's own width: the ring, or the label (its tracking included) and a pixel each side when that is wider. */
    static int widthPx(float density, float labelPx) {
        return Math.max(widthPx(density), Math.round(labelPx) + 2);
    }

    static int heightPx(float density) {
        return Math.round((RING_DP + LABEL_GAP_DP + LABEL_LINE_DP) * density);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int want = widthPx(density, labelText.isEmpty() ? 0f : label.measureText(labelText));
        setMeasuredDimension(resolveSize(want, widthMeasureSpec), resolveSize(heightPx(density), heightMeasureSpec));
    }

    @Override
    protected void onDraw(Canvas canvas) {
        float size = RING_DP * density, half = STROKE_DP * density / 2f;
        float left = (getWidth() - size) / 2f;
        ring.set(left + half, half, left + size - half, size - half);
        canvas.drawOval(ring, track);
        if (percent >= 0) {
            canvas.drawArc(ring, -90f, sweepDegrees(percent), false, arc);
            // The percent on the ring's centre line: half the cap height up from the baseline centres the digits.
            float cy = size / 2f - (value.ascent() + value.descent()) / 2f;
            canvas.drawText(valueText, getWidth() / 2f, cy, value);
        }
        if (!labelText.isEmpty()) {
            float top = size + LABEL_GAP_DP * density;
            float baseline = top + LABEL_LINE_DP * density / 2f - (label.ascent() + label.descent()) / 2f;
            canvas.drawText(labelText, getWidth() / 2f, baseline, label);
        }
    }
}
