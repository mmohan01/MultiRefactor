/**
 * This file is part of the RECODER library and protected by the LGPL.
 */
package recoder.kit.transformation.java5to4;

import java.util.ArrayList;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ProgramFactory;
import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.ParameterizedType;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.Type;
import recoder.abstraction.TypeArgument;
import recoder.abstraction.Variable;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.Expression;
import recoder.java.LoopInitializer;
import recoder.java.NonTerminalProgramElement;
import recoder.java.ProgramElement;
import recoder.java.Statement;
import recoder.java.StatementBlock;
import recoder.java.declaration.LocalVariableDeclaration;
import recoder.java.declaration.TypeArgumentDeclaration;
import recoder.java.expression.operator.Conditional;
import recoder.java.expression.operator.TypeCast;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.ReferencePrefix;
import recoder.java.reference.TypeReference;
import recoder.java.reference.UncollatedReferenceQualifier;
import recoder.java.statement.EnhancedFor;
import recoder.java.statement.For;
import recoder.java.statement.LabeledStatement;
import recoder.kit.NameConflict;
import recoder.kit.Problem;
import recoder.kit.ProblemReport;
import recoder.kit.TransformationNotApplicable;
import recoder.kit.TwoPassTransformation;
import recoder.kit.TypeKit;
import recoder.kit.VariableKit;
import recoder.list.generic.ASTArrayList;
import recoder.list.generic.ASTList;

/**
 * converts an enhanced for loop to an "old style" for loop.
 * This follows JLS 3rd edition, §14.14.2.
 * 
 * Currently, if given enhanced for iterates over an array,
 * this will replace the enhanced for with a statement block and
 * not inline it into a possibly given statement block, yielding
 * possibly not nicely formatted (but fully functional) code.
 * 
 * @author Tobias Gutzmann
 * @since 0.80
 *
 */
public final class EnhancedFor2For extends TwoPassTransformation {
	private CrossReferenceServiceConfiguration sc;
	private EnhancedFor enhancedFor;
	private String iteratorName;
	private String arrayReferenceName;
	private Type guardType;
	private Type iteratorType;
	private boolean conditional;
	private List<CompilationUnit> cul;
	private List<Item> items;
	private int counterI = 0;
	private int counterA = 0;
	
	private static class Item {
		EnhancedFor enhancedFor;
		String iteratorName;
		String arrayReferenceName;
		Type guardType;
		Type iteratorType;
		boolean conditional;
		Item(EnhancedFor e, String iteratorName, String arrayReferenceName, Type guardType, Type iteratorType, boolean conditional) {
			this.enhancedFor = e;
			this.iteratorName = iteratorName;
			this.arrayReferenceName = arrayReferenceName;
			this.guardType = guardType;
			this.iteratorType = iteratorType;
			this.conditional = conditional;
		}
	}
	
	/**
	 * creates a new transformation.
	 * Note that if neither specifying iteratorName and arrayReferenceName,
	 * this transformation will always work.
	 * @param sc cross reference source configuration
	 * @param enhancedFor the EnhancedFor to be replaced
	 * @param iteratorName name for the iterator/int. if <code>null</code>,
	 * 			will find one automatically. 
	 * @param arrayReferenceName name for the array reference (if necessary).
	 * 			if <code>null</code>, will find one automatically.
	 */
	public EnhancedFor2For(CrossReferenceServiceConfiguration sc, EnhancedFor enhancedFor, String iteratorName, String arrayReferenceName) {
		super(sc);
		this.sc = sc;
		this.enhancedFor = enhancedFor;
		this.iteratorName = iteratorName;
		this.arrayReferenceName = arrayReferenceName;
		this.conditional = false;
	}
	
	public EnhancedFor2For(CrossReferenceServiceConfiguration sc, List<CompilationUnit> cul) {
		super(sc);
		this.sc = sc;
		this.cul = cul;
	}
	
	/**
	 * calls <code>EnhancedFor2For(sc, enhancedFor, null, null)</code>
	 * @param sc
	 * @param enhancedFor
	 */
	public EnhancedFor2For(CrossReferenceServiceConfiguration sc, EnhancedFor enhancedFor) {
		this(sc, enhancedFor, null, null);
	}
	
