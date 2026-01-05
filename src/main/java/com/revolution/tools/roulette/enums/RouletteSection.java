package com.revolution.tools.roulette.enums;

/**
 * Sections of the roulette wheel (French bets).
 */
public enum RouletteSection {
    /**
     * Zero (0)
     */
    ZERO,

    /**
     * Voisins du Zero (Neighbors of Zero)
     * 22,18,29,7,28,12,35,3,26,0,32,15,19,4,21,2,25
     */
    VOISINS,

    /**
     * Tiers du Cylindre (Third of the Wheel)
     * 27,13,36,11,30,8,23,10,5,24,16,33
     */
    TIERS,

    /**
     * Orphelins (Orphans)
     * 1,20,14,31,9,17,34,6
     */
    ORPHELINS
}
