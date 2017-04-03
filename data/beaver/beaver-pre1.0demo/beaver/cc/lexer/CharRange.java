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
 * CharRange is an ordered collection of CharSpans
 */
class CharRange {
    CharSpan[] spans;

    CharRange() {
        spans = EMPTY;
    }

    CharRange(CharSpan span) {
        spans = new CharSpan[] { span };
    }

    CharRange(CharRange range) {
        spans = new CharSpan[range.spans.length];
        for (int i = 0; i < spans.length; i++) {
            spans[i] = new CharSpan(range.spans[i]);
        }
    }

    /**
     * Compiles the range from its string representation.
     *
     * @param str
     *            range representation
     */
    CharRange(CharReader str) {
        int strStart = str.mark();
        boolean isInverse = !str.isEmpty() && str.readChar() == '^';
        if (isInverse) {
            spans = new CharSpan[] { new CharSpan('\0', '\ufffe') };
        } else {
            spans = EMPTY;
            str.reset(strStart);
        }
        while (!str.isEmpty()) {
            char lb = (char) str.readChar();
            if (str.isEmpty()) {
                if (isInverse) {
                    sub(lb);
                } else {
                    add(lb);
                }
                break;
            }
            char nc = (char) str.readChar();
            while (nc != '-') {
                if (isInverse) {
                    sub(lb);
                } else {
                    add(lb);
                }

                if (str.isEmpty()) {
                    break;
                }
                lb = nc;
                nc = (char) str.readChar();
            }
            if (str.isEmpty()) {
                if (isInverse) {
                    sub(nc);
                } else {
                    add(nc);
                }
                break;
            }
            char rb = (char) str.readChar();
            if (isInverse) {
                sub(new CharSpan(lb, rb));
            } else {
                add(new CharSpan(lb, rb));
            }
        }
    }

    CharRange(String str) {
        this(new CharReader(str));
    }

    public String toString() {
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < spans.length; i++) {
            str.append(spans[i].toString());
        }
        return str.toString();
    }

    public boolean equals(Object obj) {
        return obj instanceof CharRange && this.equals((CharRange) obj);
    }

    int size() {
        int numChars = 0;
        for (int i = 0; i < spans.length; i++) {
            numChars += spans[i].size();
        }
        return numChars;
    }

    boolean contains(char c) {
        for (int i = 0; i < spans.length; i++) {
            if (spans[i].contains(c))
                return true;
        }
        return false;
    }

    /**
     * Adds another span to the range. Keeps spans ordered. The added span will be merged with an
     * existing span if possible.
     *
     * @param span
     *            to add to this range. The span instance might be inserted into the range.
     */
    void add(CharSpan span) {
        int index = 0, cmp = 0;
        while (index < spans.length && (cmp = spans[index].compare(span)) < 0) {
            index++;
        }
        if (index < spans.length && cmp == 0) {
            spans[index].add(span);
            if (index + 1 < spans.length && spans[index].compare(spans[index + 1]) == 0) {
                spans[index].add(spans[index + 1]);
                remove(index + 1);
            }
        } else {
            insertAt(index, span);
        }
    }

    /**
     * Adds a character to the range
     *
     * @param c
     *            character to add
     */
    void add(char c) {
        int index = 0, cmp = 0;
        while (index < spans.length && (cmp = spans[index].compare(c)) < 0) {
            index++;
        }
        if (index < spans.length && cmp == 0) {
            spans[index].add(c);
            if (index + 1 < spans.length && spans[index].compare(spans[index + 1]) == 0) {
                spans[index].add(spans[index + 1]);
                remove(index + 1);
            }
        } else {
            insertAt(index, new CharSpan(c));
        }
    }

    /**
     * Adds a character, which is known to be the largest, to the range.<p>
     * The behavior of this method, when the added character is not greater than
     * all existing characters within this range, is undefined.
     *
     * @param c
     *            character to add
     */
    void addLast(char c) {
        if (spans.length == 0) {
            spans = new CharSpan[1];
            spans[0] = new CharSpan(c);
        } else {
            int index = spans.length - 1;
            if (spans[index].isAdjacentTo(c)) {
                spans[index].add(c);
            } else {
                CharSpan[] newSpans = new CharSpan[spans.length + 1];
                System.arraycopy(spans, 0, newSpans, 0, spans.length);
                newSpans[index + 1] = new CharSpan(c);
                spans = newSpans;
            }
        }
    }

    /**
     * Adds characters from another range to this range
     *
     * @param range
     *            to add
     */
    void add(CharRange range) {
        for (int i = 0; i < range.spans.length; i++) {
            this.add(new CharSpan(range.spans[i]));
        }
    }

    /**
     * Removes a single character from this range
     *
     * @param c
     *            character to remove
     */
    void sub(char c) {
        sub(new CharSpan(c));
    }

    /**
     * Removes characters from this range that are present in the subtrahend span
     *
     * @param subSpan
     */
    void sub(CharSpan subSpan) {
        for (int i = 0; i < spans.length && spans.length > 0; i++) // note, the array may shrink
        {
            CharSpan span = spans[i];

            int cmp = span.compare(subSpan);
            if (cmp < 0)
                continue;
            if (cmp > 0)
                break;

            if (subSpan.isInnerSubsetOf(span)) {
                insertAt(i + 1, span.remove(subSpan));
                break;
            } else if (span.isSubsetOf(subSpan)) {
                remove(i);
                // there is now a different span at i
                --i;
            } else if (span.intersects(subSpan)) {
                span.sub(subSpan);
            }
        }
    }

    /**
     * Removes characters from this range that are present in the other range
     *
     * @param range
     *            to subtract
     */
    void sub(CharRange range) {
        for (int i = 0; i < range.spans.length; i++) {
            this.sub(range.spans[i]);
        }
    }

    boolean equals(CharRange range) {
        if (this.spans.length != range.spans.length) {
            return false;
        }
        for (int i = 0; i < this.spans.length; i++) {
            if (!this.spans[i].equals(range.spans[i])) {
                return false;
            }
        }
        return true;
    }

    void accept(CharVisitor visitor) {
        for (int i = 0; i < spans.length; i++) {
            spans[i].accept(visitor);
        }
    }

    private void insertAt(int index, CharSpan span) {
        if (index > spans.length)
            throw new IndexOutOfBoundsException();

        CharSpan[] newSpans = new CharSpan[spans.length + 1];
        if (index == 0) {
            newSpans[0] = span;
            System.arraycopy(spans, 0, newSpans, 1, spans.length);
        } else if (index == spans.length) {
            System.arraycopy(spans, 0, newSpans, 0, spans.length);
            newSpans[index] = span;
        } else {
            System.arraycopy(spans, 0, newSpans, 0, index);
            newSpans[index] = span;
            System.arraycopy(spans, index, newSpans, index + 1, spans.length - index);
        }
        spans = newSpans;
    }

    private void remove(int index) {
        CharSpan[] newSpans = new CharSpan[spans.length - 1];
        if (index == newSpans.length) {
            System.arraycopy(spans, 0, newSpans, 0, newSpans.length);
        } else {
            System.arraycopy(spans, 0, newSpans, 0, index);
            System.arraycopy(spans, index + 1, newSpans, index, newSpans.length - index);
        }
        spans = newSpans;
    }

    private static final CharSpan[] EMPTY = new CharSpan[0];
}
