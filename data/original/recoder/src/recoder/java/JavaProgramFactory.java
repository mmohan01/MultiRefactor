// This file is part of the RECODER library and protected by the LGPL

package recoder.java;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.nio.CharBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import recoder.ParserException;
import recoder.ProgramFactory;
import recoder.ServiceConfiguration;
import recoder.abstraction.TypeArgument.WildcardMode;
import recoder.convenience.TreeWalker;
import recoder.io.ProjectSettings;
import recoder.io.PropertyNames;
import recoder.java.SourceElement.Position;
import recoder.java.declaration.AnnotationDeclaration;
import recoder.java.declaration.AnnotationElementValuePair;
import recoder.java.declaration.AnnotationPropertyDeclaration;
import recoder.java.declaration.AnnotationUseSpecification;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.ClassInitializer;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.DeclarationSpecifier;
import recoder.java.declaration.EnumConstantDeclaration;
import recoder.java.declaration.EnumConstantSpecification;
import recoder.java.declaration.EnumDeclaration;
import recoder.java.declaration.Extends;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.FieldSpecification;
import recoder.java.declaration.Implements;
import recoder.java.declaration.InterfaceDeclaration;
import recoder.java.declaration.LocalVariableDeclaration;
import recoder.java.declaration.MemberDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.ParameterDeclaration;
import recoder.java.declaration.Throws;
import recoder.java.declaration.TypeArgumentDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.TypeParameterDeclaration;
import recoder.java.declaration.UnionTypeParameterDeclaration;
import recoder.java.declaration.VariableSpecification;
import recoder.java.declaration.modifier.Abstract;
import recoder.java.declaration.modifier.Final;
import recoder.java.declaration.modifier.Native;
import recoder.java.declaration.modifier.Private;
import recoder.java.declaration.modifier.Protected;
import recoder.java.declaration.modifier.Public;
import recoder.java.declaration.modifier.Static;
import recoder.java.declaration.modifier.StrictFp;
import recoder.java.declaration.modifier.Synchronized;
import recoder.java.declaration.modifier.Transient;
import recoder.java.declaration.modifier.VisibilityModifier;
import recoder.java.declaration.modifier.Volatile;
import recoder.java.expression.ArrayInitializer;
import recoder.java.expression.ElementValueArrayInitializer;
import recoder.java.expression.ParenthesizedExpression;
import recoder.java.expression.literal.BooleanLiteral;
import recoder.java.expression.literal.CharLiteral;
import recoder.java.expression.literal.DoubleLiteral;
import recoder.java.expression.literal.FloatLiteral;
import recoder.java.expression.literal.IntLiteral;
import recoder.java.expression.literal.LongLiteral;
import recoder.java.expression.literal.NullLiteral;
import recoder.java.expression.literal.StringLiteral;
import recoder.java.expression.operator.BinaryAnd;
import recoder.java.expression.operator.BinaryAndAssignment;
import recoder.java.expression.operator.BinaryNot;
import recoder.java.expression.operator.BinaryOr;
import recoder.java.expression.operator.BinaryOrAssignment;
import recoder.java.expression.operator.BinaryXOr;
import recoder.java.expression.operator.BinaryXOrAssignment;
import recoder.java.expression.operator.Conditional;
import recoder.java.expression.operator.CopyAssignment;
import recoder.java.expression.operator.Divide;
import recoder.java.expression.operator.DivideAssignment;
import recoder.java.expression.operator.Equals;
import recoder.java.expression.operator.GreaterOrEquals;
import recoder.java.expression.operator.GreaterThan;
import recoder.java.expression.operator.Instanceof;
import recoder.java.expression.operator.LessOrEquals;
import recoder.java.expression.operator.LessThan;
import recoder.java.expression.operator.LogicalAnd;
import recoder.java.expression.operator.LogicalNot;
import recoder.java.expression.operator.LogicalOr;
import recoder.java.expression.operator.Minus;
import recoder.java.expression.operator.MinusAssignment;
import recoder.java.expression.operator.Modulo;
import recoder.java.expression.operator.ModuloAssignment;
import recoder.java.expression.operator.Negative;
import recoder.java.expression.operator.New;
import recoder.java.expression.operator.NewArray;
import recoder.java.expression.operator.NotEquals;
import recoder.java.expression.operator.Plus;
import recoder.java.expression.operator.PlusAssignment;
import recoder.java.expression.operator.Positive;
import recoder.java.expression.operator.PostDecrement;
import recoder.java.expression.operator.PostIncrement;
import recoder.java.expression.operator.PreDecrement;
import recoder.java.expression.operator.PreIncrement;
import recoder.java.expression.operator.ShiftLeft;
import recoder.java.expression.operator.ShiftLeftAssignment;
import recoder.java.expression.operator.ShiftRight;
import recoder.java.expression.operator.ShiftRightAssignment;
import recoder.java.expression.operator.Times;
import recoder.java.expression.operator.TimesAssignment;
import recoder.java.expression.operator.TypeCast;
import recoder.java.expression.operator.UnsignedShiftRight;
import recoder.java.expression.operator.UnsignedShiftRightAssignment;
import recoder.java.reference.AnnotationPropertyReference;
import recoder.java.reference.ArrayReference;
import recoder.java.reference.EnumConstructorReference;
import recoder.java.reference.FieldReference;
import recoder.java.reference.MetaClassReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.PackageReference;
import recoder.java.reference.ReferencePrefix;
import recoder.java.reference.SuperConstructorReference;
import recoder.java.reference.SuperReference;
import recoder.java.reference.ThisConstructorReference;
import recoder.java.reference.ThisReference;
import recoder.java.reference.TypeReference;
import recoder.java.reference.UncollatedReferenceQualifier;
import recoder.java.reference.VariableReference;
import recoder.java.statement.Assert;
import recoder.java.statement.Branch;
import recoder.java.statement.Break;
import recoder.java.statement.Case;
import recoder.java.statement.Catch;
import recoder.java.statement.Continue;
import recoder.java.statement.Default;
import recoder.java.statement.Do;
import recoder.java.statement.Else;
import recoder.java.statement.EmptyStatement;
import recoder.java.statement.EnhancedFor;
import recoder.java.statement.Finally;
import recoder.java.statement.For;
import recoder.java.statement.If;
import recoder.java.statement.LabeledStatement;
import recoder.java.statement.Return;
import recoder.java.statement.Switch;
import recoder.java.statement.SynchronizedBlock;
import recoder.java.statement.Then;
import recoder.java.statement.Throw;
import recoder.java.statement.Try;
import recoder.java.statement.While;
import recoder.list.generic.ASTArrayList;
import recoder.list.generic.ASTList;
import recoder.parser.JavaCCParser;
import recoder.util.StringUtils;

public class JavaProgramFactory implements ProgramFactory, PropertyChangeListener {

    /**
     * No longer a singleton as of Recoder 0.93
     */
    public JavaProgramFactory() { 
    	// nothing to do
    }
    
    // TODO I rather have this removed for the future...
    public JavaCCParser getParser() {
    	return parser;
    }
    
    public static class TraceItem {
    	public final ProgramElement pe;
    	public final String st;
    	public TraceItem(ProgramElement pe) {
    		this.pe = pe;
    		StackTraceElement[] ste = new Throwable().getStackTrace();
    		int startIdx = 3;
    		while (ste[startIdx].toString().indexOf("<init>") != -1
    				|| ste[startIdx].toString().indexOf(".deepClone(") != -1)
    			startIdx++;
    		st = "\t"+ste[startIdx+0]+"\n\t"+ste[startIdx+1]+"\n\t"+ste[startIdx+2];
    	}
    	@Override
    	public boolean equals(Object o) {
    		if (o == pe)
    			return true; // TODO hack!!
    		if (!(o instanceof TraceItem))
    			return false;
    		return ((TraceItem)o).pe == pe;
    	}
    	@Override
    	public int hashCode() {
    		return pe.hashCode();
    	}
    	@Override
    	public String toString() {
    		return pe.toString() + "\n" + st;
    	}
    }
    
    private HashMap<ProgramElement, TraceItem> createdItems;
    private boolean doTrace = false;
    private boolean autoTrace = true; // TODO...
    /**
     * debug method
     * @since 0.90
     */
    public void beginTracing() {
    	createdItems = new HashMap<ProgramElement, TraceItem>(1000);
    	doTrace = true;
    }
    
    public void detrace(ProgramElement pe) {
    	TreeWalker tw = new TreeWalker(pe);
    	while (tw.next()) {
    		createdItems.remove(tw.getProgramElement());
    	}
    }
    
    public TraceItem getTraceItem(ProgramElement pe) {
    	return createdItems.get(pe);
    }
    

    /**
     * debug method
     * @since 0.90
     */
    public Collection<TraceItem> endTracing() {
    	doTrace = false;
    	return createdItems.values();
    }
    
    // only to be called from JavaSourceElement prototype-constructor and internally!!
    public void trace(ProgramElement pe) {
    	if (doTrace && autoTrace) {
    		createdItems.put(pe, new TraceItem(pe));
    	}
    }
    
    public void manTrace(ProgramElement pe) {
    	if (doTrace)
    		createdItems.put(pe, new TraceItem(pe));
    }

    /**
     * The singleton instance of the program factory.
     */
    private ServiceConfiguration serviceConfiguration;

    /**
     * StringWriter for toSource.
     */
    private StringWriter writer = new StringWriter();

    /**
     * PrettyPrinter, for toSource.
     */
    private PrettyPrinter sourcePrinter;
    
    private boolean useAddNewlineReader = true;

    /**
     * Called by the service configuration indicating that all services are
     * known. Services may now start communicating or linking among their
     * configuration partners. The service configuration can be memorized if it
     * has not been passed in by a constructor already.
     * 
     * @param cfg
     *            the service configuration this services has been assigned to.
     */
    public void initialize(ServiceConfiguration cfg) {
        serviceConfiguration = cfg;
        
        ProjectSettings settings = serviceConfiguration.getProjectSettings();
        settings.addPropertyChangeListener(this);
        writer = new StringWriter();
        sourcePrinter = new PrettyPrinter(writer, settings.getProperties());
        parser.setAwareOfAssert(StringUtils.parseBooleanProperty(settings.getProperties().getProperty(
                PropertyNames.JDK1_4)));
        parser.setJava5(StringUtils.parseBooleanProperty(settings.getProperties().getProperty(
                PropertyNames.JAVA_5)));
        parser.setJava7(StringUtils.parseBooleanProperty(settings.getProperties().getProperty(
        		PropertyNames.JAVA_7)));
    }

    /**
     * Returns the service configuration this service is a part of.
     * 
     * @return the configuration of this service.
     */
    public ServiceConfiguration getServiceConfiguration() {
        return serviceConfiguration;
    }

    /**
     * For internal reuse and synchronization.
     */
    private JavaCCParser parser = new JavaCCParser(System.in);

    private final static Position ZERO_POSITION = new Position(0, 0);
    
