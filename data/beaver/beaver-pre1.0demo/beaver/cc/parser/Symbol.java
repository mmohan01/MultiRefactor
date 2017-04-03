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

import beaver.util.BitSet;

/**
 *
 */
public abstract class Symbol {
    /**
     * Symbol's ID
     */
    int    id;
    /**
     * Name that was used to reference this symbol in the specification.
     */
    String name;

    Symbol(String name) {
        this.name = name;
    }

    public String toString() {
        return name;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    /**
     * Informs the caller whether this symbol can match an empty string. This method always return
     * false for terminals. For non-terminals the result depends on whether at least one of the
     * rules can match an empty string.
     *
     * @return true if symbol can match an empty string
     */
    abstract boolean canMatchEmptyString();

    /**
     * Informs the caller whether this symbol represents a keyword.
     *
     * @return true if symbol matches a keyword
     */
    public abstract boolean isKeyword();

    public static class Terminal extends Symbol implements Comparable {
        /**
         * Precedence: 0 - undefined (lowest), 0xffff - highest
         */
        char precedence;
        /**
         * 'L' - left, 'R' - right, 'N' - none
         */
        char associativity;
        /**
         * A text of the keyword that this terminal matches. If this terminal represents a keyword...
         */
        String text;

        public Terminal(String name) {
            super(name);
            associativity = 'N';
        }

        public Terminal(String name, String text) {
            this(name);
            this.text = text;
        }

        public void setPrecedence(char prec, char assoc) {
            precedence = prec;
            associativity = assoc;
        }

        /**
         * Terminals cannot match an empty string.
         *
         * @return false
         */
        boolean canMatchEmptyString() {
            return false;
        }

        public boolean isKeyword() {
        	return text != null;
        }

        public String getKeywordText() {
            return text;
        }

        public String toString() {
            return text != null ? '"' + text + '"': name;
        }

        public int compareTo(Object o) {
            return name.compareTo(((Symbol.Terminal) o).name);
        }
    }

    public static class NonTerminal extends Symbol {
        /**
         * Productions to derive this nonterminal, i.e. where this non-terminal is a LHS.
         */
        Production[] rules;
        /**
         * The set of all terminal symbols that could appear at the beginning of a string derived
         * from this nonterminal.
         */
        BitSet       firstSet;
        /**
         * Non-terminal can match an empty string (is nullable) if it has a production that can
         * match an empty string.
         */
        boolean      canMatchEmptyString;
        /**
         * Non-terminal is static when all its productions match only keywords. This attribute is
         * used during parser code generation.
         */
        boolean      isStatic;
        /**
         * Indicates whether this non-terminal productions are used to build a list. This attribute
         * is used during parser code generation.
         */
        boolean      isAList;
        /**
         * An element of the list if this nonterminal is a list.
         */
        Symbol       listElement;
        /**
         * There are cases (optional symbol, for instance) when a non-terminal reduces a single
         * symbol only. In this case the parser will not create a separate AST node, but will just
         * return (pass through) that symbol in a generated semantic action. That symbol functions
         * as if it is a delegate for this non-terminal.
         */
        Symbol       delegate;

        public NonTerminal(String name) {
            super(name);
        }

        public void setRules(Production[] rules) {
            this.rules = rules;
        }

        public Production[] getRules() {
            return rules;
        }

        boolean canMatchEmptyString() {
            return canMatchEmptyString;
        }

        public boolean isKeyword() {
        	return isStatic;
        }

        public String getName() {
            return (delegate == null ? name : delegate.name);
        }

        public void checkAndSetListAttributes() {
            if (rules.length == 2) {
                if (rules[0].rhs.length == 0 || rules[1].rhs.length == 0) {
                    /*
                     *  OptList
                     *       =
                     *       | OptList TERM? Elem
                     *       ;
                     */
                    Production.RHSElement[] rhs = (rules[0].rhs.length > 0 ? rules[0].rhs : rules[1].rhs);
                    if (rhs[0].symbol == this) {
                        if (rhs.length == 2 && !rhs[1].symbol.isKeyword()) {
                            isAList = true;
                            listElement = rhs[1].symbol;
                        } else if (rhs.length == 3 && rhs[1].symbol.isKeyword() && !rhs[2].symbol.isKeyword()) {
                            isAList = true;
                            listElement = rhs[2].symbol;
                        }
                    }
                } else {
                    /*
                     *  List = Elem
                     *       | List TERM? Elem
                     *       ;
                     */
                    int i = (rules[0].rhs.length < rules[1].rhs.length ? 0 : 1);
                    if (rules[i].rhs.length == 1) {
                        Symbol elem = rules[i].rhs[0].symbol;
                        Production.RHSElement[] rhs = rules[i ^ 1].rhs;
                        if (rhs.length == 2 && rhs[1].symbol == elem) {
                            isAList = true;
                            listElement = elem;
                        } else if (rhs.length == 3 && rhs[1].symbol.isKeyword() && rhs[2].symbol == elem) {
                            isAList = true;
                            listElement = elem;
                        }
                    }
                }
            }
        }
    }
}
