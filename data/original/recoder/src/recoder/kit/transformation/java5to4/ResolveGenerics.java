/**
 * 
 */
package recoder.kit.transformation.java5to4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ProgramFactory;
import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.ErasedField;
import recoder.abstraction.ErasedMethod;
import recoder.abstraction.ErasedType;
import recoder.abstraction.IntersectionType;
import recoder.abstraction.Method;
import recoder.abstraction.ParameterizedField;
import recoder.abstraction.ParameterizedMethod;
import recoder.abstraction.ParameterizedType;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.ResolvedGenericMethod;
import recoder.abstraction.Type;
import recoder.abstraction.TypeArgument.CapturedTypeArgument;
import recoder.abstraction.TypeArgument.WildcardMode;
import recoder.abstraction.TypeParameter;
import recoder.abstraction.Variable;
import recoder.bytecode.ClassFile;
import recoder.bytecode.TypeParameterInfo;
import recoder.convenience.ForestWalker;
import recoder.io.PropertyNames;
import recoder.java.CompilationUnit;
import recoder.java.Expression;
import recoder.java.NonTerminalProgramElement;
import recoder.java.ProgramElement;
import recoder.java.Reference;
import recoder.java.Statement;
import recoder.java.StatementBlock;
import recoder.java.declaration.AnnotationDeclaration;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.DeclarationSpecifier;
import recoder.java.declaration.EnumDeclaration;
import recoder.java.declaration.InterfaceDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.ParameterDeclaration;
import recoder.java.declaration.Throws;
import recoder.java.declaration.TypeArgumentDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.TypeParameterDeclaration;
import recoder.java.declaration.VariableSpecification;
import recoder.java.reference.ConstructorReference;
import recoder.java.reference.MemberReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.ReferencePrefix;
import recoder.java.reference.TypeReference;
import recoder.java.reference.VariableReference;
import recoder.java.statement.Branch;
import recoder.java.statement.Try;
import recoder.kit.MethodKit;
import recoder.kit.MiscKit;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.kit.TypeKit;
import recoder.kit.transformation.java5to4.Util.IntroduceCast;
import recoder.list.generic.ASTArrayList;
import recoder.list.generic.ASTList;
import recoder.service.CrossReferenceSourceInfo;
import recoder.service.SourceInfo;

/**
 * @author Tobias Gutzmann
 *
 */
public class ResolveGenerics extends TwoPassTransformation {
	private List<CompilationUnit> cus;
	public ResolveGenerics(CrossReferenceServiceConfiguration sc, List<CompilationUnit> cus) {
		super(sc);
		this.cus = cus;
	}
	
	private List<NonTerminalProgramElement> toRemove;
	private List<IntroduceCast> casts;
	private List<TypeReference[]> referenceReplacements;
	private HashSet<MemberReference> castAllParams;
	private int guardCount = 0;
	private List<MethodDeclaration> guardMethods;
	private List<MemberReference> replaceIn;
	private List<Expression> tgts;
	private HashMap<VariableSpecification, Type> paramChanges;
	private ClassType runtimeException;
	
