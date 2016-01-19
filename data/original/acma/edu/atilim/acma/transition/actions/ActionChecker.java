package edu.atilim.acma.transition.actions;

import java.util.Set;

import edu.atilim.acma.design.Design;

public interface ActionChecker {
	void findPossibleActions(Design design, Set<Action> set);
}
