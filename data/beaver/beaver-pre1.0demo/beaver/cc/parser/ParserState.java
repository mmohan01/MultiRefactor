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

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import beaver.cc.parser.ParserAction.Reduce;
import beaver.util.BitSet.BitVisitor;

public class ParserState {
    public static class Builder {
        static class ReduceActionsBuilder implements BitVisitor {
            private Symbol.Terminal[] terminals;
            private ParserState       state;
            private Production        prod;

            ReduceActionsBuilder(Symbol.Terminal[] terminals) {
                this.terminals = terminals;
            }

            void set(ParserState s) {
                state = s;
            }

            void set(Production p) {
                prod = p;
            }

            public void visit(int i) {
                state.add(new Reduce(terminals[i], prod));
            }
        }

        private ParserState last;
        private Map         states = new HashMap();

        public ParserState buildParserStates(Grammar grammar) {
            Item.Set kernel = new Item.Set();
            kernel.getItem(grammar.getGoal().rules[0], 0).addLookahead(grammar.getEOF());
            ParserState firstState = getState(kernel);
            int sid = 0;
            for (ParserState state = firstState; state != null; state = state.next) {
                state.id = sid++;
                state.config.reverseEmitters();
                state.config.resetContributions();
            }
            propagateLookaheads(firstState);
            buildReduceActions(firstState, grammar.terminals);
            firstState.accept = new ParserAction.Accept(grammar.getGoal());
            firstState.numActions++;
            return firstState;
        }

        private ParserState getState(Item.Set kernel) {
            kernel.link();
            ParserState state = (ParserState) states.get(kernel);
            if (state != null) {
                state.config.copyEmitters(kernel);
            } else {
                kernel.buildClosure();
                state = newState(kernel);
                buildShiftsFrom(state);
            }
            return state;
        }

        private ParserState newState(Item.Set config) {
            ParserState state = new ParserState(config);
            if (last == null)
                last = state;
            else
                last = last.next = state;
            states.put(config, state);
            return state;
        }

        private void buildShiftsFrom(ParserState state) {
            state.config.resetContributions();

            for (Item item = state.config.getFirstItem(); item != null; item = item.next) {
                if (item.hasContributed || item.isDotAfterLastSymbol())
                    continue;
                Item.Set kernel = new Item.Set();
                Symbol lookahead = item.getSymbolAfterDot();
                /*
                 * For every item in the "from" state which also has the same symbol after the dot
                 * add the same item to the core under construction but with the dot shifted one
                 * symbol to the right.
                 */
                for (Item x = item; x != null; x = x.next) {
                    if (!x.hasContributed && !x.isDotAfterLastSymbol() && x.getSymbolAfterDot() == lookahead) {
                        kernel.getItem(x.production, x.dot + 1).addEmitter(x);
                        x.hasContributed = true;
                    }
                }
                ParserState shiftTo = getState(kernel);
                /*
                 * The state "shiftTo" is reached from the the current state by a shift action on
                 * the lookahead symbol
                 */
                state.add(new ParserAction.Shift(lookahead, shiftTo));
            }
        }

        private static void propagateLookaheads(ParserState firstState) {
            for (boolean configIsMutating = true; configIsMutating;) {
                configIsMutating = false;
                for (ParserState state = firstState; state != null; state = state.next) {
                    if (state.config.propagateLookaheads()) {
                        configIsMutating = true;
                    }
                }
            }
        }

        private static void buildReduceActions(ParserState firstState, Symbol.Terminal[] terminals) {
            ReduceActionsBuilder reduceActionsbuilder = new ReduceActionsBuilder(terminals);
            for (ParserState state = firstState; state != null; state = state.next) {
                reduceActionsbuilder.set(state);
                for (Item item = state.config.getFirstItem(); item != null; item = item.next) {
                    if (item.isDotAfterLastSymbol() && item.lookaheads != null) {
                        reduceActionsbuilder.set(item.production);
                        item.lookaheads.forEachBitAccept(reduceActionsbuilder);
                    }
                }
            }
        }

