package org.jrdf.query;

import org.jrdf.util.ClosableIterator;

/**
 * Mock {@link ClosableIterator}.
 *
 * @author Tom Adams
 * @version $Revision: 624 $
 */
class MockClosableIterator implements ClosableIterator {
  public boolean close() {
    throw new UnsupportedOperationException("Implement me...");
  }

  public boolean hasNext() {
    throw new UnsupportedOperationException("Implement me...");
  }

  public Object next() {
    throw new UnsupportedOperationException("Implement me...");
  }

  public void remove() {
    throw new UnsupportedOperationException("Implement me...");
  }
}