	private ProblemReport createItem() {
		conditional = false;
		iteratorName = null; // TODO make local variable, remove as field!?
		arrayReferenceName = null;
		guardType = sc.getSourceInfo().getType(enhancedFor.getGuard());
//		System.out.println("guardType " + guardType.getFullName() + " " + 
//				((guardType instanceof ParameterizedType) ? ((ParameterizedType)guardType).getTypeArgs().get(0).getTypeName() : "no TypeArgument")
//				+ " " + ((guardType instanceof ParameterizedType) ? (((ParameterizedType)guardType).getTypeParameters()) : ""));
		
		if (enhancedFor.getGuard() instanceof Conditional) {
			Conditional c = (Conditional)enhancedFor.getGuard();
			Type t1 = getSourceInfo().getType(c.getExpressionAt(1));
			Type t2 = getSourceInfo().getType(c.getExpressionAt(2));
			if (guardType instanceof recoder.abstraction.IntersectionType || (t1 != t2 && 
				!(t1 instanceof PrimitiveType && t2 instanceof PrimitiveType) &&
				!(t1 == getNameInfo().getNullType()) && !(t2 == getNameInfo().getNullType()) &&
				!getSourceInfo().isWidening(t1, t2) && !getSourceInfo().isWidening(t2, t1)
				)) {
				TypeReference tmp = TypeKit.createTypeReference(getProgramFactory(), guardType);
				if (t1 instanceof ParameterizedType) {
					ParameterizedType p = (ParameterizedType)t1;
					ASTList<TypeArgumentDeclaration> tadList = new ASTArrayList<TypeArgumentDeclaration>(p.getTypeArgs().size());
					for (TypeArgument tad : p.getTypeArgs()) {
						tadList.add(((TypeArgumentDeclaration)tad).deepClone());
//							System.out.println("System.out.println I " + tad.getTypeName());
					}
					tmp.setTypeArguments(tadList);
//						guardType = getSourceInfo().getType(tmp);
				}
				System.err.println("Transformation not applicable: run MakeConditionalCompatible first!");
				return setProblemReport(new TransformationNotApplicable(new MakeConditionalCompatible(sc,(Conditional)enhancedFor.getGuard())));
			}
		}
		if (enhancedFor.getGuard() instanceof Conditional && !(guardType instanceof ArrayType)) {
			iteratorType = getNameInfo().getType("java.util.Iterator");
			TypeReference tmpTR = TypeKit.createTypeReference(getProgramFactory(), guardType, true);
			tmpTR.makeParentRoleValid();
			// ugly: make a parent link valid for querying. Will be resolved with new NameInfo-Query methods
			// TODO 0.90
			tmpTR.setParent((LocalVariableDeclaration)enhancedFor.getInitializers().get(0));
			TypeReference tmpTR2 = TypeKit.createTypeReference(getProgramFactory(), iteratorType, false);
			if(tmpTR.getTypeArguments() != null) {
				tmpTR2.setTypeArguments(tmpTR.getTypeArguments());
				iteratorType = ((ParameterizedType)guardType).getProgramModelInfo().getParameterizedType((ClassType)iteratorType, (java.util.List<TypeArgumentDeclaration>)tmpTR.getTypeArguments());
			}
			conditional = true;
		} else if (guardType instanceof ClassType && !(guardType instanceof ArrayType)) {
			MethodReference mr = null;
			if (enhancedFor.getGuard() instanceof TypeCast) {
				ReferencePrefix rp = getProgramFactory().createParenthesizedExpression(enhancedFor.getGuard());
				mr = getProgramFactory().createMethodReference(
						rp,
						getProgramFactory().createIdentifier("iterator")
				);
			}
			else {
				mr = getProgramFactory().createMethodReference(	
						(ReferencePrefix)enhancedFor.getGuard(),
						getProgramFactory().createIdentifier("iterator")
				);
			}
			mr.setExpressionContainer(enhancedFor);
			iteratorType = sc.getSourceInfo().getType(mr);
//			TypeReference tmp = TypeKit.createTypeReference(getProgramFactory(), iteratorType, false);
//			TypeReference tmp2 = TypeKit.createTypeReference(getProgramFactory(), guardType, true);
//			if (tmp2.getTypeArguments() != null && ((ParameterizedType)guardType).getTypeParameters() == null) tmp.setTypeArguments(tmp2.getTypeArguments());
//			else tmp.setTypeArguments(new ASTArrayList<TypeArgumentDeclaration>());
//			tmp.setParent(mr);
//			iteratorType = sc.getSourceInfo().getType(tmp);
		} else if (guardType instanceof ArrayType) {
			iteratorType = null;
		} else throw new IllegalStateException("Broken Model");
		
//		if (iteratorType != null) System.out.println(iteratorType.getFullName());
		
		if (iteratorName != null) {
			Variable v = sc.getSourceInfo().getVariable(iteratorName, enhancedFor.getASTParent());
			if (v != null) {
				return setProblemReport(new NameConflict(v));
			}
		} else {
//			iteratorName = "i";
//			int i = 0;
//			while( sc.getSourceInfo().getVariable(iteratorName, enhancedFor) != null) {
//				iteratorName = "i" + ++i;
//			}
			iteratorName = VariableKit.createValidVariableName(sc.getSourceInfo(), enhancedFor.getASTParent(), "i" + counterI);
			counterI++;
		}
		if (arrayReferenceName != null) {
			Variable v = sc.getSourceInfo().getVariable(arrayReferenceName, enhancedFor.getASTParent());
			if (v != null) {
				return setProblemReport(new NameConflict(v));
			}
		} else {
			if (iteratorType != null) {
				arrayReferenceName = VariableKit.createValidVariableName(getSourceInfo(), enhancedFor.getASTParent(), "l" + counterA);
			}
			else arrayReferenceName = VariableKit.createValidVariableName(sc.getSourceInfo(), enhancedFor.getASTParent(), "a" + counterA);
			counterA++;
		}
		items.add(new Item(enhancedFor, iteratorName, arrayReferenceName, guardType, iteratorType, conditional));
		return setProblemReport(NO_PROBLEM);
	}

