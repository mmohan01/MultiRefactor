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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeSet;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * JavaClassCharScannerWriter is a generator of CharScanner(s) that are encoded
 * as Java class files.
 */
public class JavaClassCharScannerWriter implements Opcodes {

    public void writeBaseScanner ( File outDir
                                 , String packageName
                                 , String grammarName
                                 , String baseClassName
                                 , DFA[] xdfa) throws IOException
    {
        if (packageName != null && packageName.length() > 0) {
            File pkgDir = new File(outDir, packageName.replace('.', '/'));
            if (!pkgDir.exists()) {
                pkgDir.mkdirs();
            }
            outDir = pkgDir;
        }
        PrintWriter out = new PrintWriter(new File(outDir, grammarName + baseClassName + ".java"));
        try {
            if (packageName != null && packageName.length() > 0) {
                out.println("package " + packageName + ";");
                out.println();
            }
            out.println("public abstract class " + grammarName + baseClassName + " extends beaver.CharScanner {");
            out.println("\tprotected BaseScanner(java.io.Reader sourceReader) throws java.io.IOException {");
            out.println("\t\tsuper(sourceReader);");
            out.println("\t}");
            out.println("\tpublic beaver.Term makeTerm(int id, int line, int column, Object text) {");
            out.print("\t\treturn new ");
            out.print(grammarName);
            out.println("AST.Term(id, line, column, text);");
            out.println("\t}");
            Collection reservedEvents = new HashSet();
            reservedEvents.add("Skip");
            reservedEvents.add("NewLine");
            Collection events = new TreeSet();
            for (int ai = 0; ai < xdfa.length; ai++) {
                DFA dfa = xdfa[ai];
                for (DFA.State st = dfa.start; st != null; st = st.next) {
                    if (st.patternMatchResult != null && st.patternMatchResult.events != null) {
                        for (int i = 0; i < st.patternMatchResult.events.length; i++) {
                            String eventName = st.patternMatchResult.events[i];
                            if (events.add(eventName) && !reservedEvents.contains(eventName)) {
                                out.print("\tprotected void on");
                                out.print(eventName);
                                out.println("() {");
                                out.print("\t\t// TODO: implement ");
                                out.print(eventName);
                                out.println(" event");
                                out.println("\t}");
                            }
                        }
                    }
                }
            }

            out.println("}");
        } finally {
            out.close();
        }
    }

    public void writeScanner(File outDir, String scannerClassName, String superClassName, DFA dfa) throws IOException {
        writeScanner(outDir, scannerClassName, superClassName, new DFA[] { dfa });
    }

    public byte[] generate(String className, String superClassName, RegExp re) {
        return generateClassFile(className, superClassName, new DFA[] { new DFA(new NFA(re)) });
    }

    public void writeScanner(File outDir, String scannerClassName, String superClassName, RegExp[] xre) throws IOException {
        DFA[] xdfa = new DFA[xre.length];
        for (int i = 0; i < xdfa.length; i++) {
            xdfa[i] = new DFA(new NFA(xre[i]));
        }
        writeScanner(outDir, scannerClassName, superClassName, xdfa);
    }

    public byte[] generate(String className, String superClassName, RegExp[] xre) {
        DFA[] xdfa = new DFA[xre.length];
        for (int i = 0; i < xdfa.length; i++) {
            xdfa[i] = new DFA(new NFA(xre[i]));
        }
        return generateClassFile(className, superClassName, xdfa);
    }

    public void writeScanner(File outDir, String scannerClassName, String superClassName, DFA[] xdfa) throws IOException {
        int dotPos = scannerClassName.lastIndexOf('.');
        if (dotPos > 0) {
            File pkgDir = new File(outDir, scannerClassName.substring(0, dotPos).replace('.', '/'));
            if (!pkgDir.exists()) {
                pkgDir.mkdirs();
            }
            outDir = pkgDir;
        }
        OutputStream out = new FileOutputStream(new File(outDir, scannerClassName.substring(dotPos + 1) + ".class"));
        try {
            out.write(generateClassFile(scannerClassName, superClassName, xdfa));
        } finally {
            out.close();
        }
    }

