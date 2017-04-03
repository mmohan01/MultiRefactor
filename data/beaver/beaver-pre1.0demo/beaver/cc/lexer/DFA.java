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

import beaver.cc.lexer.NFA.Node;

/**
 *
 */
public class DFA {
    int   numStates;
    State start;

    DFA(NFA nfa) {
        State.Builder stateBuilder = new State.Builder();
        stateBuilder.buildDFAStates(nfa);
        start = stateBuilder.getStartState();
        numStates = stateBuilder.getNumberOfStates();
    }

    public DFA(RegExp re) {
        this(new NFA(re));
    }

    public String toString() {
        String text = "";
        for (State st = start; st != null; st = st.next) {
            text += st.toString() + "\n";
        }
        return text;
    }

    static class State {
        State                      next;
        State                      link;
        NFA.Node[]                 kernel;
        RegExp.Pattern.MatchResult patternMatchResult;
        boolean                    isPreCtx;
        Transition                 firstTransition;
        int                        numTransitions;
        int						   sccDepth;
        int                        id;

        State(NFA.Node[] kernel, boolean isPreCtx) {
            this.kernel = kernel;
            this.isPreCtx = isPreCtx;
        }

        public String toString() {
            String str = id + ": = ";
            if (patternMatchResult != null) {
                str += patternMatchResult.id;
            }
            if (firstTransition != null) {
                str += "\n  depth = " + sccDepth;
            }
            for (DFA.State.Transition tr = firstTransition; tr != null; tr = tr.next) {
                str += "\n    " + tr;
            }
            return str;
        }

        void addTransition(CharRange onChars, State toState) {
            firstTransition = new Transition(onChars, toState, firstTransition);
            numTransitions++;
        }

        boolean hasTransition(Transition t) {
            for (Transition tr = firstTransition; tr != null; tr = tr.next) {
                if (tr.equals(t))
                    return true;
            }
            return false;
        }

        boolean isTheSameAs(State st) {
            if (isPreCtx != st.isPreCtx || numTransitions != st.numTransitions || !RegExp.Pattern.MatchResult.equals(patternMatchResult, st.patternMatchResult))
                return false;
            for (Transition tr = firstTransition; tr != null; tr = tr.next) {
                if (!st.hasTransition(tr))
                    return false;
            }
            return true;
        }

        void compactTransitions() {
            for (Transition tr = firstTransition; tr != null; tr = tr.next) {
                CharRange expandedRange = null;
                for (Transition test = tr.next, prev = tr; test != null; test = test.next) {
                    if (test.toState == tr.toState) {
                        if (expandedRange == null) {
                            expandedRange = new CharRange();
                            expandedRange.add(tr.onChars);
                            tr.onChars = expandedRange;
                        }
                        tr.onChars.add(test.onChars);
                        prev.next = test.next;
                        numTransitions--;
                    } else {
                        prev = test;
                    }
                }
            }
        }

        void replaceDestinations(State orig, State dest) {
            for (Transition tr = firstTransition; tr != null; tr = tr.next) {
                if (tr.toState == orig) {
                    tr.toState = dest;
                }
            }
        }

        void markReachable() {
            if (id >= 0) {
                id = -1;
                for (Transition tr = firstTransition; tr != null; tr = tr.next) {
                    tr.toState.markReachable();
                }
            }
        }

        static class Transition {
            Transition next;
            CharRange  onChars;
            State      toState;

            Transition(CharRange onChars, State toState, Transition next) {
                this.onChars = onChars;
                this.toState = toState;
                this.next = next;
            }

            boolean equals(Transition t) {
                return this.toState == t.toState && onChars.equals(t.onChars);
            }

            public String toString() {
                return "[" + onChars + "] -> " + toState.id;
            }
        }

        static class TransitionsCollector extends NFA.Node.Visitor {
            CharMap                    transitions = new CharMap();
            RegExp.Pattern.MatchResult patternMatchResult;

            public void visit(Node.Fork fork) {
                // there are no forks in a DFA kernel
            }

            public void visit(Node.Char node) {
                for (int i = 0; i < node.charClasses.length; i++) {
                    CharClass cls = node.charClasses[i];

                    Node.Array charTransitions = (Node.Array) transitions.get((char) cls.id);
                    if (charTransitions == null) {
                        transitions.put((char) cls.id, charTransitions = new Node.Array());
                    }
                    charTransitions.add(node.next);
                }
            }

            public void visit(Node.Term term) {
                if (patternMatchResult == null || patternMatchResult.precedence < term.patternMatchResult.precedence) {
                    patternMatchResult = term.patternMatchResult;
                }
            }

            void reset() {
                transitions.clear();
                patternMatchResult = null;
            }
        }

