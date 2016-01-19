/**
 * created on May 12, 2010 
 */
package recoder.bytecode;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Stack;

import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.signature.SignatureReader;
import org.objectweb.asm.signature.SignatureVisitor;

import recoder.abstraction.TypeArgument;
import recoder.abstraction.TypeArgument.WildcardMode;

/**
 * @author Tobias Gutzmann
 *
 */
public class ASMBytecodeParser extends AbstractBytecodeParser {
	private static final Integer ZERO = 0;
	private boolean readJava5Signatures = true;

	private static String makeReplString(String s) {
		if (s == null) return null;
		return s.replace('$', '.').replace('/', '.');
	}
	private static String[] makeReplString(String[] s) {
		if (s == null) return null;
		String[] res = new String[s.length];
		for (int i = 0; i < s.length; i++)
			res[i] = makeReplString(s[i]);
		return res;
	}
	
	private static String[] getAsStringArr(Type[] s) {
		String[] res = new String[s.length];
		for (int i = 0; i < res.length; i++)
			res[i] = s[i].getClassName().replace('$', '.');
		return res;
	}

	private static String makeAnnotationString(String s) {
		return makeReplString(s.substring(1,s.length()-1));
	}
	
	private class ClassV extends ClassVisitor {
		
		public ClassV() {
			super(Opcodes.ASM4);
		}

		@SuppressWarnings("unchecked")
		public void visit(int version, int access, String name,
				String signature, String superName, String[] interfaces) {
			if (!readJava5Signatures)
				signature = null;
			result.version = version;
			result.setAccessFlags(access);
			String physName = name.replace('/', '.');
			result.setPhysicalName(physName);
			String fullName = physName.replace('$', '.');
			result.setFullName(fullName);
			result.setName(fullName.substring(fullName.lastIndexOf('.')+1));
			
			if (signature == null) {
				if (superName != null)
					result.setSuperName(makeReplString(superName));
				else assert name.equals("java/lang/Object");
				result.setInterfaceNames(makeReplString(interfaces));
			} else {
				MySignatureVisitor mv = new MySignatureVisitor(false);
				new SignatureReader(signature).accept(mv);
				result.setSuperName(mv.superClassName);
				mv.interfaceNames.trimToSize();
				result.setInterfaceNames(mv.interfaceNames.toArray(new String[mv.interfaceNames.size()]));
				mv.superClassTAIs.trimToSize();
				result.superClassTypeArguments = mv.superClassTAIs;
				for (ArrayList<TypeArgumentInfo> al: mv.interfaceTAIs)
					al.trimToSize();
				result.superInterfacesTypeArguments = mv.interfaceTAIs.toArray(new List[mv.interfaceTAIs.size()]);

				int tpCnt = mv.typeParamNames.size();
				ArrayList<TypeParameterInfo> tps = new ArrayList<TypeParameterInfo>(tpCnt); 
				result.setTypeParameters(tps);
				for (int i = 0; i <tpCnt; i++) {
					tps.add(new TypeParameterInfo(
						mv.typeParamNames.get(i), mv.typeParamBounds.get(i).toArray(new String[0]),
						mv.typeParamTAIs.get(i).toArray(new List[0]), result
					));
				}
			}
		}

		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			if (!readJava5Signatures)
				return null;
			AnnotationUseInfo currentAnnotation = new AnnotationUseInfo(
					makeAnnotationString(desc),
					new ArrayList<ElementValuePairInfo>(0));
			classAnnotations.add(currentAnnotation);
			return new MyAnnotationVisitor(currentAnnotation);
		}

		public void visitAttribute(Attribute attr) {
			// non-standard attribute. Currently ignored.
		}

		public void visitEnd() { /* nothing to do */ }

