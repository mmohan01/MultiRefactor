/**
 * 
 */
package recoder.kit.transformation.java5to4.methodRepl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import recoder.CrossReferenceServiceConfiguration;
import recoder.abstraction.ClassType;
import recoder.abstraction.Constructor;
import recoder.abstraction.Field;
import recoder.abstraction.Method;
import recoder.abstraction.Type;
import recoder.bytecode.ByteCodeFormatException;
import recoder.bytecode.ByteCodeParser;
import recoder.java.CompilationUnit;
import recoder.java.Expression;
import recoder.java.NonTerminalProgramElement;
import recoder.java.ProgramElement;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MemberReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.ReferencePrefix;
import recoder.java.reference.TypeReference;
import recoder.java.reference.VariableReference;
import recoder.kit.MiscKit;
import recoder.kit.ProblemReport;
import recoder.kit.TwoPassTransformation;
import recoder.kit.TypeKit;
import recoder.kit.UnitKit;
import recoder.list.generic.ASTArrayList;
import recoder.list.generic.ASTList;

/**
 * @author Tobi
 *
 */
public class ApplyRetrotranslatorLibs extends TwoPassTransformation {
	private final String pathToLibs;
	/**
	 * @param sc
	 */
	public ApplyRetrotranslatorLibs(CrossReferenceServiceConfiguration sc, String 
			pathToLibs) {
		super(sc);
		this.pathToLibs = pathToLibs;
	}
	private Map<String, ClassType> completeReplacements = new HashMap<String, ClassType>();
	private Map<String, ClassType> partialReplacements = new HashMap<String, ClassType>();
//	private Map<String, ClassType> eduClasses = new HashMap<String, ClassType>();
	
	
	@Override
	public ProblemReport analyze() {
		// TODO some duplicate code...
		ZipFile backportZip;
		try {
			backportZip = new ZipFile(pathToLibs + "/backport-util-concurrent-3.1.jar");
		} catch (IOException e) {
			// TODO -> proper error handling (report, and set a proper ProblemReport)
			System.out.println("Cannot find backport-util-concurrent lib! Aborting!");
			System.out.println("Searching In:\n" + 
				new File(pathToLibs + "/backport-util-concurrent-3.1.jar").getAbsolutePath()
			);
			
			return setProblemReport(new ProblemReport() { /* TODO: better... */ });
		}
		Enumeration<? extends ZipEntry> zes = backportZip.entries();
		while (zes.hasMoreElements()) {
			ZipEntry ze = zes.nextElement();
			if (ze.getName().endsWith(".class") && ze.getName().startsWith("edu/emory/mathcs/backport/")) {
				ClassType cf;
				try {
					cf = new ByteCodeParser().parseClassFile(backportZip.getInputStream(ze)); // TODO use new ASMBytecodeParser!
					cf = getNameInfo().getClassType(cf.getFullName());
				} catch (ByteCodeFormatException e) {
					// TODO -> proper error handling (report, and set a proper ProblemReport)
					System.out.println("Problem parsing class file: " + ze.getName());
					return setProblemReport(new ProblemReport() {/* TODO: better */ });
				} catch (IOException e) {
					// TODO -> proper error handling (report, and set a proper ProblemReport)
					System.out.println("IOException while parsing file: " + ze.getName());
					return setProblemReport(new ProblemReport() {/* TODO: better */ });
				}
				
				String cfName = cf.getFullName().substring("edu.emory.mathcs.backport.".length());
				completeReplacements.put(cfName, cf);
//				eduClasses.put(cfName, cf);
			}
		}
		
		ZipFile retroZip;
		try {
			// TODO don't hardcode!
			retroZip = new ZipFile(pathToLibs + "/retrotranslator-runtime-1.2.9.jar");
		} catch (IOException e) {
			// TODO -> proper error handling (report, and set a proper ProblemReport)
			System.out.println("Cannot find retrotranslator runtime! Aborting!");
			return setProblemReport(new ProblemReport() { /* TODO: better */ });
		}
		zes = retroZip.entries();
		while (zes.hasMoreElements()) {
			ZipEntry ze = zes.nextElement();
			try {
				// the following includes javax !
				// TODO include runtime13.v14 (does that need extra work?)
				if (ze.getName().endsWith(".class") && ze.getName().startsWith("net/sf/retrotranslator/runtime/java")) {
					ClassType cf = new ByteCodeParser().parseClassFile(retroZip.getInputStream(ze));  // TODO use new ASMBytecodeParser!
					cf = getNameInfo().getClassType(cf.getFullName());
					String cfName = cf.getFullName().substring("net.sf.retrotranslator.runtime.".length());
					if (cfName.charAt(cfName.length()-1) == '_') {
						completeReplacements.put(cfName.substring(0, cfName.length()-1), cf);
					} else if (cfName.indexOf("_.") > 0) {
						completeReplacements.put(cfName.replace("_.", "."), cf);
					} else {
						assert cfName.indexOf("._") == cfName.lastIndexOf("._") && cfName.indexOf("._") > 0;
						cfName = cfName.replace("._", ".");
						partialReplacements.put(cfName, cf);
					}
				}
			} catch (ByteCodeFormatException e) {
				// TODO -> proper error handling (report, and set a proper ProblemReport)
				System.out.println("Problem parsing class file: " + ze.getName());
				return setProblemReport(new ProblemReport() { /* TODO: better */ });
			} catch (IOException e) {
				// TODO -> proper error handling (report, and set a proper ProblemReport)
				System.out.println("IOException while parsing file: " + ze.getName());
				return setProblemReport(new ProblemReport() { /* TODO: better */ });
			}
		}
		
		// second, "partial" replacements
		for (Map.Entry<String, ClassType> entry : partialReplacements.entrySet()) {
			ClassType ct = entry.getValue();
			List<? extends Field> fields = ct.getFields();
			List<Method> meths = ct.getMethods();
			List<? extends Constructor> constr = ct.getConstructors();
			for (Field f : fields) {
				// "For a static field there is a public static field with the same name and type."
				if (f.isPublic() && f.isStatic()) {
					// get original field!
					Field origF = getNameInfo().getField(entry.getKey() + "." + f.getName());
					if (origF == null) {
						System.out.println("not found: " + entry.getKey() + "." + f.getName());
						continue;
					}
					for (FieldReference r : getCrossReferenceSourceInfo().getReferences(origF)) {
						VariableReference fr = getProgramFactory().createFieldReference(
								TypeKit.createTypeReference(getProgramFactory(), f.getContainingClassType()),
								getProgramFactory().createIdentifier(f.getName())
						);
						replacements.add(new NonTerminalProgramElement[] {
							r, fr
						});
					}
				}
			}
			for (Method m : meths) {
				if (m.isPublic() && m.isStatic()) {
					// "For a static method there is a public static method with the same signature."
					// "For an instance method there is a public static method with the same signature but with an additional first parameter representing the instance."
					// "For a constructor there is a public static convertConstructorArguments method that accepts constructor's arguments and returns an argument for a Java 1.4 constructor"
					if (m.getName().equals("convertConstructorArguments")) {
						// convert constructor argument...
						// TODO not supported yet...
						continue; 
					} else if (m.getFullName().equals(
							"net.sf.retrotranslator.runtime.java.io._PrintStream.createInstanceBuilder")) {
						// TODO not supported yet...
						continue;
					} else if (m.getFullName().equals(
							"net.sf.retrotranslator.runtime.java.lang._SecurityException.createInstanceBuilder")) {
						// TODO not supported yet...
						continue;
					} else if (m.getFullName().equals(
						"net.sf.retrotranslator.runtime.java.io._PrintStream.initialize")) {
						// TODO not supported yet...
						continue;
					} else if (m.getFullName().equals(
						"net.sf.retrotranslator.runtime.java.lang._SecurityException.initialize")) {
						// TODO not supported yet...
						continue;
					} else {
						List<Type> sig = m.getSignature();
						String sigStr = makeSigString(sig, true);
						List<Method> cand = getNameInfo().getMethods(entry.getKey()+"."+m.getName()+"("+sigStr+")");
						if (cand.size() == 0) {
							if (m.isStatic()) {
								// target class not used in input program -> skip
								continue;
							}
							
							// instance method!
							sig.remove(0);
							sigStr = makeSigString(sig, true);
							cand = getNameInfo().getMethods(entry.getKey()+"."+m.getName()+"("+sigStr+")");
							if (cand.size() > 1) {
								// Bridge method/covariant return types!!! This still appears in Sourcecode :-/ 
							}

							if (cand.size() == 0)
								continue; // HOPEFULLY just something unused in target program...
							for (Method toRepl: cand) {
								for (MemberReference r : getCrossReferenceSourceInfo().getReferences(toRepl)) {
									MethodReference mr = (MethodReference)r;
									ReferencePrefix rp = mr.getReferencePrefix();
									if (rp == null) {
										rp = getProgramFactory().createThisReference();
									} else {
										rp = (ReferencePrefix)rp.deepClone();
									}
									ASTList<Expression> newArgs = ((MethodReference)r).getArguments();
									if (newArgs == null) 
										newArgs = new ASTArrayList<Expression>(1);
									else
										newArgs = newArgs.deepClone();
									newArgs.add(0, (Expression)rp);
									MethodReference newRef = getProgramFactory().createMethodReference(
											TypeKit.createTypeReference(getProgramFactory(), ct.getFullName()),
											getProgramFactory().createIdentifier(m.getName()), 
											newArgs
									);
									replacements.add(new NonTerminalProgramElement[] {
											r,
											newRef
									});
								}
							}
						} else {
							// static method!
							if (cand.size() != 1)
								throw new RuntimeException(); // HÄH?
							Method toRepl = cand.get(0);
							for (MemberReference r : getCrossReferenceSourceInfo().getReferences(toRepl)) {
								MethodReference newRef = getProgramFactory().createMethodReference(
											TypeKit.createTypeReference(getProgramFactory(), ct.getFullName()),
											getProgramFactory().createIdentifier(m.getName()), 
											((MethodReference)r).getArguments().deepClone());
								replacements.add(new NonTerminalProgramElement[] {
									r,
									newRef
								});
							}
						}
					}
				}
			}
		}
		
		// third, EDU classes
//		File retroPropFile = new File("src/recoder/kit/transformation/java5to4/methodRepl/backport14.properties");
//		// TODO we assume it's there for now...
//		try {
//			BufferedReader inp = new BufferedReader(new FileReader(retroPropFile));
//			String s = inp.readLine(); // skip first line...
//			while ((s = inp.readLine()) != null) {
//				int idx = s.indexOf(':');
//				String orig = s.substring(0, idx);
//				String repl = s.substring(idx+1, s.length());
//				List<? extends ModelElement> mes = getModelElement(orig);
//				for (ModelElement me: mes) {
//					if (me instanceof ClassType) {
//						completeReplacements.put(orig, getNameInfo().getClassType(repl));
//					} else if (me instanceof Package) {
//						List<ClassType> cts = getNameInfo().getClassTypes(orig+".**");
//						for (ClassType ct : cts) {
//							completeReplacements.put(ct.getFullName(),
//									getNameInfo().getClassType(ct.getFullName().replace(orig, repl)));
//						}
//					} else if (me instanceof Method) {
//						List<MemberReference> mrs = 
//							getCrossReferenceSourceInfo().getReferences((Method)me);
//						for (MemberReference mbr : mrs) {
//							MethodReference mr = (MethodReference)mbr;
//							MethodReference mrrepl = mr.deepClone();
//							// we always qualify; shouldn't be necessary, output could be nicer without!
//							// TODO ^^^
//							mrrepl.setReferencePrefix(TypeKit.createTypeReference(
//									getProgramFactory(), repl.substring(0, repl.lastIndexOf('.'))));
//							replacements.add(new NonTerminalProgramElement[] {
//									mr,
//									mrrepl
//							});
//						}
//					} else {
//						// HOPEFULLY a package that we don't use
//						System.out.println("Not found: " + orig);
//					}
//				}
//			} 
//		} catch (IOException e) {
//			// TODO proper problem report...
//			return setProblemReport(new Problem() {
//			});
//		}
		
		// DONE COLLECTING...
		System.out.println("Found " + completeReplacements.size() + " classes for complete replacement");
		System.out.println("Found " + partialReplacements.size() + " classes for partial replacement");
		
		// make "complete" class type replacements ready:
		for (Map.Entry<String, ClassType> me : completeReplacements.entrySet()) {
			String name = me.getKey();
			int idx = name.lastIndexOf(".");
			if (idx != -1) {
				if (!Character.isJavaIdentifierStart(name.charAt(idx+1)))
					continue; // local class. Cannot be referenced through Recoder.
			}
			ClassType oldType = getNameInfo().getClassType(name);
			if (oldType == null) {
				//System.out.println("Type not found: "+ me.getKey());
				// not found -> cannot be used by program to be transformed ;-)
				continue;
			}
			// TODO should always be present!? (provided a proper JDK is provided...)
			for(TypeReference tr : getCrossReferenceSourceInfo().getReferences(oldType, true)) {
				if (oldType.getFullName().equals("java.util.Collections") && tr.getReferenceSuffix() instanceof FieldReference)
					continue; // don't...
				replacements.add(new TypeReference[] {
					tr,
					TypeKit.createTypeReference(getProgramFactory(), me.getValue(), tr.getDimensions())
					}
				);
			}
		}
		// the "partial" replacements are next...
		
		sortRepls(replacements);
		return setProblemReport(EQUIVALENCE);
	}
	
		
		private String makeSigString(List<Type> signature, boolean fullName) {
			StringBuilder result = new StringBuilder();

			boolean first = true;
			for (Type t : signature) {
				if (!first)
					result.append(',');
				else
					first = false;
				if (fullName)
					result.append(t.getFullName());
				else
					result.append(t.getName());
			}
			return result.toString();
		}
		
