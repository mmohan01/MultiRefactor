package edu.atilim.acma.metrics;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import edu.atilim.acma.RunConfig;

public class MetricNormalizer {
    private static HashMap<UUID, double[][]> normalizationCache = new HashMap<UUID, double[][]>();

    public static void clearCache() {
        normalizationCache.clear();
    }

    public static double normalize(MetricSummary current, RunConfig config) {
        return weightedNormalize(current, config);
    }

    public static HashMap<String, Double> normalizeEach(MetricSummary current, RunConfig config) {
        // All metrics
        List<MetricRegistry.Entry> metrics = MetricRegistry.entries();
        int nummetrics = metrics.size();

        if (!normalizationCache.containsKey(config.getId())) {
            // Design set
            List<MetricSummary> designs = config.getNormalMetrics();
            int numdesigns = designs.size();

            // Table
            double[][] table = new double[nummetrics][numdesigns];

            for (int i = 0; i < nummetrics; i++) {
                MetricRegistry.Entry metric = metrics.get(i);

                for (int j = 0; j < numdesigns; j++) {
                    MetricSummary design = designs.get(j);
                    table[i][j] = design.get(metric.getName());
                }
            }

            normalizationCache.put(config.getId(), getMeansAndStDevs(table, nummetrics, numdesigns));
        }

        double[][] mn = normalizationCache.get(config.getId());

        HashMap<String, Double> items = new HashMap<String, Double>();

        for (int i = 0; i < nummetrics; i++) {
            MetricRegistry.Entry metric = metrics.get(i);

            if (!config.isMetricEnabled(metric.getName())) continue;

            double curmetric = current.get(metric.getName());

            if (Double.isNaN(curmetric) || Double.isNaN(mn[i][0]) || Double.isNaN(mn[i][1]) || mn[i][1] == 0.0) continue;

            if (metric.isMinimized()) {
                items.put(metric.getName(), Math.abs(curmetric));
            }
             else if (metric.isMaximized()) {
                items.put(metric.getName(), Math.abs(curmetric) * -1);
            }
             else {
                items.put(metric.getName(), Math.abs((curmetric - mn[i][0]) / mn[i][1]));
            }
        }

        return items;
    }

    private static double weightedNormalize(MetricSummary current, RunConfig config) {
        // All metrics
        List<MetricRegistry.Entry> metrics = MetricRegistry.entries();
        int nummetrics = metrics.size();

        if (!normalizationCache.containsKey(config.getId())) {
            // Design set
            List<MetricSummary> designs = config.getNormalMetrics();
            int numdesigns = designs.size();

            // Table
            double[][] table = new double[nummetrics][numdesigns];

            for (int i = 0; i < nummetrics; i++) {
                MetricRegistry.Entry metric = metrics.get(i);

                for (int j = 0; j < numdesigns; j++) {
                    MetricSummary design = designs.get(j);
                    table[i][j] = design.get(metric.getName());
                }
            }

            normalizationCache.put(config.getId(), getMeansAndStDevs(table, nummetrics, numdesigns));
        }

        double[][] mn = normalizationCache.get(config.getId());

        double normalvalue = 0;

        for (int i = 0; i < nummetrics; i++) {
            MetricRegistry.Entry metric = metrics.get(i);

            if (!config.isMetricEnabled(metric.getName())) continue;

            double curmetric = current.get(metric.getName());

            if (Double.isNaN(curmetric) || Double.isNaN(mn[i][0]) || Double.isNaN(mn[i][1]) || mn[i][1] == 0.0) continue;

            if (metric.isMinimized()) {
                normalvalue += Math.abs(curmetric) * config.getMetricWeight(metric.getName(), 1.0);
            }
             else if (metric.isMaximized()) {
                normalvalue -= Math.abs(curmetric) * config.getMetricWeight(metric.getName(), 1.0);
            }
             else {
                normalvalue += Math.abs((curmetric - mn[i][0]) / mn[i][1]) * config.getMetricWeight(metric.getName(), 1.0);
            }
        }

        return normalvalue;
    }

    @SuppressWarnings("unused")
     private static double unweightedNormalize(MetricSummary current, RunConfig config) {
        // All metrics
        List<MetricRegistry.Entry> metrics = MetricRegistry.entries();
        int nummetrics = metrics.size();

        if (!normalizationCache.containsKey(config.getId())) {
            // Design set
            List<MetricSummary> designs = config.getNormalMetrics();
            int numdesigns = designs.size();

            // Table
            double[][] table = new double[nummetrics][numdesigns];

            for (int i = 0; i < nummetrics; i++) {
                MetricRegistry.Entry metric = metrics.get(i);

                for (int j = 0; j < numdesigns; j++) {
                    MetricSummary design = designs.get(j);
                    table[i][j] = design.get(metric.getName());
                }
            }

            normalizationCache.put(config.getId(), getMeansAndStDevs(table, nummetrics, numdesigns));
        }

        double[][] mn = normalizationCache.get(config.getId());

        double normalvalue = 0;

        for (int i = 0; i < nummetrics; i++) {
            MetricRegistry.Entry metric = metrics.get(i);

            if (!config.isMetricEnabled(metric.getName())) continue;

            double curmetric = current.get(metric.getName());

            if (Double.isNaN(curmetric) || Double.isNaN(mn[i][0]) || Double.isNaN(mn[i][1]) || mn[i][1] == 0.0) continue;

            if (metric.isMinimized()) {
                normalvalue += Math.abs(curmetric);
            }
             else if (metric.isMaximized()) {
                normalvalue -= Math.abs(curmetric);
            }
             else {
                normalvalue += Math.abs((curmetric - mn[i][0]) / mn[i][1]);
            }
        }

        return normalvalue;
    }

    private static double[][] getMeansAndStDevs(double[][] table, int metrics, int designs) {
        double[][] result = new double[metrics][2];

        for (int i = 0; i < metrics; i++) {
            for (int j = 0; j < designs; j++) {
                result[i][0] += table[i][j];
            }
        }

        for (int i = 0; i < metrics; i++) {
            result[i][0] /= designs;
        }

        for (int i = 0; i < metrics; i++) {
            for (int j = 0; j < designs; j++) {
                double diff = table[i][j] - result[i][0];
                result[i][1] += diff * diff;
            }
        }

        for (int i = 0; i < metrics; i++) {
            result[i][1] = Math.sqrt(result[i][1] / designs);
        }

        return result;
    }
}
