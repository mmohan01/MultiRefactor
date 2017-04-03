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
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Arrays;

import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import beaver.cc.parser.ParserAction.Reduce;

/**
 *
 */
public class JavaClassParserWriter extends ParserWriter implements Opcodes {

    private final boolean genTrace;
    private final boolean delegateReduceActions;

    public JavaClassParserWriter() {
        genTrace = false;
        delegateReduceActions = false;
    }

    public JavaClassParserWriter(boolean noAst, boolean trace) {
        genTrace = trace;
        delegateReduceActions = noAst;
    }

    public void writeParser ( File outDir
                            , String parserClassName
                            , String baseParserClassName
                            , String astRootClassName
                            , Grammar grammar
                            , ParserState firstState) throws IOException
    {
        int dotPos = parserClassName.lastIndexOf('.');
        if (dotPos > 0) {
            File pkgDir = new File(outDir, parserClassName.substring(0, dotPos).replace('.', '/'));
            if (!pkgDir.exists()) {
                pkgDir.mkdirs();
            }
            outDir = pkgDir;
        }
        OutputStream out = new FileOutputStream(new File(outDir, parserClassName.substring(dotPos + 1) + ".class"));
        try {
            out.write(generate(parserClassName, baseParserClassName, astRootClassName, grammar, firstState));
        } finally {
            out.close();
        }
    }

