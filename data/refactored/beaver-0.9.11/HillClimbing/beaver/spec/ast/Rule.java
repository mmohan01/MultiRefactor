/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of Beaver Parser Generator.                       *
 * Copyright (C) 2003,2004 Alexander Demenchuk <alder@softanvil.com>.  *
 * All rights reserved.                                                *
 * See the file "LICENSE" for the terms and conditions for copying,    *
 * distribution and modification of Beaver.                            *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package beaver.spec.ast;

import beaver.Symbol;

public abstract class Rule extends Node {
    static abstract public class Definition extends Node {
        static abstract public class Element extends Node {
            public Symbol sym_name;
            public Symbol alias;
            public Symbol ebnf_sym;

            public Element(Symbol sym_name, Symbol alias, Symbol ebnf_sym)
             {
                this.sym_name = sym_name;
                this.alias = alias;
                this.ebnf_sym = ebnf_sym;
            }

            public abstract void accept(TreeWalker walker)
             {
                walker.visit(this);
            }

            public abstract String getName()
             {
                return (String) sym_name.value;
            }

            public abstract String getAlias()
             {
                return (String) alias.value;
            }

            public abstract char getExtUseMark()
             {
                return ebnf_sym.value == null ? ' ' : ((String) ebnf_sym.value).charAt(0);
            }
        }

        public Element[] elements;
        public Symbol prec_sym_name;
        public Symbol code;

        public Definition(Element[] elts, Symbol prec_sym_name, Symbol code)
         {
            this.elements = elts;
            this.prec_sym_name = prec_sym_name;
            this.code = code;
        }

        public Definition(Element[] elts)
         {
            this.elements = elts;
            this.prec_sym_name = null;
            this.code = null;
        }

        public abstract void accept(TreeWalker walker)
         {
            walker.visit(this);
        }

        public abstract String getPrecedenceSymbolName()
         {
            return (String) prec_sym_name.value;
        }

        public abstract String getReduceActionCode()
         {
            return (String) code.value;
        }
    }

    public Symbol lhs_sym;
    public Definition[] defs;

    public Rule(Symbol lhs_sym, Rule.Definition[] defs)
     {
        this.lhs_sym = lhs_sym;
        this.defs = defs;
    }

    public abstract String getLHSSymbolName()
     {
        return (String) lhs_sym.value;
    }

    public abstract void accept(TreeWalker walker)
     {
        walker.visit(this);
    }
}
