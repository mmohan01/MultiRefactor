package edu.atilim.acma.transition.actions;

import java.util.List;
import java.util.Set;

import edu.atilim.acma.design.Accessibility;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Field;
import edu.atilim.acma.design.Type;

public class DecreaseFieldSecurity {
	public static class Checker implements ActionChecker {
		@Override
		public void findPossibleActions(Design design, Set<Action> set) {
			List<Type> types = design.getTypes();
			
			for (Type t : types) {
				for (Field f : t.getFields()) {
					if (f.isCompilerGenerated() || f.isConstant() ||  f.getAccess() == Accessibility.PUBLIC) continue;
					
					Accessibility newaccess = Accessibility.PUBLIC;
					
					if (f.getAccess() == Accessibility.PRIVATE)
						newaccess = Accessibility.PACKAGE;
					if (f.getAccess() == Accessibility.PACKAGE)
						newaccess = Accessibility.PROTECTED;
					if (f.getAccess() == Accessibility.PROTECTED)
						newaccess = Accessibility.PUBLIC;
					
					set.add(new Performer(t.getName(), f.getName(), newaccess));
				}
			}
		}
	}
	
	public static class Performer implements Action {
		private String typeName;
		private String fieldName;
		private Accessibility newAccess;

		public Performer(String typeName, String fieldName,
				Accessibility newAccess) {
			this.typeName = typeName;
			this.fieldName = fieldName;
			this.newAccess = newAccess;
		}

		@Override
		public void perform(Design d) {
			d.getType(typeName).getField(fieldName).setAccess(newAccess);
		}
		
		@Override
		public String toString() {
			return String.format("[Decrease Field Security] '%s' of '%s' to '%s'", fieldName, typeName, newAccess);
		}
	}
}
