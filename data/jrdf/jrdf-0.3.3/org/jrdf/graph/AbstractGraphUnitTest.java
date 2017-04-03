/*
 * $Header: /cvsroot/jrdf/jrdf/src/org/jrdf/graph/AbstractGraphUnitTest.java,v 1.8 2004/09/14 00:59:47 pgearon Exp $
 * $Revision: 1.8 $
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

package org.jrdf.graph;

import org.jrdf.graph.*;
import org.jrdf.util.*;

import java.net.*;
import java.util.*;

// Third party packages
import junit.framework.*;

/**
 * Abstract test case for graph implementations.
 *
 * @author <a href="mailto:pgearon@users.sourceforge.net">Paul Gearon</a>
 * @author Andrew Newman
 *
 * @version $Revision: 1.8 $
 */
public abstract class AbstractGraphUnitTest extends TestCase {

  /**
   * Instance of a graph object.
   */
  protected Graph graph;

  /**
   * Instance of a factory for the graph
   */
  protected GraphElementFactory elementFactory;

  // The following are interally used "constants"
  protected static BlankNode blank1;
  protected static BlankNode blank2;

  protected static URI uri1;
  protected static URI uri2;
  protected static URI uri3;
  protected static URIReference ref1;
  protected static URIReference ref2;
  protected static URIReference ref3;

  protected static final String TEST_STR1 = "A test string";
  protected static final String TEST_STR2 = "Another test string";
  protected static Literal l1;
  protected static Literal l2;

  /**
   * Constructs a new test with the given name.
   *
   * @param name the name of the test
   */
  public AbstractGraphUnitTest(String name) {
    super(name);
  }

  /**
   * Create test instance.
   */
  public void setUp() throws Exception {
    graph = newGraph();
    elementFactory = graph.getElementFactory();

    blank1 = elementFactory.createResource();
    blank2 = elementFactory.createResource();

    uri1 = new URI("http://namespace#somevalue");
    uri2 = new URI("http://namespace#someothervalue");
    uri3 = new URI("http://namespace#yetanothervalue");
    ref1 = elementFactory.createResource(uri1);
    ref2 = elementFactory.createResource(uri2);
    ref3 = elementFactory.createResource(uri3);

    l1 = elementFactory.createLiteral(TEST_STR1);
    l2 = elementFactory.createLiteral(TEST_STR2);
  }

  /**
   * Hook for test runner to obtain a test suite from.
   * Override in derived class.
   *
   * @return The test suite
   */
  public static Test suite() {
    return null;
  }

  //
  // implementation interfaces
  //

  /**
   * Create a graph implementation.
   *
   * @return A new GraphImpl.
   */
  public abstract Graph newGraph() throws Exception;

  //
  // Test cases
  //

  /**
   * Tests that a new graph is empty.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testEmpty() throws Exception {
    assertTrue(graph.isEmpty());
    assertEquals(0, graph.getNumberOfTriples());
  }


  /**
   * Tests that it is possible to get a NodeFactory from a graph.
   *
   * @throws Exception if query fails when it should have succeeded
   */
  public void testFactory() throws Exception {
    GraphElementFactory f = graph.getElementFactory();
    assertTrue(f != null);
  }


  /**
   * Tests addition.
   */
  public void testAddition() throws Exception {

    // add in a triple by nodes
    graph.add(blank1, ref1, blank2);

    assertFalse(graph.isEmpty());
    assertEquals(1, graph.getNumberOfTriples());

    // add in a whole triple
    Triple triple2 = elementFactory.createTriple(blank2, ref1, blank2);
    graph.add(triple2);

    assertFalse(graph.isEmpty());
    assertEquals(2, graph.getNumberOfTriples());

    // add in the first triple again
    graph.add(blank1, ref1, blank2);

    assertFalse(graph.isEmpty());
    assertEquals(2, graph.getNumberOfTriples());

    // add in the second whole triple again
    Triple triple2b = elementFactory.createTriple(blank2, ref1, blank2);
    graph.add(triple2b);
    assertFalse(graph.isEmpty());
    assertEquals(2, graph.getNumberOfTriples());

    // and again
    graph.add(triple2);
    assertFalse(graph.isEmpty());
    assertEquals(2, graph.getNumberOfTriples());

    // Add using iterator
    ArrayList list = new ArrayList();
    list.add(elementFactory.createTriple(ref1, ref1, ref1));
    list.add(elementFactory.createTriple(ref2, ref2, ref2));

    graph.add(list.iterator());
    assertFalse(graph.isEmpty());
    assertEquals(4, graph.getNumberOfTriples());
  }

