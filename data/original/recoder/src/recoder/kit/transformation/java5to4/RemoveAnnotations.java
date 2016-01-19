/*
 * Created on 15.03.2006
 *
 * This file is part of the RECODER library and protected by the LGPL.
 * 
 */
package recoder.kit.transformation.java5to4;

import java.util.ArrayList;
import java.util.List;

import recoder.CrossReferenceServiceConfiguration;
import recoder.ProgramFactory;
import recoder.convenience.TreeWalker;
import recoder.java.CompilationUnit;
import recoder.java.Import;
import recoder.java.NonTerminalProgramElement;
import recoder.java.PackageSpecification;
import recoder.java.ProgramElement;
import recoder.java.declaration.AnnotationDeclaration;
import recoder.java.declaration.AnnotationPropertyDeclaration;
import recoder.java.declaration.AnnotationUseSpecification;
import recoder.java.declaration.InterfaceDeclaration;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.reference.PackageReference;
import recoder.java.reference.TypeReference;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.kit.TypeKit;
import recoder.list.generic.ASTArrayList;
import recoder.list.generic.ASTList;

/**
 * Deals with annotations. Removes all annotation use specification and
 * all annotation types, except those, that are used as regular interfaces
 * as well (which is bad coding style, however).<br> 
 * WARNING: A target program may not compile after this transformation if 
 * any method that is declared in java.lang.annotation.Annotation is referenced.
 * Since this transformation is intended for "downgrading" from Java5 to Java 1.4,
 * that wouldn't work anyway. 
 * 
 * @author Tobias Gutzmann
 * @since 0.80
 *
 */
public class RemoveAnnotations extends TwoPassTransformation {
	private NonTerminalProgramElement root;
	private List<CompilationUnit> cul;
	private List<AnnotationUseSpecification> toRemove;
	private List<AnnotationDeclaration> unusedAnnotationTypes;
	private List<AnnotationDeclaration> usedAnnotationTypes;
	private List<Import> imports;

	/**
	 * @param sc
	 */
	public RemoveAnnotations(CrossReferenceServiceConfiguration sc, NonTerminalProgramElement root) {
		super(sc);
		this.root = root;
	}
	
	public RemoveAnnotations(CrossReferenceServiceConfiguration sc, List<CompilationUnit> cul) {
		super(sc);
		this.cul = cul;
	}
	
	private void traverse (TreeWalker tw) {
		while (tw.next()) {
			ProgramElement pe = tw.getProgramElement();
			if (pe instanceof AnnotationUseSpecification) {
				toRemove.add((AnnotationUseSpecification)pe);
			}
			else if (pe instanceof AnnotationDeclaration) {
				AnnotationDeclaration ad = (AnnotationDeclaration)pe;
				List<TypeReference> trl = getServiceConfiguration().getCrossReferenceSourceInfo().getReferences(ad,true);
				boolean remove = true;
				for (int i = 0; i < trl.size(); i++) {
					if (!(trl.get(i).getASTParent() instanceof AnnotationUseSpecification)) {
						remove = false;
						break;
					}
				}
				for (int i = 0; i < ad.getMembers().size(); i++) {
					MemberDeclaration md = ad.getMembers().get(i);
					if (md instanceof TypeDeclaration) {
						// play it safely for now...
						remove = false;
						break;
					}
				}
				if (remove) unusedAnnotationTypes.add(ad);
				else usedAnnotationTypes.add(ad);
			}
			else if (pe instanceof Import) {
				Import imp = (Import)pe;
//				SourceInfo si = getServiceConfiguration().getSourceInfo();
//				if (si.getType(pe) != null) System.out.println("import: " + si.getType(pe).getFullName());
//				System.out.println(imp.toString());
				PackageReference pr = imp.getPackageReference();
				while (pr != null) {
//					System.out.println(pr.getName());
					if (pr.getName().equals("annotation") || pr.getName().equals("reflect")) imports.add(imp);
					int numChild = pr.getChildCount();
					for (int i = 0; i < numChild; i++) {
						if (pr.getChildAt(i) instanceof PackageReference) {
							pr = (PackageReference)pr.getChildAt(i);
							break;
						}
						if (i == numChild - 1) pr = null;
					}
				}
			}
		}
	}
	
