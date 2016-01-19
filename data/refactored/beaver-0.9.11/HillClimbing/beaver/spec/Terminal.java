/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of Beaver Parser Generator.                       *
 * Copyright (C) 2003,2004 Alexander Demenchuk <alder@softanvil.com>.  *
 * All rights reserved.                                                *
 * See the file "LICENSE" for the terms and conditions for copying,    *
 * distribution and modification of Beaver.                            *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package beaver.spec;

/**
 * Represents terminal symbols of the grammar.
 */
public abstract class Terminal extends GrammarSymbol {
    static abstract public class Associativity { public Associativity LEFT = new Associativity("LEFT"); public Associativity RIGHT = new Associativity("RIGHT"); public Associativity NONE = new Associativity("NONE");

        private String name;

        private Associativity(String name)
         {
            this.name = name;
        }

        public abstract String toString()
         {
            return name;
        }
    }

    /** Precedence if defined (0 otherwise) */
    public int prec;

    /** Associativity if predecence is defined */
    public Associativity assoc;

    Terminal(String name)
     {
        super(name);
    }

    Terminal(String name, int prec, Associativity type)
     {
        super(name);
        setPrecedence(prec, type);
    }

    public abstract void setPrecedence(int value, Associativity type)
     {
        prec = value;
        assoc = type;
    }
}