    /**
     * Generates binary representation - Class File - of the scanner.
     *
     * @param className - full name of the generated class
     * @param superClassName - full name of the scanner superclass
     * @param xdfa - array of DFAs. DFA[0] is the initial and all inclusive start condition.
     *               The rest of the array contains exclusive start conditions - one DFA per
     *               exclusive start condition.
     * @return class file bytes
     */
    private byte[] generateClassFile(String className, String superClassName, DFA[] xdfa) {
        className = className.replace('.', '/');
        superClassName = superClassName.replace('.', '/');

        Map events = new HashMap();
        // Scan all patterns and assign IDs to the "on match" events
        int lastEventId = 0;
        boolean hasSkipEvent = false;
        boolean hasStartCond = false;
        Collection statesWithMultiEventPatterns = new ArrayList();
        int totalStates = 0;

        for (int ai = 0; ai < xdfa.length; ai++) {
            DFA dfa = xdfa[ai];
            for (DFA.State st = dfa.start; st != null; st = st.next) {
                if (st.patternMatchResult != null) {
                    if (st.patternMatchResult.events != null) {
                        for (int i = 0; i < st.patternMatchResult.events.length; i++) {
                            String eventName = st.patternMatchResult.events[i];
                            if (eventName.equals("Skip")) {
                                // "Skip" event is special and has a reserved ID == 0
                                if (!hasSkipEvent) {
                                    hasSkipEvent = true;
                                    events.put(eventName, new Integer(0));
                                }
                            } else if (!events.containsKey(eventName)) {
                                events.put(eventName, new Integer(++lastEventId));
                            }
                        }
                        if (st.patternMatchResult.events.length > 1) {
                            statesWithMultiEventPatterns.add(st);
                        }
                    }
                    if (ai == 0 && !hasStartCond && st.patternMatchResult.startConditions != null) {
                        hasStartCond = true;
                    }
                }
                // Reassign state IDs to make them unique across DFAs
                st.id = totalStates++;
            }
        }
        boolean hasEvents = !events.isEmpty();
        boolean hasMultiEventStates = !statesWithMultiEventPatterns.isEmpty();

        ClassWriter cw = new ClassWriter(0);
        cw.visit(V1_1, ACC_PUBLIC | ACC_SUPER, className, null, superClassName, null);

        MethodVisitor mv;

        if (hasMultiEventStates) {
            /*
             * Store event arrays with 2 or more events as static fields. The more
             * common case of 1 event is handled specially to avoid storage and
             * initialization overhead.
             */
            mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
            for (Iterator i = statesWithMultiEventPatterns.iterator(); i.hasNext();) {
                DFA.State st = (DFA.State) i.next();
                String eventArrayFieldName = "$_" + Integer.toString(st.id, Character.MAX_RADIX);
                cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, eventArrayFieldName, "[I", null, null).visitEnd();
                loadConst(mv, st.patternMatchResult.events.length);
                mv.visitIntInsn(NEWARRAY, T_INT);
                /*
                 * Store events in the reverse order. This simplifies
                 * implementation of the event loop.
                 */
                for (int ix = st.patternMatchResult.events.length - 1; ix >= 0; ix--) {
                    mv.visitInsn(DUP);
                    loadConst(mv, ix);
                    String eventName = st.patternMatchResult.events[ix];
                    loadConst(mv, ((Integer) events.get(eventName)).intValue());
                    mv.visitInsn(IASTORE);
                }
                mv.visitFieldInsn(PUTSTATIC, className, eventArrayFieldName, "[I");
            }
            mv.visitInsn(RETURN);
            mv.visitMaxs(4, 0);
            mv.visitEnd();
        }

