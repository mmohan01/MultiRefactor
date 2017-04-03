package org.jrdf.connection;

import java.util.Iterator;

import org.jrdf.graph.Graph;
import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphException;
import org.jrdf.graph.ObjectNode;
import org.jrdf.graph.PredicateNode;
import org.jrdf.graph.SubjectNode;
import org.jrdf.graph.Triple;
import org.jrdf.graph.TripleFactory;
import org.jrdf.util.ClosableIterator;

/**
 * Mock {@link Graph} for unit testing.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
final class MockGraph implements Graph {
    private ClosableIterator expectedIterator;

    public MockGraph(ClosableIterator expectedIterator) {
        this.expectedIterator = expectedIterator;
    }

    public boolean contains(SubjectNode subject, PredicateNode predicate, ObjectNode object) throws GraphException {
        throw new UnsupportedOperationException("Implement me...");
    }

    public boolean contains(Triple triple) throws GraphException {
        throw new UnsupportedOperationException("Implement me...");
    }

    public ClosableIterator find(SubjectNode subject, PredicateNode predicate, ObjectNode object) throws GraphException {
        throw new UnsupportedOperationException("Implement me...");
//        return expectedIterator;
    }

    public ClosableIterator find(Triple triple) throws GraphException {
        throw new UnsupportedOperationException("Implement me...");
    }

    public void add(SubjectNode subject, PredicateNode predicate, ObjectNode object) throws GraphException {
        throw new UnsupportedOperationException("Implement me...");
    }

    public void add(Triple triple) throws GraphException {
        throw new UnsupportedOperationException("Implement me...");
    }

    public void add(Iterator triples) throws GraphException {
        throw new UnsupportedOperationException("Implement me...");
    }

    public void close() {
        throw new UnsupportedOperationException("Implement me...");
    }

    public void remove(SubjectNode subject, PredicateNode predicate, ObjectNode object) throws GraphException {
        throw new UnsupportedOperationException("Implement me...");
    }

    public void remove(Triple triple) throws GraphException {
        throw new UnsupportedOperationException("Implement me...");
    }

    public void remove(Iterator triples) throws GraphException {
        throw new UnsupportedOperationException("Implement me...");
    }

    public GraphElementFactory getElementFactory() {
        throw new UnsupportedOperationException("Implement me...");
    }

    public TripleFactory getTripleFactory() {
        throw new UnsupportedOperationException("Implement me...");
    }

    public long getNumberOfTriples() throws GraphException {
        throw new UnsupportedOperationException("Implement me...");
    }

    public boolean isEmpty() throws GraphException {
        throw new UnsupportedOperationException("Implement me...");
    }
}
