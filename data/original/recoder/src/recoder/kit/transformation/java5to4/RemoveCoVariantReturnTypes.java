/*
 * Created on 31.03.2006
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.kit.transformation.java5to4;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ProgramFactory;
import recoder.abstraction.ArrayType.ArrayCloneMethod;
import recoder.abstraction.ClassType;
import recoder.abstraction.ErasedMethod;
import recoder.abstraction.Member;
import recoder.abstraction.Method;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.Type;
import recoder.bytecode.MethodInfo;
import recoder.convenience.ForestWalker;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.StatementContainer;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.expression.operator.TypeCast;
import recoder.java.reference.MemberReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.TypeReference;
import recoder.kit.MethodKit;
import recoder.kit.MiscKit;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.kit.TypeKit;
import recoder.kit.transformation.java5to4.Util.IntroduceCast;
import recoder.service.CrossReferenceSourceInfo;
import recoder.service.SourceInfo;

/**
 * Currently requires generics to be already resolved. It does *not* 
 * add type arguments to return type replacements.
 * @author Tobias Gutzmann
 *
 */
public class RemoveCoVariantReturnTypes extends TwoPassTransformation {

	private static class ReturnTypeRefReplacement {
		TypeReference typeParamRef;
		TypeReference replacement;
		ReturnTypeRefReplacement(TypeReference from, TypeReference to) {
			this.typeParamRef = from;
			this.replacement = to;
		}
	}
	
	private Dictionary<Method, Type> visitedMethods;
	private List<CompilationUnit> cul;
	private List<IntroduceCast> casts;
	private List<ReturnTypeRefReplacement> covariantReturnTypes;
	
	public RemoveCoVariantReturnTypes(CrossReferenceServiceConfiguration sc, List<CompilationUnit> cul) {
		super(sc);
		this.cul = cul;
	}
	
	private void handleTypeDeclaration(TypeDeclaration td) {
		CrossReferenceSourceInfo si = getCrossReferenceSourceInfo();
		for (Method m : td.getMethods()) {
			if (m.getReturnType() == null)
				continue; // void
			if (visited(m) != null)
				continue; // already seen.
			if (m.getReturnType() instanceof PrimitiveType)
				continue; // no covariance allowed.
		    // Note: we can NOT use MethodKit.getAllRedefinedMethods!!
			List<Method> meths = MethodKit.getAllRelatedMethods(si, m);

			for (Method toadd : meths) {
				// marks the method to not be considered again.
				visitedMethods.put(toadd, getNameInfo().getUnknownType()); // may be overwritten later.
			}
			if (meths.size() > 1) {
				ClassType returnType = null;
				// find most general type.
				for (Method redefined : meths) {
					ClassType rt = (ClassType)redefined.getReturnType();
					rt = rt.getBaseClassType();
					if (returnType == null) {
						returnType = rt;
						continue;
					}
					if (returnType == rt)
						continue;
					if (si.isSubtype(returnType, rt))
						returnType = rt;
					else if (!si.isSubtype(rt, returnType)) {
						returnType = getNameInfo().getJavaLangObject();
						break;
					}
				}
				for (Method redefined : meths) {
					if (redefined instanceof MethodDeclaration &&
							returnType != redefined.getReturnType())
						createItem((MethodDeclaration)redefined, returnType);
				}
			}
		}
	}
	
