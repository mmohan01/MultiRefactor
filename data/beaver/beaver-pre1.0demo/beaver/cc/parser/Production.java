/**
 * Beaver: a compiler front-end compiler
 * Copyright (c) 2003-2012, Alexander Demenchuk
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package beaver.cc.parser;

/**
 *
 */
public class Production {
    /**
     * Rule ID.
     */
    int                id;
    /**
     * Production precedence - either explicit or derived from its rightmost RHS terminal.
     */
    char               precedence;
    /**
     * Rule name.
     */
    String             name;
    /**
     * Non-terminal that this production defines.
     */
    Symbol.NonTerminal lhs;
    /**
     * Sequence of symbols that defines LHS non-terminal.
     */
    RHSElement[]       rhs;

    public Production(String name, Symbol.NonTerminal lhs, RHSElement[] rhs) {
        this.name = name;
        this.lhs = lhs;
        this.rhs = rhs;
    }

    public void setPrecedence(char prec) {
        precedence = prec;
    }

    Production(Symbol.NonTerminal lhs, RHSElement[] rhs) {
        this(null, lhs, rhs);
    }

    /**
     * Production can match an empty string only if all its RHS symbols also match an empty string.
     *
     * @return true if production matches an empty string
     */
    boolean canMatchEmptyString() {
        for (int i = 0; i < rhs.length; i++) {
            if (!rhs[i].symbol.canMatchEmptyString())
                return false;
        }
        return true;
    }

    /**
     * @return true is production matches only keywords
     */
    boolean matchesKeywordsOnly() {
        for (int i = 0; i < rhs.length; i++) {
            if (!rhs[i].symbol.isKeyword())
                return false;
        }
        return true;
    }

    public boolean hasName() {
        return name != null;
    }

    /**
     * Full name is created by joining LHS non-terminal name and the rule name.
     *
     * @return production full name
     */
    public String getFullName() {
        return name == null ? lhs.name : this.name + lhs.name;
    }

    public String toString() {
        String repr = lhs + " =";
        if (name != null) {
            repr += " { " + name + " }";
        }
        for (int i = 0; i < rhs.length; i++) {
            repr += " " + rhs[i];
        }
        return repr;
    }

    /**
     * @return -1: create "new" node,
     *         -2: return NULL,
     *         -3: new empty list
     *         -4: new list with 1 element,
     *         -5: extend list
     *         [0..RHS.length): return symbol at this index,
     */
    int selectReduceStrategy() {
        if (lhs.isAList) {
            if (rhs.length == 0) {
                return -3;
            }
            Production otherRule = (lhs.rules[0] == this ? lhs.rules[1] : lhs.rules[0]);
            return (rhs.length < otherRule.rhs.length ? -4 : -5);
        }
        if (lhs.delegate instanceof Symbol.NonTerminal && ((Symbol.NonTerminal) lhs.delegate).isAList) {
            if (rhs.length == 0) {
                return -3;
            }
        }
        if (lhs.delegate != null) {
            int ix = -2;
            for (int i = 0; i < rhs.length; i++) {
                if (!rhs[i].symbol.isKeyword()) {
                    if (ix < 0) {
                        ix = i;
                    } else {
                        ix = -1;
                        break;
                    }
                }
            }
            return ix;
        }
        return -1;
    }

    public static class RHSElement {
        String name;
        Symbol symbol;

        public RHSElement(String name, Symbol symbol) {
            this.name = name;
            this.symbol = symbol;
        }

        public RHSElement(Symbol symbol) {
            this.symbol = symbol;
        }

        public String toString() {
            return name != null ? name + ":" + symbol : symbol.toString();
        }
    }
}
