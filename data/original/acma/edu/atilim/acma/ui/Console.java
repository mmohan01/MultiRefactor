package edu.atilim.acma.ui;

import java.util.logging.Handler;
import java.util.logging.LogRecord;

import edu.atilim.acma.ui.design.ConsolePanelBase;
import edu.atilim.acma.util.Log;

public class Console extends ConsolePanelBase {
	private static final long serialVersionUID = 1L;

	public Console() {
		Log.addOutput(new LogHandler());
		Log.config("Console logging enabled.");
	}
	
	public void clear() {
		out.setText("");
		validate();
	}
	
	private class LogHandler extends Handler {
		@Override
		public void close() throws SecurityException {
		}

		@Override
		public void flush() {
		}

		@Override
		public void publish(LogRecord record) {
			if (!isLoggable(record))
				return;
			String message = getFormatter().format(record);
			out.append(message);
			validate();
		}
	}
}
