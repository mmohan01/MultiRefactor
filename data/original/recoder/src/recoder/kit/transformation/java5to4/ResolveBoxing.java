/**
 * This file is part of the RECODER library and protected by the LGPL.
 */
package recoder.kit.transformation.java5to4;

import java.util.ArrayList;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ProgramFactory;
import recoder.abstraction.ClassType;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.Type;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.Expression;
import recoder.java.Identifier;
import recoder.java.NonTerminalProgramElement;
import recoder.java.ProgramElement;
import recoder.java.expression.Assignment;
import recoder.java.expression.ParenthesizedExpression;
import recoder.java.expression.operator.PostDecrement;
import recoder.java.expression.operator.PostIncrement;
import recoder.java.expression.operator.PreDecrement;
import recoder.java.expression.operator.PreIncrement;
import recoder.java.reference.MethodReference;
import recoder.java.reference.ReferencePrefix;
import recoder.java.reference.TypeReference;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.list.generic.ASTArrayList;
import recoder.service.NameInfo;
import recoder.service.SourceInfo;

/**
 * traverses a (sub)tree and replaces (un-)boxing 
 * conversions with explicit conversions.
 * 
 * @author Tobias Gutzmann
 * @since 0.80
 *
 */
public class ResolveBoxing extends TwoPassTransformation {
	
	private NonTerminalProgramElement root;
	private List<CompilationUnit> cul;
	private List<Object[]> list = new ArrayList<Object[]>();

	/**
	 * @param sc
	 * @param root
	 */
	public ResolveBoxing(CrossReferenceServiceConfiguration sc, NonTerminalProgramElement root) {
		super(sc);
		this.root = root;
	}
	
	public ResolveBoxing(CrossReferenceServiceConfiguration sc, List<CompilationUnit> cul) {
		super(sc);
		this.cul = cul;
	}
	
	private void traverse(TreeWalker tw) {
		SourceInfo si = getServiceConfiguration().getSourceInfo();
		NameInfo ni = getServiceConfiguration().getNameInfo();
		while (tw.next()) {
			ProgramElement pe = tw.getProgramElement();
			if (!(pe instanceof Expression))
				continue;
			if (pe instanceof ParenthesizedExpression)
				continue;
			Expression e = (Expression)pe;
			Type act = si.getType(e);
			
			if (act instanceof ClassType && (e instanceof PreDecrement || e instanceof PreIncrement
					|| e instanceof PostIncrement || e instanceof PostDecrement)) {
				// very special handling!
				boolean needsParentheses = Util.getRequiredContextType(si, e, true) != null;
				boolean addAdjust = needsParentheses && (e instanceof PostDecrement || e instanceof PostIncrement);
				list.add(new Object[] { e, act, addAdjust, needsParentheses });
			} else {
				Type req = Util.getRequiredContextType(si, e, true);
				if (act instanceof PrimitiveType && req instanceof ClassType && req != ni.getJavaLangString()) {
					list.add(new Object[] { e, act});
				} else if (act instanceof ClassType && req instanceof PrimitiveType) {
					list.add(new Object[] { e, act});
				}
			}
		}
	}
	
	@Override
	public ProblemReport analyze() {
		TreeWalker tw;
		if (cul != null) {
			for (CompilationUnit cu : cul) {
				tw = new TreeWalker(cu);
				traverse(tw);
			}
		}
		else {
			tw= new TreeWalker(root);
			traverse(tw);
		}
		return super.analyze();
	}
	
