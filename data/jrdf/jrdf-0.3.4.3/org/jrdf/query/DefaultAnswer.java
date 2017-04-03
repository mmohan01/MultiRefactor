package org.jrdf.query;

import org.jrdf.util.ClosableIterator;

/**
 * Default implementation of {@link Answer}.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
public final class DefaultAnswer implements Answer {
    private ClosableIterator closableIterator;

    public DefaultAnswer(ClosableIterator closableIterator) {
        this.closableIterator = closableIterator;
    }

    public ClosableIterator getClosableIterator() {
        return closableIterator;
    }
}
