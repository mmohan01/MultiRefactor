package edu.atilim.acma.search;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import edu.atilim.acma.Core;

public class RunInfoTag implements Externalizable {
	private long runDuration;
	private String runInfo;
	private boolean pareto;
	private long expansionCount;
	
	public long getRunDuration() {
		return runDuration;
	}

	public long getExpansionCount() {
		return expansionCount;
	}

	public String getRunInfo() {
		return runInfo;
	}
	
	protected boolean isPareto() {
		return pareto;
	}

	public RunInfoTag(long runDuration, String runInfo, long expansionCount) {
		this.runDuration = runDuration;
		this.runInfo = runInfo;
		this.pareto = Core.paretoMode;
		this.expansionCount = expansionCount;
	}

	public RunInfoTag() {
	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		runDuration = in.readLong();
		runInfo = in.readUTF();
		pareto = in.readBoolean();
		expansionCount = in.readLong();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeLong(runDuration);
		out.writeUTF(runInfo);
		out.writeBoolean(pareto);
		out.writeLong(expansionCount);
	}
}
