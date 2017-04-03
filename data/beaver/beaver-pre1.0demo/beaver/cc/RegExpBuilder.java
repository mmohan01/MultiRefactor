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
package beaver.cc;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import beaver.cc.lexer.RegExp;
import beaver.cc.parser.Symbol;

class RegExpBuilder extends AST.Walker {
    Collection errorLog = new ArrayList();
    Collection warningLog = new ArrayList();
    
    static class Stack {
        private RegExp[] re = new RegExp[16];
        private int      sp = -1;
        
        void push(RegExp val) {            
            if (++sp == re.length) {
                RegExp[] newArray = new RegExp[re.length * 2]; 
                System.arraycopy(re, 0, newArray, 0, re.length);
                re = newArray;
            }
            re[sp] = val;
        }
        
        RegExp pop() {
            return re[sp--];
        }
    }
    
    RegExpBuilder.Stack      stack;
    Map        macros;
    int        precedence;
    int        skipTokenId;
    Map        symbols;
    AST.Node   eventListNode;
    Collection events;
    AST.Node   startCondListNode;
    Collection startConditions;
    Map        condIds;
    RegExp[]   condRules;
    String[]   condNames;
    
    RegExpBuilder(Map symbols, Collection startConditions) {
        this.symbols = symbols;
        
        condIds = new HashMap();
        condIds.put("MAIN", new Integer(0));
        for (Iterator i = startConditions.iterator(); i.hasNext();) {
            String condName = (String) i.next();
            if (condName.charAt(0) == 'X') {
                condIds.put(condName, new Integer(condIds.size()));
            }
        }
        condRules = new RegExp[condIds.size()];
        condNames = new String[condIds.size()];
        for (Iterator j = condIds.entrySet().iterator(); j.hasNext();) {
            Map.Entry e = (Map.Entry) j.next();
            String name = (String) e.getKey();
            Integer idx = (Integer) e.getValue();
            condNames[idx.intValue()] = name;
        }
        
        for (Iterator i = startConditions.iterator(); i.hasNext();) {
            String condName = (String) i.next();
            if (!condIds.containsKey(condName)) {
                condIds.put(condName, new Integer(condIds.size()));
            }
        }
        
        stack = new Stack();
        macros = new HashMap();
        events = new ArrayList();
        this.startConditions = new ArrayList();
        precedence = Integer.MAX_VALUE - 1;
        /* 
         * Find ID that will be assigned to skip-able tokens
         */
        for (Iterator i = symbols.values().iterator(); i.hasNext();) {
            Object sym = i.next();
            if (sym instanceof Symbol.Terminal) {
                Symbol.Terminal term = (Symbol.Terminal) sym;
                if (term.getId() > skipTokenId) {
                    skipTokenId = term.getId(); 
                }
            }
        }
    }

    void visit(AST.ProductionList node) {
    }

    void visit(AST.PrecedenceList node) {
    }
    
    void visit(AST.RegExp.Text node) {
        RegExp re = RegExp.matchText(strip((String) node.text.text));
        if (node.optQuant != null) {
            re = new RegExp.Quantified(re, node.optQuant.text.toString().charAt(0));
        }
        stack.push(re);
    }
    
    void visit(AST.RegExp.Range node) {
        RegExp re = new RegExp.MatchRange(strip((String) node.range.text));
        if (node.optQuant != null) {
            re = new RegExp.Quantified(re, node.optQuant.text.toString().charAt(0));
        }
        stack.push(re);
    }
    
    void visit(AST.RangeExp.Name node) {
        RegExp re = (RegExp) macros.get(node.name.text);
        if (re == null) {
            errorLog.add("@" + node.name.line + "," + node.name.column + " unknown macro " + node.name.text);
            re = new RegExp.MatchRange("");
        } else if (!(re instanceof RegExp.MatchRange)) {
            errorLog.add("@" + node.name.line + "," + node.name.column + " macro " + node.name.text + " must represent a character range");
            re = new RegExp.MatchRange("");
        }
        stack.push(re);
    }
    
    void visit(AST.RangeExp.Text node) {
        stack.push(new RegExp.MatchRange(strip((String) node.range.text)));
    }
    
    void leave(AST.RegExp.SubRange node) {
        RegExp subtrahend = stack.pop();
        stack.push(((RegExp.MatchRange) stack.pop()).subtract(subtrahend));
    }
    
    void leave(AST.RegExp.Cat node) {
        RegExp re = stack.pop();
        stack.push(new RegExp.Cat(stack.pop(), re));            
    }
    
    void leave(AST.RegExp.Alt node) {
        stack.push(new RegExp.Alt(stack.pop(), stack.pop()));
    }
    
    void leave(AST.RegExp.Nested node) {
        if (node.optQuant != null) {
            stack.push(new RegExp.Quantified(stack.pop(), node.optQuant.text.toString().charAt(0)));
        }
    }
    
