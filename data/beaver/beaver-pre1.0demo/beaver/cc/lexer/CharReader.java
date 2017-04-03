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
package beaver.cc.lexer;

/**
 * CharReader reads characters, that may be escaped, from a regular expression pattern
 */
class CharReader {
    private char[] chars;
    private int    readPos;
    private int    end;

    CharReader(char[] chars, int offset, int length) {
        this.chars = chars;
        this.readPos = offset;
        this.end = offset + length;
    }

    CharReader(String str) {
        this.chars = str.toCharArray();
        this.end = chars.length;
    }

    /**
     * Probes whether a substring is empty.
     *
     * @return true if a substring references empty set of characters
     */
    boolean isEmpty() {
        return readPos == end;
    }

    /**
     * Removes specified number of characters from both ends of the substring
     *
     * @param numChars
     *            number of characters to remove
     */
    void trim(int numChars) {
        readPos += numChars;
        end -= numChars;
    }

    /**
     * Returns current read position
     */
    int mark() {
        return readPos;
    }

    /**
     * Resets current read position back to the marked position
     *
     * @param mark
     */
    void reset(int mark) {
        if (mark < 0 || mark > readPos)
            throw new IllegalArgumentException("out of range");
        readPos = mark;
    }

    /**
     * Reads the next character, returns it and trims the substring so that its second character
     * becomes first
     *
     * @return next character
     */
    private int readNextChar() {
        if (isEmpty())
            return -1;

        return chars[readPos++];
    }

    /**
     * Reads next character and returns its numeric value if the character can represent a digit in
     * the specified radix.
     *
     * @param radix
     *            the radix
     * @return the numeric value represented by the next character in the specified radix.
     */
    int readDigit(int radix) {
        int digit;

        if (isEmpty())
            throw new IllegalStateException("empty");

        digit = Character.digit(readNextChar(), radix);
        if (digit < 0)
            throw new IllegalStateException("Illegal digit");

        return digit;
    }

    /**
     * Returns next, possibly unescaped first, character.
     *
     * @return next character
     */
    int readChar() {
        int c = readNextChar();

        if (c == '\\' && readPos < end) {
            switch (c = readNextChar()) {
                case 'b':
                    return '\b';
                case 'f':
                    return '\f';
                case 'n':
                    return '\n';
                case 'r':
                    return '\r';
                case 't':
                    return '\t';
                case 'x': {
                    return readDigit(16) << 4 | readDigit(16);
                }
                case 'u': {
                    c = readDigit(16) << 12 | readDigit(16) << 8 | readDigit(16) << 4 | readDigit(16);
                    if (c == '\uffff')
                        throw new IllegalStateException("\\uffff is an illegal character");
                    break;
                }
            }
        }
        return c;
    }

    static String escape(char c) {
        if (c <= 0xf) {
            switch (c) {
                case '\b':
                    return "\\b";
                case '\f':
                    return "\\f";
                case '\n':
                    return "\\n";
                case '\r':
                    return "\\r";
                case '\t':
                    return "\\t";
            }
            return "\\x0" + Integer.toHexString(c);
        }
        if (c <= ' ')
            return "\\x" + Integer.toHexString(c);
        if (c <= 0x7f)
            return String.valueOf(c);
        if (c <= 0xff)
            return "\\x" + Integer.toHexString(c);
        if (c <= 0xfff)
            return "\\u0" + Integer.toHexString(c);
        return "\\u" + Integer.toHexString(c);
    }
}
