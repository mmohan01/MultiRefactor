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
package beaver.cc.parser;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

public abstract class ParserWriter {

    public abstract void writeParser(File outDir, String parserClassName, String baseParserClassName, String astRootClassName, Grammar grammar, ParserState firstState) throws IOException;

    static String getNodeType(Symbol symbol) {
        if (symbol instanceof Symbol.NonTerminal) {
            Symbol.NonTerminal nt = (Symbol.NonTerminal) symbol;
            if (nt.delegate == null || nt.delegate instanceof Symbol.NonTerminal) {
                return symbol.getName();
            }
        }
        return "Term";
    }

    static class PackedTermLookaheads {
        static class ParserStateTermLookaheads implements Comparable {
        	ParserState state;
        	char[]      terms;
        	int         count;

        	ParserStateTermLookaheads (ParserState st) {
        		state = st;
        		terms = getIdsOfTerminalLookaheads(st);
        		for (int i = 0; i < terms.length; i++) {
        			if (terms[i] < Character.MAX_VALUE) {
        				count++;
        			}
        		}
        	}

        	public int compareTo(Object o) {
        		ParserStateTermLookaheads x = (ParserStateTermLookaheads) o;
        		int cmp = x.count - count;
        		if (cmp == 0) {
        			cmp = x.terms.length - terms.length;
        		}
        		return cmp;
        	}

            private static char[] getIdsOfTerminalLookaheads (ParserState st) {
            	int minId = Integer.MAX_VALUE;
            	int maxId = Integer.MIN_VALUE;
            	// find the range
            	ParserAction act;
            	for (act = st.firstShift; act != null; act = act.next) {
            		if (act.lookahead instanceof Symbol.Terminal) {
            			if (act.lookahead.id < minId) {
            				minId = act.lookahead.id;
            			}
            			if (act.lookahead.id > maxId) {
            				maxId = act.lookahead.id;
            			}
            		}
            	}
            	for (act = st.firstReduce; act != null; act = act.next) {
            		if (act.lookahead instanceof Symbol.Terminal) {
            			if (act.lookahead.id < minId) {
            				minId = act.lookahead.id;
            			}
            			if (act.lookahead.id > maxId) {
            				maxId = act.lookahead.id;
            			}
            		}
            	}
            	act = st.defaultReduce;
        		if (act != null && act.lookahead instanceof Symbol.Terminal) {
        			if (act.lookahead.id < minId) {
        				minId = act.lookahead.id;
        			}
        			if (act.lookahead.id > maxId) {
        				maxId = act.lookahead.id;
        			}
        		}
        		if (maxId < minId) {
        			return new char[0];
        		}
        		char[] terms = new char[maxId - minId + 1];
        		Arrays.fill(terms, Character.MAX_VALUE);
            	for (act = st.firstShift; act != null; act = act.next) {
            		if (act.lookahead instanceof Symbol.Terminal) {
            			terms[act.lookahead.id - minId] = (char) act.lookahead.id;
            		}
            	}
            	for (act = st.firstReduce; act != null; act = act.next) {
            		if (act.lookahead instanceof Symbol.Terminal) {
            			terms[act.lookahead.id - minId] = (char) act.lookahead.id;
            		}
            	}
            	act = st.defaultReduce;
        		if (act != null && act.lookahead instanceof Symbol.Terminal) {
        			terms[act.lookahead.id - minId] = (char) act.lookahead.id;
        		}
        		if (terms[0] == Character.MAX_VALUE || terms[terms.length - 1] == Character.MAX_VALUE) {
        		    throw new IllegalStateException();
        		}
        		return terms;
            }
        }

        char[] packedStateLookaheads;
        int    maxTermId;
        char[] stateOffsets;
        int    maxOffset;

