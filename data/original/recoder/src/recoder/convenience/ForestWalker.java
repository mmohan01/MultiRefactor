// This file is part of the RECODER library and protected by the LGPL.

package recoder.convenience;

import java.util.List;

import recoder.java.NonTerminalProgramElement;
import recoder.java.ProgramElement;

/**
 * Walks all syntax trees from a list of program elements in depth-first order.
 * 
 * @author AL
 */
public class ForestWalker extends AbstractTreeWalker {

    private List<? extends ProgramElement> rootList;

    private int unitIndex;

    public ForestWalker(List<? extends ProgramElement> roots) {
        super(roots.size() * 8);
        this.rootList = roots;
        unitIndex = 0;
        if (rootList.size() > 0) {
            reset(rootList.get(unitIndex));
        }
    }

    public boolean next() {
        if (count == 0) {
            current = null;
            if (unitIndex >= rootList.size() - 1) {
                return false;
            }
            unitIndex += 1;
            reset(rootList.get(unitIndex));
            return next();
        }
        current = stack[--count]; // pop
        if (current instanceof NonTerminalProgramElement) {
            NonTerminalProgramElement nt = (NonTerminalProgramElement) current;
            int s = nt.getChildCount();
            if (count + s >= stack.length) {
                ProgramElement[] newStack = new ProgramElement[Math.max(stack.length * 2, count + s)];
                System.arraycopy(stack, 0, newStack, 0, count);
                stack = newStack;
            }
            for (int i = s - 1; i >= 0; i -= 1) {
                stack[count++] = nt.getChildAt(i); // push
            }
        }
        return true;
    }

    public boolean equals(Object x) {
        if (!(x instanceof ForestWalker)) {
            return false;
        }
        ForestWalker fw = (ForestWalker) x;
        if (!super.equals(x)) {
            return false;
        }
        return (fw.unitIndex == unitIndex && fw.rootList.equals(rootList));
    }

}