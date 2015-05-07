/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package dbtool.generator;

import java.util.Random;

/**
 * Represents a range with a min and max value.
 *
 * @author Koen
 */
public class Range {

    private int min = 0;
    private int max = 100;

    /**
     * Get the value of min
     *
     * @return the value of min
     */
    public int getMin() {
        return min;
    }

    /**
     * Set the value of min
     *
     * @param min new value of min
     */
    public void setMin(int min) {
        this.min = min;
    }

    /**
     * Get the value of max
     *
     * @return the value of max
     */
    public int getMax() {
        return max;
    }

    /**
     * Set the value of max
     *
     * @param max new value of max
     */
    public void setMax(int max) {
        this.max = max;
    }

    /**
     * Specifies a range between the two given limits.
     *
     * @param min
     * @param max
     */
    public Range(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public int getIntegerInRange(Random random) {
        return random.nextInt(getMax() - getMin()) + getMin();
    }

}