    public byte[] generate ( String parserClassName
                           , String baseParserClassName
                           , String astRootClassName
                           , Grammar grammar
                           , ParserState firstState)
    {
        parserClassName = parserClassName.replace('.', '/');
        baseParserClassName = baseParserClassName.replace('.', '/');
        String astClassPrefix = astRootClassName.replace('.', '/') + '$';

        ClassWriter cw = new ClassWriter(0);
        cw.visit(V1_1, ACC_PUBLIC | ACC_SUPER, parserClassName, null, baseParserClassName, null);

        ParserWriter.PackedTermLookaheads pack = new ParserWriter.PackedTermLookaheads(firstState);
        int numStates = pack.stateOffsets.length;

        MethodVisitor mv = cw.visitMethod(ACC_STATIC, "<clinit>", "()V", null, null);
        String tokensArrayType, offsetsArrayType;
        if (pack.maxTermId < 256) {
            // use byte array
            tokensArrayType = "[B";
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "tokens", tokensArrayType, null, null).visitEnd();
            mv.visitLdcInsn(new String(pack.packedStateLookaheads));
            mv.visitMethodInsn(INVOKESTATIC, "beaver/Parser", "toByteArray", "(Ljava/lang/String;)[B");
            mv.visitFieldInsn(PUTSTATIC, parserClassName, "tokens", tokensArrayType);
        } else {
            tokensArrayType = "[C";
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "tokens", tokensArrayType, null, null).visitEnd();
            mv.visitLdcInsn(new String(pack.packedStateLookaheads));
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C");
            mv.visitFieldInsn(PUTSTATIC, parserClassName, "tokens", tokensArrayType);
        }
        if (pack.maxOffset < 256) {
            offsetsArrayType = "[B";
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "offsets", offsetsArrayType, null, null).visitEnd();
            mv.visitLdcInsn(new String(pack.stateOffsets));
            mv.visitMethodInsn(INVOKESTATIC, "beaver/Parser", "toByteArray", "(Ljava/lang/String;)[B");
            mv.visitFieldInsn(PUTSTATIC, parserClassName, "offsets", offsetsArrayType);
        } else {
            offsetsArrayType = "[C";
            cw.visitField(ACC_PRIVATE | ACC_STATIC | ACC_FINAL, "offsets", offsetsArrayType, null, null).visitEnd();
            mv.visitLdcInsn(new String(pack.stateOffsets));
            mv.visitMethodInsn(INVOKEVIRTUAL, "java/lang/String", "toCharArray", "()[C");
            mv.visitFieldInsn(PUTSTATIC, parserClassName, "offsets", offsetsArrayType);
        }
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 0);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC, "<init>", "()V", null, null);
        mv.visitVarInsn(ALOAD, THIS);
        mv.visitMethodInsn(INVOKESPECIAL, baseParserClassName, "<init>", "()V");
        mv.visitInsn(RETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PROTECTED, "getErrorTokenId", "()I", null, null);
        loadConst(mv, grammar.getError() != null ? grammar.getError().id : -1);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PROTECTED, "getLastTokenId", "()I", null, null);
        loadConst(mv, grammar.getLastTerminalId());
        mv.visitInsn(IRETURN);
        mv.visitMaxs(1, 1);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC, "isKeyword", "(I)Z", null, null);
        Label returnNotAKeyword = new Label();
        mv.visitVarInsn(ILOAD, 1); // tokenId
        loadConst(mv, grammar.getLastKeyword().id);
        mv.visitJumpInsn(IF_ICMPGT, returnNotAKeyword);
        loadConst(mv, 1);
        mv.visitInsn(IRETURN);
        mv.visitLabel(returnNotAKeyword);
        loadConst(mv, 0);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(2, 2);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PUBLIC, "isValid", "(I)Z", null, null);
        Label returnNotValid = new Label();
        mv.visitFieldInsn(GETSTATIC, parserClassName, "offsets", offsetsArrayType);
        mv.visitVarInsn(ALOAD, 0); // this
        mv.visitFieldInsn(GETFIELD, parserClassName, "state", "I");
        if (offsetsArrayType.equals("[B")) {
            mv.visitInsn(BALOAD);
            loadConst(mv, 255);
            mv.visitInsn(IAND);
        } else {
            mv.visitInsn(CALOAD);
        }
        mv.visitVarInsn(ILOAD, 1); // tokenId
        mv.visitInsn(IADD);
        mv.visitInsn(DUP);
        mv.visitVarInsn(ISTORE, 2); // index
        mv.visitJumpInsn(IFLT, returnNotValid);
        mv.visitVarInsn(ILOAD, 2); // index
        loadConst(mv, pack.packedStateLookaheads.length);
        mv.visitJumpInsn(IF_ICMPGE, returnNotValid);
        mv.visitFieldInsn(GETSTATIC, parserClassName, "tokens", tokensArrayType);
        mv.visitVarInsn(ILOAD, 2); // index
        if (tokensArrayType.equals("[B")) {
            mv.visitInsn(BALOAD);
            loadConst(mv, 255);
            mv.visitInsn(IAND);
        } else {
            mv.visitInsn(CALOAD);
        }
        mv.visitVarInsn(ILOAD, 1); // tokenId
        mv.visitJumpInsn(IF_ICMPNE, returnNotValid);
        loadConst(mv, 1);
        mv.visitInsn(IRETURN);
        mv.visitLabel(returnNotValid);
        loadConst(mv, 0);
        mv.visitInsn(IRETURN);
        mv.visitMaxs(2, 3);
        mv.visitEnd();

        mv = cw.visitMethod(ACC_PROTECTED, "parse", "(Lbeaver/Scanner;I)Z", null, null);
        Label[] states = new Label[numStates];
        for (int i = 0; i < states.length; i++) {
            states[i] = new Label();
        }
        Label invalidState = new Label();

        /*
         * Saved lookahead ID is actually set by a reduce action. However it will be only used after the nonterminal is
         * shifted. This is too much however for the byte code verifier, that sees it as uninitialized.
         */
        loadConst(mv, 0);
        mv.visitVarInsn(ISTORE, SAVED_LOOKAHEAD_ID);

        // setup a local copy of the parser state for easier/faster access
        mv.visitVarInsn(ALOAD, THIS);
        mv.visitFieldInsn(GETFIELD, parserClassName, "states", "[C");
        mv.visitVarInsn(ASTORE, STATES);

        mv.visitVarInsn(ALOAD, THIS);
        mv.visitFieldInsn(GETFIELD, parserClassName, "symbols", "[Ljava/lang/Object;");
        mv.visitVarInsn(ASTORE, SYMBOLS);

        mv.visitVarInsn(ALOAD, THIS);
        mv.visitFieldInsn(GETFIELD, parserClassName, "sp", "I");
        mv.visitVarInsn(ISTORE, SP);

        // load the initial state
        mv.visitVarInsn(ALOAD, STATES);
        mv.visitVarInsn(ILOAD, SP);
        mv.visitInsn(CALOAD); // the initial state is still on JVM stack at this point
        // unwind the stack so we can jump into the "go to state" code
        mv.visitIincInsn(SP, -1);

        Label getNextToken = new Label();
        mv.visitLabel(getNextToken);
        // before we load the next token we need to store the current state for the "isValid" method
        mv.visitInsn(DUP);
        mv.visitVarInsn(ALOAD, THIS);
        mv.visitInsn(SWAP);
        mv.visitFieldInsn(PUTFIELD, parserClassName, "state", "I");
        // now request the next token from the scanner
        mv.visitVarInsn(ALOAD, SCANNER);
        mv.visitMethodInsn(INVOKEINTERFACE, "beaver/Scanner", "getNextToken", "()Lbeaver/Term;");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ASTORE, TOKEN);
        mv.visitMethodInsn(INVOKEINTERFACE, "beaver/Term", "getId", "()I");
        mv.visitVarInsn(ISTORE, LOOKAHEAD_ID);

        // Check whether we are in a recovery mode
        Label checkErrorRecoveryProgress = new Label();
        mv.visitVarInsn(ILOAD, NUM_SHIFTS);
        mv.visitJumpInsn(IFGT, checkErrorRecoveryProgress);

        Label gotoState = new Label();
        mv.visitLabel(gotoState);
        mv.visitVarInsn(ISTORE, STATE);
        mv.visitIincInsn(SP, 1);
        Label tryPushState = new Label(), endPushState = new Label(), expandStacks = new Label();
        mv.visitTryCatchBlock(tryPushState, endPushState, expandStacks, "java/lang/ArrayIndexOutOfBoundsException");
        mv.visitLabel(tryPushState);
        mv.visitVarInsn(ALOAD, STATES);
        mv.visitVarInsn(ILOAD, SP);
        mv.visitVarInsn(ILOAD, STATE);
        mv.visitInsn(CASTORE);
        mv.visitLabel(endPushState);

        Label switchState = new Label();
        mv.visitLabel(switchState);
        if (genTrace) {
            mv.visitVarInsn(ALOAD, THIS);
            mv.visitVarInsn(ILOAD, STATE);
            mv.visitVarInsn(ILOAD, LOOKAHEAD_ID);
            mv.visitVarInsn(ALOAD, TOKEN);
            mv.visitVarInsn(ALOAD, SYMBOLS);
            mv.visitVarInsn(ILOAD, SP);
            mv.visitInsn(AALOAD);
            mv.visitMethodInsn(INVOKEVIRTUAL, parserClassName, "enter", "(IILbeaver/Term;Ljava/lang/Object;)V");
        }
        mv.visitVarInsn(ILOAD, LOOKAHEAD_ID);
        mv.visitVarInsn(ILOAD, STATE);
        mv.visitTableSwitchInsn(0, numStates - 1, invalidState, states);

        mv.visitLabel(invalidState);
        mv.visitTypeInsn(NEW, "java/lang/IllegalStateException");
        mv.visitInsn(DUP);
        mv.visitVarInsn(ILOAD, STATE);
        mv.visitMethodInsn(INVOKESTATIC, "java/lang/Integer", "toString", "(I)Ljava/lang/String;");
        mv.visitMethodInsn(INVOKESPECIAL, "java/lang/IllegalStateException", "<init>", "(Ljava/lang/String;)V");
        mv.visitInsn(ATHROW);

        mv.visitLabel(expandStacks);
        mv.visitJumpInsn(IFNULL, tryPushState);
        mv.visitVarInsn(ALOAD, STATES);
        mv.visitMethodInsn(INVOKESTATIC, "beaver/Parser", "expand", "([C)[C");
        mv.visitVarInsn(ASTORE, STATES);
        mv.visitVarInsn(ALOAD, THIS);
        mv.visitVarInsn(ALOAD, STATES);
        mv.visitFieldInsn(PUTFIELD, parserClassName, "states", "[C");
        mv.visitVarInsn(ALOAD, SYMBOLS);
        mv.visitMethodInsn(INVOKESTATIC, "beaver/Parser", "expand", "([Ljava/lang/Object;)[Ljava/lang/Object;");
        mv.visitVarInsn(ASTORE, SYMBOLS);
        mv.visitVarInsn(ALOAD, THIS);
        mv.visitVarInsn(ALOAD, SYMBOLS);
        mv.visitFieldInsn(PUTFIELD, parserClassName, "symbols", "[Ljava/lang/Object;");
        mv.visitJumpInsn(GOTO, tryPushState);

        mv.visitLabel(checkErrorRecoveryProgress);
        mv.visitIincInsn(NUM_SHIFTS, -1);
        mv.visitVarInsn(ILOAD, NUM_SHIFTS);
        mv.visitJumpInsn(IFGT, gotoState);
        mv.visitInsn(POP); // remove the next state No from the JVM stack
        loadConst(mv, 1);
        mv.visitInsn(IRETURN);

        Label shiftTerminal = new Label();
        mv.visitLabel(shiftTerminal);
        mv.visitVarInsn(ALOAD, SYMBOLS);
        mv.visitVarInsn(ILOAD, SP);
        mv.visitVarInsn(ALOAD, TOKEN);
        mv.visitInsn(AASTORE);
        if (genTrace) {
            mv.visitVarInsn(ALOAD, THIS);
            mv.visitVarInsn(ALOAD, TOKEN);
            mv.visitMethodInsn(INVOKEVIRTUAL, parserClassName, "shift", "(Lbeaver/Term;)V");
        }
        // the "go to" state is still on the JVM stack
        mv.visitJumpInsn(GOTO, getNextToken);

        Label shiftNonterminal = new Label();
        mv.visitLabel(shiftNonterminal);
        mv.visitVarInsn(ILOAD, SAVED_LOOKAHEAD_ID);
        mv.visitVarInsn(ISTORE, LOOKAHEAD_ID);
        // the "go to" state is still on the JVM stack
        mv.visitJumpInsn(GOTO, gotoState);

        Label findNonterminalAction = new Label();
        mv.visitLabel(findNonterminalAction);
        mv.visitVarInsn(ALOAD, STATES);
        mv.visitVarInsn(ILOAD, SP);
        mv.visitInsn(CALOAD);
        mv.visitVarInsn(ISTORE, STATE);
        mv.visitJumpInsn(GOTO, switchState);

        Label syntaxError = new Label();
        mv.visitLabel(syntaxError);
        // update SP field - error recovery needs to know where the parsing was interrupted
        mv.visitVarInsn(ALOAD, THIS);
        mv.visitVarInsn(ILOAD, SP);
        mv.visitFieldInsn(PUTFIELD, parserClassName, "sp", "I");
        loadConst(mv, 0);
        mv.visitInsn(IRETURN);

        ActionCodeWriter actionCodeWriter =
                new ActionCodeWriter ( baseParserClassName
                                     , astClassPrefix
                                     , mv
                                     , shiftTerminal
                                     , shiftNonterminal
                                     , findNonterminalAction);

        for (ParserState st = firstState; st != null; st = st.next) {
            mv.visitLabel(states[st.id]);
            ParserAction[] actions = getLookaheadActions(st);
            Arrays.sort(actions);
            int[] lookaheads = new int[actions.length];
            Label[] actLabels = new Label[actions.length];
            for (int i = 0; i < actions.length; i++) {
                lookaheads[i] = actions[i].lookahead.id;
                actLabels[i] = new Label();
            }
            Label defaultAction = (st.defaultReduce == null ? syntaxError : new Label());
            mv.visitLookupSwitchInsn(defaultAction, lookaheads, actLabels);
            for (int i = 0; i < actions.length; i++) {
                if (actions[i] != null) {
                    mv.visitLabel(actLabels[i]);
                    // scan and consolidate remaining actions
                    for (int x = i + 1; x < actions.length; x++) {
                        if (actions[x] != null && actions[x].equals(actions[i])) {
                            actions[x] = null;
                            mv.visitLabel(actLabels[x]);
                        }
                    }
                    actions[i].accept(actionCodeWriter);
                }
            }
            if (st.defaultReduce != null) {
                mv.visitLabel(defaultAction);
                st.defaultReduce.accept(actionCodeWriter);
            }
        }

        mv.visitMaxs(Math.max(4, actionCodeWriter.getMaxStack()), NUM_LOCALS);
        mv.visitEnd();

        return cw.toByteArray();
    }

    public void writeStates(File outDir, String fileName, ParserState firstState) throws IOException {
        PrintWriter out = new PrintWriter(new File(outDir, fileName));
        try {
            writeStates(out, firstState);
        } finally {
            out.close();
        }
    }

    public void writeStates(PrintWriter out, ParserState firstState) throws IOException {
        for (ParserState state = firstState; state != null; state = state.next) {
            out.println(state);
        }
    }

    private static ParserAction[] getLookaheadActions(ParserState st) {
        ParserAction[] actions = new ParserAction[st.numActions];
        int i = 0;
        for (ParserAction act = st.firstShift; act != null; act = act.next) {
            actions[i++] = act;
        }
        for (ParserAction act = st.firstReduce; act != null; act = act.next) {
            actions[i++] = act;
        }
        if (st.accept != null) {
            actions[i++] = st.accept;
        }
        if (i != st.numActions) {
            throw new IllegalStateException();
        }
        return actions;
    }

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

    private class ActionCodeWriter extends ParserAction.Visitor {
        private MethodVisitor    mv;
        private Label            shiftTerminal;
        private Label            shiftNonterminal;
        private Label            findNonterminalAction;
        private String           astPackagePrefix;
        private String           actionClassName;
        private int              maxStack = 0;
        private ReduceCodeWriter reduceCodeWriter;

        ActionCodeWriter ( String actionClassName
                         , String astPackage
                         , MethodVisitor mv
                         , Label shiftTerminal
                         , Label shiftNonterminal
                         , Label findNonterminalAction )
        {
            this.actionClassName = actionClassName;
            this.astPackagePrefix = astPackage;
            this.mv = mv;
            this.shiftTerminal = shiftTerminal;
            this.shiftNonterminal = shiftNonterminal;
            this.findNonterminalAction = findNonterminalAction;
            if (delegateReduceActions) {
                this.reduceCodeWriter = new ReduceToDelegateCodeWriter();
            } else {
                this.reduceCodeWriter = new ReduceToAstCodeWriter();
            }
        }

        void visit(ParserAction.Accept action) {
            if (genTrace) {
                mv.visitVarInsn(ALOAD, THIS);
                mv.visitVarInsn(ALOAD, SYMBOLS);
                mv.visitVarInsn(ILOAD, SP);
                mv.visitInsn(AALOAD);
                mv.visitMethodInsn(INVOKEVIRTUAL, actionClassName, "accept", "(Ljava/lang/Object;)V");
            }
            loadConst(mv, 1);
            mv.visitInsn(IRETURN);
        }

        void visit(ParserAction.Shift action) {
            loadConst(mv, action.dest.id);
            if (action.lookahead instanceof Symbol.NonTerminal) {
                mv.visitJumpInsn(GOTO, shiftNonterminal);
            } else {
                mv.visitJumpInsn(GOTO, shiftTerminal);
            }
        }

        void visit(ParserAction.Reduce action) {
            mv.visitVarInsn(ILOAD, LOOKAHEAD_ID);
            mv.visitVarInsn(ISTORE, SAVED_LOOKAHEAD_ID);
            loadConst(mv, action.production.lhs.id);
            mv.visitVarInsn(ISTORE, LOOKAHEAD_ID);

            if (action.production.rhs.length > 0) {
                mv.visitIincInsn(SP, -action.production.rhs.length);
            }
            mv.visitVarInsn(ILOAD, NUM_SHIFTS);
            mv.visitJumpInsn(IFGT, findNonterminalAction);

            int reduceStrategy = action.production.selectReduceStrategy();
            switch (reduceStrategy) {
                case -1: { // new node
                    reduceCodeWriter.newNode(action);
                    break;
                }
                case -2: { // return NULL
                    mv.visitVarInsn(ALOAD, SYMBOLS);
                    mv.visitVarInsn(ILOAD, SP);
                    mv.visitInsn(ACONST_NULL);
                    mv.visitInsn(AASTORE);
                    maxStack = Math.max(maxStack, 3);
                    break;
                }
                case -3: { // new empty list
                    reduceCodeWriter.emptyList(action);
                    break;
                }
                case -4: { // new list with 1 element
                    reduceCodeWriter.newList(action);
                    break;
                }
                case -5: { // extend list
                    reduceCodeWriter.extendList(action);
                    break;
                }
                case 0: {
                    // do nothing as the symbol we need is already where we need it
                    break;
                }
                default: { // return symbol at this index
                    if (reduceStrategy < 0 || action.production.rhs.length <= reduceStrategy) {
                        throw new IllegalStateException("unknown strategy");
                    }
                    mv.visitVarInsn(ALOAD, SYMBOLS);
                    mv.visitVarInsn(ILOAD, SP);
                    mv.visitInsn(DUP2);
                    loadConst(mv, reduceStrategy);
                    mv.visitInsn(IADD);
                    mv.visitInsn(AALOAD);
                    mv.visitInsn(AASTORE);
                    maxStack = Math.max(maxStack, 5);
                    break;
                }
            }

            if (genTrace) {
                mv.visitVarInsn(ALOAD, THIS);
                mv.visitVarInsn(ALOAD, SYMBOLS);
                mv.visitVarInsn(ILOAD, SP);
                mv.visitInsn(AALOAD);
                mv.visitMethodInsn(INVOKEVIRTUAL, actionClassName, "reduce", "(Ljava/lang/Object;)V");
            }
            mv.visitJumpInsn(GOTO, findNonterminalAction);
        }

        int getMaxStack() {
            return maxStack;
        }

        abstract class ReduceCodeWriter {
            abstract void newNode(ParserAction.Reduce action);
            abstract void emptyList(ParserAction.Reduce action);
            abstract void newList(ParserAction.Reduce action);
            abstract void extendList(ParserAction.Reduce action);
        }

        class ReduceToAstCodeWriter extends ReduceCodeWriter {
            void newNode(ParserAction.Reduce action) {
                mv.visitVarInsn(ALOAD, SYMBOLS);
                mv.visitVarInsn(ILOAD, SP);
                String nodeType = astPackagePrefix + action.production.lhs.getName();
                if (action.production.name != null) {
                    nodeType += "$" + action.production.name;
                }
                mv.visitTypeInsn(NEW, nodeType);
                mv.visitInsn(DUP);
                // collect constructor arguments and build its signature
                int sp = 4, maxSP = sp;
                String constructorSignature = "(";
                for (int i = 0; i < action.production.rhs.length; i++) {
                    if (!action.production.rhs[i].symbol.isKeyword()) {
                        String argType = astPackagePrefix + getNodeType(action.production.rhs[i].symbol);
                        constructorSignature += "L" + argType + ";";
                        mv.visitVarInsn(ALOAD, SYMBOLS);
                        mv.visitVarInsn(ILOAD, SP);
                        sp += 2;
                        maxSP = sp;
                        if (i > 0) {
                            loadConst(mv, i);
                            maxSP++;
                            mv.visitInsn(IADD);
                        }
                        mv.visitInsn(AALOAD);
                        mv.visitTypeInsn(CHECKCAST, argType);
                        sp--;
                    }
                }
                maxStack = Math.max(maxStack, maxSP);
                constructorSignature += ")V";
                mv.visitMethodInsn(INVOKESPECIAL, nodeType, "<init>", constructorSignature);
                mv.visitInsn(AASTORE);
            }

            void emptyList(ParserAction.Reduce action) {
                String listType = astPackagePrefix + action.production.lhs.getName();
                mv.visitVarInsn(ALOAD, SYMBOLS);
                mv.visitVarInsn(ILOAD, SP);
                mv.visitTypeInsn(NEW, listType);
                mv.visitInsn(DUP);
                mv.visitMethodInsn(INVOKESPECIAL, listType, "<init>", "()V");
                mv.visitInsn(AASTORE);
                maxStack = Math.max(maxStack, 4);
            }

            void newList(ParserAction.Reduce action) {
                String listType = astPackagePrefix + action.production.lhs.getName();
                int elemIdx = action.production.rhs.length - 1;
                String elemType = astPackagePrefix + getNodeType(action.production.rhs[elemIdx].symbol);
                mv.visitVarInsn(ALOAD, SYMBOLS);
                mv.visitVarInsn(ILOAD, SP);
                mv.visitTypeInsn(NEW, listType);
                mv.visitInsn(DUP);
                mv.visitVarInsn(ALOAD, SYMBOLS);
                mv.visitVarInsn(ILOAD, SP);
                loadConst(mv, elemIdx);
                mv.visitInsn(IADD);
                mv.visitInsn(AALOAD);
                mv.visitTypeInsn(CHECKCAST, elemType);
                mv.visitMethodInsn(INVOKESPECIAL, listType, "<init>", "(L" + elemType + ";)V");
                mv.visitInsn(AASTORE);
                maxStack = Math.max(maxStack, 7);
            }

            void extendList(ParserAction.Reduce action) {
                String listType = astPackagePrefix + action.production.lhs.getName();
                int elemIdx = action.production.rhs.length - 1;
                String elemType = astPackagePrefix + "Node";
                mv.visitVarInsn(ALOAD, SYMBOLS);
                mv.visitVarInsn(ILOAD, SP);
                mv.visitInsn(AALOAD);
                mv.visitTypeInsn(CHECKCAST, listType);
                mv.visitVarInsn(ALOAD, SYMBOLS);
                mv.visitVarInsn(ILOAD, SP);
                loadConst(mv, elemIdx);
                mv.visitInsn(IADD);
                mv.visitInsn(AALOAD);
                mv.visitTypeInsn(CHECKCAST, elemType);
                mv.visitMethodInsn(INVOKEVIRTUAL, listType, "add", "(L" + elemType + ";)V");
                maxStack = Math.max(maxStack, 4);
                // the "reduced" symbol is already where it should be
            }
        }

        class ReduceToDelegateCodeWriter extends ReduceCodeWriter {
            void newNode(Reduce action) {
                String methodName = "create";
                if (action.production.name != null) {
                    methodName += action.production.name;
                }
                methodName += action.production.lhs.getName();

                mv.visitVarInsn(ALOAD, SYMBOLS);
                mv.visitVarInsn(ILOAD, SP);
                // collect method arguments and build its signature
                mv.visitVarInsn(ALOAD, THIS);
                int sp = 3, maxSP = sp;
                String methodSignature = "(";
                for (int i = 0; i < action.production.rhs.length; i++) {
                    if (!action.production.rhs[i].symbol.isKeyword()) {
                        String argType = astPackagePrefix + getNodeType(action.production.rhs[i].symbol);
                        methodSignature += "L" + argType + ";";
                        mv.visitVarInsn(ALOAD, SYMBOLS);
                        mv.visitVarInsn(ILOAD, SP);
                        sp += 2;
                        maxSP = sp;
                        if (i > 0) {
                            loadConst(mv, i);
                            maxSP++;
                            mv.visitInsn(IADD);
                        }
                        mv.visitInsn(AALOAD);
                        mv.visitTypeInsn(CHECKCAST, argType);
                        sp--;
                    }
                }
                maxStack = Math.max(maxStack, maxSP);
                methodSignature += ")L" + astPackagePrefix + getNodeType(action.production.lhs) + ";";
                mv.visitMethodInsn(INVOKEVIRTUAL, actionClassName, methodName, methodSignature);
                mv.visitInsn(AASTORE);
            }

            void emptyList(Reduce action) {
                String listType = astPackagePrefix + action.production.lhs.getName();
                mv.visitVarInsn(ALOAD, SYMBOLS);
                mv.visitVarInsn(ILOAD, SP);
                mv.visitVarInsn(ALOAD, THIS);
                mv.visitMethodInsn(INVOKEVIRTUAL, actionClassName, "create" + action.production.lhs.getName(), "()L" + listType + ";");
                mv.visitInsn(AASTORE);
                maxStack = Math.max(maxStack, 3);
            }

            void newList(Reduce action) {
                String listType = astPackagePrefix + action.production.lhs.getName();
                int elemIdx = action.production.rhs.length - 1;
                String elemType = astPackagePrefix + getNodeType(action.production.rhs[elemIdx].symbol);
                mv.visitVarInsn(ALOAD, SYMBOLS);
                mv.visitVarInsn(ILOAD, SP);
                mv.visitVarInsn(ALOAD, THIS);
                mv.visitVarInsn(ALOAD, SYMBOLS);
                mv.visitVarInsn(ILOAD, SP);
                loadConst(mv, elemIdx);
                mv.visitInsn(IADD);
                mv.visitInsn(AALOAD);
                mv.visitTypeInsn(CHECKCAST, elemType);
                mv.visitMethodInsn(INVOKEVIRTUAL, actionClassName, "create" + action.production.lhs.getName(), "(L" + elemType + ";)L" + listType + ";");
                mv.visitInsn(AASTORE);
                maxStack = Math.max(maxStack, 5);
            }

            void extendList(Reduce action) {
                String listType = astPackagePrefix + action.production.lhs.getName();
                int elemIdx = action.production.rhs.length - 1;
                String elemType = astPackagePrefix + getNodeType(action.production.rhs[elemIdx].symbol);
                mv.visitVarInsn(ALOAD, THIS);
                mv.visitVarInsn(ALOAD, SYMBOLS);
                mv.visitVarInsn(ILOAD, SP);
                mv.visitInsn(AALOAD);
                mv.visitTypeInsn(CHECKCAST, listType);
                mv.visitVarInsn(ALOAD, SYMBOLS);
                mv.visitVarInsn(ILOAD, SP);
                loadConst(mv, elemIdx);
                mv.visitInsn(IADD);
                mv.visitInsn(AALOAD);
                mv.visitTypeInsn(CHECKCAST, elemType);
                mv.visitMethodInsn(INVOKEVIRTUAL, actionClassName, "extend" + action.production.lhs.getName(), "(L" + listType + ";L" + elemType + ";)V");
                maxStack = Math.max(maxStack, 5);
                // the "reduced" symbol is already where it should be
            }
        }
    }

    private static final int THIS               = 0;
    private static final int SCANNER            = 1;
    private static final int NUM_SHIFTS         = 2;
    private static final int STATES             = 3;
    private static final int SP                 = 4;
    private static final int STATE              = 5;
    private static final int SYMBOLS            = 6;
    private static final int TOKEN              = 7;
    private static final int LOOKAHEAD_ID       = 8;
    private static final int SAVED_LOOKAHEAD_ID = 9;
    private static final int NUM_LOCALS         = 10;
}
