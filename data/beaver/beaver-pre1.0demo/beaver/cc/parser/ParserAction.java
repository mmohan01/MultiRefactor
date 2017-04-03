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
abstract class ParserAction implements Comparable {
    Symbol       lookahead;
    ParserAction next;

    ParserAction(Symbol lookahead) {
        this.lookahead = lookahead;
    }

    abstract String descr();

    abstract void accept(Visitor visitor);

    public String toString() {
        return lookahead + " ?";
    }

    public int compareTo(Object object) {
        ParserAction act = (ParserAction) object;
        return lookahead.id - act.lookahead.id;
    }

    static class Shift extends ParserAction {
        ParserState dest;

        Shift(Symbol lookahead, ParserState shiftTo) {
            super(lookahead);
            this.dest = shiftTo;
        }

        String descr() {
            return "shift";
        }

        void accept(Visitor visitor) {
            visitor.visit(this);
        }

        public String toString() {
            return lookahead + " -> " + dest.id;
        }

        public boolean equals(Object object) {
            return object instanceof Shift && ((Shift) object).dest.id == this.dest.id;
        }
    }

    static class Reduce extends ParserAction {
        Production production;

        Reduce(Symbol lookahead, Production prod) {
            super(lookahead);
            this.production = prod;
        }

        String descr() {
            return "reduce " + production;
        }

        void accept(Visitor visitor) {
            visitor.visit(this);
        }

        public String toString() {
            return lookahead + " -> " + production;
        }

        public boolean equals(Object object) {
            return object instanceof Reduce && ((Reduce) object).production.id == this.production.id;
        }
    }

    static class Accept extends ParserAction {

        Accept(Symbol grammarGoal) {
            super(grammarGoal);
        }

        String descr() {
            return "accept " + lookahead;
        }

        void accept(Visitor visitor) {
            visitor.visit(this);
        }

        public String toString() {
            return lookahead + " -> ACCEPT";
        }
    }

    static class Conflict {
        Conflict     next;

        ParserState  state;
        ParserAction action1;
        ParserAction action2;

        Conflict(ParserState state, ParserAction action1, ParserAction action2, Conflict last) {
            this.state = state;
            this.action1 = action1;
            this.action2 = action2;
            this.next = last;
        }

        public String toString() {
            return "on " + action1.lookahead + ": " + action1.descr() + " or " + action2.descr();
        }
    }

    static abstract class Visitor {
        abstract void visit(Shift action);
        abstract void visit(Reduce action);
        abstract void visit(Accept action);
    }
}
