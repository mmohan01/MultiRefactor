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
 * Parser provides a foundation for generated parsers.
 */
public abstract class Parser 
    implements TokenInspector 
{
    public static interface ErrorLog {
        /**
         * Parsing automaton uses this method to notify the parser about a sequence of characters that was not expected by
         * the scanner in the current state.
         * 
         * @param token
         *            An unrecognized token.
         */
        void cannotRecognize(Term token);

        /**
         * Informs the parser that to recover from the error a sequence of characters that scanner was not able to match to
         * any token in the current parser state was deleted.
         * 
         * @param token
         *            An unrecognized token.
         */
        void unexpectedTokenDeleted(Term token);

        /**
         * Informs the parser that to recover from the error a missing token was inserted.
         * 
         * @param token
         *            An inserted token
         */
        void missingTokenInserted(Term token);

        /**
         * Informs the parser that to recover from the error a sequence of characters that scanner was not able to match to
         * any token in the current parser state was replaced by one of the expected tokens.
         * 
         * @param unexpectedToken
         *            An unexpected token.
         * @param replacementToken
         *            An replacement token
         */
        void unexpectedTokenReplaced(Term unexpectedToken, Term replacementToken);

        /**
         * Informs the parser that to recover from the error a phrase was removed.
         * 
         * @param from
         *            The first terminal or a nonterminal that starts the error phrase 
         * @param thru
         *            The last token of the error phrase
         * @param unexpectedToken
         *            is a token in the middle of that phrase that led to this drastic recovery.
         */
        void errorPhraseRemoved(Object from, Term thru, Term unexpectedToken);
    }
    
    public static class NullLog implements ErrorLog {
        public void unexpectedTokenReplaced(Term unexpectedToken, Term replacementToken) {}                
        public void unexpectedTokenDeleted(Term token) {}
        public void missingTokenInserted(Term token) {}
        public void errorPhraseRemoved(Object from, Term thru, Term unexpectedToken) {}
        public void cannotRecognize(Term token) {}
    };
    
    /**
     * Part of the parsing stack where the states are stored.
     */
    protected char[]   states;
    /**
     * Part of the parsing stack where the shifted symbols are stored.
     */
    protected Object[] symbols;
    /**
     * Stack pointer.
     */
    protected int      sp;
    /**
     * Current states. Used to recover a list of currently expected tokens.
     */
    protected int      state;

    /**
     * Main parsing method.
     * 
     * @param scanner
     *          input
     * @return
     *          the goal nonterminal of the grammar
     * @throws IOException 
     * @throws
     *          IllegalStateException when input cannot be parsed
     */
    public Object parse(Scanner scanner, ErrorLog log) throws IOException {
        if (log == null) {
            log = new NullLog();
        }
        scanner.setTokenInspector(this);
        BufferedScanner bufScanner = null;
        states = new char[128];
        symbols = new Object[128];
        // set starting state
        states[sp = 0] = 0;
        
        while (!parse(scanner, -1)) {
            if (bufScanner == null) {
                bufScanner = new BufferedScanner(scanner);
            } else {
                bufScanner.reset();
            }
            recoverFromError(bufScanner, log);
            scanner = bufScanner;
        }
        Object goal = symbols[0]; 
        symbols = null;
        states = null;
        return goal;
    }
    
    public Object parse(Scanner scanner) throws IOException {
        return parse(scanner, null);
    }
    
    /**
     * This method implements a parsing automaton. Depending on the invocation it either parses the input stream and
     * returns a value of the goal nonterminal (on a stack), or it simulates the parsing after some error recovery
     * strategy has been applied to the incoming tokens and to the parser state.
     * 
     * @param input is a source of tokens
     * @param numShift is a number if tokens automaton need to shift before returning a success. -1 means everything
     * @return true when parsing is successful, false when parsing is interrupted due to a parsing error.
     */
    protected abstract boolean parse(Scanner input, int numShift);
    
    /**
     * 
     * @param scanner
     *          
     * @param sp
     * @return
     * @throws IOException 
     */
    protected void recoverFromError(BufferedScanner scanner, ErrorLog log) throws IOException {
        // prepare error recovery stack
        Object[] savedSymbols = symbols;
        char[] savedStack = states;
        int savedSP = sp;
        // we need at least 8 more slots on the recovery stack to cover the worst case scenario
        // when all 4 tokens have to be shifted and the forward parsing will create 4 new (empty)
        // nonterminals
        states = new char[(sp + 15) & ~7];
        symbols = new Object[states.length];

        Term unexpectedToken = scanner.getLastToken();
        if (!recoverFromTokenError(unexpectedToken, scanner, savedStack, savedSP, log)) {
            System.arraycopy(savedSymbols, 0, symbols, 0, savedSP + 1);
            int newSP = recoverFromPhraseError(unexpectedToken, scanner, savedStack, savedSP, log);
            if (newSP < 0) {
                throw new IllegalStateException("cannot recover from the syntax error @" + unexpectedToken.getLine() + "," + unexpectedToken.getColumn() + " - " + unexpectedToken + " in " + Integer.toString(states[savedSP]));
            }
            savedSP = newSP;
        }
        scanner.rewind();
        sp = savedSP;
        states = savedStack;
        symbols = savedSymbols;
    }

    /**
     * This is a helper method that is implemented by the generated parser
     * 
     * @return ID of the ERROR terminal or -1 if ERROR was not used by the grammar
     */
    protected abstract int getErrorTokenId();

    /**
     * This is a helper method that is implemented by the generated parser
     * 
     * @return ID of the last token that is expected to be returned by the scanner
     */
    protected abstract int getLastTokenId();

    /**
     * Generated parser maintains per-state lists of expected tokens that the error recovery procedures uses to alter
     * the token stream around the syntax error. This method returns the first expected token at the current state.
     * 
     * @return ID of the first expected token or -1 if none exists for the specified state
     */
    private int getFirstExpectedToken(int state) {
        return getNextExpectedToken(state, -1);
    }

    /**
     * This method returns the next expected token at the current state.
     * 
     * @param token
     *            ID of the token found by either getFirstExpectedToken or by previous call to getNextExpectedToken
     * @return ID of the next after last expected token or -1 if last was the last :-)
     */
    protected int getNextExpectedToken(int state, int token) {
        this.state = state;
        int lastTokenId = getLastTokenId();
        while (++token <= lastTokenId) {
            if (isValid(token)) {
                return token;
            }
        }
        return -1;
    }

    /**
     * Performs several simple error recovery procedures. These simple procedures only change the parser input - the
     * tokens around the error.
     * 
     * @param scanner
     *            is an instance of a BufferedScanner
     * @param origStack
     *            is a reference to the original state stack
     * @param origSP
     *            is a stack pointer that was saved when error was encountered
     * 
     * @return true if error recover was able to alter the token stream in such a way that further parsing is possible.
     * @throws IOException
     */
    protected boolean recoverFromTokenError(Term unexpectedToken, BufferedScanner scanner, char[] origStack, int origSP, ErrorLog log) throws IOException {
        int failState = origStack[origSP]; 
        
        Term newToken = scanner.makeTerm(-1, unexpectedToken.getLine(), unexpectedToken.getColumn(), "");
        // Prepare insert/replace
        scanner.append(newToken); // synthetic expected token
        
        if (unexpectedToken.getId() < 0) {
            log.cannotRecognize(unexpectedToken); // will try to replace first
        } else {
            /*
             * Insert expected token before unexpected. Intended to fix simple typos where an expected terminal was not
             * entered.
             */
            scanner.append(unexpectedToken);
            for (int tokenId = getFirstExpectedToken(failState); tokenId >= 0; tokenId = getNextExpectedToken(failState, tokenId)) {
                newToken.setId(tokenId);
                scanner.rewind();
                setupErrorRecoveryStack(origStack, origSP);
                if (parse(scanner, 4)) {
                    log.missingTokenInserted(newToken);
                    return true;
                }
            }
            scanner.delete(1); // i.e. the unexpected token
        }
        /*
         * Replace unexpected token with expected. Fixes cases where a writer used one token instead of another that the
         * grammar requires (parenthesis instead of braces)
         */
        for (int tokenId = getFirstExpectedToken(failState); tokenId >= 0; tokenId = getNextExpectedToken(failState, tokenId)) {
            newToken.setId(tokenId);
            scanner.rewind();
            setupErrorRecoveryStack(origStack, origSP);
            if (parse(scanner, 4)) {
                log.unexpectedTokenReplaced(unexpectedToken, newToken);
                return true;
            }
        }        
        scanner.delete(0); // synthetic new token
        /*
         * Delete unexpected token. Fixes cases where a writer assumed a token is needed, like a terminator, but the
         * grammar did not use one.
         */
        scanner.rewind();
        setupErrorRecoveryStack(origStack, origSP);
        if (parse(scanner, 3)) {
            log.unexpectedTokenDeleted(unexpectedToken);
            return true;
        }
        /*
         * Nothing worked.
         */
        return false;
    }

    /**
     * Performs an error phrase recovery. This method is called if simple recoveries have failed. It discards already
     * shifted (and reduced) symbols until it reaches a state where a special ERROR terminal can be shifted. Then it
     * starts discarding incoming tokens until it can shift next 3 (after the ERROR) terminals.
     * 
     * @param scanner
     *            is an instance of a BufferedScanner
     * @param origStack
     *            is a reference to the original state stack
     * @param origSP
     *            is a stack pointer that was saved when error was encountered
     * 
     * @return new stack pointer that points to a state that can accept ERROR or -1 if error phrase cannot be recovered
     * @throws IOException
     */
    protected int recoverFromPhraseError(Term unexpectedToken, BufferedScanner scanner, char[] origStack, int origSP, ErrorLog log) throws IOException {
        int errorTokenId = getErrorTokenId();
        if (errorTokenId > 0) {            
            setupErrorRecoveryStack(origStack, origSP);
            Object from = unexpectedToken;
            while (sp >= 0 && !isValid(errorTokenId)) {
                from = symbols[sp--];
            }
            if (sp >= 0) {
                origSP = sp;
                Term thru = unexpectedToken;
                scanner.insert(scanner.makeTerm(errorTokenId, unexpectedToken.getLine(), unexpectedToken.getColumn(), unexpectedToken.getText()));
                while(scanner.getLastToken().getId() > 0) {
                    scanner.rewind();
                    setupErrorRecoveryStack(origStack, origSP);
                    if (parse(scanner, 4)) {
                        log.errorPhraseRemoved(from, thru, unexpectedToken);
                        return origSP;
                    }
                    thru = scanner.delete(1); // and keep the ERROR terminal
                }
            }
        }
        return -1;
    }

    /**
     * Initializes error recovery stack.
     * 
     * @param origStack the preserved stack of states from the time when parsing was interrupted by the error  
     * @param origSP reserved SP from the time when parsing was interrupted by the error
     */
    private void setupErrorRecoveryStack(char[] origStack, int origSP) {
        System.arraycopy(origStack, 0, states, 0, (sp = origSP) + 1);
    }

    /**
     * This is a helper method that expands state stack.
     * 
     * @param stack
     *            full state stack
     * @return expanded state stack
     */
    protected static char[] expand(char[] stack) {
        char[] newStack = new char[stack.length * 2];
        System.arraycopy(stack, 0, newStack, 0, stack.length);
        return newStack;
    }

    /**
     * This is a helper method that expands symbol stack.
     * 
     * @param stack
     *            full symbol stack
     * @return expanded symbol stack
     */
    protected static Object[] expand(Object[] stack) {
        Object[] newStack = new Object[stack.length * 2];
        System.arraycopy(stack, 0, newStack, 0, stack.length);
        return newStack;
    }
    
    /**
     * This is a class init method helper that transforms a string into a byte array.
     * 
     * @param str encoded lookaheads or offsets
     * @return byte array representation of the original string
     */
    protected static byte[] toByteArray(String str) {
        byte[] bytes = new byte[str.length()];
        for (int i = 0; i < bytes.length; i++) {
            bytes[i] = (byte) str.charAt(i);
        }
        return bytes;
    }
    
    protected void enter(int state, int lookaheadId, Term token, Object lastNonterminal) {        
        System.out.print(" @ " + state + " : ");
        if (lookaheadId == token.getId()) {
            System.out.println(token);
        } else {
            System.out.println(lastNonterminal == null ? "''" : lastNonterminal);
        }
    }
    
    protected void shift(Term t) {
        System.out.println("<< " + (t == null ? "''" : t.toString()));
    }
    
    protected void reduce(Object s) {
        System.out.println(">> " + (s == null ? "''" : s.toString()));
    }
    
    protected void accept(Object g) {
        System.out.println("== " + g);
    }
}