	@Override
	public ProblemReport analyze() {
		toRemove = new ArrayList<NonTerminalProgramElement>();
		casts = new ArrayList<IntroduceCast>();
		referenceReplacements = new ArrayList<TypeReference[]>();
		castAllParams = new HashSet<MemberReference>();
		guardMethods = new ArrayList<MethodDeclaration>();
		replaceIn = new ArrayList<MemberReference>();
		tgts = new ArrayList<Expression>();
		paramChanges = new HashMap<VariableSpecification, Type>();
		runtimeException = getNameInfo().getClassType("java.lang.RuntimeException");
		
		CrossReferenceSourceInfo si = getCrossReferenceSourceInfo();
		ForestWalker fw = new ForestWalker(cus);
		while (fw.next()) {
			ProgramElement pe = fw.getProgramElement();
			NonTerminalProgramElement parent = pe.getASTParent();
			boolean castAdded = false;
			if (pe instanceof VariableReference) {
				VariableReference vr = (VariableReference)pe;
				Variable v = si.getVariable(vr); 
				if (v instanceof ErasedField)
					v = ((ErasedField)v).getGenericField();
				else if (v instanceof ParameterizedField)
					v = ((ParameterizedField)v).getGenericField();
				Type t = paramChanges.get(v);
				boolean hit = t != null;
				if (t == null)
					t = si.getType(v);
				TypeParameter tp = isBaseTypeParameterOrArrayThereof(t);
				if (hit || tp != null) {
					castAdded = checkCastRequired(vr, t, Util.getRequiredContextType(getSourceInfo(), vr, false));
				}
			} else if (pe instanceof MethodReference || pe instanceof ConstructorReference) {
				MemberReference mr = (MemberReference)pe;
				// remove possible type args.
				ASTList<TypeArgumentDeclaration> typeArgs = 
					pe instanceof MethodReference ? ((MethodReference)pe).getTypeArguments()
							: null;
				removeTypeArgs(typeArgs);
				// add cast if required. 
				Method m = 
					pe instanceof MethodReference ? si.getMethod((MethodReference)pe)
							: si.getConstructor((ConstructorReference)pe);
				// find out: What is the "original" return type?
				// for this, we remove all possible generic stuff... 
				while (m instanceof ParameterizedMethod || m instanceof ResolvedGenericMethod || m instanceof ErasedMethod) {
					if (m instanceof ParameterizedMethod)
						m = ((ParameterizedMethod)m).getGenericMethod();
					else if (m instanceof ResolvedGenericMethod)
						m = ((ResolvedGenericMethod)m).getGenericMethod();
					else 
						m = ((ErasedMethod)m).getGenericMethod();
				}
				Type t = m.getReturnType(); 
				
				TypeParameter tp = isBaseTypeParameterOrArrayThereof(t);
				if (tp != null ) {
					Type req = Util.getRequiredContextType(si, (Expression)mr, false);
					castAdded = checkCastRequired(mr, t, req);
				} else if (t instanceof CapturedTypeArgument) {
					Type req = Util.getRequiredContextType(si, (Expression)mr, false);
					if (req != null) {
						casts.add(new IntroduceCast((Expression)mr, TypeKit.createTypeReference(si, 
								req, mr, false)));
						castAdded = true;
					}
				}
				// rare case (but happens): after removing generics, the method reference becomes ambiguous.
				// what do we do? We mark the method as such, and add type casts to each argument...
				ArrayList<Type> transSig = new ArrayList<Type>();
				for (Type param: si.makeSignature(mr instanceof MethodReference ? 
						((MethodReference)mr).getArguments() : ((ConstructorReference)mr).getArguments())) {
					if (param instanceof PrimitiveType || param instanceof ArrayType)
						transSig.add(param);
					else if (param instanceof ParameterizedType)
						transSig.add(((ParameterizedType)param).getGenericType().getErasedType());
					else if (param instanceof TypeParameter)
						transSig.add(getFirstBound((TypeParameter)param).getErasedType());
					else 
						transSig.add(((ClassType)param).getErasedType());
				}
				int i = 
					mr instanceof MethodReference ? 
							si.getMethods(m.getContainingClassType(), m.getName(), transSig, MiscKit.getParentTypeDeclaration(mr)).size()
						:	si.getConstructors(m.getContainingClassType(), transSig, MiscKit.getParentTypeDeclaration(mr)).size();
				if (i > 1) {
					castAllParams.add(mr);
				}
			} else if (pe instanceof TypeReference) {
				TypeReference tr = (TypeReference)pe;
				if (isPartOf(tr, TypeArgumentDeclaration.class)) {
					// will be removed anyway. ignore!
					continue;
				} 
				if (isPartOf(tr, ParameterDeclaration.class)) {
					// later / part of MethodDeclaration!
					if (MiscKit.getParentMemberDeclaration(tr) instanceof MethodDeclaration)
						continue;
				}
				// if type parameter, replace it.
				Type t = si.getType(tr);
				if (t == null || t instanceof PrimitiveType) {
					// void / primitive. ignore!
					continue;
				} else {
					TypeParameter tp = isBaseTypeParameterOrArrayThereof(t);
					if (tp != null) {
						// replace entire type reference
						int dim = tr.getDimensions();
						TypeReference newRef = TypeKit.createTypeReference(si, getFirstBound(tp), pe, false);
						newRef.setDimensions(dim);
						referenceReplacements.add(new TypeReference[] { tr, newRef});
						if (parent instanceof Throws) {
							// some extra handling required...
							handleTypeParameterInThrows((TypeReference)pe);
						}
					} else {
						// do this only if we don't replace the entire type reference after all.
						removeTypeArgs(tr.getTypeArguments());
					}
				}
			} else if (pe instanceof TypeDeclaration) {
				// remove type parameter
				if (!(pe instanceof TypeParameterDeclaration))
					removeTypeParams(((TypeDeclaration) pe).getTypeParameters());
			} else if (pe instanceof MethodDeclaration) {
				MethodDeclaration md = (MethodDeclaration)pe; 
				// remove type parameters
				removeTypeParams(md.getTypeParameters());

				// check signature. Need to replace parameters ?
				List<Method> redefined = MethodKit.getAllRedefinedMethods(md);
				List<Type> sig = md.getSignature();
				for (int i = 0; i < sig.size(); i++) {
					List<ClassType> supers = new ArrayList<ClassType>();
					Type orig = sig.get(i);
					TypeParameter tp_orig = isBaseTypeParameterOrArrayThereof(orig);
					boolean makeParamRepl = tp_orig != null;
					Type firstBound_orig = getFirstBound(tp_orig);
					if (firstBound_orig == null)
						firstBound_orig = orig;
					else {
						while (orig instanceof ArrayType) {
							firstBound_orig = firstBound_orig.createArrayType();
							orig = ((ArrayType)orig).getBaseType();
						}
					}
					if (firstBound_orig instanceof ClassType && !(firstBound_orig instanceof ArrayType))
						firstBound_orig = ((ClassType)firstBound_orig).getErasedType(); 
					for (Method m : redefined) {
						if (m instanceof ParameterizedMethod) 
							m = ((ParameterizedMethod)m).getGenericMethod();
						List<Type> sig_redef = m.getSignature();
						Type redef = sig_redef.get(i);
						TypeParameter tp_redef = isBaseTypeParameterOrArrayThereof(redef);
						if (tp_redef != null) {
							ClassType firstBound_redef = getFirstBound(tp_redef);
							if (firstBound_redef instanceof ParameterizedType)
								firstBound_redef = ((ParameterizedType)firstBound_redef).getGenericType();
							if (!(firstBound_orig instanceof ArrayType))
								firstBound_orig = ((ClassType)firstBound_orig).getErasedType();
							if (!(firstBound_redef instanceof ArrayType))
								firstBound_redef = firstBound_redef.getErasedType();
							if (firstBound_orig == firstBound_redef) continue;
							while (redef instanceof ArrayType) {
								firstBound_redef = firstBound_redef.createArrayType();
								redef = ((ArrayType)redef).getBaseType();
							}
							if (!(firstBound_redef instanceof ArrayType))
								firstBound_redef = firstBound_redef.getErasedType();
							supers.add(firstBound_redef);
						}
					}
					if (!supers.isEmpty()) {
						ClassType ct = si.getCommonSupertype(supers.toArray(new ClassType[supers.size()]));
						if (ct instanceof IntersectionType) {
							for (Type t : supers) {
								System.out.println(t.getFullName());
							}
							System.out.println("----");
						}
						if (!(ct instanceof ArrayType))
							ct = ct.getErasedType();
						ParameterDeclaration pd = md.getParameterDeclarationAt(i);
						TypeReference newTR = TypeKit.createTypeReference(getSourceInfo(), ct, md);
						if (pd.isVarArg()) {
							newTR.setDimensions(newTR.getDimensions()-1);
						}
						referenceReplacements.add(new TypeReference[] { pd.getTypeReference(), newTR });
						paramChanges.put(pd.getVariableSpecification(), ct);
					} else { 
						if (makeParamRepl) { 
							ParameterDeclaration pd = md.getParameterDeclarationAt(i);
							TypeReference newTR = TypeKit.createTypeReference(getSourceInfo(), firstBound_orig, md);
							if (pd.isVarArg()) {
								newTR.setDimensions(newTR.getDimensions()-1);
							}
							referenceReplacements.add(new TypeReference[] { pd.getTypeReference(), newTR });
							paramChanges.put(pd.getVariableSpecification(), firstBound_orig);
						} else {
							removeTypeArgs(md.getParameterDeclarationAt(i).getTypeReference().getTypeArguments());
						}
					}
				}
			}
			if (!castAdded 
					&& (parent instanceof ConstructorReference || parent instanceof MethodReference)
					&& castAllParams.contains(parent)) {
				if (parent instanceof ConstructorReference
					&& !((ConstructorReference)parent).getArguments().contains(pe))
						continue;
				if (parent instanceof MethodReference
					&& !((MethodReference)parent).getArguments().contains(pe))
						continue;
				// need to cast just in case...
				Type t = Util.getRequiredContextType(si, (Expression)pe, false);
				casts.add(new IntroduceCast((Expression)pe, 
						TypeKit.createTypeReference(si, t, pe, false)));
			}
		}
		return setProblemReport(NO_PROBLEM);
	}
	
