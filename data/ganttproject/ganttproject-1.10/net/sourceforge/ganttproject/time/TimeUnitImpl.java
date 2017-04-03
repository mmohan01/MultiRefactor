package net.sourceforge.ganttproject.time;

import java.util.Date;

import net.sourceforge.ganttproject.time.TimeUnit;
import net.sourceforge.ganttproject.time.TimeUnitGraph;


/**
 * Created by IntelliJ IDEA.
 * @author bard
 * Date: 01.02.2004
 */
public class TimeUnitImpl implements TimeUnit {
    private final String myName;
    private final TimeUnitGraph myGraph;
    private final TimeUnit myDirectAtomUnit;
    private TextFormatter myTextFormatter;

    public TimeUnitImpl(String name, TimeUnitGraph graph, TimeUnit directAtomUnit) {
        myName = name;
        myGraph = graph;
        myDirectAtomUnit = directAtomUnit;
    }

    public String getName() {
        return myName;
    }

    public boolean isConstructedFrom(TimeUnit atomUnit) {
        return myGraph.getComposition(this, atomUnit)!=null;
    }

    public int getAtomCount(TimeUnit atomUnit) {
        return myGraph.getComposition(this, atomUnit).getAtomCount();
    }

    public TimeUnit getDirectAtomUnit() {
        return myDirectAtomUnit;
    }

    public String toString() {
    	return getName();
    }

    public void setTextFormatter(TextFormatter formatter) {
        myTextFormatter = formatter;
    }

    public String format(Date baseDate) {
        return myTextFormatter==null ? "" : myTextFormatter.format(this, baseDate);
    }
    
    protected TextFormatter getTextFormatter() {
        return myTextFormatter;
    }
    

}