        static void makeDefaultReduceActions(ParserState firstState, Grammar grammar) {
            int[] ruleUseCounts = new int[grammar.productions.length];
            for (ParserState state = firstState; state != null; state = state.next) {
                Arrays.fill(ruleUseCounts, 0);
                for (ParserAction.Reduce action = (ParserAction.Reduce) state.firstReduce; action != null; action = (ParserAction.Reduce) action.next) {
                    ruleUseCounts[action.production.id]++;
                }
                int maxUse = Math.max(4, state.numActions / 4); // avoid default reductions in minor
                                                                // cases
                int defaultProductionId = -1;
                for (int i = 0; i < ruleUseCounts.length; i++) {
                    if (ruleUseCounts[i] > maxUse) {
                        maxUse = ruleUseCounts[i];
                        defaultProductionId = i;
                    }
                }
                if (defaultProductionId >= 0) {
                    state.createDefaultReduceAction(defaultProductionId);
                }
            }
        }

        public static void resolveConflicts(ParserState firstState, Collection removalLog) {
            StringBuffer text = new StringBuffer(1024);
            for (ParserState state = firstState; state != null; state = state.next) {
                ParserAction.Conflict conflict = state.resolveConflicts(null, removalLog);
                if (conflict != null) {
                    text.append("Conflict");
                    if (conflict.next != null) {
                        text.append('s');
                    }
                    text.append(" in state ").append(state);
                    for (; conflict != null; conflict = conflict.next) {
                        text.append("  ").append(conflict).append('\n');
                    }
                }
            }
            if (text.length() > 0) {
                throw new IllegalStateException(text.toString());
            }
        }
    }

    ParserState  next;
    int          id;
    Item.Set     config;
    ParserAction firstShift;
    ParserAction firstReduce;
    ParserAction defaultReduce;
    ParserAction accept;
    ParserAction removedShift;
    ParserAction removedReduce;
    int          numActions;

    ParserState(Item.Set kernel) {
        config = kernel;
    }

    void add(ParserAction.Shift action) {
        action.next = firstShift;
        firstShift = action;
        numActions++;
    }

    void add(ParserAction.Reduce action) {
        action.next = firstReduce;
        firstReduce = action;
        numActions++;
    }

    ParserAction.Conflict resolveConflicts(ParserAction.Conflict last, Collection removalLog) {
        last = resolveShiftReduceConflicts(last);
        last = resolveReduceReduceConflicts(last);

        if (removalLog != null && (removedShift != null || removedReduce != null)) {
            removalLog.add(this.toString());
            removedShift = null;
            removedReduce = null;
        }
        return last;
    }

    void createDefaultReduceAction(int productionId) {
        ParserAction.Reduce action = (ParserAction.Reduce) firstReduce;
        ParserAction.Reduce prevAction = null;
        while (action != null) {
            ParserAction.Reduce nextAction = (ParserAction.Reduce) action.next;
            if (action.production.id == productionId) {
                removeReduceAction(action, prevAction);
                action.next = defaultReduce;
                defaultReduce = action;
            } else {
                prevAction = action;
            }
            action = nextAction;
        }
    }

    private ParserAction.Conflict resolveShiftReduceConflicts(ParserAction.Conflict last) {
        if (firstShift != null && firstReduce != null) {
            ParserAction.Shift nextShift;
            ParserAction.Shift prevShift = null;
            nextShift: for (ParserAction.Shift shift = (ParserAction.Shift) firstShift; shift != null; shift = nextShift) {
                nextShift = (ParserAction.Shift) shift.next;

                ParserAction.Reduce nextReduce;
                ParserAction.Reduce prevReduce = null;
                for (ParserAction.Reduce reduce = (ParserAction.Reduce) firstReduce; reduce != null; reduce = nextReduce) {
                    nextReduce = (ParserAction.Reduce) reduce.next;

                    if (shift.lookahead == reduce.lookahead) {
                        ParserAction remove = resolveConflict(shift, reduce);
                        if (remove == shift) {
                            removeShiftAction(shift, prevShift);
                            continue nextShift;
                        }
                        if (remove == reduce) {
                            removeReduceAction(reduce, prevReduce);
                            continue;
                        }
                        last = new ParserAction.Conflict(this, shift, reduce, last);
                    }
                    prevReduce = (ParserAction.Reduce) reduce;
                }
                prevShift = (ParserAction.Shift) shift;
            }
        }
        return last;
    }

