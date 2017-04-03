package org.jrdf.example;

import java.io.*;
import java.net.*;
import java.util.*;

import org.jrdf.graph.*;
import org.jrdf.graph.mem.*;
import org.jrdf.parser.*;
import org.jrdf.parser.rdfxml.*;

public class RdfXmlParserExample {
    public static void main(String[] args) throws Exception {
        String baseURI = "http://rss.slashdot.org/Slashdot/slashdot";
        URL url = new URL(baseURI);
        InputStream is = url.openStream();
        final Graph jrdfMem = new GraphImpl();
        RdfXmlParser parser = new RdfXmlParser(jrdfMem.getElementFactory());
        parser.setStatementHandler(new StatementHandler() {
            public void handleStatement(SubjectNode subject, PredicateNode predicate, ObjectNode object) {
                try {
                    jrdfMem.add(subject, predicate, object);
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        parser.parse(is, baseURI);
        Iterator iter = jrdfMem.find(null, null, null);
        while (iter.hasNext()) {
            System.err.println("Graph: " + iter.next());
        }
        is.close();
    }
}
