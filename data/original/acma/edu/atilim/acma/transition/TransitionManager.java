package edu.atilim.acma.transition;

import java.util.HashSet;
import java.util.Set;

import edu.atilim.acma.RunConfig;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.transition.ActionRegistry.Entry;
import edu.atilim.acma.transition.actions.Action;

public class TransitionManager {
	public static Set<Action> getPossibleActions(Design d) {
		return getPossibleActions(d, RunConfig.getDefault());
	}
	
	public static Set<Action> getPossibleActions(Design d, RunConfig config) {
		Set<Action> actions = new HashSet<Action>();
		
		Set<Entry> entries = ActionRegistry.entries();
		
		for (Entry e : entries) {
			if (config.isActionEnabled(e.getName()))
				e.getChecker().findPossibleActions(d, actions);
		}
			
		return actions;
	}
}
