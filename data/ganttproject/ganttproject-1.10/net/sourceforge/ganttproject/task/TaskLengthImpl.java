package net.sourceforge.ganttproject.task;

import net.sourceforge.ganttproject.time.TimeUnit;

/**
 * Created by IntelliJ IDEA.
 * @author bard
 * Date: 31.01.2004
 */
public class TaskLengthImpl implements TaskLength {
    private final TimeUnit myUnit;
    private long myCount;

    public TaskLengthImpl(TimeUnit unit, long count) {
        myUnit = unit;
        myCount = count;
    }

    public long getLength() {
        return myCount;
    }

    public TimeUnit getTimeUnit() {
        return myUnit;
    }

    public void setLength(TimeUnit unit, long length) {
        if (!unit.equals(myUnit)) {
            throw new IllegalArgumentException("Can't convert unit="+unit+" to my unit="+myUnit);
        }
        myCount = length;
    }

    public long getLength(TimeUnit unit) {
        if (!unit.equals(myUnit)) {
            throw new IllegalArgumentException("Can't convert unit="+unit+" to my unit="+myUnit);
        }
        return myCount;
    }

    public String toString(){
        return ""+myCount+" "+myUnit.getName();
    }
}
