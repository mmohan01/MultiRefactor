package edu.atilim.acma.ws;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.Date;
import java.util.UUID;

import edu.atilim.acma.concurrent.ConcurrentTask;
import edu.atilim.acma.concurrent.Instance;
import edu.atilim.acma.concurrent.InstanceSet;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.search.ConcurrentAlgorithm;

public class RunRequest implements Externalizable, ConcurrentTask {
	public enum State {
		WAITING,
		RUNNING,
		COMPLETED,
		FAILED
	}
	
	private UUID id;
	private String name;
	private Date date;
	private State state;
	private UUID context;
	private ConcurrentAlgorithm algorithm;
	private Design finalDesign;
	
	UUID getId() {
		return id;
	}

	String getName() {
		return name;
	}

	Date getDate() {
		return date;
	}

	State getState() {
		return state;
	}

	void setState(State state) {
		this.state = state;
	}

	UUID getContext() {
		return context;
	}

	Design getFinalDesign() {
		return finalDesign;
	}

	void setFinalDesign(Design finalDesign) {
		this.finalDesign = finalDesign;
	}

	public RunRequest() {
	}
	
	public RunRequest(Context context, String name, ConcurrentAlgorithm algorithm) {
		this.id = UUID.randomUUID();
		this.name = name;
		this.date = new Date();
		this.state = State.WAITING;
		this.context = context.getId();
		this.algorithm = algorithm;
		this.algorithm.setName(String.format("%s|%s", this.context.toString(), this.id.toString()));
	}
	
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id = (UUID)in.readObject();
		name = in.readUTF();
		date = (Date)in.readObject();
		state = (State)in.readObject();
		context = (UUID)in.readObject();
		algorithm = (ConcurrentAlgorithm)in.readObject();
		finalDesign = (Design)in.readObject();
		
	}
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(id);
		out.writeUTF(name);
		out.writeObject(date);
		out.writeObject(state);
		out.writeObject(context);
		out.writeObject(algorithm);
		out.writeObject(finalDesign);
	}

	@Override
	public void runMaster(InstanceSet instances) {
		state = State.RUNNING;
		algorithm.runMaster(instances);
	}

	@Override
	public void runWorker(Instance master) {
		algorithm.runWorker(master);
	}

	@Override
	public void interrupt() {
		state = State.FAILED;
		algorithm.interrupt();
	}

	@Override
	public void clearInterrupt() {
		algorithm.clearInterrupt();
	}
}
