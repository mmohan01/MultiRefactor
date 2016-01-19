package edu.atilim.acma.search;

import edu.atilim.acma.util.Log;

public abstract class AbstractAlgorithm {
	public static final int STATE_NEW = 0;
	public static final int STATE_RUNNING = 1;
	public static final int STATE_PAUSED = 2;
	public static final int STATE_FINISHED = 3;
	
	private AlgorithmObserver observer;
	protected SolutionDesign initialDesign;
	protected SolutionDesign finalDesign;
	private int stepCount;
	
	private int state;
	
	public int getState() {
		return state;
	}

	public AbstractAlgorithm(SolutionDesign initialDesign) {
		this(initialDesign, null);
	}
	
	public AbstractAlgorithm(SolutionDesign initialDesign, AlgorithmObserver observer) {
		this.observer = observer;
		this.initialDesign = initialDesign;
		this.state = STATE_NEW;
		this.stepCount = 0;
	}
	
	public abstract String getName();
	public abstract boolean step();
	
	protected void beforeStart() {
		
	}
	
	protected void afterFinish() {
		
	}
	
	protected int getStepCount() {
		return stepCount;
	}

	public AlgorithmObserver getObserver() {
		return observer;
	}
	
	public void start() {
		start(true);
	}
	
	public void start(boolean threaded) {		
		if (state == STATE_NEW)
			beforeStart();
		
		state = STATE_RUNNING;
			
		Runnable algo = new Runnable() {
			@Override
			public void run() {
				while (state == STATE_RUNNING) {
					stepCount++;
					
					if (observer != null)
						observer.onStep(AbstractAlgorithm.this, stepCount);
					
					if (step()) {
						state = STATE_FINISHED;
						afterFinish();
						return;
					}
				}
			}
		};
		
		if (threaded)
			new Thread(algo).start();
		else
			algo.run();
	}
	
	public void pause() {
		state = STATE_PAUSED;
	}
	
	protected void log(String log) {
		Log.info("[%s] %s", getName(), log);
		
		if (getObserver() != null)
			getObserver().onLog(this, log);
	}
	
	protected void log(String log, Object... args) {
		log(String.format(log, args));
	}
	
	public void setObserver(AlgorithmObserver observer) {
		this.observer = observer;
	}
}
