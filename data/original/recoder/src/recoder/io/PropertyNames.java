// This file is part of the RECODER library and protected by the LGPL.

package recoder.io;

/**
 * This interface defines the known property names in the project settings.
 * 
 * @author AL
 */
public interface PropertyNames {

    /**
     * Property <TT>output.path</TT>.
     * <P>
     * 
     * Defines the output path used by the
     * {@link recoder.io.SourceFileRepository}to write back changed or new
     * compilation units. Defaults to the corresponding environment variable or
     * if there is none, the current user directory.
     */
    String OUTPUT_PATH = "output.path";

    /**
     * Property <TT>input.path</TT>.
     * <P>
     * 
     * Defines the search path list used by the
     * {@link recoder.io.ClassFileRepository}and
     * {@link recoder.io.SourceFileRepository}to load new classes. Defaults to
     * the corresponding environment variable or if there is none, the current
     * class path, or if there is none, ".". The system should at least define
     * the java.lang-classes.
     */
    String INPUT_PATH = "input.path";

    /**
     * Property <TT>jdk1.4</TT>.
     * <P>
     * 
     * Defines the global behavior of the parser and lexer: If set, <CODE>
     * assert</CODE> is considered a keyword, otherwise no assert statement can
     * be parsed. The setting defaults to <CODE>true</CODE>
     */
    String JDK1_4 = "jdk1.4";
    
    /**
     * Property <TT>JAVA_5</TT>.
     * <P>
     * 
     * Defines the global behavior of the parser and lexer: If set, <CODE>
     * Java 5 language features (like generics, autoboxing, ...) are accepted.
     * Setting this property to <CODE>true</CODE> automatically sets the
     * property <TT>JDK1_4</TT> to <CODE>true</CODE>.
     * The setting defaults to <CODE>true</CODE>
     */
    String JAVA_5 = "java5";

    /**
     * Property <TT>JAVA_7</TT>.
     * <P>
     * 
     * Defines the global behavior: Should the new language features of Java 7
     * (http://openjdk.java.net/projects/coin/) be accepted? 
     * This has no effect on the parser (it always accepts those features), and
     * has only effects on the semantics checker / AST validation. 
     * Setting this property to <CODE>true</CODE> automatically sets the
     * properties <TT>JDK1_4</TT> and <TT>JAVA_%</TT> to <CODE>true</CODE>.
     * The setting defaults to <CODE>true</CODE>
     */
    String JAVA_7 = "java7";

    /**
     * Property <TT>ADD_NEWLINE_AT_END_OF_FILE</TT>
     * <p>
     * If set to <CODE>true</CODE>, adds an extra newline at end of each 
     * source file, which allows to parse files that end with a single
     * line comment (without a newline feed). While those are not valid .java-files, 
     * most compilers accept these files. 
     * The setting defaults to <CODE>true</CODE>
     */
    String ADD_NEWLINE_AT_END_OF_FILE = "extra_newline_at_end_of_file";
    
    /**
     * Property <TT>error.threshold</TT>.
     * <P>
     * 
     * Defines the maximum number of errors the error handler accepts before
     * falling back to a safe state.
     */
    String ERROR_THRESHOLD = "error.threshold";

    /**
     * Property <TT>class.search.mode</TT>.
     * <P>
     * 
     * Defines the search mode of the {@link recoder.service.NameInfo}. Valid
     * values consist of a combination of at most one of each mode indicators
     * <CODE>'s'</CODE>,<CODE>'c'</CODE> and <CODE>'r'</CODE> (or
     * uppercase versions).
     * <DL COMPACT>
     * <DT><CODE>'s'</CODE>
     * <DD>look for source files
     * <DT><CODE>'c'</CODE>
     * <DD>look for class files
     * <DT><CODE>'r'</CODE>
     * <DD>look for classes by runtime reflection
     * </DL>
     * <P>
     * Examples: <BR>
     * <CODE>"sc"</CODE>- default setting, looks for source files, then class
     * files. <BR>
     * <CODE>"scr"</CODE>- use reflection as a last resort. <BR>
     * <CODE>""</CODE>- do not reload classes at all.
     */
    String CLASS_SEARCH_MODE = "class.search.mode";

