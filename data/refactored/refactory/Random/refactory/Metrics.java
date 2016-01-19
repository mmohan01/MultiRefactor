package refactory;

import java.util.List;

import recoder.convenience.AbstractTreeWalker;
import recoder.convenience.ForestWalker;
import recoder.java.CompilationUnit;
import recoder.java.ProgramElement;
import recoder.java.declaration.ClassDeclaration;
import recoder.java.declaration.ConstructorDeclaration;
import recoder.java.declaration.FieldDeclaration;
import recoder.java.declaration.InterfaceDeclaration;
import recoder.java.declaration.MethodDeclaration;
import recoder.java.declaration.TypeDeclaration;
import recoder.java.declaration.VariableDeclaration;
import recoder.java.declaration.modifier.Private;
import recoder.java.declaration.modifier.Protected;
import recoder.java.declaration.modifier.Public;
import recoder.java.declaration.modifier.VisibilityModifier;

// Calculates various software metrics from the source code input.
public class Metrics {
    private List<CompilationUnit> units;
    private final AbstractTreeWalker tw;

    public Metrics(List<CompilationUnit> units)
     {
        this.units = units;
    }

    public List<CompilationUnit> getUnits()
     {
        return this.units;
    }

    public void setUnits(List<CompilationUnit> units)
     {
        this.units = units;
    }

    // Amount of classes in project.
    // Iterated from the class declarations.
    public int classDeclarationAmount()
     {
        int classCounter = 0;
        this.tw = new ForestWalker(this.units);

        while (this.tw.next())
            if (this.tw.getProgramElement() instanceof ClassDeclaration)
                classCounter++;

        return classCounter;
    }

    // Amount of methods in project.
    // Iterated from the method declarations.
    // Not including constructor methods.
    public int methodDeclarationAmount()
     {
        int methodCounter = 0;
        this.tw = new ForestWalker(this.units);

        while (this.tw.next())
            if (this.tw.getProgramElement() instanceof MethodDeclaration)
                    if (!(this.tw.getProgramElement() instanceof ConstructorDeclaration))
                        methodCounter++;

        return methodCounter;
    }

    // Amount of methods over the overall amount of classes.
    // Uses all method declarations and all types.
    // Abstract method declarations are not excluded.
    public int methodsPerType()
     {
        int typeCounter = 0;
        int methodCounter;
        this.tw = new ForestWalker(this.units);

        while (this.tw.next())
            if (this.tw.getProgramElement() instanceof TypeDeclaration)
                typeCounter++;

        methodCounter = methodDeclarationAmount();
        return methodCounter / typeCounter;
    }

    // Amount of interfaces over the overall amount of classes (as a percentage).
    public float abstractness()
     {
        int typeCounter = 0;
        int interfaceCounter = 0;
        this.tw = new ForestWalker(this.units);

        while (this.tw.next())
         {
            if (this.tw.getProgramElement() instanceof TypeDeclaration)
                typeCounter++;
            if (this.tw.getProgramElement() instanceof InterfaceDeclaration)
                interfaceCounter++;
        }

        float answer = (float)interfaceCounter / typeCounter * 100;
        return answer;
    }

    // Amount of abstract elements in project.
    public int abstractAmount()
     {
        int abstractCounter = 0;
        this.tw = new ForestWalker(this.units);

        while (this.tw.next())
         {
            if (this.tw.getProgramElement() instanceof TypeDeclaration)
             {
                TypeDeclaration td = (TypeDeclaration)(this.tw.getProgramElement());
                if (td.isAbstract())
                    abstractCounter++;
            }
             else if (this.tw.getProgramElement() instanceof MethodDeclaration)
             {
                MethodDeclaration md = (MethodDeclaration)(this.tw.getProgramElement());
                if (md.isAbstract())
                    abstractCounter++;
            }
        }

        return abstractCounter;
    }

