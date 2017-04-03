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
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;

public class JavaASTWriter {

    static boolean isAList(Symbol symbol) {
        if (symbol instanceof Symbol.NonTerminal) {
            Symbol.NonTerminal nt = (Symbol.NonTerminal) symbol;
            return nt.isAList || (nt.delegate instanceof Symbol.NonTerminal && ((Symbol.NonTerminal) nt.delegate).isAList);
        }
        return false;
    }

    static class FieldDef {
        Symbol  symbol;
        String  typeName;

        FieldDef(Symbol symbol) {
            this.symbol = symbol;
            typeName = ParserWriter.getNodeType(symbol);
        }

        boolean canBeNull() {
            return symbol.canMatchEmptyString() && !isAList(symbol);
        }

        String makeFieldName() {
            if (symbol instanceof Symbol.Terminal) {
                return symbol.name.toLowerCase();
            } else if (canBeNull()) {
                if (symbol.name.length() < 3 || !symbol.name.substring(0, 3).equalsIgnoreCase("opt")) {
                    return "opt" + symbol.name;
                } else {
                    return symbol.name.substring(0, 1).toLowerCase() + symbol.name.substring(1);
                }
            } else {
                return symbol.getName().substring(0, 1).toLowerCase() + symbol.getName().substring(1);
            }
        }
    }

    static abstract class ClassDef {
        abstract void writeClass(PrintWriter out);
        abstract void writeVisitor(PrintWriter out);
        abstract void writeReduceActions(PrintWriter out);
        abstract String getOuterClass();
    }

    static class NodeDef extends ClassDef {
        String     parentName;
        String     name;
        Collection fields;
        Map        fieldTypes;
        Collection constructors;

        NodeDef(Symbol.NonTerminal nt, String productionName) {
            fields = new ArrayList();
            fieldTypes = new TreeMap();
            constructors = new ArrayList();

            if (productionName == null) {
                name = nt.name;
            } else {
                name = productionName;
                parentName = nt.name;
            }

            for (int i = 0; i < nt.rules.length; i++) {
                Production prod = nt.rules[i];
                if (prod.matchesKeywordsOnly())
                    continue;
                if (prod.name == productionName || prod.name != null && prod.name.equals(productionName)) {
                    Collection args = new ArrayList();
                    Production.RHSElement[] rhs = prod.rhs;
                    for (int j = 0; j < rhs.length; j++) {
                        if (!rhs[j].symbol.isKeyword()) {
                            FieldDef argType = new FieldDef(rhs[j].symbol);
                            String argName = rhs[j].name;
                            if (argName == null) {
                                argName = argType.makeFieldName();
                            }
                            if (args.contains(argName) || fieldTypes.containsKey(argName) && !((FieldDef) fieldTypes.get(argName)).typeName.equals(argType.typeName)) {
                                String nameBase = argName;
                                int n = 1;
                                do {
                                    argName = nameBase + Integer.toString(++n);
                                } while (args.contains(argName) || fieldTypes.containsKey(argName) && !((FieldDef) fieldTypes.get(argName)).typeName.equals(argType.typeName));
                            }
                            if (!fieldTypes.containsKey(argName)) {
                                fieldTypes.put(argName, argType);
                                fields.add(argName);
                            }
                            args.add(argName);
                        }
                    }
                    if (args.size() > 0) {
                        constructors.add(args);
                    }
                }
            }
        }

        String getOuterClass() {
            return parentName;
        }

