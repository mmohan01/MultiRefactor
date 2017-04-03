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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import beaver.util.BitSet;

/**
 * Grammar represents "the set of structural rules that govern the composition of sentences,
 * phrases, and words in any given (natural) language."
 */
public class Grammar {
    /**
     * Rules to derive nonterminal symbols.
     */
    Production[]         productions;
    /**
     * Symbols that are created by a scanner and represent input tokens for the parser.
     */
    Symbol.Terminal[]    terminals;
    /**
     * Symbols that are created by the parser, when RHS of a production is reduced to LHS.
     */
    Symbol.NonTerminal[] nonterminals;

    public Grammar(Production[] productions) {
        this.productions = productions;
        this.nonterminals = collectNonterminals(productions);
        this.terminals = collectTerminals(productions);

        assignIDs();
        markNullableNonTerminals();
        buildFirstSets();
        assignPrecedences();

        // data discovered below are needed for parser code generation
        markStaticNonTerminals();
        markListProducers();
        findDelegates();
    }

    private static Symbol.NonTerminal[] collectNonterminals(Production[] productions) {
        int cnt = 0;
        Map nts = new HashMap();
        for (int i = 0; i < productions.length; i++) {
			if (!nts.containsKey(productions[i].lhs)) {
				nts.put(productions[i].lhs, new Integer(cnt++));
			}
		}
        Symbol.NonTerminal[] nonterminals = new Symbol.NonTerminal[cnt];
        for (Iterator i = nts.entrySet().iterator(); i.hasNext();) {
			Map.Entry e = (Map.Entry) i.next();
			int idx = ((Integer) e.getValue()).intValue();
	        if (nonterminals[idx] != null) {
	        	throw new IllegalStateException(e.getKey() + " slot is used by " + nonterminals[idx]);
	        }
			nonterminals[idx] = (Symbol.NonTerminal) e.getKey();
		}
        return nonterminals;
    }

    private static Symbol.Terminal[] collectTerminals(Production[] productions) {
        Map terms = new HashMap();
        int lastKeywordId = 0; // need to add later the (always present) EOF with ID=0
        int lastTokenId = Character.MAX_VALUE;
        Symbol.Terminal error = null;
        for (int i = 0; i < productions.length; i++) {
        	Production.RHSElement[] rhs = productions[i].rhs;
        	for (int j = 0; j < rhs.length; j++) {
        		Symbol sym = rhs[j].symbol;
    			if (sym instanceof Symbol.Terminal && !terms.containsKey(sym)) {
    				Symbol.Terminal term = (Symbol.Terminal) sym;
    				if ("ERROR".equals(term.name)) {
    					// special case - should be the last one in the list
    					error = term;
    				} else if (term.isKeyword()) {
    					// keywords are added first
    					terms.put(term, new Integer(++lastKeywordId));
    				} else {
    					terms.put(term, new Integer(++lastTokenId));
    				}
    			}
			}
		}
        int numTerms = 1 + lastKeywordId + (lastTokenId - Character.MAX_VALUE);
        if (error != null) {
        	numTerms++;
        }
        Symbol.Terminal[] terminals = new Symbol.Terminal[numTerms];
        terminals[0] = new Symbol.Terminal("EOF");
        if (error != null) {
            terminals[numTerms - 1] = error;
        }
        for (Iterator i = terms.entrySet().iterator(); i.hasNext();) {
			Map.Entry e = (Map.Entry) i.next();
			int idx = ((Integer) e.getValue()).intValue();
			if (idx > Character.MAX_VALUE) {
				idx -= Character.MAX_VALUE - lastKeywordId;
			}
	        if (terminals[idx] != null) {
	        	throw new IllegalStateException(e.getKey() + " slot is used by " + terminals[idx]);
	        }
			terminals[idx] = (Symbol.Terminal) e.getKey();
		}
        return terminals;
    }

    private void assignIDs() {
        int sid = 0;
        for (int i = 0; i < terminals.length; i++) {
            terminals[i].id = sid++;
        }
        for (int i = 0; i < nonterminals.length; i++) {
            nonterminals[i].id = sid++;
        }
        for (int i = 0; i < productions.length; i++) {
            productions[i].id = i;
        }
    }

    private void findDelegates() {
        for (int i = 0; i < nonterminals.length; i++) {
            Symbol.NonTerminal nt = nonterminals[i];
            Symbol delegate = null;
            boolean delegateIsFound = false;
            for (int j = 0; j < nt.rules.length; j++) {
                Production.RHSElement[] rhs = nt.rules[j].rhs;
                Symbol value = null;
                boolean multiValueRhs = false;
                for (int k = 0; k < rhs.length; k++) {
                    if (!rhs[k].symbol.isKeyword()) {
                        if (value == null) {
                            value = rhs[k].symbol;
                        } else {
                            multiValueRhs = true;
                            break;
                        }
                    }
                }
                if (multiValueRhs) {
                    // there cannot be a delegate for this non-terminal
                    delegateIsFound = false;
                    break;
                } else if (value != null) {
                    if (delegate == null) {
                        delegate = value;
                        delegateIsFound = true;
                    } else if (delegate != value) {
                        delegateIsFound = false;
                        break;
                    }
                }
            }
            if (delegateIsFound) {
                nt.delegate = delegate;
            }
        }
        // break chains of delegates
        boolean mutating;
        do {
            mutating = false;
            for (int i = 0; i < nonterminals.length; i++) {
                Symbol.NonTerminal nt = nonterminals[i];
                while (nt.delegate instanceof Symbol.NonTerminal && ((Symbol.NonTerminal) nt.delegate).delegate != null) {
                    nt.delegate = ((Symbol.NonTerminal) nt.delegate).delegate;
                    mutating = true;
                }
            }
        } while (mutating);
    }