		public FieldVisitor visitField(int access, String name, String desc,
				String signature, Object value) {
			desc = makeReplString(desc);
			if (!readJava5Signatures)
				signature = null;
			final String type;
			final boolean isTypeParam;
			final List<TypeArgumentInfo> tas; 
			if (signature != null) {
				int p = 0; 
				while (signature.charAt(p) == '[') p++;
				isTypeParam = signature.charAt(p) == 'T';
				// parse signature:
				
				MySignatureVisitor mv = new MySignatureVisitor(true);
				new SignatureReader(signature).acceptType(mv);
				type = mv.mainType;
				tas = mv.fieldTAIs;
				
				if (tas != null)
					((ArrayList<TypeArgumentInfo>)tas).trimToSize();
			} else {
				isTypeParam = false;
				type = Type.getType(desc).getClassName();
				tas = Collections.<TypeArgumentInfo>emptyList();
			}

			final FieldInfo f; 
			if ((access & AccessFlags.ENUM) > 0)
				f = new EnumConstantInfo(access, name, type, result, value==null ? null : value.toString(), tas);
			else 
				f = new FieldInfo(access, name, type, isTypeParam, result, value==null ? null : value.toString(), tas);
			f.typeArgs = tas;
			fields.add(f);
			
			if (readJava5Signatures) {
				fieldAnnotations = new ArrayList<AnnotationUseInfo>(0);
				return new MyFieldVisitor();
			} else {
				return null;
			}
		}

		public void visitInnerClass(String name, String outerName,
				String innerName, int access) {
			if (innerName == null) {
				// anonymous class, accessible only via opcodes. Recoder does not look at opcodes,
				// so it will never(?) be loaded and we don't need to perform any additional task here. 
				return; 
			} else {
				int idx = name.lastIndexOf('$');
				if (idx != -1) {
					 // some (possibly older javac???) compilers make strange use of INNERCLASS attributes 
					if (outerName != null && outerName.equals(name.substring(0, name.lastIndexOf('$')))) {

						String nameRepl = makeReplString(name);
						innerClassesNames.add(nameRepl);
						if ((access & Opcodes.ACC_STATIC) == 0 && nameRepl.equals(result.getFullName()))
							result.isInner = true;
						// TODO: inner class detection is probably possible easier...
					}
				}
			} 
		}

