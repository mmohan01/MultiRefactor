package net.sourceforge.ganttproject.time;

import java.util.Date;

public interface TextFormatter {
    public String format(TimeUnit timeUnit, Date baseDate);
}
