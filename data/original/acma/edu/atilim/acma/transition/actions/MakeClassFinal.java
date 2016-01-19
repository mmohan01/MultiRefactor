package edu.atilim.acma.transition.actions;

import java.util.Set;

import edu.atilim.acma.design.Accessibility;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Type;
import edu.atilim.acma.util.Log;

public class MakeClassFinal {
	public static class Checker implements ActionChecker {
		@Override
		public void findPossibleActions(Design design, Set<Action> set) {
			for (Type t : design.getTypes()) {
				if (t.getAccess() == Accessibility.PUBLIC || t.getExtenders().size() != 0 || t.isCompilerGenerated() || t.isAnnotation() || t.isInterface() || t.isFinal()) 
					continue;
				
				set.add(new Performer(t.getName()));
			}
		}
	}
	
	public static class Performer implements Action {
		private String typeName;
		
		public Performer(String typeName) {
			this.typeName = typeName;
		}

		@Override
		public void perform(Design d) {
			Type t = d.getType(typeName);
			
			if (t == null) {
				Log.severe("[MakeClassFinal] Can not find type: %s.", typeName);
				return;
			}
			
			t.setFinal(true);
		}
		
		@Override
		public String toString() {
			return String.format("[Make Class Final] %s", typeName);
		}
	}
}

