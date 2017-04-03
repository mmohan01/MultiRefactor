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
 * A Scanner implementation that parser uses during error recovery. It buffers
 * a number of recognized tokens and allows parser to see them multiple times -
 * during various attempts to recover from a syntax error and finally to continue
 * parsing when error condition has been addressed.
 */
class BufferedScanner
    implements Scanner
{
    /**
     * Original scanner.
     */
    private Scanner scanner;
    /**
     * Token buffer
     */
    private Term[]  tokens;
    /**
     * Number of tokens in a buffer.
     */
    private int     numTokens;
    /**
     * Index of the next buffered token to be returned to a parser.
     */
    private int     readIndex;

    BufferedScanner(Scanner origScanner) throws IOException {
        scanner = origScanner;
        tokens = new Term[4];
    }

    public void setTokenInspector(TokenInspector insp) {
        scanner.setTokenInspector(insp);
    }

    public Term makeTerm(int id, int line, int column, Object text) {
        return scanner.makeTerm(id, line, column, text);
    }

    public Term getLastToken() {
        return scanner.getLastToken();
    }

    public Term getNextToken() throws IOException {
        if (readIndex == tokens.length) {
            return scanner.getNextToken();
        }
        if (readIndex < numTokens) {
            return tokens[readIndex++];
        }
        readIndex++; // ensure readIndex == numTokens to continue buffering until explicit rewind
        return (tokens[numTokens++] = scanner.getNextToken());
    }

    void append(Term t) {
        tokens[numTokens++] = t;
        readIndex = numTokens;
    }

    Term delete(int idx) {
        if (numTokens <= idx) {
            throw new IllegalStateException("invalid index");
        }
        Term t = tokens[idx];
        if (--numTokens > idx) {
            System.arraycopy(tokens, idx + 1, tokens, idx, numTokens - idx);
        }
        return t;
    }

    void insert(Term t) {
        if (numTokens == tokens.length) {
            throw new IllegalStateException("buffer overflow");
        }
        System.arraycopy(tokens, 0, tokens, 1, numTokens);
        tokens[0] = t;
        readIndex = 0;
    }

    void rewind() {
        readIndex = 0;
    }

    void reset() {
        readIndex = numTokens = 0;
    }
}
