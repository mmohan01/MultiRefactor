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

import beaver.cc.parser.Symbol;

class SymbolCollector extends AST.Walker {
    Map        symbols      = new HashMap();
    Collection errorLog     = new ArrayList();
    String     kwdMatch;

    Map getSymbols() {
        return symbols;
    }

    boolean enter(AST.Symbol.Text node) {
        String symbolName = (String) node.text.text;
        if (!symbols.containsKey(symbolName)) {
            symbols.put(symbolName, new Symbol.Terminal(symbolName, strip(symbolName)));
        }
        return false;
    }

    void visit(AST.Symbol.Name node) {
    }

    boolean enter(AST.Production node) {
        String symbolName = (String) node.name.text;
        if (!symbols.containsKey(symbolName)) {
            symbols.put(symbolName, new Symbol.NonTerminal(symbolName));
        }
        return true; // need to see the keywords
    }

    boolean enter(AST.Token node) {
        kwdMatch = null;
        return true;
    }
    
    void visit(AST.RegExp.Text node) {
        if (node.optQuant == null) {
            kwdMatch = strip((String) node.text.text);
        } else {
            kwdMatch = null;
        }
    }
    
    void visit(AST.RegExp.Range node) {
        kwdMatch = null;
    }    
    
    void visit(AST.RangeExp.Name node) {
        kwdMatch = null;
    }
    
    void visit(AST.RangeExp.Text node) {
        kwdMatch = null;
    }
    
    void leave(AST.RegExp.SubRange node) {
        kwdMatch = null;
    }

    void leave(AST.RegExp.Cat node) {
        kwdMatch = null;
    }

    void leave(AST.RegExp.Alt node) {
        kwdMatch = null;
    }
    
    void leave(AST.RegExp.Nested node) {
        kwdMatch = null;
    }
    
    void leave(AST.Token node) {
        String symbolName = (String) node.name.text;
        if (symbols.containsKey(symbolName)) {
            errorLog.add("@" + node.name.getLine() + "," + node.name.getColumn() + " " + symbolName + " is already defined");            
        } else {
            SkipEventChecker skipEventChecker = new SkipEventChecker();
            node.eventList.accept(skipEventChecker);
            if (!skipEventChecker.foundSkipEvent) {
                symbols.put(symbolName, new Symbol.Terminal(symbolName, kwdMatch));
            }            
        }
    }
    
    void visit(AST.PrecedenceList node) {
    }

    void visit(AST.MacroList node) {
    }
    
    private static String strip(String text) {
        return text.substring(1, text.length() - 1);
    }
}