    private ParserAction.Conflict resolveReduceReduceConflicts(ParserAction.Conflict last) {
        if (firstReduce != null) {
            ParserAction.Reduce nextReduce1;
            ParserAction.Reduce prevReduce1 = null;
            nextReduce: for (ParserAction.Reduce reduce1 = (ParserAction.Reduce) firstReduce; reduce1 != null; reduce1 = nextReduce1) {
                nextReduce1 = (ParserAction.Reduce) reduce1.next;

                ParserAction.Reduce nextReduce2;
                ParserAction.Reduce prevReduce2 = reduce1;
                for (ParserAction.Reduce reduce2 = (ParserAction.Reduce) reduce1.next; reduce2 != null; reduce2 = nextReduce2) {
                    nextReduce2 = (ParserAction.Reduce) reduce2.next;

                    if (reduce1.lookahead == reduce2.lookahead) {
                        ParserAction remove = resolveConflict(reduce1, reduce2);
                        if (remove == reduce1) {
                            removeReduceAction(reduce1, prevReduce1);
                            continue nextReduce;
                        }
                        if (remove == reduce2) {
                            removeReduceAction(reduce2, prevReduce2);
                            continue;
                        }
                        last = new ParserAction.Conflict(this, reduce1, reduce2, last);
                    }
                    prevReduce2 = (ParserAction.Reduce) reduce2;
                }
                prevReduce1 = (ParserAction.Reduce) reduce1;
            }
        }
        return last;
    }

    private void removeShiftAction(ParserAction action, ParserAction prevAction) {
        if (prevAction == null) {
            firstShift = action.next;
        } else {
            prevAction.next = action.next;
        }
        numActions--;
        action.next = removedShift;
        removedShift = action;
    }

    private void removeReduceAction(ParserAction action, ParserAction prevAction) {
        if (prevAction == null) {
            firstReduce = action.next;
        } else {
            prevAction.next = action.next;
        }
        numActions--;
        action.next = removedReduce;
        removedReduce = action;
    }

    private static ParserAction resolveConflict(ParserAction.Shift shift, ParserAction.Reduce reduce) {
        if (shift.lookahead instanceof Symbol.Terminal) {
            // try precedence declarations first
            Symbol.Terminal lookahead = (Symbol.Terminal) shift.lookahead;
            if (lookahead.precedence > reduce.production.precedence)
                return reduce;
            if (reduce.production.precedence > lookahead.precedence)
                return shift;
            switch (lookahead.associativity) {
                case 'L': {
                    return shift;
                }
                case 'R': {
                    return reduce;
                }
            }
        }
        // prefer shift
        return reduce;
    }

    private static ParserAction resolveConflict(ParserAction.Reduce reduce1, ParserAction.Reduce reduce2) {
        if (reduce1.production.precedence > reduce2.production.precedence)
            return reduce2;
        if (reduce2.production.precedence > reduce1.production.precedence)
            return reduce1;
        return null;
    }

    private static int countStates(ParserState state) {
        int n = 0;
        for (; state != null; state = state.next) {
            n++;
        }
        return n;
    }

    static ParserState[] toArray(ParserState state) {
        ParserState[] states = new ParserState[countStates(state)];
        int i = 0;
        for (; state != null; state = state.next) {
            states[i++] = state;
        }
        return states;
    }

    public String toString() {
        StringBuffer buf = new StringBuffer(500);
        String sep = "------------------------";
        int align = sep.length();
        buf.append(sep).append('\n').append(id).append(":\n");
        if (firstShift != null) {
            buf.append("   shift:\n");
            for (ParserAction action = firstShift; action != null; action = action.next) {
                appendToString(buf, action, align);
            }
        }
        if (removedShift != null) {
            if (firstShift == null) {
                buf.append("   shift:\n");
            }
            for (ParserAction action = removedShift; action != null; action = action.next) {
                buf.append("<<<");
                appendToString(buf, action, align - 3);
            }
        }
        if (firstReduce != null) {
            buf.append("   reduce:\n");
            for (ParserAction action = firstReduce; action != null; action = action.next) {
                appendToString(buf, action, align);
            }
        }
        if (removedReduce != null) {
            if (firstReduce == null) {
                buf.append("   shift:\n");
            }
            for (ParserAction action = removedReduce; action != null; action = action.next) {
                buf.append("<<<");
                appendToString(buf, action, align - 3);
            }
        }
        if (defaultReduce != null) {
            buf.append("   default:\n");
            appendToString(buf, defaultReduce, align);
        }
        if (accept != null) {
            buf.append("   accept:\n");
            appendToString(buf, accept, align);
        }
        return buf.toString();
    }

    private static void appendToString(StringBuffer buf, ParserAction action, int align) {
        String symstr = action.lookahead.toString();
        int strlen = symstr.length();
        for (int i = strlen; i < align; i++) {
            buf.append(' ');
        }
        buf.append(action).append('\n');
    }
}
