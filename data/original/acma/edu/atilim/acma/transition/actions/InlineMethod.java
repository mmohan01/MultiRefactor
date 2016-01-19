package edu.atilim.acma.transition.actions;

import java.util.List;
import java.util.Set;

import edu.atilim.acma.design.Accessibility;
import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.Field;
import edu.atilim.acma.design.Method;
import edu.atilim.acma.design.Type;

public class InlineMethod {
	public static class Checker implements ActionChecker {
		@Override
		public void findPossibleActions(Design design, Set<Action> set) {
			for (Type t : design.getTypes()) {
				if(t.isInterface() || t.isAbstract() || t.isCompilerGenerated() || t.isAnnotation()) 
					continue;
				
				for (Method m : t.getMethods()) {
					if (m.isCompilerGenerated() || m.isAbstract() || m.isClassConstructor() || m.getAccess() == Accessibility.PUBLIC)
						continue;
					
					List<Method> callers = m.getCallerMethods();
					
					if (callers.size() == 1) {
						Method caller = callers.get(0);
						
						if (caller.getSignature().equals(m.getSignature()) || caller.getOwnerType() != t) continue;
						
						set.add(new Performer(t.getName(), t.getName(), m.getSignature(), callers.get(0).getSignature()));
					}
				}
			}
		}
		
	}
	
	public static class Performer implements Action {
		private String sourceType;
		private String targetType;
		private String inlinedMethod;
		private String targetMethod;
		
		public Performer(String sourceType, String targetType, String inlinedMethod, String targetMethod) {
			this.sourceType = sourceType;
			this.targetType = targetType;
			this.inlinedMethod = inlinedMethod;
			this.targetMethod = targetMethod;
		}

		@Override
		public void perform(Design d) {
			Type st = d.getType(sourceType);
			Type tt = d.getType(targetType);
			
			if (st == null || tt == null) return;
			
			Method sm = st.getMethod(inlinedMethod);
			Method tm = st.getMethod(targetMethod);
			
			if (sm == null || tm == null) return;
			
			for (Method acm : sm.getCalledMethods())
				tm.addCalledMethod(acm);
			
			for (Field acf : sm.getAccessedFields())
				tm.addAccessedField(acf);
			
			for (Type t : sm.getInstantiatedTypes())
				tm.addInstantiatedType(t);
			
			tm.removeCalledMethod(sm);
			sm.remove();
		}
		
		@Override
		public String toString() {
			return String.format("[Inline Method] %s.%s into %s.%s", sourceType, inlinedMethod, targetType, targetMethod);
		}
	}
}