	private void handleTypeParameterInThrows(TypeReference pe) {
		CrossReferenceSourceInfo crs = getCrossReferenceSourceInfo();
		ProgramFactory f = getProgramFactory();
		Throws thrs = (Throws)pe.getASTParent();
		MethodDeclaration md = thrs.getParent();
		List<MemberReference> mrs = crs.getReferences(md);
		for (int i = 0; i < mrs.size(); i++) {
			MethodReference mr = (MethodReference)mrs.get(i);
			Method methodReferenceResolved = crs.getMethod(mr);
			// if this is not a ResolvedGenericMethod, then it's use of a raw type.
			// in that case, we don't need to do anything - the first bound
			// that gets replaced everywhere already matches.
			if (methodReferenceResolved instanceof ResolvedGenericMethod) {
				ResolvedGenericMethod rgm = (ResolvedGenericMethod)methodReferenceResolved;
				// need to handle this if: (1) we're in a try-block, or (2) method doesn't declare the thrown type.
				List<ClassType> declaredExceptions = Collections.emptyList();
				NonTerminalProgramElement npe = mr;
				for (;;) {
					npe = npe.getASTParent();
					if (npe instanceof Try) {
						break; // must handle *all* exceptions (I think??)
					}
					if (npe instanceof MethodDeclaration) {
						declaredExceptions = ((MethodDeclaration)npe).getExceptions();
						break;
					}
					if (npe == null) {
						System.out.println("???");
						break; // hopefully we only got runtime exceptions, otherwise it would be weird.
					}
				}
				List<ClassType> e_afterTrafo = new ArrayList<ClassType>();
				for (ClassType ct : crs.getExceptions(rgm.getGenericMethod())) {
					e_afterTrafo.add(ct instanceof TypeParameter ? getFirstBound((TypeParameter)ct) : ct);
				}
				
				REMOVE_EXCEPTIONS: for (Iterator<ClassType> it = e_afterTrafo.iterator(); it.hasNext(); ) {
					ClassType exc = it.next();
					List<ClassType> excSupers = exc.getAllSupertypes();
					if (excSupers.contains(runtimeException)) {
						it.remove();
						continue REMOVE_EXCEPTIONS;
					} else {
						for (ClassType declExc : declaredExceptions) {
							if (excSupers.contains(declExc)) {
								it.remove();
								continue REMOVE_EXCEPTIONS;
							}
						}
					}
				}
				List<ClassType> actuallyPossible = crs.getExceptions(rgm);
				// e_afterTrafo now contains the exceptions that the compiler thinks may happen.
				if (e_afterTrafo.isEmpty()) continue;
				
				// create a guard-method.
				ASTList<DeclarationSpecifier> decls = new ASTArrayList<DeclarationSpecifier>(2);
				decls.add(f.createPrivate());
				if (methodReferenceResolved.isStatic())
					decls.add(f.createStatic()); // TODO: Can be static if top level class or static member class
				ASTList<ParameterDeclaration> pds = new ASTArrayList<ParameterDeclaration>(md.getParameterDeclarationCount()+1);
				ASTList<Expression> args = new ASTArrayList<Expression>(pds.size());
				if (!rgm.isStatic())
					pds.add(f.createParameterDeclaration(
						TypeKit.createTypeReference(crs, rgm.getContainingClassType(), mr), 
						f.createIdentifier("___callTgt")));
				for (ParameterDeclaration orig_pd : md.getParameters()) {
					Type ttt = crs.getType(orig_pd.getVariableSpecification());
					pds.add(f.createParameterDeclaration(
							TypeKit.createTypeReference(crs, ttt, mr, false), 
							orig_pd.getVariableSpecification().getIdentifier().deepClone()));
					args.add(f.createVariableReference(orig_pd.getVariableSpecification().getIdentifier().deepClone()));
				}
				TypeReference tr;
				if (crs.getType(mr) == null)
					tr = f.createTypeReference(f.createIdentifier("void"));
				else {
					Type t = crs.getType(mr);
					if (t instanceof TypeParameter) {
						t = ((TypeParameter)t).getBaseClassType();
					}
					tr = TypeKit.createTypeReference(crs, t, mr);
				}
				ASTList<TypeReference> _thrown = new ASTArrayList<TypeReference>(actuallyPossible.size());
				for (ClassType ct : actuallyPossible) {
					if (ct instanceof TypeParameter) {
						ct = ct.getBaseClassType();
					}
					_thrown.add(TypeKit.createTypeReference(crs, ct, mr));
				}
				Throws _throws = f.createThrows(_thrown);
				
				ASTList<Branch> catches = new ASTArrayList<Branch>();
				catches.add(f.createCatch(
						f.createParameterDeclaration(f.createTypeReference(f.createIdentifier("RuntimeException")), 
								f.createIdentifier("e")),
						f.createStatementBlock(new ASTArrayList<Statement>(
								f.createThrow(f.createVariableReference(f.createIdentifier("e")))
						))
				));
				for (ClassType exc : actuallyPossible) {
					if (exc instanceof TypeParameter) {
						exc = exc.getBaseClassType();
					}
					catches.add(f.createCatch(
							f.createParameterDeclaration(TypeKit.createTypeReference(crs, exc, mr), 
									f.createIdentifier("e")),
							f.createStatementBlock(new ASTArrayList<Statement>(
									f.createThrow(f.createVariableReference(f.createIdentifier("e")))
							))
					));
				}
				// must not happen -> wrap in "Error"!
				catches.add(f.createCatch(
						f.createParameterDeclaration(f.createTypeReference(f.createIdentifier("Throwable")), 
								f.createIdentifier("e")),
						f.createStatementBlock(new ASTArrayList<Statement>(
								f.createThrow(f.createNew(null, 
										f.createTypeReference(f.createIdentifier("Error")), 
										new ASTArrayList<Expression>(f.createVariableReference(f.createIdentifier("e")))))
						))
				));
				Statement stmnt = f.createMethodReference(
								md.isStatic() ? TypeKit.createTypeReference(crs, md.getMemberParent(), mr)
											  : f.createVariableReference(f.createIdentifier("___callTgt")),
											  md.getIdentifier().deepClone(),args
						);
				if (crs.getType(mr) != null)
					stmnt = f.createReturn((Expression)stmnt);
				ASTList<Statement> tryStmnts = new ASTArrayList<Statement>(stmnt);
				StatementBlock try_sb = f.createStatementBlock(tryStmnts);
				Try _try = f.createTry(try_sb, catches);
				
				StatementBlock sb = f.createStatementBlock(new ASTArrayList<Statement>(_try));
				MethodDeclaration guardMD = f.createMethodDeclaration(
						decls, tr, f.createIdentifier("___guard___" + guardCount++), pds, _throws, sb);
				guardMD.setMemberParent(MiscKit.getParentTypeDeclaration(mr));
				guardMethods.add(guardMD);
				replaceIn.add(mr);
				ReferencePrefix refPref = mr.getReferencePrefix();
				Expression tgt; 
				if (refPref instanceof Expression)
					tgt = ((Expression)mr.getReferencePrefix().deepClone());
				else if (md.isStatic())
					tgt = null;
				else
					tgt = f.createThisReference();
				tgts.add(tgt);
			}
		}
	}
	private boolean checkCastRequired(Reference mr, Type actual, Type required) {
		if (required == null)
			return false; // no cast required. 
		if (required == getNameInfo().getJavaLangObject() || required == getNameInfo().getJavaLangObject().getErasedType())
			return false; // obviously no cast required ;-)
		int dim = 0;
		while (actual instanceof ArrayType) {
			dim++;
			actual = ((ArrayType)actual).getBaseType();
			required = ((ArrayType)required).getBaseType();
		}
		if (isBaseTypeParameterOrArrayThereof(actual) != null)
			actual = getFirstBound(isBaseTypeParameterOrArrayThereof(actual));
		
		if (required instanceof CapturedTypeArgument) {
			required = dealWithCapturedTypeArgument((CapturedTypeArgument)required);
		}
		if (isBaseTypeParameterOrArrayThereof(required) != null) {
			int dim2 = 0;
			while (required instanceof ArrayType) {
				dim2++;
				required = ((ArrayType)required).getBaseType();
			}
			required = getFirstBound(isBaseTypeParameterOrArrayThereof(required));
			while (dim2-- > 0) {
				required = required.createArrayType();
			}
		}
		SourceInfo si = getSourceInfo();
		if (required instanceof PrimitiveType) {
			required = si.getBoxedType((PrimitiveType)required);
		}
		if (!si.isWidening(actual, required)) {
			TypeReference newTR = TypeKit.createTypeReference(si, required, mr, false);
			while (required instanceof ArrayType) {
				// yepp, this may happen if the type argument was an array type in the first place:
				// ...<int[]>, for example!
				required = ((ArrayType)required).getBaseType();
				dim++;
			}
			
			newTR.setDimensions(dim);
			casts.add(new IntroduceCast((Expression)mr, newTR));
			return true;
		}
		return false;
	}
	
