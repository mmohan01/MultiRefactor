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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import beaver.cc.parser.Symbol.Terminal;
import beaver.util.BitSet;

/**
 *
 */
class Item implements Comparable {
    Production production;
    int        dot;
    BitSet     lookaheads;

    /**
     * Items may initially be missing some symbols from their lookahead sets. Acceptors are the
     * items that would need to be updated if new symbols are added to this item lookahead sets.
     */
    Collection acceptors;

    /**
     * Items may initially be missing some symbols from their lookahead sets. Emitters are the items
     * that might contribute new lookahead symbols to this item.
     */
    Collection emitters;

    /**
     * A flag to mark items that have already contributed to acceptors.
     */
    boolean    hasContributed;

    /**
     * Items in a set are linked
     */
    Item       next;

    Item(Production rule, int dot) {
        this.production = rule;
        this.dot = dot;
    }

    void become(Production rule, int dot) {
        this.production = rule;
        this.dot = dot;
    }

    boolean isDotAfterLastSymbol() {
        return dot == production.rhs.length;
    }

    Symbol getSymbolAfterDot() {
        return production.rhs[dot].symbol;
    }

    void addLookahead(Terminal term) {
        if (lookaheads == null)
            lookaheads = new BitSet(production.lhs.firstSet.capacity());

        lookaheads.add(term.id);
    }

    boolean addLookaheads(BitSet terms) {
        if (lookaheads == null)
            lookaheads = new BitSet(production.lhs.firstSet.capacity());

        return lookaheads.add(terms);
    }

    /**
     * Adds lookahead symbols from a given item.
     *
     * @return true if all RHS parts were null-able nonterminals and hence the lookahead set needs
     *         to be expanded by propagating terminals.
     */
    boolean addLookaheadsFrom(Item item) {
        if (lookaheads == null)
            lookaheads = new BitSet(production.lhs.firstSet.capacity());

        Production.RHSElement[] rhs = item.production.rhs;
        for (int i = item.dot + 1; i < rhs.length; i++) {
            Symbol rhsSymbol = rhs[i].symbol;

            if (rhsSymbol instanceof Terminal)
                lookaheads.add(rhsSymbol.id);
            else
                lookaheads.add(((Symbol.NonTerminal) rhsSymbol).firstSet);

            if (!rhsSymbol.canMatchEmptyString())
                return false;
        }
        return true;
    }

    void addAcceptor(Item item) {
        if (acceptors == null)
            acceptors = new ArrayList();

        acceptors.add(item);
    }

    void addEmitter(Item item) {
        if (emitters == null)
            emitters = new ArrayList();

        emitters.add(item);
    }

    void copyEmittersOf(Item item) {
        if (item.emitters != null) {
            if (emitters == null)
                emitters = new ArrayList(item.emitters);
            else
                emitters.addAll(item.emitters);
        }
    }

    void reverseEmitters() {
        if (emitters != null) {
            for (Iterator iter = emitters.iterator(); iter.hasNext();) {
                Item item = (Item) iter.next();
                item.addAcceptor(this);
            }
            emitters = null;
        }
    }

    /**
     * Propagates this item lookaheads to acceptor items
     *
     * @return true if this item lookaheads were propagated to acceptor items
     */
    boolean propagateLookaheads() {
        boolean propagated = false;
        if (acceptors != null) {
            for (Iterator i = acceptors.iterator(); i.hasNext();) {
                Item item = (Item) i.next();
                if (item.addLookaheads(lookaheads)) {
                    propagated = true;
                    /*
                     * the item that accepted new lookaheads may need to contribute them to other
                     * items
                     */
                    item.hasContributed = false;
                }
            }
        }
        return propagated;
    }

    public int hashCode() {
        return production.id * 37 ^ dot;
    }

    public boolean equals(Object o) {
        if (o == this) {
            return true;
        } else if (o instanceof Item) {
            Item c = (Item) o;
            return this.production == c.production && this.dot == c.dot;
        } else {
            return false;
        }
    }

    public int compareTo(Object o) {
        if (o == this)
            return 0;

        Item item = (Item) o;

        int cmp = this.production.id - item.production.id;
        if (cmp == 0) {
            cmp = this.dot - item.dot;
        }
        return cmp;
    }

