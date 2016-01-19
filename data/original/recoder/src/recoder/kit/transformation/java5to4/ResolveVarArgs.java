/*
 * Created on 25.03.2006
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.kit.transformation.java5to4;

import java.util.ArrayList;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ProgramFactory;
import recoder.abstraction.ArrayType;
import recoder.abstraction.Method;
import recoder.abstraction.Type;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.Expression;
import recoder.java.ProgramElement;
import recoder.java.declaration.DeclarationSpecifier;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.ParameterDeclaration;
import recoder.java.expression.ArrayInitializer;
import recoder.java.expression.operator.NewArray;
import recoder.java.reference.ConstructorReference;
import recoder.java.reference.MemberReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.TypeReference;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.kit.TypeKit;
import recoder.list.generic.ASTArrayList;
import recoder.list.generic.ASTList;
import recoder.service.SourceInfo;

/**
 * Replaces references to var arg methods and var arg methods itself to make it
 * java 1.4 compliant. Does not change redefined/redefining from different 
 * compilation units, so that an entire compilation unit list can be analyzed first
 * before being executed. Note that this means that, if used on a single compilation
 * unit, this transformation may lead to incompilable code.
 * @author Tobias Gutzmann
 * @since 0.80
 *
 */
public class ResolveVarArgs extends TwoPassTransformation {
	private static class Item implements Util.DepSortable {
		final MemberReference memberReference;
		final TypeReference newTypeRef;
		final int firstVA;
		public ProgramElement getSortItem() {
			return memberReference;
		}
		Item(MemberReference mr, TypeReference newTypeRef, int firstVA) {
			memberReference = mr;
			this.newTypeRef = newTypeRef;
			this.firstVA = firstVA;
		}
	}
	
	private List<CompilationUnit> cul;
	private List<MethodDeclaration> varArgMeths;
	private List<Item> items;
	private List<TypeReference> newLastParamRefs;
	

	public ResolveVarArgs(CrossReferenceServiceConfiguration sc, CompilationUnit cu) {
		super(sc);
		cul = new ArrayList<CompilationUnit>();
		cul.add(cu);
	}
	
	public ResolveVarArgs(CrossReferenceServiceConfiguration sc, List<CompilationUnit> cul) {
		super(sc);
		this.cul = cul;
	}

	@Override
	public ProblemReport analyze() {
		SourceInfo si = getSourceInfo();
		varArgMeths = new ArrayList<MethodDeclaration>();
		items = new ArrayList<Item>();
		newLastParamRefs = new ArrayList<TypeReference>();
		TreeWalker tw;
		for (CompilationUnit cu : cul) {
			tw = new TreeWalker(cu);
			while (tw.next()) {
				ProgramElement pe = tw.getProgramElement();
				if (pe instanceof MethodDeclaration) {
					MethodDeclaration md = (MethodDeclaration)pe;
					if (md.isVarArgMethod()) {
						varArgMeths.add(md);
						Type t = si.getType(md.getParameters().get(md.getParameters().size()-1).getTypeReference()).createArrayType();
						newLastParamRefs.add(TypeKit.createTypeReference(getSourceInfo(), t, md));
						// that's all we do here. References are handled when we encounter 
						// a method reference during traversal.
					}
				}
				else if (pe instanceof MethodReference || pe instanceof ConstructorReference) {
					MemberReference mr = (MemberReference)pe;
					Method m = mr instanceof MethodReference ? 
							getSourceInfo().getMethod((MethodReference)mr) :
							getSourceInfo().getConstructor((ConstructorReference)mr);
					ASTList<Expression> args = mr instanceof MethodReference ?
							((MethodReference)mr).getArguments() :
							((ConstructorReference)mr).getArguments();
					if (m.isVarArgMethod()) {
						if (args != null && args.size() == m.getSignature().size()) {
							int idx = args.size() - 1;
							Type tt = getSourceInfo().getType(args.get(idx));
							if (!(tt instanceof ArrayType)) { // && tt.equals(getSourceInfo().getType(m.getSignature().get(idx))))) {
								List<Type> sig = m.getSignature();
								TypeReference tr = TypeKit.createTypeReference(getSourceInfo(),
										sig.get(sig.size()-1), mr);
								items.add(new Item(mr, tr, sig.size()-1));
							}
						}
						else {
							if (args == null) {
								if (mr instanceof MethodReference)
									((MethodReference)mr).setArguments(new ASTArrayList<Expression>(1)); // in preparation for the transformation
								else
									((ConstructorReference)mr).setArguments(new ASTArrayList<Expression>(1)); // in preparation for the transformation
							}
							List<Type> sig = m.getSignature();
							TypeReference tr = TypeKit.createTypeReference(getSourceInfo(),
									sig.get(sig.size()-1), mr);
							items.add(new Item(mr, tr, sig.size()-1));
						}
					}
				}
			}
		}
		return super.analyze();
	}

