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

import java.util.HashMap;
import java.util.Map;

/**
 * CharClass
 */
class CharClass {
    /**
     * CharClass Builder examines all regular expression patterns and splits a set of used
     * characters into classes
     */
    static class Builder extends RegExp.Visitor {

        CharMap charClasses = new CharMap();
        int     numClasses  = 0;

        void visit(RegExp.Nil op) {
            // nothing to do here
        }

        void visit(RegExp.MatchChar op) {
            CharClass cls = (CharClass) charClasses.get(op.chr);
            if (cls != null) {
                if (cls.cardinality == 1)
                    return;
                // "remove" character from the original class
                cls.cardinality--;
            }
            // "put" this character into the new class
            cls = new CharClass();
            cls.cardinality++;
            charClasses.put(op.chr, cls);
        }

        void visit(RegExp.MatchRange op) {
            // If this is the very first range we have encountered and it has a single span of characters
            // (in the typical and at the same time the most heavy case it'll be a range of // "any = [^]" macro)
            // we can bypass the character by character test-and-set as we know they all belong to a single (first)
            // class
            CharSpan[] charSpans = op.range.spans;
            if (charClasses.size() == 0 && charSpans.length == 1) {
                CharSpan span = charSpans[0];
                CharClass newClass = new CharClass();
                newClass.cardinality = span.size();
                for (char c = span.lb; c < span.ub; c++) {
                    charClasses.put(c, newClass);
                }
            } else {
                CharClass  newClass     = null;
                Map        splitClasses = new HashMap();
                // This method is too hot to use CharVisitor, so the character scanning loop is manually
                // inlined and while a nice abstraction is lost, quite a bit of speed is gained.
                for (int i = 0; i < charSpans.length; i++) {
                    for (char c = charSpans[i].lb; c < charSpans[i].ub; c++) {
                        CharClass cls = (CharClass) charClasses.get(c);
                        if (cls == null) {
                            // character has not been classified yet
                            if (newClass == null) {
                                newClass = new CharClass();
                            }
                            newClass.cardinality++;
                            charClasses.put(c, newClass);
                        } else {
                            CharClass splitClass = (CharClass) splitClasses.get(cls);
                            if (splitClass == null) {
                                // have not started splitting this class yet
                                splitClasses.put(cls, splitClass = new CharClass());
                            }
                            // "remove" character from the original class
                            cls.cardinality--;
                            // and "put" it into the split class
                            splitClass.cardinality++;
                            // associate current character with the split class
                            charClasses.put(c, splitClass);
                        }
                    }
                }
            }
        }

        void visit(RegExp.Alt op) {
            op.exp1.accept(this);
            op.exp2.accept(this);
        }

        void visit(RegExp.Cat op) {
            op.exp1.accept(this);
            op.exp2.accept(this);
        }

        void visit(RegExp.Quantified op) {
            op.exp.accept(this);
        }

        void visit(RegExp.Pattern op) {
            op.exp.accept(this);
            op.ctx.accept(this);
        }

        /**
         * CharClassFinalizer counts unique classes and builds ranges of characters for all classes.
         */
        static class CharClassFinalizer extends CharMap.Visitor {
            int counter;

            void visit(char c, Object v) {
                CharClass cls = (CharClass) v;
                if (cls.id < 0) {
                    cls.id = c;
                    cls.range = new CharRange();
                    counter++;
                } else if (cls.id > c) {
                    // this is not really necessary, but it simplifies testing
                    cls.id = c;
                }
                // Note, "addLast" is valid here only because we know that CharMap is really just a fancy array.
                cls.range.addLast(c);
            }
        }

        /**
         * Launches CharClassFinalizer to add characters to the classes they belong to, and count
         * the classes
         */
        private int countClasses() {
            CharClassFinalizer finalizer = new CharClassFinalizer();
            charClasses.accept(finalizer);
            return finalizer.counter;
        }

        CharMap getMapOfClasses() {
            if (numClasses == 0) {
                numClasses = countClasses();
            }
            return charClasses;
        }

        int getNumberOfClasses() {
            if (numClasses == 0) {
                numClasses = countClasses();
            }
            return numClasses;
        }
    }

    /**
     * Class uses code of one of its characters (a "representative") as its ID
     */
    int       id;

    /**
     * Number of characters in the class
     */
    int       cardinality;

    /**
     * The "collection" of characters that belong to this class
     */
    CharRange range;

    CharClass() {
        this.id = -1;
    }

    public boolean equals(Object obj) {
        return obj instanceof CharClass && cardinality == ((CharClass) obj).cardinality && range.equals(((CharClass) obj).range);
    }
}
