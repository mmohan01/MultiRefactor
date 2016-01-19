package edu.atilim.acma.search;


public interface AlgorithmObserver {
	public static final int UPDATE_CURRENT = 1;
	public static final int UPDATE_BEST = 2;
	
	void onStart(AbstractAlgorithm algorithm, SolutionDesign initial);
	void onFinish(AbstractAlgorithm algorithm, SolutionDesign last);
	
	void onExpansion(AbstractAlgorithm algorithm, int count);
	
	void onUpdateItems(AbstractAlgorithm algorithm, SolutionDesign current, SolutionDesign best, int updateType);
	
	void onLog(AbstractAlgorithm algorithm, String log);
	
	void onAdvance(AbstractAlgorithm algorithm, int current, int total);
	
	void onStep(AbstractAlgorithm algorithm, int step);
}
