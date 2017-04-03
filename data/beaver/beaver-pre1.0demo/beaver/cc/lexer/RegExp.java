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
package beaver.cc.lexer;

import java.util.Arrays;

/**
 * RegExp represents a regular expression pattern.
 */
public abstract class RegExp {
    /**
     * Visitor defines an interface of a pattern processor.
     */
    static abstract class Visitor {
        abstract void visit(MatchChar op);
        abstract void visit(MatchRange op);
        abstract void visit(Alt op);
        abstract void visit(Cat op);
        abstract void visit(Quantified op);
        abstract void visit(Pattern op);
        abstract void visit(Nil op);
    }

    /**
     * Accept a regular expression visitor. Subclasses provide implementation that informs the
     * visitor about the operation it is visiting.
     *
     * @param visitor
     *            operation visitor
     */
    abstract void accept(Visitor visitor);

    /**
     * MatchChar matches a single character.
     */
    public static class MatchChar extends RegExp {
        char chr;

        public MatchChar(char c) {
            this.chr = c;
        }

        public String toString() {
            return "'" + (' ' <= chr && chr < 0x7f ? Character.toString(chr) : CharReader.escape(chr)) + "'";
        }

        void accept(Visitor v) {
            v.visit(this);
        }
    }

    /**
     * MatchRange matches a character from a character range
     */
    public static class MatchRange extends RegExp {
        CharRange range;

        public MatchRange(CharRange range) {
            this.range = range;
        }

        public MatchRange(String range) {
            this.range = new CharRange(range);
        }

        public String toString() {
            return "[" + range + "]";
        }

        void accept(Visitor v) {
            v.visit(this);
        }

        /**
         * Removes a specified range of characters and returns a new expression that matches a
         * reduced set of characters.
         *
         * @param subRange
         *            is a subtrahend
         * @return new expression
         */
        public RegExp.MatchRange subtract(CharRange subRange) {
            CharRange newRange = new CharRange(range);
            newRange.sub(subRange);
            return new MatchRange(newRange);
        }

        public RegExp.MatchRange subtract(RegExp subtrahend) {
            if (!(subtrahend instanceof MatchRange))
                throw new IllegalArgumentException("RegExp matching a range expected for the subtrahend");

            return subtract(((MatchRange) subtrahend).range);
        }

        public RegExp.MatchRange subtract(String subRange) {
            CharRange newRange = new CharRange(range);
            newRange.sub(new CharRange(subRange));
            return new MatchRange(newRange);
        }
    }

    /**
     * Alt matches one of the two alternatives
     */
    public static class Alt extends RegExp {
        RegExp exp1;
        RegExp exp2;

        public Alt(RegExp exp1, RegExp exp2) {
            this.exp1 = (exp1 != null ? exp1 : new RegExp.Nil());
            this.exp2 = (exp2 != null ? exp2 : new RegExp.Nil());
        }

        /**
         * Adds a new alternative to the existing set of multiple alternatives.<p>
         * This method is just a helper for RegExp builders.
         *
         * @param exp yet another alternative
         * @return this RegExp
         */
        public Alt add(RegExp exp) {
            this.exp1 = new Alt(exp1, exp2);
            this.exp2 = exp;
            return this;
        }

        public String toString() {
            return exp1 + " | " + exp2;
        }

        /**
         * Counts the number of non-Alt expressions that are attached, directory or
         * indirectly, to this node.
         *
         * @return number of alternatives
         */
        public int countAlternatives() {
            return ((exp1 instanceof RegExp.Alt) ? ((RegExp.Alt) exp1).countAlternatives() : 1)
                 + ((exp2 instanceof RegExp.Alt) ? ((RegExp.Alt) exp2).countAlternatives() : 1);
        }

        void accept(Visitor v) {
            v.visit(this);
        }
    }

    /**
     * Cat concatenates subexpressions
     */
    public static class Cat extends RegExp {
        RegExp exp1;
        RegExp exp2;

        public Cat(RegExp exp1, RegExp exp2) {
            this.exp1 = exp1;
            this.exp2 = exp2;
        }

        public String toString() {
            return exp1 + " " + exp2;
        }

        void accept(Visitor v) {
            v.visit(this);
        }
    }

    /**
     * Quantifier specifies how often a preceding expression is allowed to occur
     */
    public static class Quantified extends RegExp {
        RegExp exp;
        int    min;
        int    max;

        public Quantified(RegExp exp, int min, int max) {
            this.exp = exp;
            this.min = min;
            this.max = max;
        }