    // Amount of static elements in project.
    // Of the variable declarations, only a field can be static.
    public int staticAmount()
     {
        int staticCounter = 0;
        this.tw = new ForestWalker(this.units);

        while (this.tw.next())
         {
            if (this.tw.getProgramElement() instanceof TypeDeclaration)
             {
                TypeDeclaration td = (TypeDeclaration)(this.tw.getProgramElement());
                if (td.isStatic())
                    staticCounter++;
            }
             else if (this.tw.getProgramElement() instanceof MethodDeclaration)
             {
                MethodDeclaration md = (MethodDeclaration)(this.tw.getProgramElement());
                if (md.isStatic())
                    staticCounter++;
            }
             else if (this.tw.getProgramElement() instanceof FieldDeclaration)
             {
                FieldDeclaration fd = (FieldDeclaration)(this.tw.getProgramElement());
                if (fd.isStatic())
                    staticCounter++;
            }
        }

        return staticCounter;
    }

    // Amount of final elements in project.
    public int finalAmount()
     {
        int finalCounter = 0;
        this.tw = new ForestWalker(this.units);

        while (this.tw.next())
         {
            if (this.tw.getProgramElement() instanceof TypeDeclaration)
             {
                TypeDeclaration td = (TypeDeclaration)(this.tw.getProgramElement());
                if (td.isFinal())
                    finalCounter++;
            }
             else if (this.tw.getProgramElement() instanceof MethodDeclaration)
             {
                MethodDeclaration md = (MethodDeclaration)(this.tw.getProgramElement());
                if (md.isFinal())
                    finalCounter++;
            }
             else if (this.tw.getProgramElement() instanceof VariableDeclaration)
             {
                VariableDeclaration vd = (VariableDeclaration)(this.tw.getProgramElement());
                if (vd.isFinal())
                    finalCounter++;
            }
        }

        return finalCounter;
    }

    // Amount of classes in the project that are declared inside other classes.
    public int innerClassAmount()
     {
        int innerClassCounter = 0;
        this.tw = new ForestWalker(this.units);

        while (this.tw.next())
         {
            if (this.tw.getProgramElement() instanceof TypeDeclaration)
             {
                TypeDeclaration td = (TypeDeclaration)(this.tw.getProgramElement());
                if (td.isInner())
                    innerClassCounter++;
            }
        }

        return innerClassCounter;
    }

    // Accumulative amount of child classes for each class.
    public int childAmount()
     {
        int childCounter = 0;

        for (CompilationUnit u: this.units)
            childCounter += u.getChildCount();

        return childCounter;
    }

    // Amount of lines of code in the project.
    public int linesOfCode()
     {
        int childCounter = 0;

        for (CompilationUnit u: this.units)
            childCounter += u.getEndPosition().getLine();

        return childCounter;
    }

    // Amount of java files in the source code.
    public int fileAmount()
     {
        return this.units.size();
    }

    // Amount of visibility in a project among type, method and variable declarations.
    // Returns integer value to represent the visibility where a higher value means more visibility.
    public int visibility()
     {
        int counter = 0;
        this.tw = new ForestWalker(this.units);

        while (this.tw.next())
         {
            ProgramElement pe = this.tw.getProgramElement();

            if (pe instanceof VariableDeclaration)
             {
                VariableDeclaration vd = (VariableDeclaration) pe;
                counter += identifier(vd.getVisibilityModifier());
            }
            if (pe instanceof MethodDeclaration)
             {
                MethodDeclaration md = (MethodDeclaration) pe;
                counter += identifier(md.getVisibilityModifier());
            }
            if (pe instanceof TypeDeclaration)
             {
                TypeDeclaration td = (TypeDeclaration) pe;
                counter += identifier(td.getVisibilityModifier());
            }
        }

        return counter;
    }

    // Returns a value to represent the visibility of a modifier.
    public int identifier(VisibilityModifier vm)
     {
        if (vm instanceof Public)
            return 3;
         else if (vm instanceof Protected)
            return 2;
         else if (vm instanceof Private)
            return 0;
         else
            return 1;
    }
}
