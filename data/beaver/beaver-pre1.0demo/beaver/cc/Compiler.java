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
package beaver.cc;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import beaver.cc.lexer.DFA;
import beaver.cc.lexer.JavaClassCharScannerWriter;
import beaver.cc.lexer.RegExp;
import beaver.cc.parser.Grammar;
import beaver.cc.parser.JavaASTWriter;
import beaver.cc.parser.JavaClassParserWriter;
import beaver.cc.parser.ParserState;
import beaver.cc.parser.Production;

public class Compiler {

    private static void execute(String[] cmd) throws IOException {
        Arguments args = new Arguments(cmd);

        ParserErrorLog log = new ParserErrorLog();
        AST.Grammar grammarNode = (AST.Grammar) new Parser().parse(new Scanner(new FileReader(args.specFile)), log);
        if (log.messages.size() > 0) {
            printLog(log.messages);
            throw new IllegalStateException("syntax errors");
        }
        
        EBNFInliner ebnfInliner = new EBNFInliner();
        grammarNode.accept(ebnfInliner);
        if (ebnfInliner.errorLog.size() > 0) {
            printLog(ebnfInliner.errorLog);
            throw new IllegalStateException("grammar errors");
        }

        SymbolCollector symbolCollector = new SymbolCollector();
        grammarNode.accept(symbolCollector);
        if (symbolCollector.errorLog.size() > 0) {
            printLog(symbolCollector.errorLog);
            throw new IllegalStateException("grammar errors");
        }
        Map symbols = symbolCollector.getSymbols();

        SymbolChecker symbolChecker = new SymbolChecker(symbols.keySet());
        grammarNode.accept(symbolChecker);
        if (symbolChecker.errorLog.size() > 0) {
            printLog(symbolChecker.errorLog);
            throw new IllegalStateException("grammar errors");
        }

        ProductionBuilder productionBuilder = new ProductionBuilder(symbols);
        grammarNode.accept(productionBuilder);
        if (productionBuilder.errorLog.size() > 0) {
            printLog(productionBuilder.errorLog);
            throw new IllegalStateException("grammar errors");
        }
        Production[] productions = productionBuilder.getProductions();
        
        PrecedenceAssigner precedenceAssigner = new PrecedenceAssigner(symbols, productions);
        grammarNode.accept(precedenceAssigner);
        if (precedenceAssigner.errorLog.size() > 0) {
            printLog(precedenceAssigner.errorLog);
            throw new IllegalStateException("grammar errors");
        }        
        Grammar grammar = new Grammar(productions);

        ScannerStartConditionFinder startCondFinder = new ScannerStartConditionFinder();
        grammarNode.accept(startCondFinder);
        
        RegExpBuilder regExpBuilder = new RegExpBuilder(symbols, startCondFinder.startConditions);
        grammarNode.accept(regExpBuilder);
        if (regExpBuilder.errorLog.size() > 0) {
            printLog(regExpBuilder.errorLog);
            throw new IllegalStateException("grammar errors");
        }
        if (regExpBuilder.warningLog.size() > 0) {
            printLog(regExpBuilder.warningLog);
        }
        RegExp[] scannerRules = regExpBuilder.condRules;
        // System.out.println("   " + scannerRules);
        
        JavaASTWriter astWriter = new JavaASTWriter(grammar); 
        astWriter.writeAST(args.srcDir, args.pkgName, args.clsNamePrefix);
        if (args.reduceViaDelegates) {
            astWriter.writeBaseParser(args.srcDir, args.pkgName, args.clsNamePrefix, args.baseParserName);
        }
        
        ParserState firstState = new ParserState.Builder().buildParserStates(grammar);
        if (args.parserStatesConflictResolutionLogFile != null) {
            Collection removalLog = new ArrayList();
            ParserState.Builder.resolveConflicts(firstState, removalLog);
            try {
                PrintWriter out = new PrintWriter(args.parserStatesConflictResolutionLogFile);
                try {
                    for (Iterator i = removalLog.iterator(); i.hasNext();) {
                        out.println(i.next());
                    }
                } finally {
                    out.close();
                }
            } catch (IOException e) {
                System.err.println("failed to write parser conflict resolution log - " + e.getMessage());
            }
        } else {
            ParserState.Builder.resolveConflicts(firstState, null);
        }
        JavaClassParserWriter parserWriter = new JavaClassParserWriter(args.reduceViaDelegates, args.tracingParser);
        if (args.parserStatesFile != null) {
            try {
                PrintWriter out = new PrintWriter(args.parserStatesFile);
                try {
                    parserWriter.writeStates(out, firstState);
                } finally {
                    out.close();
                }
            } catch (IOException e) {
                System.err.println("failed to dump parser states - " + e.getMessage());
            }
        }
        String classNamePrefix = (args.pkgName.length() > 0 ? args.pkgName + "." + args.clsNamePrefix : args.clsNamePrefix);
        parserWriter.writeParser ( args.clsDir
                                 , classNamePrefix + "Parser"
                                 , args.baseParserName != null ? classNamePrefix + args.baseParserName 
                                                               : "beaver.Parser"  
                                 , classNamePrefix + "AST"
                                 , grammar
                                 , firstState);

        DFA[] xdfa = new DFA[scannerRules.length];
        for (int i = 0; i < xdfa.length; i++) {
            xdfa[i] = new DFA(scannerRules[i]);
        }
        if (args.scannerDFAFile != null) {        
            try {
                Writer out = new FileWriter(args.scannerDFAFile);
                try {
                    for (int i = 0; i < xdfa.length; i++) {
                        out.write(regExpBuilder.condNames[i]);
                        out.write(" ===========\n\n");
                        out.write(xdfa[i].toString()); 
                        out.write("\n");
                    }
                } finally {
                    out.close();
                }
            } catch (IOException e) {
                System.err.println("failed to dump scanner DFA - " + e.getMessage());
            }
        }
        JavaClassCharScannerWriter scannerWriter = new JavaClassCharScannerWriter();
        scannerWriter.writeBaseScanner(args.srcDir, args.pkgName, args.clsNamePrefix, args.baseScannerName, xdfa);
        String baseScannerClassName = classNamePrefix + args.baseScannerName;
        scannerWriter.writeScanner(args.clsDir, classNamePrefix + "Scanner", baseScannerClassName, xdfa);
    }

    private static void printLog(Collection errorLog) {
        for (Iterator i = errorLog.iterator(); i.hasNext();) {
            System.err.println(i.next());
        }
    }

    /**
     * @param cmd
     *            GrammarFile.name
     *            -root-dir dir/name
     *            "project" root directory. Generated files will be stored in "src" and "lib" directories
     *            underneath the root. Either root or both src and lib have to be provided.
     *            -src-dir dir/name
     *            directory where the generated Java sources will be stored.
     *            -lib-dir dir/name
     *            directory where the generated scanner and parser class files will be stored
     *            -package-name package.name
     *            name of the package for the generated classes
     *            -class-name-prefix ClassNamePrefix
     *            prefix that will be added to all generated class names. Optional.
     */
    public static void main(String[] cmd) throws IOException {
        try {
            execute(cmd);
        } catch (Exception e) {
            System.err.println("Cannot continue: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
