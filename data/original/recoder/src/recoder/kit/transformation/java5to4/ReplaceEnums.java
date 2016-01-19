/**
 * 
 */
package recoder.kit.transformation.java5to4;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import net.sf.retrotranslator.transformer.Retrotranslator;
import recoder.CrossReferenceServiceConfiguration;
import recoder.ProgramFactory;
import recoder.abstraction.ArrayType;
import recoder.abstraction.ClassType;
import recoder.abstraction.EnumConstant;
import recoder.abstraction.Field;
import recoder.abstraction.Type;
import recoder.convenience.TreeWalker;
import recoder.io.PropertyNames;
import recoder.java.CompilationUnit;
import recoder.java.Expression;
import recoder.java.LoopInitializer;
import recoder.java.NonTerminalProgramElement;
import recoder.java.ProgramElement;
import recoder.java.Statement;
import recoder.java.StatementBlock;
import recoder.java.declaration.AnnotationUseSpecification;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.ClassInitializer;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.DeclarationSpecifier;
import recoder.java.declaration.EnumConstantDeclaration;
import recoder.java.declaration.EnumConstantSpecification;
import recoder.java.declaration.EnumDeclaration;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.FieldSpecification;
import recoder.java.declaration.LocalVariableDeclaration;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.ParameterDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.modifier.Abstract;
import recoder.java.declaration.modifier.Static;
import recoder.java.expression.ParenthesizedExpression;
import recoder.java.expression.operator.New;
import recoder.java.expression.operator.NewArray;
import recoder.java.reference.ArrayReference;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.ReferencePrefix;
import recoder.java.reference.SpecialConstructorReference;
import recoder.java.reference.TypeReference;
import recoder.java.statement.Case;
import recoder.java.statement.For;
import recoder.java.statement.If;
import recoder.java.statement.Switch;
import recoder.kit.MiscKit;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.kit.TypeKit;
import recoder.list.generic.ASTArrayList;
import recoder.list.generic.ASTList;
import recoder.service.DefaultNameInfo;
import recoder.service.NameInfo;
import recoder.service.SourceInfo;
import recoder.util.FileUtils;

/**
 * @author Tobias Gutzmann
 *
 */
public class ReplaceEnums extends TwoPassTransformation {
	private static final String ENUM_REPLACEMENT_TYPE = "net.sf.retrotranslator.runtime.java.lang.Enum_";
	private static final String ENUMSET_REPLACEMENT_TYPE = "net.sf.retrotranslator.runtime.java.util.EnumSet_";
	private static final String ENUMMAP_REPLACEMENT_TYPE = "net.sf.retrotranslator.runtime.java.util.EnumMap_";
	private static final String ORDINAL_VALUE_CONSTANT_FIELD_PREFIX = "";
	private static final String ORDINAL_VALUE_CONSTANT_FIELD_SUFFIX = "__ORD_VALUE__";
	
	/**
	 * @param sc
	 */
	public ReplaceEnums(CrossReferenceServiceConfiguration sc) {
		super(sc);
	}
	static class TransObj {
		final ProgramElement pe; 
		final ProgramElement repl;
		TransObj(ProgramElement pe, ProgramElement repl) {
			this.pe = pe;
			this.repl = repl;
		}
	}
	
