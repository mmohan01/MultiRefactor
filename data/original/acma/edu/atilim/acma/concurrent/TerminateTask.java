package edu.atilim.acma.concurrent;

public class TerminateTask implements ConcurrentTask {
	private static final long serialVersionUID = 1L;

	@Override
	public void runMaster(InstanceSet instances) {
		System.out.println("Terminating server instance!");
		System.exit(0);
	}

	@Override
	public void runWorker(Instance master) {
		System.out.println("Terminating worker instance!");
		System.exit(0);
	}
	
	@Override
	public void interrupt() {
	}

	@Override
	public void clearInterrupt() {
	}
}
