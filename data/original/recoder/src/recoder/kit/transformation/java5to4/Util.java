package recoder.kit.transformation.java5to4;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.ErasedType;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.Type;
import recoder.abstraction.TypeParameter;
import recoder.java.CompilationUnit;
import recoder.java.Expression;
import recoder.java.NonTerminalProgramElement;
import recoder.java.ProgramElement;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.VariableSpecification;
import recoder.java.expression.ArrayInitializer;
import recoder.java.expression.Assignment;
import recoder.java.expression.Operator;
import recoder.java.expression.ParenthesizedExpression;
import recoder.java.expression.operator.BinaryAnd;
import recoder.java.expression.operator.BinaryAndAssignment;
import recoder.java.expression.operator.BinaryNot;
import recoder.java.expression.operator.BinaryOr;
import recoder.java.expression.operator.BinaryOrAssignment;
import recoder.java.expression.operator.BinaryXOr;
import recoder.java.expression.operator.BinaryXOrAssignment;
import recoder.java.expression.operator.ComparativeOperator;
import recoder.java.expression.operator.Conditional;
import recoder.java.expression.operator.Divide;
import recoder.java.expression.operator.Equals;
import recoder.java.expression.operator.LogicalAnd;
import recoder.java.expression.operator.LogicalNot;
import recoder.java.expression.operator.LogicalOr;
import recoder.java.expression.operator.Minus;
import recoder.java.expression.operator.Modulo;
import recoder.java.expression.operator.Negative;
import recoder.java.expression.operator.NotEquals;
import recoder.java.expression.operator.Plus;
import recoder.java.expression.operator.Times;
import recoder.java.expression.operator.TypeCast;
import recoder.java.reference.ArrayReference;
import recoder.java.reference.ConstructorReference;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.TypeReference;
import recoder.java.statement.Do;
import recoder.java.statement.For;
import recoder.java.statement.If;
import recoder.java.statement.Return;
import recoder.java.statement.Switch;
import recoder.java.statement.Throw;
import recoder.java.statement.While;
import recoder.kit.MiscKit;
import recoder.kit.UnitKit;
import recoder.service.NameInfo;
import recoder.service.SourceInfo;

