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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/**
 *
 */
class NFA {
    Node    start;
    CharSet cset;

    NFA(RegExp re) {
        cset = new CharSet(re);
        Compiler comp = new Compiler(cset);
        re.accept(comp);
        start = comp.getStart();
    }

    static abstract class Node {
        Node    next;
        boolean marked;

        boolean isRemovable() {
            return false;
        }

        void unlink(Node node) {
            if (this.next == node) {
                this.next = node.next;
            }
        }

        void markCtx() {
            // do nothing
        }

        boolean isCtxStart() {
            return false;
        }

        abstract void accept(Visitor v);

        static abstract class Visitor {
            abstract void visit(Fork fork);

            abstract void visit(Char node);

            abstract void visit(Term term);
        }

        static abstract class CtxStart extends Node {
            boolean isCtxStart;

            void markCtx() {
                isCtxStart = true;
            }

            boolean isCtxStart() {
                return isCtxStart;
            }
        }

        static class Char extends CtxStart {
            CharClass[] charClasses;

            Char(CharClass[] classes) {
                charClasses = classes;
            }

            Char(CharClass cls) {
                if (cls == null)
                    throw new IllegalArgumentException("null");
                charClasses = new CharClass[] { cls };
            }

            void accept(Visitor v) {
                v.visit(this);
            }

            public boolean equals(Object obj) {
                return obj instanceof Char && Arrays.equals(charClasses, ((Char) obj).charClasses);
            }
        }

        static class Fork extends CtxStart {
            Node alt;

            void unlink(Node node) {
                super.unlink(node);
                if (alt == node) {
                    alt = node.next;
                }
            }

            boolean isRemovable() {
                return alt == next || alt == this || alt == null;
            }

            void accept(Visitor v) {
                v.visit(this);
            }
        }

        static class Term extends Node {
            RegExp.Pattern.MatchResult patternMatchResult;

            Term(RegExp.Pattern pattern) {
                this.patternMatchResult = pattern.result;
            }

            void accept(Visitor v) {
                v.visit(this);
            }

            public boolean equals(Object obj) {
                return obj instanceof Term && patternMatchResult == ((Term) obj).patternMatchResult;
            }
        }

        static class Array {
            private Node[] nodes = new Node[16];
            private int    count;

            void add(Node node) {
                if (count >= nodes.length) {
                    // expand the backing store
                    Node[] expanded = new Node[nodes.length << 1];
                    System.arraycopy(nodes, 0, expanded, 0, nodes.length);
                    nodes = expanded;
                }
                nodes[count++] = node;
            }

            boolean matches(Node[] otherNodes) {
                if (otherNodes.length != count)
                    return false;

                for (int i = 0; i < otherNodes.length; i++) {
                    if (!otherNodes[i].marked)
                        return false;
                }
                return true;
            }

            void unlink(Node node) {
                for (int i = 0; i < count; i++) {
                    nodes[i].unlink(node);
                }
            }

            void reset() {
                for (int i = 0; i < count; i++) {
                    nodes[i].marked = false;
                }
            }

            void clear() {
                reset();
                count = 0;
            }

            boolean isEmpty() {
                return count == 0;
            }

            Node[] toArray() {
                Node[] array = new Node[count];
                System.arraycopy(nodes, 0, array, 0, count);
                return array;
            }

            void accept(Visitor visitor) {
                for (int i = 0; i < count; i++) {
                    nodes[i].accept(visitor);
                }
            }
        }
    }

    static class Compiler extends RegExp.Visitor {

        static class CharClassCollector extends CharVisitor {
            private CharMap    classes;
            private Collection matches;

            CharClassCollector(CharSet cset) {
                classes = cset.classes;
                matches = new ArrayList(cset.size);
            }

            void reset() {
                matches.clear();
            }

            CharClass[] getCharClasses() {
                return (CharClass[]) matches.toArray(new CharClass[matches.size()]);
            }

            void visit(char c) {
                CharClass cls = (CharClass) classes.get(c);
                if (cls.id == c) {
                    matches.add(cls);
                }
            }
        }

        /**
         * Sometimes it is impossible to set all node fields at the time of construction. Typically
         * happens with forks pointing forward, or branches merging with a main trunk. When a node
         * is added, which these incomplete nodes have to point to, fixes are run to set missing
         * references.
         */
        static abstract class NodeFinalizer {
            NodeFinalizer next;

            abstract void applyTo(Node node);

            static class MergeBranch extends NodeFinalizer {
                private Node branchLastNode;

                MergeBranch(Node node) {
                    branchLastNode = node;
                }

                void applyTo(Node node) {
                    branchLastNode.next = node;
                }
            }

            static class SetMultiForwardForkTarget extends NodeFinalizer {
                private Node.Fork[] forks;

                SetMultiForwardForkTarget(Node.Fork[] forks) {
                    this.forks = forks;
                }

                void applyTo(Node node) {
                    for (int i = 0; i < forks.length; i++) {
                        forks[i].alt = node;
                    }
                }
            }

            static class SetSingleForwardForkTarget extends NodeFinalizer {
                private Node.Fork fork;

