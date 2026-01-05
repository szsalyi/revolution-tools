package com.revolution.tools.roulette.util;

import java.util.Arrays;
import java.util.List;
import java.util.Set;

/**
 * Roulette number characteristics and mappings.
 */
public class RouletteCharacteristics {

    // Red numbers in European roulette
    public static final Set<Integer> RED_NUMBERS = Set.of(
            1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36
    );

    // Black numbers (all non-red, excluding 0)
    public static final Set<Integer> BLACK_NUMBERS = Set.of(
            2, 4, 6, 8, 10, 11, 13, 15, 17, 20, 22, 24, 26, 28, 29, 31, 33, 35
    );

    // First dozen (1-12)
    public static final List<Integer> FIRST_DOZEN = Arrays.asList(
            1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12
    );

    // Second dozen (13-24)
    public static final List<Integer> SECOND_DOZEN = Arrays.asList(
            13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24
    );

    // Third dozen (25-36)
    public static final List<Integer> THIRD_DOZEN = Arrays.asList(
            25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36
    );

    // Wheel order for European roulette
    public static final int[] WHEEL_ORDER = {
            0, 32, 15, 19, 4, 21, 2, 25, 17, 34, 6, 27, 13, 36, 11, 30, 8, 23, 10,
            5, 24, 16, 33, 1, 20, 14, 31, 9, 22, 18, 29, 7, 28, 12, 35, 3, 26
    };

    /**
     * Check if number is red.
     */
    public static boolean isRed(Integer number) {
        return RED_NUMBERS.contains(number);
    }

    /**
     * Check if number is black.
     */
    public static boolean isBlack(Integer number) {
        return BLACK_NUMBERS.contains(number);
    }

    /**
     * Check if number is even (excluding 0).
     */
    public static boolean isEven(Integer number) {
        return number != 0 && number % 2 == 0;
    }

    /**
     * Check if number is odd.
     */
    public static boolean isOdd(Integer number) {
        return number != 0 && number % 2 == 1;
    }

    /**
     * Get dozen for number (1, 2, or 3).
     */
    public static Integer getDozen(Integer number) {
        if (number == 0) return null;
        if (number <= 12) return 1;
        if (number <= 24) return 2;
        return 3;
    }

    /**
     * Get all numbers in a specific dozen.
     */
    public static List<Integer> getDozenNumbers(Integer dozen) {
        return switch (dozen) {
            case 1 -> FIRST_DOZEN;
            case 2 -> SECOND_DOZEN;
            case 3 -> THIRD_DOZEN;
            default -> List.of();
        };
    }

    /**
     * Get numeric sector (0-9, 10-19, 20-29, 30-36).
     */
    public static Integer getSector(Integer number) {
        if (number >= 0 && number <= 9) return 0;
        if (number >= 10 && number <= 19) return 1;
        if (number >= 20 && number <= 29) return 2;
        if (number >= 30 && number <= 36) return 3;
        return null;
    }

    /**
     * Get all red numbers.
     */
    public static List<Integer> getAllRedNumbers() {
        return RED_NUMBERS.stream().sorted().toList();
    }

    /**
     * Get all black numbers.
     */
    public static List<Integer> getAllBlackNumbers() {
        return BLACK_NUMBERS.stream().sorted().toList();
    }

    /**
     * Get all even numbers (excluding 0).
     */
    public static List<Integer> getAllEvenNumbers() {
        return Arrays.asList(2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 30, 32, 34, 36);
    }

    /**
     * Get all odd numbers.
     */
    public static List<Integer> getAllOddNumbers() {
        return Arrays.asList(1, 3, 5, 7, 9, 11, 13, 15, 17, 19, 21, 23, 25, 27, 29, 31, 33, 35);
    }

    /**
     * Get opposite color numbers.
     */
    public static List<Integer> getOppositeColorNumbers(Integer number) {
        if (number == 0) return getAllRedNumbers(); // Default to red for 0
        return isRed(number) ? getAllBlackNumbers() : getAllRedNumbers();
    }

    /**
     * Get opposite parity numbers.
     */
    public static List<Integer> getOppositeParity(Integer number) {
        if (number == 0) return getAllEvenNumbers(); // Default to even for 0
        return isEven(number) ? getAllOddNumbers() : getAllEvenNumbers();
    }

    /**
     * Find position of number on wheel.
     */
    public static int getWheelPosition(Integer number) {
        for (int i = 0; i < WHEEL_ORDER.length; i++) {
            if (WHEEL_ORDER[i] == number) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Get mirror number (opposite side of wheel).
     */
    public static Integer getMirrorNumber(Integer number) {
        int position = getWheelPosition(number);
        if (position == -1) return null;

        // Calculate opposite position (halfway around the wheel)
        int mirrorPosition = (position + (WHEEL_ORDER.length / 2)) % WHEEL_ORDER.length;
        return WHEEL_ORDER[mirrorPosition];
    }
}
