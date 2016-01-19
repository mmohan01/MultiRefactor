package edu.atilim.acma.ws;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import edu.atilim.acma.search.ConcurrentAlgorithm;
import edu.atilim.acma.search.SolutionDesign;
import edu.atilim.acma.util.ACMAUtil;
import edu.atilim.acma.ws.RunRequest.State;

public class ContextManager {
	private static Map<UUID, Context> registry = new HashMap<UUID, Context>();
	
	public static Context getContext(String id) {
		try {
			return getContext(UUID.fromString(id));
		} catch (Exception e) {
		}
		
		return null;
	}
	
	public static Context getContext(UUID id) {
		synchronized (registry) {
			return registry.get(id);
		}
	}
	
	static void register(Context context) {
		synchronized (registry) {
			registry.put(context.getId(), context);
		}
	}
	
	static void initialize() {
		ConcurrentAlgorithm.setListener(new FinishListener());
	}
	
	private static class FinishListener implements ConcurrentAlgorithm.Listener {
		@Override
		public void onAlgorithmFinish(String name, SolutionDesign finalDesign) {
			String cname = name.split("\\|")[0];
			String rname = name.split("\\|")[1];
			
			Context context = getContext(cname);
			
			if (context != null) {
				RunRequest req = context.getRequest(rname);
				
				if (req == null) return;
				
				req.setState(State.COMPLETED);
				req.setFinalDesign(finalDesign.getDesign());
				
				String email = context.getEmail();
				if (email != null && email.trim().length() != 0) {
					String message = "Hello,\r\nYour refactoring request on A-CMA has been completed. Please collect your results using the link below:\r\nhttp://online.a-cma.com/?cid=" + cname + "&rid=" + rname + "\r\n\r\nThank you.";
					String subject = "Refactoring Completed";
					
					ACMAUtil.sendMail(email, subject, message);
				}
			}
			
		}
	}
}