    private void attachComment(Comment c, ProgramElement pe) {
    	ProgramElement dest = pe;

    	if (c.isPrefixed() && pe instanceof CompilationUnit && ((CompilationUnit)pe).getChildCount() > 0) {
    		// may need attach to first child element
    		ProgramElement fc = ((CompilationUnit)pe).getChildAt(0);
    		int distcu = c.getStartPosition().getLine();
    		int distfc = fc.getStartPosition().getLine() - c.getEndPosition().getLine();
    		if (c instanceof SingleLineComment) distcu--;
    		if (distcu >= distfc) {
    			dest = fc; 
    		}
    	}
    	else if (!c.isPrefixed()) {
            NonTerminalProgramElement ppe = dest.getASTParent();
            int i = 0;
            if (ppe != null) {
                while (ppe.getChildAt(i) != dest) i++;
            }
            if (i == 0) { // before syntactical parent
                c.setPrefixed(true);
            } else {
                dest = ppe.getChildAt(i - 1);
                while (dest instanceof NonTerminalProgramElement) {
                    ppe = (NonTerminalProgramElement) dest;
                    i = ppe.getChildCount();
                    if (i == 0) {
                        break;
                    }
                    dest = ppe.getChildAt(i - 1);
                }
                // Comments attached better - Fix by T.Gutzmann
                ppe = dest.getASTParent();
                boolean doChange = false;
                while (ppe != null && ppe.getASTParent() != null
                        && ppe.getEndPosition().compareTo(dest.getEndPosition()) >= 0
                        && ppe.getASTParent().getEndPosition().compareTo(c.getStartPosition()) <= 0) {
                    ppe = ppe.getASTParent();
                    doChange = true;
                }
                if (ppe != null && doChange)
                    dest = ppe;
                if (dest instanceof NonTerminalProgramElement) {
                    ppe = (NonTerminalProgramElement) dest;
                    if (ppe.getEndPosition().compareTo(c.getStartPosition()) >= 0) {
                        while (ppe.getChildCount() > 0
                                && ppe.getChildAt(ppe.getChildCount() - 1).getEndPosition().compareTo(
                                        ppe.getEndPosition()) == 0
                                // TODO Gutzmann - this shouldn't be neccessary
                                && ppe.getChildAt(ppe.getChildCount() - 1) instanceof NonTerminalProgramElement) {
                            ppe = (NonTerminalProgramElement) ppe.getChildAt(ppe.getChildCount() - 1);
                            dest = ppe;
                        }
                        c.setContainerComment(true);
                    }
                }
                if (!c.isContainerComment() && pe != dest) {
                    // if in between two program elements in same line, prefer prefixing/look at number of whitespaces
                    if (pe.getFirstElement().getStartPosition().getLine() == dest.getLastElement().getEndPosition().getLine()) {
                        // TODO strategy when looking at # of whitespaces ?!
                        int before = c.getStartPosition().getColumn() - dest.getLastElement().getEndPosition().getColumn();
                        int after = pe.getFirstElement().getStartPosition().getColumn() - c.getEndPosition().getColumn();
                        if (after <= before) {
                            dest = pe;
                            c.setPrefixed(true);
                        }
                    }
                }
            }
        }
    	if (c.isPrefixed()) {
    		// once again, go up as long as possible
    		NonTerminalProgramElement npe = dest.getASTParent();
    		while (npe != null && npe.getStartPosition().equals(dest.getStartPosition())) {
    			dest = npe;
    			npe = npe.getASTParent();
    		}
    	} else if (!c.isContainerComment()) {
    		NonTerminalProgramElement npe = dest.getASTParent();
    		while (npe != null && npe.getEndPosition().equals(dest.getEndPosition())) {
    			dest = npe;
    			npe = npe.getASTParent();
    		}
    	}
    	// if this is a full line comment, may need to change
    	if (c.isPrefixed() && c.getEndPosition().getLine() <  dest.getStartPosition().getLine()) {
    		NonTerminalProgramElement npe = dest.getASTParent();
    		if (npe != null) {
    			int idx = npe.getIndexOfChild(dest);
    			if (idx > 0) {
    				// calculate distance, maybe attach to next element
    				int distPre = dest.getStartPosition().getLine() - c.getEndPosition().getLine();
    				int distPost = c.getStartPosition().getLine() - npe.getChildAt(idx-1).getEndPosition().getLine();
    				if (c instanceof SingleLineComment)
    					distPost--; // prefer postfix comment in this case
    				if (distPost < distPre) {
    					dest = npe.getChildAt(idx-1);
    					c.setPrefixed(false);
    				}
    			}
    		}
    	} else if (!c.isPrefixed() && c.getStartPosition().getLine() > dest.getEndPosition().getLine()) {
    		NonTerminalProgramElement npe = dest.getASTParent();
    		if (npe != null) {
    			int idx = npe.getIndexOfChild(dest);
    			if (idx+1 < npe.getChildCount()) {
    				int distPre = npe.getChildAt(idx+1).getStartPosition().getLine() - c.getEndPosition().getLine();
    				int distPost = c.getStartPosition().getLine() - dest.getEndPosition().getLine();
    				if (c instanceof SingleLineComment)
    					distPost--;
    				if (distPre <= distPost) {
    					dest = npe.getChildAt(idx+1);
    					c.setPrefixed(true);
    				}
    			}
    		}
    	}
    	
        if (c instanceof SingleLineComment && c.isPrefixed()) {
            Position p = dest.getFirstElement().getRelativePosition();
            if (p.getLine() < 1) {
                p.setLine(1);
                dest.getFirstElement().setRelativePosition(p);
            }
        }
        ASTList<Comment> cml = dest.getComments();
        if (cml == null) {
            dest.setComments(cml = new ASTArrayList<Comment>(1));
        }
        cml.add(c);
    }

    /**
     * Perform post work on the created element. Creates parent links and
     * assigns comments.
     */
    private void postWork(ProgramElement pe, List<Comment> comments) {
    	int commentIndex = 0;
        int commentCount = comments.size();
        Position cpos = ZERO_POSITION;
        Comment current = null;
        if (commentIndex < commentCount) {
            current = comments.get(commentIndex);
            cpos = current.getFirstElement().getStartPosition();
        }
        TreeWalker tw = new TreeWalker(pe);
        while (tw.next()) {
            pe = tw.getProgramElement();
            if (pe instanceof NonTerminalProgramElement) {
                ((NonTerminalProgramElement) pe).makeParentRoleValid();
            }
            if (pe instanceof StatementBlock || pe instanceof ArrayInitializer || pe instanceof TypeDeclaration) {
            	// Just another hotfix...
            	while (
            		(	(pe instanceof StatementBlock && ((StatementBlock)pe).getStatementCount() == 0)
            		|| (pe instanceof ArrayInitializer && (((ArrayInitializer)pe).getArguments() == null || ((ArrayInitializer)pe).getArguments().size() == 0))
            		|| (pe instanceof TypeDeclaration && (((TypeDeclaration)pe).getMembers() == null ||((TypeDeclaration)pe).getMembers().size() == 0)))
            		&& (pe.getStartPosition().compareTo(cpos) < 0 && pe.getEndPosition().compareTo(cpos) > 0)) {
            			current.setContainerComment(true);
            			ASTList<Comment> cml = pe.getComments();
            	        if (cml == null) {
            	            pe.setComments(cml = new ASTArrayList<Comment>(1));
            	        }
            	        cml.add(current);
            	        commentIndex += 1;
            	        if (commentIndex < commentCount) {
            	        	current = comments.get(commentIndex);
            	        	cpos = current.getFirstElement().getStartPosition();
            	        } else break;
            	}
            }

            Position pos = pe.getFirstElement().getStartPosition();
            while ((commentIndex < commentCount) && pos.compareTo(cpos) > 0) {
                attachComment(current, pe);
                commentIndex += 1;
                if (commentIndex < commentCount) {
                    current = comments.get(commentIndex);
                    cpos = current.getFirstElement().getStartPosition();
                }
            }
        }
        if (commentIndex < commentCount) {
            while (pe.getASTParent() != null) {
                pe = pe.getASTParent();
            }

            /*
             * postfixed comments may need to be attached to a child of current
             * program element, so move down AST while child is closer to comment
             * position.
             */
            do {
                current = comments.get(commentIndex);
                ProgramElement dest = pe;
                ProgramElement newDest = null;
                while (dest instanceof NonTerminalProgramElement) {
                    NonTerminalProgramElement npe = (NonTerminalProgramElement) dest;
                    if (npe.getChildCount() == 0)
                        break;
                    newDest = npe.getChildAt(npe.getChildCount() - 1);
                    if ((npe.getEndPosition().compareTo(current.getStartPosition()) > 0 || ((npe.getEndPosition()
                            .compareTo(current.getStartPosition()) == 0) && newDest.getEndPosition().compareTo(
                            current.getStartPosition()) <= 0))
                            && dest != newDest)
                        dest = newDest;
                    else
                        break;
                }
                ASTList<Comment> cml = dest.getComments();
                if (cml == null) {
                    dest.setComments(cml = new ASTArrayList<Comment>(1));
                }
                current.setPrefixed(false);
                cml.add(current);
                commentIndex += 1;
            } while (commentIndex < commentCount);
        }
    }
    
    private class AddNewlineReader extends Reader {
    	private Reader reader;
    	AddNewlineReader(Reader reader) {
    		this.reader = reader;
    	}
    	@Override
		public void mark(int readAheadLimit) throws IOException {
			reader.mark(readAheadLimit);
		}
		@Override
		public boolean markSupported() {
			return reader.markSupported();
		}
		@Override
		public int read() throws IOException {
			return reader.read();
		}
		@Override
		public int read(char[] cbuf) throws IOException {
			return reader.read(cbuf);
		}
		@Override
		public int read(CharBuffer target) throws IOException {
			return reader.read(target);
		}
		@Override
		public boolean ready() throws IOException {
			return reader.ready();
		}
		@Override
		public void reset() throws IOException {
			reader.reset();
		}
		@Override
		public long skip(long n) throws IOException {
			return reader.skip(n);
		}		
		@Override
		public void close() throws IOException {
			reader.close();
		}
		private boolean added = false;
		@Override
		public int read(char[] cbuf, int off, int len) throws IOException {
			if (added) return -1;
			int result = reader.read(cbuf, off, len);
			if (!added && result < len) {
				if (result == -1) result++;
				cbuf[off+result++] = '\n';
				added = true;
			}			
			return result;
		}
    }

    /**
     * used SOLELY for testing: when set to "true", each CompilationUnit
     * is deepClone()d before returned. Doesn't do any harm to set to
     * true, but affects performance and possibly even memory
     * consumption. 
     */
    public static boolean TESTING_DeepClone_Each_CU_before_return = false;
    
    /**
     * Parse a {@link CompilationUnit}from the given reader.
     */
    @SuppressWarnings("resource") // ok as AddNewlineReader doesn't hold any resource. Caller will close underlying stream.
	public CompilationUnit parseCompilationUnit(Reader in) throws IOException, ParserException {
//    	try {
//    		JavaLexer jl = new JavaLexer(new ANTLRReaderStream(in));
//    		TokenStream ts = new CommonTokenStream(jl);
//    		//ts.consume();
//    		JavaParser jp = new JavaParser(ts);
//    		jp.factory = this;
//    		CompilationUnit cu = jp.compilationUnit();
//    		postWork(cu, jp.getComments());
//    		return cu;
//    	} catch (Exception e) {
//    		throw new RuntimeException(e);
//    	}
    	        synchronized 
        (parser) {
            parser.initialize(useAddNewlineReader ? new AddNewlineReader(in) : in, this);
            CompilationUnit res = parser.CompilationUnit();
            postWork(res, parser.getComments());
            if (TESTING_DeepClone_Each_CU_before_return)
            	res = res.deepClone(); // for testing purposes.
            return res;
        }
    }
    
    /**
     * Parse a {@link CompilationUnit}from the given reader.
     * The supplied sourceVersion parameter describes the java version.
     * @author NAI
     * 
     * @param in
     * @param sourceVersion allowed values: "1.3", "1.4", "5". Defaults to Java 1.4 behavior if sourceVersion is <code>null</code> or any other string.
     * @return
     * @throws IOException
     * @throws ParserException
     */
    @SuppressWarnings("resource") // ok as AddNewlineReader doesn't hold any resource. Caller will close underlying stream.
    public CompilationUnit parseCompilationUnit(Reader in, String sourceVersion) throws IOException, ParserException {
    	//default java version is java1.4
    	boolean java14=true;
    	boolean java5=false;
    	boolean java7=false;
    	if(sourceVersion!=null){
    		if(sourceVersion.equals("1.3") || sourceVersion.startsWith("1.3.")){
    			java14=false;
    			java5=false;
    			java7=false;
    		}
    		if(sourceVersion.equals("1.4") || sourceVersion.startsWith("1.4.")){
    			java14=true;
    			java5=false;
    			java7=false;
    		}
    		if(sourceVersion.equals("1.5") || sourceVersion.startsWith("1.5.")){
    			java14=true;
    			java5=true;
    			java7=false;
    		}
    		if(sourceVersion.equals("5") || sourceVersion.startsWith("5.")){
    			java14=true;
    			java5=true;
    			java7=false;
    		}
    		if (sourceVersion.equals("1.7") || sourceVersion.startsWith("1.7.")) {
    			java14 = java5 = java7 = true;
    		}
    		if (sourceVersion.equals("7") || sourceVersion.startsWith("7.")) {
    			java14 = java5 = java7 = true;
    		}
    	}
        synchronized 
        (parser) {
        	boolean wasJava14=parser.isAwareOfAssert();
        	boolean wasJava5 =parser.isJava5();
        	boolean wasJava7 = parser.isJava7();
        	
        	parser.setAwareOfAssert(java14);
            parser.setJava5(java5);
            parser.setJava7(java7);
            parser.initialize(useAddNewlineReader ? new AddNewlineReader(in) : in, this);
            CompilationUnit res = parser.CompilationUnit();
            postWork(res, parser.getComments());

            parser.setAwareOfAssert(wasJava14);
            parser.setJava5(wasJava5);
            parser.setJava7(wasJava7);
                        
            return res;
        }
    }

