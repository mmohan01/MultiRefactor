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
 * CharSpan is a consecutive range of characters.
 */
class CharSpan {
    char lb, ub; // [lb,ub)

    CharSpan(char firstChar, char lastChar) {
        if (firstChar > lastChar)
            throw new IllegalArgumentException("first > last");
        this.lb = firstChar;
        this.ub = (char) (lastChar + 1);
    }

    CharSpan(char c) {
        this.lb = c;
        this.ub = (char) (c + 1);
    }

    CharSpan(CharSpan span) {
        this.lb = span.lb;
        this.ub = span.ub;
    }

    int size() {
        return ub - lb;
    }

    boolean equals(CharSpan span) {
        return this.lb == span.lb && this.ub == span.ub;
    }

    boolean intersects(CharSpan span) {
        return this.lb < span.ub && span.lb < this.ub;
    }

    boolean isAdjacentTo(CharSpan span) {
        return this.lb == span.ub || this.ub == span.lb;
    }

    boolean isSubsetOf(CharSpan span) {
        return span.lb <= this.lb && this.ub <= span.ub;
    }

    boolean isInnerSubsetOf(CharSpan span) {
        return span.lb < this.lb && this.ub < span.ub;
    }

    boolean contains(char c) {
        return this.lb <= c && c < this.ub;
    }

    boolean isAdjacentTo(char c) {
        return c == this.ub || this.lb - c == 1;
    }

    /**
     * Compares relative position of the two spans.
     *
     * @param span
     * @return 0 if this span intersects with or is adjacent to the specified span 1 if this span
     *         completely to the right of the specified span -1 if this span completely to the left
     *         of the specified span
     */
    int compare(CharSpan span) {
        if (this.ub < span.lb)
            return -1;
        if (span.ub < this.lb)
            return 1;
        return 0;
    }

    /**
     * Compares the position of the span relative to the specified character
     *
     * @param c
     *            character
     * @return 0 if this span contains or is adjacent to the specified character 1 if this span
     *         completely to the right of the specified character -1 if this span completely to the
     *         left of the specified character
     */
    int compare(char c) {
        if (this.ub < c)
            return -1;
        if (this.lb - c > 1)
            return 1;
        return 0;
    }

    /**
     * Expands the current span to also include characters from the other span.
     *
     * @param span
     */
    void add(CharSpan span) {
        if (compare(span) != 0)
            throw new IllegalArgumentException("disjoint span");
        if (this.lb > span.lb)
            this.lb = span.lb;
        if (this.ub < span.ub)
            this.ub = span.ub;
    }

    /**
     * Adds a character to the span
     *
     * @param c
     */
    void add(char c) {
        if (this.ub == c)
            this.ub++;
        else if (this.lb - c == 1)
            this.lb--;
        else if (!this.contains(c))
            throw new IllegalArgumentException("disjoint char");
    }

    /**
     * Removes characters from this span that are also in the other span.
     *
     * @param span
     */
    void sub(CharSpan span) {
        if (!span.intersects(this))
            throw new IllegalArgumentException(span + " does not intersect with " + this);
        if (span.isInnerSubsetOf(this))
            throw new IllegalArgumentException(span + " is not at the edge of " + this);

        if (span.lb > this.lb)
            this.ub = span.lb;
        else
            this.lb = span.ub;
    }

    /**
     * Removes characters from this span that are in the inner span. Reduces current span to the
     * left sub-span.
     *
     * @param spanToRemove
     *            to remove
     * @return right sub-span
     */
    CharSpan remove(CharSpan spanToRemove) {
        if (!spanToRemove.isInnerSubsetOf(this))
            throw new IllegalArgumentException(spanToRemove + " is not a subset of " + this);

        CharSpan rightSpan = new CharSpan(this);
        // trim right span
        rightSpan.lb = spanToRemove.ub;
        // trim current, or left, span
        this.ub = spanToRemove.lb;
        return rightSpan;
    }

    void accept(CharVisitor visitor) {
        for (char c = lb; c < ub; c++) {
            visitor.visit(c);
        }
    }

    public String toString() {
        switch (this.size()) {
            case 1: {
                return CharReader.escape(lb);
            }
            case 2: {
                return CharReader.escape(lb) + CharReader.escape((char) (ub - 1));
            }
            default: {
                return CharReader.escape(lb) + "-" + CharReader.escape((char) (ub - 1));
            }
        }
    }

    public boolean equals(Object obj) {
        return obj instanceof CharSpan && this.equals((CharSpan) obj);
    }
}
