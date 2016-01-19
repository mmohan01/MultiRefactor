/*
 * Created on 25.03.2006
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.kit.transformation.java5to4;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.Method;
import recoder.abstraction.ResolvedGenericMethod;
import recoder.abstraction.Type;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.Import;
import recoder.java.NonTerminalProgramElement;
import recoder.java.ProgramElement;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MemberReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.NameReference;
import recoder.java.reference.ReferenceSuffix;
import recoder.java.reference.TypeReference;
import recoder.java.statement.Case;
import recoder.kit.MiscKit;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.service.DefaultSourceInfo;

/**
 * Removes static imports from a given Compilation Unit and adds 
 * qualification prefixes to (possible) uses of such imports.
 * 
 * @author Tobias Gutzmann
 * @since 0.80
 *
 */
public class RemoveStaticImports extends TwoPassTransformation {
	private List<CompilationUnit> cul;
	
	private static class Item {
		NameReference r;
		TypeReference prefix;
		Item(NameReference r, TypeReference prefix) {
			this.r = r;
			this.prefix = prefix;
		}
	}
	
	private List<Item> hotSpots;
	
	private List<Import> allStatics;
	
	/**
	 * @param sc
	 */
	public RemoveStaticImports(CrossReferenceServiceConfiguration sc, CompilationUnit cu) {
		super(sc);
		cul = new ArrayList<CompilationUnit>();
		cul.add(cu);
	}
	
	public RemoveStaticImports(CrossReferenceServiceConfiguration sc, List<CompilationUnit> cul) {
		super(sc);
		this.cul = cul;
	}

	@Override
	public ProblemReport analyze() {
		hotSpots = new ArrayList<Item>();
		allStatics = new ArrayList<Import>();

		// are there any static imports at all?
		for (CompilationUnit cu : cul) {
			List<Import> statics = new ArrayList<Import>();
			List<Import> il = cu.getImports();
			if (il == null || il.isEmpty())
				continue;
			for (int i = 0, s = il.size(); i < s; i++) {
				Import im = il.get(i);
				if (im.isStaticImport())
					statics.add(im);
			}
			if (statics.isEmpty())
				continue;
			
			// traverse tree, consider member references only
			TreeWalker tw = new TreeWalker(cu);
			while (tw.next()) {
				ProgramElement pe = tw.getProgramElement();
				if (pe instanceof MemberReference && pe instanceof ReferenceSuffix && pe instanceof NameReference) {
					MemberReference r = (MemberReference)pe;
					ReferenceSuffix s = (ReferenceSuffix)pe;
					NameReference nr = (NameReference)pe;
					if (s.getReferencePrefix() != null) 
						continue; // not found through a static import

					ClassType targetCT;
					if (r instanceof MethodReference) {
						targetCT = getSourceInfo().getMethod((MethodReference)r).getContainingClassType();
					} else if (r instanceof FieldReference) {
						targetCT = getSourceInfo().getField((FieldReference)r).getContainingClassType();
						if (targetCT.isEnumType() && r.getASTParent() instanceof Case)
							continue; // must not be fully qualified!
					} else if (r instanceof TypeReference) {
						Type t = getSourceInfo().getType((TypeReference)r);
						while (t instanceof ArrayType)
							t = ((ArrayType)t).getBaseType();
						if (!(t instanceof ClassType))
							continue;
						targetCT = (ClassType)t;
						while (targetCT.getContainingClassType() != null)
							targetCT = targetCT.getContainingClassType();
					} else {
						continue;
					}
					if (targetCT instanceof TypeDeclaration && MiscKit.getParentTypeDeclaration((TypeDeclaration)targetCT) == targetCT)
						continue;
					String n = nr.getName();
					for (int i = 0, si = statics.size(); i < si; i++) {
						Import im = statics.get(i);
						TypeReference tr = im.getTypeReference(); // has to be set!
						
						DefaultSourceInfo dsi = (DefaultSourceInfo)getSourceInfo();
						
						if (im.isMultiImport()) {
							if (pe instanceof FieldReference) {
								if (dsi.getField((FieldReference)pe) ==
										dsi.getVariableFromStaticOnDemandImport(((FieldReference)pe).getName(), 
										Collections.singletonList(im), MiscKit.getParentTypeDeclaration(pe)))
								{
									// hit!
								} else continue; 
							}
							else if (pe instanceof MethodReference) {
								Method res = dsi.getMethod((MethodReference)pe);
								if (res instanceof ResolvedGenericMethod)
									res = ((ResolvedGenericMethod)res).getGenericMethod();
								List<Method> meths = dsi.getMethodsFromStaticOnDemandImports((MethodReference)pe,
										Collections.singletonList(im));
								if (meths.size() == 1 && res == meths.get(0)
									) {
									// hit!
								} else continue;
							}
							else {
								Type res = dsi.getType((TypeReference)pe);
								ClassType type = dsi.getFromPackageImports(((TypeReference)pe).getName(), 
										Collections.singletonList(im), 
										cu.getTypeDeclarationCount() == 0 ? null : cu.getTypeDeclarationAt(0));
								if (type != res)
									continue;
								// type reference...
								// TODO may still not be it... unlikely, but yet...
								// (different type import may match)
								// however, the way we currently handle this, no harm is done...
								if (r instanceof TypeReference && n.equals(im.getTypeReference().getName()))
									continue;
							}
						} else {
							if (pe instanceof FieldReference) {
								if (dsi.getField((FieldReference)pe) ==
									dsi.getVariableFromStaticSingleImport(((FieldReference)pe).getName(), 
											Collections.singletonList(im), MiscKit.getParentTypeDeclaration(pe))) {
									// hit!
								} else continue;
							} else if (pe instanceof MethodReference) {
								Method res = dsi.getMethod((MethodReference)pe);
								if (res instanceof ResolvedGenericMethod)
									res = ((ResolvedGenericMethod)res).getGenericMethod();
								List<Method> meths = dsi.getMethodsFromStaticSingleImports( 
										(MethodReference)pe, Collections.singletonList(im));
								if (meths.size() == 1 && res == meths.get(0)) {
									// hit!
								} else continue;
							} else {
								// type reference
								if ((ClassType)getSourceInfo().getType(tr) != targetCT)
									continue;
								if (!n.equals(im.getStaticIdentifier().getText()))
									continue;
								// TODO check: if another import matches, I *think*
								// that should be a semantic error
								// however, the way we currently handle this, no harm is done...
							}
						}
						TypeReference prefix = im.getTypeReference();
						hotSpots.add(new Item(nr,prefix));
						break;
					}
				}
			}
			allStatics.addAll(statics);
		}
		return super.analyze();
	}