    /**
     * Parse a {@link TypeDeclaration}from the given reader.
     */
    public TypeDeclaration parseTypeDeclaration(Reader in) throws IOException, ParserException {
        synchronized (parser) {
            parser.initialize(in, this);
            TypeDeclaration res = parser.TypeDeclaration();
            postWork(res, parser.getComments());
            return res;
        }
    }
    
    public TypeArgumentDeclaration parseTypeArgumentDeclaration(Reader in) throws IOException, ParserException {
    	synchronized(parser) {
    		parser.initialize(in, this);
    		TypeArgumentDeclaration res = parser.TypeArgument();
    		postWork(res, parser.getComments());
    		return res;
    	}
    }

    /**
     * Parse a {@link FieldDeclaration}from the given reader.
     */
    public FieldDeclaration parseFieldDeclaration(Reader in) throws IOException, ParserException {
        synchronized (parser) {
            parser.initialize(in, this);
            FieldDeclaration res = parser.FieldDeclaration();
            postWork(res, parser.getComments());
            return res;
        }
    }

    /**
     * Parse a {@link MethodDeclaration}from the given reader.
     */
    public MethodDeclaration parseMethodDeclaration(Reader in) throws IOException, ParserException {
        synchronized (parser) {
            parser.initialize(in, this);
            MethodDeclaration res = parser.MethodDeclaration();
            postWork(res, parser.getComments());
            return res;
        }
    }

    /**
     * Parse a {@link MemberDeclaration}from the given reader.
     */
    public MemberDeclaration parseMemberDeclaration(Reader in) throws IOException, ParserException {
        synchronized (parser) {
            parser.initialize(in, this);
            MemberDeclaration res = parser.ClassBodyDeclaration();
            postWork(res, parser.getComments());
            return res;
        }
    }

    /**
     * Parse a {@link ParameterDeclaration}from the given reader.
     */
    public ParameterDeclaration parseParameterDeclaration(Reader in) throws IOException, ParserException {
        synchronized (parser) {
            parser.initialize(in, this);
            ParameterDeclaration res = parser.FormalParameter();
            postWork(res, parser.getComments());
            return res;
        }
    }

    /**
     * Parse a {@link ConstructorDeclaration}from the given reader.
     */
    public ConstructorDeclaration parseConstructorDeclaration(Reader in) throws IOException, ParserException {
        synchronized (parser) {
            parser.initialize(in, this);
            ConstructorDeclaration res = parser.ConstructorDeclaration();
            postWork(res, parser.getComments());
            return res;
        }
    }

    /**
     * Parse a {@link TypeReference}from the given reader.
     */
    public TypeReference parseTypeReference(Reader in) throws IOException, ParserException {
        synchronized (parser) {
            parser.initialize(in, this);
            TypeReference res = parser.ResultType();
            postWork(res, parser.getComments());
            return res;
        }
    }
    
    public PackageReference parsePackageReference(Reader in) throws IOException, ParserException {
        synchronized (parser) {
            parser.initialize(in, this);
            PackageReference res = parser.Name().toPackageReference();
            postWork(res, parser.getComments());
            return res;
        }
    }

    /**
     * Parse an {@link Expression}from the given reader.
     */
    public Expression parseExpression(Reader in) throws IOException, ParserException {
        synchronized (parser) {
            parser.initialize(in, this);
            Expression res = parser.Expression();
            postWork(res, parser.getComments());
            return res;
        }
    }

    /**
     * Parse some {@link Statement}s from the given reader.
     */
    public ASTList<Statement> parseStatements(Reader in) throws IOException, ParserException {
        synchronized (parser) {
            parser.initialize(in, this);
            ASTList<Statement> res = parser.GeneralizedStatements();
            for (int i = 0; i < res.size(); i += 1) {
                postWork(res.get(i), parser.getComments());
            }
            return res;
        }
    }

    /**
     * Parse a {@link StatementBlock}from the given string.
     */
    public StatementBlock parseStatementBlock(Reader in) throws IOException, ParserException {
        synchronized (parser) {
            parser.initialize(in, this);
            StatementBlock res = parser.Block();
            postWork(res, parser.getComments());
            return res;
        }
    }
    
    /**
     * Parse a {@link CompilationUnit}from the given string.
     */
    public CompilationUnit parseCompilationUnit(String in) throws ParserException {
        try {
            return parseCompilationUnit(new StringReader(in));
        } catch (IOException ioe) {
            throw new ParserException(("" + ioe));
        }
    }

    /**
     * Parse {@link CompilationUnit}s from the given string.
     */
    public List<CompilationUnit> parseCompilationUnits(String[] ins) throws ParserException {
        try {
        	List<CompilationUnit> cus = new ArrayList<CompilationUnit>();
            for (int i = 0; i < ins.length; i++) {
                CompilationUnit cu = parseCompilationUnit(new FileReader(ins[i]));
                cus.add(cu);
            }
            return cus;
        } catch (IOException ioe) {
            throw new ParserException(("" + ioe));
        }
    }

    /**
     * Parse a {@link TypeDeclaration}from the given string.
     */
    public TypeDeclaration parseTypeDeclaration(String in) throws ParserException {
        try {
            return parseTypeDeclaration(new StringReader(in));
        } catch (IOException ioe) {
            throw new ParserException(("" + ioe));
        }
    }

    /**
     * Parse a {@link MemberDeclaration}from the given string.
     */
    public MemberDeclaration parseMemberDeclaration(String in) throws ParserException {
        try {
            return parseMemberDeclaration(new StringReader(in));
        } catch (IOException ioe) {
            throw new ParserException(("" + ioe));
        }
    }

    /**
     * Parse a {@link FieldDeclaration}from the given string.
     */
    public FieldDeclaration parseFieldDeclaration(String in) throws ParserException {
        try {
            return parseFieldDeclaration(new StringReader(in));
        } catch (IOException ioe) {
            throw new ParserException(("" + ioe));
        }
    }

    /**
     * Parse a {@link MethodDeclaration}from the given string.
     */
    public MethodDeclaration parseMethodDeclaration(String in) throws ParserException {
        try {
            return parseMethodDeclaration(new StringReader(in));
        } catch (IOException ioe) {
            throw new ParserException(("" + ioe));
        }
    }

    /**
     * Parse a {@link ParameterDeclaration}from the given string.
     */
    public ParameterDeclaration parseParameterDeclaration(String in) throws ParserException {
        try {
            return parseParameterDeclaration(new StringReader(in));
        } catch (IOException ioe) {
            throw new ParserException(("" + ioe));
        }
    }

    /**
     * Parse a {@link ConstructorDeclaration}from the given string.
     */
    public ConstructorDeclaration parseConstructorDeclaration(String in) throws ParserException {
        try {
            return parseConstructorDeclaration(new StringReader(in));
        } catch (IOException ioe) {
            throw new ParserException(("" + ioe));
        }
    }

    /**
     * Parse a {@link TypeReference}from the given string.
     */
    public TypeReference parseTypeReference(String in) throws ParserException {
        try {
            return parseTypeReference(new StringReader(in));
        } catch (IOException ioe) {
            throw new ParserException(("" + ioe));
        }
    }

    /**
     * Parse a {@link TypeReference}from the given string.
     */
    public PackageReference parsePackageReference(String in) throws ParserException {
        try {
            return parsePackageReference(new StringReader(in));
        } catch (IOException ioe) {
            throw new ParserException(("" + ioe));
        }
    }

    /**
     * Parse an {@link Expression}from the given string.
     */
    public Expression parseExpression(String in) throws ParserException {
        try {
            return parseExpression(new StringReader(in));
        } catch (IOException ioe) {
            throw new ParserException(("" + ioe));
        }
    }

    /**
     * Parse a list of {@link Statement}s from the given string.
     */
    public ASTList<Statement> parseStatements(String in) throws ParserException {
        try {
            return parseStatements(new StringReader(in));
        } catch (IOException ioe) {
            throw new ParserException(("" + ioe));
        }
    }
    
    public StatementBlock parseStatementBlock(String in) throws ParserException {
        try {
            return parseStatementBlock(new StringReader(in));
        } catch (IOException ioe) {
            throw new ParserException(("" + ioe));
        }
    }



    /**
     * Replacement for Integer.parseInt allowing "supercharged" non-decimal
     * constants. In contrast to Integer.parseInt, works for 0x80000000 and
     * higher octal and hex constants as well as -MIN_VALUE which is allowed in
     * case that the minus sign has been interpreted as an unary minus. The
     * method will return Integer.MIN_VALUE in that case; this is fine as
     * -MIN_VALUE == MIN_VALUE.<br>
     * As of Recoder 0.96, also supports underscores in integer literals (where allowed). This is done always and not configurable.
     */
    public static int parseInt(String nm) throws NumberFormatException {
        int radix;
        boolean negative = false;
        int result;

        // remove underscores (Java7).
        nm = nm.replaceAll("_", "");
        
        int index = 0;
        if (nm.startsWith("-")) {
            negative = true;
            index++;
        }
        if (nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
            index += 2;
            radix = 16;
        } else if (nm.startsWith("0b", index) || nm.startsWith("0B", index)) {
        	index += 2;
        	radix = 2;
        } else if (nm.startsWith("0", index) && nm.length() > 1 + index) {
            index++;
            radix = 8;
        } else {
            radix = 10;
        }
        if (nm.startsWith("-", index))
            throw new NumberFormatException("Negative sign in wrong position");
        int len = nm.length() - index;
        if (radix == 16 && len == 8) {
            char first = nm.charAt(index);
            index++;
            result = Integer.valueOf(nm.substring(index), radix).intValue();
            result |= Character.digit(first, 16) << 28;
            return negative ? -result : result;
        } else if (radix == 8 && len == 11) {
            char first = nm.charAt(index);
            index++;
            result = Integer.valueOf(nm.substring(index), radix).intValue();
            result |= Character.digit(first, 8) << 30; // TODO check! (why is it << 30 here and << 63 for long?)
            return negative ? -result : result;
        } else if (radix == 2 && len == 32) {
        	char first = nm.charAt(index);
        	index++;
        	result = Integer.valueOf(nm.substring(index), radix).intValue();
        	result |= Character.digit(first, 2) << 31;
        	return negative ? -result : result;
        }
        if (!negative && radix == 10 && len == 10 && nm.indexOf("2147483648", index) == index) {
            return Integer.MIN_VALUE;
        }
        result = Integer.valueOf(nm.substring(index), radix).intValue();
        return negative ? -result : result;
    }

    /**
     * Replacement for Long.parseLong which is not available in JDK 1.1 and does
     * not handle 'l' or 'L' suffices in JDK 1.2.
     * As of Recoder 0.96, also supports underscores in integer literals (where allowed). This is done always and not configurable.
     */
    public static long parseLong(String nm) throws NumberFormatException {
    	// fixes a bug
    	if (nm.equalsIgnoreCase("0L"))
    		return 0;
    	
        int radix;
        boolean negative = false;
        long result;

        // remove underscores (Java7).
        nm = nm.replaceAll("_", "");
        
        int index = 0;
        if (nm.startsWith("-")) {
            negative = true;
            index++;
        }
        if (nm.startsWith("0x", index) || nm.startsWith("0X", index)) {
            index += 2;
            radix = 16;
        } else if (nm.startsWith("0b", index) || nm.startsWith("0B", index)) {
        	index += 2;
        	radix = 2;
        } else if (nm.startsWith("0", index) && nm.length() > 1 + index) {
            index++;
            radix = 8;
        } else {
            radix = 10;
        }

        if (nm.startsWith("-", index))
            throw new NumberFormatException("Negative sign in wrong position");
        int endIndex = nm.length();
        if (nm.endsWith("L") || nm.endsWith("l")) {
            endIndex -= 1;
        }

        int len = endIndex - index;
        if (radix == 16 && len == 16) {
            char first = nm.charAt(index);
            index += 1;
            result = Long.valueOf(nm.substring(index, endIndex), radix).longValue();
            result |= (long) Character.digit(first, 16) << 60;
            return negative ? -result : result;
        } else if (radix == 8 && len == 21) {
            char first = nm.charAt(index);
            index += 1;
            result = Long.valueOf(nm.substring(index, endIndex), radix).longValue();
            result |= Character.digit(first, 8) << 63;  // TODO check! (why is it << 63 here and << 30 for int?)
            return negative ? -result : result;
        } else if (radix == 2 && len == 64) {
        	char first = nm.charAt(index);
        	index++;
        	result = Long.valueOf(nm.substring(index), radix).longValue();
        	result |= Character.digit(first, 2) << 63;
        	return negative ? -result : result;
        }
        if (!negative && radix == 10 && len == 19 && nm.indexOf("9223372036854775808", index) == index) {
            return Long.MIN_VALUE;
        }
        result = Long.valueOf(nm.substring(index, endIndex), radix).longValue();
        return negative ? -result : result;
    }