  /**
   * Tests removal.
   */
  public void testRemoval() throws Exception {
    // add some test data
    graph.add(blank1, ref1, blank2);
    graph.add(blank1, ref2, blank2);
    graph.add(ref1, ref2, l2);
    Triple t1 = elementFactory.createTriple(blank2, ref1, blank1);
    graph.add(t1);
    Triple t2 = elementFactory.createTriple(blank2, ref2, blank1);
    graph.add(t2);
    Triple t3 = elementFactory.createTriple(blank2, ref1, l1);
    graph.add(t3);

    // check that all is well
    assertFalse(graph.isEmpty());
    assertEquals(6, graph.getNumberOfTriples());

    // delete the first statement
    graph.remove(blank1, ref1, blank2);
    assertEquals(5, graph.getNumberOfTriples());

    // delete the last statement
    graph.remove(t3);
    assertEquals(4, graph.getNumberOfTriples());

    // delete the next last statement with a new "triple object"
    t2 = elementFactory.createTriple(blank2, ref2, blank1);
    graph.remove(t2);
    assertEquals(3, graph.getNumberOfTriples());

    // delete the next last statement with a triple different to what it was built with
    graph.remove(blank2, ref1, blank1);
    assertEquals(2, graph.getNumberOfTriples());

    // delete the next last statement with a triple different to what it was built with
    graph.remove(ref1, ref2, l2);
    assertEquals(1, graph.getNumberOfTriples());

    // delete the wrong triple
    try {
      graph.remove(blank2, ref1, blank1);
      assertTrue(false);
    } catch (GraphException e) { /* no-op */ }
    assertEquals(1, graph.getNumberOfTriples());

    // delete a triple that never existed
    try {
      graph.remove(blank2, ref2, l2);
      assertTrue(false);
    } catch (GraphException e) { /* no-op */ }
    assertEquals(1, graph.getNumberOfTriples());

    // and delete with a triple object
    t1 = elementFactory.createTriple(blank2, ref1, blank1);
    try {
      graph.remove(t1);
      assertTrue(false);
    } catch (GraphException e) { /* no-op */ }
    assertEquals(1, graph.getNumberOfTriples());

    // now clear out the graph
    assertFalse(graph.isEmpty());
    graph.remove(blank1, ref2, blank2);
    assertTrue(graph.isEmpty());
    assertEquals(0, graph.getNumberOfTriples());

    // check that we can't still remove things
    try {
      graph.remove(blank1, ref2, blank2);
      assertTrue(false);
    } catch (GraphException e) { /* no-op */ }
    assertTrue(graph.isEmpty());
    assertEquals(0, graph.getNumberOfTriples());

    // Check removal using iterator
    graph.add(elementFactory.createTriple(ref1, ref1, ref1));
    graph.add(elementFactory.createTriple(ref2, ref2, ref2));

    ArrayList list = new ArrayList();
    list.add(elementFactory.createTriple(ref1, ref1, ref1));
    list.add(elementFactory.createTriple(ref2, ref2, ref2));
    graph.remove(list.iterator());

    // check that we can't still remove things
    try {
      graph.remove(ref2, ref2, ref2);
      assertTrue(false);
    } catch (GraphException e) { /* no-op */ }

    assertTrue(graph.isEmpty());
    assertEquals(0, graph.getNumberOfTriples());
  }


