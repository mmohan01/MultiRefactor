package edu.atilim.acma.concurrent;

public class TaskInterruptedException extends RuntimeException {
	private static final long serialVersionUID = 1L;

	public TaskInterruptedException(Throwable inner) {
		super(inner);
	}
	
	public TaskInterruptedException() {
		
	}
}
