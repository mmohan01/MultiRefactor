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
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import beaver.cc.parser.Production;
import beaver.cc.parser.Symbol;

class ProductionBuilder extends AST.Walker {
    Map        symbols;
    Collection productions = new ArrayList();
    Collection rhs         = new ArrayList();
    Collection errorLog    = new ArrayList();
    Map        symbolRules = new HashMap();

    ProductionBuilder(Map symbols) {
        this.symbols = symbols;
    }

    Production[] getProductions() {
        return (Production[]) productions.toArray(new Production[productions.size()]);
    }

    boolean enter(AST.SymbolDefinition node) {
        rhs.clear();
        return true;
    }

    boolean enter(AST.Symbol.Name node) {
        rhs.add(new Production.RHSElement((Symbol) symbols.get(node.name.text)));
        return false;
    }

    boolean enter(AST.Symbol.Text node) {
        rhs.add(new Production.RHSElement((Symbol) symbols.get(node.text.text)));
        return false;
    }

    void leave(AST.SymbolDefinition node) {
        // Production
        // ..SymbolDefinitionList
        // ....SymbolDefinition
        String ruleName = (node.optRuleName == null ? null : (String) node.optRuleName.text);
        String symbolName = (String) ((AST.Production) node.parent.parent).name.text;
        Symbol.NonTerminal symbol = (Symbol.NonTerminal) symbols.get(symbolName);
        Production production = new Production(ruleName, symbol, getRuleRHS());
        productions.add(production);

        Collection rules = (Collection) symbolRules.get(symbolName);
        if (rules == null) {
            symbolRules.put(symbolName, rules = new ArrayList());
        }
        rules.add(production);
    }

    void visit(AST.PrecedenceList node) {
    }

    void visit(AST.MacroList node) {
    }

    void visit(AST.TokenList node) {
    }

    void leave(AST.Grammar node) {
        for (Iterator i = symbolRules.entrySet().iterator(); i.hasNext();) {
            Map.Entry e = (Map.Entry) i.next();
            Symbol.NonTerminal nt = (Symbol.NonTerminal) symbols.get(e.getKey());
            Collection ntRules = (Collection) e.getValue();
            Production[] rules = (Production[]) ntRules.toArray(new Production[ntRules.size()]);
            // check that either every rule has a name or none has
            for (int n = 1; n < rules.length; n++) {
                if (rules[0].hasName() ^ rules[n].hasName()) {
                    errorLog.add("@" + nt + ": either all productions or none should be named");
                    break;
                }
            }
            nt.setRules(rules);
        }
    }

    private Production.RHSElement[] getRuleRHS() {
        return (Production.RHSElement[]) rhs.toArray(new Production.RHSElement[rhs.size()]);
    }
}