package com.example.sptm_systerm;

public class ElectricityBillCalculator {

    public static double calculateBill(double consumption, boolean isNewTariff) {
        double totalBill = 0.0;
        double fixedCharge = 0.0;

        // Apply tariffs based on consumption and whether the new tariff applies
        if (consumption <= 60) {
            // Block for consumption 0-60 kWh per month
            if (consumption <= 30) {
                totalBill = consumption * (isNewTariff ? 4.00 : 6.00);
                fixedCharge = isNewTariff ? 75.00 : 100.00;
            } else {
                totalBill = (30 * (isNewTariff ? 4.00 : 6.00)) +
                        ((consumption - 30) * (isNewTariff ? 6.00 : 9.00));
                fixedCharge = isNewTariff ? 200.00 : 250.00;
            }
        } else {
            // Block for consumption above 60 kWh per month
            if (consumption <= 60) {
                totalBill = consumption * (isNewTariff ? 11.00 : 15.00);
                fixedCharge = 0; // No fixed charge mentioned in the image for this range
            } else if (consumption <= 90) {
                totalBill = (60 * (isNewTariff ? 11.00 : 15.00)) +
                        ((consumption - 60) * (isNewTariff ? 14.00 : 18.00));
                fixedCharge = 400.00;
            } else if (consumption <= 120) {
                totalBill = (60 * (isNewTariff ? 11.00 : 15.00)) +
                        (30 * (isNewTariff ? 14.00 : 18.00)) +
                        ((consumption - 90) * (isNewTariff ? 20.00 : 30.00));
                fixedCharge = 1000.00;
            } else if (consumption <= 180) {
                totalBill = (60 * (isNewTariff ? 11.00 : 15.00)) +
                        (30 * (isNewTariff ? 14.00 : 18.00)) +
                        (30 * (isNewTariff ? 20.00 : 30.00)) +
                        ((consumption - 120) * (isNewTariff ? 33.00 : 42.00));
                fixedCharge = 1500.00;
            } else {
                totalBill = (60 * (isNewTariff ? 11.00 : 15.00)) +
                        (30 * (isNewTariff ? 14.00 : 18.00)) +
                        (30 * (isNewTariff ? 20.00 : 30.00)) +
                        (60 * (isNewTariff ? 33.00 : 42.00)) +
                        ((consumption - 180) * (isNewTariff ? 52.00 : 65.00));
                fixedCharge = 2000.00;
            }
        }

        return totalBill + fixedCharge;
    }
}