    private void markListProducers() {
        for (int i = 0; i < nonterminals.length; i++) {
            nonterminals[i].checkAndSetListAttributes();
        }
    }

    private void markStaticNonTerminals() {
        boolean mutating;
        do {
            mutating = false;
            for (int i = 0; i < nonterminals.length; i++) {
                Symbol.NonTerminal nt = nonterminals[i];
                if (!nt.isStatic) {
                	boolean ntMatchesKeywordsOnly = true;
                    for (int j = 0; j < nt.rules.length; j++) {
                        if (!nt.rules[j].matchesKeywordsOnly()) {
                        	ntMatchesKeywordsOnly = false;
                            break;
                        }
                    }
                    if (ntMatchesKeywordsOnly) {
                    	nt.isStatic = true;
                    	mutating = true;
                    }
                }
            }
        } while (mutating);
    }


    /**
     * Checks nonterminal definitions. If the latter includes a production that can match an empty
     * string, the nonterminal is marked as such too.
     */
    private void markNullableNonTerminals() {
        boolean mutating;
        do {
            mutating = false;
            for (int i = 0; i < nonterminals.length; i++) {
                Symbol.NonTerminal nt = nonterminals[i];
                if (!nt.canMatchEmptyString) {
                    for (int j = 0; j < nt.rules.length; j++) {
                        if (nt.rules[j].canMatchEmptyString()) {
                            nt.canMatchEmptyString = true;
                            mutating = true;
                            break;
                        }
                    }
                }
            }
        } while (mutating);
    }

    private void buildFirstSets() {
        /*
         * Setup
         */
        for (int i = 0; i < nonterminals.length; i++) {
            nonterminals[i].firstSet = new BitSet(terminals.length);
        }
        /*
         * Create "first generation" of first set terminals
         */
        for (int i = 0; i < productions.length; i++) {
            Symbol.NonTerminal lhs = productions[i].lhs;
            Production.RHSElement[] rhs = productions[i].rhs;

            for (int j = 0; j < rhs.length; j++) {
                Symbol symbol = rhs[j].symbol;
                if (symbol instanceof Symbol.Terminal) {
                    lhs.firstSet.add(symbol.id);
                } else {
                    Symbol.NonTerminal nt = (Symbol.NonTerminal) symbol;
                    if (nt != lhs) {
                        lhs.firstSet.add(nt.firstSet);
                    }
                }
                if (!symbol.canMatchEmptyString()) {
                    break;
                }
            }
        }
        /*
         * Keep adding terminals from leading nonterminals
         */
        boolean firstSetsAreMutating;
        do {
            firstSetsAreMutating = false;
            for (int i = 0; i < productions.length; i++) {
                Symbol.NonTerminal lhs = productions[i].lhs;
                Production.RHSElement[] rhs = productions[i].rhs;
                for (int j = 0; j < rhs.length; j++) {
                    Symbol symbol = rhs[j].symbol;
                    if (symbol instanceof Symbol.NonTerminal) {
                        Symbol.NonTerminal nt = (Symbol.NonTerminal) symbol;
                        if (nt != lhs) {
                            if (lhs.firstSet.add(nt.firstSet)) {
                                firstSetsAreMutating = true;
                            }
                        }
                    }
                    if (!symbol.canMatchEmptyString()) {
                        break;
                    }
                }
            }
        } while (firstSetsAreMutating);
    }

    /**
     * Assigns production default precedences. A default precedence of a production is the
     * precedence of its rightmost terminal.
     */
    private void assignPrecedences() {
        for (int i = 0; i < productions.length; i++) {
        	Production prod = productions[i];
            if (prod.precedence == '\0') {
            	Production.RHSElement[] rhs = prod.rhs;
                for (int j = rhs.length - 1; j >= 0; j--) {
                	Symbol sym = rhs[j].symbol;
                    if (sym instanceof Symbol.Terminal) {
                        prod.precedence = ((Symbol.Terminal) sym).precedence;
                        break;
                    }
                }
            }
        }
    }

    /**
     * @return Nonterminal that is returned at the end of successful parsing and that represents the
     *         entire parsed text.
     */
    public Symbol.NonTerminal getGoal() {
        return productions[0].lhs;
    }

    public Symbol.Terminal getEOF() {
        return terminals[0];
    }

    public Symbol.Terminal getError() {
        Symbol.Terminal lastTerm = terminals[terminals.length - 1];
        return "ERROR".equals(lastTerm.name) ? lastTerm : null;
    }

    public int getLastTerminalId() {
        return terminals[terminals.length - 1].id;
    }

    public Symbol.Terminal getLastKeyword() {
        Symbol.Terminal lastKeyword = terminals[0];
        for (int i = 1; i < terminals.length && terminals[i].isKeyword(); i++) {
            lastKeyword = terminals[i];
        }
        return lastKeyword;
    }

    public String[] getKeywords() {
        String[] keywords = new String[getLastKeyword().id + 1];
        keywords[0] = "<EOF>";
        for (int i = 1; i < keywords.length; i++) {
            keywords[i] = terminals[i].text;
        }
        return keywords;
    }

    public String[] getTokens() {
        int tokenId = getLastKeyword().id + 1;
        String[] tokens = new String[terminals.length - tokenId];
        for (int i = 0; i < tokens.length; i++, tokenId++) {
            tokens[i] = terminals[tokenId].name;
        }
        return tokens;
    }
}
