/*
 * $Header: /cvsroot/jrdf/jrdf/src/org/jrdf/graph/mem/GraphIterator.java,v 1.5 2004/09/14 00:59:47 pgearon Exp $
 * $Revision: 1.5 $
 * $Date: 2004/09/14 00:59:47 $
 *
 * ====================================================================
 *
 * The Apache Software License, Version 1.1
 *
 * Copyright (c) 2003, 2004 The JRDF Project.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in
 *    the documentation and/or other materials provided with the
 *    distribution.
 *
 * 3. The end-user documentation included with the redistribution, if
 *    any, must include the following acknowlegement:
 *       "This product includes software developed by the
 *        the JRDF Project (http://jrdf.sf.net/)."
 *    Alternately, this acknowlegement may appear in the software itself,
 *    if and wherever such third-party acknowlegements normally appear.
 *
 * 4. The names "The JRDF Project" and "JRDF" must not be used to endorse
 *    or promote products derived from this software without prior written
 *    permission. For written permission, please contact
 *    newmana@users.sourceforge.net.
 *
 * 5. Products derived from this software may not be called "JRDF"
 *    nor may "JRDF" appear in their names without prior written
 *    permission of the JRDF Project.
 *
 * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE APACHE SOFTWARE FOUNDATION OR
 * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
 * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
 * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * ====================================================================
 *
 * This software consists of voluntary contributions made by many
 * individuals on behalf of the JRDF Project.  For more
 * information on JRDF, please see <http://jrdf.sourceforge.net/>.
 */

package org.jrdf.graph.mem;

import org.jrdf.util.ClosableIterator;
import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphException;

import java.util.*;

/**
 * An iterator that iterates over an entire graph.
 * Relies on internal iterators which iterate over all entries in
 * the first map, the maps it points to, and the sets they point to.
 * The itemIterator  is used to indicate the current position.
 * It will always be set to return the next value until it reaches
 * the end of the graph.
 *
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 *
 * @version $Revision: 1.5 $
 */
public class GraphIterator implements ClosableIterator {

  /** The iterator for the first index. */
  private Iterator iterator;

  /** The iterator for the second index. */
  private Iterator subIterator;

  /** The iterator for the third index. */
  private Iterator itemIterator;

  /** The current element for the iterator on the first index. */
  private Map.Entry firstEntry;

  /** The current element for the iterator on the second index. */
  private Map.Entry secondEntry;

  /** The graph this iterator will operate on.  Only needed by the remove method. */
  private GraphImpl graph;

  /** The index of this iterator.  Only needed for initialization and the remove method. */
  private Map index;

  /** The current subject predicate and object, last returned from next().  Only needed by the remove method. */
  private Long[] currentNodes;

  /** The nodeFactory used to create the nodes to be returned in the triples. */
  private GraphElementFactoryImpl nodeFactory;


  /**
   * Constructor.  Sets up the internal iterators.
   */
  GraphIterator(Map index, GraphElementFactoryImpl nodeFactory, GraphImpl graph) {
    // store the node factory
    this.nodeFactory = nodeFactory;
    this.graph = graph;
    this.index = index;
    this.currentNodes = null;

    // initialise the iterators to empty
    itemIterator = null;
    subIterator = null;
    // start the iterator on the main index
    iterator = index.entrySet().iterator();
  }


  /**
   * Returns true if the iteration has more elements.
   *
   * @return <code>true</code> If there is an element to be read.
   */
  public boolean hasNext() {
    // confirm we still have an item iterator, and that it has data available
    return (itemIterator != null && itemIterator.hasNext()) ||
      (subIterator != null && subIterator.hasNext()) ||
      (iterator != null && iterator.hasNext());
  }


  /**
   * Returns the next element in the iteration.
   *
   * @return the next element in the iteration.
   * @throws NoSuchElementException iteration has no more elements.
   */
  public Object next() throws NoSuchElementException {
    if (iterator == null) throw new NoSuchElementException();

    // move to the next position
    updatePosition();

    if (iterator == null) throw new NoSuchElementException();

    // get the next item
    Long third = (Long) itemIterator.next();

    // construct the triple
    Long second = (Long) secondEntry.getKey();
    Long first = (Long) firstEntry.getKey();

    // get back the nodes for these IDs and uild the triple
    currentNodes = new Long[] { first, second, third };
    return new TripleImpl(nodeFactory, first, second, third);
  }


  /**
   * Helper method to move the iterators on to the next position.
   * If there is no next position then {@link #itemIterator itemIterator}
   * will be set to null, telling {@link #hasNext() hasNext} to return
   * <code>false</code>.
   */
  private void updatePosition() {
    // progress to the next item if needed
    if (itemIterator == null || !itemIterator.hasNext()) {
      // the current iterator been exhausted
      if (subIterator == null || !subIterator.hasNext()) {
        // the subiterator has been exhausted
        if (!iterator.hasNext()) {
          // the main iterator has been exhausted
          // tell the iterator to finish
          iterator = null;
          return;
        }
        // move on the main iterator
        firstEntry = (Map.Entry)iterator.next();
        // now get an iterator to the sub index map
        subIterator = ((Map)firstEntry.getValue()).entrySet().iterator();
        assert subIterator.hasNext();
      }
      // get the next entry of the sub index
      secondEntry = (Map.Entry)subIterator.next();
      // get an interator to the next set from the sub index
      itemIterator = ((Set)secondEntry.getValue()).iterator();
      assert itemIterator.hasNext();
    }
  }


  /**
   * Implemented for java.util.Iterator.
   */
  public void remove() {
    if (itemIterator != null) {
      itemIterator.remove();
      // clean up the current index after the removal
      cleanIndex();
      // now remove from the other 2 indexes
      removeFromNonCurrentIndex();
    } else {
      throw new IllegalStateException("Beyond end of data");
    }
  }


  /**
   * Checks if a removed item is the last of its type, and removes any associated subindexes if appropriate.
   */
  private void cleanIndex() {
    // check if a set was cleaned out
    Set subGroup = (Set)secondEntry.getValue();
    if (subGroup.isEmpty()) {
      // remove the entry for the set
      subIterator.remove();
      // check if a subindex was cleaned out
      Map subIndex = (Map)firstEntry.getValue();
      if (subIndex.isEmpty()) {
        // remove the subindex
        iterator.remove();
      }
    }
  }


  /**
   * Helper function for removal.  This removes the current statement from the indexes which
   * the current iterator is not associated with.
   */
  private void removeFromNonCurrentIndex() {
    try {
      // can instead use var here to determine how to delete, but this is more intuitive
      if (index == graph.index012) {
        graph.remove(graph.index120, currentNodes[1], currentNodes[2], currentNodes[0]);
        graph.remove(graph.index201, currentNodes[2], currentNodes[0], currentNodes[1]);
      }
      if (index == graph.index120) {
        graph.remove(graph.index012, currentNodes[2], currentNodes[0], currentNodes[1]);
        graph.remove(graph.index201, currentNodes[1], currentNodes[2], currentNodes[0]);
      }
      if (index == graph.index201) {
        graph.remove(graph.index012, currentNodes[1], currentNodes[2], currentNodes[0]);
        graph.remove(graph.index120, currentNodes[2], currentNodes[0], currentNodes[1]);
      }
    } catch (GraphException ge) {
      throw new IllegalStateException(ge.getMessage());
    }
  }


  /**
   * Closes the iterator by freeing any resources that it current holds.
   * Nothing to be done for this class.
   * @return <code>true</code> indicating success.
   */
  public boolean close() {
    return true;
  }

}