    void leave(AST.RegExp.Macro node) {
        RegExp re = (RegExp) macros.get(node.name.text);
        if (re == null) {
            errorLog.add("@" + node.name.line + "," + node.name.column + " unknown macro " + node.name.text);
            re = new RegExp.Nil();
        }
        if (node.optQuant != null) {
            re = new RegExp.Quantified(re, node.optQuant.text.toString().charAt(0));
        }
        stack.push(re);
    }

    void leave(AST.Macro node) {
        macros.put(node.name.text, stack.pop());
    }
    
    boolean enter(AST.EventList node) {
        eventListNode = node;
        events.clear();
        return true;
    }
    
    boolean enter(AST.StartCondList node) {
        startCondListNode = node;
        startConditions.clear();
        return true;
    }
    
    void visit(AST.Term node) {
        if (node.parent == eventListNode) {
            events.add(strip((String) node.text));
        } else if (node.parent == startCondListNode) {
            startConditions.add(node.text.toString().toUpperCase());
        }
    }
    
    void leave(AST.Token node) {
        int tokenId;
        Symbol token;
        if (events.contains("Skip")) {
            tokenId = ++skipTokenId;
        } else if ((token = (Symbol) symbols.get(node.name.text)) != null) {
            tokenId = token.getId();
        } else {
            warningLog.add("@" + node.name.line + "," + node.name.column + " token is not used by the grammar " + node.name.text);
            return;
        }
        String[] matchEvents = (events.size() == 0 ? null : (String[]) events.toArray(new String[events.size()]));
        
        int nextCond = -1;
        if (node.optCondShift != null) {
            Integer nextCondId = (Integer) condIds.get(node.optCondShift.text.toString().toUpperCase());
            if (nextCondId == null) {
                errorLog.add("@" + node.optCondShift.line + "," + node.optCondShift.column + " start condition " + node.optCondShift.text + " is not used for any token");
                return;
            }
            nextCond = nextCondId.intValue();
        }

        int[] mainStartCond = null;
        if (startConditions.size() > 0) {
            List condList = new ArrayList();
            for (Iterator i = startConditions.iterator(); i.hasNext();) {
                String condName = (String) i.next();
                if (condName.charAt(0) != 'X') {
                    condList.add(condIds.get(condName));
                }
            }
            if (condList.size() > 0) {
                mainStartCond = new int[condList.size()];
                Collections.sort(condList);
                int ix = 0;
                for (Iterator i = condList.iterator(); i.hasNext(); ix++) {
                    Integer condId = (Integer) i.next();
                    mainStartCond[ix] = condId.intValue();
                }
            }
        }
        
        RegExp matchExpr = new RegExp.Pattern(
                stack.pop(), 
                new RegExp.Pattern.MatchResult(
                        tokenId, 
                        --precedence, 
                        matchEvents,
                        mainStartCond,
                        nextCond));
        
        if (startConditions.size() == 0 || mainStartCond != null) {
            if (condRules[0] == null) {
                condRules[0] = matchExpr;
            } else {
                condRules[0] = new RegExp.Alt(condRules[0], matchExpr);
            }
        }
        for (Iterator i = startConditions.iterator(); i.hasNext();) {
            String condName = (String) i.next();
            if (condName.charAt(0) == 'X') {
                int idx = ((Integer) condIds.get(condName)).intValue();
                if (condRules[idx] == null) {
                    condRules[idx] = matchExpr;
                } else {
                    condRules[idx] = new RegExp.Alt(condRules[idx], matchExpr);
                }
            }
        }
    }
    
    void leave(AST.TokenList node) {
        // ensure all exclusive start condition rules are present
        for (int i = 0; i < condRules.length; i++) {
            if (condRules[i] == null) {
                throw new IllegalStateException("rule-set for condition " + condNames[i] + " is empty");
            }
        }
        // add keyword rules
        int termPrecedence = Integer.MAX_VALUE;
        for (Iterator i = symbols.values().iterator(); i.hasNext();) {
            Object sym = i.next();
            if (sym instanceof Symbol.Terminal) {
                Symbol.Terminal term = (Symbol.Terminal) sym;
                // Keyword terminal's name is its quoted text
                // Keywords declared as tokens will have a declared name
                if (term.isKeyword() && term.getName().charAt(0) == '"') {
                    RegExp rule = new RegExp.Pattern(
                            RegExp.matchText(term.getKeywordText()),
                            new RegExp.Pattern.MatchResult(
                                    term.getId(), 
                                    termPrecedence));
                    if (condRules[0] == null) {
                        condRules[0] = rule;
                    } else {
                        condRules[0] = new RegExp.Alt(condRules[0], rule); 
                    }
                }
            }
        }
    }
    
    private static String strip(String text) {
        return text.substring(1, text.length() - 1);
    }
}