        void writeClass(PrintWriter out) {
            String indent = (parentName != null ? "\t\t" : "\t");

            out.print(indent);
            out.print("public static class ");
            out.print(name);
            out.print(" extends ");
            out.print(parentName != null ? parentName : "Node");
            out.println(" {");

            for (Iterator i = fields.iterator(); i.hasNext();) {
                String fieldName = (String) i.next();
                FieldDef field = (FieldDef) fieldTypes.get(fieldName);
                out.print(indent);
                out.print("\t");
                out.print(field.typeName);
                out.print(" ");
                out.print(fieldName);
                out.println(";");
            }

            for (Iterator i = constructors.iterator(); i.hasNext();) {
                Collection args = (Collection) i.next();
                out.print(indent);
                out.print("\tpublic ");
                out.print(name);
                out.print("(");
                boolean seenFirstArg = false;
                for (Iterator j = args.iterator(); j.hasNext();) {
                    String argName = (String) j.next();
                    FieldDef field = (FieldDef) fieldTypes.get(argName);
                    if (seenFirstArg) {
                        out.print(", ");
                    } else {
                        seenFirstArg = true;
                    }
                    out.print(field.typeName);
                    out.print(" ");
                    out.print(argName);

                }
                out.println(") {");
                for (Iterator j = args.iterator(); j.hasNext();) {
                    String argName = (String) j.next();
                    FieldDef field = (FieldDef) fieldTypes.get(argName);
                    if (field.canBeNull()) {
                        out.print(indent);
                        out.print("\t\tif(");
                        out.print(argName);
                        out.println(" != null) {");
                        out.print(indent);
                        out.print("\t\t\t(this.");
                        out.print(argName);
                        out.print(" = ");
                        out.print(argName);
                        out.println(").parent = this;");
                        out.print(indent);
                        out.println("\t\t}");
                    } else {
                        out.print(indent);
                        out.print("\t\t(this.");
                        out.print(argName);
                        out.print(" = ");
                        out.print(argName);
                        out.println(").parent = this;");
                    }
                }
                out.print(indent);
                out.println("\t}");
            }

            out.print(indent);
            out.print("\tprivate ");
            out.print(name);
            out.print("(");
            out.print(name);
            out.println(" node) {");
            for (Iterator i = fields.iterator(); i.hasNext();) {
                String fieldName = (String) i.next();
                FieldDef field = (FieldDef) fieldTypes.get(fieldName);
                if (field.canBeNull()) {
                    out.print(indent);
                    out.print("\t\tif(node.");
                    out.print(fieldName);
                    out.println(" != null) {");
                    out.print(indent);
                    out.print("\t\t\t(this.");
                    out.print(fieldName);
                    out.print(" = (");
                    out.print(field.typeName);
                    out.print(") node.");
                    out.print(fieldName);
                    out.println(".dup()).parent = this;");
                    out.print(indent);
                    out.println("\t\t}");
                } else {
                    out.print(indent);
                    out.print("\t\t(this.");
                    out.print(fieldName);
                    out.print(" = (");
                    out.print(field.typeName);
                    out.print(") node.");
                    out.print(fieldName);
                    out.println(".dup()).parent = this;");
                }
            }
            out.print(indent);
            out.println("\t}");

            out.print(indent);
            out.println("\tNode dup() {");
            out.print(indent);
            out.print("\t\treturn new ");
            out.print(name);
            out.println("(this);");
            out.print(indent);
            out.println("\t}");

            out.print(indent);
            out.println("\tvoid accept(Walker walker) {");
            out.print(indent);
            out.println("\t\twalker.visit(this);");
            out.print(indent);
            out.println("\t}");

            out.print(indent);
            out.println("\tpublic String toString() {");
            out.print(indent);
            out.print("\t\treturn \"(");
            out.print(parentName != null ? parentName + "." + name : name);
            out.print("\"");
            for (Iterator i = fields.iterator(); i.hasNext();) {
                String fieldName = (String) i.next();
                FieldDef field = (FieldDef) fieldTypes.get(fieldName);
                if (field.canBeNull()) {
                    out.print(" + ");
                    out.print("asString(");
                    out.print(fieldName);
                    out.print(")");
                } else {
                    out.print(" + \" \" + ");
                    out.print(fieldName);
                }
            }
            out.println(" + \")\";");
            out.print(indent);
            out.println("\t}");

            out.print(indent);
            out.println("}");
        }

        void writeVisitor(PrintWriter out) {
            String nodeType = (parentName != null ? parentName + "." + name : name);
            out.print("\t\tboolean enter(");
            out.print(nodeType);
            out.println(" node) {");
            out.println("\t\t\treturn true;");
            out.println("\t\t}");
            out.print("\t\tvoid leave(");
            out.print(nodeType);
            out.println(" node) {}");
            out.print("\t\tvoid visit(");
            out.print(nodeType);
            out.println(" node) {");
            out.println("\t\t\tif (enter(node)) {");
            for (Iterator i = fields.iterator(); i.hasNext();) {
                String fieldName = (String) i.next();
                FieldDef field = (FieldDef) fieldTypes.get(fieldName);
                out.print("\t\t\t\t");
                if (field.canBeNull()) {
                    out.print("if (node.");
                    out.print(fieldName);
                    out.print(" != null) { node.");
                    out.print(fieldName);
                    out.println(".accept(this); }");
                } else if (field.typeName.equals("Term")) {
                    out.print("visit(node.");
                    out.print(fieldName);
                    out.println(");");
                } else {
                    out.print("node.");
                    out.print(fieldName);
                    out.println(".accept(this);");
                }
            }
            out.println("\t\t\t\tleave(node);");
            out.println("\t\t\t}");
            out.println("\t\t}");
        }