    /**
     * Creates a syntactical representation of the source element.
     */
    String toSource(JavaSourceElement jse) {
        synchronized (writer) {
            sourcePrinter.setIndentationLevel(0);
            jse.accept(sourcePrinter);
            StringBuffer buf = writer.getBuffer();
            String str = buf.toString();
            buf.setLength(0);
            return str;
        }
    }

    /**
     * Returns a new suitable {@link recoder.java.PrettyPrinter}obeying the
     * current project settings for the specified writer,
     * 
     * @param out
     *            the (initial) writer to print to.
     * @return a new pretty printer.
     */
    public PrettyPrinter getPrettyPrinter(Writer out) {
        return new PrettyPrinter(out, serviceConfiguration.getProjectSettings().getProperties());
    }

    public void propertyChange(PropertyChangeEvent evt) {
        sourcePrinter = new PrettyPrinter(writer, serviceConfiguration.getProjectSettings().getProperties());
        String changedProp = evt.getPropertyName();
        if (changedProp.equals(PropertyNames.JDK1_4)) {
            parser.setAwareOfAssert(StringUtils.parseBooleanProperty(evt.getNewValue().toString()));
            // call automatically sets Java_5 to false if neccessary.
        } else if (changedProp.equals(PropertyNames.JAVA_5)) {
            parser.setJava5(StringUtils.parseBooleanProperty(evt.getNewValue().toString()));
            // call automatically sets awareOfAssert to true if neccessary.
        } else if (changedProp.equals(PropertyNames.JAVA_7)) {
        	parser.setJava7(StringUtils.parseBooleanProperty(evt.getNewValue().toString()));
        } else if (changedProp.equals(PropertyNames.ADD_NEWLINE_AT_END_OF_FILE)) {
        	useAddNewlineReader = StringUtils.parseBooleanProperty(evt.getNewValue().toString());
        } 
    }

    /**
     * Creates a new {@link Comment}.
     * 
     * @return a new instance of Comment.
     */
    public Comment createComment() {
        Comment res = new Comment();
        res.setFactory(this);
        return res;
    }

    /**
     * Creates a new {@link Comment}.
     * 
     * @return a new instance of Comment.
     */
    public Comment createComment(String text) {
        Comment res = new Comment(text);
        res.setFactory(this);
        return res;
    }

    /**
     * Creates a new {@link Comment}.
     * 
     * @return a new instance of Comment.
     */
    public Comment createComment(String text, boolean prefixed) {
        Comment res = new Comment(text, prefixed);
        res.setFactory(this);
        return res;
    }

    /**
     * Creates a new {@link CompilationUnit}.
     * 
     * @return a new instance of CompilationUnit.
     */
    public CompilationUnit createCompilationUnit() {
        CompilationUnit res = new CompilationUnit();
        res.setFactory(this);
        return res;
    }

    /**
     * Creates a new {@link CompilationUnit}.
     * 
     * @return a new instance of CompilationUnit.
     */
    public CompilationUnit createCompilationUnit(PackageSpecification pkg, ASTList<Import> imports,
    		ASTList<TypeDeclaration> typeDeclarations) {
        CompilationUnit res = new CompilationUnit(pkg, imports, typeDeclarations);
        res.setFactory(this);
        return res;
    }

    /**
     * Creates a new {@link DocComment}.
     * 
     * @return a new instance of DocComment.
     */
    public DocComment createDocComment() {
        DocComment res = new DocComment();
        res.setFactory(this);
        return res;
    }

    /**
     * Creates a new {@link DocComment}.
     * 
     * @return a new instance of DocComment.
     */
    public DocComment createDocComment(String text) {
        DocComment res = new DocComment(text);
        res.setFactory(this);
        return res;
    }

    /**
     * Creates a new {@link Identifier}.
     * 
     * @return a new instance of Identifier.
     */
    public Identifier createIdentifier() {
        Identifier res = new Identifier();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Identifier}.
     * 
     * @return a new instance of Identifier.
     */
    public Identifier createIdentifier(String text) {
        Identifier res = new Identifier(text);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Import}.
     * 
     * @return a new instance of Import.
     */
    public Import createImport() {
        Import res = new Import();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Import}.
     * 
     * @return a new instance of Import.
     */
    public Import createImport(TypeReference t, boolean multi) {
        Import res = new Import(t, multi);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Import}.
     * 
     * @return a new instance of Import.
     */
    public Import createImport(PackageReference t) {
        Import res = new Import(t);
        res.setFactory(this);
        trace(res);
        return res;
    }
    
    public Import createStaticImport(TypeReference t) {
        Import res = new Import(t, true, true);
        res.setFactory(this);
        trace(res);
        return res;
    }
    
    public Import createStaticImport(TypeReference t, Identifier id) {
        Import res = new Import(t, id);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link PackageSpecification}.
     * 
     * @return a new instance of PackageSpecification.
     */
    public PackageSpecification createPackageSpecification() {
        PackageSpecification res =  new PackageSpecification();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link PackageSpecification}.
     * 
     * @return a new instance of PackageSpecification.
     */
    public PackageSpecification createPackageSpecification(PackageReference pkg) {
        PackageSpecification res =  new PackageSpecification(pkg);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link SingleLineComment}.
     * 
     * @return a new instance of SingleLineComment.
     */
    public SingleLineComment createSingleLineComment() {
        SingleLineComment res = new SingleLineComment();
        res.setFactory(this);
        return res;
    }

    /**
     * Creates a new {@link SingleLineComment}.
     * 
     * @return a new instance of SingleLineComment.
     */
    public SingleLineComment createSingleLineComment(String text) {
        SingleLineComment res = new SingleLineComment(text);
        res.setFactory(this);
        return res;
    }

    /**
     * Creates a new {@link TypeReference}.
     * 
     * @return a new instance of TypeReference.
     */
    public TypeReference createTypeReference() {
        TypeReference res = new TypeReference();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link TypeReference}.
     * 
     * @return a new instance of TypeReference.
     */
    public TypeReference createTypeReference(Identifier name) {
        TypeReference res = new TypeReference(name);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link TypeReference}.
     * 
     * @return a new instance of TypeReference.
     */
    public TypeReference createTypeReference(ReferencePrefix prefix, Identifier name) {
        TypeReference res =  new TypeReference(prefix, name);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link TypeReference}.
     * 
     * @return a new instance of TypeReference.
     */
    public TypeReference createTypeReference(Identifier name, int dim) {
        TypeReference res = new TypeReference(name, dim);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link PackageReference}.
     * 
     * @return a new instance of PackageReference.
     */
    public PackageReference createPackageReference() {
        PackageReference res = new PackageReference();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link PackageReference}.
     * 
     * @return a new instance of PackageReference.
     */
    public PackageReference createPackageReference(Identifier id) {
        PackageReference res = new PackageReference(id);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link PackageReference}.
     * 
     * @return a new instance of PackageReference.
     */
    public PackageReference createPackageReference(PackageReference path, Identifier id) {
        PackageReference res = new PackageReference(path, id);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link UncollatedReferenceQualifier}.
     * 
     * @return a new instance of UncollatedReferenceQualifier.
     */
    public UncollatedReferenceQualifier createUncollatedReferenceQualifier() {
        UncollatedReferenceQualifier res = new UncollatedReferenceQualifier();
        res.setFactory(this);
        return res;
    }

    /**
     * Creates a new {@link UncollatedReferenceQualifier}.
     * 
     * @return a new instance of UncollatedReferenceQualifier.
     */
    public UncollatedReferenceQualifier createUncollatedReferenceQualifier(Identifier id) {
        UncollatedReferenceQualifier res = new UncollatedReferenceQualifier(id);
        res.setFactory(this);
        return res;
    }

    /**
     * Creates a new {@link UncollatedReferenceQualifier}.
     * 
     * @return a new instance of UncollatedReferenceQualifier.
     */
    public UncollatedReferenceQualifier createUncollatedReferenceQualifier(ReferencePrefix prefix, Identifier id) {
    	UncollatedReferenceQualifier res = new UncollatedReferenceQualifier(prefix, id);
        res.setFactory(this);
        return res;
    }

    /**
     * Creates a new {@link ClassDeclaration}.
     * 
     * @return a new instance of ClassDeclaration.
     */
    public ClassDeclaration createClassDeclaration() {
        ClassDeclaration res = new ClassDeclaration();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ClassDeclaration}.
     * 
     * @return a new instance of ClassDeclaration.
     */
    public ClassDeclaration createClassDeclaration(ASTList<DeclarationSpecifier> declSpecs, Identifier name, Extends extended,
            Implements implemented, ASTList<MemberDeclaration> members) {
        ClassDeclaration res = new ClassDeclaration(declSpecs, name, extended, implemented, members);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ClassDeclaration}.
     * 
     * @return a new instance of ClassDeclaration.
     */
    public ClassDeclaration createClassDeclaration(ASTList<MemberDeclaration> members) {
        ClassDeclaration res = new ClassDeclaration(members);
        res.setFactory(this);
        trace(res);
        return res;
    }
    
    public TypeArgumentDeclaration createTypeArgumentDeclaration(TypeReference ref) {
    	TypeArgumentDeclaration res = new TypeArgumentDeclaration(ref);
    	res.setFactory(this);
    	trace(res);
    	return res;
    }
    
    public TypeArgumentDeclaration createTypeArgumentDeclaration(TypeReference ref, WildcardMode wm) {
    	TypeArgumentDeclaration res = new TypeArgumentDeclaration(ref, wm);
    	res.setFactory(this);
    	trace(res);
    	return res;
    }
    
    public TypeArgumentDeclaration createTypeArgumentDeclaration() {
    	TypeArgumentDeclaration res = new TypeArgumentDeclaration();
    	res.setFactory(this);
    	trace(res);
    	return res;
    }
    
    public TypeParameterDeclaration createTypeParameterDeclaration() {
    	TypeParameterDeclaration res = new TypeParameterDeclaration();
    	res.setFactory(this);
    	trace(res);
    	return res;
    }
    
    /**
     * Creates a new {@link ClassInitializer}.
     * 
     * @return a new instance of ClassInitializer.
     */
    public ClassInitializer createClassInitializer() {
        ClassInitializer res = new ClassInitializer();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ClassInitializer}.
     * 
     * @return a new instance of ClassInitializer.
     */
    public ClassInitializer createClassInitializer(StatementBlock body) {
        ClassInitializer res = new ClassInitializer(body);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ClassInitializer}.
     * 
     * @return a new instance of ClassInitializer.
     */
    public ClassInitializer createClassInitializer(Static modifier, StatementBlock body) {
        ClassInitializer res =new ClassInitializer(modifier, body);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ConstructorDeclaration}.
     * 
     * @return a new instance of ConstructorDeclaration.
     */
    public ConstructorDeclaration createConstructorDeclaration() {
    	ConstructorDeclaration res = new ConstructorDeclaration();
    	res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ConstructorDeclaration}.
     * 
     * @return a new instance of ConstructorDeclaration.
     */
    public ConstructorDeclaration createConstructorDeclaration(VisibilityModifier modifier, Identifier name,
    		ASTList<ParameterDeclaration> parameters, Throws exceptions, StatementBlock body) {
        ConstructorDeclaration res = new ConstructorDeclaration(modifier, name, parameters, exceptions, body);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Throws}.
     * 
     * @return a new instance of Throws.
     */
    public Throws createThrows() {
       Throws res = new Throws();
       res.setFactory(this);
       trace(res);
       return res;
    }