    public String toString() {
        String repr = production.lhs + " =";
        for (int i = 0; i < production.rhs.length; i++) {
            if (i == dot) {
                repr += " *";
            }
            repr += " " + production.rhs[i];
        }
        if (dot == production.rhs.length) {
            repr += " *";
        }
        return repr;
    }

    static class Set {
        private Map  items = new HashMap();
        private Item probe = new Item(null, 0);
        private Item firstItem;
        private Item lastItem;
        private int  kernelSize;
        private int  kernelHash;

        Item getItem(Production rule, int dot) {
            probe.become(rule, dot);
            Item item = (Item) items.get(probe);
            if (item == null) {
                add(item = new Item(rule, dot));
            }
            return item;
        }

        void link() {
            Item[] kernel = (Item[]) items.values().toArray(new Item[items.size()]);
            Arrays.sort(kernel);
            /*
             * Link all items and calculate kernel hash code, while we link kernel items
             */
            firstItem = lastItem = kernel[0];
            kernelHash = firstItem.hashCode();
            for (int i = 1; i < kernel.length; i++) {
                lastItem = lastItem.next = kernel[i];
                kernelHash = kernelHash * 571 ^ lastItem.hashCode();
            }
            lastItem.next = null;
            kernelSize = kernel.length;
        }

        void buildClosure() {
            for (Item item = firstItem; item != null; item = item.next) {
                if (!item.isDotAfterLastSymbol()) {
                    Symbol symbol = item.getSymbolAfterDot();
                    if (symbol instanceof Symbol.NonTerminal) {
                        Production[] ntRules = ((Symbol.NonTerminal) symbol).rules;
                        for (int i = 0; i < ntRules.length; i++) {
                            Item newItem = getItem(ntRules[i], 0);
                            if (newItem.addLookaheadsFrom(item)) {
                                item.addAcceptor(newItem);
                            }
                        }
                    }
                }
            }
        }

        void resetContributions() {
            for (Item item = firstItem; item != null; item = item.next) {
                item.hasContributed = false;
            }
        }

        void copyEmitters(Item.Set set) {
            if (set.kernelSize != kernelSize)
                throw new IllegalArgumentException("unequal kernels");

            Item src = set.firstItem;
            Item dst = firstItem;
            for (int n = kernelSize; n > 0; n--) {
                dst.copyEmittersOf(src);

                src = src.next;
                dst = dst.next;
            }
        }

        void reverseEmitters() {
            for (Item item = firstItem; item != null; item = item.next) {
                item.reverseEmitters();
            }
        }

        boolean propagateLookaheads() {
            boolean propagated = false;
            for (Item item = firstItem; item != null; item = item.next) {
                if (!item.hasContributed) {
                    if (item.propagateLookaheads()) {
                        propagated = true;
                    }
                    item.hasContributed = true;
                }
            }
            return propagated;
        }

        Item getFirstItem() {
            return firstItem;
        }

        private void add(Item item) {
            items.put(item, item);
            if (lastItem != null) { // there is no need to link items while we build the kernel
                lastItem = lastItem.next = item;
            }
        }

        boolean equals(Item.Set other) {
            if (kernelSize != other.kernelSize)
                return false;

            Item myItem = firstItem, otherItem = other.firstItem;
            for (int n = kernelSize; n > 0; n--, myItem = myItem.next, otherItem = otherItem.next) {
                if (!myItem.equals(otherItem))
                    return false;
            }
            return true;
        }

        public boolean equals(Object o) {
            return o == this || o instanceof Item.Set && this.equals((Item.Set) o);
        }

        public int hashCode() {
            return kernelHash;
        }

        public String toString() {
            String repr = "";
            if (firstItem != null) {
                repr = "  " + firstItem.toString();
                int n = 1;
                for (Item item = firstItem.next; item != null; item = item.next) {
                    repr += "\n";
                    if (++n <= kernelSize) {
                        repr += "  ";
                    } else {
                        repr += "+ ";
                    }
                    repr += item;
                }
            }
            return repr;
        }
    }
}
