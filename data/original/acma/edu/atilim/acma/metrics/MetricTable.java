package edu.atilim.acma.metrics;

import java.util.List;


public interface MetricTable {
	public MetricRow row(Object key);
	
	public List<String> getRows();
	
	public void set(Object key, String metric, double value);
	public double get(Object key, String metric);
	
	public double getAverage(String metric);
	
	public MetricSummary getSummary();
	
	public interface MetricRow {
		public void set(String metric, double value);
		public void increase(String metric);
		public void add(String metric, double value);
		public double get(String metric);
	}
}
