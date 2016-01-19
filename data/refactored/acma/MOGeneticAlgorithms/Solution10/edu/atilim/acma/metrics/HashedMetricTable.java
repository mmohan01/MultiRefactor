package edu.atilim.acma.metrics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.atilim.acma.util.Log;

class HashedMetricTable implements MetricTable {
    private HashMap<String, HashMap<String, Double>> metrics;

    public HashedMetricTable() {
        metrics = new HashMap<String, HashMap<String, Double>>();
    }

    @Override
     public MetricRow row(Object key) {
        return new Row(key);
    }

    @Override
     public void set(Object key, String metric, double value) {
        HashMap<String, Double> inner = metrics.get(key.toString());
        if (inner == null) {
            inner = new HashMap<String, Double>();
            metrics.put(key.toString(), inner);
        }
        inner.put(metric, value);
    }

    @Override
     public double get(Object key, String metric) {
        HashMap<String, Double> inner = metrics.get(key.toString());
        if (inner == null || !inner.containsKey(metric)) return Double.NaN;
        return inner.get(metric);
    }

    @Override
     public List<String> getRows() {
        return new ArrayList<String>(metrics.keySet());
    }

    @Override
     public double getAverage(String metric) {
        double sum = 0;
        int cnt = 0;

        for (HashMap<String, Double> map: metrics.values()) {
            Double val = map.get(metric);
            if (val != null && !val.isNaN()) {
                sum += val;
                cnt++;
            }
        }

        if (cnt == 0) return 0;

        return sum / cnt;
    }

    @Override
     public MetricSummary getSummary() {
        return new MetricSummary("", this);
    }

    private class Row implements MetricRow {
        private Object key;

        private Row(Object key) {
            this.key = key;
        }

        @Override
         public void set(String metric, double value) {
            HashedMetricTable.this.set(key, metric, value);
        }

        @Override
         public void increase(String metric) {
            add(metric, 1.0);
        }

        @Override
         public double get(String metric) {
            return HashedMetricTable.this.get(key, metric);
        }

        @Override
         public void add(String metric, double value) {
            double current = HashedMetricTable.this.get(key, metric);
            if (Double.isNaN(current)) {
                current = 0;
            }
            set(metric, current + value);
        }
    }
}