        void writeReduceActions(PrintWriter out) {
            for (Iterator i = constructors.iterator(); i.hasNext();) {
                Collection args = (Collection) i.next();
                out.print("\tprotected ");
                out.print(parentName != null ? parentName : name);
                out.print(" create");
                out.print(name);
                if (parentName != null) {
                    out.print(parentName);
                }
                out.print("(");
                boolean seenFirstArg = false;
                for (Iterator j = args.iterator(); j.hasNext();) {
                    String argName = (String) j.next();
                    FieldDef field = (FieldDef) fieldTypes.get(argName);
                    if (seenFirstArg) {
                        out.print(", ");
                    } else {
                        seenFirstArg = true;
                    }
                    out.print(field.typeName);
                    out.print(" ");
                    out.print(argName);
                }
                out.println(") {");
                out.print("\t\treturn new ");
                if (parentName != null) {
                    out.print(parentName);
                    out.print('.');
                }
                out.print(name);
                out.print("(");
                seenFirstArg = false;
                for (Iterator j = args.iterator(); j.hasNext();) {
                    String argName = (String) j.next();
                    if (seenFirstArg) {
                        out.print(", ");
                    } else {
                        seenFirstArg = true;
                    }
                    out.print(argName);
                }
                out.println(");");
                out.println("\t}");
            }
        }
    }

    static class ListDef extends ClassDef {
        String  listType;
        String  itemType;
        boolean canBeEmpty;
        boolean hasInitItem;

        ListDef(Symbol.NonTerminal nt, boolean isOpt) {
            listType = nt.name;
            itemType = ParserWriter.getNodeType(nt.listElement);
            hasInitItem = nt.rules[0].rhs.length > 0 && nt.rules[1].rhs.length > 0;
            canBeEmpty = isOpt || !hasInitItem;
        }

        String getOuterClass() {
            return null;
        }

        void writeClass(PrintWriter out) {
            out.print("\tpublic static class ");
            out.print(listType);
            out.println(" extends NodeList {");
            out.print("\t\tprivate ");
            out.print(listType);
            out.print("(");
            out.print(listType);
            out.println(" list) {");
            out.println("\t\t\tsuper(list);");
            out.println("\t\t}");
            out.print("\t\tpublic ");
            out.print(listType);
            out.println("() {");
            out.println("\t\t\tsuper();");
            out.println("\t\t}");
            out.print("\t\tpublic ");
            out.print(listType);
            out.print("(");
            out.print(itemType);
            out.println(" elem) {");
            out.println("\t\t\tsuper(elem);");
            out.println("\t\t}");
            out.println("\t\tNode dup() {");
            out.print("\t\t\treturn new ");
            out.print(listType);
            out.println("(this);");
            out.println("\t\t}");
            out.println("\t\tvoid accept(Walker walker) {");
            out.println("\t\t\twalker.visit(this);");
            out.println("\t\t}");
            out.println("\t\tpublic String toString() {");
            out.print("\t\t\treturn \"(");
            out.print(listType);
            out.println(" \" + super.toString() + \")\";");
            out.println("\t\t}");
            out.println("\t}");
        }

        void writeVisitor(PrintWriter out) {
            out.print("\t\tboolean enter(");
            out.print(listType);
            out.println(" node) {");
            out.println("\t\t\treturn true;");
            out.println("\t\t}");
            out.print("\t\tvoid leave(");
            out.print(listType);
            out.println(" node) {}");
            out.print("\t\tvoid visit(");
            out.print(listType);
            out.println(" node) {");
            out.println("\t\t\tif (enter(node)) {");
            out.println("\t\t\t\tfor (Node e = node.first; e != null; e = e.next) {");
            out.println("\t\t\t\t\te.accept(this);");
            out.println("\t\t\t\t}");
            out.println("\t\t\t\tleave(node);");
            out.println("\t\t\t}");
            out.println("\t\t}");
        }

