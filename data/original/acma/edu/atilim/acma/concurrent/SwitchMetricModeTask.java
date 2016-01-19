package edu.atilim.acma.concurrent;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

import edu.atilim.acma.Core;

public class SwitchMetricModeTask implements ConcurrentTask, Externalizable {
	private boolean paretoMode;
	
	public SwitchMetricModeTask() {

	}

	public SwitchMetricModeTask(boolean paretoMode) {
		this.paretoMode = paretoMode;
	}

	@Override
	public void runMaster(InstanceSet instances) {
		System.out.println(String.format("Switching to %s metric mode", paretoMode ? "Pareto" : "Aggregate"));
		Core.paretoMode = this.paretoMode;
	}

	@Override
	public void runWorker(Instance master) {
		System.out.println(String.format("Switching to %s metric mode", paretoMode ? "Pareto" : "Aggregate"));
		Core.paretoMode = this.paretoMode;
	}

	@Override
	public void interrupt() {

	}

	@Override
	public void clearInterrupt() {

	}

	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		paretoMode = in.readBoolean();
	}

	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeBoolean(paretoMode);
	}

	@Override
	public String toString() {
		return String.format("Use %s mode", paretoMode ? "Pareto" : "Aggregate");
	}
}
