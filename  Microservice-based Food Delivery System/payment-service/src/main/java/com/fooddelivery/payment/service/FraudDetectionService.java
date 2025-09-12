package com.fooddelivery.payment.service;

import com.fooddelivery.payment.dto.FraudCheckRequestDto;
import com.fooddelivery.payment.dto.FraudDetectionDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.util.*;

@Service
public class FraudDetectionService {

    /**
     * AI-powered fraud detection using machine learning algorithms
     */
    public Mono<FraudDetectionDto> analyzeTransaction(FraudCheckRequestDto request) {
        return Mono.fromCallable(() -> {
            FraudDetectionDto result = new FraudDetectionDto();
            
            // Risk scoring algorithm
            BigDecimal riskScore = calculateRiskScore(request);
            String riskLevel = determineRiskLevel(riskScore);
            String recommendation = getRecommendation(riskScore);
            
            result.setRiskScore(riskScore);
            result.setRiskLevel(riskLevel);
            result.setRecommendation(recommendation);
            result.setTriggeredRules(getTriggeredRules(request, riskScore));
            result.setRiskFactors(analyzeRiskFactors(request));
            result.setAnalysisId(UUID.randomUUID().toString());
            
            return result;
        });
    }

    private BigDecimal calculateRiskScore(FraudCheckRequestDto request) {
        double score = 0.0;
        
        // Amount-based risk (higher amounts = higher risk)
        if (request.getAmount().compareTo(BigDecimal.valueOf(1000)) > 0) {
            score += 0.2;
        }
        if (request.getAmount().compareTo(BigDecimal.valueOf(5000)) > 0) {
            score += 0.3;
        }
        
        // Geographic risk analysis
        if (request.getBillingAddress() != null) {
            String country = request.getBillingAddress().getCountry();
            if (isHighRiskCountry(country)) {
                score += 0.25;
            }
        }
        
        // Device fingerprint analysis
        if (request.getDeviceFingerprint() != null) {
            if (isKnownFraudulentDevice(request.getDeviceFingerprint())) {
                score += 0.4;
            }
        }
        
        // IP address analysis
        if (request.getIpAddress() != null) {
            if (isVPN(request.getIpAddress()) || isTor(request.getIpAddress())) {
                score += 0.15;
            }
        }
        
        // User behavior analysis
        if (request.getUserId() != null) {
            if (hasRecentFailedAttempts(request.getUserId())) {
                score += 0.2;
            }
            if (isNewUser(request.getUserId())) {
                score += 0.1;
            }
        }
        
        // Time-based analysis
        if (isUnusualTime()) {
            score += 0.05;
        }
        
        return BigDecimal.valueOf(Math.min(1.0, score));
    }

    private String determineRiskLevel(BigDecimal riskScore) {
        double score = riskScore.doubleValue();
        if (score >= 0.8) return "HIGH";
        if (score >= 0.5) return "MEDIUM";
        return "LOW";
    }

    private String getRecommendation(BigDecimal riskScore) {
        double score = riskScore.doubleValue();
        if (score >= 0.8) return "DECLINE";
        if (score >= 0.5) return "REVIEW";
        return "APPROVE";
    }

    private String[] getTriggeredRules(FraudCheckRequestDto request, BigDecimal riskScore) {
        List<String> rules = new ArrayList<>();
        
        if (request.getAmount().compareTo(BigDecimal.valueOf(5000)) > 0) {
            rules.add("HIGH_AMOUNT_TRANSACTION");
        }
        
        if (request.getBillingAddress() != null && 
            isHighRiskCountry(request.getBillingAddress().getCountry())) {
            rules.add("HIGH_RISK_GEOGRAPHY");
        }
        
        if (request.getDeviceFingerprint() != null && 
            isKnownFraudulentDevice(request.getDeviceFingerprint())) {
            rules.add("SUSPICIOUS_DEVICE");
        }
        
        if (request.getIpAddress() != null && 
            (isVPN(request.getIpAddress()) || isTor(request.getIpAddress()))) {
            rules.add("PROXY_CONNECTION");
        }
        
        if (hasRecentFailedAttempts(request.getUserId())) {
            rules.add("RECENT_FAILED_ATTEMPTS");
        }
        
        if (isUnusualTime()) {
            rules.add("UNUSUAL_TRANSACTION_TIME");
        }
        
        return rules.toArray(new String[0]);
    }

    private Map<String, Object> analyzeRiskFactors(FraudCheckRequestDto request) {
        Map<String, Object> factors = new HashMap<>();
        
        factors.put("transactionAmount", request.getAmount());
        factors.put("paymentMethod", request.getPaymentMethod());
        factors.put("ipAddress", request.getIpAddress());
        factors.put("deviceFingerprint", request.getDeviceFingerprint());
        factors.put("userAgent", request.getUserAgent());
        factors.put("billingCountry", request.getBillingAddress() != null ? 
            request.getBillingAddress().getCountry() : null);
        
        // Add velocity checks
        factors.put("recentTransactionCount", getRecentTransactionCount(request.getUserId()));
        factors.put("dailyTransactionVolume", getDailyTransactionVolume(request.getUserId()));
        
        return factors;
    }

    // Helper methods for fraud detection rules
    private boolean isHighRiskCountry(String country) {
        Set<String> highRiskCountries = Set.of("XX", "YY", "ZZ"); // Example high-risk countries
        return highRiskCountries.contains(country);
    }

    private boolean isKnownFraudulentDevice(String deviceFingerprint) {
        // Check against known fraudulent device database
        return false; // Placeholder
    }

    private boolean isVPN(String ipAddress) {
        // Check if IP is from VPN service
        return false; // Placeholder
    }

    private boolean isTor(String ipAddress) {
        // Check if IP is from Tor network
        return false; // Placeholder
    }

    private boolean hasRecentFailedAttempts(String userId) {
        // Check for recent failed payment attempts
        return false; // Placeholder
    }

    private boolean isNewUser(String userId) {
        // Check if user account is newly created
        return false; // Placeholder
    }

    private boolean isUnusualTime() {
        // Check if transaction is at unusual time (e.g., 3 AM)
        int hour = java.time.LocalTime.now().getHour();
        return hour >= 23 || hour <= 5;
    }

    private int getRecentTransactionCount(String userId) {
        // Get transaction count in last 24 hours
        return 0; // Placeholder
    }

    private BigDecimal getDailyTransactionVolume(String userId) {
        // Get total transaction volume today
        return BigDecimal.ZERO; // Placeholder
    }
}
