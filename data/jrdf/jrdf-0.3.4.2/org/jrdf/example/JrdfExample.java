package org.jrdf.example;

import java.net.URI;
import java.util.Iterator;

import org.jrdf.graph.BlankNode;
import org.jrdf.graph.Graph;
import org.jrdf.graph.GraphElementFactory;
import org.jrdf.graph.GraphElementFactoryException;
import org.jrdf.graph.GraphException;
import org.jrdf.graph.Literal;
import org.jrdf.graph.Triple;
import org.jrdf.graph.TripleFactory;
import org.jrdf.graph.URIReference;
import org.jrdf.graph.mem.GraphImpl;
import org.jrdf.util.ClosableIterator;

/**
 * An example that performs simple operations on a JRDF Graph.
 *
 * @author Robert Turner
 */
public class JrdfExample {

  //Resources
  private URIReference person;
  private BlankNode address;

  //Properties
  private URIReference hasAddress;
  private URIReference hasStreet;
  private URIReference hasCity;
  private URIReference hasState;
  private URIReference hasPostCode;

  //Values
  private Literal street;
  private Literal city;
  private Literal state;
  private Literal postCode;

  //Statements
  private Triple addressStatement;
  private Triple streetStatement;
  private Triple cityStatement;
  private Triple stateStatement;
  private Triple postCodeStatement;

  /**
   * Default Constructor.
   */
  public JrdfExample() {

  }

  /**
   * Obtains a JRDF Graph and performs example operations on it.
   *
   * @throws Exception
   */
  private void runExample() throws Exception {

    System.out.println("Running example.");

    //get a JRDF Graph
    System.out.println("Creating Graph...");
    Graph graph = getGraph();

    //create example data
    initializeData(graph);

    //insert example statements
    populateGraph(graph);

    //perform find() operations
    searchGraph(graph);

    //reify a Statement
    performReification(graph);

    //remove a statement
    removeStatement(graph);

    System.out.println("Example finished.");
  }

  /**
   * Creates Nodes and Triples used by the Example.
   *
   * @throws Exception
   * @param graph Graph
   */
  private void initializeData(Graph graph) throws Exception {

    //initialize Nodes and Triples
    System.out.println("Creating Graph Elements...");

    //get the Factory
    GraphElementFactory elementFactory = graph.getElementFactory();

    //create resources
    person = elementFactory.createResource(new URI("http://example.org/staffid#85740"));
    address = elementFactory.createResource();

    //create properties
    hasAddress = elementFactory.createResource(new URI("http://example.org/terms#address"));
    hasStreet = elementFactory.createResource(new URI("http://example.org/terms#street"));
    hasCity = elementFactory.createResource(new URI("http://example.org/terms#city"));
    hasState = elementFactory.createResource(new URI("http://example.org/terms#state"));
    hasPostCode = elementFactory.createResource(new URI("http://example.org/terms#postalCode"));

    //create values
    street = elementFactory.createLiteral("1501 Grant Avenue");
    city = elementFactory.createLiteral("Bedford");
    state = elementFactory.createLiteral("Massachusetts");
    postCode = elementFactory.createLiteral("01730");

    //create statements
    addressStatement = elementFactory.createTriple(person, hasAddress, address);
    streetStatement = elementFactory.createTriple(address, hasStreet, street);
    cityStatement = elementFactory.createTriple(address, hasCity, city);
    stateStatement = elementFactory.createTriple(address, hasState, state);
    postCodeStatement = elementFactory.createTriple(address, hasPostCode, postCode);
  }

  /**
   * Inserts example statements into the Graph.
   *
   * @param graph Graph
   * @throws Exception
   */
  private void populateGraph(Graph graph) throws Exception {

    System.out.println("Populating Graph...");

    //insert the statements
    graph.add(addressStatement);
    graph.add(streetStatement);
    graph.add(cityStatement);
    graph.add(stateStatement);
    graph.add(postCodeStatement);

    //print contents
    print("Graph contains: ", graph);
  }