        void writeReduceActions(PrintWriter out) {
            if (canBeEmpty) {
                out.print("\tprotected ");
                out.print(listType);
                out.print(" create");
                out.print(listType);
                out.println("() {");
                out.print("\t\treturn new ");
                out.print(listType);
                out.println("();");
                out.println("\t}");
            }
            if (hasInitItem) {
                out.print("\tprotected ");
                out.print(listType);
                out.print(" create");
                out.print(listType);
                out.print("(");
                out.print(itemType);
                out.println(" item) {");
                out.print("\t\treturn new ");
                out.print(listType);
                out.println("(item);");
                out.println("\t}");
            }
            out.print("\tprotected ");
            out.print("void extend");
            out.print(listType);
            out.print("(");
            out.print(listType);
            out.print(" list, ");
            out.print(itemType);
            out.println(" item) {");
            out.println("\t\tlist.add(item);");
            out.println("\t}");
        }
    }

    Collection astClasses;
    String[]   keywords;
    String[]   tokens;
    Collection optLists;

    public JavaASTWriter(Grammar grammar) {
        optLists = new HashSet();
        for (int i = 0; i < grammar.nonterminals.length; i++) {
            Symbol.NonTerminal nt = grammar.nonterminals[i];
            if (nt.delegate instanceof Symbol.NonTerminal && ((Symbol.NonTerminal) nt.delegate).isAList) {
                optLists.add(nt.delegate);
            } else if (nt.isAList && nt.canMatchEmptyString) {
                optLists.add(nt);
            }
        }
        astClasses = new ArrayList();
        for (int i = 0; i < grammar.nonterminals.length; i++) {
            Symbol.NonTerminal nt = grammar.nonterminals[i];
            if (nt.delegate != null)
                continue;
            if (nt.isAList) {
                astClasses.add(new ListDef(nt, optLists.contains(nt)));
            } else if (nt.rules[0].name == null) {
                // each production represents a constructor
                astClasses.add(new NodeDef(nt, null));
            } else {
                // production with the same name create a distinct type
                Collection productionNames = new TreeSet();
                for (int j = 0; j < nt.rules.length; j++) {
                    if (!productionNames.contains(nt.rules[j].name)) {
                        productionNames.add(nt.rules[j].name);
                        astClasses.add(new NodeDef(nt, nt.rules[j].name));
                    }
                }
            }
        }
        keywords = grammar.getKeywords();
        tokens = grammar.getTokens();
    }

    public void writeAST(File outDir, String packageName, String grammarName) throws IOException {
        if (packageName != null) {
            File pkgDir = new File(outDir, packageName.replace('.', '/'));
            if (!pkgDir.exists()) {
                pkgDir.mkdirs();
            }
            outDir = pkgDir;
        }
        PrintWriter out = new PrintWriter(new File(outDir, grammarName + "AST.java"));
        try {
            if (packageName != null && !packageName.equals("")) {
                out.println("package " + packageName + ";");
            }
            out.println("public abstract class " + grammarName + "AST {");
            writeAST(out);
            out.println("}");
        } finally {
            out.close();
        }
    }

    public void writeBaseParser(File outDir, String packageName, String grammarName, String parserName) throws IOException {
        if (packageName != null) {
            File pkgDir = new File(outDir, packageName.replace('.', '/'));
            if (!pkgDir.exists()) {
                pkgDir.mkdirs();
            }
            outDir = pkgDir;
        }
        PrintWriter out = new PrintWriter(new File(outDir, grammarName + parserName + ".java"));
        try {
            if (packageName != null && !packageName.equals("")) {
                out.println("package " + packageName + ";");
            }
            out.println("import " + packageName + "." + grammarName + "AST.*;");
            out.println("public abstract class " + grammarName + parserName + " extends beaver.Parser {");
            for (Iterator i = astClasses.iterator(); i.hasNext();) {
                ((ClassDef) i.next()).writeReduceActions(out);
            }
            out.println("}");
        } finally {
            out.close();
        }
    }