	private final List<NonTerminalProgramElement[]> replacements = new ArrayList<NonTerminalProgramElement[]>();
	
	private void sortReplsLoc(List<NonTerminalProgramElement[]> casts) {
		boolean changed = true;
		NonTerminalProgramElement ex1, ex2;
		NonTerminalProgramElement[] tmp;
		NonTerminalProgramElement ntpe = null;
		while(changed) {
			changed = false;
			for (int i = 0; i < casts.size() - 1; i++) {
				for (int j = i + 1; j < casts.size(); j++) {
					ex1 = casts.get(i)[0];
					ex2 = casts.get(j)[0];
					
					ntpe = ex1;
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
	
	private boolean isChild(NonTerminalProgramElement ex1, NonTerminalProgramElement ex2) {
		if (ex1 == ex2) return false;
		return MiscKit.contains(ex1, ex2);
	}
	
	private void sortRepls(List<NonTerminalProgramElement[]> casts) {
		HashMap<ProgramElement, ArrayList<NonTerminalProgramElement[]>> temp = new HashMap<ProgramElement, ArrayList<NonTerminalProgramElement[]>>(casts.size());
		for (NonTerminalProgramElement[] cast : casts) {
			CompilationUnit decl = UnitKit.getCompilationUnit(cast[0]);
			ArrayList<NonTerminalProgramElement[]> al = temp.get(decl);
			if (al == null) {
				al = new ArrayList<NonTerminalProgramElement[]>(4);
				temp.put(decl, al);
			}
			al.add(cast);
		}
		casts.clear();
		for (ArrayList<NonTerminalProgramElement[]> al : temp.values()) {
			sortReplsLoc(al);
			casts.addAll(al);
		}
	}
	
	@Override
	public void transform() {
		super.transform();
		for (NonTerminalProgramElement[] repl: replacements) {
			replace(repl[0], repl[1]);
		}
	}
}
