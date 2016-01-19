/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of Beaver Parser Generator.                       *
 * Copyright (C) 2003,2004 Alexander Demenchuk <alder@softanvil.com>.  *
 * All rights reserved.                                                *
 * See the file "LICENSE" for the terms and conditions for copying,    *
 * distribution and modification of Beaver.                            *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package beaver.spec.ast;

import beaver.Symbol;

/**
 *
 */
public abstract class Declaration extends Node {
    static abstract public class NameContainer extends Declaration {
        public Symbol name;

        protected NameContainer(Symbol name)
         {
            this.name = name;
        }

        public abstract String getName()
         {
            return (String) name.value;
        }
    }

    static abstract public class CodeContainer extends Declaration {
        public Symbol code;

        protected CodeContainer(Symbol code)
         {
            this.code = code;
        }

        public abstract String getCode()
         {
            return (String) code.value;
        }
    }

    static abstract public class SymbolsContainer extends Declaration {
        public Symbol[] symbols;

        protected SymbolsContainer(Symbol[] symbols)
         {
            this.symbols = symbols;
        }
    }

    static abstract public class Error extends Declaration {
        public abstract void accept(TreeWalker walker)
         {
            walker.visit(this);
        }
    }

    static abstract public class Goal extends NameContainer {
        public Goal(Symbol name)
         {
            super(name);
        }

        public abstract void accept(TreeWalker walker)
         {
            walker.visit(this);
        }
    }

    static abstract public class ListType extends NameContainer {
        public ListType(Symbol name)
         {
            super(name);
        }

        public abstract void accept(TreeWalker walker)
         {
            walker.visit(this);
        }
    }

    static abstract public class Header extends CodeContainer {
        public Header(Symbol code)
         {
            super(code);
        }

        public abstract void accept(TreeWalker walker)
         {
            walker.visit(this);
        }
    }

    static abstract public class PackageName extends NameContainer {
        public PackageName(Symbol name)
         {
            super(name);
        }

        public abstract void accept(TreeWalker walker)
         {
            walker.visit(this);
        }
    }

    static abstract public class Implements extends SymbolsContainer {
        public Implements(Symbol[] names)
         {
            super(names);
        }

        public abstract void accept(TreeWalker walker)
         {
            walker.visit(this);
        }
    }

    static abstract public class Imports extends SymbolsContainer {
        public Imports(Symbol[] symbols)
         {
            super(symbols);
        }

        public abstract void accept(TreeWalker walker)
         {
            walker.visit(this);
        }
    }

    static abstract public class ClassName extends NameContainer {
        public ClassName(Symbol name)
         {
            super(name);
        }

        public abstract void accept(TreeWalker walker)
         {
            walker.visit(this);
        }
    }

    static abstract public class ClassCode extends CodeContainer {
        public ClassCode(Symbol code)
         {
            super(code);
        }

        public abstract void accept(TreeWalker walker)
         {
            walker.visit(this);
        }
    }

    static abstract public class ConstructorCode extends CodeContainer {
        public ConstructorCode(Symbol code)
         {
            super(code);
        }

        public abstract void accept(TreeWalker walker)
         {
            walker.visit(this);
        }
    }

    static abstract public class LeftAssoc extends SymbolsContainer {
        public LeftAssoc(Symbol[] symbols)
         {
            super(symbols);
        }

        public abstract void accept(TreeWalker walker)
         {
            walker.visit(this);
        }
    }

    static abstract public class RightAssoc extends SymbolsContainer {
        public RightAssoc(Symbol[] symbols)
         {
            super(symbols);
        }

        public abstract void accept(TreeWalker walker)
         {
            walker.visit(this);
        }
    }

    static abstract public class NonAssoc extends SymbolsContainer {
        public NonAssoc(Symbol[] symbols)
         {
            super(symbols);
        }

        public abstract void accept(TreeWalker walker)
         {
            walker.visit(this);
        }
    }

    static abstract public class Terminals extends SymbolsContainer {
        public Terminals(Symbol[] tokens)
         {
            super(tokens);
        }

        public abstract void accept(TreeWalker walker)
         {
            walker.visit(this);
        }
    }

    static abstract public class TypeOf extends SymbolsContainer {
        public Symbol type;

        public TypeOf(Symbol[] symbols, Symbol type)
         {
            super(symbols);
            this.type = type;
        }

        public abstract String getTypeName()
         {
            return type == null ? null : (String) type.value;
        }

        public abstract void accept(TreeWalker walker)
         {
            walker.visit(this);
        }
    }
}
