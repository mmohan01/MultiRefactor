package edu.atilim.acma.concurrent;

import java.io.Serializable;

public interface Command extends Serializable {
	public String getCommand();
}
