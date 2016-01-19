package edu.atilim.acma;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RunResult {
	public class DesignInfo {
		private double score;
		private int appliedActions;
		
		private HashMap<String, Double> metrics;
		
		protected HashMap<String, Double> getMetrics() {
			return metrics;
		}
		
		protected double getScore() {
			return score;
		}
		
		public int getAppliedActions() {
			return appliedActions;
		}

		private DesignInfo() {
			metrics = new HashMap<String, Double>();
		}
	}
	
	private enum ReadStage {
		HEADER,
		INITIAL,
		INITIALMETRICS,
		FINAL,
		FINALMETRICS
	}
	
	public static RunResult readFrom(String file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		
		RunResult result = new RunResult();
		
		result.id = UUID.fromString(new File(file).getName().replace(".txt", ""));
	
		ReadStage stage = ReadStage.HEADER;
		String line = null;
		String match = null;
		
		while ((line = br.readLine()) != null) {
			line = line.trim();
			
			if ((match = matchRegex("\\* Name: ([A-z]+)", line, 1)) != null)
				result.setAttribute("Benchmark", match.trim());
			if ((match = matchRegex("Run Info: ([A-z ]+)\\.", line, 1)) != null) {
				if (new File(file).getParentFile().getName().contains(" SBS "))
					result.setAttribute("Algorithm", "Stochastic Beam Search");
				else
					result.setAttribute("Algorithm", match.trim());
			}
			if ((match = matchRegex("Time taken: ([\\d,\\.]+) seconds", line, 1)) != null)
				result.setAttribute("Time", match.trim());
			if ((match = matchRegex("Restart Count: ([\\d]+)", line, 1)) != null)
				result.setAttribute("RestartCount", match.trim());
			if ((match = matchRegex("Beam Length: ([\\d]+)", line, 1)) != null)
				result.setAttribute("BeamLength", match.trim());
			if ((match = matchRegex("Randomization: ([\\d]+)", line, 1)) != null)
				result.setAttribute("Randomization", match.trim());
			if ((match = matchRegex("Population Size: ([\\d]+)", line, 1)) != null)
				result.setAttribute("PopulationSize", match.trim());
			if ((match = matchRegex("Max Trials: ([\\d]+)", line, 1)) != null)
				result.setAttribute("MaxTrials", match.trim());
			if ((match = matchRegex("Initial Temperature: ([\\d,\\.]+)", line, 1)) != null)
				result.setAttribute("InitialTemperature", match.trim());
			if ((match = matchRegex("Depth: ([\\d]+)\\.", line, 1)) != null)
				result.setAttribute("Depth", match.trim());
			if ((match = matchRegex("Iterations: ([\\d]+)", line, 1)) != null)
				result.setAttribute("Iterations", match.trim());
			if ((match = matchRegex("Expanded Designs: ([\\d]+)", line, 1)) != null)
				result.setAttribute("NodeExpansion", match.trim());
			/*
			if (line.trim().startsWith(" - ")) {
				int numActs = Integer.parseInt(result.getAttribute("NumActions", "0"));
				result.setAttribute("NumActions", String.valueOf(numActs + 1));
			}
			*/
			if (stage == ReadStage.HEADER) {
				if (line.startsWith("* Name:"))
					result.runName = line.substring(8);
				else if (line.startsWith("* Initial Design:"))
					stage = ReadStage.INITIAL;
				else if (line.startsWith("* Final Design:"))
					stage = ReadStage.FINAL;
			} else if (stage == ReadStage.INITIAL || stage == ReadStage.FINAL) {
				DesignInfo design = stage == ReadStage.INITIAL ? result.initialDesign : result.finalDesign;
				
				if (line.startsWith("* Score:"))
					design.score = Double.parseDouble(line.substring(9).replace(',', '.'));
				else if (line.startsWith("* Applied Actions:"))
					design.appliedActions = Integer.parseInt(line.substring(19));
				else if (line.startsWith("* Metric Summary:")) {
					if (stage == ReadStage.INITIAL) stage = ReadStage.INITIALMETRICS;
					else if (stage == ReadStage.FINAL) stage = ReadStage.FINALMETRICS;
					continue;
				}
			} else if (stage == ReadStage.INITIALMETRICS || stage == ReadStage.FINALMETRICS) {
				DesignInfo design = stage == ReadStage.INITIALMETRICS ? result.initialDesign : result.finalDesign;
				
				if (line.length() == 0) {
					stage = ReadStage.HEADER;
					continue;
				}
				
				String[] tokens = line.split(" ");
				
				String mName = tokens[1].trim();
				mName = mName.replace(":", "");
				
				double mVal = Double.parseDouble(tokens[2].replace(',', '.'));
				
				design.metrics.put(mName, mVal);
			}
		}
		
		br.close();
		
		return result;
	}
	
	private static String matchRegex(String pattern, String input, int group) {
		Pattern p = Pattern.compile(pattern);
		Matcher m = p.matcher(input);
		if (m.find())
			return m.group(group);
		return null;
	}
	
	private UUID id;
	private String runName;
	private Map<String, String> attributes;
	private DesignInfo initialDesign;
	private DesignInfo finalDesign;
	
	private RunResult() {
		id = UUID.randomUUID();
		initialDesign = new DesignInfo();
		finalDesign = new DesignInfo();
		attributes = new HashMap<String, String>();
	}

	private RunResult(UUID id, String runName, DesignInfo initialDesign, DesignInfo finalDesign) {
		this.id = id;
		this.runName = runName;
		this.initialDesign = initialDesign;
		this.finalDesign = finalDesign;
		this.attributes = new HashMap<String, String>();
	}
	
	public String getBenchmark() {
		return runName.split(" ")[0].trim();
	}

	public DesignInfo getFinalDesign() {
		return finalDesign;
	}

	public UUID getId() {
		return id;
	}

	public DesignInfo getInitialDesign() {
		return initialDesign;
	}

	public String getName() {
		return runName;
	}
	
	private void setAttribute(String key, String value) {
		if (value != null)
			attributes.put(key, value);
	}
	
	public String getAttribute(String key) {
		return getAttribute(key, null);
	}
	
	public String getAttribute(String key, String def) {
		if (attributes.containsKey(key)) {
			return attributes.get(key);
		}
		
		return def;
	}
	
	public String toCSVString() {
		StringBuilder sb = new StringBuilder();
		
		NumberFormat nf = NumberFormat.getInstance(Locale.FRENCH);
		
		sb.append(id.toString()).append(';');
		sb.append(getAttribute("Benchmark", "")).append(';');
		sb.append(getAttribute("Algorithm", "")).append(';');
		sb.append(nf.format(initialDesign.score)).append(';');
		sb.append(nf.format(finalDesign.score)).append(';');
		sb.append(getAttribute("Time", "").replace('.', ',')).append(';');
		sb.append(getAttribute("Iterations", "")).append(';');
		sb.append(getAttribute("RestartCount", "")).append(';');
		sb.append(getAttribute("Randomization", ""));
		sb.append(getAttribute("Depth", ""));
		sb.append(';');
		sb.append(getAttribute("BeamLength", "")).append(';');
		sb.append(getAttribute("PopulationSize", "")).append(';');
		sb.append(getAttribute("MaxTrials", "")).append(';');
		sb.append(getAttribute("InitialTemperature", "Simulated Annealing".equals(getAttribute("Algorithm")) ? "1,5" : ""));
		
		return sb.toString();
	}
}