  /**
   * Tests containership.
   */
  public void testContains() throws Exception {
    // add some test data
    graph.add(blank1, ref1, blank2);
    graph.add(blank1, ref2, blank2);
    graph.add(ref1, ref2, l2);
    Triple t1 = elementFactory.createTriple(blank2, ref1, blank1);
    graph.add(t1);
    Triple t2 = elementFactory.createTriple(blank2, ref2, blank1);
    graph.add(t2);
    Triple t3 = elementFactory.createTriple(blank2, ref1, l1);
    graph.add(t3);

    // test containership
    assertTrue(graph.contains(blank1, ref1, blank2));
    // test with existing and built triples
    assertTrue(graph.contains(t1));
    t1 = elementFactory.createTriple(blank2, ref2, blank1);
    assertTrue(graph.contains(t1));

    // test non containership
    assertFalse(graph.contains(blank1, ref1, blank1));
    t1 = elementFactory.createTriple(blank2, ref2, ref1);
    assertFalse(graph.contains(t1));

    // test containership after removal
    graph.remove(blank1, ref1, blank2);
    assertFalse(graph.contains(blank1, ref1, blank2));
    t1 = elementFactory.createTriple(blank1, ref1, blank2);
    assertFalse(graph.contains(t1));

    // put it back in and test again
    graph.add(blank1, ref1, blank2);
    assertTrue(graph.contains(blank1, ref1, blank2));
    assertTrue(graph.contains(t1));

    // Null in contains.
    Graph newGraph = newGraph();
    assertFalse(newGraph.contains(null, null, null));

    // Add a statement
    GraphElementFactory newElementFactory = newGraph.getElementFactory();
    blank1 = newElementFactory.createResource();
    blank2 = newElementFactory.createResource();
    ref1 = newElementFactory.createResource(uri1);
    t1 = newElementFactory.createTriple(blank1, ref1, blank2);
    newGraph.add(t1);

    // Check for existance
    assertTrue(newGraph.contains(null, ref1, blank2));
    assertTrue(newGraph.contains(null, null, blank2));
    assertTrue(newGraph.contains(null, null, null));
    assertTrue(newGraph.contains(blank1, null, blank2));
    assertTrue(newGraph.contains(blank1, null, null));
    assertTrue(newGraph.contains(blank1, ref1, null));

    // Check non-existance
    assertFalse(newGraph.contains(null, ref2, blank1));
    assertFalse(newGraph.contains(null, null, blank1));
    assertFalse(newGraph.contains(blank2, null, blank1));
    assertFalse(newGraph.contains(blank2, null, null));
    assertFalse(newGraph.contains(blank2, ref2, null));
  }


  /**
   * Tests finding.
   */
  public void testFinding() throws Exception {
    graph.add(blank1, ref1, blank2);
    graph.add(blank1, ref1, l1);
    graph.add(blank1, ref2, blank2);
    graph.add(blank1, ref1, l2);
    graph.add(blank2, ref1, blank2);
    graph.add(blank2, ref2, blank2);
    graph.add(blank2, ref1, l1);
    graph.add(blank2, ref1, l2);

    // look for the first triple and check that one is returned
    ClosableIterator it = graph.find(blank1, ref1, blank2);
    assertTrue(it.hasNext());
    it.close();

    // look for a non-existent triple
    it = graph.find(ref1, ref1, blank1);
    assertFalse(it.hasNext());
    it.close();

    // look for doubles and check that there is data there
    it = graph.find(blank1, ref1, null);
    assertTrue(it.hasNext());
    it.close();
    it = graph.find(blank1, null, blank2);
    assertTrue(it.hasNext());
    it.close();
    it = graph.find(null, ref1, blank2);
    assertTrue(it.hasNext());
    it.close();

    // look for a non-existent double
    it = graph.find(ref1, ref1, null);
    assertFalse(it.hasNext());
    it.close();
    it = graph.find(ref1, null, blank2);
    assertFalse(it.hasNext());
    it.close();
    it = graph.find(null, ref3, blank2);
    assertFalse(it.hasNext());
    it.close();

    // look for singles
    it = graph.find(blank1, null, null);
    assertTrue(it.hasNext());
    it.close();
    it = graph.find(null, ref1, null);
    assertTrue(it.hasNext());
    it.close();
    it = graph.find(null, null, l1);
    assertTrue(it.hasNext());
    it.close();

    // look for non-existent singles
    it = graph.find(ref1, null, null);
    assertFalse(it.hasNext());
    it.close();
    it = graph.find(null, ref3, null);
    assertFalse(it.hasNext());
    it.close();
    it = graph.find(null, null, ref1);
    assertFalse(it.hasNext());
    it.close();

    // do it all again with triples

    // look for the first triple and check that one is returned
    Triple t = elementFactory.createTriple(blank1, ref1, blank2);
    it = graph.find(t);
    assertTrue(it.hasNext());
    it.close();

    // look for a non-existent triple
    t = elementFactory.createTriple(ref1, ref1, blank1);
    it = graph.find(t);
    assertFalse(it.hasNext());
    it.close();

    // look for doubles and check that there is data there
    t = elementFactory.createTriple(blank1, ref1, null);
    it = graph.find(t);
    assertTrue(it.hasNext());
    it.close();
    t = elementFactory.createTriple(blank1, null, blank2);
    it = graph.find(t);
    assertTrue(it.hasNext());
    it.close();
    t = elementFactory.createTriple(null, ref1, blank2);
    it = graph.find(t);
    assertTrue(it.hasNext());
    it.close();

    // look for a non-existent double
    t = elementFactory.createTriple(ref1, ref1, null);
    it = graph.find(t);
    assertFalse(it.hasNext());
    it.close();
    t = elementFactory.createTriple(ref1, null, blank2);
    it = graph.find(t);
    assertFalse(it.hasNext());
    it.close();
    t = elementFactory.createTriple(null, ref3, blank2);
    it = graph.find(t);
    assertFalse(it.hasNext());
    it.close();

    // look for singles
    t = elementFactory.createTriple(blank1, null, null);
    it = graph.find(t);
    assertTrue(it.hasNext());
    it.close();
    t = elementFactory.createTriple(null, ref1, null);
    it = graph.find(t);
    assertTrue(it.hasNext());
    it.close();
    t = elementFactory.createTriple(null, null, l1);
    it = graph.find(t);
    assertTrue(it.hasNext());
    it.close();

    // look for non-existent singles
    t = elementFactory.createTriple(ref1, null, null);
    it = graph.find(t);
    assertFalse(it.hasNext());
    it.close();
    t = elementFactory.createTriple(null, ref3, null);
    it = graph.find(t);
    assertFalse(it.hasNext());
    it.close();
    t = elementFactory.createTriple(null, null, ref1);
    it = graph.find(t);
    assertFalse(it.hasNext());
    it.close();
  }