	private ClassType getFirstBound(TypeParameter tp) {
		if (tp == null)
			return null;
		return tp.getBaseClassType();
	}

	private TypeParameter isBaseTypeParameterOrArrayThereof(Type t) {
		if (t == null)
			return null; // void type.
		while (t instanceof ArrayType)
			t = ((ArrayType)t).getBaseType();
		if (t instanceof CapturedTypeArgument)
			t = dealWithCapturedTypeArgument((CapturedTypeArgument)t);
		if (t instanceof ParameterizedType) {
			t = ((ParameterizedType)t).getGenericType();
		}
		if (t instanceof ErasedType)
			t = ((ErasedType)t).getGenericType();
		assert t.getClass() == ClassFile.class 
			|| t.getClass() == ClassDeclaration.class
			|| t.getClass() == InterfaceDeclaration.class
			|| t.getClass() == EnumDeclaration.class
			|| t.getClass() == AnnotationDeclaration.class
			|| t.getClass() == TypeParameterDeclaration.class
			|| t.getClass() == PrimitiveType.class
			|| t.getClass() == TypeParameterInfo.class : t.getClass();
		return t instanceof TypeParameter ? (TypeParameter)t : null;
	}

	private void removeTypeArgs(ASTList<TypeArgumentDeclaration> typeArguments) {
		if (typeArguments != null)
			for (int i = 0; i < typeArguments.size(); i++)
				toRemove.add(typeArguments.get(i));
	}
	