    void writeAST(PrintWriter out) {
        out.println("\tpublic static abstract class Node {");
        out.println("\t\tNode parent;");
        out.println("\t\tNode prev;");
        out.println("\t\tNode next;");
        out.println("\t\tabstract void accept(Walker walker);");
        out.println("\t\tabstract Node dup();");
        out.println("\t\tstatic String asString(Object object) {");
        out.println("\t\t\treturn object == null ? \"\" : \" \" + object.toString();");
        out.println("\t\t}");
        out.println("\t}");

        out.println("\tpublic static class Term extends Node implements beaver.Term {");
        out.print("\t\tstatic final String[] keywords = { ");
        out.print("\"'");
        out.print(keywords[0]);
        out.print("'\"");
        for (int i = 1; i < keywords.length; i++) {
            out.print(", \"'");
            out.print(keywords[i]);
            out.print("'\"");
        }
        out.println(" };");
        out.print("\t\tstatic final String[] tokens = { ");
        out.print('"');
        out.print(tokens[0]);
        out.print('"');
        for (int i = 1; i < tokens.length; i++) {
            out.print(", \"");
            out.print(tokens[i]);
            out.print('"');
        }
        out.println(" };");
        out.println("\t\tint    id;");
        out.println("\t\tint    line;");
        out.println("\t\tint    column;");
        out.println("\t\tObject text;");
        out.println("\t\tpublic Term(int id, int line, int column, Object text) {");
        out.println("\t\t\tthis.id = id;");
        out.println("\t\t\tthis.line = line;");
        out.println("\t\t\tthis.column = column;");
        out.println("\t\t\tthis.text = text;");
        out.println("\t\t}");
        out.println("\t\tprivate Term(Term term) {");
        out.println("\t\t\tthis.id = term.id;");
        out.println("\t\t\tthis.line = term.line;");
        out.println("\t\t\tthis.column = term.column;");
        out.println("\t\t\tthis.text = term.text;");
        out.println("\t\t}");
        out.println("\t\tpublic void setId(int newId) {");
        out.println("\t\t\tid = newId;");
        out.println("\t\t}");
        out.println("\t\tpublic int getId() {");
        out.println("\t\t\treturn id;");
        out.println("\t\t}");
        out.println("\t\tpublic Object getText() {");
        out.println("\t\t\treturn text;");
        out.println("\t\t}");
        out.println("\t\tpublic int getLine() {");
        out.println("\t\t\treturn line;");
        out.println("\t\t}");
        out.println("\t\tpublic int getColumn() {");
        out.println("\t\t\treturn column;");
        out.println("\t\t}");
        out.println("\t\tvoid accept(Walker walker) {");
        out.println("\t\t\twalker.visit(this);");
        out.println("\t\t}");
        out.println("\t\tNode dup() {");
        out.println("\t\t\treturn new Term(this);");
        out.println("\t\t}");
        out.println("\t\tpublic String toString() {");
        out.println("\t\t\tif (id >= 0) {");
        out.println("\t\t\t\tif (id < keywords.length) {");
        out.println("\t\t\t\t\treturn keywords[id];");
        out.println("\t\t\t\t}");
        out.println("\t\t\t\tint ix = id - keywords.length;");
        out.println("\t\t\t\tif (ix < tokens.length) {");
        out.println("\t\t\t\t\treturn tokens[ix];");
        out.println("\t\t\t\t}");
        out.println("\t\t\t}");
        out.println("\t\t\treturn text.toString();");
        out.println("\t\t}");
        out.println("\t}");

        out.println("\tpublic static abstract class NodeList extends Node {");
        out.println("\t\tNode first;");
        out.println("\t\tNode last;");
        out.println("\t\tint  size;");
        out.println("\t\tNodeList() {}");
        out.println("\t\tNodeList(Node node) {");
        out.println("\t\t\tnode.parent = this;");
        out.println("\t\t\tlast = first = node;");
        out.println("\t\t\tsize = 1;");
        out.println("\t\t}");
        out.println("\t\tprotected NodeList(NodeList list) {");
        out.println("\t\t\tNode elem = list.first;");
        out.println("\t\t\tif (elem != null) {");
        out.println("\t\t\t\tlast = first = elem.dup();");
        out.println("\t\t\t\tlast.parent = this;");
        out.println("\t\t\t\tlast.prev = last.next = null;");
        out.println("\t\t\t\tsize = 1;");
        out.println("\t\t\t\tfor (elem = elem.next; elem != null; elem = elem.next) {");
        out.println("\t\t\t\t\tadd(elem.dup());");
        out.println("\t\t\t\t}");
        out.println("\t\t\t}");
        out.println("\t\t}");
        out.println("\t\tpublic void add(Node node) {");
        out.println("\t\t\tnode.parent = this;");
        out.println("\t\t\tnode.next = null;");
        out.println("\t\t\tif ((node.prev = last) == null) {");
        out.println("\t\t\t\tlast = first = node;");
        out.println("\t\t\t} else {");
        out.println("\t\t\t\tlast = last.next = node;");
        out.println("\t\t\t}");
        out.println("\t\t\tsize++;");
        out.println("\t\t}");
        out.println("\t\tpublic void remove(Node node) {");
        out.println("\t\t\tif (node.parent != this) {");
        out.println("\t\t\t\tthrow new IllegalArgumentException(\"wrong list\");");
        out.println("\t\t\t}");
        out.println("\t\t\tif (node.prev != null) {");
        out.println("\t\t\t\tfirst = node.next;");
        out.println("\t\t\t} else {");
        out.println("\t\t\t\tnode.prev.next = node.next;");
        out.println("\t\t\t}");
        out.println("\t\t\tif (node.next == null) {");
        out.println("\t\t\t\tlast = node.prev;");
        out.println("\t\t\t} else {");
        out.println("\t\t\t\tnode.next.prev = node.prev;");
        out.println("\t\t\t}");
        out.println("\t\t\tsize--;");
        out.println("\t\t}");
        out.println("\t\tpublic void replace(Node node, Node newNode) {");
        out.println("\t\t\tif (node.parent != this) {");
        out.println("\t\t\t\tthrow new IllegalArgumentException(\"wrong list\");");
        out.println("\t\t\t}");
        out.println("\t\t\tnewNode.parent = this;");
        out.println("\t\t\tnewNode.prev = node.prev;");
        out.println("\t\t\tnewNode.next = node.next;");
        out.println("\t\t\tif (newNode.prev == null) {");
        out.println("\t\t\t\tfirst = newNode;");
        out.println("\t\t\t} else {");
        out.println("\t\t\t\tnewNode.prev.next = newNode;");
        out.println("\t\t\t}");
        out.println("\t\t\tif (newNode.next == null) {");
        out.println("\t\t\t\tlast = newNode;");
        out.println("\t\t\t} else {");
        out.println("\t\t\t\tnewNode.next.prev = newNode;");
        out.println("\t\t\t}");
        out.println("\t\t}");
        out.println("\t\tpublic String toString() {");
        out.println("\t\t\tif (first != null) {");
        out.println("\t\t\t\tString str = first.toString();");
        out.println("\t\t\t\tfor (Node e = first.next; e != null; e = e.next) {");
        out.println("\t\t\t\t\tstr += \", \" + e;");
        out.println("\t\t\t\t}");
        out.println("\t\t\t\treturn str;");
        out.println("\t\t\t}");
        out.println("\t\t\treturn \"\";");
        out.println("\t\t}");
        out.println("\t}");

        String currentAbstractNode = null;
        for (Iterator i = astClasses.iterator(); i.hasNext();) {
            ClassDef def = ((ClassDef) i.next());
            String outerClass = def.getOuterClass();
            if (outerClass == null) {
                if (currentAbstractNode != null) {
                    out.println("\t}");
                    currentAbstractNode = null;
                }
            } else if (!outerClass.equals(currentAbstractNode)) {
                if (currentAbstractNode != null) {
                    out.println("\t}");
                }
                out.print("\tpublic static abstract class ");
                out.print(currentAbstractNode = outerClass);
                out.println(" extends Node {");
            }
            def.writeClass(out);
        }
        if (currentAbstractNode != null) {
            out.println("\t}");
        }

        out.println("\tpublic static class Walker {");
        out.println("\t\tvoid visit(Term node) {}");
        for (Iterator i = astClasses.iterator(); i.hasNext();) {
            ((ClassDef) i.next()).writeVisitor(out);
        }
        out.println("\t}");
    }
}