		@SuppressWarnings("unchecked")
		public MethodVisitor visitMethod(int access, String name, String desc,
				String signature, String[] exceptions) {
			// TODO - currently, class initializers are skipped by recoder.
			// This should be changed in the future, I guess!?
			
			if (name.equals("<clinit>"))
				return null;

			if (!readJava5Signatures)
				signature = null;
			boolean isConstructor = name.equals("<init>");
			
			String[] paramTypes;
			String resType;
			boolean resultIsTypeVar;

			MySignatureVisitor sv = null;
			
			if (signature != null) {
				// decode signature, set type arguments...
				sv = new MySignatureVisitor(false);
				new SignatureReader(signature).accept(sv);
				resType = sv.mainType;
				int checkAt = signature.indexOf(')')+1;
				while (signature.charAt(checkAt) == '[')
					checkAt++;
				resultIsTypeVar = signature.charAt(checkAt) == 'T';
				paramTypes = sv.paramTypes.toArray(new String[0]);
				
				Type[] descParams = Type.getArgumentTypes(desc);
				if (paramTypes.length != descParams.length) {
					// fixing bug 3293766 (and other issues)
					// funny thing: implicit arguments (e.g., enclosing instance, "String" and "int" for enums) are in desc, but not in signature. Need to keep them.
					String[] newParamTypes = new String[descParams.length];
					int i = 0, diff = descParams.length-paramTypes.length;
					for (; i < diff; i++) {
						newParamTypes[i] = descParams[i].getClassName();
						sv.paramTAIs.add(0, null); // also adjust paramTAIs:
					}
					System.arraycopy(paramTypes, 0, newParamTypes, i, paramTypes.length);
					paramTypes = newParamTypes;
				}
				
				// exception may (or may not have) been overwritten.
				// As class file format demands, only if exceptions contain
				// a type variable, this has to be set.
				if (sv.exceptionNames.size() > 0) {
					assert sv.exceptionNames.size() == exceptions.length;
					sv.exceptionNames.toArray(exceptions); // copies result
				}
			} else {
				resType = Type.getReturnType(desc).getClassName().replace('$','.');
				resultIsTypeVar = false;
				paramTypes = getAsStringArr(Type.getArgumentTypes(desc));
			}
			if (isConstructor) {
				currentMethod = new ConstructorInfo(access, result.getName(), paramTypes, makeReplString(exceptions), result);
				constructors.add((ConstructorInfo)currentMethod);
			} else {
				if ((result.getAccessFlags() & 0x2000) != 0) {
					currentMethod = new AnnotationPropertyInfo(access, resType, resultIsTypeVar, name, result, null);
				} else {
					currentMethod = new MethodInfo(access, resType, resultIsTypeVar, name, paramTypes, makeReplString(exceptions), result);
				}
				methods.add(currentMethod);
			}
			
			if (signature != null) {
				int totalSize = sv.paramTAIs.size()+1;
				for (int i = 0; i < sv.paramTAIs.size(); i++)
					sv.paramTAIs.trimToSize();
				currentMethod.paramTypeArgs = sv.paramTAIs.toArray(new List[totalSize]);
				currentMethod.paramTypeArgs[totalSize-1] = sv.returnTAIs;
				setTypeArgParentRec(currentMethod.paramTypeArgs, currentMethod);
				int tpCnt = sv.typeParamNames.size();
				currentMethod.typeParms = new ArrayList<TypeParameterInfo>(tpCnt);
				for (int i = 0; i <tpCnt; i++) {
					List<TypeArgumentInfo>[] typeParamTAIs = sv.typeParamTAIs.get(i).toArray(new List[0]);
					setTypeArgParentRec(typeParamTAIs, currentMethod);
					currentMethod.typeParms.add(new TypeParameterInfo(
						sv.typeParamNames.get(i), sv.typeParamBounds.get(i).toArray(new String[0]),
						typeParamTAIs, currentMethod
					));
				}
			}
			
			if (readJava5Signatures) { // obs: this does not imply that signature != null
				// retain annotations etc.
				methodAnnotations = new ArrayList<AnnotationUseInfo>();
				paramAnnotations = new ArrayList[paramTypes.length];
				for (int i = 0; i < paramTypes.length; i++)
					paramAnnotations[i] = new ArrayList<AnnotationUseInfo>();
				return new MyMethodVisitor();
			} else {
				currentMethod = null;
				return null;
			}
		}

		public void visitOuterClass(String owner, String name, String desc) {
			if (name != null)
				result.enclosingMethod = owner + "." + name + desc;
		}

		public void visitSource(String source, String debug)  { /* nothing to do */ }
		
