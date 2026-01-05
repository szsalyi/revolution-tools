package com.revolution.tools.roulette.entity;

import com.revolution.tools.roulette.enums.RouletteColor;
import com.revolution.tools.roulette.enums.RouletteSection;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Entity representing a single roulette spin result.
 */
@Entity
@Table(name = "roulette_spins", indexes = {
    @Index(name = "idx_session_id", columnList = "session_id"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RouletteSpin {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "session_id", nullable = false)
    private Long sessionId;

    @Column(name = "spin_number", nullable = false)
    private Integer spinNumber; // 0-36

    @Column(name = "color", nullable = false, length = 10)
    @Enumerated(EnumType.STRING)
    private RouletteColor color;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "section", length = 20)
    @Enumerated(EnumType.STRING)
    private RouletteSection section;

    @Column(name = "dozen")
    private Integer dozen; // 1, 2, or 3

    @Column(name = "column_num")
    private Integer columnNum; // 1, 2, or 3

    @Column(name = "is_even")
    private Boolean isEven;

    @Column(name = "is_high")
    private Boolean isHigh; // 19-36 = high, 1-18 = low

    @Column(name = "sequence_number")
    private Integer sequenceNumber; // Order in session (1, 2, 3, ...)

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        calculateProperties();
    }

    /**
     * Helper to create a minimal session object for compatibility.
     */
    public RouletteSession getSession() {
        RouletteSession session = new RouletteSession();
        session.setId(this.sessionId);
        return session;
    }

    /**
     * Calculates derived properties from spin number.
     */
    private void calculateProperties() {
        if (spinNumber == null) return;

        // Calculate color
        if (spinNumber == 0) {
            color = RouletteColor.GREEN;
        } else if (isRedNumber(spinNumber)) {
            color = RouletteColor.RED;
        } else {
            color = RouletteColor.BLACK;
        }

        // Calculate dozen (1st: 1-12, 2nd: 13-24, 3rd: 25-36)
        if (spinNumber == 0) {
            dozen = null;
        } else if (spinNumber <= 12) {
            dozen = 1;
        } else if (spinNumber <= 24) {
            dozen = 2;
        } else {
            dozen = 3;
        }

        // Calculate column
        if (spinNumber == 0) {
            columnNum = null;
        } else {
            columnNum = ((spinNumber - 1) % 3) + 1;
        }

        // Calculate even/odd
        isEven = spinNumber != 0 && spinNumber % 2 == 0;

        // Calculate high/low
        if (spinNumber == 0) {
            isHigh = null;
        } else {
            isHigh = spinNumber >= 19;
        }

        // Calculate section (simplified version)
        section = calculateSection(spinNumber);
    }

    /**
     * Determines if a number is red on the roulette wheel.
     */
    private boolean isRedNumber(int number) {
        int[] redNumbers = {1, 3, 5, 7, 9, 12, 14, 16, 18, 19, 21, 23, 25, 27, 30, 32, 34, 36};
        for (int red : redNumbers) {
            if (number == red) return true;
        }
        return false;
    }

    /**
     * Calculates the wheel section for the number.
     */
    private RouletteSection calculateSection(int number) {
        // Simplified section mapping - can be enhanced with actual wheel positions
        if (number == 0) {
            return RouletteSection.ZERO;
        } else if (isVoisinsNumber(number)) {
            return RouletteSection.VOISINS;
        } else if (isOrphelinsNumber(number)) {
            return RouletteSection.ORPHELINS;
        } else {
            return RouletteSection.TIERS;
        }
    }

    /**
     * Checks if number is in Voisins du Zero section.
     */
    private boolean isVoisinsNumber(int number) {
        int[] voisins = {22, 18, 29, 7, 28, 12, 35, 3, 26, 0, 32, 15, 19, 4, 21, 2, 25};
        for (int v : voisins) {
            if (number == v) return true;
        }
        return false;
    }

    /**
     * Checks if number is in Orphelins section.
     */
    private boolean isOrphelinsNumber(int number) {
        int[] orphelins = {1, 20, 14, 31, 9, 17, 34, 6};
        for (int o : orphelins) {
            if (number == o) return true;
        }
        return false;
    }
}
