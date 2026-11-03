package com.gathin.tvhome;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/** hero_height: the Launcher settings choice over the theme's key, and the tile fit's title room scaling with it. */
public class HeroHeightTest {

    private static final float D = 2f;
    private static final int INNER = 1696, GRID_H = 846;

    @Test
    public void aSavedChoiceInRangeOverridesTheTheme() {
        assertEquals(60, Config.chosenHeroHeight(60, 100));
        assertEquals(120, Config.chosenHeroHeight(120, 80));
        assertEquals(50, Config.chosenHeroHeight(Config.HERO_HEIGHT_MIN, 100));
        assertEquals(130, Config.chosenHeroHeight(Config.HERO_HEIGHT_MAX, 100));
    }

    @Test
    public void noChoiceOrAnOutOfRangeOneLeavesTheTheme() {
        assertEquals(80, Config.chosenHeroHeight(-1, 80));
        assertEquals(100, Config.chosenHeroHeight(0, 100));
        assertEquals(100, Config.chosenHeroHeight(49, 100));
        assertEquals(100, Config.chosenHeroHeight(131, 100));
    }

    @Test
    public void thePanelOffersTheFourLevelsInRange() {
        int[] levels = SettingsPanel.HERO_HEIGHTS;
        assertEquals(4, levels.length);
        for (int pct : levels) {
            assertTrue(pct >= Config.HERO_HEIGHT_MIN && pct <= Config.HERO_HEIGHT_MAX);
            assertEquals(pct, Config.chosenHeroHeight(pct, 100));
        }
        assertEquals(100, levels[2]);   // Normal is the layout's own
    }

    @Test
    public void aShorterShelfReservesLessTitleRoomSoTilesStayLarger() {
        // Two rows under a titled dock at 4 columns: at 100 the tiles sit on the 85% floor; a 60% shelf frees 42dp
        // of title room, so the fit can give them more width, and never less.
        int at100 = AppAdapter.tileWidthPx(INNER, GRID_H, 4, 2, true, true, D, (int) AppAdapter.ITEM_EXTRA_DP, 100);
        int at60 = AppAdapter.tileWidthPx(INNER, GRID_H, 4, 2, true, true, D, (int) AppAdapter.ITEM_EXTRA_DP, 60);
        int at120 = AppAdapter.tileWidthPx(INNER, GRID_H, 4, 2, true, true, D, (int) AppAdapter.ITEM_EXTRA_DP, 120);
        assertTrue(at60 >= at100);
        assertTrue(at120 <= at100);
        // The two-argument-shorter overload is the 100 case.
        assertEquals(at100, AppAdapter.tileWidthPx(INNER, GRID_H, 4, 2, true, true, D, (int) AppAdapter.ITEM_EXTRA_DP));
    }
}
