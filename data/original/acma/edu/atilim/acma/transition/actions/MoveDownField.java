package edu.atilim.acma.transition.actions;

import java.util.List;
import java.util.Set;

import edu.atilim.acma.design.Accessibility;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Field;
import edu.atilim.acma.design.Method;
import edu.atilim.acma.design.Type;
import edu.atilim.acma.util.Log;

public class MoveDownField {
	public static class Checker implements ActionChecker {
		@Override
		public void findPossibleActions(Design design, Set<Action> set) {
			for (Type type : design.getTypes()) {
				List<Type> childTypeList = type.getExtenders();
				
				if(childTypeList == null || type.isAnnotation() || type.isCompilerGenerated())
					continue;
				
				for (Field f : type.getFields()) {
					if(f.getAccess() == Accessibility.PRIVATE || f.isCompilerGenerated()) 
						continue;
					
					type:
					for(Type t : childTypeList){
						if(t.isAnnotation() || t.isCompilerGenerated()) continue;
						
						for (Method m : f.getAccessors()) {
							if (!m.canAccess(t) || !m.canAccess(t, f.getAccess()) || !t.isAncestorOf(m.getOwnerType())) 
								break type;
						}
						
						set.add(new Performer(type.getName(), f.getName(), t.getName()));
					}	
				}	
			}
		}
	}
	
	public static class Performer implements Action {
		private String typeName;
		private String fieldName;
		private String newOwnerTypeName;
	
		public Performer(String typeName, String fieldName, String newOwnerTypeName) {
			this.typeName = typeName;
			this.fieldName = fieldName;
			this.newOwnerTypeName = newOwnerTypeName;
		}

		@Override
		public void perform(Design d) {
			Field f = d.getType(typeName).getField(fieldName);
			Type t = d.getType(newOwnerTypeName);
		
			if (f == null) {
				Log.severe("[MoveDownField] Can not find field: %s.", fieldName);
				return;
			}
			
			f.setOwnerType(t);	
		}
	
		@Override
		public String toString() {	
			return String.format("[Move Down Field] '%s' of '%s' to its child class '%s'", fieldName,typeName,newOwnerTypeName);
		}
	}
}

