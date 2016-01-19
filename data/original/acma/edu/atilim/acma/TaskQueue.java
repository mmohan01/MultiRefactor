package edu.atilim.acma;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import edu.atilim.acma.concurrent.ConcurrentTask;
import edu.atilim.acma.util.Log;

public class TaskQueue {
	static {
		init();
	}
	
	private static Queue<ConcurrentTask> queue;
	
	public synchronized static void push(ConcurrentTask task) {
		queue.add(task);
		save();
	}
	
	public synchronized static ConcurrentTask pop() {
		ConcurrentTask task = queue.poll();
		save();
		return task;
	}
	
	public synchronized static ConcurrentTask peek() {
		return queue.peek();
	}
	
	public synchronized static void remove(ConcurrentTask task) {
		queue.remove(task);
		save();
	}
	
	public synchronized static int size() {
		return queue.size();
	}
	
	public synchronized static List<ConcurrentTask> asList() {
		return new ArrayList<ConcurrentTask>(queue);
	}
	
	@SuppressWarnings("unchecked")
	private static void init() {
		ObjectInputStream ois = null;
		try {
			ois = new ObjectInputStream(new FileInputStream("./data/user/user.tasks"));
			queue = (LinkedList<ConcurrentTask>)ois.readObject();
		} catch (Exception e) {
			queue = new LinkedList<ConcurrentTask>();
		} finally {
			if (ois != null) {
				try { ois.close(); } catch (IOException e) {}
			}
		}
	}
	
	private static void save() {
		ObjectOutputStream oos = null;
		try {
			oos = new ObjectOutputStream(new FileOutputStream("./data/user/user.tasks"));
			oos.writeObject(queue);
		} catch (Exception e) {
			Log.severe("Could not save task list!");
		} finally {
			if (oos != null) {
				try { oos.flush(); oos.close(); } catch (IOException e) {}
			}
		}
	}
}
