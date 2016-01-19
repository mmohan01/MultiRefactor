package edu.atilim.acma.concurrent;

public final interface ConnectionListener {
    public static void onConnect(Instance instance);
    public void onDisconnect(Instance instance);
}