        public Quantified(RegExp exp, char type) {
            this.exp = exp;
            switch (type) {
                case '?':
                    this.min = 0;
                    this.max = 1;
                    break;
                case '+':
                    this.min = 1;
                    this.max = -1;
                    break;
                case '*':
                    this.min = 0;
                    this.max = -1;
                    break;
                default:
                    throw new IllegalArgumentException("type=" + type);
            }
        }

        public String toString() {
            String quant;
            if (min == 0 && max == 1) {
                quant = "?";
            } else if (min == 0 && max == -1) {
                quant = "*";
            } else if (min == 1 && max == -1) {
                quant = "+";
            } else if (max == -1) {
                quant = "{" + min + ",}";
            } else {
                quant = "{" + min + "," + max + "}";
            }
            return (exp instanceof RegExp.Cat || exp instanceof RegExp.Alt ? "(" + exp + ")" : exp.toString()) + quant;
        }

        void accept(Visitor v) {
            v.visit(this);
        }
    }

    /**
     * Pattern represents a token matching expression
     */
    public static class Pattern extends RegExp {
        /**
         * MatchResult specifies what happens when a pattern matches a token
         */
        public static class MatchResult {
            /**
             * ID of the recognized token
             */
            int id;
            /**
             * Token precedence. When more than one pattern matches the same token ID the rule
             * with higher precedence will be used.
             */
            int precedence;
            /**
             * New start condition the scanner will be shifted after this match
             */
            int newStartCond;
            /**
             * The match is only active when recognition started in one of the listed conditions.
             * Note, these must be sorted (the array is fed directory to the "table lookup")
             */
            int[] startConditions;
            /**
             * The list of event notifications that should be the "sent" when there is a match.
             */
            String[] events;

            public MatchResult(int id, int precedence) {
                this.id = id;
                this.precedence = precedence;
                this.newStartCond = -1; // no shift
            }

            public MatchResult(int id, int precedence, String[] events) {
                this.id = id;
                this.precedence = precedence;
                this.newStartCond = -1;
                this.events = events;
            }

            public MatchResult(int id, int precedence, String[] events, int[] startConds, int nextStart) {
                this.id = id;
                this.precedence = precedence;
                this.startConditions = startConds;
                this.events = events;
                this.newStartCond = nextStart;
            }

            public boolean equals(Object o) {
                MatchResult r = (MatchResult) o;
                return r != null && id == r.id && Arrays.equals(events, r.events);
            }

            public static boolean equals(MatchResult r1, MatchResult r2) {
                return r1 == null ? r2 == null : r1.equals(r2);
            }
        }

        RegExp      exp;
        RegExp      ctx;
        MatchResult result;
        NFA.Node    firstNFANode;

        public Pattern(RegExp exp, RegExp ctx, MatchResult result) {
            this.exp = exp;
            this.ctx = ctx;
            this.result = result;
        }

        public Pattern(RegExp exp, MatchResult result) {
            this.exp = exp;
            this.ctx = new RegExp.Nil();
            this.result = result;
        }

        public String toString() {
            return result.id + " = " + exp + (ctx instanceof RegExp.Nil ? "" : " / " + ctx) + (result.events != null && result.events.length > 0 ? " -> " + Arrays.toString(result.events) : "") + "\n";
        }

        void accept(Visitor v) {
            v.visit(this);
        }
    }

    /**
     * Nil does not match anything, or rather it matches nothing :-)
     */
    public static class Nil extends RegExp {

        public String toString() {
            return "";
        }

        void accept(Visitor v) {
            v.visit(this);
        }
    }

    /**
     * A utility method that build a subexpression to match specified sequence of characters
     *
     * @param text
     *            to match
     * @return an expression that would match the specified text
     */
    public static RegExp matchText(String text) {
        if (text.isEmpty())
            return new Nil();
        CharReader charReader = new CharReader(text);
        RegExp re = new RegExp.MatchChar((char) charReader.readChar());
        for (int c = charReader.readChar(); c >= 0; c = charReader.readChar()) {
            re = new RegExp.Cat(re, new RegExp.MatchChar((char) c));
        }
        return re;
    }

    /**
     * A utility method that subtracts a character range from another range.
     *
     * @param minuend
     * @param subtrahend
     * @return the difference
     */
    public static RegExp diff(RegExp minuend, RegExp subtrahend) {
        if (!(minuend instanceof MatchRange))
            throw new IllegalArgumentException("Range RegExp is expected");
        return ((MatchRange) minuend).subtract(subtrahend);
    }

    public static RegExp diff(RegExp re, CharRange subRange) {
        if (!(re instanceof MatchRange))
            throw new IllegalArgumentException("Range RegExp expected");

        return ((MatchRange) re).subtract(subRange);
    }
}