	@Override
	public ProblemReport analyze() {
		items = new ArrayList<Item>();
		setProblemReport(NO_PROBLEM);
		if (cul != null) {
			TreeWalker tw;
			for (CompilationUnit cu : cul) {
				tw = new TreeWalker(cu);
	    		while (tw.next()) {
	    			ProgramElement pe = tw.getProgramElement();
	    			if (pe instanceof EnhancedFor) {
	    				enhancedFor = (EnhancedFor)pe;
	    				ProblemReport tmp = createItem();
	    				if (tmp instanceof Problem) {
	    					System.err.println("Problem " + cu.getName() + " " + tmp.getClass());
	    					return tmp;
	    				}
	    			}
	    		}
			}
			return NO_PROBLEM;
		}
		else if (enhancedFor != null) {
			return createItem();
		}
		else return NO_PROBLEM;
	}	

	private boolean isChild(NonTerminalProgramElement ex1, Statement ex2) {
		Statement tmp;
		boolean result = false;
		for (int c = 0; c < ex1.getChildCount(); c++) {
			if (ex1.getChildAt(c) instanceof Statement) {
				tmp = (Statement)ex1.getChildAt(c);
				if (tmp.equals(ex2)) return true;
				else if (ex1.getChildAt(c) instanceof NonTerminalProgramElement) {
					result = isChild((NonTerminalProgramElement) ex1.getChildAt(c), ex2);
					if (result) return true;
				}
			}
			else if(ex1.getChildAt(c) instanceof NonTerminalProgramElement) {
				result = isChild((NonTerminalProgramElement)ex1.getChildAt(c),ex2);
				if (result) return true;
			}
		}
		return result;
	}
	
	public void sortItems(List<Item> list) {
		boolean changed = true;
		Statement ex1, ex2;
		Item tmp;
		NonTerminalProgramElement ntpe = null;
		while(changed) {
			changed = false;
			for (int i = 0; i < list.size() - 1; i++) {
				for (int j = i+ 1; j < list.size(); j++) {
					ex1 = list.get(i).enhancedFor;
					ex2 = list.get(j).enhancedFor;
					if (ex1 instanceof NonTerminalProgramElement) {
						ntpe = (NonTerminalProgramElement)ex1;
						if (isChild(ntpe, ex2)) {
							tmp = list.get(i);
							list.set(i, list.get(j));
							list.set(j, tmp);
							changed = true;
						}
					}
				}
			}
		}
	}
		

