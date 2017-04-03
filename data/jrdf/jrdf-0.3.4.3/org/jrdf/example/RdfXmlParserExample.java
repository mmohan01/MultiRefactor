package org.jrdf.example;

import java.io.*;
import java.net.*;
import java.util.*;

import org.jrdf.graph.*;
import org.jrdf.graph.mem.*;
import org.jrdf.parser.*;
import org.jrdf.parser.rdfxml.*;

public class RdfXmlParserExample {
    private static final String DEFAULT_RDF_URL = "http://rss.slashdot.org/Slashdot/slashdot";

    public static void main(String[] args) throws Exception {
        URL url = getDocumentURL(args);
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
        parser.parse(is, url.toString());
        Iterator iter = jrdfMem.find(null, null, null);
        while (iter.hasNext()) {
            System.err.println("Graph: " + iter.next());
        }
        is.close();
    }

    private static URL getDocumentURL(String[] args) throws MalformedURLException {
        String baseURL;
        if (args.length == 0 || args[0].length() == 0) {
            System.out.println("First argument empty so using: " + DEFAULT_RDF_URL);
            baseURL = DEFAULT_RDF_URL;
        } else {
            baseURL = args[0];
        }
        return new URL(baseURL);
    }
}
