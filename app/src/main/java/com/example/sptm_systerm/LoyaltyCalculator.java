package com.example.sptm_systerm;

public class LoyaltyCalculator {

    public static double calculateLoyaltyPoints(double lastMonthBill, double currentMonthBill) {
        if (lastMonthBill <= 0 || currentMonthBill <= 0) {
            return 0; // No loyalty if bills are invalid
        }

        // Calculate bill reduction percentage
        double billReduction = lastMonthBill - currentMonthBill;
        double efficiencyScore = (billReduction / lastMonthBill) * 100;

        // Determine reward percentage based on efficiency score
        double rewardPercentage = 0;

        if (efficiencyScore > 20) {
            if (currentMonthBill < 500) {
                rewardPercentage = 5;
            } else if (currentMonthBill >= 500 && currentMonthBill <= 2000) {
                rewardPercentage = 7;
            } else {
                rewardPercentage = 10;
            }
        } else if (efficiencyScore >= 10) {
            if (currentMonthBill < 500) {
                rewardPercentage = 3;
            } else if (currentMonthBill >= 500 && currentMonthBill <= 2000) {
                rewardPercentage = 5;
            } else {
                rewardPercentage = 7;
            }
        } else if (efficiencyScore >= 5) {
            if (currentMonthBill < 500) {
                rewardPercentage = 1;
            } else if (currentMonthBill >= 500 && currentMonthBill <= 2000) {
                rewardPercentage = 2;
            } else {
                rewardPercentage = 4;
            }
        } else { // efficiencyScore < 5
            if (currentMonthBill < 500) {
                return 0.1; // Flat 0.1 LKR
            } else if (currentMonthBill >= 500 && currentMonthBill <= 2000) {
                return 0.5; // Flat 0.5 LKR
            } else {
                return 1; // Flat 1 LKR
            }
        }

        // Calculate loyalty points
        double loyaltyPoints = (rewardPercentage / 100) * currentMonthBill;
        return loyaltyPoints;
    }
}
