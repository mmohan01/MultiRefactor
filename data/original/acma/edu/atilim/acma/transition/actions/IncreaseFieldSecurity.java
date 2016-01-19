package edu.atilim.acma.transition.actions;

import java.util.List;
import java.util.Set;

import edu.atilim.acma.design.Accessibility;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Field;
import edu.atilim.acma.design.Method;
import edu.atilim.acma.design.Type;

public class IncreaseFieldSecurity {
	public static class Checker implements ActionChecker {

		@Override
		public void findPossibleActions(Design design, Set<Action> set) {
			List<Type> types = design.getTypes();
			
			for (Type t : types) {
				field:
				for (Field f : t.getFields()) {
					// Turns out, Java compiler binds accesses to constants (static final) in compile time
					// So, a constant does not reflect the access characteristics of a field in bytecode.
					if (f.isCompilerGenerated() || f.isConstant() ||  f.getAccess() == Accessibility.PRIVATE) continue;
					
					Accessibility newaccess = Accessibility.PRIVATE;
					
					if (f.getAccess() == Accessibility.PUBLIC)
						newaccess = Accessibility.PROTECTED;
					if (f.getAccess() == Accessibility.PROTECTED)
						newaccess = Accessibility.PACKAGE;
					if (f.getAccess() == Accessibility.PACKAGE)
						newaccess = Accessibility.PRIVATE;
					
					for (Method m : f.getAccessors()) {
						if (!m.canAccess(f, newaccess))
							break field;
					}
					
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
			return String.format("[Increase Field Security] '%s' of '%s' to '%s'", fieldName, typeName, newAccess);
		}
	}
}
