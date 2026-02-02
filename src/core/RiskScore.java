package com.fortis.core;

import java.util.HashMap;
import java.util.Map;

/**
 * Risk score calculation result with detailed breakdown
 */
public class RiskScore {
    private final int totalScore;
    private final Map<String, Integer> factorScores;
    private final RiskLevel riskLevel;
    private final String recommendation;
    
    public enum RiskLevel {
        LOW(0, 30, "Low risk - proceed"),
        MEDIUM(31, 70, "Medium risk - review recommended"),
        HIGH(71, 100, "High risk - automatic rollback");
        
        private final int minScore;
        private final int maxScore;
        private final String description;
        
        RiskLevel(int minScore, int maxScore, String description) {
            this.minScore = minScore;
            this.maxScore = maxScore;
            this.description = description;
        }
        
        public static RiskLevel fromScore(int score) {
            if (score <= 30) return LOW;
            if (score <= 70) return MEDIUM;
            return HIGH;
        }
        
        public String getDescription() {
            return description;
        }
    }
    
    public RiskScore(int totalScore, Map<String, Integer> factorScores) {
        this.totalScore = Math.min(100, Math.max(0, totalScore));
        this.factorScores = new HashMap<>(factorScores);
        this.riskLevel = RiskLevel.fromScore(this.totalScore);
        this.recommendation = generateRecommendation();
    }
    
    private String generateRecommendation() {
        StringBuilder sb = new StringBuilder();
        sb.append(riskLevel.getDescription()).append(". ");
        
        if (riskLevel == RiskLevel.HIGH) {
            sb.append("Transaction will be automatically rolled back. ");
        }
        
        // Add specific factor warnings
        if (factorScores.getOrDefault("amount", 0) > 25) {
            sb.append("High transaction amount. ");
        }
        if (factorScores.getOrDefault("frequency", 0) > 20) {
            sb.append("Unusual transaction frequency. ");
        }
        if (factorScores.getOrDefault("velocity", 0) > 20) {
            sb.append("Rapid successive transfers detected. ");
        }
        
        return sb.toString().trim();
    }
    
    public int getTotalScore() {
        return totalScore;
    }
    
    public Map<String, Integer> getFactorScores() {
        return new HashMap<>(factorScores);
    }
    
    public RiskLevel getRiskLevel() {
        return riskLevel;
    }
    
    public String getRecommendation() {
        return recommendation;
    }
    
    public boolean shouldRollback() {
        return riskLevel == RiskLevel.HIGH;
    }
    
    public String getDetailedBreakdown() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("Total Risk Score: %d/100 (%s)\n", totalScore, riskLevel));
        sb.append("Factor Breakdown:\n");
        factorScores.forEach((factor, score) -> 
            sb.append(String.format("  - %s: %d\n", factor, score))
        );
        sb.append("Recommendation: ").append(recommendation);
        return sb.toString();
    }
    
    @Override
    public String toString() {
        return String.format("RiskScore[Total: %d, Level: %s]", totalScore, riskLevel);
    }
}
