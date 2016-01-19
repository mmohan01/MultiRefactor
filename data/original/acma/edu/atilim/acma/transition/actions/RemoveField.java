package edu.atilim.acma.transition.actions;

import java.util.Set;

import edu.atilim.acma.design.Accessibility;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Field;
import edu.atilim.acma.design.Type;
import edu.atilim.acma.util.Log;

public class RemoveField {
	public static class Checker implements ActionChecker {
		@Override
		public void findPossibleActions(Design design, Set<Action> set) {
			
			for (Type t : design.getTypes())
			{
				for(Field f : t.getFields())
				{
					if(f.getAccess() == Accessibility.PUBLIC || f.getAccess() == Accessibility.PROTECTED || f.isConstant() || !f.getAccessors().isEmpty() || f.isCompilerGenerated()) continue;
					
					set.add(new Performer(t.getName(), f.getName()));
				}
			}
		}
	}
	
	public static class Performer implements Action {
		
		private String typeName;
		private String fieldName;
		
		public Performer(String typeName, String fieldName) {
			
			this.typeName = typeName;
			this.fieldName = fieldName;
		}

		@Override
		public void perform(Design d) {
			
			Field f = d.getType(typeName).getField(fieldName);
			if (f == null) {
				Log.severe("[RemoveField] Can not find field %s of type: %s.", fieldName,typeName);
				return;
			}
			f.remove();
			
		}
		
		@Override
		public String toString() {
			return String.format("[Remove Field] '%s' of type '%s'", fieldName,typeName);
		}
	}

}
