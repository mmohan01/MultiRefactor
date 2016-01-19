/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of Beaver Parser Generator.                       *
 * Copyright (C) 2003,2004 Alexander Demenchuk <alder@softanvil.com>.  *
 * All rights reserved.                                                *
 * See the file "LICENSE" for the terms and conditions for copying,    *
 * distribution and modification of Beaver.                            *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package beaver.spec.ast;

/**
 * AST root node
 */
public abstract class GrammarTreeRoot extends Node {
    public Declaration[] declarations;
    public Rule[] rules;

    public GrammarTreeRoot(Declaration[] declarations, Rule[] rules)
     {
        this.declarations = declarations;
        this.rules = rules;
    }

    public abstract void accept(TreeWalker walker)
     {
        walker.visit(this);
    }
}