	private void removeTypeParams(ASTList<TypeParameterDeclaration> typeParameters) {
		if (typeParameters != null)
			for (int i = 0; i < typeParameters.size(); i++)
				toRemove.add(typeParameters.get(i));
	}


	// TODO copy and paste from old version + quickly adapted. Can probably be improved!!!
	private Type dealWithCapturedTypeArgument(CapturedTypeArgument ty) {
		Type res = null;
		TypeReference tRef = null;
		if (ty.getTypeArgument().getWildcardMode() == WildcardMode.None && ty.getTypeArgument() instanceof TypeArgumentDeclaration) {
			tRef = ((TypeArgumentDeclaration)ty.getTypeArgument()).getTypeReference();
		}
		else if (ty.getTypeArgument().getWildcardMode() == WildcardMode.Extends && ty.getTypeArgument() instanceof TypeArgumentDeclaration) {
			tRef = ((TypeArgumentDeclaration)ty.getTypeArgument()).getTypeReference();
		}
		//TODO what to do if Any
		else { //if (capTypeArg.getTypeArgument().getWildcardMode() == WildcardMode.Super || capTypeArg.getTypeArgument().getWildcardMode() == WildcardMode.Any) {
			return getNameInfo().getJavaLangObject();
		}
		res = getSourceInfo().getType(tRef);
		return res;
	}
    