		private void setTypeArgParentRec(List<? extends TypeArgument>[] typeArgs, MethodInfo res) {
			for(int i = 0; i < typeArgs.length; i++) {
				if (typeArgs[i] != null)
					setTypeArgParentRec(typeArgs[i], res);
			}
		}
		private void setTypeArgParentRec(List<? extends TypeArgument> typeArgs, MethodInfo res) {
			for(TypeArgument ta: typeArgs) {
				TypeArgumentInfo tai = (TypeArgumentInfo)ta;
				tai.parent = res;
				if (tai.typeArgs != null) {
					setTypeArgParentRec(tai.typeArgs, res);
				}
			}
		}
	}
	
	private ClassFile result;
	private ArrayList<FieldInfo> fields;
	private ArrayList<MethodInfo> methods;
	private ArrayList<ConstructorInfo> constructors;
	private MethodInfo currentMethod;
	private ArrayList<String> innerClassesNames;
	
	private ArrayList<AnnotationUseInfo> classAnnotations;
	private ArrayList<AnnotationUseInfo> methodAnnotations;
	private ArrayList<AnnotationUseInfo> paramAnnotations[];
	private ArrayList<AnnotationUseInfo> fieldAnnotations;
	
	public ClassFile parseClassFile(InputStream is, String location, boolean useJava5Features) throws IOException {
		this.readJava5Signatures = useJava5Features;
		result = new ClassFile();
		fields = new ArrayList<FieldInfo>();
		methods = new ArrayList<MethodInfo>();
		constructors = new ArrayList<ConstructorInfo>();
		innerClassesNames = new ArrayList<String>();
		classAnnotations = new ArrayList<AnnotationUseInfo>();

		result.setLocation(location);

		ClassReader classReader = new ClassReader(is);
		classReader.accept(new ClassV(), 
				ClassReader.SKIP_DEBUG | ClassReader.SKIP_FRAMES | ClassReader.SKIP_CODE);
	
		fields.trimToSize();
		methods.trimToSize();
		constructors.trimToSize();
		result.setFields(fields);
		result.setMethods(methods);
		result.setConstructors(constructors);
		result.setInnerClassNames(innerClassesNames.toArray(new String[innerClassesNames.size()]));
		classAnnotations.trimToSize();
		result.setAnnotations(classAnnotations);
		
		return result;
	}

	class MySignatureVisitor extends SignatureVisitor {
		private ArrayList<TypeArgumentInfo> curTAIs;
		
		private String mainType = null;
		private ArrayList<TypeArgumentInfo> returnTAIs;
		private ArrayList<TypeArgumentInfo> fieldTAIs;

		private ArrayList<ArrayList<TypeArgumentInfo>> paramTAIs = new ArrayList<ArrayList<TypeArgumentInfo>>();
		private ArrayList<String> paramTypes = new ArrayList<String>();
		
		private Stack<Integer> dimStack = new Stack<Integer>();
		private ArrayList<ArrayList<TypeArgumentInfo>> taiStack =
			new ArrayList<ArrayList<TypeArgumentInfo>>();
		private WildcardMode currentWM;
		private Stack<WildcardMode> wmStack = new Stack<WildcardMode>();
		
		private String superClassName;
		private ArrayList<String> interfaceNames = new ArrayList<String>();
		private ArrayList<TypeArgumentInfo> superClassTAIs;
		private ArrayList<ArrayList<TypeArgumentInfo>> interfaceTAIs = new ArrayList<ArrayList<TypeArgumentInfo>>();
		
		private ArrayList<String> typeParamNames = new ArrayList<String>();
		private ArrayList<ArrayList<String>> typeParamBounds = new ArrayList<ArrayList<String>>();
		private ArrayList<ArrayList<ArrayList<TypeArgumentInfo>>> typeParamTAIs = new ArrayList<ArrayList<ArrayList<TypeArgumentInfo>>>();
		
		private ArrayList<String> exceptionNames = new ArrayList<String>();
		
		private Stack<String> toAddStack = new Stack<String>();
		private Stack<CurrentlyParsing> cpStack = new Stack<CurrentlyParsing>();
		
		public MySignatureVisitor(boolean forField) {
			super(Opcodes.ASM4);
			if (forField) {
				dimStack.push(ZERO);
			}
		}

		private void addType(String s, boolean isTypeVar, boolean isPrimitive) {
			if (!(isPrimitive | isTypeVar))
				s = makeReplString(s);
			ArrayList<TypeArgumentInfo> newTAIs =  new ArrayList<TypeArgumentInfo>(0);
			if (currentlyParsing == CurrentlyParsing.PARAMETER) {
				paramTAIs.add(newTAIs);
				if (isTypeVar | isPrimitive) {
					int dim = dimStack.pop();
					while (dim > 0) { // typevar / primitive: visitEnd() not called!
						s += "[]";
						dim--;
					}
					paramTypes.add(s);
				}
			} else if (currentlyParsing == CurrentlyParsing.RETURNTYPE) {
				if (isTypeVar) {
					int dim = dimStack.pop();
					while (dim > 0) { // typevar / primitive: visitEnd() not called!
						s += "[]";
						dim--;
					}
					mainType = s;
				} else {
					returnTAIs = newTAIs;
					if (isPrimitive) {
						int dim = dimStack.pop();
						while (dim > 0) { // typevar / primitive: visitEnd() not called!
							s += "[]";
							dim--;
						}
						mainType = s;
					}
				}
			} else if (currentlyParsing == CurrentlyParsing.TYPEARGUMENT){
				boolean isArray = false;
				if (isPrimitive || isTypeVar) {
					int dim = dimStack.pop();
					while (dim > 0) { // typevar / primitive: visitEnd() not called!
						s += "[]";
						dim--;
						isArray = true;
					}
				}
				if (isTypeVar || s == null) {
					TypeArgumentInfo tai = new TypeArgumentInfo(currentWM, s, null, 
							currentMethod == null ? result : currentMethod, s != null);
					curTAIs.add(tai);
				} else {
					wmStack.push(currentWM);
				}
				assert isArray || !isPrimitive : "assertion error while parsing " + result.getLocation();
			} else if (currentlyParsing == CurrentlyParsing.CLASSORINTERFACEBOUND) {
				typeParamTAIs.get(typeParamTAIs.size()-1).add(newTAIs);
			} else if (currentlyParsing == null) {
				if (isTypeVar) {
					int dim = dimStack.pop();
					while (dim > 0) { // typevar / primitive: visitEnd() not called!
						s += "[]";
						dim--;
					}
					mainType = s;
				}
				fieldTAIs = newTAIs;
				assert !isPrimitive; // then there should definitely not be a signature, or what? 
			} else if (currentlyParsing == CurrentlyParsing.INTERFACE) {
				if (isTypeVar) {
					throw new RuntimeException("???");
				} else {
					interfaceTAIs.add(newTAIs);
				}
			} else if (currentlyParsing == CurrentlyParsing.SUPERCLASS) {
				superClassTAIs = newTAIs;
				assert !isTypeVar;
			} else if (currentlyParsing == CurrentlyParsing.FORMALTYPEPARAM) {
				typeParamNames.add(s);
				typeParamBounds.add(new ArrayList<String>(0));
				typeParamTAIs.add(new ArrayList<ArrayList<TypeArgumentInfo>>(0));
			} else if (currentlyParsing == CurrentlyParsing.EXCEPTION) {
				if (isTypeVar) {
					exceptionNames.add(s);
				}
				assert !isPrimitive;
			} else {
				throw new RuntimeException();
			}
			if (s != null && !isTypeVar && ((!dimStack.isEmpty() && dimStack.peek() > 0) || !isPrimitive)) {
				taiStack.add(curTAIs = newTAIs);
				cpStack.add(currentlyParsing);
				toAddStack.add(s);
			} else if (s != null && isTypeVar && currentlyParsing == CurrentlyParsing.CLASSORINTERFACEBOUND) {
				// no visitEnd will be called, so add the typeParamBounds already here.
				typeParamBounds.get(typeParamBounds.size()-1).add(s);
			}
		}
		
		public void visitEnd() {
			List<TypeArgumentInfo> oldTAIs = taiStack.remove(taiStack.size()-1); // == curTAIs
			if (!taiStack.isEmpty()) {
				curTAIs = taiStack.get(taiStack.size()-1);
			} else curTAIs = null;
			
			String s = toAddStack.pop();
			currentlyParsing = cpStack.pop();

			int dim = dimStack.pop();
			while (dim > 0) {
				s += "[]";
				dim--;
			}
			
			if (currentlyParsing == CurrentlyParsing.PARAMETER) {
				paramTypes.add(s);
			} else if (currentlyParsing == CurrentlyParsing.RETURNTYPE) {
				mainType = s;
			} else if (currentlyParsing == CurrentlyParsing.TYPEARGUMENT){
				currentWM = wmStack.pop();
				TypeArgumentInfo tai = new TypeArgumentInfo(currentWM, s, oldTAIs, 
						currentMethod == null ? result : currentMethod, false);
				curTAIs.add(tai);
			} else if (currentlyParsing == CurrentlyParsing.CLASSORINTERFACEBOUND) {
				typeParamBounds.get(typeParamBounds.size()-1).add(s);
			} else if (currentlyParsing == null) {
				mainType = s;
			} else if (currentlyParsing == CurrentlyParsing.INTERFACE) {
				interfaceNames.add(s);
			} else if (currentlyParsing == CurrentlyParsing.SUPERCLASS) {
				superClassName = s;
			} else if (currentlyParsing == CurrentlyParsing.FORMALTYPEPARAM) {
				// not of interest here (TODO why?)
			} else if (currentlyParsing == CurrentlyParsing.EXCEPTION) {
				exceptionNames.add(s);
			} else {
				throw new RuntimeException();
			}
		}

		
		public SignatureVisitor visitArrayType() {
			int dim = dimStack.pop();
			dim++;
			dimStack.push(dim);
			return this;
		}

		public void visitBaseType(char descriptor) {
			addType(Type.getType(descriptor+"").getClassName(), false, true);
		}

		public SignatureVisitor visitClassBound() {
			currentlyParsing = CurrentlyParsing.CLASSORINTERFACEBOUND;
			dimStack.push(ZERO);
			return this;
		}

		public SignatureVisitor visitInterfaceBound() {
			currentlyParsing = CurrentlyParsing.CLASSORINTERFACEBOUND;
			dimStack.push(ZERO);
			return this;
		}

		public void visitTypeArgument() {
			currentlyParsing = CurrentlyParsing.TYPEARGUMENT;
			currentWM = WildcardMode.Any;
			addType(null, false, false);
		}

		public SignatureVisitor visitTypeArgument(char wildcard) {
			currentlyParsing = CurrentlyParsing.TYPEARGUMENT;
			dimStack.push(ZERO);
			switch (wildcard) {
			case '+': currentWM = WildcardMode.Extends; break;
			case '-': currentWM = WildcardMode.Super; break;
			case '=': currentWM = WildcardMode.None; break;
			default: throw new RuntimeException();
			}
			return this;
		}

		public void visitClassType(String name) {
			addType(name, false, false);
		}
		
		public void visitTypeVariable(String name) {
			addType(name, true, false);
		}
		
		public SignatureVisitor visitExceptionType() {
			currentlyParsing = CurrentlyParsing.EXCEPTION;
			dimStack.push(ZERO);
			return this;
		}

		public void visitFormalTypeParameter(String name) {
			currentlyParsing = CurrentlyParsing.FORMALTYPEPARAM;
			dimStack.push(ZERO);
			addType(name, false, false);
		}
		
		public void visitInnerClassType(String name) {
			String old = toAddStack.pop();
			toAddStack.add(old + "." + name);
		}
		
		public SignatureVisitor visitInterface() {
			currentlyParsing = CurrentlyParsing.INTERFACE;
			dimStack.push(ZERO);
			return this;
		}
		
		public SignatureVisitor visitParameterType() {
			currentlyParsing = CurrentlyParsing.PARAMETER;
			dimStack.push(ZERO);
			return this;
		}
		
		public SignatureVisitor visitReturnType() {
			currentlyParsing = CurrentlyParsing.RETURNTYPE;
			dimStack.push(ZERO);
			return this;
		}
		
		public SignatureVisitor visitSuperclass() {
			currentlyParsing = CurrentlyParsing.SUPERCLASS;
			dimStack.push(ZERO);
			return this;
		}
		
		private CurrentlyParsing currentlyParsing;
		
	}

	enum CurrentlyParsing {
		RETURNTYPE, PARAMETER, TYPEARGUMENT, CLASSORINTERFACEBOUND, INTERFACE, SUPERCLASS,
		FORMALTYPEPARAM, EXCEPTION
	}
	
	class MyFieldVisitor extends FieldVisitor {
		public MyFieldVisitor() {
			super(Opcodes.ASM4);
		}

		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			assert readJava5Signatures;
			AnnotationUseInfo currentAnnotation = new AnnotationUseInfo(
					makeAnnotationString(desc),
					new ArrayList<ElementValuePairInfo>(0));
			fieldAnnotations.add(currentAnnotation);
			return new MyAnnotationVisitor(currentAnnotation);
		}

		public void visitAttribute(Attribute attr) {
			// non-standard attribute. Currently ignored.
		}

		public void visitEnd() {
			if (readJava5Signatures) {
				fieldAnnotations.trimToSize();
				fields.get(fields.size()-1).annotations = fieldAnnotations;
				fieldAnnotations = null;
			}
		}
		
	}
	
	class MyMethodVisitor extends MethodVisitor {
		public MyMethodVisitor() {
			super(Opcodes.ASM4);
		}

		public AnnotationVisitor visitAnnotation(String desc, boolean visible) {
			assert readJava5Signatures;
			AnnotationUseInfo currentAnnotation = new AnnotationUseInfo(
					makeAnnotationString(desc),
					new ArrayList<ElementValuePairInfo>(0));
			methodAnnotations.add(currentAnnotation);
			return new MyAnnotationVisitor(currentAnnotation);
		}
		
		public AnnotationVisitor visitAnnotationDefault() {
			return new MyAnnotationVisitor((AnnotationPropertyInfo)currentMethod);
		}
		public void visitAttribute(Attribute attr) {
			// non-standard attribute. Currently ignored.
		}
		@SuppressWarnings("unchecked")
		public void visitEnd() {
			if (readJava5Signatures) {
				methodAnnotations.trimToSize();
				currentMethod.annotations = methodAnnotations;
				methodAnnotations = null;
				currentMethod.paramAnnotations = new AnnotationUseInfo[paramAnnotations.length][];
				for (int i = 0; i < paramAnnotations.length; i++) {
					currentMethod.paramAnnotations[i] = paramAnnotations[i].toArray(new AnnotationUseInfo[paramAnnotations[i].size()]);
				}
				paramAnnotations = null;
			}
			// in case this is a constructor of an inner class, remove first parameter entirely:
			if (currentMethod instanceof ConstructorInfo && result.isInner) {
				currentMethod.paramTypeArgs = removeFirstInArray(currentMethod.paramTypeArgs, List.class);
				currentMethod.paramAnnotations = removeFirstInArray(currentMethod.paramAnnotations, AnnotationUseInfo[].class);
				currentMethod.paramtypes = removeFirstInArray(currentMethod.paramtypes, String.class);
			}

			currentMethod = null;
		}
		
		private <E> E[] removeFirstInArray(E[] arr, Class<E> clazz) { 
			if (arr == null) return null;
			@SuppressWarnings("unchecked")
			E[] res = (E[])Array.newInstance(clazz, arr.length-1);
			System.arraycopy(arr, 1, res, 0, arr.length-1);
			return res;
		}
		
		public AnnotationVisitor visitParameterAnnotation(int parameter,
				String desc, boolean visible) {
			if (!readJava5Signatures)
				return null;
			AnnotationUseInfo currentAnnotation = new AnnotationUseInfo(
					makeAnnotationString(desc),
					new ArrayList<ElementValuePairInfo>(0));
			paramAnnotations[parameter].add(currentAnnotation);
			return new MyAnnotationVisitor(currentAnnotation);
		}
	}
	
	private static class MyAnnotationVisitor extends AnnotationVisitor {
		private ArrayList<String> arrayName = new ArrayList<String>();
		private ArrayList<ArrayList<Object>> arrayValues = new ArrayList<ArrayList<Object>>();
		private AnnotationPropertyInfo setDefaultFor;
		
		private ArrayList<Object> nestedStack = new ArrayList<Object>();
		
		MyAnnotationVisitor(AnnotationUseInfo forAnnotationUse) {
			super(Opcodes.ASM4);
			nestedStack.add(forAnnotationUse);
			arrayName.add(null);
		}
		
		MyAnnotationVisitor(AnnotationPropertyInfo setDefaultFor) {
			super(Opcodes.ASM4);
			this.setDefaultFor = setDefaultFor; 
			nestedStack.add(null); // reading a default value - must be handled specially!
			arrayName.add(null);
		}
		
		private ArrayList<Object> getCurrentArrayValueList() {
			return arrayValues.get(arrayValues.size()-1);
		}
		
		private boolean isInArray() {
			Object o = nestedStack.get(nestedStack.size()-1);
			return o != null && !(o instanceof AnnotationUseInfo);
		}
		
		private boolean isSettingDefault() {
			if (nestedStack.get(0) != null)
				return false;
			for (int i = nestedStack.size()-1; i >= 1; i--) {
				Object o = nestedStack.get(i);
				if (o instanceof AnnotationUseInfo)
					return false;
			}
			return true;
		}
		
		private AnnotationUseInfo getCurrentAnnotation() {
			// visitEnd is also called after visitArray. Therefore, a dummy-object is 
			// put on stack for each visitArray. Since the current annotation still needs
			// to be accessible, we look from top of the stack for the first AnnotationUseInfo.
			for (int i = nestedStack.size()-1;; i--) {
				Object o = nestedStack.get(i);
				if (o instanceof AnnotationUseInfo)
					return (AnnotationUseInfo)o;
			}
		}
		
		public void visitEnum(String name, String desc, String value) {
			EnumConstantReferenceInfo ecri = new EnumConstantReferenceInfo(makeAnnotationString(desc), value);
			if (isInArray()) {
				assert name == null;
				getCurrentArrayValueList().add(ecri);
			} else if (isSettingDefault()){
				setDefaultFor.defaultValue = ecri;
			} else {
				getCurrentAnnotation().elementValuePairs.add(new ElementValuePairInfo(name, 
						ecri));
			}
		}
		public void visitEnd() {
			Object rem = nestedStack.remove(nestedStack.size()-1);
			if (rem instanceof AnnotationUseInfo || rem == null) {
				// nothing else to do.
			} else {
				String curArrayName = arrayName.remove(arrayName.size()-1);
				assert (curArrayName != null || isSettingDefault());
				if (curArrayName != null) {
					getCurrentAnnotation().elementValuePairs.add(
						new ElementValuePairInfo(curArrayName, getCurrentArrayValueList().toArray()));
					arrayValues.remove(arrayValues.size()-1);
				} else {
					setDefaultFor.defaultValue = new Object[0];
				}
			}
			 
		}
		public AnnotationVisitor visitArray(String name) {
			arrayName.add(name);
			arrayValues.add(new ArrayList<Object>());
			nestedStack.add(new Object()); // hack: visitEnd is also called after visitArray.
			return this;
		}
		public AnnotationVisitor visitAnnotation(String name, String desc) {
			AnnotationUseInfo aui = new AnnotationUseInfo(makeAnnotationString(desc), new ArrayList<ElementValuePairInfo>(0));
			if (isInArray()) {
				assert name == null;
				getCurrentArrayValueList().add(aui);
			} else if (isSettingDefault()){
				setDefaultFor.defaultValue = aui;
			} else {
				getCurrentAnnotation().elementValuePairs.add(new ElementValuePairInfo(name, aui));
			}
			nestedStack.add(aui);
			return this;
		}
		public void visit(String name, Object value) {
			if (isInArray()) {
				assert name == null;
				getCurrentArrayValueList().add(value);
			} else if (isSettingDefault()){
				setDefaultFor.defaultValue = value;
			} else {
				getCurrentAnnotation().elementValuePairs.add(new ElementValuePairInfo(name, value));
			}
		}
	}
}