  /**
   * Tests iteration over a found set.
   */
  public void testIteration() throws Exception {
    Triple t1 = elementFactory.createTriple(blank1, ref1, blank2);
    Triple t2 = elementFactory.createTriple(blank1, ref2, blank2);
    Triple t3 = elementFactory.createTriple(blank1, ref1, l1);
    Triple t4 = elementFactory.createTriple(blank1, ref1, l2);
    Triple t5 = elementFactory.createTriple(blank2, ref1, blank2);
    Triple t6 = elementFactory.createTriple(blank2, ref2, blank2);
    Triple t7 = elementFactory.createTriple(blank2, ref1, l1);
    Triple t8 = elementFactory.createTriple(blank2, ref1, l2);
    graph.add(t1);
    graph.add(t2);
    graph.add(t3);
    graph.add(t4);
    graph.add(t5);
    graph.add(t6);
    graph.add(t7);
    graph.add(t8);

    // look for the first triple and check that there is only one returned
    ClosableIterator it = graph.find(t1);
    assertTrue(it.hasNext());
    Triple t = (Triple)it.next();
    assertTrue(t.equals(t1));
    assertFalse(it.hasNext());

    Set triples = new HashSet();

    // look for doubles and check that there is data there
    t = elementFactory.createTriple(blank1, ref1, null);
    triples.add(t1);
    triples.add(t3);
    triples.add(t4);
    it = graph.find(t);
    checkSet(triples, it);

    t = elementFactory.createTriple(blank1, null, blank2);
    triples.add(t1);
    triples.add(t2);
    it = graph.find(t);
    checkSet(triples, it);

    t = elementFactory.createTriple(null, ref1, blank2);
    triples.add(t1);
    triples.add(t5);
    it = graph.find(t);
    checkSet(triples, it);

    // look for singles
    t = elementFactory.createTriple(blank1, null, null);
    triples.add(t1);
    triples.add(t2);
    triples.add(t3);
    triples.add(t4);
    it = graph.find(t);
    checkSet(triples, it);

    t = elementFactory.createTriple(null, ref1, null);
    triples.add(t1);
    triples.add(t3);
    triples.add(t4);
    triples.add(t5);
    triples.add(t7);
    triples.add(t8);
    it = graph.find(t);
    checkSet(triples, it);

    t = elementFactory.createTriple(null, null, l1);
    triples.add(t3);
    triples.add(t7);
    it = graph.find(t);
    checkSet(triples, it);

    // look for the first triple and check that there is only one returned
    it = graph.find(blank1, ref1, blank2);
    assertTrue(it.hasNext());
    t = (Triple)it.next();
    assertTrue(t.equals(t1));
    assertFalse(it.hasNext());

    // look for doubles and check that there is data there
    it = graph.find(blank1, ref1, null);
    triples.add(t1);
    triples.add(t3);
    triples.add(t4);
    checkSet(triples, it);

    it=graph.find(blank1, null, blank2);
    triples.add(t1);
    triples.add(t2);
    checkSet(triples, it);

    it=graph.find(null, ref1, blank2);
    triples.add(t1);
    triples.add(t5);
    checkSet(triples, it);

    // look for singles
    it=graph.find(blank1, null, null);
    triples.add(t1);
    triples.add(t2);
    triples.add(t3);
    triples.add(t4);
    checkSet(triples, it);

    it=graph.find(null, ref1, null);
    triples.add(t1);
    triples.add(t3);
    triples.add(t4);
    triples.add(t5);
    triples.add(t7);
    triples.add(t8);
    checkSet(triples, it);

    it=graph.find(null, null, l1);
    triples.add(t3);
    triples.add(t7);
    checkSet(triples, it);

  }