        PackedTermLookaheads(ParserState firstState) {
            int numStates = 0;
            for (ParserState st = firstState; st != null; st = st.next, numStates++) {}
            int maxPackSize = 0;
            maxTermId = -1;
            PackedTermLookaheads.ParserStateTermLookaheads[] matrix = new PackedTermLookaheads.ParserStateTermLookaheads[numStates];
            for (ParserState st = firstState; st != null; st = st.next) {
                char[] terms = (matrix[st.id] = new PackedTermLookaheads.ParserStateTermLookaheads(st)).terms;
                maxPackSize += terms.length;
                if (terms.length > 0 && terms[terms.length - 1] > maxTermId) {
                    maxTermId = terms[terms.length - 1];
                }
            }
            if (maxPackSize > Character.MAX_VALUE) {
                throw new IllegalStateException("char data type is too small to store offsets");
            }
            Arrays.sort(matrix);
            if (matrix[0].terms.length == 0) {
                throw new IllegalStateException();
            }
            maxPackSize += matrix[0].terms[0];
            char[] packedTokens = new char[maxPackSize];
            Arrays.fill(packedTokens, Character.MAX_VALUE);
            int lastTokenIdx = -1;
            char[] offsets = new char[numStates];
            Arrays.fill(offsets, Character.MAX_VALUE);
            maxOffset = -1;

            for (int i = 0; i < matrix.length && matrix[i].terms.length > 0; i++) {
                int maxTryOffset = maxPackSize - matrix[i].terms[matrix[i].terms.length - 1];
                for (int offset = 0; offset < maxTryOffset; offset++) {
                    if (testOffset(offset, packedTokens, matrix[i].terms, maxTermId)) {
                        char[] log = packTokens(offset, packedTokens, matrix[i].terms);
                        if (testConflicts(packedTokens, offsets, matrix, i, maxTermId)) {
                            offsets[matrix[i].state.id] = (char) offset;
                            if (maxOffset < offset) {
                                maxOffset = offset;
                            }
                            break;
                        } else {
                            // roll-back
                            System.arraycopy(log, 0, packedTokens, offset + matrix[i].terms[0], log.length);
                        }
                    }
                }
                if (offsets[matrix[i].state.id] == Character.MAX_VALUE) {
                    throw new IllegalStateException();
                }
                int maxIdx = offsets[matrix[i].state.id] + matrix[i].terms[matrix[i].terms.length - 1];
                if (lastTokenIdx < maxIdx) {
                    lastTokenIdx = maxIdx;
                }
            }
            packedStateLookaheads = new char[lastTokenIdx + 1];
            System.arraycopy(packedTokens, 0, packedStateLookaheads, 0, packedStateLookaheads.length);
            stateOffsets = offsets;
        }

        private static boolean testOffset(int offset, char[] packedTokens, char[] stateTerms, int maxTermId) {
            int idx = offset;
            for (int token = 0; token < stateTerms[0]; token++, idx++) {
                if (packedTokens[idx] == token)
                    return false;
            }
            for (int i = 0; i < stateTerms.length; i++, idx++) {
                if (stateTerms[i] != Character.MAX_VALUE) {
                    if (packedTokens[idx] != Character.MAX_VALUE && packedTokens[idx] != stateTerms[i])
                        return false;
                } else {
                    if (packedTokens[idx] == (idx - offset))
                        return false;
                }
            }
            for (int token = stateTerms[stateTerms.length - 1] + 1; token <= maxTermId; token++, idx++) {
                if (packedTokens[idx] == token)
                    return false;
            }
            return true;
        }

        private static boolean testConflicts(char[] packedTokens, char[] offsets, PackedTermLookaheads.ParserStateTermLookaheads[] matrix, int currentRow, int maxTermId) {
            for (int i = 0; i < currentRow; i++) {
                if (!testOffset(offsets[matrix[i].state.id], packedTokens, matrix[i].terms, maxTermId))
                    // conflict
                    return false;
            }
            return true;
        }

        private static char[] packTokens(int offset, char[] packedTokens, char[] stateTerms) {
            char[] log = new char[stateTerms.length];
            int idx = offset + stateTerms[0];
            System.arraycopy(packedTokens, idx, log, 0, log.length);

            for (int i = 0; i < stateTerms.length; i++, idx++) {
                if (stateTerms[i] != Character.MAX_VALUE) {
                    packedTokens[idx] = stateTerms[i];
                }
            }
            return log;
        }
    }
}