	private boolean isChild(NonTerminalProgramElement nr1, NameReference nr2) {
		NameReference tmp;
		for (int c = 0; c < nr1.getChildCount(); c++) {
			if (nr1.getChildAt(c) instanceof NameReference) {
				tmp = (NameReference)nr1.getChildAt(c);
				if (tmp.equals(nr2)) return true;
				else return isChild(tmp, nr2);
			}
			else if(nr1.getChildAt(c) instanceof NonTerminalProgramElement)
				return isChild((NonTerminalProgramElement)nr1.getChildAt(c),nr2);
		}
		return false;
	}
	
	private void sortReferences(List<Item> its) {
		boolean changed = true;
		Item it1,it2,tmp;
		while (changed) {
			changed = false;
			for (int i = 0; i < its.size() - 1; i++) {
				for (int j = i + 1; j < its.size(); j++) {
					it1 = its.get(i);
					it2 = its.get(j);
					if (isChild(it1.r,it2.r)) {
						tmp = its.get(i);
						its.set(i, it2);
						its.set(j, tmp);
						changed = true;
//						System.out.println("changed");
					}
				}
			}
		}
	}
	
	@Override
	public void transform() {
		super.transform();
		sortReferences(hotSpots);
		System.out.println("static imports: " + allStatics.size() + " hotSpots: " + hotSpots.size());
		for (Import i : allStatics) {
			detach(i);
		}
		for (Item hs : hotSpots) {
			// TODO take care of not adding java.lang again. Makes nicer code.
//			String x = Naming.toPathName(hs.prefix, hs.r.getName());
//			if (x.startsWith("java.lang."))
//				x = x.substring(10);
			MiscKit.unindent(hs.r);
			if (hs.r instanceof MethodReference) {
				MethodReference mr = (MethodReference)hs.r;
				TypeReference tr = hs.prefix.deepClone();
				mr.setReferencePrefix(tr);
				mr.makeParentRoleValid();
				getChangeHistory().attached(tr);
			}
			else if (hs.r instanceof TypeReference) {
				attach(hs.prefix.deepClone(), (TypeReference)hs.r);
			} else {
				FieldReference fr = (FieldReference)hs.r;
				TypeReference tr = hs.prefix.deepClone();
				fr.setReferencePrefix(tr);
				fr.makeParentRoleValid();
				getChangeHistory().attached(tr);
			}
		}
	}
}
