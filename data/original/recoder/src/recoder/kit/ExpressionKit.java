// This file is part of the RECODER library and protected by the LGPL.

package recoder.kit;

import java.util.List;

import recoder.ProgramFactory;
import recoder.abstraction.PrimitiveType;
import recoder.abstraction.Type;
import recoder.java.Expression;
import recoder.java.ExpressionContainer;
import recoder.java.Statement;
import recoder.java.expression.Assignment;
import recoder.java.expression.Literal;
import recoder.java.expression.Operator;
import recoder.java.expression.ParenthesizedExpression;
import recoder.java.reference.ArrayReference;
import recoder.java.reference.ConstructorReference;
import recoder.java.reference.MethodReference;
import recoder.java.reference.ReferencePrefix;
import recoder.java.reference.ReferenceSuffix;
import recoder.java.reference.VariableReference;
import recoder.list.generic.ASTArrayList;
import recoder.service.NameInfo;
import recoder.util.Debug;

public class ExpressionKit {

    private ExpressionKit() {
    	super();
    }

    /**
     * Query deciding if the given expression tree contains statements as a
     * conservative estimate if it has side effects. An expression that contains
     * no statements (method calls, assignments) cannot have any side-effects.
     * Parenthesized expressions are not considered statements in this context,
     * even though they technically may appear as such.
     * 
     * @param expr
     *            an expression.
     * @return <CODE>true</CODE>, if the expression contains expressions,
     *         <CODE>false</CODE> if it does not.
     */
    public static boolean containsStatements(Expression expr) {
        if (expr instanceof Statement) {
            if (!(expr instanceof ParenthesizedExpression)) {
                return true;
            }
        }
        if (expr instanceof ExpressionContainer) {
            ExpressionContainer con = (ExpressionContainer) expr;
            for (int i = 0, s = con.getExpressionCount(); i < s; i += 1) {
                if (containsStatements(con.getExpressionAt(i))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Non-updating query deciding if the given expression is used as a
     * left-hand value ("L-value"). L-values are either variables, or array
     * references. As there are no call-by-reference output parameters in Java,
     * L-value references must occur as first argument of an assigment operator
     * such as <CODE>=</CODE> or <CODE>++</CODE>.
     * 
     * @param r
     *            an expression.
     * @return <CODE>true</CODE> if the specified expression is an L-value,
     *         <CODE>false</CODE> otherwise.
     * @since 0.63
     */
    public static boolean isLValue(Expression r) {
        if ((r instanceof VariableReference) || (r instanceof ArrayReference)) {
            ExpressionContainer c = r.getExpressionContainer();
            return (c instanceof Assignment) && (c.getExpressionAt(0) == r);
        }
        return false;
    }

    /**
     * Query that collects all expressions that are evaluated before the given
     * expression in its statement or initializer in the correct order.
     * 
     * @param x
     *            an expression as part of a statement or an initializer.
     * @return a mutable list of expressions that preceed the given one.
     */
    public static List<Expression> collectPreceedingExpressions(Expression x) {
        Debug.assertNonnull(x);
        List<Expression> dest = new ASTArrayList<Expression>();
        if ((x instanceof MethodReference) || (x instanceof ConstructorReference)) {
            ExpressionContainer ec = (ExpressionContainer) x;
            for (int i = 0, s = ec.getExpressionCount(); i < s; i += 1) {
                dest.add(ec.getExpressionAt(i));
            }
        } else if (x instanceof ReferenceSuffix) {
            ReferencePrefix rp = ((ReferenceSuffix) x).getReferencePrefix();
            if (rp instanceof Expression) {
                dest.add((Expression) rp);
            }
        }
        while (true) {
            ExpressionContainer parent = x.getExpressionContainer();
            if (parent == null) {
                return dest;
            }
            boolean leftAssociative;
            if (parent instanceof Operator) {
                leftAssociative = ((Operator) parent).isLeftAssociative();
            } else {
                // all non-operator expression containers such as method calls
                // or array initializers are left-associative
                leftAssociative = true;
            }
            // collect all child expressions of parent that are evaluated before
            // x
            Expression expr;
            if (leftAssociative) {
                int i = 0;
                if (parent instanceof ReferenceSuffix) {
                    if (((ReferenceSuffix) parent).getReferencePrefix() instanceof Expression) {
                        i = 1;
                    }
                }
                while ((expr = parent.getExpressionAt(i)) != x) {
                    dest.add(expr);
                    i += 1;
                }
            } else {
                for (int i = parent.getExpressionCount() - 1; (expr = parent.getExpressionAt(i)) != x; i -= 1) {
                    dest.add(expr);
                }
            }
            if (!(parent instanceof Expression)) {
                return dest;
            }
            x = (Expression) parent;
        }
    }

    /**
     * Factory method that creates the default literal to a given type. For
     * non-primitive type, the result is a
     * {@link recoder.java.expression.literal.NullLiteral}, for primitive types
     * their corresponding default value (<CODE>0</CODE>,<CODE>false
     * </CODE>,<CODE>'\0'</CODE>).
     * 
     * @param f
     *            the program factory for the literal to create.
     * @param ni
     *            the name info defining the primitive type objects.
     * @param t
     *            the type to create a default value for.
     * @return a new literal object widening to the given type.
     */
    public static Literal createDefaultValue(ProgramFactory f, NameInfo ni, Type t) {
        Debug.assertNonnull(f, ni, t);
        if (t instanceof PrimitiveType) {
            if (t == ni.getIntType()) {
                return f.createIntLiteral(0);
            }
            if (t == ni.getBooleanType()) {
                return f.createBooleanLiteral(false);
            }
            if (t == ni.getCharType()) {
                return f.createCharLiteral('\0');
            }
            if (t == ni.getShortType()) {
                return f.createIntLiteral(0);
            }
            if (t == ni.getByteType()) {
                return f.createIntLiteral(0);
            }
            if (t == ni.getLongType()) {
                return f.createLongLiteral(0L);
            }
            if (t == ni.getFloatType()) {
                return f.createFloatLiteral(0.0f);
            }
            if (t == ni.getDoubleType()) {
                return f.createDoubleLiteral(0.0);
            }
            throw new IllegalArgumentException("Unknown primitive type " + t.getName());
        } else {
            return f.createNullLiteral();
        }
    }

}