	private static boolean isPartOf(ProgramElement pe, Class<? extends ProgramElement> c) {
        while (pe != null && !c.isInstance(pe)) {
            pe = pe.getASTParent();
        }
        return pe != null;
    }
	
	@Override
	public void transform() {
		super.transform();
		System.out.println("Remove: " + toRemove.size() + " repl: " + referenceReplacements.size() + " casts: " + casts.size()
				+ " prophylaxis performed: " + castAllParams.size() + " guard methods inserted: " + guardMethods.size());

		for (int i = guardMethods.size()-1; i >= 0; i--) {
			MethodDeclaration md = guardMethods.get(i);
			MethodReference mr = (MethodReference)replaceIn.get(i);
			Expression tgt = tgts.get(i);
			attach(md, md.getMemberParent(), 0);
			ProgramFactory f = getProgramFactory();
			ASTList<Expression> args = mr.getArguments() == null ? new ASTArrayList<Expression>(1) : mr.getArguments().deepClone();
			if (tgt != null)
				args.add(0, tgt);
			replace(mr, f.createMethodReference(md.getIdentifier().deepClone(), args));
		}

		for (VariableSpecification vd : paramChanges.keySet()) {
			// weird, yes, but necessary ;-)
			vd.setDimensions(0);
		}
		
		for (NonTerminalProgramElement nte : toRemove) {
			detach(nte);
		}
		// Here, the order probably doesn't matter, but we do it in reverse order anyway (leave to root)
		for (int i = referenceReplacements.size()-1; i >= 0; i--) {
			TypeReference[] trp = referenceReplacements.get(i);
			replace(trp[0], trp[1]);
		}
		ProgramFactory f = getProgramFactory();
		// This must be done in reverse order for sure!
		for (int i = casts.size()-1; i >= 0; i--) {
			IntroduceCast c = casts.get(i);
			replace(c.toBeCasted, f.createParenthesizedExpression(f.createTypeCast(c.toBeCasted.deepClone(), c.castedType)));
		}
		getServiceConfiguration().getProjectSettings().setProperty(PropertyNames.DO_NOT_CHECK_TYPE_ARGUMENTS_FOR_PARAMETER_MATCHES, "true");
	}
}

