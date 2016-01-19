package edu.atilim.acma;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import edu.atilim.acma.metrics.MetricSummary;

public class RunConfig implements Externalizable {
	
	public static RunConfig getDefault() {
		RunConfig rc = new RunConfig();
		rc.name = "Default";
		return rc;
	}
	
	private String name;
	private UUID uuid;
	private HashMap<String, MetricOverride> metricOverrides;
	private HashMap<String, ActionOverride> actionOverrides;
	private ArrayList<MetricSummary> normalMetrics;
	
	private boolean usingWeightedSum;
	
	public UUID getId() {
		return uuid;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public boolean isUsingWeightedSum() {
		return usingWeightedSum;
	}

	public void setUsingWeightedSum(boolean usingWeightedSum) {
		this.usingWeightedSum = usingWeightedSum;
	}

	public RunConfig() {
		metricOverrides = new HashMap<String, RunConfig.MetricOverride>();
		actionOverrides = new HashMap<String, RunConfig.ActionOverride>();
		normalMetrics = new ArrayList<MetricSummary>();
		uuid = UUID.randomUUID();
		usingWeightedSum = false;
	}
	
	public boolean isActionEnabled(String name) {
		ActionOverride ao = actionOverrides.get(name);
		return ao == null || ao.enabled;
	}
	
	public void setActionEnabled(String name, boolean enabled) {
		ActionOverride mo = actionOverrides.get(name);
		if (mo == null) {
			mo = new ActionOverride();
			mo.weight = Double.NaN;
			mo.actionName = name;
			actionOverrides.put(name, mo);
		}
		mo.enabled = enabled;
	}
	
	public double getActionWeight(String name) {
		ActionOverride mo = actionOverrides.get(name);
		if (mo == null) return 1.0;
		return Double.isNaN(mo.weight) ? 1.0 : mo.weight;
	}
	
	public void setActionWeight(String name, double value) {
		ActionOverride mo = actionOverrides.get(name);
		if (mo == null) {
			mo = new ActionOverride();
			mo.enabled = true;
			mo.actionName = name;
			actionOverrides.put(name, mo);
		}
		mo.weight = value;
	}
	
	public boolean isMetricEnabled(String name) {
		MetricOverride mo = metricOverrides.get(name);
		return mo == null || mo.enabled;
	}
	
	public void setMetricEnabled(String name, boolean value) {
		MetricOverride mo = metricOverrides.get(name);
		if (mo == null) {
			mo = new MetricOverride();
			mo.weight = Double.NaN;
			mo.metricName = name;
			metricOverrides.put(name, mo);
		}
		mo.enabled = value;
	}
	
	public double getMetricWeight(String name, double def) {
		MetricOverride mo = metricOverrides.get(name);
		if (mo == null) return def;
		return Double.isNaN(mo.weight) ? def : mo.weight;
	}
	
	public void setMetricWeight(String name, double value) {
		MetricOverride mo = metricOverrides.get(name);
		if (mo == null) {
			mo = new MetricOverride();
			mo.enabled = true;
			mo.metricName = name;
			metricOverrides.put(name, mo);
		}
		mo.weight = value;
	}
	
	public List<MetricSummary> getNormalMetrics() {
		return normalMetrics;
	}
	
	private class MetricOverride {
		private String metricName;
		private double weight;
		private boolean enabled;
	}
	
	private class ActionOverride {
		private String actionName;
		private double weight;
		private boolean enabled;
	}
	
	@Override
	public String toString() {
		return name;
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		
		in.readInt(); //version
		
		name = in.readUTF();
		uuid = (UUID)in.readObject();
		usingWeightedSum = in.readBoolean();
		
		int mocnt = in.readInt();
		for (int i = 0; i < mocnt; i++) {
			MetricOverride mo = new MetricOverride();
			mo.metricName = in.readUTF();
			mo.weight = in.readDouble();
			mo.enabled = in.readBoolean();
			metricOverrides.put(mo.metricName, mo);
		}
		
		int aocnt = in.readInt();
		for (int i = 0; i < aocnt; i++) {
			ActionOverride mo = new ActionOverride();
			mo.actionName = in.readUTF();
			mo.weight = in.readDouble();
			mo.enabled = in.readBoolean();
			actionOverrides.put(mo.actionName, mo);
		}
		
		int nmcnt = in.readInt();
		for (int i = 0; i < nmcnt; i++) {
			normalMetrics.add((MetricSummary)in.readObject());
		}
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(0); //version
		
		out.writeUTF(name);
		out.writeObject(uuid);
		out.writeBoolean(usingWeightedSum);
		
		out.writeInt(metricOverrides.size());
		for (MetricOverride mo : metricOverrides.values()) {
			out.writeUTF(mo.metricName);
			out.writeDouble(mo.weight);
			out.writeBoolean(mo.enabled);
		}
		
		out.writeInt(actionOverrides.size());
		for (ActionOverride ao : actionOverrides.values()) {
			out.writeUTF(ao.actionName);
			out.writeDouble(ao.weight);
			out.writeBoolean(ao.enabled);
		}
		
		out.writeInt(normalMetrics.size());
		for (MetricSummary ms : normalMetrics) {
			out.writeObject(ms);
		}
	}
}
