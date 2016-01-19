package edu.atilim.acma;

import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.UUID;

import edu.atilim.acma.concurrent.ConcurrentTask;
import edu.atilim.acma.concurrent.ConnectionListener;
import edu.atilim.acma.concurrent.Instance;
import edu.atilim.acma.concurrent.SocketInstance;

public class Client implements Runnable, ConnectionListener {
	private String hostname;
	private int port;
	
	private Instance master;
	private ConcurrentTask currentTask;
	
	public Client(String hostname, int port) {
		this.hostname = hostname;
		this.port = port;
	}

	@Override
	public void run() {
		while(true) {
			try {
				new Client(hostname, port).runInternal();
			} catch (Exception e) {
				String name = String.format("./output/%s_exception.log", UUID.randomUUID().toString());
				PrintWriter pw =  null;
				try {
					pw = new PrintWriter(new FileOutputStream(name));
					e.printStackTrace(pw);
				} catch (Exception ie) {
				} finally {
					try { pw.close(); } catch (Exception iie) { }
				}
			}
		}
	}
	
	private void runInternal() {
		System.out.printf("[Client] Connecting to %s:%d\n", hostname, port);
		for(;;) {
			master = SocketInstance.tryConnect(hostname, port);
			if (master == null) {
				System.out.println("[Client] Could not connect. Retrying in 2 seconds...");
				try { Thread.sleep(2000); } catch (InterruptedException e) { }
				continue;
			}
			master.setConnectionListener(this);
			System.out.println("[Client] Connection established.");
			break;
		}
		
		while(true) {
			System.out.println("[Client] Waiting for task assignment.");
			currentTask = master.receive(ConcurrentTask.class);
			currentTask.runWorker(master);
			currentTask = null;
		}
	}

	@Override
	public void onConnect(Instance instance) {
	}

	@Override
	public void onDisconnect(Instance instance) {
		System.out.printf("[Client] Connection to server %s closed. Ceasing operations!\n", instance.getName());
		dispose();
	}
	
	private volatile boolean disposed = false;
	
	private synchronized void dispose() {
		if (!disposed) {
			disposed = true;
			
			System.out.println("[Client] Disposing client.");
			try { master.dispose(); } catch(Exception e) {}
			try { currentTask.interrupt(); } catch(Exception e) {}
		}
	}
}