	@Override
	public void transform() {
		super.transform();
		ProgramFactory pf = getProgramFactory();
		System.out.println("EnhancedFors: " + items.size());
		LoopInitializer init;
		Expression guard;
		ASTList<Expression> update;
		LocalVariableDeclaration firstStmnt;
		sortItems(items);
		for (Item it : items) {
			// common part, initializer is set independently afterwards:
			firstStmnt = (((LocalVariableDeclaration)it.enhancedFor.getInitializers().get(0)).deepClone());
			
			if (it.iteratorType == null) {
				// array type
				init = pf.createLocalVariableDeclaration(
						null,
						TypeKit.createTypeReference(pf, "int"),
						pf.createIdentifier(it.iteratorName),
						pf.createIntLiteral(0)
				);
				UncollatedReferenceQualifier varRef = pf.createUncollatedReferenceQualifier(pf.createIdentifier(it.arrayReferenceName));
				FieldReference fRef = pf.createFieldReference(pf.createIdentifier("length"));
				fRef.setReferencePrefix(varRef);
				fRef.makeAllParentRolesValid();
				varRef.makeAllParentRolesValid();
				guard = pf.createLessThan(
					pf.createVariableReference(pf.createIdentifier(it.iteratorName)),
					fRef
				);
				update = new ASTArrayList<Expression>( pf.createPostIncrement(
							pf.createVariableReference(
									pf.createIdentifier(it.iteratorName)
							)
				));
				firstStmnt.getVariableSpecifications().get(0).setInitializer(
						pf.createArrayReference(
							pf.createVariableReference(pf.createIdentifier(it.arrayReferenceName)),
							new ASTArrayList<Expression>(
								pf.createVariableReference(pf.createIdentifier(it.iteratorName))
							)
						)
				);
			} else {
				// Iterable
				TypeReference tmp = null;
				/*if (it.iteratorType instanceof ParameterizedType && !(it.guardType instanceof ParameterizedType)) {
					tmp = TypeKit.createTypeReference(pf, it.iteratorType, false);
					if (((ParameterizedType)it.iteratorType).getTypeArgs().get(0).getTypeName().equals(((LocalVariableDeclaration)it.enhancedFor.getInitializers().get(0)).getTypeReference().getName())) {
						tmp = TypeKit.createTypeReference(pf, it.iteratorType, true);
					}
				}
				else*/ if (!(it.iteratorType instanceof ParameterizedType) && it.guardType instanceof ParameterizedType) {
					tmp = TypeKit.createTypeReference(pf,it.iteratorType,true);
					TypeReference tmp2 = TypeKit.createTypeReference(pf, it.guardType, true);
					if (((ParameterizedType)it.guardType).getTypeParameters() == null) tmp.setTypeArguments(tmp2.getTypeArguments().deepClone());
					else tmp.setTypeArguments(new ASTArrayList<TypeArgumentDeclaration>());
					tmp.makeAllParentRolesValid();
					iteratorType = sc.getSourceInfo().getType(tmp);
				} else {
//					System.out.println(it.iteratorType.getFullName());
					// TODO I outcommented the stuff below to fix a strange issue. It works, but the generated code is not so nice in certain cases.
					// It triggers a bug in case where a wildcard type arg is somewhere NOT on the first level of the iterator.
//					if (it.iteratorType instanceof ParameterizedType && ((ParameterizedType)it.iteratorType).getTypeArgs().get(0).getWildcardMode() == WildcardMode.Any) {
						tmp = TypeKit.createTypeReference(pf, it.iteratorType, false);
//					}
//					else {
//						tmp = TypeKit.createTypeReference(pf, it.iteratorType, true);
//					}
				}
				if (it.enhancedFor.getGuard() instanceof Conditional) {
					init = pf.createLocalVariableDeclaration(
							null,
							tmp,
							pf.createIdentifier(it.iteratorName),
							pf.createMethodReference(
									pf.createVariableReference(pf.createIdentifier(it.arrayReferenceName)),
									pf.createIdentifier("iterator")
							)
						);
				}
				else {
					ReferencePrefix pr;
					if (it.enhancedFor.getExpressionAt(0) instanceof TypeCast) {
						pr = getProgramFactory().createParenthesizedExpression(it.enhancedFor.getExpressionAt(0));
					}
					else {
						pr = (ReferencePrefix)it.enhancedFor.getExpressionAt(0).deepClone();
					}
					init = pf.createLocalVariableDeclaration(
							null,
							tmp,
							pf.createIdentifier(it.iteratorName),
							pf.createMethodReference(
									pr,
									pf.createIdentifier("iterator")
							)
						);
				}
	
				guard = pf.createMethodReference(
						pf.createVariableReference(pf.createIdentifier(it.iteratorName)),
						pf.createIdentifier("hasNext")
					);
				update = null;
				TypeReference tr = firstStmnt.getTypeReference().deepClone();
				if (tr.getDimensions() == 0) {
					// TODO not so nice HACK 
					if (tr.getName().equals("int"))
						tr.setIdentifier(tr.getFactory().createIdentifier("Integer"));
					else if (tr.getName().equals("short"))
						tr.setIdentifier(tr.getFactory().createIdentifier("Short"));
					else if (tr.getName().equals("boolean"))
						tr.setIdentifier(tr.getFactory().createIdentifier("Boolean"));
					else if (tr.getName().equals("byte"))
						tr.setIdentifier(tr.getFactory().createIdentifier("Byte"));
					else if (tr.getName().equals("char"))
						tr.setIdentifier(tr.getFactory().createIdentifier("Character"));
					else if (tr.getName().equals("double"))
						tr.setIdentifier(tr.getFactory().createIdentifier("Double"));
					else if (tr.getName().equals("float"))
						tr.setIdentifier(tr.getFactory().createIdentifier("Float"));
					else if (tr.getName().equals("long"))
						tr.setIdentifier(tr.getFactory().createIdentifier("Long"));
					tr.makeParentRoleValid();
				}				
				firstStmnt.getVariableSpecifications().get(0).setInitializer(
						pf.createTypeCast(
					pf.createMethodReference(pf.createVariableReference(pf.createIdentifier(it.iteratorName)), pf.createIdentifier("next")
							) ,
							tr
						)
				);
			}
	
			ASTList<Statement> statements = new ASTArrayList<Statement>(2);
			
			statements.add(firstStmnt);	
			if (it.enhancedFor.getStatementCount() > 0) {
				// if statement block, go into it
				Statement s = it.enhancedFor.getStatementAt(0);
				if (s instanceof StatementBlock) {
					StatementBlock sb = (StatementBlock)s;
					for(int i = 0; i < sb.getStatementCount(); i++) {
						statements.add(sb.getStatementAt(i).deepClone());
					}
				} else {
					statements.add(s.deepClone());
				}
			}
			
			For newFor = getProgramFactory().createFor(new ASTArrayList<LoopInitializer>(init),guard, update, getProgramFactory().createStatementBlock(statements));
			newFor.makeAllParentRolesValid();
			
			Statement replacement;
			Statement replacee;
			
			if (it.iteratorType == null || it.conditional) {
				ASTArrayList<Statement> sml = new ASTArrayList<Statement>(2);
				// create array name reference
				LocalVariableDeclaration lvd = pf.createLocalVariableDeclaration(
					null,
					TypeKit.createTypeReference(pf, ((LocalVariableDeclaration)it.enhancedFor.getInitializers().get(0)).getVariables().get(0).getType().createArrayType(), true),
					pf.createIdentifier(it.arrayReferenceName),
					it.enhancedFor.getGuard().deepClone()
				);
				sml.add(lvd);
				
				// clone labels
				Statement s;
				s = newFor;
				replacee = it.enhancedFor;
				while (replacee.getASTParent() instanceof LabeledStatement) {
					replacee = (LabeledStatement)replacee.getASTParent();
					s = getProgramFactory().createLabeledStatement(((LabeledStatement)replacee).getIdentifier().deepClone(), s);
				}
				sml.add(s);
				replacement = pf.createStatementBlock(sml);
			} else {
				replacement = newFor;
				replacee = it.enhancedFor;
			}
			if (replacee.getComments() != null) {
				replacement.setComments(replacee.getComments().deepClone());
			}
//			replace(replacee, replacee.deepClone());
			replace(replacee, replacement);
		}
	}
}