    /**
     * Property <TT>overwriteParsePositions</TT>.
     * <P>
     * 
     * If set, global parse positions of source elements are reset by the pretty
     * printer according to the new positions. Default is <TT>false</TT>.
     */
    String OVERWRITE_PARSE_POSITIONS = "overwriteParsePositions";

    /**
     * Property <TT>overwriteIndentation</TT>.
     * <P>
     * 
     * If set, indentations are always set by the pretty printer, even if they
     * already are defined. Default is <TT>false</TT>.
     */
    String OVERWRITE_INDENTATION = "overwriteIndentation";

    /**
     * Property <TT>indentationAmount</TT>.
     * <P>
     * 
     * The number of blanks for each indentation level. Default is 4.
     * <P>
     * <TABLE BORDER>
     * <TR>
     * <TH WIDTH=33%>n = 2</TH>
     * <TH WIDTH=33%>n = 3</TH>
     * <TH WIDTH=33%>n = 4</TH>
     * </TR>
     * <TR VALIGN=top>
     * <TD>
     * 
     * <PRE>
     * 
     * while (i < n) { if (a[i] == x) { return i; } i += 1; }
     * 
     * </PRE>
     * 
     * </TD>
     * <TD>
     * 
     * <PRE>
     * 
     * while (i < n) { if (a[i] == x) { return i; } i += 1; }
     * 
     * </PRE>
     * 
     * </TD>
     * <TD>
     * 
     * <PRE>
     * 
     * while (i < n) { if (a[i] == x) { return i; } i += 1; }
     * 
     * </PRE>
     * 
     * </TD>
     * </TR>
     * </TABLE>
     */
    String INDENTATION_AMOUNT = "indentationAmount";

    /**
     * Property <TT>glueStatementBlocks</TT>.
     * <P>
     * 
     * If set, the opening bracket of a statement block follows immediately
     * after the parent instruction. Default is <TT>true</TT>.
     * <P>
     * <TABLE BORDER>
     * <TR>
     * <TH WIDTH=50%>true</TH>
     * <TH WIDTH=50%>false</TH>
     * </TR>
     * <TR VALIGN=top>
     * <TD>
     * 
     * <PRE>
     * 
     * void foo() { ...
     * 
     * 
     * while... { ...
     * 
     * </PRE>
     * 
     * </TD>
     * <TD>
     * 
     * <PRE>
     * 
     * void foo() { ...
     * 
     * while... { ...
     * 
     * </PRE>
     * 
     * </TD>
     * </TR>
     * </TABLE>
     */
    String GLUE_STATEMENT_BLOCKS = "glueStatementBlocks";

    /**
     * Property <TT>glueSequentialBranches</TT>.
     * <P>
     * 
     * If set, branches Else, Catch, Default, Catch and Finally follows
     * immediately after the closing bracket of the previous branch or the
     * enclosing statement. Default is <TT>true</TT>. If set, the property
     * <TT>glueStatementBlocks</TT> should also be set.
     * <P>
     * <TABLE BORDER>
     * <TR>
     * <TH WIDTH=50%>true</TH>
     * <TH WIDTH=50%>false</TH>
     * </TR>
     * <TR VALIGN=top>
     * <TD>
     * 
     * <PRE>
     * 
     * ... } else ...
     *  ' switch ... case ... case ... default ...
     * 
     * ... } catch ... } catch ... } finally ...
     * 
     * </PRE>
     * 
     * </TD>
     * <TD>
     * 
     * <PRE>
     * 
     * ... } else ...
     * 
     * switch ... case ... case ... default ...
     * 
     * ... } catch ... } catch ... } finally ...
     * 
     * </PRE>
     * 
     * </TD>
     * </TR>
     * </TABLE>
     */
    String GLUE_SEQUENTIAL_BRANCHES = "glueSequentialBranches";

