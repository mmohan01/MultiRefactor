package edu.atilim.acma.search;

import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.Externalizable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.util.Map.Entry;
import java.util.List;
import java.util.UUID;

import edu.atilim.acma.RunConfig;
//import edu.atilim.acma.WikiBot;
import edu.atilim.acma.concurrent.ConcurrentTask;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.io.WriteClass;
import edu.atilim.acma.ui.ConfigManager;
import edu.atilim.acma.ui.ConfigManager.Metric;
import edu.atilim.acma.util.Log;


public abstract class ConcurrentAlgorithm implements ConcurrentTask, Externalizable {
	public static interface Listener {
		public void onAlgorithmFinish(String name, SolutionDesign finalDesign);
	}
	
	private static Listener listener;
	private String name;
	private RunConfig config;
	private Design initialDesign;
	private volatile boolean interrupted = false;
	
	public Listener getListener() {
		return ConcurrentAlgorithm.listener;
	}

	public static void setListener(Listener listener) {
		ConcurrentAlgorithm.listener = listener;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	protected RunConfig getConfig() {
		return config;
	}
	
	protected Design getInitialDesign() {
		return initialDesign;
	}
	
	protected boolean isInterrupted() {
		return interrupted;
	}
	
	public void clearInterrupt() {
		interrupted = false;
	}
	
	@Override
	public void interrupt() {
		interrupted = true;
	}

	public ConcurrentAlgorithm() {
	}
	
	public ConcurrentAlgorithm(String name, RunConfig config, Design initialDesign) {
		this.name = name;
		this.config = config;
		this.initialDesign = initialDesign;
	}
	
	protected synchronized String onFinish(Design fDesign, int runCount) {
		// Activates email instead that doesn't work
		//if (listener != null) {
		//	listener.onAlgorithmFinish(name, new SolutionDesign(fDesign, config));
		//	return;
		//}
		
		UUID id = UUID.randomUUID();
		
		String pathName = String.format("./results/%s/", getName().replace('|', '-'));
		String runName = String.format("%s%sRUNS.txt", pathName, runCount);
//		String runName = String.format("%s%s.txt", pathName, id.toString());
//		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy--HH-mm-ss");
//		String date = (dateFormat.format(new Date()));
//		String runName = String.format("%s%s.txt", pathName, date);
		
		File dir = new File(pathName);
		if (!dir.exists()) dir.mkdirs();
		
		SolutionDesign initialDesign = new SolutionDesign(this.initialDesign, config);
		SolutionDesign finalDesign = new SolutionDesign(fDesign, config);
		
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(runName, true));
			bw.write("====== Run Result ======\r\n\r\n");
			bw.write(String.format("* Name: %s\r\n", getName()));
			
			if (fDesign.getTag() != null && fDesign.getTag() instanceof RunInfoTag) {
				RunInfoTag tag = (RunInfoTag)fDesign.getTag();
				bw.write(String.format("  * Run Info: %s\r\n", tag.getRunInfo()));
				bw.write(String.format("  * Time Taken: %.2f seconds\r\n", tag.getRunDuration() / 1000.0));
				bw.write(String.format("  * Quality Gain: %f\r\n", initialDesign.getScore() - finalDesign.getScore()));
				bw.write(String.format("  * Metric Mode: %s\r\n", tag.isPareto() ? "Pareto" : "Aggregate"));
				bw.write(String.format("  * Expanded Designs: %d\r\n", tag.getExpansionCount()));
				bw.write("\r\n");
			}
			
			bw.write("* Initial Design:\r\n");
			bw.write(getDesignInfo(initialDesign));
			bw.write("\r\n");
			bw.write("* Final Design:\r\n");
			bw.write(getDesignInfo(finalDesign));
			bw.write("\r\n");
			bw.write("* Applied Actions:\r\n");
			for (String act : finalDesign.getDesign().getModifications()) {
				bw.write(String.format("  - %s\r\n", act));
			}
			bw.write("\r\n");
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try { bw.close(); } catch (Exception e) { }
		}
		
		// Pushes results to web page that doesn't exist
		//WikiBot.pushRun(getName(), id.toString(), new File(runName));
		
		return runName;
	}
	
	private String getDesignInfo(SolutionDesign design) {
		StringBuilder builder = new StringBuilder();
		
		builder.append("  * Score: ").append(String.format("%.6f", design.getScore())).append("\r\n");
		builder.append("  * Possible Actions: ").append(design.getAllActions().size()).append("\r\n");
		builder.append("  * Applied Actions: ").append(design.getDesign().getModifications().size()).append("\r\n");
		builder.append("  * Num Types: ").append(design.getDesign().getTypes().size()).append("\r\n");
		builder.append("  * Num Packages: ").append(design.getDesign().getPackages().size()).append("\r\n");
		
		builder.append("\r\n");
		
		builder.append("  * Metric Summary:").append("\r\n");
		for (Entry<String, Double> e : design.getMetricSummary().getMetrics().entrySet()) {
			if (getConfig().isMetricEnabled(e.getKey()))
				builder.append("    * ").append(e.getKey()).append(": ").append(String.format("%.4f", e.getValue())).append("\r\n");
		}
		
		return builder.toString();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeInt(0); //version
		
		out.writeUTF(name);
		out.writeObject(config);
		out.writeObject(initialDesign);
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		in.readInt();
		
		name = in.readUTF();
		config = (RunConfig)in.readObject();
		initialDesign = (Design)in.readObject();
	}
	
	@Override
	public String toString() {
		return name;
	}
}
