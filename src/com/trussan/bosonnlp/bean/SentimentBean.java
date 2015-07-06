package com.trussan.bosonnlp.bean;

/**
 * Created by Jarod Yv @7/5/15 1:09 PM
 */
public class SentimentBean {

    private double positive;
    private double negative;

    public SentimentBean(double positive, double negative) {
        this.positive = positive;
        this.negative = negative;
    }

    public double getNegative() {
        return negative;
    }

    public void setNegative(double negative) {
        this.negative = negative;
    }

    public double getPositive() {
        return positive;
    }

    public void setPositive(double positive) {
        this.positive = positive;
    }
}
