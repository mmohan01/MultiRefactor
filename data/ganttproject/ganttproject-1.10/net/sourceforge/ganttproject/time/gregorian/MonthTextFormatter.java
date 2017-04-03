package net.sourceforge.ganttproject.time.gregorian;

import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import net.sourceforge.ganttproject.time.DateFrameable;
import net.sourceforge.ganttproject.time.TextFormatter;
import net.sourceforge.ganttproject.time.TimeUnit;

public class MonthTextFormatter implements TextFormatter {
    public String format(TimeUnit timeUnit, Date baseDate) {
        String result = null;
        if (timeUnit instanceof DateFrameable) {
            Date adjustedLeft = ((DateFrameable)timeUnit).adjustLeft(baseDate);
            result = MessageFormat.format("{0}", new Object[] {myFormat.format(adjustedLeft)});
        }
        return result;
    }
    
    
    private SimpleDateFormat myFormat = new SimpleDateFormat("MMMM");

}