	private List<TransObj> trans;
	private List<EnumDeclaration> enums;
	private List<CompilationUnit> detach;
	
	
	@Override
	public ProblemReport analyze() {
		trans = new ArrayList<TransObj>();
		enums = new ArrayList<EnumDeclaration>();
		detach = new ArrayList<CompilationUnit>(5);
		SourceInfo si = getSourceInfo();
		NameInfo ni = getNameInfo();
		ProgramFactory pf = getProgramFactory();
		
		TypeReference enumRefProto = TypeKit.createTypeReference(pf, ENUM_REPLACEMENT_TYPE);
		TypeReference enumSetProto = TypeKit.createTypeReference(pf, ENUMSET_REPLACEMENT_TYPE);
		TypeReference enumMapProto = TypeKit.createTypeReference(pf, ENUMMAP_REPLACEMENT_TYPE);
		
		Type t;
		if ((t = ni.getType("java.lang.Enum")) instanceof TypeDeclaration) {
			detach.add((CompilationUnit)((TypeDeclaration)t).getASTParent()); // the CU
		}
		if ((t = ni.getType("java.util.RegularEnumSet")) instanceof TypeDeclaration) {
			detach.add((CompilationUnit)((TypeDeclaration)t).getASTParent()); // the CU
		}
		if ((t = ni.getType("java.util.EnumMap")) instanceof TypeDeclaration) {
			detach.add((CompilationUnit)((TypeDeclaration)t).getASTParent()); // the CU
		}
		if ((t = ni.getType("java.util.EnumSet")) instanceof TypeDeclaration) {
			detach.add((CompilationUnit)((TypeDeclaration)t).getASTParent()); // the CU
		}
		if ((t = ni.getType("java.util.JumboEnumSet")) instanceof TypeDeclaration) {
			detach.add((CompilationUnit)((TypeDeclaration)t).getASTParent()); // the CU
		}

		for (CompilationUnit cu : getSourceFileRepository().getCompilationUnits()) {
			TreeWalker tw = new TreeWalker(cu);
			while (tw.next()) {
				ProgramElement pe = tw.getProgramElement();
				if (pe instanceof CompilationUnit && detach.contains(pe)) {
					continue; // will be removed!
				}
				if (pe instanceof EnumDeclaration) {
					enums.add((EnumDeclaration)pe);
				} else if (pe instanceof Expression && pe.getASTParent() instanceof Switch) {
					Type type = si.getType((Expression)pe);
					if (type instanceof ClassType && ((ClassType)type).isEnumType()) {
						// replace with .ordinal()
						trans.add(new TransObj(pe, 
								pf.createMethodReference((ReferencePrefix)pe.deepClone(), 
										pf.createIdentifier("ordinal")	
								)));
					}
				} else if (pe instanceof FieldReference) {
					FieldReference fr = (FieldReference)pe;
					if (fr.getReferencePrefix() != null)
						continue;  // not interested, then!
					Field f = (Field)si.getVariable((FieldReference)pe);
					if (!(f instanceof EnumConstant)) 
						continue; // not interested, then!
					NonTerminalProgramElement parent = pe.getASTParent();
					if (parent instanceof Case) {
						trans.add(new TransObj(pe,
							pf.createFieldReference(
									TypeKit.createTypeReference(si, f.getContainingClassType(), MiscKit.getParentTypeDeclaration(parent)),
									pf.createIdentifier(ORDINAL_VALUE_CONSTANT_FIELD_PREFIX + f.getName() + ORDINAL_VALUE_CONSTANT_FIELD_SUFFIX))));
					}
				} else if (pe instanceof TypeReference) {
					TypeReference tr = (TypeReference)pe;
					t = si.getType(tr);
					while (t instanceof ArrayType) {
						t = ((ArrayType)t).getBaseType();
					}
					TypeReference repl;
					if (t == ni.getJavaLangEnum()) {
						repl = enumRefProto.deepClone();
						repl.setDimensions(tr.getDimensions());
					} else if (t == ni.getClassType("java.util.EnumMap")) {
						repl = enumMapProto.deepClone();
						repl.setDimensions(tr.getDimensions());
					}  else if (t == ni.getClassType("java.util.EnumSet")) {
						repl = enumSetProto.deepClone();
						repl.setDimensions(tr.getDimensions());
					} else continue;
					trans.add(new TransObj(pe, repl));
				}
			}
		}
		if (detach.isEmpty() && enums.isEmpty() && trans.isEmpty())
			return setProblemReport(IDENTITY);
		return setProblemReport(NO_PROBLEM);
	}
	