    /**
     * Creates a new {@link Throws}.
     * 
     * @return a new instance of Throws.
     */
    public Throws createThrows(TypeReference exception) {
        Throws res = new Throws(exception);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Throws}.
     * 
     * @return a new instance of Throws.
     */
    public Throws createThrows(ASTList<TypeReference> list) {
        Throws res = new Throws(list);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link FieldDeclaration}.
     * 
     * @return a new instance of FieldDeclaration.
     */
    public FieldDeclaration createFieldDeclaration() {
        FieldDeclaration res = new FieldDeclaration();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link FieldDeclaration}.
     * 
     * @return a new instance of FieldDeclaration.
     */
    public FieldDeclaration createFieldDeclaration(TypeReference typeRef, Identifier name) {
        FieldDeclaration res = new FieldDeclaration(typeRef, createFieldSpecification(name));
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link FieldDeclaration}.
     * 
     * @return a new instance of FieldDeclaration.
     */
    public FieldDeclaration createFieldDeclaration(ASTList<DeclarationSpecifier> mods, TypeReference typeRef, Identifier name,
            Expression init) {
        FieldDeclaration res = new FieldDeclaration(mods, typeRef, createFieldSpecification(name, init));
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link FieldDeclaration}.
     * 
     * @return a new instance of FieldDeclaration.
     */
    public FieldDeclaration createFieldDeclaration(ASTList<DeclarationSpecifier> mods, TypeReference typeRef,
    		ASTList<FieldSpecification> vars) {
        FieldDeclaration res = new FieldDeclaration(mods, typeRef, vars);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Extends}.
     * 
     * @return a new instance of Extends.
     */
    public Extends createExtends() {
        Extends res = new Extends();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Extends}.
     * 
     * @return a new instance of Extends.
     */
    public Extends createExtends(TypeReference supertype) {
        Extends res = new Extends(supertype);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Extends}.
     * 
     * @return a new instance of Extends.
     */
    public Extends createExtends(ASTList<TypeReference> list) {
        Extends res = new Extends(list);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Implements}.
     * 
     * @return a new instance of Implements.
     */
    public Implements createImplements() {
        Implements res = new Implements();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Implements}.
     * 
     * @return a new instance of Implements.
     */
    public Implements createImplements(TypeReference supertype) {
        Implements res = new Implements(supertype);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Implements}.
     * 
     * @return a new instance of Implements.
     */
    public Implements createImplements(ASTList<TypeReference> list) {
        Implements res = new Implements(list);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link InterfaceDeclaration}.
     * 
     * @return a new instance of InterfaceDeclaration.
     */
    public InterfaceDeclaration createInterfaceDeclaration() {
        InterfaceDeclaration res = new InterfaceDeclaration();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link InterfaceDeclaration}.
     * 
     * @return a new instance of InterfaceDeclaration.
     */
    public InterfaceDeclaration createInterfaceDeclaration(ASTList<DeclarationSpecifier> modifiers, Identifier name,
            Extends extended, ASTList<MemberDeclaration> members) {
        InterfaceDeclaration res = new InterfaceDeclaration(modifiers, name, extended, members);
        res.setFactory(this);
        trace(res);
        return res;
    }
    
    public AnnotationDeclaration createAnnotationDeclaration() {
        AnnotationDeclaration res = new AnnotationDeclaration();
        res.setFactory(this);
        trace(res);
        return res;
    }
    
    public AnnotationDeclaration createAnnotationDeclaration(ASTList<DeclarationSpecifier> modifiers, Identifier name,
    		ASTList<MemberDeclaration> members) {
        AnnotationDeclaration res = new AnnotationDeclaration(modifiers, name, members);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LocalVariableDeclaration}.
     * 
     * @return a new instance of LocalVariableDeclaration.
     */
    public LocalVariableDeclaration createLocalVariableDeclaration() {
        LocalVariableDeclaration res = new LocalVariableDeclaration();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LocalVariableDeclaration}.
     * 
     * @return a new instance of LocalVariableDeclaration.
     */
    public LocalVariableDeclaration createLocalVariableDeclaration(TypeReference typeRef, Identifier name) {
    	VariableSpecification varSpec = createVariableSpecification(name);
    	LocalVariableDeclaration res = new LocalVariableDeclaration(
    			null, typeRef, varSpec);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LocalVariableDeclaration}.
     * 
     * @return a new instance of LocalVariableDeclaration.
     */
    public LocalVariableDeclaration createLocalVariableDeclaration(ASTList<DeclarationSpecifier> mods, TypeReference typeRef,
    		ASTList<VariableSpecification> vars) {
        LocalVariableDeclaration res = new LocalVariableDeclaration(mods, typeRef, vars);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LocalVariableDeclaration}.
     * 
     * @return a new instance of LocalVariableDeclaration.
     */
    public LocalVariableDeclaration createLocalVariableDeclaration(ASTList<DeclarationSpecifier> mods, TypeReference typeRef,
            Identifier name, Expression init) {
        LocalVariableDeclaration res = new LocalVariableDeclaration(mods, typeRef, 
        		createVariableSpecification(name, init));
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link MethodDeclaration}.
     * 
     * @return a new instance of MethodDeclaration.
     */
    public MethodDeclaration createMethodDeclaration() {
        MethodDeclaration res = new MethodDeclaration();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link MethodDeclaration}.
     * 
     * @return a new instance of MethodDeclaration.
     */
    public MethodDeclaration createMethodDeclaration(ASTList<DeclarationSpecifier> modifiers, TypeReference returnType,
            Identifier name, ASTList<ParameterDeclaration> parameters, Throws exceptions) {
        MethodDeclaration res = new MethodDeclaration(modifiers, returnType, name, parameters, exceptions);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link MethodDeclaration}.
     * 
     * @return a new instance of MethodDeclaration.
     */
    public MethodDeclaration createMethodDeclaration(ASTList<DeclarationSpecifier> modifiers, TypeReference returnType,
            Identifier name, ASTList<ParameterDeclaration> parameters, Throws exceptions, StatementBlock body) {
        MethodDeclaration res = new MethodDeclaration(modifiers, returnType, name, parameters, exceptions, body);
        res.setFactory(this);
        trace(res);
        return res;
    }
    
    public AnnotationPropertyDeclaration createAnnotationPropertyDeclaration(ASTList<DeclarationSpecifier> modifiers, TypeReference returnType,
            Identifier name, Expression defaultValue) {
        AnnotationPropertyDeclaration res = new AnnotationPropertyDeclaration(modifiers, returnType, name, defaultValue);
        res.setFactory(this);
        trace(res);
        return res;
    }
    
    /**
     * Creates a new {@link ParameterDeclaration}.
     * 
     * @return a new instance of ParameterDeclaration.
     */
    public ParameterDeclaration createParameterDeclaration() {
        ParameterDeclaration res = new ParameterDeclaration();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ParameterDeclaration}.
     * 
     * @return a new instance of ParameterDeclaration.
     */
    public ParameterDeclaration createParameterDeclaration(TypeReference typeRef, Identifier name) {
        ParameterDeclaration res = new ParameterDeclaration(typeRef, createVariableSpecification(name));
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ParameterDeclaration}.
     * 
     * @return a new instance of ParameterDeclaration.
     */
    public ParameterDeclaration createParameterDeclaration(ASTList<DeclarationSpecifier> mods, TypeReference typeRef,
            Identifier name) {
        ParameterDeclaration res = new ParameterDeclaration(typeRef, createVariableSpecification(name));
        res.setDeclarationSpecifiers(mods);
        res.setFactory(this);
        trace(res);
        return res;
    }
    
    public UnionTypeParameterDeclaration createUnionTypeParameterDeclaration() {
    	UnionTypeParameterDeclaration res = new UnionTypeParameterDeclaration();
    	res.setFactory(this);
    	trace(res);
    	return res;
    }

