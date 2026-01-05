package com.revolution.tools.roulette.controller;

import com.revolution.tools.roulette.entity.CustomRule;
import com.revolution.tools.roulette.service.CustomRuleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for managing custom betting rules.
 */
@Slf4j
@RestController
@RequestMapping("/api/roulette/rules")
@RequiredArgsConstructor
public class RouletteRulesController {

    private final CustomRuleService ruleService;

    /**
     * Initialize default rules.
     * Safe to call multiple times - won't create duplicates.
     *
     * POST /api/roulette/rules/initialize-defaults
     */
    @PostMapping("/initialize-defaults")
    public ResponseEntity<String> initializeDefaultRules() {
        log.info("Initializing default custom rules");
        ruleService.initializeDefaultRules();
        return ResponseEntity.ok("Default rules initialized successfully");
    }

    /**
     * Get all enabled rules.
     *
     * GET /api/roulette/rules
     */
    @GetMapping
    public ResponseEntity<List<CustomRule>> getAllRules() {
        log.debug("Fetching all enabled rules");
        List<CustomRule> rules = ruleService.getAllEnabledRules();
        return ResponseEntity.ok(rules);
    }

    /**
     * Get rule by ID.
     *
     * GET /api/roulette/rules/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<CustomRule> getRuleById(@PathVariable Long id) {
        log.debug("Fetching rule ID: {}", id);
        CustomRule rule = ruleService.getRuleById(id);
        return ResponseEntity.ok(rule);
    }

    /**
     * Create a new custom rule.
     *
     * POST /api/roulette/rules
     * {
     *   "name": "My Custom Rule",
     *   "description": "Description of the pattern",
     *   "ruleType": "PAIR",
     *   "triggerNumbers": "15",
     *   "suggestedNumbers": "28,33",
     *   "confidence": 70,
     *   "enabled": true
     * }
     */
    @PostMapping
    public ResponseEntity<CustomRule> createRule(@Valid @RequestBody CustomRule rule) {
        log.info("Creating new custom rule: {}", rule.getName());
        CustomRule savedRule = ruleService.saveRule(rule);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedRule);
    }

    /**
     * Update an existing rule.
     *
     * PUT /api/roulette/rules/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<CustomRule> updateRule(
            @PathVariable Long id,
            @Valid @RequestBody CustomRule rule) {
        log.info("Updating rule ID: {}", id);
        rule.setId(id); // Ensure ID matches path
        CustomRule updatedRule = ruleService.saveRule(rule);
        return ResponseEntity.ok(updatedRule);
    }

    /**
     * Enable or disable a rule.
     *
     * PATCH /api/roulette/rules/{id}/toggle?enabled=true
     */
    @PatchMapping("/{id}/toggle")
    public ResponseEntity<CustomRule> toggleRule(
            @PathVariable Long id,
            @RequestParam Boolean enabled) {
        log.info("Toggling rule ID {} to enabled={}", id, enabled);
        CustomRule updatedRule = ruleService.toggleRule(id, enabled);
        return ResponseEntity.ok(updatedRule);
    }

    /**
     * Delete a rule.
     *
     * DELETE /api/roulette/rules/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteRule(@PathVariable Long id) {
        log.info("Deleting rule ID: {}", id);
        ruleService.deleteRule(id);
        return ResponseEntity.noContent().build();
    }
}
