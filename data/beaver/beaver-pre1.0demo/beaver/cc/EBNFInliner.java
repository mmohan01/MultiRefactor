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

class EBNFInliner extends AST.Walker {
    Collection errorLog = new ArrayList();

    void leave(AST.Symbol.Nested node) {
        if (!(node.parent instanceof AST.SymbolList)) {
            throw new IllegalStateException("unexpected AST");
        }
        AST.SymbolList parentList = (AST.SymbolList) node.parent;
        if (node.optQuant == null) {
            // in-line the list then
            parentList.replace(node, node.symbolList);
        } else if (node.optQuant.text.equals("?")) {
            // replace current symbol definition with 2 alternates - one with
            // the optional symbols and one without.
            if (!(parentList.parent instanceof AST.SymbolDefinition)) {
                throw new IllegalStateException("unexpected AST");
            }
            AST.SymbolDefinition def = (AST.SymbolDefinition) parentList.parent;
            if (!(def.parent instanceof AST.SymbolDefinitionList)) {
                throw new IllegalStateException("unexpected AST");
            }
            // AST.SymbolDefinitionList defList = (AST.SymbolDefinitionList) def.parent;

            
        }
    }
}