	@Override
	public ProblemReport analyze() {
		toRemove = new ArrayList<AnnotationUseSpecification>(100);
		unusedAnnotationTypes = new ArrayList<AnnotationDeclaration>(10);
		usedAnnotationTypes = new ArrayList<AnnotationDeclaration>(10);
		imports = new ArrayList<Import>(10);
		
		TreeWalker tw;
		if (cul != null && cul.size() > 0) {
			for (CompilationUnit cu : cul) {
				tw = new TreeWalker(cu);
				traverse(tw);
			}
		}
		else if (root != null) {
			tw = new TreeWalker(root);
			traverse(tw);
		}

		return super.analyze();
	}
	
	//TODO remove import java.lang.annotation.*;
	//remove comments associated to annotationTypeDeclarations;
	
	@Override
	public void transform() {
		super.transform();
		System.out.println("toRemove: " + toRemove.size() + " unusedAnnotationTypes: " + unusedAnnotationTypes.size() + " usedAnnotationsTypes: " + usedAnnotationTypes.size());
		for (AnnotationUseSpecification au : toRemove) {
			if (!(au.getASTParent() instanceof PackageSpecification) || (au.getASTParent() instanceof PackageSpecification && ((PackageSpecification)au.getASTParent()).getAnnotations() != null) )
				detach(au);
			if (au.getASTParent() instanceof PackageSpecification) {
				PackageSpecification ps = (PackageSpecification)au.getASTParent();
				ps.setAnnotations(null);
			}
		}
		for (AnnotationDeclaration ad : unusedAnnotationTypes) {
			detach(ad);
		}
		for (AnnotationDeclaration ad : usedAnnotationTypes) {
			replace(ad,makeInterface(ad));
		}
	}

	private InterfaceDeclaration makeInterface(AnnotationDeclaration ad) {
		ProgramFactory f = getProgramFactory();
		InterfaceDeclaration replacement = getProgramFactory().createInterfaceDeclaration();
		
		replacement.setExtendedTypes(f.createExtends(
				TypeKit.createTypeReference(f, "java.lang.annotation.Annotation")));
		ASTList<MemberDeclaration> oldMems = ad.getMembers();
		ASTList<MemberDeclaration> newMems = new ASTArrayList<MemberDeclaration>(oldMems.size());
		for (int i = 0; i < oldMems.size(); i++) {
			MemberDeclaration md = oldMems.get(i);
			MemberDeclaration newMD;
			if (md instanceof AnnotationPropertyDeclaration) {
				AnnotationPropertyDeclaration apd = (AnnotationPropertyDeclaration)md;
				MethodDeclaration m = f.createMethodDeclaration();
				if (apd.getComments() != null)
					m.setComments(apd.getComments().deepClone());
				m.setIdentifier(apd.getIdentifier().deepClone());
				m.setTypeReference(apd.getTypeReference().deepClone());
				// everything else is not allowed to be set for annotation property declaration
				newMD = m;
			} else if (md instanceof AnnotationDeclaration) {
				// anntotation types marked for deletion have
				// already been deleted
				newMD = makeInterface((AnnotationDeclaration)md);
			} else {
				newMD = md.deepClone();
			}
			newMD.makeParentRoleValid();
			newMems.add(newMD);
		}
		replacement.setIdentifier(ad.getIdentifier().deepClone());
		replacement.setMembers(newMems);
		if (ad.getComments() != null)
			replacement.setComments(ad.getComments().deepClone());
		replacement.setDeclarationSpecifiers(ad.getDeclarationSpecifiers().deepClone());
		replacement.makeParentRoleValid();
		return replacement;
	}
}
