package edu.atilim.acma.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public final class Log {
	private static Logger logger = Logger.getLogger("a-cma");
	
	static {
		logger.setUseParentHandlers(false);
		logger.setLevel(Level.FINEST);
	}
	
	public static void config(String info)
	{
		logger.config(info);
	}
	
	public static void config(String format, Object... args)
	{
		config(String.format(format, args));
	}
	
	public static void severe(String info)
	{
		logger.severe(info);
	}
	
	public static void severe(String format, Object... args)
	{
		severe(String.format(format, args));
	}
	
	public static void info(String info)
	{
		logger.info(info);
	}
	
	public static void info(String format, Object... args)
	{
		info(String.format(format, args));
	}
	
	public static void warning(String info)
	{
		logger.warning(info);
	}
	
	public static void warning(String format, Object... args)
	{
		warning(String.format(format, args));
	}
	
	public static void addOutput(Handler handler) {
		handler.setFormatter(new Format());
		handler.setLevel(Level.FINEST);
		logger.addHandler(handler);
	}
	
	public static void addOutput(OutputStream stream) {
		StreamHandler sh = new StreamHandler(stream, new Format());
		sh.setLevel(Level.FINEST);
		logger.addHandler(sh);
	}
	
	public static void addOutput(String filename) {
		try {
			FileHandler fh = new FileHandler(filename);
			fh.setFormatter(new Format());
			fh.setLevel(Level.FINEST);
			logger.addHandler(fh);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private static class Format extends Formatter {

		@Override
		public String format(LogRecord record) {
			return String.format("[%d] %s: %s\n", record.getSequenceNumber(), record.getLevel(), record.getMessage());
		}
		
	}
}
