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
import java.io.Reader;

/**
 * Scanner class describes a contract between a scanner and a parser. It also
 * provides a basis for implementation of the generated scanner.
 * <p>
 * To speed up recognition grammars are required to use tokens that do not span
 * several lines and the new line matching rule should be present.
 */
public abstract class CharScanner
    implements Scanner
{
    /**
     * Source buffer
     */
    protected char[]         text;
    /**
     * Index of the end of the text in the text buffer.
     */
    protected int            endOfText;
    /**
     * Index of the first character of the recognized token. The start value is
     * only valid after a token has been matched, i.e. after the getNextToken
     * has returned a token ID.
     */
    protected int            start;
    /**
     * Index of a character just after the last character of the recognized
     * token. The recognition of the next token starts from that character.
     */
    protected int            cursor;
    /**
     * Start Condition - a selector of a subset of rules that the scanner will use
     * to match the next token.
     */
    protected int            startCond;
    /**
     * Source line number where the last token has been found.
     */
    protected int            line;
    /**
     * Index of the first character on the current line. Can be negative if
     * refillBuffer shifted the first character out of the buffer.
     */
    protected int            lineStart;
    /**
     * The Source reader
     */
    private Reader           source;
    /**
     * Last recognized token.
     */
    private Term             lastToken;
    /**
     * A token inspector approves or rejects matches. Normally scanner would try to
     * match the longest possible token. Inspector may invalidate some candidates
     * depending on what parser expects in the current state. This allows matching 
     * > instead of >> in certain parser states, for instance.
     */
    protected TokenInspector inspector;
    
    /**
     * CharScanner constructor.
     * 
     * @param sourceReader
     *            a reader to read the source from
     * @throws IOException
     *             when the source reader fails
     */
    protected CharScanner(Reader sourceReader) throws IOException {
        source = sourceReader;
        text = new char[8192];
        start = lineStart = endOfText = text.length;
        refillBuffer(0);
        line = 1;
        cursor = start;
    }

    /**
     * Scans the input trying to match it to one of the patterns. When token inspector
     * is present it might veto some matches because parser does not expect them in its
     * current state. If all matches have been vetoed scanner will overrule the last veto
     * and return that token. This will lead to a syntax error and will force parser to
     * start error recovery.
     * 
     * @return the ID of the next token found in the input, or -1 if the input
     *         does not match any known pattern.
     * @throws IOException when input cannot be read
     */
    protected abstract int scan() throws IOException;
    
    /**
     * Normally scans the input and returns the next recognized token. If character
     * sequence does not match any known pattern and thus cannot be recognized as a
     * token, it returns a token with ID == -1 and unrecognized characters as its text.   
     * 
     * @return next token
     * @throws IOException when input cannot be read
     */
    public Term getNextToken() throws IOException {
        int id = scan();
        return lastToken = 
                makeTerm(id, line, getCurrentTokenColumn(), 
                        id >= 0 && inspector != null && inspector.isKeyword(id) 
                                ? null 
                                : new String(text, start, cursor - start));
    }

    /**
     * Returns the last token back to the scanner, so that the next time getNextToken is called
     * this token is returned. Parser uses it to return a token it cannot accept in its current
     * state. Error recovery procedure reads it and deals with it in due course.
     */
    public Term getLastToken() {
        return lastToken;
    }
    
    /**
     * Sets an inspector that validates every match. Inspector might reject a match if
     * at that time parser does not expect the matched token.
     * 
     * @param insp token inspector, usually the parser
     */
    public void setTokenInspector(TokenInspector insp) {
        inspector = insp;
    }
    
    /**
     * It is expected that grammars will define a token for the new line with
     * the "NewLine" event. In which case the generated scanner will call this
     * event method, so that we can adjust the cursor position.
     * <p>
     * This approach cannot handle cases when a new line is part of a complex
     * token. Therefore it is expected the the scanner specification is written
     * with this in mind, new line rule is explicitly defined and it uses the
     * "NewLine" event.
     */
    protected void onNewLine() {
        line++;
        lineStart = cursor;
    }

    /**
     * Called by the generated scanner when cursor reaches the end of the text
     * in the text buffer.
     * 
     * @param needCount
     *          number of characters that must be available without additional
     *          refill calls unless the cursor is near the end of the file
     * 
     * @return shift distance (always a negative number)
     */
    protected int refillBuffer(int needCount) throws IOException {        
        int shiftDistance = -start; // (0 - start)
    	endOfText += shiftDistance;
    	if (endOfText + needCount >= text.length) {
    	    // Token is so large that it will not fit in the current buffer
    	    char[] buff = new char[text.length * 2];
            System.arraycopy(text, start, buff, 0, endOfText);
            text = buff;
    	} else {
    	    System.arraycopy(text, start, text, 0, endOfText);
    	}
        start = 0;
        lineStart += shiftDistance;
        // Fill the rest of the buffer
        int charRead = source.read(text, endOfText, text.length - endOfText);
        if (charRead >= 0) {
            endOfText += charRead;            
            int tailSize = needCount - charRead;
            if (tailSize > 0) {
                // fill in the rest with an "invalid" character code
                int tailEnd = endOfText + tailSize;
                for (int i = endOfText; i < tailEnd; i++) {
                    text[i] = '\ufffe'; 
                }
            }
        } else {
            text[endOfText] = '\uffff'; // EOF sentinel            
        }
        return shiftDistance;
    }
    
    /**
     * @return a column where the token that was just matched by scan is found.
     */
    private int getCurrentTokenColumn() {
        return start - lineStart + 1;
    }
}