	private ClassDeclaration makeReplacement(EnumDeclaration ed) {
		ProgramFactory f = getProgramFactory();
		ASTList<MemberDeclaration> members = new ASTArrayList<MemberDeclaration>();
		ClassDeclaration res = f.createClassDeclaration(
				ed.getDeclarationSpecifiers() == null ? null : ed.getDeclarationSpecifiers().deepClone(), 
				ed.getIdentifier().deepClone(), 
				f.createExtends(
						TypeKit.createTypeReference(f, ENUM_REPLACEMENT_TYPE)),
						ed.getImplementedTypes() == null ? null : ed.getImplementedTypes().deepClone(),
								members);
		boolean hasStatic = false;
		if (res.getDeclarationSpecifiers() != null) {
			for (DeclarationSpecifier ds : res.getDeclarationSpecifiers()) {
				if (ds instanceof Static) {
					hasStatic = true;
					break;
				}
			}
		}
		if (!hasStatic && ed.getParent() instanceof TypeDeclaration) {
			Static stat = new Static();
			if (res.getDeclarationSpecifiers() == null)
				res.setDeclarationSpecifiers(new ASTArrayList<DeclarationSpecifier>(1));
			res.getDeclarationSpecifiers().add(stat);
			stat.setParent(res);
		}
		if (ed.getComments() != null)
			res.setComments(ed.getComments().deepClone());
		// all done but the members!
		boolean hasConstructor = false;
		List<FieldSpecification> enumConstantRepls = new ArrayList<FieldSpecification>();
		int ord = 0;
		boolean needsAbstract = false; 
		for (MemberDeclaration md: ed.getMembers()) {
			if (md instanceof ClassInitializer) {
				members.add(md.deepClone());
			} else if (md instanceof EnumConstantDeclaration) {
				EnumConstantDeclaration ec = (EnumConstantDeclaration)md;
				EnumConstantSpecification ecs = ec.getEnumConstantSpecification();

				// create replacement for current constant
				ASTArrayList<DeclarationSpecifier> dsml = new ASTArrayList<DeclarationSpecifier>();
				if (ec.getAnnotations() != null) {
					for (AnnotationUseSpecification a : ec.getAnnotations()) {
						dsml.add(a.deepClone());
					}
				}
				dsml.add(f.createFinal());
				dsml.add(f.createPublic());
				dsml.add(f.createStatic());
				FieldDeclaration fd = f.createFieldDeclaration(
						dsml,
						f.createTypeReference(ed.getIdentifier().deepClone()),
						ecs.getIdentifier().deepClone(),
						null
					);
				FieldSpecification fs = fd.getFieldSpecifications().get(0);
				enumConstantRepls.add(fs);
				
				dsml = new ASTArrayList<DeclarationSpecifier>();
				dsml.add(f.createPublic());
				dsml.add(f.createStatic());
				dsml.add(f.createFinal());
				FieldDeclaration constDecl = f.createFieldDeclaration(
						dsml, 
						f.createTypeReference(f.createIdentifier("int")), 
						f.createIdentifier(ORDINAL_VALUE_CONSTANT_FIELD_PREFIX + ecs.getName() + ORDINAL_VALUE_CONSTANT_FIELD_SUFFIX), 
						f.createIntLiteral(ord));
				members.add(constDecl);
				
				New e = f.createNew();
				e.setTypeReference(f.createTypeReference(ed.getIdentifier().deepClone()));
				ASTArrayList<Expression> args = null;
				if (ecs.getConstructorReference().getArguments() != null || ecs.getConstructorReference().getClassDeclaration() != null) {
					List<Expression> ecsargs = ecs.getConstructorReference().getArguments();
					int s = ecsargs == null ? 0 : ecsargs.size();
					args = new ASTArrayList<Expression>(s+2);
					for (int j = 0; j < s; j++)
						args.add(ecsargs.get(j).deepClone());
					if (ecs.getConstructorReference().getClassDeclaration() != null) {
						ClassDeclaration cd = ecs.getConstructorReference().getClassDeclaration().deepClone();
						e.setClassDeclaration(cd);
					}
				} else {
					args = new ASTArrayList<Expression>(2);
				}
				args.add(f.createStringLiteral("\"" + ecs.getIdentifier().getText() + "\""));
				args.add(f.createIntLiteral(ord));
				e.setArguments(args);
				fs.setInitializer(e);
				e.makeParentRoleValid();
				fs.makeParentRoleValid();
				fd.makeParentRoleValid();
				members.add(fd);
				ord++;
			} else if (md instanceof FieldDeclaration) {
				members.add(md.deepClone());
			} else if (md instanceof ConstructorDeclaration) {
				hasConstructor = true;
				ConstructorDeclaration cd = (ConstructorDeclaration)md.deepClone();
				cd.getParameters().add(f.createParameterDeclaration(f.createTypeReference(f.createIdentifier("String")), f.createIdentifier("__name")));
				cd.getParameters().add(f.createParameterDeclaration(f.createTypeReference(f.createIdentifier("int")), f.createIdentifier("__ord")));
				
				ASTList<Expression> superRefArgs;
				if (cd.getBody().getBody().size() > 0 
						&& cd.getBody().getBody().get(0) instanceof SpecialConstructorReference) {
					SpecialConstructorReference scr = (SpecialConstructorReference)cd.getBody().getBody().get(0);
					superRefArgs = scr.getArguments();
					superRefArgs.add(f.createVariableReference(f.createIdentifier("__name")));
					superRefArgs.add(f.createVariableReference(f.createIdentifier("__ord")));
				} else {
					superRefArgs = new ASTArrayList<Expression>(2);
					superRefArgs.add(f.createVariableReference(f.createIdentifier("__name")));
					superRefArgs.add(f.createVariableReference(f.createIdentifier("__ord")));
					cd.getBody().getBody().add(0, 
							f.createSuperConstructorReference(superRefArgs)	
					);
				}
				cd.makeAllParentRolesValid();
				members.add(cd);
			} else if (md instanceof MethodDeclaration) {
				members.add(md.deepClone());
				if (((MethodDeclaration) md).isAbstract())
					needsAbstract = true;
			} else if (md instanceof EnumDeclaration) {
				throw new RuntimeException("Inner enums should be resolved first, this code piece should not be reachable");
			} else if (md instanceof TypeDeclaration) {
				members.add(md.deepClone());
			}
		}
		if (needsAbstract) {
			Abstract abstr = f.createAbstract();
			if (res.getDeclarationSpecifiers() == null)
				res.setDeclarationSpecifiers(new ASTArrayList<DeclarationSpecifier>(1));
			res.getDeclarationSpecifiers().add(abstr);
			abstr.setParent(res);
		}
		if (!hasConstructor) {
			// add default constructor.
			ConstructorDeclaration newConstructor;
			ASTList<Statement> block = new ASTArrayList<Statement>(1);
			ASTList<Expression> scrArgs = new ASTArrayList<Expression>(2);
			scrArgs.add(f.createVariableReference(f.createIdentifier("name")));
			scrArgs.add(f.createVariableReference(f.createIdentifier("ord")));
			SpecialConstructorReference scr = f.createSuperConstructorReference(scrArgs);
			block.add(scr);
			StatementBlock body = f.createStatementBlock(block);
			ASTList<ParameterDeclaration> constrParams = new ASTArrayList<ParameterDeclaration>(2);
			constrParams.add(f.createParameterDeclaration(f.createTypeReference(f.createIdentifier("String")),
														  f.createIdentifier("name")));
			constrParams.add(f.createParameterDeclaration(f.createTypeReference(f.createIdentifier("int")),
					  f.createIdentifier("ord")));
			newConstructor = f.createConstructorDeclaration(
					f.createPrivate(),
					ed.getIdentifier().deepClone(),
					constrParams, 
					null,
					body
			);
			members.add(newConstructor);
		}
		MethodDeclaration values = f.createMethodDeclaration();
		MethodDeclaration valueOf = f.createMethodDeclaration();
		MethodDeclaration compareTo = f.createMethodDeclaration();
		values.setIdentifier(f.createIdentifier("values"));
		valueOf.setIdentifier(f.createIdentifier("valueOf"));
		compareTo.setIdentifier(f.createIdentifier("compareTo"));
		ASTList<DeclarationSpecifier> declSpecs = new ASTArrayList<DeclarationSpecifier>();
		declSpecs.add(f.createPublic());
		compareTo.setDeclarationSpecifiers(declSpecs.deepClone());
		declSpecs.add(f.createStatic());
		values.setDeclarationSpecifiers(declSpecs);
		declSpecs = declSpecs.deepClone();
		valueOf.setDeclarationSpecifiers(declSpecs);

		values.setTypeReference(f.createTypeReference(ed.getIdentifier().deepClone(), 1));
		valueOf.setTypeReference(f.createTypeReference(ed.getIdentifier().deepClone()));
		compareTo.setTypeReference(f.createTypeReference(f.createIdentifier("int")));

		valueOf.setParameters(
				new ASTArrayList<ParameterDeclaration>(
						f.createParameterDeclaration(TypeKit.createTypeReference(f, "String"),
						f.createIdentifier("name")		
						)
				)
		);
		compareTo.setParameters(
				new ASTArrayList<ParameterDeclaration>(
						f.createParameterDeclaration(f.createTypeReference(f.createIdentifier("Object")),
						f.createIdentifier("o")
						)
				)
		);

		StatementBlock valuesSt = f.createStatementBlock();
		StatementBlock valueOfSt = f.createStatementBlock();
		StatementBlock compareToSt = f.createStatementBlock();
		values.setBody(valuesSt);
		valueOf.setBody(valueOfSt);
		compareTo.setBody(compareToSt);

		ParenthesizedExpression pExpr = f.createParenthesizedExpression(
				f.createTypeCast(f.createVariableReference(f.createIdentifier("o")), f.createTypeReference(ed.getIdentifier().deepClone())));
		FieldReference fRef = f.createFieldReference(f.createIdentifier("ordinal"));
		pExpr.setReferenceSuffix(fRef);
		fRef.setReferencePrefix(pExpr);
		pExpr.makeParentRoleValid();
		fRef.makeParentRoleValid();
		compareToSt.setBody(new ASTArrayList<Statement>(
				f.createReturn(
						f.createMethodReference(
								f.createSuperReference(),
								f.createIdentifier("compareTo"),
								new ASTArrayList<Expression>(
									f.createTypeCast(
											f.createVariableReference(f.createIdentifier("o")),
											TypeKit.createTypeReference(f, ENUM_REPLACEMENT_TYPE)
									)
								)
							)
						)
				)
		);
		
		NewArray na = f.createNewArray();
		na.setTypeReference(f.createTypeReference(ed.getIdentifier().deepClone(),1));
		na.setArrayInitializer(f.createArrayInitializer(new ASTArrayList<Expression>(enumConstantRepls.size())));
		na.makeParentRoleValid();
		valuesSt.setBody(new ASTArrayList<Statement>(f.createReturn(na)));
		// TODO doesn't work if no enum constants are declared (makes no sense, but yet...)
		ASTList<Statement> stmtList = new ASTArrayList<Statement>();
		valueOfSt.setBody(stmtList);
		If ite = f.createIf();
		LocalVariableDeclaration lvd = f.createLocalVariableDeclaration(null, TypeKit.createTypeReference(f, "int"), f.createIdentifier("i"), f.createIntLiteral(0));
		FieldReference fieldRef = f.createFieldReference(f.createIdentifier("length"));
		MethodReference methRef = f.createMethodReference(f.createIdentifier("values"));
		fieldRef.setReferencePrefix(methRef.deepClone());
		fieldRef.makeAllParentRolesValid();
		For fst = f.createFor(new ASTArrayList<LoopInitializer>(lvd),
				f.createLessThan(f.createVariableReference(f.createIdentifier("i")), fieldRef.deepClone()), 
				new ASTArrayList<Expression>(f.createPostIncrement(f.createVariableReference(f.createIdentifier("i")))),
				ite);
		methRef = f.createMethodReference(f.createIdentifier("values"));
		ArrayReference arrRef = f.createArrayReference(methRef, new ASTArrayList<Expression>(f.createVariableReference(f.createIdentifier("i"))));
		arrRef.setReferencePrefix(methRef.deepClone());
		arrRef.makeParentRoleValid();
		ArrayReference arrRef2 = arrRef.deepClone();
		methRef = f.createMethodReference(f.createIdentifier("name"));
		methRef.setReferencePrefix(arrRef.deepClone());
		methRef.makeParentRoleValid();
		MethodReference methRef2 = f.createMethodReference(f.createIdentifier("equals"), new ASTArrayList<Expression>(f.createVariableReference(f.createIdentifier("name"))));
		methRef2.setReferencePrefix(methRef.deepClone());
		methRef2.makeParentRoleValid();
		ite.setExpression(methRef2.deepClone());
		ite.setThen(f.createThen(f.createReturn(arrRef2)));
		ite.makeParentRoleValid();
		stmtList.add(fst);
		stmtList.add(f.createThrow(f.createNew(null,f.createTypeReference(f.createIdentifier("IllegalArgumentException")),null)));
		fst.makeParentRoleValid();
		for (int i = 0; i < enumConstantRepls.size(); i++) {
			FieldSpecification fs = enumConstantRepls.get(i);
			na.getArrayInitializer().getArguments().add(f.createFieldReference(fs.getIdentifier().deepClone()));
		}
		na.getArrayInitializer().makeParentRoleValid();
		valuesSt.makeParentRoleValid();
		valueOfSt.makeParentRoleValid();
		compareToSt.makeParentRoleValid();

		compareTo.makeParentRoleValid();
		values.makeParentRoleValid();
		valueOf.makeParentRoleValid();
		
		members.add(values);
		members.add(valueOf);
		members.add(compareTo);
		res.makeParentRoleValid();
		return res;
	}
	
