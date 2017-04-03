package net.sourceforge.ganttproject.task.algorithm;

/**
 * Created by IntelliJ IDEA.
 * User: bard
 */
public class AlgorithmCollection {
    private final FindPossibleDependeesAlgorithm myFindPossibleDependeesAlgorithm;
    private final RecalculateTaskScheduleAlgorithm myRecalculateTaskScheduleAlgorithm;
    private final AdjustTaskBoundsAlgorithm myAdjustTaskBoundsAlgorithm;
    private final RecalculateTaskCompletionPercentageAlgorithm myCompletionPercentageAlgorithm;

    public AlgorithmCollection(FindPossibleDependeesAlgorithm myFindPossibleDependeesAlgorithm, RecalculateTaskScheduleAlgorithm recalculateTaskScheduleAlgorithm, AdjustTaskBoundsAlgorithm adjustTaskBoundsAlgorithm, RecalculateTaskCompletionPercentageAlgorithm completionPercentageAlgorithm) {
        this.myFindPossibleDependeesAlgorithm = myFindPossibleDependeesAlgorithm;
        myRecalculateTaskScheduleAlgorithm = recalculateTaskScheduleAlgorithm;
        myAdjustTaskBoundsAlgorithm = adjustTaskBoundsAlgorithm;
        myCompletionPercentageAlgorithm = completionPercentageAlgorithm;

    }

    public FindPossibleDependeesAlgorithm getFindPossibleDependeesAlgorithm() {
        return myFindPossibleDependeesAlgorithm;
    }

    public RecalculateTaskScheduleAlgorithm getRecalculateTaskScheduleAlgorithm() {
        return myRecalculateTaskScheduleAlgorithm;
    }

    public AdjustTaskBoundsAlgorithm getAdjustTaskBoundsAlgorithm() {
        return myAdjustTaskBoundsAlgorithm;
    }

    public RecalculateTaskCompletionPercentageAlgorithm getRecalculateTaskCompletionPercentageAlgorithm() {
        return myCompletionPercentageAlgorithm;
    }
}
