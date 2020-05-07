package com.example.ndvi.activity;

import com.orhanobut.logger.Logger;

import java.util.List;

public class Threshold {

    /**
     * 阈值使用需要 ( >= threshold)( < threshold)
     *
     * @param d1
     * @param d2
     * @return
     */
    public Double calculateThreshold(List<Double> d1, List<Double> d2) {
        double threshold = 0;
        double avg1 = getAverage(d1);
        double avg2 = getAverage(d2);
        Logger.d("calculateThreshold  avg1----" + avg1);
        Logger.d("calculateThreshold  avg2----" + avg2);
        if (avg1 < avg2) {
            double max1 = getMax(d1, 1);
            double min2 = getMin(d2, 0);
            Logger.d("avg1 < avg2  max1----" + max1);
            Logger.d("avg1 < avg2  min2----" + min2);
            while (max1 >= min2) {
                max1 = getMax(d1, max1);
                min2 = getMin(d2, min2);
            }
            threshold = min2;
            Logger.d("threshold = min2----" + threshold);

        } else if (avg1 > avg2) {
            double max2 = getMax(d2, 1);
            double min1 = getMin(d1, 0);
            Logger.d("avg1 > avg2 max2----" + max2);
            Logger.d("avg1 > avg2  min1----" + min1);
            while (max2 >= min1) {
                max2 = getMax(d2, max2);
                min1 = getMin(d1, min1);
            }
            threshold = min1;
            Logger.d("avg1 > avg2  threshold----" + threshold);

        } else {
            Logger.d("threshold = avg1----" + avg1);
            threshold = avg1;
        }

        return threshold;
    }

    public Double getMax(List<Double> d, double exclude) {
        double max = 0;
        int length = d.size();
        for (int i = 0; i < length; i++)
            if (Math.abs(d.get(i)) < exclude && Math.abs(d.get(i)) > max) max = Math.abs(d.get(i));
        return max;
    }

    public Double getMin(List<Double> d, double exclude) {
        double min = 2;
        int length = d.size();
        for (int i = 0; i < length; i++)
            if (Math.abs(d.get(i)) > exclude && Math.abs(d.get(i)) < min) min = Math.abs(d.get(i));
        return min;
    }

    public Double getAverage(List<Double> d) {
        double avg = 0;
        double sum = 0;
        int length = d.size();

        for (int i = 0; i < length; i++) {
            sum += d.get(i);
        }
        Logger.d("length---" + length);
        Logger.d("sum---" + sum);
        avg = sum / length;
        return avg;
    }
}