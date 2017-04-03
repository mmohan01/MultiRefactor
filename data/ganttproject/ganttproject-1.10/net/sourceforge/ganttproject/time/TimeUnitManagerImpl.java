package net.sourceforge.ganttproject.time;

/**
 * Created by IntelliJ IDEA.
 * @author bard
 * Date: 03.02.2004
 */
public class TimeUnitManagerImpl implements TimeUnitManager {
    private TimeUnit[] myUnits;
    private TimeUnitGraph myTimeGraph = new TimeUnitGraph();

    public TimeUnitManagerImpl() {
        myUnits = new TimeUnit[] {
            myTimeGraph.createAtomTimeUnit("day")
        };
    }

    public TimeUnit getTimeUnit(int unitID) {
        return myUnits[unitID];
    }


}