  /**
   * Tests iterative removal.
   */
  public void testIterativeRemoval() throws Exception {
    // add some test data
    graph.add(blank1, ref1, blank2);
    graph.add(blank1, ref2, blank2);
    graph.add(ref1, ref2, l2);
    Triple t1 = elementFactory.createTriple(blank2, ref1, blank1);
    graph.add(t1);
    Triple t2 = elementFactory.createTriple(blank2, ref2, blank1);
    graph.add(t2);
    Triple t3 = elementFactory.createTriple(blank2, ref1, l1);
    graph.add(t3);

    // check that all is well
    assertFalse(graph.isEmpty());
    assertEquals(6, graph.getNumberOfTriples());

    // get an iterator for the blank2,ref1 elements
    ClosableIterator ci = graph.find(blank2, ref1, null);

    // remove the first element
    assertTrue(ci.hasNext());
    ci.next();
    ci.remove();
    assertEquals(5, graph.getNumberOfTriples());
    
    // remove the second element
    assertTrue(ci.hasNext());
    ci.next();
    ci.remove();
    assertEquals(4, graph.getNumberOfTriples());

    assertFalse(ci.hasNext());

    // get an iterator for the blank1 elements
    ci = graph.find(blank1, null, null);

    // remove the first element
    assertTrue(ci.hasNext());
    ci.next();
    ci.remove();
    assertEquals(3, graph.getNumberOfTriples());
    
    // remove the second element
    assertTrue(ci.hasNext());
    ci.next();
    ci.remove();
    assertEquals(2, graph.getNumberOfTriples());

    assertFalse(ci.hasNext());

    // get an iterator for the ref1, ref2, l2 element
    ci = graph.find(ref1, ref2, l2);

    // remove the element
    assertTrue(ci.hasNext());
    ci.next();
    ci.remove();
    assertEquals(1, graph.getNumberOfTriples());
    
    assertFalse(ci.hasNext());

    // get an iterator for the final element
    ci = graph.find(null, null, null);

    // remove the element
    assertTrue(ci.hasNext());
    ci.next();
    ci.remove();
    assertEquals(0, graph.getNumberOfTriples());
    assertTrue(graph.isEmpty());
    
    assertFalse(ci.hasNext());
    ci.close();

    // check that we can't still remove things
    try {
      graph.remove(ref2, ref2, ref2);
      assertTrue(false);
    } catch (GraphException e) { /* no-op */ }

  }


  /**
   * Tests full iterative removal.
   */
  public void testFullIterativeRemoval() throws Exception {
    // add some test data
    graph.add(blank1, ref1, blank2);
    graph.add(blank1, ref2, blank2);
    graph.add(ref1, ref2, l2);
    Triple t1 = elementFactory.createTriple(blank2, ref1, blank1);
    graph.add(t1);
    Triple t2 = elementFactory.createTriple(blank2, ref2, blank1);
    graph.add(t2);
    Triple t3 = elementFactory.createTriple(blank2, ref1, l1);
    graph.add(t3);

    // check that all is well
    assertFalse(graph.isEmpty());
    assertEquals(6, graph.getNumberOfTriples());

    // get an iterator for all the elements
    ClosableIterator ci = graph.find(null, null, null);
    for (int i = 5; i >= 0; i--) {
      // remove the element
      assertTrue(ci.hasNext());
      ci.next();
      ci.remove();
      assertEquals(i, graph.getNumberOfTriples());
    }

    assertTrue(graph.isEmpty());
    
    assertFalse(ci.hasNext());

    ci.close();
  }


  /**
   * Checks that an iterator matches a set exactly.
   * The set will be emptied and the iterator will be closed.
   *
   * @throws Exception If the set does not match the iterator.
   */
  private void checkSet(Set triples, ClosableIterator it) throws Exception {
    while (it.hasNext()) {
      Triple t = (Triple)it.next();
      assertTrue(triples.contains(t));
      triples.remove(t);
    }
    if (!triples.isEmpty()) {
      System.out.println("triples still contains: " + triples.toString());
    }
    assertTrue(triples.isEmpty());
    it.close();
  }

}