        // Constructor(java.io.Reader)

        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "(Ljava/io/Reader;)V", null, new String[] { "java/io/IOException" });
        mv.visitVarInsn(ALOAD, THIS);
        mv.visitVarInsn(ALOAD, 1);
        mv.visitMethodInsn(INVOKESPECIAL, superClassName, "<init>", "(Ljava/io/Reader;)V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();

        // scan method

        mv = cw.visitMethod(ACC_PUBLIC, "scan", "()I", null, new String[] { "java/io/IOException" });
        // Cache scanner state into local variables (for easier and faster access)
        mv.visitVarInsn(ALOAD, THIS);
        mv.visitFieldInsn(GETFIELD, className, "text", "[C");
        mv.visitVarInsn(ASTORE, TEXT);

        mv.visitVarInsn(ALOAD, THIS);
        mv.visitFieldInsn(GETFIELD, className, "cursor", "I");
        mv.visitVarInsn(ISTORE, CURSOR);

        mv.visitVarInsn(ALOAD, THIS);
        mv.visitFieldInsn(GETFIELD, className, "endOfText", "I");
        mv.visitVarInsn(ILOAD, CURSOR);
        mv.visitInsn(ISUB);
        mv.visitVarInsn(ISTORE, TEXT_LEN);

        mv.visitVarInsn(ALOAD, THIS);
        mv.visitFieldInsn(GETFIELD, className, "inspector", "Lbeaver/TokenInspector;");
        mv.visitVarInsn(ASTORE, INSPECTOR);

        Label startScan = new Label();
        mv.visitLabel(startScan);

        mv.visitVarInsn(ALOAD, THIS);
        mv.visitVarInsn(ILOAD, CURSOR);
        mv.visitFieldInsn(PUTFIELD, className, "start", "I");

        if (hasStartCond || xdfa.length > 1) {
            mv.visitVarInsn(ALOAD, THIS);
            mv.visitFieldInsn(GETFIELD, className, "startCond", "I");
            mv.visitVarInsn(ISTORE, STARTCOND);

            loadConst(mv, -1);
            mv.visitVarInsn(ISTORE, NEXTSTARTCOND);
        }

        loadConst(mv, -1);
        mv.visitVarInsn(ISTORE, TOKEN_END);

        loadConst(mv, -1);
        mv.visitVarInsn(ISTORE, CTXPTR);

        loadConst(mv, -1);
        mv.visitVarInsn(ISTORE, TOKEN_ID);

        loadConst(mv, -1);
        mv.visitVarInsn(ISTORE, LAST_MATCH_ID);

        loadConst(mv, -1);
        mv.visitVarInsn(ISTORE, LAST_MATCH_END);

        if (hasEvents) {
            loadConst(mv, -1);
            mv.visitVarInsn(ISTORE, EVENT_ID);

            if (hasMultiEventStates) {
                mv.visitInsn(ACONST_NULL);
                mv.visitVarInsn(ASTORE, CURRENT_EVENTS);
            }
        }

        Label[] stateLabels = new Label[totalStates];
        for (int i = 0; i < stateLabels.length; i++) {
            stateLabels[i] = new Label();
        }
        Label endOfScan = new Label(), refillBuffer = new Label();

        if (xdfa.length > 1) {
            // Use start condition to jump into the indicated DFA starting state
            Label[] startingStateLabels = new Label[xdfa.length];
            for (int i = 0; i < xdfa.length; i++) {
                startingStateLabels[i] = stateLabels[xdfa[i].start.id];
            }
            mv.visitVarInsn(ILOAD, STARTCOND);
            mv.visitTableSwitchInsn(0, xdfa.length - 1, stateLabels[0], startingStateLabels);
        }

        for (int ai = 0; ai < xdfa.length; ai++) {
            DFA dfa = xdfa[ai];
            for (DFA.State st = dfa.start; st != null; st = st.next) {
                mv.visitLabel(stateLabels[st.id]);
                if (st.patternMatchResult != null) {
                    Label endOfMatch = new Label();

                    if (st.patternMatchResult.startConditions != null) {
                        // check whether starting condition allows a match
                        if (st.patternMatchResult.startConditions.length == 1) {
                            mv.visitVarInsn(ILOAD, STARTCOND);
                            loadConst(mv, st.patternMatchResult.startConditions[0]);
                            mv.visitJumpInsn(IF_ICMPNE, endOfMatch);
                        } else {
                            Label processMatch = new Label();
                            Label[] condLabels = new Label[st.patternMatchResult.startConditions.length];
                            for (int i = 0; i < condLabels.length; i++) {
                                condLabels[i] = processMatch;
                            }
                            mv.visitVarInsn(ILOAD, STARTCOND);
                            mv.visitLookupSwitchInsn(endOfMatch, st.patternMatchResult.startConditions, condLabels);

                            mv.visitLabel(processMatch);
                        }
                    }

                    if (st.patternMatchResult.newStartCond >= 0) {
                        loadConst(mv, st.patternMatchResult.newStartCond);
                        mv.visitVarInsn(ISTORE, NEXTSTARTCOND);
                    }

                    loadConst(mv, st.patternMatchResult.id);
                    mv.visitVarInsn(ISTORE, LAST_MATCH_ID);
                    mv.visitVarInsn(ILOAD, CURSOR);
                    mv.visitVarInsn(ISTORE, LAST_MATCH_END);
                    /*
                     * Check the events.
                     * Note, that scanner returns the last matched token even if inspector rejects all matches.
                     */
                    if (hasEvents) {
                        if (st.patternMatchResult.events == null) {
                            loadConst(mv, -1);
                            mv.visitVarInsn(ISTORE, EVENT_ID);
                            if (hasMultiEventStates) {
                                mv.visitInsn(ACONST_NULL);
                                mv.visitVarInsn(ASTORE, CURRENT_EVENTS);
                            }
                        } else if (st.patternMatchResult.events.length == 1) {
                            loadConst(mv, ((Integer) events.get(st.patternMatchResult.events[0])).intValue());
                            mv.visitVarInsn(ISTORE, EVENT_ID);
                            /*
                             * no need to reset the "events" pointer as it is
                             * only checked if the "event" is -1
                             */
                        } else {
                            loadConst(mv, -1);
                            mv.visitVarInsn(ISTORE, EVENT_ID);
                            String eventArrayFieldName = "$_" + Integer.toString(st.id, Character.MAX_RADIX);
                            mv.visitFieldInsn(GETSTATIC, className, eventArrayFieldName, "[I");
                            mv.visitVarInsn(ASTORE, CURRENT_EVENTS);
                        }
                    }
                    Label saveMatch = new Label();
                    mv.visitVarInsn(ALOAD, INSPECTOR);
                    mv.visitJumpInsn(IFNULL, saveMatch);

                    mv.visitVarInsn(ALOAD, INSPECTOR);
                    loadConst(mv, st.patternMatchResult.id);
                    mv.visitMethodInsn(INVOKEINTERFACE, "beaver/TokenInspector", "isValid", "(I)Z");
                    mv.visitJumpInsn(IFEQ, endOfMatch);

                    mv.visitLabel(saveMatch);
                    loadConst(mv, st.patternMatchResult.id);
                    mv.visitVarInsn(ISTORE, TOKEN_ID);
                    /*
                     * Save the cursor, so if this is not a final state and we
                     * continue scanning and an unexpected character is encountered,
                     * then we return saved "result" and roll-back cursor to the
                     * marked position.
                     */
                    mv.visitVarInsn(ILOAD, CURSOR);
                    mv.visitVarInsn(ISTORE, TOKEN_END);

                    mv.visitLabel(endOfMatch);
                }
                if (st.firstTransition == null) { // then this is a final state
                    // sanity check
                    if (st.patternMatchResult == null) {
                        throw new IllegalStateException("Final state " + st.id + " does not have a pattern match result");
                    }
                    mv.visitJumpInsn(GOTO, endOfScan);
                } else {
                    if (st.isPreCtx) {
                        // automaton has to recognize the entire pattern before the token is returned, but
                        // the latter will span only up to the context marker
                        mv.visitVarInsn(ILOAD, CURSOR);
                        mv.visitVarInsn(ISTORE, CTXPTR);
                    }
                    if (st.sccDepth > 0) {
                        Label loadCurrentChar = new Label();
                        mv.visitVarInsn(ILOAD, TEXT_LEN);
                        loadConst(mv, st.sccDepth);
                        mv.visitJumpInsn(IF_ICMPGE, loadCurrentChar);

                        loadConst(mv, st.sccDepth);
                        mv.visitJumpInsn(JSR, refillBuffer);

                        mv.visitLabel(loadCurrentChar);
                    }
                    mv.visitVarInsn(ALOAD, TEXT);
                    mv.visitVarInsn(ILOAD, CURSOR);
                    mv.visitInsn(CALOAD);
                    mv.visitVarInsn(ISTORE, CURCHR);

                    mv.visitIincInsn(CURSOR, 1); // cursor now points to the next character
                    mv.visitIincInsn(TEXT_LEN, -1);

                    Collection stateTransitions = SpanTransition.forState(st);
                    SpanTransition[] singleCharTransitions = SpanTransition.getSingleCharTransitions(stateTransitions);
                    SpanTransition[] charRangeTransitions = SpanTransition.getCharRangeTransitions(stateTransitions);
                    if (singleCharTransitions.length >= 4 ) {
                        int[] onChars = new int[singleCharTransitions.length];
                        Label[] gotoStates = new Label[singleCharTransitions.length];
                        for (int i = 0; i < singleCharTransitions.length; i++) {
                            SpanTransition tr = singleCharTransitions[i];
                            onChars[i] = tr.onChars.lb;
                            gotoStates[i] = stateLabels[tr.toState.id];
                        }
                        Label nomatch = (charRangeTransitions.length > 0 ? new Label() : endOfScan);
                        mv.visitVarInsn(ILOAD, CURCHR);
                        mv.visitLookupSwitchInsn(nomatch, onChars, gotoStates);
                        if (charRangeTransitions.length > 0) {
                            mv.visitLabel(nomatch);
                        }
                    } else {
                        for (int i = 0; i < singleCharTransitions.length; i++) {
                            SpanTransition tr = singleCharTransitions[i];
                            mv.visitVarInsn(ILOAD, CURCHR);
                            loadConst(mv, tr.onChars.lb);
                            mv.visitJumpInsn(IF_ICMPEQ, stateLabels[tr.toState.id]);
                        }
                        if (charRangeTransitions.length == 0) {
                            mv.visitJumpInsn(GOTO, endOfScan);
                        }
                    }
                    if (charRangeTransitions.length > 0) {
                        visit(charRangeTransitions, 0, charRangeTransitions.length, mv, stateLabels, endOfScan);
                    }
                }
            }
        }

        Label saveCursor = new Label();
        Label moveCursorToContext = new Label();
        Label unexpectedCharacter = new Label();
        Label checkAnythingMatched = new Label();

        mv.visitLabel(checkAnythingMatched);
        mv.visitVarInsn(ILOAD, LAST_MATCH_ID);
        mv.visitJumpInsn(IFLT, unexpectedCharacter);
        mv.visitVarInsn(ILOAD, LAST_MATCH_ID);
        mv.visitVarInsn(ISTORE, TOKEN_ID);
        mv.visitVarInsn(ILOAD, LAST_MATCH_END);
        mv.visitVarInsn(ISTORE, CURSOR);
        mv.visitJumpInsn(GOTO, saveCursor);

        mv.visitLabel(unexpectedCharacter);
        // Check for EOF
        mv.visitVarInsn(ILOAD, CURCHR);
        loadConst(mv, 0xffff);
        mv.visitJumpInsn(IF_ICMPNE, saveCursor);
        // shift cursor back to exclude sentinel
        mv.visitIincInsn(CURSOR, -1);
        // We are at EOF if and only if "start" also points to it
        mv.visitVarInsn(ALOAD, THIS);
        mv.visitFieldInsn(GETFIELD, className, "start", "I");
        mv.visitVarInsn(ILOAD, CURSOR);
        mv.visitJumpInsn(IF_ICMPNE, saveCursor);
        // return EOF
        loadConst(mv, 0);
        mv.visitVarInsn(ISTORE, TOKEN_ID);
        mv.visitJumpInsn(GOTO, saveCursor);

        mv.visitLabel(moveCursorToContext);
        mv.visitVarInsn(ILOAD, CTXPTR);
        mv.visitVarInsn(ISTORE, CURSOR);
        mv.visitJumpInsn(GOTO, saveCursor);

        mv.visitLabel(endOfScan);
        // Check whether a token was recognized and approved
        mv.visitVarInsn(ILOAD, TOKEN_ID);
        mv.visitJumpInsn(IFLT, checkAnythingMatched);
        // Check whether a token was recognized after a trailing context matched
        mv.visitVarInsn(ILOAD, CTXPTR);
        mv.visitJumpInsn(IFGE, moveCursorToContext);
        // Reset cursor to the position it was when the token was recognized.
        mv.visitVarInsn(ILOAD, TOKEN_END);
        mv.visitVarInsn(ISTORE, CURSOR);

        mv.visitLabel(saveCursor);
        mv.visitVarInsn(ALOAD, THIS);
        mv.visitVarInsn(ILOAD, CURSOR);
        mv.visitFieldInsn(PUTFIELD, className, "cursor", "I");

        if (hasStartCond || xdfa.length > 1) {
            Label startCondSet = new Label();

            mv.visitVarInsn(ILOAD, NEXTSTARTCOND);
            mv.visitJumpInsn(IFLT, startCondSet);
            mv.visitVarInsn(ALOAD, THIS);
            mv.visitVarInsn(ILOAD, NEXTSTARTCOND);
            mv.visitFieldInsn(PUTFIELD, className, "startCond", "I");

            mv.visitLabel(startCondSet);
        }

        // invoke the event if one exists and return the result
        if (hasEvents) {

            if (lastEventId == 0) {
                // "Skip" is the only event
                mv.visitVarInsn(ILOAD, EVENT_ID);
                mv.visitJumpInsn(IFGE, startScan);
            } else {
                Label[] eventCases = new Label[events.size()];
                for (int i = 0; i < eventCases.length; i++) {
                    eventCases[i] = new Label();
                }
                // Prepare "Event ID" to "Event Name" map
                String[] eventNames = new String[lastEventId + 1];
                for (Iterator i = events.entrySet().iterator(); i.hasNext();) {
                    Map.Entry e = (Map.Entry) i.next();
                    String name = (String) e.getKey();
                    Integer eid = (Integer) e.getValue();
                    eventNames[eid.intValue()] = name;
                }

                Label noCurrentEvents = new Label();
                Label nextEvent = new Label();
                Label endOfEvents = new Label();

                if (hasMultiEventStates) {
                    Label fireEvent = new Label();
                    Label checkMultipleEvents = new Label();
                    mv.visitVarInsn(ILOAD, EVENT_ID);
                    mv.visitJumpInsn(IFLT, checkMultipleEvents);

                    // pretend this is an array with 1 element
                    loadConst(mv, 0); // element "index"
                    mv.visitVarInsn(ISTORE, EVENT_IDX);
                    mv.visitVarInsn(ILOAD, EVENT_ID);
                    mv.visitJumpInsn(GOTO, fireEvent);

                    mv.visitLabel(checkMultipleEvents);
                    mv.visitVarInsn(ALOAD, CURRENT_EVENTS);
                    mv.visitJumpInsn(IFNULL, noCurrentEvents);

                    // temp_event_index = events.length - 1;
                    mv.visitVarInsn(ALOAD, CURRENT_EVENTS);
                    mv.visitInsn(ARRAYLENGTH);
                    mv.visitVarInsn(ISTORE, EVENT_IDX);

                    mv.visitLabel(nextEvent);
                    mv.visitIincInsn(EVENT_IDX, -1);
                    mv.visitVarInsn(ILOAD, EVENT_IDX);
                    mv.visitJumpInsn(IFLT, endOfEvents);

                    mv.visitVarInsn(ALOAD, CURRENT_EVENTS);
                    mv.visitVarInsn(ILOAD, EVENT_IDX);
                    mv.visitInsn(IALOAD);

                    mv.visitLabel(fireEvent);
                } else {
                    mv.visitVarInsn(ILOAD, EVENT_ID);
                    mv.visitJumpInsn(IFLT, noCurrentEvents);
                    mv.visitVarInsn(ILOAD, EVENT_ID);
                }

                int i = 0;
                if (hasSkipEvent) {
                    mv.visitTableSwitchInsn(0, lastEventId, endOfEvents, eventCases);
                    mv.visitLabel(eventCases[i++]);
                    // Replace the result (token ID) with -1, to restart the scan without returning the token
                    loadConst(mv, -1);
                    mv.visitVarInsn(ISTORE, TOKEN_ID);
                    if (hasMultiEventStates) {
                        mv.visitJumpInsn(GOTO, nextEvent);
                    } else {
                        mv.visitJumpInsn(GOTO, endOfEvents);
                    }
                } else {
                    mv.visitTableSwitchInsn(1, lastEventId, endOfEvents, eventCases);
                }

                while (i < eventCases.length) {
                    mv.visitLabel(eventCases[i]);
                    mv.visitVarInsn(ALOAD, THIS);
                    mv.visitMethodInsn(INVOKEVIRTUAL, className, "on" + eventNames[i++], "()V");
                    if (hasMultiEventStates) {
                        mv.visitJumpInsn(GOTO, nextEvent);
                    } else if (i < eventCases.length - 1) {
                        mv.visitJumpInsn(GOTO, endOfEvents);
                    }
                }
                mv.visitLabel(endOfEvents);
                // if "Skip" replaced the token, restart the scan
                mv.visitVarInsn(ILOAD, TOKEN_ID);
                mv.visitJumpInsn(IFLT, startScan);

                mv.visitLabel(noCurrentEvents);
            }
        }
        mv.visitVarInsn(ILOAD, TOKEN_ID);
        mv.visitInsn(IRETURN);

        mv.visitLabel(refillBuffer);
        mv.visitVarInsn(ASTORE, RET_ADDR);
        mv.visitVarInsn(ALOAD, THIS);
        mv.visitInsn(SWAP);
        mv.visitVarInsn(ILOAD, TEXT_LEN);
        mv.visitInsn(ISUB);
        mv.visitMethodInsn(INVOKEVIRTUAL, className, "refillBuffer", "(I)I");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ILOAD, CURSOR);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ISTORE, CURSOR);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ILOAD, TOKEN_END);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ISTORE, TOKEN_END);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ILOAD, CTXPTR);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ISTORE, CTXPTR);
        mv.visitVarInsn(ILOAD, LAST_MATCH_END);
        mv.visitInsn(IADD);
        mv.visitVarInsn(ISTORE, LAST_MATCH_END);
        // update remaining text length
        mv.visitVarInsn(ALOAD, THIS);
        mv.visitFieldInsn(GETFIELD, className, "endOfText", "I");
        mv.visitVarInsn(ILOAD, CURSOR);
        mv.visitInsn(ISUB);
        mv.visitVarInsn(ISTORE, TEXT_LEN);
        // reload "text" reference that could have been updated by refillBuffer
        mv.visitVarInsn(ALOAD, THIS);
        mv.visitFieldInsn(GETFIELD, className, "text", "[C");
        mv.visitVarInsn(ASTORE, TEXT);
        mv.visitVarInsn(RET, RET_ADDR);

        mv.visitMaxs(4, NUMLOCALS);
        mv.visitEnd();

        return cw.toByteArray();
    }

    /**
     * THIS is this :-)
     */
    private static final int THIS           = 0;
    /**
     * CURSOR is the index of the current character while the scan is in
     * progress
     */
    private static final int CURSOR         = 1;
    /**
     * TOKEN_END marks a position of the cursor when a token is recognized and
     * is approved by the inspector.
     */
    private static final int TOKEN_END      = 2;
    /**
     * CURCHR caches the current character to minimize array accesses
     */
    private static final int CURCHR         = 3;
    /**
     * TOKEN_ID saves the ID of the last recognized token. Valid IDs are 0 and
     * positive numbers. -1, the initial value, signals that an
     * "unexpected character" is encountered.
     */
    private static final int TOKEN_ID       = 4;
    /**
     * TEXT is a locally cached CharScanner.text
     */
    private static final int TEXT           = 5;
    /**
     * TEXT_LEN keeps the number of remaining characters is the text buffer.
     */
    private static final int TEXT_LEN       = 6;
    /**
     * CTXPTR marks cursor position at the beginning of a trailing context
     */
    private static final int CTXPTR         = 7;
    /**
     * EVENT keeps ID of the event that will be fired on final recognition
     */
    private static final int EVENT_ID       = 8;
    /**
     * EVENTS keeps a reference to an array of events to be fired when a token
     * is accepted. Note, that "event" (single) and "events" (multiple) are
     * mutually exclusive.
     */
    private static final int CURRENT_EVENTS = 9;
    /**
     * EVENTIDX is an events array index that is used to loop through the events.
     */
    private static final int EVENT_IDX      = 10;
    /**
     * RET_ADDR is a return address for "refill buffer" JSR calls
     */
    private static final int RET_ADDR       = 11;
    /**
     * INSPECTOR a local copy of super.inspector
     */
    private static final int INSPECTOR      = 12;
    /**
     * LAST_MATCH_ID is an ID of the last match.
     */
    private static final int LAST_MATCH_ID  = 13;
    /**
     * LAST_MATCH_END keeps a position of the cursor when a token is recognized, but not
     * yet approved by the inspector.
     */
    private static final int LAST_MATCH_END = 14;
    /**
     * STARTCOND starting condition of the next scan
     */
    private static final int STARTCOND      = 15;
    /**
     * NEXTSTARTCOND = starting condition of the next scan
     */
    private static final int NEXTSTARTCOND  = 16;
    /**
     * NUMLOCALS is a number of local variable slots at this method frame. Used for visitMaxs
     */
    private static final int NUMLOCALS      = 17;

    /**
     * loadConst generates an instruction to push a given constant onto a stack.
     *
     * @param mv
     *            Method visitor
     * @param c
     *            constant
     */
    private static void loadConst(MethodVisitor mv, int c) {
        if (Byte.MIN_VALUE <= c && c <= Byte.MAX_VALUE) {
            switch (c) {
                case -1:
                    mv.visitInsn(ICONST_M1);
                    break;
                case 0:
                    mv.visitInsn(ICONST_0);
                    break;
                case 1:
                    mv.visitInsn(ICONST_1);
                    break;
                case 2:
                    mv.visitInsn(ICONST_2);
                    break;
                case 3:
                    mv.visitInsn(ICONST_3);
                    break;
                case 4:
                    mv.visitInsn(ICONST_4);
                    break;
                case 5:
                    mv.visitInsn(ICONST_5);
                    break;
                default:
                    mv.visitIntInsn(BIPUSH, c);
            }
        } else if (Short.MIN_VALUE <= c && c <= Short.MAX_VALUE) {
            mv.visitIntInsn(SIPUSH, c);
        } else {
            mv.visitLdcInsn(new Integer(c));
        }
    }

    /**
     * DFA state transitions use character ranges per each destination. To
     * encode automaton we need to split entire set of characters recognizable
     * by a state into ordered collection of spans.
     */
    static class SpanTransition implements Comparable {
        CharSpan  onChars;
        DFA.State toState;

        private SpanTransition(CharSpan onChars, DFA.State toState) {
            this.onChars = onChars;
            this.toState = toState;
        }

        public int compareTo(Object anotherTransition) {
            return onChars.lb - ((SpanTransition) anotherTransition).onChars.lb;
        }

        public String toString() {
            return onChars.toString() + " -> " + toState.id;
        }

        static Collection forState(DFA.State state) {
            List onSpanTransitions = new ArrayList();
            for (DFA.State.Transition tr = state.firstTransition; tr != null; tr = tr.next) {
                for (int i = 0; i < tr.onChars.spans.length; i++) {
                    CharSpan span = tr.onChars.spans[i];
                    onSpanTransitions.add(new SpanTransition(span, tr.toState));
                }
            }
            Collections.sort(onSpanTransitions);
            return onSpanTransitions;
        }

        static SpanTransition[] getSingleCharTransitions(Collection stateTransitions) {
            Collection singleCharTransitions = new ArrayList();
            for (Iterator i = stateTransitions.iterator(); i.hasNext();) {
                SpanTransition t = (SpanTransition) i.next();
                if (t.onChars.size() == 1) {
                    singleCharTransitions.add(t);
                }
            }
            return (SpanTransition[]) singleCharTransitions.toArray(new SpanTransition[singleCharTransitions.size()]);
        }

        static SpanTransition[] getCharRangeTransitions(Collection stateTransitions) {
            Collection charRangeTransitions = new ArrayList();
            for (Iterator i = stateTransitions.iterator(); i.hasNext();) {
                SpanTransition t = (SpanTransition) i.next();
                if (t.onChars.size() > 1) {
                    charRangeTransitions.add(t);
                }
            }
            return (SpanTransition[]) charRangeTransitions.toArray(new SpanTransition[charRangeTransitions.size()]);
        }
    }

    private static void visit(SpanTransition[] tr, int l, int r, MethodVisitor mv, Label[] stateLabels, Label unexpectedChar) {
        switch (r - l) {
            case 1: {
                int i = l;
                if (tr[i].onChars.size() == 1) {
                    mv.visitVarInsn(ILOAD, CURCHR);
                    loadConst(mv, tr[i].onChars.lb);
                    mv.visitJumpInsn(IF_ICMPEQ, stateLabels[tr[i].toState.id]);
                } else {
                    mv.visitVarInsn(ILOAD, CURCHR);
                    loadConst(mv, tr[i].onChars.lb);
                    mv.visitJumpInsn(IF_ICMPLT, unexpectedChar);
                    mv.visitVarInsn(ILOAD, CURCHR);
                    loadConst(mv, tr[i].onChars.ub);
                    mv.visitJumpInsn(IF_ICMPLT, stateLabels[tr[i].toState.id]);
                }
                mv.visitJumpInsn(GOTO, unexpectedChar);
                break;
            }
            case 2: {
                int i = l;
                if (tr[i].onChars.size() == 1) {
                    mv.visitVarInsn(ILOAD, CURCHR);
                    loadConst(mv, tr[i].onChars.lb);
                    mv.visitJumpInsn(IF_ICMPEQ, stateLabels[tr[i].toState.id]);
                } else {
                    mv.visitVarInsn(ILOAD, CURCHR);
                    loadConst(mv, tr[i].onChars.lb);
                    mv.visitJumpInsn(IF_ICMPLT, unexpectedChar); // unexpected
                                                                 // character
                    mv.visitVarInsn(ILOAD, CURCHR);
                    loadConst(mv, tr[i].onChars.ub);
                    mv.visitJumpInsn(IF_ICMPLT, stateLabels[tr[i].toState.id]);
                }
                i++;
                if (tr[i].onChars.size() == 1) {
                    mv.visitVarInsn(ILOAD, CURCHR);
                    loadConst(mv, tr[i].onChars.lb);
                    mv.visitJumpInsn(IF_ICMPEQ, stateLabels[tr[i].toState.id]);
                } else {
                    mv.visitVarInsn(ILOAD, CURCHR);
                    loadConst(mv, tr[i].onChars.lb);
                    mv.visitJumpInsn(IF_ICMPLT, unexpectedChar);
                    mv.visitVarInsn(ILOAD, CURCHR);
                    loadConst(mv, tr[i].onChars.ub);
                    mv.visitJumpInsn(IF_ICMPLT, stateLabels[tr[i].toState.id]);
                }
                mv.visitJumpInsn(GOTO, unexpectedChar);
                break;
            }
            default: {
                int m = (r + l) / 2;
                if (tr[m].onChars.size() == 1) {
                    mv.visitVarInsn(ILOAD, CURCHR);
                    loadConst(mv, tr[m].onChars.lb);
                    mv.visitJumpInsn(IF_ICMPEQ, stateLabels[tr[m].toState.id]);
                    Label checkRightSubtree = new Label();
                    mv.visitVarInsn(ILOAD, CURCHR);
                    loadConst(mv, tr[m].onChars.lb);
                    mv.visitJumpInsn(IF_ICMPGT, checkRightSubtree);
                    visit(tr, l, m, mv, stateLabels, unexpectedChar);
                    mv.visitLabel(checkRightSubtree);
                    visit(tr, m + 1, r, mv, stateLabels, unexpectedChar);
                } else {
                    Label checkUpperBoundary = new Label();
                    mv.visitVarInsn(ILOAD, CURCHR);
                    loadConst(mv, tr[m].onChars.lb);
                    mv.visitJumpInsn(IF_ICMPGE, checkUpperBoundary);
                    visit(tr, l, m, mv, stateLabels, unexpectedChar);
                    mv.visitLabel(checkUpperBoundary);
                    mv.visitVarInsn(ILOAD, CURCHR);
                    loadConst(mv, tr[m].onChars.ub);
                    mv.visitJumpInsn(IF_ICMPLT, stateLabels[tr[m].toState.id]);
                    visit(tr, m + 1, r, mv, stateLabels, unexpectedChar);
                }
            }
        }
    }
}