    /**
     * Creates a new {@link VariableSpecification}.
     * 
     * @return a new instance of VariableSpecification.
     */
    public VariableSpecification createVariableSpecification() {
        VariableSpecification res =  new VariableSpecification();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link VariableSpecification}.
     * 
     * @return a new instance of VariableSpecification.
     */
    public VariableSpecification createVariableSpecification(Identifier name) {
        VariableSpecification res = new VariableSpecification(name);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link VariableSpecification}.
     * 
     * @return a new instance of VariableSpecification.
     */
    public VariableSpecification createVariableSpecification(Identifier name, Expression init) {
        VariableSpecification res = new VariableSpecification(name, init);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link VariableSpecification}.
     * 
     * @return a new instance of VariableSpecification.
     */
    public VariableSpecification createVariableSpecification(Identifier name, int dimensions, Expression init) {
        VariableSpecification res =  new VariableSpecification(name, dimensions, init);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link FieldSpecification}.
     * 
     * @return a new instance of FieldSpecification.
     */
    public FieldSpecification createFieldSpecification() {
        FieldSpecification res = new FieldSpecification();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link FieldSpecification}.
     * 
     * @return a new instance of FieldSpecification.
     */
    public FieldSpecification createFieldSpecification(Identifier name) {
        FieldSpecification res =  new FieldSpecification(name);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link FieldSpecification}.
     * 
     * @return a new instance of FieldSpecification.
     */
    public FieldSpecification createFieldSpecification(Identifier name, Expression init) {
        FieldSpecification res =  new FieldSpecification(name, init);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link FieldSpecification}.
     * 
     * @return a new instance of FieldSpecification.
     */
    public FieldSpecification createFieldSpecification(Identifier name, int dimensions, Expression init) {
        FieldSpecification res = new FieldSpecification(name, dimensions, init);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ArrayInitializer}.
     * 
     * @return a new instance of ArrayInitializer.
     */
    public ArrayInitializer createArrayInitializer() {
        ArrayInitializer res =  new ArrayInitializer();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ArrayInitializer}.
     * 
     * @return a new instance of ArrayInitializer.
     */
    public ArrayInitializer createArrayInitializer(ASTList<Expression> args) {
        ArrayInitializer res =  new ArrayInitializer(args);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ParenthesizedExpression}.
     * 
     * @return a new instance of ParenthesizedExpression.
     */
    public ParenthesizedExpression createParenthesizedExpression() {
        ParenthesizedExpression res = new ParenthesizedExpression();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ParenthesizedExpression}.
     * 
     * @return a new instance of ParenthesizedExpression.
     */
    public ParenthesizedExpression createParenthesizedExpression(Expression child) {
    	ParenthesizedExpression res = new ParenthesizedExpression(child);
    	res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link BooleanLiteral}.
     * 
     * @return a new instance of BooleanLiteral.
     */
    public BooleanLiteral createBooleanLiteral() {
        BooleanLiteral res = new BooleanLiteral();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link BooleanLiteral}.
     * 
     * @return a new instance of BooleanLiteral.
     */
    public BooleanLiteral createBooleanLiteral(boolean value) {
        BooleanLiteral res = new BooleanLiteral(value);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link CharLiteral}.
     * 
     * @return a new instance of CharLiteral.
     */
    public CharLiteral createCharLiteral() {
        CharLiteral res = new CharLiteral();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link CharLiteral}.
     * 
     * @return a new instance of CharLiteral.
     */
    public CharLiteral createCharLiteral(char value) {
        CharLiteral res = new CharLiteral(value);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link CharLiteral}.
     * 
     * @return a new instance of CharLiteral.
     */
    public CharLiteral createCharLiteral(String value) {
        CharLiteral res = new CharLiteral(value);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link DoubleLiteral}.
     * 
     * @return a new instance of DoubleLiteral.
     */
    public DoubleLiteral createDoubleLiteral() {
        DoubleLiteral res = new DoubleLiteral();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link DoubleLiteral}.
     * 
     * @return a new instance of DoubleLiteral.
     */
    public DoubleLiteral createDoubleLiteral(double value) {
        DoubleLiteral res = new DoubleLiteral(value);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link DoubleLiteral}.
     * 
     * @return a new instance of DoubleLiteral.
     */
    public DoubleLiteral createDoubleLiteral(String value) {
        DoubleLiteral res = new DoubleLiteral(value);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link FloatLiteral}.
     * 
     * @return a new instance of FloatLiteral.
     */
    public FloatLiteral createFloatLiteral() {
        FloatLiteral res = new FloatLiteral();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link FloatLiteral}.
     * 
     * @return a new instance of FloatLiteral.
     */
    public FloatLiteral createFloatLiteral(float value) {
        FloatLiteral res = new FloatLiteral(value);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link FloatLiteral}.
     * 
     * @return a new instance of FloatLiteral.
     */
    public FloatLiteral createFloatLiteral(String value) {
        FloatLiteral res = new FloatLiteral(value);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link IntLiteral}.
     * 
     * @return a new instance of IntLiteral.
     */
    public IntLiteral createIntLiteral() {
        IntLiteral res = new IntLiteral();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link IntLiteral}.
     * 
     * @return a new instance of IntLiteral.
     */
    public IntLiteral createIntLiteral(int value) {
        IntLiteral res = new IntLiteral(value);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link IntLiteral}.
     * 
     * @return a new instance of IntLiteral.
     */
    public IntLiteral createIntLiteral(String value) {
        IntLiteral res = new IntLiteral(value);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LongLiteral}.
     * 
     * @return a new instance of LongLiteral.
     */
    public LongLiteral createLongLiteral() {
        LongLiteral res = new LongLiteral();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LongLiteral}.
     * 
     * @return a new instance of LongLiteral.
     */
    public LongLiteral createLongLiteral(long value) {
        LongLiteral res = new LongLiteral(value);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LongLiteral}.
     * 
     * @return a new instance of LongLiteral.
     */
    public LongLiteral createLongLiteral(String value) {
        LongLiteral res = new LongLiteral(value);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link NullLiteral}.
     * 
     * @return a new instance of NullLiteral.
     */
    public NullLiteral createNullLiteral() {
        NullLiteral res =  new NullLiteral();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link StringLiteral}.
     * 
     * @return a new instance of StringLiteral.
     */
    public StringLiteral createStringLiteral() {
        StringLiteral res = new StringLiteral();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link StringLiteral}.
     * 
     * @return a new instance of StringLiteral.
     */
    public StringLiteral createStringLiteral(String value) {
        StringLiteral res = new StringLiteral(value);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ArrayReference}.
     * 
     * @return a new instance of ArrayReference.
     */
    public ArrayReference createArrayReference() {
        ArrayReference res = new ArrayReference();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ArrayReference}.
     * 
     * @return a new instance of ArrayReference.
     */
    public ArrayReference createArrayReference(ReferencePrefix accessPath, ASTList<Expression> initializers) {
        ArrayReference res = new ArrayReference(accessPath, initializers);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link FieldReference}.
     * 
     * @return a new instance of FieldReference.
     */
    public FieldReference createFieldReference() {
        FieldReference res = new FieldReference();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link FieldReference}.
     * 
     * @return a new instance of FieldReference.
     */
    public FieldReference createFieldReference(Identifier id) {
        FieldReference res = new FieldReference(id);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link FieldReference}.
     * 
     * @return a new instance of FieldReference.
     */
    public FieldReference createFieldReference(ReferencePrefix prefix, Identifier id) {
        FieldReference res = new FieldReference(prefix, id);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link MetaClassReference}.
     * 
     * @return a new instance of MetaClassReference.
     */
    public MetaClassReference createMetaClassReference() {
        MetaClassReference res = new MetaClassReference();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link MetaClassReference}.
     * 
     * @return a new instance of MetaClassReference.
     */
    public MetaClassReference createMetaClassReference(TypeReference reference) {
        MetaClassReference res = new MetaClassReference(reference);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link MethodReference}.
     * 
     * @return a new instance of MethodReference.
     */
    public MethodReference createMethodReference() {
        MethodReference res = new MethodReference();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link MethodReference}.
     * 
     * @return a new instance of MethodReference.
     */
    public MethodReference createMethodReference(Identifier name) {
        MethodReference res = new MethodReference(name);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link MethodReference}.
     * 
     * @return a new instance of MethodReference.
     */
    public MethodReference createMethodReference(ReferencePrefix accessPath, Identifier name) {
        MethodReference res = new MethodReference(accessPath, name);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link MethodReference}.
     * 
     * @return a new instance of MethodReference.
     */
    public MethodReference createMethodReference(Identifier name, ASTList<Expression> args) {
        MethodReference res =new MethodReference(name, args);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link MethodReference}.
     * 
     * @return a new instance of MethodReference.
     */
    public MethodReference createMethodReference(ReferencePrefix accessPath, Identifier name, ASTList<Expression> args) {
        MethodReference res = new MethodReference(accessPath, name, args);
        res.setFactory(this);
        trace(res);
        return res;
    }
    
    public MethodReference createMethodReference(ReferencePrefix accessPath, Identifier name, ASTList<Expression> args,
    											 ASTList<TypeArgumentDeclaration> typeArgs) {
        MethodReference res = new MethodReference(accessPath, name, args, typeArgs);
        res.setFactory(this);
        trace(res);
        return res;
    }
    
    public AnnotationPropertyReference createAnnotationPropertyReference(String id) {
    	Identifier ident = createIdentifier(id);
    	ident.setFactory(this);
        AnnotationPropertyReference res = new AnnotationPropertyReference(ident);
    	res.setFactory(this);
        trace(res);
        return res;
    }