                SetSingleForwardForkTarget(Node.Fork fork) {
                    this.fork = fork;
                }

                void applyTo(Node node) {
                    fork.alt = node;
                }
            }

            static class SetPatternFirstNode extends NodeFinalizer {
                private RegExp.Pattern pattern;

                SetPatternFirstNode(RegExp.Pattern pattern) {
                    this.pattern = pattern;
                }

                void applyTo(Node node) {
                    pattern.firstNFANode = node;
                }
            }

            static class MarkCtx extends NodeFinalizer {
                void applyTo(Node node) {
                    node.markCtx();
                }
            }
        }

        private Node               first;
        private Node               last;
        private NodeFinalizer      firstFinalizer;
        private NodeFinalizer      lastFinalizer;

        private Node.Array         nodes;
        private CharMap            classes;
        private CharClassCollector charClassCollector;

        Compiler(CharSet cset) {
            nodes = new Node.Array();
            classes = cset.classes;
            charClassCollector = new CharClassCollector(cset);
        }

        private Compiler(Compiler comp) {
            this.nodes = comp.nodes;
            this.classes = comp.classes;
            this.charClassCollector = comp.charClassCollector;
        }

        private void addFinalizers(Compiler comp) {
            if (comp.firstFinalizer != null) {
                if (firstFinalizer == null) {
                    firstFinalizer = comp.firstFinalizer;
                    lastFinalizer = comp.lastFinalizer;
                } else {
                    lastFinalizer.next = comp.firstFinalizer;
                    lastFinalizer = comp.lastFinalizer;
                }
            }
        }

        private void addFinalizer(NodeFinalizer finalizer) {
            if (lastFinalizer == null) {
                lastFinalizer = firstFinalizer = finalizer;
            } else {
                lastFinalizer = lastFinalizer.next = finalizer;
            }
        }

        private void add(Node node) {
            if (last == null) {
                last = first = node;
            } else {
                last = last.next = node;

                if (firstFinalizer != null) {
                    for (NodeFinalizer fin = firstFinalizer; fin != null; fin = fin.next) {
                        fin.applyTo(node);
                    }
                    lastFinalizer = firstFinalizer = null;
                }
            }
            nodes.add(node);
        }

        private void optimize(Node.Fork fork) {
            if (fork.next == null) {
                fork.next = fork.alt;
                fork.alt = null;
            }
            fork.next = optimize(fork.next);
            fork.alt = optimize(fork.alt);
        }

        private Node optimize(Node start) {
            for (Node node = start; node != null && !node.marked; node = node.next) {
                node.marked = true;

                if (node instanceof Node.Fork) {
                    optimize((Node.Fork) node);
                    while (node.isRemovable()) {
                        nodes.unlink(node);
                        if (node == start) {
                            start = node.next;
                        }
                        node = node.next;
                    }
                    break;
                } else if (node instanceof Node.Term) {
                    node.next = null;
                }
            }
            return start;
        }

        Node getStart() {
            first = optimize(first);
            nodes.reset();
            return first;
        }

        void visit(RegExp.Nil op) {
            // nothing to do here
        }

        void visit(RegExp.MatchChar op) {
            this.add(new Node.Char((CharClass) classes.get(op.chr)));
        }

        void visit(RegExp.MatchRange op) {
            charClassCollector.reset();
            op.range.accept(charClassCollector);
            this.add(new Node.Char(charClassCollector.getCharClasses()));
        }

        void visit(RegExp.Alt op) {
            Node.Fork fork = new Node.Fork();
            this.add(fork);
            op.exp1.accept(this);

            Compiler comp = new Compiler(this);
            op.exp2.accept(comp);
            fork.alt = comp.first;

            addFinalizers(comp);
            addFinalizer(new NodeFinalizer.MergeBranch(comp.last));
        }

        void visit(RegExp.Cat op) {
            op.exp1.accept(this);
            op.exp2.accept(this);
        }

        void visit(RegExp.Quantified op) {
            if (op.max > op.min) {
                Node.Fork[] forwardForks = new Node.Fork[op.max - op.min];
                for (int i = 0; i < forwardForks.length; i++) {
                    this.add(forwardForks[i] = new Node.Fork());
                    op.exp.accept(this);
                }
                addFinalizer(new NodeFinalizer.SetMultiForwardForkTarget(forwardForks));
            }

            if (op.max < 0) {
                Node.Fork over = null;
                if (op.min == 0) {
                    this.add(over = new Node.Fork());
                }
                Node lastNode = last;
                op.exp.accept(this);

                Node.Fork back = new Node.Fork();
                this.add(back);
                back.alt = lastNode != null ? lastNode.next : first;

                if (over != null) {
                    addFinalizer(new NodeFinalizer.SetSingleForwardForkTarget(over));
                }
            }

            for (int i = 1; i < op.min; i++) {
                op.exp.accept(this);
            }
        }

        public void visit(RegExp.Pattern op) {
            addFinalizer(new NodeFinalizer.SetPatternFirstNode(op));
            op.exp.accept(this);
            addFinalizer(new NodeFinalizer.MarkCtx());
            op.ctx.accept(this);
            add(new Node.Term(op));
        }
    }
}