        static class Closure extends NFA.Node.Visitor {
            private NFA.Node.Array kernel = new NFA.Node.Array();
            private boolean        foundCtxNode;

            void visit(NFA.Node.Char node) {
                if (!node.marked) {
                    if (!foundCtxNode && node.isCtxStart()) {
                        foundCtxNode = true;
                    }
                    node.marked = true;
                    kernel.add(node);
                }
            }

            void visit(NFA.Node.Fork node) {
                if (!foundCtxNode && node.isCtxStart()) {
                    foundCtxNode = true;
                }
                node.next.accept(this);
                node.alt.accept(this);
            }

            void visit(NFA.Node.Term node) {
                if (!node.marked) {
                    node.marked = true;
                    kernel.add(node);
                }
            }

            boolean kernelMatches(NFA.Node[] kernel) {
                return this.kernel.matches(kernel);
            }

            void reset() {
                kernel.clear();
                foundCtxNode = false;
            }

            NFA.Node[] getKernel() {
                return kernel.toArray();
            }

            boolean isPreCtx() {
                return foundCtxNode;
            }
        }

        static class SCCFinder {
        	State[]	stack;
        	int		top;

        	SCCFinder(int numStates) {
        		stack = new State[numStates];
        	}

			void traverse(State st) {
				stack[top++] = st;
				int d = st.sccDepth = top;

				for (Transition tr = st.firstTransition; tr != null; tr = tr.next) {
					State dest = tr.toState;
					if (dest.sccDepth == 0) {
						traverse(dest);
					}
					if (dest.sccDepth < st.sccDepth) {
						st.sccDepth = dest.sccDepth;
					}
				}

				if (st.sccDepth == d) {
					do {
						stack[--top].sccDepth = Integer.MAX_VALUE;
						stack[top].link = st;
					} while (stack[top] != st);
				}
			}
        }

        static class Builder {
            State first;
            State last;
            State work;
            int   numStates;

            State getStartState() {
                return first;
            }

            int getNumberOfStates() {
                return numStates;
            }

            void buildDFAStates(NFA nfa) {
                createStates(nfa);
                removeUnreachable();
                compactTransitions();
//                removeDuplicateStates();
                minimize();
                compactTransitions();
                findSCCs();
                assignStateIds();
            }

            private void reset() {
                last = null;
                numStates = 0;
                work = null;
            }

            State getWorkState() {
                State st = work;
                work = st.link;
                st.link = null;
                return st;
            }

            private State addState(Closure closure) {
                State state = new State(closure.getKernel(), closure.isPreCtx());
                numStates++;
                if (last == null)
                    last = state;
                else
                    last = last.next = state;

                state.link = work;
                work = state;

                return state;
            }

            private State getState(Closure closure) {
                State state = findState(closure);
                if (state == null) {
                    state = addState(closure);
                }
                return state;
            }

            private State findState(Closure closure) {
                for (State state = first; state != null; state = state.next) {
                    if (closure.kernelMatches(state.kernel)) {
                        return state;
                    }
                }
                return null;
            }

            private void createStates(final NFA nfa) {
                final TransitionsCollector transitionsCollector = new TransitionsCollector();
                final Closure closure = new Closure();

                reset();

                nfa.start.accept(closure);
                first = addState(closure);

                while (work != null) {
                    final State st = getWorkState();

                    for (int i = 0; i < st.kernel.length; i++) {
                        st.kernel[i].accept(transitionsCollector);
                    }
                    st.patternMatchResult = transitionsCollector.patternMatchResult;

                    transitionsCollector.transitions.accept(new CharMap.Visitor() {
                        public void visit(char key, Object value) {
                            closure.reset();
                            ((NFA.Node.Array) value).accept(closure);
                            st.addTransition(((CharClass) nfa.cset.classes.get(key)).range, getState(closure));
                        }
                    });
                    transitionsCollector.reset();
                }
            }

            private void removeUnreachable() {
                first.markReachable();
                for (State st = first.next, ps = first; st != null; st = st.next) {
                    if (st.id >= 0) { // not marked
                        ps.next = st.next;
                        numStates--;
                    } else {
                        ps = st;
                    }
                }
            }

            void compactTransitions() {
                for (State st = first; st != null; st = st.next) {
                    st.compactTransitions();
                }
            }

            void removeDuplicateStates() {
                for (State s = first; s != null; s = s.next) {
                    State t = s.next, p = s;
                    while (t != null) {
                        if (t.isTheSameAs(s)) {
                            // remove t from the list
                            p.next = t.next;
                            numStates--;
                            // replace references to t with s
                            replace(t, s);
                            System.out.println("-- " + t);
                        } else {
                            p = t;
                        }
                        t = t.next;
                    }
                }
            }

