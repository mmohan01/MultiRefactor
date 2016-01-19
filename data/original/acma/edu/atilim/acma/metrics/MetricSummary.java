package edu.atilim.acma.metrics;

import java.io.Externalizable;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.Map.Entry;

public class MetricSummary implements Externalizable {
	private String name;
	private UUID uuid;
	private HashMap<String, Double> metrics;
	
	public static MetricSummary load(InputStream in) throws Exception {
		ObjectInputStream oin = new ObjectInputStream(in);
		MetricSummary ms = (MetricSummary)oin.readObject();
		return ms;
	}
	
	public static MetricSummary load(String fileName) throws Exception {
		FileInputStream fi = null;
		try {
			fi = new FileInputStream(fileName);
			MetricSummary ms = load(fi);
			return ms;
		} finally {
			if (fi != null) {
				fi.close();
			}
		}
	}
	
	public MetricSummary(String name, MetricTable table) {
		this.name = name;
		this.uuid = UUID.randomUUID();
		this.metrics = new HashMap<String, Double>();
		
		for (MetricRegistry.Entry me : MetricRegistry.entries()) {
			this.metrics.put(me.getName(), table.getAverage(me.getName()));
		}
	}
	
	public MetricSummary() {
		name = "";
		uuid = UUID.randomUUID();
		metrics = new HashMap<String, Double>();
	}
	
	public MetricSummary(HashMap<String, Double> metrics) {
		this();
		this.metrics = metrics;
	}
	
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public UUID getUuid() {
		return uuid;
	}
	
	public double get(String metric) {
		Double val = metrics.get(metric);
		if (val == null) return 0;
		return val;
	}
	
	public Map<String, Double> getMetrics() {
		return Collections.unmodifiableMap(metrics);
	}

	public void save(OutputStream out) throws Exception {
		ObjectOutputStream oout = new ObjectOutputStream(out);
		oout.writeObject(this);
		oout.flush();
	}
	
	public void save(String fileName) throws Exception {
		FileOutputStream fo = null;
		try {
			fo = new FileOutputStream(fileName);
			save(fo);
		} finally {
			if (fo != null) {
				fo.close();
			}
		}
	}
 	
	@Override
	public void readExternal(ObjectInput in) throws IOException,
			ClassNotFoundException {
		
		in.readInt(); // version
		
		name = in.readUTF();
		uuid = (UUID)in.readObject(); 
			
		int msize = in.readInt();
		for (int i = 0; i < msize; i++) {
			String mname = in.readUTF();
			double mval = in.readDouble();
			
			metrics.put(mname, mval);
		}
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(0); // version
		
		out.writeUTF(name);
		out.writeObject(uuid);
		
		out.writeInt(metrics.size());
		for (Entry<String, Double> e : metrics.entrySet()) {
			out.writeUTF(e.getKey());
			out.writeDouble(e.getValue());
		}
	}
}
