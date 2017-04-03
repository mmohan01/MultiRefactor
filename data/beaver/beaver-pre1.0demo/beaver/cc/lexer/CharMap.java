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

import java.util.Arrays;

/**
 * A Map where keys are characters.<p>
 * Historical note: this class used to implement a hash map, however as soon as "any = [^]" macro is used,
 * which is almost always :), the Map would "explode" to 128K entries consuming more memory than an array
 * would need while being much much slower at the same time.<p>
 * This implementation preserves the API of the CharMap, but it is just a fancy array.
 */
class CharMap {

    private Object[] table;
    private int      size;

    static abstract class Visitor {
        abstract void visit(char key, Object value);
    }

    CharMap() {
        table = new Object[Character.MAX_VALUE + 1];
    }

    void put(char c, Object value) {
        if (table[c] == null) {
            if (value != null) {
                size++;
            }
        } else {
            if (value == null) {
                size--;
            }
        }
        table[c] = value;
    }

    Object get(char c) {
        return table[c];
    }

    int size() {
        return size;
    }

    void clear() {
        Arrays.fill(table, null);
        size = 0;
    }

    void accept(Visitor proc) {
        for (int i = 0; i < table.length; i++) {
            if (table[i] != null) {
                proc.visit((char) i, table[i]);
            }
        }
    }
}