    /**
     * 
     * Property <TT>glueControlExpressions</TT>.
     * <P>
     * 
     * If set, the boolean condition of a conditional statement follows
     * immediately after the statement keyword. Default is <TT>false</TT>.
     * <P>
     * <TABLE BORDER>
     * <TR>
     * <TH WIDTH=50%>true</TH>
     * <TH WIDTH=50%>false</TH>
     * </TR>
     * <TR VALIGN=top>
     * <TD>
     * 
     * <PRE>
     * 
     * if(x > 0) ...
     * 
     * </PRE>
     * 
     * </TD>
     * <TD>
     * 
     * <PRE>
     * 
     * if (x > 0) ...
     * 
     * </PRE>
     * 
     * </TD>
     * </TR>
     * </TABLE>
     */
    String GLUE_CONTROL_EXPRESSIONS = "glueControlExpressions";

    /**
     * Property <TT>glueParameterLists</TT>.
     * <P>
     * 
     * If set, the parameter list of a method declaration as well as a method
     * call and exception catch clauses follows immediately after the method
     * name or the catch symbol. Default is <TT>true</TT>.
     * <P>
     * <TABLE BORDER>
     * <TR>
     * <TH WIDTH=50%>true</TH>
     * <TH WIDTH=50%>false</TH>
     * </TR>
     * <TR VALIGN=top>
     * <TD>
     * 
     * <PRE>
     * 
     * void foo(...)
     * 
     * catch(...)
     * 
     * </PRE>
     * 
     * </TD>
     * <TD>
     * 
     * <PRE>
     * 
     * void foo (...)
     * 
     * catch (...)
     * 
     * </PRE>
     * 
     * </TD>
     * </TR>
     * </TABLE>
     */
    String GLUE_PARAMETER_LISTS = "glueParameterLists";

    /**
     * Property <TT>glueParameters</TT>.
     * <P>
     * 
     * If set, the parameters of a method declaration as well as the arguments
     * of a method call follows immediately after the comma. Default is <TT>
     * false</TT>.
     * <P>
     * <TABLE BORDER>
     * <TR>
     * <TH WIDTH=50%>true</TH>
     * <TH WIDTH=50%>false</TH>
     * </TR>
     * <TR VALIGN=top>
     * <TD>
     * 
     * <PRE>
     * 
     * foo(x,y,z)
     * 
     * </PRE>
     * 
     * </TD>
     * <TD>
     * 
     * <PRE>
     * 
     * foo(x, y, z)
     * 
     * </PRE>
     * 
     * </TD>
     * </TR>
     * </TABLE>
     */
    String GLUE_PARAMETERS = "glueParameters";

    /**
     * 
     * Property <TT>glueParameterParentheses</TT>.
     * <P>
     * 
     * If set, the parameters of a method declaration as well as a method call
     * follow / end immediately after / before the parantheses. Default is <TT>
     * true</TT>.
     * <P>
     * <TABLE BORDER>
     * <TR>
     * <TH WIDTH=50%>true</TH>
     * <TH WIDTH=50%>false</TH>
     * </TR>
     * <TR VALIGN=top>
     * <TD>
     * 
     * <PRE>
     * 
     * foo(x,...,z)
     * 
     * </PRE>
     * 
     * </TD>
     * <TD>
     * 
     * <PRE>
     * 
     * foo( x,...,z )
     * 
     * </PRE>
     * 
     * </TD>
     * </TR>
     * </TABLE>
     */
    String GLUE_PARAMETER_PARENTHESES = "glueParameterParentheses";

    /**
     * Property <TT>glueExpressionParentheses</TT>.
     * <P>
     * 
     * If set, expressions in parentheses follow / end immediately after /
     * before the parantheses. Default is <TT>true</TT>.
     * <P>
     * <TABLE BORDER>
     * <TR>
     * <TH WIDTH=50%>true</TH>
     * <TH WIDTH=50%>false</TH>
     * </TR>
     * <TR VALIGN=top>
     * <TD>
     * 
     * <PRE>
     * 
     * (1 < < 3)
     * 
     * </PRE>
     * 
     * </TD>
     * <TD>
     * 
     * <PRE>
     *  ( 1 < < 3 )
     * </PRE>
     * 
     * </TD>
     * </TR>
     * </TABLE>
     */
    String GLUE_EXPRESSION_PARENTHESES = "glueExpressionParentheses";

