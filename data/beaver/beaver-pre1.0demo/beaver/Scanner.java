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
package beaver;

import java.io.IOException;

/**
 * A protocol between a parser and a scanner. 
 */
public interface Scanner {
    /**
     * Scans the input and returns the next recognized token. If character sequence
     * does not match any known pattern and thus cannot be recognized as a token, it
     * returns a token with ID == -1 and unrecognized characters as its text.   
     * 
     * @return next token
     * @throws IOException when input cannot be read
     */
    Term getNextToken() throws IOException;
    /**
     * Sets an inspector that validates every match. Inspector might reject a match if
     * at that time parser does not expect the matched token. Note that, if all matches
     * have been vetoed, scanner will overrule the last veto and return the last matched
     * token. This will lead to a syntax error and will force parser to start error recovery.
     * 
     * @param insp token inspector, usually the parser
     */
    void setTokenInspector(TokenInspector insp);
    /**
     * Returns the last token recognized by the scanner - the same token that was just returned
     * by getNextToken.
     */
    Term getLastToken();
    /**
     * Creates a new Terminal
     */
    Term makeTerm(int id, int line, int column, Object text);
}