	@Override
	public void transform() {
		super.transform();
		ProgramFactory f = getProgramFactory();

		Util.sortCasts(items);
		System.out.println("refs: " + items.size() + " varArgMeths: " + varArgMeths.size());
		for (Item item : items) {
			int from = item.firstVA;
			int cnt = 0;
			ASTList<Expression> eml = new ASTArrayList<Expression>();
			if (item.memberReference instanceof MethodReference) {
				MethodReference methRef = (MethodReference)item.memberReference;
				cnt = methRef.getArguments() == null ? 0 : methRef.getArguments().size() - from;
				eml = new ASTArrayList<Expression>(cnt);
				for (int i = 0; i < cnt; i++) {
					eml.add(methRef.getArguments().get(from+i).deepClone());
				}
				ArrayInitializer ai = f.createArrayInitializer(eml);
				NewArray na = f.createNewArray(
					item.newTypeRef, 0, ai 
				);
				MethodReference repl = methRef.deepClone();
				while (cnt-- > 0)
					repl.getArguments().remove(repl.getArguments().size()-1);
				if (repl.getArguments() == null)
					repl.setArguments(new ASTArrayList<Expression>(0));
				repl.getArguments().add(na);
				repl.makeParentRoleValid();
				replace(item.memberReference, repl);
			}
			else if (item.memberReference instanceof ConstructorReference) {
				ConstructorReference cRef = (ConstructorReference)item.memberReference;
				cnt = cRef.getArguments() == null ? 0 : cRef.getArguments().size() - from;
				eml = new ASTArrayList<Expression>(cnt);
				for (int i = 0; i < cnt; i++) {
					eml.add(cRef.getArguments().get(from+i).deepClone());
				}
				ArrayInitializer ai = f.createArrayInitializer(eml);
				NewArray na = f.createNewArray(
					item.newTypeRef, 0, ai 
				);
				ConstructorReference repl = (ConstructorReference)cRef.deepClone();
				while (cnt-- > 0)
					repl.getArguments().remove(repl.getArguments().size()-1);
				if (repl.getArguments() == null)
					repl.setArguments(new ASTArrayList<Expression>(0));
				repl.getArguments().add(na);
				repl.makeParentRoleValid();
				replace(item.memberReference, repl);
			}
		}
		
		int idx = 0;
		for (MethodDeclaration md : varArgMeths) {
			MethodDeclaration repl = md.deepClone();
			List<ParameterDeclaration> pds = md.getParameters();
			ParameterDeclaration pd = pds.get(pds.size()-1);
			ASTList<DeclarationSpecifier> decls = null;
			if (pd.isFinal())
				decls = new ASTArrayList<DeclarationSpecifier>(f.createFinal());
			ParameterDeclaration newpd = f.createParameterDeclaration(
					decls,
//					TypeKit.createTypeReference(f,
//					getNameInfo().createArrayType(lastParamTypes.get(idx++))),
					newLastParamRefs.get(idx++),
					pd.getVariableSpecification().getIdentifier().deepClone()
			);
			newpd.setVarArg(false);
			replace(repl.getParameterDeclarationAt(repl.getParameterDeclarationCount()-1), newpd);
			repl.makeParentRoleValid();
			replace(md, repl);
		}
	}
}