	@Override
	public void transform() {
		super.transform();
		for (CompilationUnit det: detach) {
			detach(det);
		}
		for (int i = trans.size()-1; i >= 0; i--) {
			TransObj to = trans.get(i);
			replace(to.pe, to.repl);
		}
		for (int i = enums.size()-1; i >= 0; i--) {
			EnumDeclaration ed = enums.get(i);
			replace(ed, makeReplacement(ed));
		}
		
		
		runRetrotranslator();
	}
	
	private void runRetrotranslator() {
		System.out.println("Running Retrotranslator");
		String inputPath = getServiceConfiguration().getProjectSettings().getProperty(
				PropertyNames.INPUT_PATH);
		String outputPath = getServiceConfiguration().getProjectSettings().getProperty(
				PropertyNames.OUTPUT_PATH);
		StringTokenizer st = new StringTokenizer(inputPath, File.pathSeparator);
		StringBuffer newInputPath = new StringBuffer(inputPath.length());
		File destDir = new File(outputPath + File.separator + "_transformed_bytecode_");
		if (!destDir.exists())
			destDir.mkdirs();
		while (st.hasMoreTokens()) {
			String s = st.nextToken();
			if (s.replace("/", "\\").startsWith(FileUtils.getPathOfExtensionClassesDir().getParentFile().getAbsolutePath().replace("/", "\\"))
					|| s.replace("/","\\").startsWith(FileUtils.getPathOfSystemClasses().getParentFile().getAbsolutePath().replace("/", "\\"))) {
				newInputPath.append(s);
				newInputPath.append(File.pathSeparator);
				continue;
			}
			boolean jar = s.endsWith(".jar");
			String dest = destDir.getAbsolutePath() + (jar ? s.substring(s.lastIndexOf(File.separator), s.length()) : "");
			
			new Retrotranslator().execute(new String[] {
					jar ? "-srcjar" : "-srcdir", s,
					jar ? "-destjar" : "-destdir", dest,
					"-stripsign"
			});
			newInputPath.append(dest);
			newInputPath.append(File.pathSeparator);
		}
		
		getServiceConfiguration().getProjectSettings().setProperty(PropertyNames.INPUT_PATH, 
				newInputPath.toString());
		System.out.println("New input_path: " + getServiceConfiguration().getProjectSettings().getProperty(
				PropertyNames.INPUT_PATH));
		System.out.println("---");

		// reset cached class files.
		((DefaultNameInfo)getNameInfo()).resetBytecode();
	}
}