	@Override
	public ProblemReport analyze() {
		this.visitedMethods = new Hashtable<Method, Type>();
		this.casts = new ArrayList<IntroduceCast>();
		this.covariantReturnTypes = new ArrayList<ReturnTypeRefReplacement>();
		
		SourceInfo si = getSourceInfo();
		// first make a forest walk, finding all method references that target bytecode directly
		// and all type declarations (including anonymous types).
		ForestWalker fw = new ForestWalker(cul);
		while (fw.next()) {
			ProgramElement pe = fw.getProgramElement();
			if (pe instanceof TypeDeclaration) {
				handleTypeDeclaration((TypeDeclaration)pe);
			}
			else if (pe instanceof MethodReference) {
				MethodReference mr = (MethodReference)pe;
				Method m = si.getMethod(mr);
				if (m instanceof ErasedMethod)
					m = ((ErasedMethod)m).getGenericMethod();
				if (m instanceof ArrayCloneMethod) {
					Type req = Util.getRequiredContextType(si, mr, true);
					if (!(mr.getASTParent() instanceof TypeCast) && req != null && req != getNameInfo().getJavaLangObject())
						casts.add(new IntroduceCast(mr, TypeKit.createTypeReference(si, m.getReturnType(), mr, false)));
				} else if (m instanceof MethodInfo) {
					Type retType = m.getReturnType();
					if (retType == null || retType instanceof PrimitiveType)
						continue;
					List<Method> lm = MethodKit.getAllRedefinedMethods(m); // here we can (hopefully) use this.
					if (!lm.isEmpty()) {
						List<ClassType> types = new ArrayList<ClassType>(lm.size());
						for (Method om : lm)
							types.add((ClassType)om.getReturnType());
						ClassType newCT = si.getCommonSupertype(types.toArray(new ClassType[lm.size()]));
						ClassType req = (ClassType)Util.getRequiredContextType(si, mr, true);
						if (req != null && !si.isSubtype(newCT, req))
							casts.add(new IntroduceCast(mr, TypeKit.createTypeReference(si, req, mr, false)));
					}
				}
			}
		}
		if (casts.isEmpty() && covariantReturnTypes.isEmpty())
			return setProblemReport(IDENTITY);
		return super.analyze();
	}
	
	private Type visited(Method md) {
		return visitedMethods.get(md);
	}	

	private void createItem(MethodDeclaration md, ClassType originalType) {
		visitedMethods.put(md, originalType);

		List<MemberReference> references = getCrossReferenceSourceInfo().getReferences(md);
		TypeReference newReturnTypeReference = TypeKit.createTypeReference(getSourceInfo(), originalType, md, false);
		covariantReturnTypes.add(new ReturnTypeRefReplacement(md.getTypeReference(), newReturnTypeReference));
		
		for (MemberReference memRef : references) {
			MethodReference mr = (MethodReference)memRef;
			
			Type reqType = Util.getRequiredContextType(getSourceInfo(), mr, true);
			if (reqType instanceof PrimitiveType)
				reqType = getSourceInfo().getBoxedType((PrimitiveType)reqType);
			if (reqType == null || getSourceInfo().isSubtype(originalType, (ClassType)reqType))
				continue; // skip this one, cast is not necessary.
			if (!getSourceInfo().isVisibleFor((Member)reqType, MiscKit.getParentTypeDeclaration(mr))) {
				reqType = getSourceInfo().getType(mr); // need to use a visible type!
			}
			casts.add(new IntroduceCast(mr, TypeKit.createTypeReference(getSourceInfo(), reqType, mr, false)));
		}
	}
	
	@Override
	public void transform() {
		super.transform();
		System.out.println("casts: " + casts.size() + " covariantReturnTypes: " + covariantReturnTypes.size());
		ProgramFactory f = getProgramFactory();
		for (ReturnTypeRefReplacement rtrr : covariantReturnTypes) {
			if (rtrr.typeParamRef.getASTParent().getIndexOfChild(rtrr.typeParamRef) != -1)
				replace(rtrr.typeParamRef, rtrr.replacement);
		}
		Util.sortCasts(casts);
		for (IntroduceCast c : casts) {
			MiscKit.unindent(c.toBeCasted);
			if (c.toBeCasted.getASTParent().getIndexOfChild(c.toBeCasted) != -1 
				&&	!(c.toBeCasted.getASTParent() instanceof StatementContainer))
					replace(c.toBeCasted, f.createParenthesizedExpression(f.createTypeCast(c.toBeCasted.deepClone(), c.castedType)));
		}
	}
}
