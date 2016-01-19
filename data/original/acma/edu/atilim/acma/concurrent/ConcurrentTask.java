package edu.atilim.acma.concurrent;

import java.io.Serializable;

public interface ConcurrentTask extends Serializable {
	public void runMaster(InstanceSet instances);
	public void runWorker(Instance master);
	public void interrupt();
	public void clearInterrupt();
}