            void replace(State orig, State dest) {
                for (State st = first; st != null; st = st.next) {
                    st.replaceDestinations(orig, dest);
                }
            }

            void minimize() {
                // create initial partition
                State[] sets = new State[numStates];

                int lastSet = -1;
                for (State st = first; st != null; st = st.next) {
                    if (st.patternMatchResult != null && st.firstTransition == null) {
                        int i = 0;
                        for (; i <= lastSet; i++) {
                            State x = sets[i];
                            if (st.patternMatchResult == x.patternMatchResult)
                                break;
                        }
                        st.id = i;
                        st.link = sets[i];
                        sets[i] = st;

                        if (lastSet < i) {
                            lastSet = i;
                        }
                    }
                }

                int firstSet = lastSet + 1;
                for (State st = first; st != null; st = st.next) {
                    if (st.patternMatchResult != null && st.firstTransition != null) {
                        int i = firstSet;
                        for (; i <= lastSet; i++) {
                            State x = sets[i];
                            if (st.patternMatchResult == x.patternMatchResult)
                                break;
                        }
                        st.id = i;
                        st.link = sets[i];
                        sets[i] = st;

                        if (lastSet < i) {
                            lastSet = i;
                        }
                    }
                }

                int nextSet = lastSet + 1;
                for (State st = first; st != null; st = st.next) {
                    if (st.patternMatchResult == null) {
                        st.id = nextSet;
                        st.link = sets[nextSet];
                        sets[nextSet] = st;

                        if (lastSet < nextSet) {
                            lastSet = nextSet;
                        }
                    }
                }

                // partition existing sets into subsets
                while (lastSet == nextSet) {
                    for (int i = firstSet; i <= lastSet; i++) {
                        nextSet = lastSet + 1;

                        State ref = sets[i], st = ref.link, ps = ref;
                        while (st != null) {
                            if (!st.isTheSameAs(ref)) {
                                lastSet = nextSet;

                                ps.link = st.link;
                                st.id = nextSet;
                                st.link = sets[nextSet];
                                sets[nextSet] = st;

                                st = ps.link;
                            } else {
                                ps = st;
                                st = st.link;
                            }
                        }
                    }
                }

                // coalesce states of each subset
                for (int i = 0; i <= lastSet; i++) {
                    for (Transition tr = sets[i].firstTransition; tr != null; tr = tr.next) {
                        tr.toState = sets[tr.toState.id];
                    }
                }

                // mark "extra" states
                for (int i = 0; i <= lastSet; i++) {
                    for (State st = sets[i].link; st != null; st = st.link) {
                        st.id = -1;
                    }
                }

                // remove "extra" states
                for (State ps = first, st = ps.next; st != null; st = st.next) {
                    if (st.id < 0) {
                        ps.next = st.next;
                        numStates--;
                    } else {
                        ps = st;
                    }
                }
            }

			void findSCCs() {
				for (State st = first; st != null; st = st.next) {
					st.sccDepth = 0;
					st.link = null;
				}
				SCCFinder scc = new SCCFinder(numStates);
				for (State st = first; st != null; st = st.next) {
					if (st.sccDepth == 0) {
						scc.traverse(st);
					}
				}
				// Mark non-key states
				for (State st = first.next; st != null; st = st.next) {
					if (!isKeySCCState(st)) {
						st.link = null;
					}
				}
				for (State st = first; st != null; st = st.next) {
					st.sccDepth = Integer.MAX_VALUE;
				}
				// Find max number of transitions before a key state is reached
				for (State st = first; st != null; st = st.next) {
					int d = findSccDepth(st);
					if (st.firstTransition != null && (d == 0 || d == Integer.MAX_VALUE)) {
						throw new IllegalStateException("unexpected SCC depth in a non-final state");
					}
				}
			}

			boolean isKeySCCState(State st) {
				if (st.link != st)
					return true;

				for (Transition tr = st.firstTransition; tr != null; tr = tr.next) {
					if (tr.toState.link == st)
						return true;
				}
				return false;
			}

			int findSccDepth(State st) {
				if (st.sccDepth == Integer.MAX_VALUE) {
					int mm = 0;
					for (Transition tr = st.firstTransition; tr != null; tr = tr.next) {
						State dest = tr.toState;
						int m = 1;
						if (dest.link == null) {
							// marked as a non-key state
							m += findSccDepth(dest);
						}
						if (mm < m) {
							mm = m;
						}
					}
					st.sccDepth = mm;
				}
				return st.sccDepth;
			}

            void assignStateIds() {
                int id = 0;
                for (State st = first; st != null; st = st.next) {
                    st.id = id++;
                }
                if (id != numStates)
                    throw new IllegalStateException("Wrong number of states");
            }
        }
    }
}
