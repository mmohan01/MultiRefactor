package edu.atilim.acma.ws;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import edu.atilim.acma.RunConfig;
import edu.atilim.acma.TaskQueue;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.ui.ConfigManager;
import edu.atilim.acma.util.ACMAUtil;

public class Context implements Externalizable {
	private UUID id;
	private Design design;
	private RunConfig runConfig;
	private ContextState state;
	private String email;
	private List<RunRequest> requests;
	
	public UUID getId() {
		return id;
	}

	public Design getDesign() {
		return design;
	}
	
	
	public void setDesign(Design design) {
		this.design = design;
	}

	public RunConfig getRunConfig() {
		return runConfig;
	}

	public ContextState getState() {
		return state;
	}
	
	void setState(ContextState state) {
		this.state = state;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getEmail() {
		return email;
	}

	public static Context create() {
		Context context = new Context();
		ContextManager.register(context);
		return context;
	}
	
	public static Context create(Design design) {
		Context context = create();
		context.setDesign(design);
		return context;
	}
	
	public void addRequest(RunRequest request) {
		requests.add(request);
		TaskQueue.push(request);
	}
	
	public RunRequest getRequest(String name) {
		for (RunRequest req : requests) {
			if (req.getId().toString().equals(name)) {
				return req;
			}
		}
		return null;
	}
	
	public List<RunRequest> requests() {
		return Collections.unmodifiableList(requests);
	}

	public Context() {
		id = UUID.randomUUID();
		design = null;
		runConfig = ACMAUtil.deepCopy(ConfigManager.getRunConfig("Default"));
		state = ContextState.WAITING;
		email = "";
		requests = new ArrayList<RunRequest>();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
		id = (UUID)in.readObject();
		design = (Design)in.readObject();
		runConfig = (RunConfig)in.readObject();
		state = (ContextState)in.readObject();
		email = in.readUTF();
		requests = (ArrayList<RunRequest>)in.readObject();
	}
	
	@Override
	public void writeExternal(ObjectOutput out) throws IOException {
		out.writeObject(id);
		out.writeObject(design);
		out.writeObject(runConfig);
		out.writeObject(state);
		out.writeUTF(email);
		out.writeObject(requests);
	}
}