	@Override
	public void transform() {
		super.transform();
		ProgramFactory f = getProgramFactory();
		SourceInfo si = getServiceConfiguration().getSourceInfo();
		System.out.println("To (un-)box: " + list.size());
		MethodReference replacement;
		for (int i = list.size()-1; i >= 0; i--) {
			Object[] op = list.get(i);
			Expression e = (Expression)op[0];
			Type t = (Type)op[1];
			boolean isPlus = (e instanceof PreIncrement || 
							  e instanceof PostIncrement);
			if (op.length == 4) {
				// TODO test the following code.
				// unbox, apply operator, box back. Works only in special cases so far.
				Assignment a = (Assignment)e;
				Expression repl = 
					f.createCopyAssignment(
							a.getExpressionAt(0).deepClone(), 
							f.createMethodReference(
									f.createTypeReference(makeBoxIdentifierForType(si.getUnboxedType((ClassType)t))),
									f.createIdentifier("valueOf"),
									new ASTArrayList<Expression>(
											isPlus ?
													f.createPlus(
															f.createMethodReference(
																	(ReferencePrefix)a.getExpressionAt(0).deepClone(),
																	makeUnboxIdentifierForType((ClassType)t),
																	null
															),
															f.createIntLiteral(1)
													)
											:
												f.createMinus(
														f.createMethodReference(
																(ReferencePrefix)a.getExpressionAt(0).deepClone(),
																makeUnboxIdentifierForType((ClassType)t),
																null
														),
														f.createIntLiteral(1)
												)														
									)
							)
					);
				if ((Boolean)op[3])
					repl = f.createParenthesizedExpression(repl);
				if ((Boolean)op[2]) {
					// post inc/decrement.
					if (isPlus)
						repl = f.createMinus(repl, f.createIntLiteral(1));
					else
						repl = f.createPlus(repl, f.createIntLiteral(1));
					repl = f.createParenthesizedExpression(repl);
				}
				replace(e, repl);
			} else if (t instanceof ClassType) {
				Identifier id = makeUnboxIdentifierForType((ClassType)t);
				ReferencePrefix rp;
				if (e instanceof ReferencePrefix) {
					rp = (ReferencePrefix)e.deepClone();
				} else  {
					rp = f.createParenthesizedExpression(e.deepClone());
				}
				replacement = f.createMethodReference(rp, id);
				replace(e, replacement);
			} else { 
				Identifier id = makeBoxIdentifierForType((PrimitiveType)t);
				TypeReference tr = f.createTypeReference(id);
				replacement = f.createMethodReference(tr, 
						f.createIdentifier("valueOf"),
						new ASTArrayList<Expression>(
								e.deepClone()
						)
				);
				replace(e, replacement);
			}
		}
	}
	
	private Identifier makeUnboxIdentifierForType(ClassType t) {
		Identifier id;
		NameInfo ni = getNameInfo();
		ProgramFactory f = getProgramFactory();
		if (t == ni.getJavaLangBoolean()) {
			id = f.createIdentifier("booleanValue");
		} else if (t == ni.getJavaLangByte()) {
			id = f.createIdentifier("byteValue");
		} else if (t == ni.getJavaLangShort()) {
			id = f.createIdentifier("shortValue");
		} else if (t == ni.getJavaLangCharacter()) {
			id = f.createIdentifier("charValue");
		} else if (t == ni.getJavaLangInteger()) {
			id = f.createIdentifier("intValue");
		} else if (t == ni.getJavaLangLong()) {
			id = f.createIdentifier("longValue");
		} else if (t == ni.getJavaLangFloat()) {
			id = f.createIdentifier("floatValue");
		} else if (t == ni.getJavaLangDouble()) {
			id = f.createIdentifier("doubleValue");
		} else throw new Error("cannot unbox type " + t.getFullName() + " ("+t.getClass()+")\n");
		return id;
	}
	
	private Identifier makeBoxIdentifierForType(PrimitiveType t) {
		Identifier id;
		NameInfo ni = getNameInfo();
		ProgramFactory f = getProgramFactory();
		if (t == ni.getBooleanType()) {
			id = f.createIdentifier("Boolean");
		} else if (t == ni.getByteType()) {
			id = f.createIdentifier("Byte");
		} else if (t == ni.getShortType()) {
			id = f.createIdentifier("Short");
		} else if (t == ni.getCharType()) {
			id = f.createIdentifier("Character");
		} else if (t == ni.getIntType()) {
			id = f.createIdentifier("Integer");
		} else if (t == ni.getLongType()) {
			id = f.createIdentifier("Long");
		} else if (t == ni.getFloatType()) {
			id = f.createIdentifier("Float");
		} else if (t == ni.getDoubleType()) {
			id = f.createIdentifier("Double");
		} else {
			System.out.println("cannot box: " + t + " " + t);
			throw new Error();
		}
		return id;
	}
}


