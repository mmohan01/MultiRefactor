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
import java.util.Map;

import beaver.cc.parser.Production;
import beaver.cc.parser.Symbol;

class PrecedenceAssigner extends AST.Walker {        
    Map        symbols;
    char       prec        = Character.MAX_VALUE;
    char       assoc       = 'N';
    Map        productions = new HashMap();
    Production unmapped    = new Production(null, null, null);
    Collection errorLog    = new ArrayList();

    PrecedenceAssigner(Map symbols, Production[] productions) {
        this.symbols = symbols;
        for (int i = 0; i < productions.length; i++) {
            String ruleName = productions[i].getFullName();
            if (this.productions.put(ruleName, productions[i]) != null) {
                // cannot have same name mapped to several productions
                // replace with a marker
                this.productions.put(ruleName, unmapped);
            }
        }
    }

    boolean enter(AST.Precedence node) {
        prec--;
        assoc = ((String) node.assoc.text).toUpperCase().charAt(0);
        return true;
    }

    boolean enter(AST.PrecedenceSymbol.Name node) {
        String name = (String) node.name.text;
        Symbol symbol = (Symbol) symbols.get(name);
        if (symbol instanceof Symbol.NonTerminal) {
            Production[] rules = ((Symbol.NonTerminal) symbol).getRules(); 
            if (rules.length > 1) {
                errorLog.add("cannot assign precedence to " + name + " - it matches several productions");
            } else {
                rules[0].setPrecedence(prec);
            }
        } else if (symbol instanceof Symbol.Terminal) {
            ((Symbol.Terminal) symbol).setPrecedence(prec, assoc);
        } else {
            Production prod = (Production) productions.get(name);
            if (prod == unmapped) {
                errorLog.add("cannot assign precedence to " + name + " - it matches several productions");
            } else if (prod == null) {
                errorLog.add("cannot assign precedence to " + name + " - this symbol is unknown");
            } else {
                prod.setPrecedence(prec);
            }
        }
        return false;
    }

    boolean enter(AST.PrecedenceSymbol.Text node) {
        String name = (String) node.text.text;            
        Symbol.Terminal symbol = (Symbol.Terminal) symbols.get(name);
        if (symbol == null) {
            errorLog.add("cannot assign precedence to " + name + " - an unknown keyword");
        } else {
            symbol.setPrecedence(prec, assoc);
        }
        return false;
    }

    void visit(AST.ProductionList node) {
    }

    void visit(AST.MacroList node) {
    }

    void visit(AST.TokenList node) {
    }
}