package edu.atilim.acma.concurrent;

public interface ConnectionListener {
	public void onConnect(Instance instance);
	public void onDisconnect(Instance instance);
}