public class Util {
	public static Type getRequiredContextType(SourceInfo si, Expression c, boolean resolveTypeVar) {
		Type target = null;
		NameInfo ni = si.getServiceConfiguration().getNameInfo();
		int dim = 0;
		NonTerminalProgramElement parent = c.getASTParent();
		Expression c_upp = c;

		while (parent instanceof ParenthesizedExpression) {
			c_upp = (Expression)parent;
			parent = parent.getASTParent();
		}

		if (parent instanceof ArrayReference && ((ArrayReference)parent).getDimensionExpressions().contains(c_upp)) {
			// Shortcut for this special case only!!! 
			// Avoid shortcuts otherwise because of array-handling at the
			// end of the method!
			// can stay what it is. Possibly unboxed, though
			target = si.getType(c);
			if (target instanceof ClassType)
				target = si.getUnboxedType((ClassType)target);
			return resolveTypeVar(target, resolveTypeVar);
		}
		
		while (parent instanceof ArrayReference) {
			c_upp = (Expression)parent;
			parent = parent.getASTParent();
			dim++;
		}
		if (parent instanceof MethodReference || parent instanceof ConstructorReference) {
			Method m = parent instanceof MethodReference ? si.getMethod((MethodReference)parent)
					: si.getConstructor((ConstructorReference)parent);
			List<Expression> args = parent instanceof MethodReference ?
					((MethodReference)parent).getArguments() :
						((ConstructorReference)parent).getArguments();

					if (args != null && args.contains(c_upp)) {
						// argument!
						int pos = args.indexOf(c_upp);
						if (m.isVarArgMethod() && pos >= m.getSignature().size()) {
							pos = m.getSignature().size()-1;
							target = ((ArrayType)m.getSignature().get(pos)).getBaseType();
						} else if (m.isVarArgMethod() && pos == m.getSignature().size()-1) { 
							// Pick best fit:
							Type whatWeGot = si.getType(c);
							target = m.getSignature().get(pos);
							int dim1 = 0, dim2 = 0;
							while (whatWeGot instanceof ArrayType) {
								dim1++;
								whatWeGot = ((ArrayType)whatWeGot).getBaseType();
							}
							whatWeGot = target;
							while (whatWeGot instanceof ArrayType) {
								dim2++;
								whatWeGot = ((ArrayType)whatWeGot).getBaseType();
							}
							if (dim1 + 1 == dim2) {
								target = ((ArrayType)target).getBaseType();
							}
						} else {
							target = m.getSignature().get(pos);
						}
//						if (target instanceof Member && !(target instanceof CapturedTypeArgument)) {
//							if (!si.isVisibleFor((Member)target, MiscKit.getParentTypeDeclaration(c))) {
//								target = si.getType(c); // then a visible type is actually required... 
//							}
//						}
					} else {
						// reference prefix
						target = m.getContainingClassType();
					}
		} else if (parent instanceof FieldReference){
			Field f = si.getField((FieldReference)parent);
			target = f.getContainingClassType();
		} else if (parent instanceof Return) {
			target = ((MethodDeclaration)MiscKit.getParentMemberDeclaration(parent)).getReturnType();
		} else if (parent instanceof Operator) {
			if (((Operator)parent).getArity() == 2 && !(parent instanceof Assignment)) {
				Operator p = (Operator)parent;
				Type t1 = si.getType(((Operator)parent).getArguments().get(0));
				Type t2 = si.getType(((Operator)parent).getArguments().get(1));
				if (t1 == ni.getJavaLangString() || t2 == ni.getJavaLangString()) {
					target = ni.getJavaLangString();
				} else if (t1 instanceof PrimitiveType ^ t2 instanceof PrimitiveType) {
					if (c_upp == p.getArguments().get(0) && t1 instanceof ClassType) {
						target = si.getUnboxedType((ClassType)t1);
					} else if (c_upp == p.getArguments().get(1) && t2 instanceof ClassType) {
						target = si.getUnboxedType((ClassType)t2);
					} else target = null; // this argument can be what it wants, doesn't need to be changed. The other needs to be.
				} else if (t1 instanceof ClassType && t2 instanceof ClassType) {
					if (!(p instanceof Equals) && !(p instanceof NotEquals) &&
							t1 != ni.getJavaLangString() && t2 != ni.getJavaLangString()
							&& t1 != ni.getNullType() && t2 != ni.getNullType()) {
						// unbox BOTH!
						target = si.getUnboxedType((ClassType)si.getType(c_upp));
					}
				}
			}
			if (target != null) {
				// already done above.
			} else if ((parent instanceof Minus) || (parent instanceof Times) || (parent instanceof Divide)
            		|| (parent instanceof Modulo) || (parent instanceof Negative)) {
				target = si.getType(c_upp);
				if (target instanceof ClassType)
					target = si.getUnboxedType((ClassType)target);
			} else if (parent instanceof LogicalAnd || parent instanceof LogicalOr
					|| parent instanceof LogicalNot) {
				target = ni.getBooleanType();
			} else if (parent instanceof Plus) {
				Type t1 = si.getType(((Operator)parent).getArguments().get(0));
				Type t2 = si.getType(((Operator)parent).getArguments().get(1));
				if (t1 instanceof ErasedType)
					t1 = ((ErasedType)t1).getGenericType();
				if (t2 instanceof ErasedType)
					t2 = ((ErasedType)t2).getGenericType();
				if (t1 == ni.getJavaLangString() || t2 == ni.getJavaLangString()) {
					// can be whatever it wants to be - compiler adds toString()
				} else {
					target = si.getType(c_upp);
					if (target instanceof ClassType)
						target = si.getUnboxedType((ClassType)target);
				}
			} else if (parent instanceof Assignment) {
				if (((Assignment)parent).getExpressionAt(0) == c_upp) {
					target = null; // can be whatever it wants to be as LHS! TODO ?? BinaryXOr etc. ?
				} else {
					if (parent instanceof BinaryAndAssignment
							|| parent instanceof BinaryOrAssignment
							|| parent instanceof BinaryXOrAssignment
					) {
						target = si.getType(parent);
					} else {
						target = si.getType(((Assignment)parent).getExpressionAt(0));
					} 
				}
			} else if (parent instanceof BinaryAnd
					|| parent instanceof BinaryOr
					|| parent instanceof BinaryXOr) {
				target = si.getType(parent); // as used in context
				if (target instanceof ClassType)
					target = si.getUnboxedType((ClassType)target); // but possibly unboxed.
			} else if (parent instanceof BinaryNot) {
				target = ni.getBooleanType();
			} else if (parent instanceof ComparativeOperator) {
				if (parent instanceof Equals || parent instanceof NotEquals) {
					Type t1 = si.getType(((ComparativeOperator)parent).getExpressionAt(0)); 
					Type t2 = si.getType(((ComparativeOperator)parent).getExpressionAt(1));
					if (t1 instanceof ClassType ^ t2 instanceof ClassType) {
						if (si.getType(c) instanceof ClassType) {
							// the one that it is, actually. Needs to be unboxed later on.
							target = si.getUnboxedType((ClassType)si.getType(c));
						}
					}
				} else {
					Type ct = si.getType(c);
					target = ct instanceof PrimitiveType ? ct : si.getUnboxedType((ClassType)ct);
				}
			} else if (parent instanceof Conditional) {
				if (((Conditional)parent).getExpressionAt(0) == c_upp)
					target = ni.getBooleanType();
				else
					target = si.getType(parent); // TODO ???
			} else if (parent instanceof TypeCast) {
				Type tct = si.getType(parent);
				Type act = si.getType(c_upp);
				if (tct instanceof PrimitiveType && act instanceof PrimitiveType) {
					// remain as is. 
					target = act; 
				} else if (tct instanceof ClassType && act instanceof PrimitiveType) {
					target = si.getBoxedType((PrimitiveType)act);
				} else if (tct instanceof PrimitiveType && act instanceof ClassType) {
					target = si.getUnboxedType((ClassType)act);
				}
			}
		} else if (parent instanceof VariableSpecification) {
			VariableSpecification vs = (VariableSpecification)parent;
			target = vs.getType();
		} else if (parent instanceof If) {
			target = ni.getBooleanType(); // the guard
		} else if (parent instanceof Switch) {
			Type t = si.getType(c);
			if (t instanceof ClassType && ((ClassType)t).isEnumType())
				target = t; // can stay what it is
			else
				target = ni.getIntType();
		} else if (parent instanceof Do) {
			if (((Do) parent).getGuard() == c_upp)
				target = ni.getBooleanType();
		} else if (parent instanceof While) {
			if (((While)parent).getGuard() == c_upp)
				target = ni.getBooleanType();
		} else if (parent instanceof For) {
			if (((For)parent).getGuard() == c_upp)
				target = ni.getBooleanType();
		} else if (parent instanceof Throw) {
			target = si.getType(c); // needs to be exactly it. Otherwise, the exception might not be declared. 
		} else if (parent instanceof ArrayInitializer) {
			int goDownDim = 1;
			while (parent.getASTParent() instanceof ArrayInitializer) {
				parent = parent.getASTParent();
				goDownDim++;
			}
			target = si.getType(parent.getASTParent());
			while (goDownDim-- > 0)
				target = ((ArrayType)target).getBaseType();
		}
		if (target != null) {
			while (dim > 0) {
				dim--;
				target = target.createArrayType();
			}
		}
		else if (dim > 0) {
			/* need for the case:
			 * (X) a.b()[i];
			 * TODO check if this is too restrictive?
			 */
			target = si.getType(c);
		}
		return resolveTypeVar(target, resolveTypeVar);
	}