  /**
   * Performs find() operations on the Graph.
   *
   * @param graph Graph
   * @throws Exception
   */
  private void searchGraph(Graph graph) throws Exception {

    System.out.println("Searching Graph...");

    //get the Factory
    GraphElementFactory elementFactory = graph.getElementFactory();

    //get all Triples
    Triple findAll = elementFactory.createTriple(null, null, null);
    ClosableIterator allTriples = graph.find(findAll);
    print("Search for all triples: ", allTriples);
    allTriples.close();

    //search for address (as a subject)
    Triple findAddress = elementFactory.createTriple(address, null, null);
    ClosableIterator addressSubject = graph.find(findAddress);
    print("Search for address as a subject: ", addressSubject);
    addressSubject.close();

    //search for the city: "Bedford"
    Triple findCity = elementFactory.createTriple(null, null, city);
    ClosableIterator bedfordCity = graph.find(findCity);
    print("Search for city ('Bedford'): ", bedfordCity);
    bedfordCity.close();

    //search for any subject that has an address
    Triple findAddresses = elementFactory.createTriple(null, hasAddress, null);
    ClosableIterator addresses = graph.find(findAddresses);
    print("Search for subjects that have an address: ", addresses);
    addresses.close();
  }

  /**
   * Reifies a Statement.
   *
   * @param graph Graph
   * @throws Exception
   */
  private void performReification(Graph graph) throws Exception {

    System.out.println("Reifying a statement...");

    //get the Factories
    GraphElementFactory elementFactory = graph.getElementFactory();
    TripleFactory tripleFactory = graph.getTripleFactory();

    //create a resource to identify the statement
    URIReference statement = elementFactory.createResource(new URI("http://example.org/statement#address"));

    //reify the address statement (person, hasAddress, address)
    tripleFactory.reifyTriple(addressStatement, statement);

    //insert a statement about the original statement
    URIReference manager = elementFactory.createResource(new URI("http://example.org/managerid#65"));
    URIReference hasConfirmed = elementFactory.createResource(new URI("http://example.org/terms#hasConfirmed"));
    Triple confirmationStatement = elementFactory.createTriple(manager, hasConfirmed, statement);
    graph.add(confirmationStatement);

    //print the contents
    print("Graph contains (after reification): ", graph);
  }

  /**
   * Deletes a statement from the Graph.
   *
   * @param graph Graph
   * @throws Exception
   */
  private void removeStatement(Graph graph) throws Exception {

    System.out.println("Removing a statement...");

    //delete the city (address, hasCity, city)
    graph.remove(cityStatement);

    //print the contents
    print("Graph contains (after remove): ", graph);
  }

  /**
   * Returns an in-memory JRDF Graph implementation.
   *
   * @throws GraphException
   * @return Graph
   */
  private Graph getGraph() throws GraphException {

    return new GraphImpl();
  }

  /**
   * Prints the entire contents of a Graph to System.out
   *
   * @param message String
   * @param graph Graph
   * @throws IllegalArgumentException
   * @throws GraphElementFactoryException
   * @throws GraphException
   */
  private void print(String message, Graph graph) throws
      IllegalArgumentException,
      GraphElementFactoryException, GraphException {

    //validate
    if (null == graph) {

      throw new IllegalArgumentException("Graph is null.");
    }

    //find all statements
    Triple findAll = graph.getElementFactory().createTriple(null, null, null);
    ClosableIterator allTriples = graph.find(findAll);

    //print them
    print(message, allTriples);

    //release any resources
    allTriples.close();
  }

  /**
   * Prints the contents of an Iterator to System.out
   *
   * @param message String
   * @param iterator Iterator
   * @throws IllegalArgumentException
   */
  private void print(String message, Iterator iterator) throws
      IllegalArgumentException {

    //validate
    if (null == iterator) {

      throw new IllegalArgumentException("Iterator is null.");
    }

    //print message first
    System.out.println(message);

    //print the contents
    while (iterator.hasNext()) {

      System.out.println("" + iterator.next());
    }

    //print an empty line as a spacer
    System.out.println("");
  }

  /**
   * Instantiates a JRDFExample and runs it.
   *
   * @param args String[]
   */
  public static void main(String[] args) {

    try {

      JrdfExample example = new JrdfExample();
      example.runExample();
    }
    catch (Exception exception) {

      //print an exception if one occurs
      exception.printStackTrace();
    }
  }

}
