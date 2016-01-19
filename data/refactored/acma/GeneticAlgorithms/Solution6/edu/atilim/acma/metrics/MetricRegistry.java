package edu.atilim.acma.metrics;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.atilim.acma.util.Log;

public final class MetricRegistry {
    static {
        initialize();
    }

    private static HashMap<String, Entry> entries;

    public static List<Entry> entries() {
        return Collections.unmodifiableList(new ArrayList<Entry>(entries.values()));
    }

    private static void initialize() {
        entries = new HashMap<String, MetricRegistry.Entry>();

        try {
            Log.config("Loading metrics.xml for metric configuration."
            /* Equivalent of following three beautiful Java lines... in C#:
			 * XDocument.Load("data/metrics.xml"); */);
            DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
            Document doc = docBuilder.parse(new File("./data/metrics.xml"));

            NodeList categories = doc.getElementsByTagName("category");
            for (int i = 0; i < categories.getLength(); i++) {
                Node category = categories.item(i);

                // Equivalent of this, in C#, would be: category.Attribute("name").Value
                String className = category.getAttributes().getNamedItem("class").getTextContent();
                // String name = category.getAttributes().getNamedItem("name").getTextContent(); // what are we gonna do with this?

                MetricCalculator.
                registerType(className);

                // BTW, I'm still wondering why this "NodeList" does not implement any of the Collection interfaces.
                // Or at least Iterable!
                NodeList cmetrics = ((Element)category).getElementsByTagName("metric");

                for (int j = 0; j < cmetrics.getLength(); j++) {
                    Node metric = cmetrics.item(j);

                    String mname = metric.getAttributes().getNamedItem("name").getTextContent();
                    String mweight = metric.getAttributes().getNamedItem("weight").getTextContent();
                    // C# has TryParse methods on these... In case you don't want to try and catch stuff all the time.
                    double dmweight = Double.parseDouble(mweight);
                    boolean pmetric = metric.getAttributes().getNamedItem("packagemetric") != null;
                    boolean minimize = metric.getAttributes().getNamedItem("minimize") != null;
                    boolean maximize = metric.getAttributes().getNamedItem("maximize") != null;

                    entries.put(mname, new Entry(mname, dmweight, pmetric, minimize, maximize));
                }
            }

            Log.config("Found %d defined metrics.", entries.size());
        } catch (Exception e) {
            System.out.println("Could not initialize metric calculator. Details:");
            e.printStackTrace();
        }
    }

    public static class Entry {
        private String name;
        private double weight;
        private boolean packageMetric;
        private boolean minimized;
        private boolean maximized;

        public double getWeight() {
            return weight;
        }

        public String getName() {
            return name;
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

        private Entry(String name, double weight, boolean packageMetric, boolean minimized, boolean maximized) {
            this.name = name;
            this.weight = weight;
            this.packageMetric = packageMetric;
            this.minimized = minimized;
            this.maximized = maximized;
        }

        @Override
         public int hashCode() {
            return name.hashCode();
        }

        @Override
         public boolean equals(Object obj) {
            return obj != null && obj instanceof Entry && ((Entry)obj).name.equals(name);
        }
    }
}