    /**
     * Property <TT>glueInitializerParentheses</TT>.
     * <P>
     * 
     * If set, expressions in initializer blocks follow / end immediately after /
     * before the brackets. Default is <TT>false</TT>.
     * <P>
     * <TABLE BORDER>
     * <TR>
     * <TH WIDTH=50%>true</TH>
     * <TH WIDTH=50%>false</TH>
     * </TR>
     * <TR VALIGN=top>
     * <TD>
     * 
     * <PRE>
     * 
     * {1, 2, 3}
     * 
     * </PRE>
     * 
     * </TD>
     * <TD>
     * 
     * <PRE>
     *  { 1, 2, 3 }
     * </PRE>
     * 
     * </TD>
     * </TR>
     * </TABLE>
     *  
     */
    String GLUE_INITIALIZER_PARENTHESES = "glueInitializerParentheses";

    /**
     * Property <TT>glueInfixOperators</TT>.
     * <P>
     * 
     * If set, infix operators are attached to their operands. Default is <TT>
     * false</TT>.
     * <P>
     * <TABLE BORDER>
     * <TR>
     * <TH WIDTH=50%>true</TH>
     * <TH WIDTH=50%>false</TH>
     * </TR>
     * <TR VALIGN=top>
     * <TD>
     * 
     * <PRE>
     * 
     * x=y>0?a+b*c:0
     * 
     * </PRE>
     * 
     * </TD>
     * <TD>
     * 
     * <PRE>
     * 
     * x = y > 0 ? a + b * c : 0
     * 
     * </PRE>
     * 
     * </TD>
     * </TR>
     * </TABLE>
     *  
     */
    String GLUE_INFIX_OPERATORS = "glueInfixOperators";

    /**
     * Property <TT>glueUnaryOperators</TT>.
     * <P>
     * 
     * If set, unary operators are attached to their operands. Default is <TT>
     * true</TT>.
     * <P>
     * <TABLE BORDER>
     * <TR>
     * <TH WIDTH=50%>true</TH>
     * <TH WIDTH=50%>false</TH>
     * </TR>
     * <TR VALIGN=top>
     * <TD>
     * 
     * <PRE>
     * 
     * -(a++)
     * 
     * </PRE>
     * 
     * </TD>
     * <TD>
     * 
     * <PRE>
     *  - (a ++)
     * </PRE>
     * 
     * </TD>
     * </TR>
     * </TABLE>
     */
    String GLUE_UNARY_OPERATORS = "glueUnaryOperators";

    /**
     * 
     * Property <TT>glueMembers</TT>.
     * <P>
     * 
     * If set, members follow without blank lines in between. Default is <TT>
     * false</TT>.
     * <P>
     * <TABLE BORDER>
     * <TR>
     * <TH WIDTH=50%>true</TH>
     * <TH WIDTH=50%>false</TH>
     * </TR>
     * <TR VALIGN=top>
     * <TD>
     * 
     * <PRE>
     * 
     * int fee = 0; void foo() { ... } int faa(int x);
     * 
     * </PRE>
     * 
     * </TD>
     * <TD>
     * 
     * <PRE>
     * 
     * int fee = 0;
     * 
     * void foo() { ... }
     * 
     * int faa(int x);
     * 
     * </PRE>
     * 
     * </TD>
     * </TR>
     * </TABLE>
     *  
     */
    String GLUE_MEMBERS = "glueMembers";

    String GLUE_LABELS = "glueLabels";

    String ALIGN_LABELS = "alignLabels";

    String GLUE_DECLARATION_APPENDICES = "glueDeclarationAppendices";

    String INDENT_CASE_BRANCHES = "indentCaseBranches";
    
    String TABSIZE = "TabSize";
    
    String USE_OLD_BYTECODE_PARSER = "useOldBytecodeParser";
    
    /**
     * Ignore type args in "param matches". Should be usually set to "false", but some transformations may require this 
     * (Java5to4 in particular, which is why this option is here in the first place).
     */
    String DO_NOT_CHECK_TYPE_ARGUMENTS_FOR_PARAMETER_MATCHES = "doNotUseTypeArgsForParameterMatches";
}