	private static Type resolveTypeVar(Type target, boolean resolveTypeVar) {
		if (resolveTypeVar && target instanceof TypeParameter) {
			target = ((TypeParameter)target).getBaseClassType();
		}
		return target;
	}

	interface DepSortable {
		ProgramElement getSortItem();
	}

	static class IntroduceCast implements DepSortable {
		Expression toBeCasted;
		TypeReference castedType;
		IntroduceCast(Expression e, TypeReference tr) {
			this.toBeCasted = e;
			this.castedType = tr;
		}
		public ProgramElement getSortItem() { return toBeCasted; }
	}

	private static <T extends DepSortable> void sortCastsLoc(List<T> casts) {
		boolean changed = true;
		ProgramElement ex1, ex2;
		T tmp;
		NonTerminalProgramElement ntpe = null;
		while(changed) {
			changed = false;
			for (int i = 0; i < casts.size() - 1; i++) {
				for (int j = i + 1; j < casts.size(); j++) {
					ex1 = casts.get(i).getSortItem();
					ex2 = casts.get(j).getSortItem();
					if (ex1 instanceof NonTerminalProgramElement) {
						ntpe = (NonTerminalProgramElement) ex1;
						if (isChild(ntpe, ex2)) {
							tmp = casts.get(i);
							casts.set(i, casts.get(j));
							casts.set(j, tmp);
							changed = true;
						}
					}
				}
			}
		}
	}

	static <T extends DepSortable> void sortCasts(List<T> casts) {
		System.out.println("Start sorting casts...");
		long start = System.currentTimeMillis();
		HashMap<ProgramElement, ArrayList<T>> temp = new HashMap<ProgramElement, ArrayList<T>>(casts.size());
		for (T cast : casts) {
			CompilationUnit decl = UnitKit.getCompilationUnit(cast.getSortItem());
			ArrayList<T> al = temp.get(decl);
			if (al == null) {
				al = new ArrayList<T>(4);
				temp.put(decl, al);
			}
			al.add(cast);
		}
		System.out.println("Done pre-sorting casts. ");
		casts.clear();
		for (ArrayList<T> al : temp.values()) {
			sortCastsLoc(al);
			casts.addAll(al);
		}
		System.out.println("Sorting casts took " + ((System.currentTimeMillis()-start)/1000 + " ms"));
	}

	static boolean isChild(ProgramElement ex1, ProgramElement ex2) {
		if (!(ex1 instanceof NonTerminalProgramElement))
			return false;
		if (ex1 == ex2) return false;
		return MiscKit.contains((NonTerminalProgramElement)ex1, ex2);
	}
}
