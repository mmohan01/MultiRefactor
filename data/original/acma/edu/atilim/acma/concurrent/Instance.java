package edu.atilim.acma.concurrent;

import java.io.Serializable;

public interface Instance {
	public String getName();
	public void setConnectionListener(ConnectionListener connectionListener);
	public void send(Serializable message);
	public <T extends Serializable> T receive(Class<T> cls);
	public Serializable receive();
	public <T extends Serializable> T tryReceive(Class<T> cls);
	public Serializable tryReceive();
	public void dispose();
}