    public AnnotationPropertyReference createAnnotationPropertyReference(Identifier id) {
    	AnnotationPropertyReference res = new AnnotationPropertyReference(id);
    	res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link SuperConstructorReference}.
     * 
     * @return a new instance of SuperConstructorReference.
     */
    public SuperConstructorReference createSuperConstructorReference() {
        SuperConstructorReference res = new SuperConstructorReference();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link SuperConstructorReference}.
     * 
     * @return a new instance of SuperConstructorReference.
     */
    public SuperConstructorReference createSuperConstructorReference(ASTList<Expression> arguments) {
        SuperConstructorReference res = new SuperConstructorReference(arguments);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link SuperConstructorReference}.
     * 
     * @return a new instance of SuperConstructorReference.
     */
    public SuperConstructorReference createSuperConstructorReference(ReferencePrefix path,
    		ASTList<Expression> arguments) {
        SuperConstructorReference res = new SuperConstructorReference(path, arguments);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link SuperReference}.
     * 
     * @return a new instance of SuperReference.
     */
    public SuperReference createSuperReference() {
        SuperReference res = new SuperReference();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link SuperReference}.
     * 
     * @return a new instance of SuperReference.
     */
    public SuperReference createSuperReference(ReferencePrefix accessPath) {
        SuperReference res = new SuperReference(accessPath);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ThisConstructorReference}.
     * 
     * @return a new instance of ThisConstructorReference.
     */
    public ThisConstructorReference createThisConstructorReference() {
        ThisConstructorReference res = new ThisConstructorReference();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ThisConstructorReference}.
     * 
     * @return a new instance of ThisConstructorReference.
     */
    public ThisConstructorReference createThisConstructorReference(ASTList<Expression> arguments) {
        ThisConstructorReference res = new ThisConstructorReference(arguments);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ThisReference}.
     * 
     * @return a new instance of ThisReference.
     */
    public ThisReference createThisReference() {
        ThisReference res = new ThisReference();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ThisReference}.
     * 
     * @return a new instance of ThisReference.
     */
    public ThisReference createThisReference(TypeReference outer) {
        ThisReference res = new ThisReference(outer);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link VariableReference}.
     * 
     * @return a new instance of VariableReference.
     */
    public VariableReference createVariableReference() {
        VariableReference res = new VariableReference();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link VariableReference}.
     * 
     * @return a new instance of VariableReference.
     */
    public VariableReference createVariableReference(Identifier id) {
        VariableReference res = new VariableReference(id);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link BinaryAnd}.
     * 
     * @return a new instance of BinaryAnd.
     */
    public BinaryAnd createBinaryAnd() {
        BinaryAnd res = new BinaryAnd();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link BinaryAnd}.
     * 
     * @return a new instance of BinaryAnd.
     */
    public BinaryAnd createBinaryAnd(Expression lhs, Expression rhs) {
        BinaryAnd res = new BinaryAnd(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link BinaryAndAssignment}.
     * 
     * @return a new instance of BinaryAndAssignment.
     */
    public BinaryAndAssignment createBinaryAndAssignment() {
        BinaryAndAssignment res = new BinaryAndAssignment();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link BinaryAndAssignment}.
     * 
     * @return a new instance of BinaryAndAssignment.
     */
    public BinaryAndAssignment createBinaryAndAssignment(Expression lhs, Expression rhs) {
        BinaryAndAssignment res = new BinaryAndAssignment(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link BinaryNot}.
     * 
     * @return a new instance of BinaryNot.
     */
    public BinaryNot createBinaryNot() {
        BinaryNot res = new BinaryNot();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link BinaryNot}.
     * 
     * @return a new instance of BinaryNot.
     */
    public BinaryNot createBinaryNot(Expression child) {
        BinaryNot res = new BinaryNot(child);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link BinaryOr}.
     * 
     * @return a new instance of BinaryOr.
     */
    public BinaryOr createBinaryOr() {
        BinaryOr res = new BinaryOr();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link BinaryOr}.
     * 
     * @return a new instance of BinaryOr.
     */
    public BinaryOr createBinaryOr(Expression lhs, Expression rhs) {
        BinaryOr res = new BinaryOr(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link BinaryOrAssignment}.
     * 
     * @return a new instance of BinaryOrAssignment.
     */
    public BinaryOrAssignment createBinaryOrAssignment() {
        BinaryOrAssignment res = new BinaryOrAssignment();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link BinaryOrAssignment}.
     * 
     * @return a new instance of BinaryOrAssignment.
     */
    public BinaryOrAssignment createBinaryOrAssignment(Expression lhs, Expression rhs) {
        BinaryOrAssignment res = new BinaryOrAssignment(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link BinaryXOr}.
     * 
     * @return a new instance of BinaryXOr.
     */
    public BinaryXOr createBinaryXOr() {
        BinaryXOr res = new BinaryXOr();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link BinaryXOr}.
     * 
     * @return a new instance of BinaryXOr.
     */
    public BinaryXOr createBinaryXOr(Expression lhs, Expression rhs) {
        BinaryXOr res = new BinaryXOr(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link BinaryXOrAssignment}.
     * 
     * @return a new instance of BinaryXOrAssignment.
     */
    public BinaryXOrAssignment createBinaryXOrAssignment() {
        BinaryXOrAssignment res = new BinaryXOrAssignment();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link BinaryXOrAssignment}.
     * 
     * @return a new instance of BinaryXOrAssignment.
     */
    public BinaryXOrAssignment createBinaryXOrAssignment(Expression lhs, Expression rhs) {
        BinaryXOrAssignment res = new BinaryXOrAssignment(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Conditional}.
     * 
     * @return a new instance of Conditional.
     */
    public Conditional createConditional() {
        Conditional res = new Conditional();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Conditional}.
     * 
     * @return a new instance of Conditional.
     */
    public Conditional createConditional(Expression guard, Expression thenExpr, Expression elseExpr) {
        Conditional res = new Conditional(guard, thenExpr, elseExpr);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link CopyAssignment}.
     * 
     * @return a new instance of CopyAssignment.
     */
    public CopyAssignment createCopyAssignment() {
        CopyAssignment res = new CopyAssignment();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link CopyAssignment}.
     * 
     * @return a new instance of CopyAssignment.
     */
    public CopyAssignment createCopyAssignment(Expression lhs, Expression rhs) {
        CopyAssignment res = new CopyAssignment(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Divide}.
     * 
     * @return a new instance of Divide.
     */
    public Divide createDivide() {
        Divide res = new Divide();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Divide}.
     * 
     * @return a new instance of Divide.
     */
    public Divide createDivide(Expression lhs, Expression rhs) {
        Divide res = new Divide(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link DivideAssignment}.
     * 
     * @return a new instance of DivideAssignment.
     */
    public DivideAssignment createDivideAssignment() {
        DivideAssignment res = new DivideAssignment();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link DivideAssignment}.
     * 
     * @return a new instance of DivideAssignment.
     */
    public DivideAssignment createDivideAssignment(Expression lhs, Expression rhs) {
        DivideAssignment res = new DivideAssignment(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Equals}.
     * 
     * @return a new instance of Equals.
     */
    public Equals createEquals() {
        Equals res = new Equals();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Equals}.
     * 
     * @return a new instance of Equals.
     */
    public Equals createEquals(Expression lhs, Expression rhs) {
        Equals res = new Equals(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link GreaterOrEquals}.
     * 
     * @return a new instance of GreaterOrEquals.
     */
    public GreaterOrEquals createGreaterOrEquals() {
        GreaterOrEquals res = new GreaterOrEquals();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link GreaterOrEquals}.
     * 
     * @return a new instance of GreaterOrEquals.
     */
    public GreaterOrEquals createGreaterOrEquals(Expression lhs, Expression rhs) {
        GreaterOrEquals res = new GreaterOrEquals(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link GreaterThan}.
     * 
     * @return a new instance of GreaterThan.
     */
    public GreaterThan createGreaterThan() {
        GreaterThan res = new GreaterThan();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link GreaterThan}.
     * 
     * @return a new instance of GreaterThan.
     */
    public GreaterThan createGreaterThan(Expression lhs, Expression rhs) {
        GreaterThan res = new GreaterThan(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Instanceof}.
     * 
     * @return a new instance of Instanceof.
     */
    public Instanceof createInstanceof() {
        Instanceof res = new Instanceof();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Instanceof}.
     * 
     * @return a new instance of Instanceof.
     */
    public Instanceof createInstanceof(Expression child, TypeReference typeref) {
        Instanceof res = new Instanceof(child, typeref);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LessOrEquals}.
     * 
     * @return a new instance of LessOrEquals.
     */
    public LessOrEquals createLessOrEquals() {
        LessOrEquals res = new LessOrEquals();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LessOrEquals}.
     * 
     * @return a new instance of LessOrEquals.
     */
    public LessOrEquals createLessOrEquals(Expression lhs, Expression rhs) {
        LessOrEquals res = new LessOrEquals(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LessThan}.
     * 
     * @return a new instance of LessThan.
     */
    public LessThan createLessThan() {
        LessThan res = new LessThan();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LessThan}.
     * 
     * @return a new instance of LessThan.
     */
    public LessThan createLessThan(Expression lhs, Expression rhs) {
        LessThan res = new LessThan(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LogicalAnd}.
     * 
     * @return a new instance of LogicalAnd.
     */
    public LogicalAnd createLogicalAnd() {
        LogicalAnd res = new LogicalAnd();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LogicalAnd}.
     * 
     * @return a new instance of LogicalAnd.
     */
    public LogicalAnd createLogicalAnd(Expression lhs, Expression rhs) {
        LogicalAnd res = new LogicalAnd(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LogicalNot}.
     * 
     * @return a new instance of LogicalNot.
     */
    public LogicalNot createLogicalNot() {
        LogicalNot res = new LogicalNot();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LogicalNot}.
     * 
     * @return a new instance of LogicalNot.
     */
    public LogicalNot createLogicalNot(Expression child) {
        LogicalNot res = new LogicalNot(child);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LogicalOr}.
     * 
     * @return a new instance of LogicalOr.
     */
    public LogicalOr createLogicalOr() {
        LogicalOr res = new LogicalOr();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LogicalOr}.
     * 
     * @return a new instance of LogicalOr.
     */
    public LogicalOr createLogicalOr(Expression lhs, Expression rhs) {
        LogicalOr res = new LogicalOr(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Minus}.
     * 
     * @return a new instance of Minus.
     */
    public Minus createMinus() {
        Minus res = new Minus();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Minus}.
     * 
     * @return a new instance of Minus.
     */
    public Minus createMinus(Expression lhs, Expression rhs) {
        Minus res = new Minus(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link MinusAssignment}.
     * 
     * @return a new instance of MinusAssignment.
     */
    public MinusAssignment createMinusAssignment() {
        MinusAssignment res = new MinusAssignment();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link MinusAssignment}.
     * 
     * @return a new instance of MinusAssignment.
     */
    public MinusAssignment createMinusAssignment(Expression lhs, Expression rhs) {
        MinusAssignment res = new MinusAssignment(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Modulo}.
     * 
     * @return a new instance of Modulo.
     */
    public Modulo createModulo() {
        Modulo res = new Modulo();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Modulo}.
     * 
     * @return a new instance of Modulo.
     */
    public Modulo createModulo(Expression lhs, Expression rhs) {
        Modulo res =new Modulo(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ModuloAssignment}.
     * 
     * @return a new instance of ModuloAssignment.
     */
    public ModuloAssignment createModuloAssignment() {
        ModuloAssignment res = new ModuloAssignment();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ModuloAssignment}.
     * 
     * @return a new instance of ModuloAssignment.
     */
    public ModuloAssignment createModuloAssignment(Expression lhs, Expression rhs) {
        ModuloAssignment res = new ModuloAssignment(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Negative}.
     * 
     * @return a new instance of Negative.
     */
    public Negative createNegative() {
        Negative res = new Negative();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Negative}.
     * 
     * @return a new instance of Negative.
     */
    public Negative createNegative(Expression child) {
        Negative res = new Negative(child);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link New}.
     * 
     * @return a new instance of New.
     */
    public New createNew() {
        New res = new New();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link New}.
     * 
     * @return a new instance of New.
     */
    public New createNew(ReferencePrefix accessPath, TypeReference constructorName, ASTList<Expression> arguments) {
        New res = new New(accessPath, constructorName, arguments);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link New}.
     * 
     * @return a new instance of New.
     */
    public New createNew(ReferencePrefix accessPath, TypeReference constructorName, ASTList<Expression> arguments,
            ClassDeclaration anonymousClass) {
        New res = new New(accessPath, constructorName, arguments, anonymousClass);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link NewArray}.
     * 
     * @return a new instance of NewArray.
     */
    public NewArray createNewArray() {
        NewArray res = new NewArray();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link NewArray}.
     * 
     * @return a new instance of NewArray.
     */
    public NewArray createNewArray(TypeReference arrayName, ASTList<Expression> dimExpr) {
        NewArray res = new NewArray(arrayName, dimExpr);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link NewArray}.
     * 
     * @return a new instance of NewArray.
     */
    public NewArray createNewArray(TypeReference arrayName, int dimensions, ArrayInitializer initializer) {
        NewArray res = new NewArray(arrayName, dimensions, initializer);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link NotEquals}.
     * 
     * @return a new instance of NotEquals.
     */
    public NotEquals createNotEquals() {
        NotEquals res = new NotEquals();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link NotEquals}.
     * 
     * @return a new instance of NotEquals.
     */
    public NotEquals createNotEquals(Expression lhs, Expression rhs) {
        NotEquals res = new NotEquals(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Plus}.
     * 
     * @return a new instance of Plus.
     */
    public Plus createPlus() {
        Plus res = new Plus();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Plus}.
     * 
     * @return a new instance of Plus.
     */
    public Plus createPlus(Expression lhs, Expression rhs) {
        Plus res =  new Plus(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link PlusAssignment}.
     * 
     * @return a new instance of PlusAssignment.
     */
    public PlusAssignment createPlusAssignment() {
        PlusAssignment res = new PlusAssignment();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link PlusAssignment}.
     * 
     * @return a new instance of PlusAssignment.
     */
    public PlusAssignment createPlusAssignment(Expression lhs, Expression rhs) {
        PlusAssignment res = new PlusAssignment(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Positive}.
     * 
     * @return a new instance of Positive.
     */
    public Positive createPositive() {
        Positive res = new Positive();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Positive}.
     * 
     * @return a new instance of Positive.
     */
    public Positive createPositive(Expression child) {
        Positive res = new Positive(child);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link PostDecrement}.
     * 
     * @return a new instance of PostDecrement.
     */
    public PostDecrement createPostDecrement() {
        PostDecrement res = new PostDecrement();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link PostDecrement}.
     * 
     * @return a new instance of PostDecrement.
     */
    public PostDecrement createPostDecrement(Expression child) {
        PostDecrement res = new PostDecrement(child);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link PostIncrement}.
     * 
     * @return a new instance of PostIncrement.
     */
    public PostIncrement createPostIncrement() {
        PostIncrement res = new PostIncrement();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link PostIncrement}.
     * 
     * @return a new instance of PostIncrement.
     */
    public PostIncrement createPostIncrement(Expression child) {
        PostIncrement res = new PostIncrement(child);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link PreDecrement}.
     * 
     * @return a new instance of PreDecrement.
     */
    public PreDecrement createPreDecrement() {
        PreDecrement res = new PreDecrement();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link PreDecrement}.
     * 
     * @return a new instance of PreDecrement.
     */
    public PreDecrement createPreDecrement(Expression child) {
        PreDecrement res = new PreDecrement(child);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link PreIncrement}.
     * 
     * @return a new instance of PreIncrement.
     */
    public PreIncrement createPreIncrement() {
        PreIncrement res = new PreIncrement();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link PreIncrement}.
     * 
     * @return a new instance of PreIncrement.
     */
    public PreIncrement createPreIncrement(Expression child) {
        PreIncrement res = new PreIncrement(child);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ShiftLeft}.
     * 
     * @return a new instance of ShiftLeft.
     */
    public ShiftLeft createShiftLeft() {
        ShiftLeft res =  new ShiftLeft();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ShiftLeft}.
     * 
     * @return a new instance of ShiftLeft.
     */
    public ShiftLeft createShiftLeft(Expression lhs, Expression rhs) {
        ShiftLeft res = new ShiftLeft(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ShiftLeftAssignment}.
     * 
     * @return a new instance of ShiftLeftAssignment.
     */
    public ShiftLeftAssignment createShiftLeftAssignment() {
        ShiftLeftAssignment res = new ShiftLeftAssignment();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ShiftLeftAssignment}.
     * 
     * @return a new instance of ShiftLeftAssignment.
     */
    public ShiftLeftAssignment createShiftLeftAssignment(Expression lhs, Expression rhs) {
        ShiftLeftAssignment res = new ShiftLeftAssignment(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ShiftRight}.
     * 
     * @return a new instance of ShiftRight.
     */
    public ShiftRight createShiftRight() {
        ShiftRight res = new ShiftRight();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ShiftRight}.
     * 
     * @return a new instance of ShiftRight.
     */
    public ShiftRight createShiftRight(Expression lhs, Expression rhs) {
        ShiftRight res = new ShiftRight(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ShiftRightAssignment}.
     * 
     * @return a new instance of ShiftRightAssignment.
     */
    public ShiftRightAssignment createShiftRightAssignment() {
        ShiftRightAssignment res =  new ShiftRightAssignment();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link ShiftRightAssignment}.
     * 
     * @return a new instance of ShiftRightAssignment.
     */
    public ShiftRightAssignment createShiftRightAssignment(Expression lhs, Expression rhs) {
        ShiftRightAssignment res =  new ShiftRightAssignment(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Times}.
     * 
     * @return a new instance of Times.
     */
    public Times createTimes() {
        Times res =  new Times();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Times}.
     * 
     * @return a new instance of Times.
     */
    public Times createTimes(Expression lhs, Expression rhs) {
        Times res =  new Times(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link TimesAssignment}.
     * 
     * @return a new instance of TimesAssignment.
     */
    public TimesAssignment createTimesAssignment() {
        TimesAssignment res = new TimesAssignment();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link TimesAssignment}.
     * 
     * @return a new instance of TimesAssignment.
     */
    public TimesAssignment createTimesAssignment(Expression lhs, Expression rhs) {
        TimesAssignment res = new TimesAssignment(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link TypeCast}.
     * 
     * @return a new instance of TypeCast.
     */
    public TypeCast createTypeCast() {
        TypeCast res = new TypeCast();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link TypeCast}.
     * 
     * @return a new instance of TypeCast.
     */
    public TypeCast createTypeCast(Expression child, TypeReference typeref) {
        TypeCast res = new TypeCast(child, typeref);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link UnsignedShiftRight}.
     * 
     * @return a new instance of UnsignedShiftRight.
     */
    public UnsignedShiftRight createUnsignedShiftRight() {
        UnsignedShiftRight res = new UnsignedShiftRight();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link UnsignedShiftRight}.
     * 
     * @return a new instance of UnsignedShiftRight.
     */
    public UnsignedShiftRight createUnsignedShiftRight(Expression lhs, Expression rhs) {
        UnsignedShiftRight res = new UnsignedShiftRight(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link UnsignedShiftRightAssignment}.
     * 
     * @return a new instance of UnsignedShiftRightAssignment.
     */
    public UnsignedShiftRightAssignment createUnsignedShiftRightAssignment() {
        UnsignedShiftRightAssignment res = new UnsignedShiftRightAssignment();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link UnsignedShiftRightAssignment}.
     * 
     * @return a new instance of UnsignedShiftRightAssignment.
     */
    public UnsignedShiftRightAssignment createUnsignedShiftRightAssignment(Expression lhs, Expression rhs) {
        UnsignedShiftRightAssignment res = new UnsignedShiftRightAssignment(lhs, rhs);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Abstract}.
     * 
     * @return a new instance of Abstract.
     */
    public Abstract createAbstract() {
        Abstract res = new Abstract();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Final}.
     * 
     * @return a new instance of Final.
     */
    public Final createFinal() {
        Final res = new Final();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Native}.
     * 
     * @return a new instance of Native.
     */
    public Native createNative() {
        Native res = new Native();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Private}.
     * 
     * @return a new instance of Private.
     */
    public Private createPrivate() {
        Private res = new Private();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Protected}.
     * 
     * @return a new instance of Protected.
     */
    public Protected createProtected() {
        Protected res = new Protected();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Public}.
     * 
     * @return a new instance of Public.
     */
    public Public createPublic() {
        Public res = new Public();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Static}.
     * 
     * @return a new instance of Static.
     */
    public Static createStatic() {
        Static res = new Static();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Synchronized}.
     * 
     * @return a new instance of Synchronized.
     */
    public Synchronized createSynchronized() {
        Synchronized res = new Synchronized();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Transient}.
     * 
     * @return a new instance of Transient.
     */
    public Transient createTransient() {
        Transient res = new Transient();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link StrictFp}.
     * 
     * @return a new instance of StrictFp.
     */
    public StrictFp createStrictFp() {
        StrictFp res = new StrictFp();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Volatile}.
     * 
     * @return a new instance of Volatile.
     */
    public Volatile createVolatile() {
        Volatile res = new Volatile();
        res.setFactory(this);
        trace(res);
        return res;
    }
    
    public AnnotationUseSpecification createAnnotationUseSpecification() {
        AnnotationUseSpecification res = new AnnotationUseSpecification();
        res.setFactory(this);
        trace(res);
        return res;
    }
    
    public AnnotationElementValuePair createAnnotationElementValuePair(AnnotationPropertyReference ref, Expression e) {
    	AnnotationElementValuePair res = new AnnotationElementValuePair(ref, e);
    	res.setFactory(this);
    	trace(res);
    	return res;
    }
    
    public ElementValueArrayInitializer createElementValueArrayInitializer() {
    	ElementValueArrayInitializer res = new ElementValueArrayInitializer();
    	res.setFactory(this);
    	trace(res);
    	return res;
    }

    /**
     * Creates a new {@link Break}.
     * 
     * @return a new instance of Break.
     */
    public Break createBreak() {
        Break res = new Break();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Break}.
     * 
     * @return a new instance of Break.
     */
    public Break createBreak(Identifier label) {
        Break res = new Break(label);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Case}.
     * 
     * @return a new instance of Case.
     */
    public Case createCase() {
        Case res = new Case();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Case}.
     * 
     * @return a new instance of Case.
     */
    public Case createCase(Expression e) {
        Case res = new Case(e);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Case}.
     * 
     * @return a new instance of Case.
     */
    public Case createCase(Expression e, ASTList<Statement> body) {
        Case res = new Case(e, body);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Catch}.
     * 
     * @return a new instance of Catch.
     */
    public Catch createCatch() {
        Catch res = new Catch();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Catch}.
     * 
     * @return a new instance of Catch.
     */
    public Catch createCatch(ParameterDeclaration e, StatementBlock body) {
        Catch res = new Catch(e, body);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Continue}.
     * 
     * @return a new instance of Continue.
     */
    public Continue createContinue() {
        Continue res =  new Continue();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Continue}.
     * 
     * @return a new instance of Continue.
     */
    public Continue createContinue(Identifier label) {
        Continue res = new Continue(label);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Default}.
     * 
     * @return a new instance of Default.
     */
    public Default createDefault() {
        Default res = new Default();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Default}.
     * 
     * @return a new instance of Default.
     */
    public Default createDefault(ASTList<Statement> body) {
        Default res = new Default(body);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Do}.
     * 
     * @return a new instance of Do.
     */
    public Do createDo() {
        Do res = new Do();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Do}.
     * 
     * @return a new instance of Do.
     */
    public Do createDo(Expression guard) {
        Do res = new Do(guard);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Do}.
     * 
     * @return a new instance of Do.
     */
    public Do createDo(Expression guard, Statement body) {
        Do res = new Do(guard, body);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Else}.
     * 
     * @return a new instance of Else.
     */
    public Else createElse() {
        Else res =  new Else();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Else}.
     * 
     * @return a new instance of Else.
     */
    public Else createElse(Statement body) {
        Else res =  new Else(body);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link EmptyStatement}.
     * 
     * @return a new instance of EmptyStatement.
     */
    public EmptyStatement createEmptyStatement() {
        EmptyStatement res = new EmptyStatement();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Finally}.
     * 
     * @return a new instance of Finally.
     */
    public Finally createFinally() {
        Finally res =  new Finally();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Finally}.
     * 
     * @return a new instance of Finally.
     */
    public Finally createFinally(StatementBlock body) {
        Finally res = new Finally(body);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link For}.
     * 
     * @return a new instance of For.
     */
    public For createFor() {
        For res = new For();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link For}.
     * 
     * @return a new instance of For.
     */
    public For createFor(ASTList<LoopInitializer> inits, Expression guard, ASTList<Expression> updates,
            Statement body) {
        For res = new For(inits, guard, updates, body);
        res.setFactory(this);
        trace(res);
        return res;
    }
    
    public EnhancedFor createEnhancedFor() {
        EnhancedFor res = new EnhancedFor();
        res.setFactory(this);
        trace(res);
        return res;
    }
    
    public EnumDeclaration createEnumDeclaration() {
    	EnumDeclaration res = new EnumDeclaration();
    	res.setFactory(this);
    	trace(res);
    	return res;
    }
    
    public EnumConstructorReference createEnumConstructorReference() {
    	EnumConstructorReference res = new EnumConstructorReference();
    	res.setFactory(this);
    	trace(res);
    	return res;
    }
    
    public EnumConstructorReference createEnumConstructorReference(ASTList<Expression> args, ClassDeclaration cd) {
    	EnumConstructorReference res = new EnumConstructorReference(args, cd);
    	res.setFactory(this);
    	trace(res);
    	return res;
    }
    
    public EnumConstantSpecification createEnumConstantSpecification(Identifier id, EnumConstructorReference ref) {
    	EnumConstantSpecification res = new EnumConstantSpecification(id, ref);
    	res.setFactory(this);
    	trace(res);
    	return res;
    }
    
    public EnumConstantDeclaration createEnumConstantDeclaration() {
    	EnumConstantDeclaration res = new EnumConstantDeclaration();
    	res.setFactory(this);
    	trace(res);
    	return res;
    }

    /**
     * Creates a new {@link Assert}.
     * 
     * @return a new instance of Assert.
     */
    public Assert createAssert() {
        Assert res = new Assert();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Assert}.
     * 
     * @return a new instance of Assert.
     */
    public Assert createAssert(Expression cond) {
        Assert res = new Assert(cond);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Assert}.
     * 
     * @return a new instance of Assert.
     */
    public Assert createAssert(Expression cond, Expression msg) {
        Assert res = new Assert(cond, msg);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link If}.
     * 
     * @return a new instance of If.
     */
    public If createIf() {
        If res = new If();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link If}.
     * 
     * @return a new instance of If.
     */
    public If createIf(Expression e, Statement thenStatement) {
        If res = new If(e, createThen(thenStatement));
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link If}.
     * 
     * @return a new instance of If.
     */
    public If createIf(Expression e, Then thenBranch) {
        If res = new If(e, thenBranch);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link If}.
     * 
     * @return a new instance of If.
     */
    public If createIf(Expression e, Then thenBranch, Else elseBranch) {
        If res = new If(e, thenBranch, elseBranch);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link If}.
     * 
     * @return a new instance of If.
     */
    public If createIf(Expression e, Statement thenStatement, Statement elseStatement) {
        If res = new If(e, createThen(thenStatement), createElse(elseStatement));
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LabeledStatement}.
     * 
     * @return a new instance of LabeledStatement.
     */
    public LabeledStatement createLabeledStatement() {
        LabeledStatement res = new LabeledStatement();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LabeledStatement} with an {@link EmptyStatement} as body.
     * 
     * @return a new instance of LabeledStatement.
     */
    public LabeledStatement createLabeledStatement(Identifier id) {
        LabeledStatement res = new LabeledStatement(id, createEmptyStatement());
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link LabeledStatement}.
     * 
     * @return a new instance of LabeledStatement.
     */
    public LabeledStatement createLabeledStatement(Identifier id, Statement statement) {
        LabeledStatement res = new LabeledStatement(id, statement);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Return}.
     * 
     * @return a new instance of Return.
     */
    public Return createReturn() {
        Return res = new Return();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Return}.
     * 
     * @return a new instance of Return.
     */
    public Return createReturn(Expression expr) {
        Return res = new Return(expr);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link StatementBlock}.
     * 
     * @return a new instance of StatementBlock.
     */
    public StatementBlock createStatementBlock() {
        StatementBlock res = new StatementBlock();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link StatementBlock}.
     * 
     * @return a new instance of StatementBlock.
     */
    public StatementBlock createStatementBlock(ASTList<Statement> block) {
        StatementBlock res = new StatementBlock(block);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Switch}.
     * 
     * @return a new instance of Switch.
     */
    public Switch createSwitch() {
        Switch res = new Switch();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Switch}.
     * 
     * @return a new instance of Switch.
     */
    public Switch createSwitch(Expression e) {
        Switch res = new Switch(e);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Switch}.
     * 
     * @return a new instance of Switch.
     */
    public Switch createSwitch(Expression e, ASTList<Branch> branches) {
        Switch res = new Switch(e, branches);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link SynchronizedBlock}.
     * 
     * @return a new instance of SynchronizedBlock.
     */
    public SynchronizedBlock createSynchronizedBlock() {
        SynchronizedBlock res = new SynchronizedBlock();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link SynchronizedBlock}.
     * 
     * @return a new instance of SynchronizedBlock.
     */
    public SynchronizedBlock createSynchronizedBlock(StatementBlock body) {
        SynchronizedBlock res = new SynchronizedBlock(body);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link SynchronizedBlock}.
     * 
     * @return a new instance of SynchronizedBlock.
     */
    public SynchronizedBlock createSynchronizedBlock(Expression e, StatementBlock body) {
        SynchronizedBlock res = new SynchronizedBlock(e, body);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Then}.
     * 
     * @return a new instance of Then.
     */
    public Then createThen() {
        Then res = new Then();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Then}.
     * 
     * @return a new instance of Then.
     */
    public Then createThen(Statement body) {
        Then res = new Then(body);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Throw}.
     * 
     * @return a new instance of Throw.
     */
    public Throw createThrow() {
        Throw res =  new Throw();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Throw}.
     * 
     * @return a new instance of Throw.
     */
    public Throw createThrow(Expression expr) {
        Throw res = new Throw(expr);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Try}.
     * 
     * @return a new instance of Try.
     */
    public Try createTry() {
        Try res = new Try();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Try}.
     * 
     * @return a new instance of Try.
     */
    public Try createTry(StatementBlock body) {
        Try res = new Try(body);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link Try}.
     * 
     * @return a new instance of Try.
     */
    public Try createTry(StatementBlock body, ASTList<Branch> branches) {
        Try res = new Try(body, branches);
        res.setFactory(this);
        trace(res);
        return res;
    }
    
    /**
     * Creates a new {@link While}.
     * 
     * @return a new instance of While.
     */
    public While createWhile() {
        While res = new While();
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link While}.
     * 
     * @return a new instance of While.
     */
    public While createWhile(Expression guard) {
        While res = new While(guard);
        res.setFactory(this);
        trace(res);
        return res;
    }

    /**
     * Creates a new {@link While}.
     * 
     * @return a new instance of While.
     */
    public While createWhile(Expression guard, Statement body) {
        While res = new While(guard, body);
        res.setFactory(this);
        trace(res);
        return res;
    }
}