package edu.atilim.acma.transition.actions;

import java.util.Set;

import edu.atilim.acma.design.Accessibility;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Type;
import edu.atilim.acma.util.Log;

public class RemoveInterface {
	
	public static class Checker implements ActionChecker {
		@Override
		public void findPossibleActions(Design design, Set<Action> set) {
			
			for (Type t : design.getTypes())
			{	
				if(t.getAccess() == Accessibility.PUBLIC || t.isAnnotation() || t.isCompilerGenerated()) continue;
				
				if(t.isInterface())
				{
					if(t.getImplementers().isEmpty() && t.getDependentMethodsAsParameter().isEmpty() && t.getDependentMethodsAsReturnType().isEmpty() )
						set.add(new Performer(t.getName()));
				}
				
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
				Log.severe("[RemoveInterface] Can not find interface: %s.", typeName);
				return;
			}
			t.remove();
		}
		
		@Override
		public String toString() {
			return String.format("[Remove Interface] %s", typeName);
		}
	}

}
