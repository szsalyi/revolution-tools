package com.revolution.tools.roulette.repository;

import com.revolution.tools.roulette.entity.RouletteSpin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Repository for RouletteSpin entities.
 */
@Repository
public interface RouletteSpinRepository extends JpaRepository<RouletteSpin, Long> {

    /**
     * Finds all spins for a session, ordered by sequence.
     */
    List<RouletteSpin> findBySessionIdOrderBySequenceNumberAsc(Long sessionId);

    /**
     * Finds the most recent N spins for a session.
     */
    @Query("SELECT s FROM RouletteSpin s WHERE s.sessionId = :sessionId ORDER BY s.sequenceNumber DESC")
    List<RouletteSpin> findRecentSpins(@Param("sessionId") Long sessionId);

    /**
     * Finds the last N spins for a session.
     */
    @Query(value = "SELECT * FROM roulette_spins WHERE session_id = :sessionId ORDER BY sequence_number DESC LIMIT :limit", nativeQuery = true)
    List<RouletteSpin> findLastNSpins(@Param("sessionId") Long sessionId, @Param("limit") int limit);

    /**
     * Counts total spins for a session.
     */
    Long countBySessionId(Long sessionId);

    /**
     * Finds spins by number for a session (for frequency analysis).
     */
    List<RouletteSpin> findBySessionIdAndSpinNumber(Long sessionId, Integer spinNumber);

    /**
     * Counts how many times a specific number appeared in a session.
     */
    Long countBySessionIdAndSpinNumber(Long sessionId, Integer spinNumber);
}
