package com.tudoreloprisan.licenta.timelapse.model;

import java.util.List;

/**
 * Created by Doru on 6/25/2016.
 */
public class Aperture {
    private double currentFNumber;
    private List<Double> availableFNumbers;



    public Aperture() {
    }

    public double getCurrentFNumber() {
        return currentFNumber;
    }

    public void setCurrentFNumber(double currentFNumber) {
        this.currentFNumber = currentFNumber;
    }

    public List<Double> getAvailableFNumbers() {
        return availableFNumbers;
    }

    public void setAvailableFNumbers(List<Double> availableFNumbers) {
        this.availableFNumbers = availableFNumbers;
    }
}
