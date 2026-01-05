package com.revolution.tools.roulette.repository;

import com.revolution.tools.roulette.entity.CustomRule;
import com.revolution.tools.roulette.enums.RuleType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * Repository for CustomRule entities.
 */
@Repository
public interface CustomRuleRepository extends JpaRepository<CustomRule, Long> {

    /**
     * Find all enabled rules.
     */
    List<CustomRule> findByEnabledTrue();

    /**
     * Find enabled rules by type.
     */
    List<CustomRule> findByRuleTypeAndEnabledTrue(RuleType ruleType);

    /**
     * Find rule by name.
     */
    Optional<CustomRule> findByName(String name);

    /**
     * Find rules containing specific trigger number.
     */
    List<CustomRule> findByTriggerNumbersContainingAndEnabledTrue(String triggerNumber);

    /**
     * Find all rules ordered by hit rate descending.
     */
    List<CustomRule> findByEnabledTrueOrderByHitCountDesc();
}
