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

import beaver.Parser;

class ParserErrorLog implements Parser.ErrorLog {
    Collection messages = new ArrayList();

    public void cannotRecognize(beaver.Term token) {
        messages.add("cannot recognize " + token + " @" + token.getLine() + "," + token.getColumn());
    }

    public void unexpectedTokenDeleted(beaver.Term token) {
        messages.add("unexpected token " + token + " deleted @" + token.getLine() + "," + token.getColumn());
    }

    public void missingTokenInserted(beaver.Term token) {
        messages.add("missing token " + token + " inserted @" + token.getLine() + "," + token.getColumn());
    }

    public void unexpectedTokenReplaced(beaver.Term unexpectedToken, beaver.Term replacementToken) {
        messages.add("missing token " + unexpectedToken + " is replaced with " + replacementToken + " @"
                + unexpectedToken.getLine() + "," + unexpectedToken.getColumn());
    }

    public void errorPhraseRemoved(Object from, beaver.Term thru, beaver.Term unexpectedToken) {
        messages.add("error phrase removed around " + unexpectedToken + " @" + unexpectedToken.getLine() + ","
                + unexpectedToken.getColumn());
    }
}