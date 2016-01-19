package edu.atilim.acma.ui;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import edu.atilim.acma.RunConfig;
import edu.atilim.acma.metrics.MetricNormalizer;
import edu.atilim.acma.metrics.MetricRegistry;
import edu.atilim.acma.metrics.MetricSummary;
import edu.atilim.acma.transition.ActionRegistry;
import edu.atilim.acma.util.ACMAUtil;
import edu.atilim.acma.util.Log;

public final class ConfigManager {
    static {
        load();
    }

    public static final String CONFIG_CHANGED = "configChanged";

    private static ArrayList<RunConfig> runConfigs;

    public static List<RunConfig> runConfigs() {
        return runConfigs;
    }

    protected

     static void saveChanges() {
        save();

        MainWindow.getInstance().fireEvent(CONFIG_CHANGED);
    }

    static void discardChanges() {
        load();
    }

    static void add(RunConfig config) {
        runConfigs.add(config);
    }

    static void add(MetricSummary normal) {
        for (RunConfig rc: runConfigs) {
            rc.getNormalMetrics().add(normal);
        }
        MetricNormalizer.clearCache();
    }

    static void remove(RunConfig config) {
        runConfigs.remove(config);
    }

    public static RunConfig getRunConfig(String name) {
        for (RunConfig rc: runConfigs) {
            if (rc.getName().equals(name))
                return rc;
        }
        return null;
    }

    public static List<Action> getActions(RunConfig config) {
        Set<ActionRegistry.Entry> actions = ActionRegistry.entries();
        List<Action> list = new ArrayList<Action>();

        for (ActionRegistry.Entry e: actions) {
            list.add(new Action(config, e.getName()));
        }

        return Collections.unmodifiableList(list);
    }

    public static List<Metric> getMetrics(RunConfig config) {
        List<MetricRegistry.Entry> metrics = MetricRegistry.entries();
        List<Metric> list = new ArrayList<Metric>();

        for (MetricRegistry.Entry e: metrics) {
            list.add(new Metric(config, e.getName(), e.getWeight(), e.isPackageMetric(), e.isMinimized(), e.isMaximized()));
        }

        return Collections.unmodifiableList(list);
    }

    static List<NormalMetric> getNormalMetrics(RunConfig config) {
        List<NormalMetric> list = new ArrayList<NormalMetric>();

        for (MetricSummary ms: config.getNormalMetrics()) {
            list.add(new NormalMetric(config, ms.getName(), ms.getUuid()));
        }

        return Collections.unmodifiableList(list);
    }

    public static class Action {
        private RunConfig config;
        private String name;

        public boolean isEnabled() {
            return config.isActionEnabled(name);
        }

        public void setEnabled(boolean enabled) {
            config.setActionEnabled(name, enabled);
        }

        public double getWeight() {
            return config.getActionWeight(name);
        }

        public void setWeight(double weight) {
            config.setActionWeight(name, weight);
        }

        public String getName() {
            return name;
        }

        private Action(RunConfig config, String name) {
            this.config = config;
            this.name = name;
        }

        @Override
         public String toString() {
            return ACMAUtil.splitCamelCase(name);
        }
    }

    public static class Metric {
        private RunConfig config;
        private String name;
        private double weight;
        private boolean packageMetric;
        private boolean minimized;
        private boolean maximized;

        public String getName() {
            return name;
        }

        public double getWeight() {
            return config.getMetricWeight(name, weight);
        }

        public void setWeight(double weight) {
            config.setMetricWeight(name, weight);
        }

        public boolean isEnabled() {
            return config.isMetricEnabled(name);
        }

        public void setEnabled(boolean enabled) {
            config.setMetricEnabled(name, enabled);
        }

        public boolean isPackageMetric() {
            return packageMetric;
        }

        public boolean isMinimized() {
            return minimized;
        }

        public boolean isMaximized() {
            return maximized;
        }

        private Metric(RunConfig config, String name, double weight, boolean packageMetric, boolean minimized, boolean maximized) {
            this.config = config;
            this.name = name;
            this.weight = weight;
            this.packageMetric = packageMetric;
            this.minimized = minimized;
            this.maximized = maximized;
        }
    }

    static class NormalMetric {
        private RunConfig config;
        private UUID id;
        private String name;

        public void remove() {
            List<MetricSummary> mslist = config.getNormalMetrics();
            for (MetricSummary ms: mslist) {
                if (ms.getUuid().equals(id)) {
                    mslist.remove(ms);
                    return;
                }
            }
            MetricNormalizer.clearCache();
        }

        public UUID getID() {
            return id;
        }

        public String getName() {
            return name;
        }

        private NormalMetric(RunConfig config, String name, UUID id) {
            this.config = config;
            this.name = name;
            this.id = id;
        }
    }

    private static void load() {
        runConfigs = new ArrayList<RunConfig>();

        ObjectInputStream ois = null;
        try {
            ois = new ObjectInputStream(new FileInputStream("./data/user/user.config"));

            int rccnt = ois.readInt();
            for (int i = 0; i < rccnt; i++) {
                runConfigs.add((RunConfig)ois.readObject());
            }
        } catch (Exception e) {
            Log.severe("Could not load user configuration. Falling back to default.");
            runConfigs = new ArrayList<RunConfig>();
            runConfigs.add(RunConfig.getDefault());
        } finally {
            if (ois != null) {
                try { ois.close(); } catch (IOException e) {}
            }
        }
    }

    private static void save() {
        ObjectOutputStream oos = null;
        try {
            oos = new ObjectOutputStream(new FileOutputStream("./data/user/user.config"));

            oos.writeInt(runConfigs.size());
            for (int i = 0; i < runConfigs.size(); i++)
                oos.writeObject(runConfigs.get(i));
        } catch (Exception e) {
            Log.severe("Could not save user configuration!");
        } finally {
            if (oos != null) {
                try { oos.flush(); oos.close();
                } catch (IOException e) {}
            }
        }
    }
}
