/*  Sesame - Storage and Querying architecture for RDF and RDF Schema
 *  Copyright (C) 2001-2004 Aduna
 *  Copyright (C) 2004-2005 Andrew Newman - Conversion to JRDF
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
package org.jrdf.parser.rdfxml;

import java.io.*;
import java.net.*;
import java.util.*;

import javax.xml.transform.*;
import javax.xml.transform.sax.*;
import javax.xml.transform.stream.*;

import org.jrdf.graph.*;
import org.jrdf.graph.mem.*;
import org.jrdf.parser.*;
import org.jrdf.vocabulary.*;
import org.xml.sax.*;

/**
 * A filter on SAX events to make life easier on the RDF parser
 * itself. This filter does things like combining a call to
 * startElement() that is directly followed by a call to
 * endElement() to a single call to emptyElement().
 */
class SAXFilter implements org.xml.sax.ContentHandler {

  /**
   * The transformer handler.
   */
  private TransformerHandler _th;

  /**
   * Byte array for escaping XML.
   */
  private ByteArrayOutputStream _os = new ByteArrayOutputStream();

  /**
   * The RDF parser to supply the filtered SAX events to.
   **/
  private RdfXmlParser _rdfParser;

  /**
   * A Locator indicating a position in the text that is currently being
   * parsed by the SAX parser.
   **/
  private Locator _locator;

  /**
   * A listener that is interested in the progress of the SAX parser.
   **/
  private ParseLocationListener _locListener;

  /**
   * A listener that is interested in the namespaces that are defined in
   * the parsed RDF.
   **/
  private NamespaceListener _nsListener;

  /**
   * Stack of ElementInfo objects.
   **/
  private Stack _elInfoStack = new Stack();

  /**
   * StringBuffer used to collect text during parsing.
   **/
  private StringBuffer _charBuf = new StringBuffer(512);

  /**
   * The document's URI.
   **/
  private String _documentURI;

  /**
   * Flag indicating whether the parser parses stand-alone RDF
   * documents. In stand-alone documents, the rdf:RDF element is
   * optional if it contains just one element.
   **/
  private boolean _parseStandAloneDocuments;

  /**
   * Variable used to defer reporting of start tags. Reporting start
   * tags is deferred to be able to combine a start tag and an immediately
   * following end tag to a single call to emptyElement().
   **/
  private ElementInfo _deferredElement;

  /**
   * New namespace mappings that have been reported for the next start tag
   * by the SAX parser, but that are not yet assigned to an ElementInfo
   * object.
   **/
  private Map _newNamespaceMappings = new HashMap();

  /**
   * Flag indicating whether we're currently parsing RDF elements.
   **/
  private boolean _inRdfContext;

  /**
   * The number of elements on the stack that are in the RDF context.
   **/
  private int _rdfContextStackHeight;

  /**
   * Flag indicating whether we're currently parsing an XML literal.
   **/
  private boolean _parseLiteralMode;

  /**
   * The number of elements on the stack that are part of an XML literal.
   **/
  private int _xmlLiteralStackHeight;

  /**
   * The prefixes that are defined in the XML literal itself (this in
   * contrast to the namespaces from the XML literal's context).
   **/
  private List _xmlLiteralPrefixes = new ArrayList();

  /**
   * The prefixes that were used in an XML literal, but that were not
   * defined in it (but rather in the XML literal's context).
   **/
  private List _unknownPrefixesInXmlLiteral = new ArrayList();

  SAXFilter(RdfXmlParser rdfParser) throws TransformerConfigurationException {
    _rdfParser = rdfParser;
    _th = ((SAXTransformerFactory) SAXTransformerFactory.newInstance()).
        newTransformerHandler();
    _th.setResult(new StreamResult(_os));
  }

  public Locator getLocator() {
    return _locator;
  }

  public void setParseLocationListener(ParseLocationListener el) {
    _locListener = el;

    if (null != _locator) {
      _locListener.parseLocationUpdate(
          _locator.getLineNumber(), _locator.getColumnNumber());
    }
  }

  public ParseLocationListener getParseLocationListener() {
    return _locListener;
  }

  public void setNamespaceListener(NamespaceListener nl) {
    _nsListener = nl;
  }

  public NamespaceListener getNamespaceListener() {
    return _nsListener;
  }

  public void clear() {
    _locator = null;
    _elInfoStack.clear();
    _charBuf.setLength(0);
    _documentURI = null;
    _deferredElement = null;

    _newNamespaceMappings.clear();

    _inRdfContext = false;
    _rdfContextStackHeight = 0;

    _parseLiteralMode = false;
    _xmlLiteralStackHeight = 0;

    _xmlLiteralPrefixes.clear();
    _unknownPrefixesInXmlLiteral.clear();
  }

  public void setDocumentURI(String documentURI) throws SAXException {
    _documentURI = _createBaseURI(documentURI);
  }

  public void setParseStandAloneDocuments(boolean standAloneDocs) {
    _parseStandAloneDocuments = standAloneDocs;
  }

  public boolean getParseStandAloneDocuments() {
    return _parseStandAloneDocuments;
  }

  public void setDocumentLocator(Locator locator) {
    _locator = locator;
    if (null != _locListener) {
      _locListener.parseLocationUpdate(
          locator.getLineNumber(), locator.getColumnNumber());
    }
  }

  public void startDocument() throws SAXException {
    _th.startDocument();
  }

  public void endDocument() throws SAXException {
    // ignore
  }

  public void startPrefixMapping(String prefix, String uri) throws SAXException {
    if (null != _deferredElement) {
      // This new prefix mapping must come from a new start tag
      _reportDeferredStartElement();
    }

    _newNamespaceMappings.put(prefix, uri);

    if (_parseLiteralMode) {
      // This namespace is introduced inside an XML literal
      _xmlLiteralPrefixes.add(prefix);
    }

    if (null != _nsListener) {
      _nsListener.handleNamespace(prefix, uri);
    }
  }

  public void endPrefixMapping(String prefix) throws SAXException {
    if (_parseLiteralMode) {
      _xmlLiteralPrefixes.remove(prefix);
    }
  }

  public void startElement(String namespaceURI, String localName,
      String qName, Attributes attributes) throws SAXException {

    // Reset at start of element.
    _charBuf.setLength(0);

    if (null != _deferredElement) {
      // The next call could set _parseLiteralMode to true!
      _reportDeferredStartElement();
    }

    if (_parseLiteralMode) {
      _appendStartTag(qName, attributes);
      _xmlLiteralStackHeight++;
    }
    else {
      ElementInfo parent = _peekStack();
      ElementInfo elInfo = new ElementInfo(parent, qName, namespaceURI,
          localName);

      elInfo.setNamespaceMappings(_newNamespaceMappings);
      _newNamespaceMappings.clear();

      if (!_inRdfContext && _parseStandAloneDocuments &&
          (!"RDF".equals(localName) ||
          !namespaceURI.equals(RDF.baseURI.toString()))) {
        // Stand-alone document that does not start with an rdf:RDF root
        // element. Assume this root element is omitted.
        _inRdfContext = true;
      }

      if (!_inRdfContext) {
        // Check for presence of xml:base and xlm:lang attributes.
        for (int i = 0; i < attributes.getLength(); i++) {
          String attQName = attributes.getQName(i);

          if ("xml:base".equals(attQName)) {
            elInfo.baseURI = _createBaseURI(attributes.getValue(i));
          }
          else if ("xml:lang".equals(attQName)) {
            elInfo.xmlLang = attributes.getValue(i);
          }
        }

        // If xml:base or xml:lang attribute was not
        // present, inherit it from context.
        _inheritXmlAttributes(elInfo);

        _elInfoStack.push(elInfo);

        // Check if we are entering RDF context now.
        if ("RDF".equals(localName) &&
            namespaceURI.equals(RDF.baseURI.toString())) {
          _inRdfContext = true;
          _rdfContextStackHeight = 0;
        }
      }
      else {
        // We're parsing RDF elements.
        _checkAndCopyAttributes(attributes, elInfo);

        // If xml:base or xml:lang attribute was not
        // present, inherit it from context.
        _inheritXmlAttributes(elInfo);

        // Don't report the new element to the RDF parser just yet.
        _deferredElement = elInfo;
      }
    }
  }

  private void _reportDeferredStartElement() throws SAXException {
    /*
      // Only useful for debugging.
      if (_deferredElement == null) {
       throw new RuntimeException("no deferred start element available");
      }
     */

    _elInfoStack.push(_deferredElement);
    _rdfContextStackHeight++;

    try {
      _rdfParser.setBaseURI(new URI(_deferredElement.baseURI));
    }
    catch (URISyntaxException ex) {
      throw new SAXException("Bad URI", ex);
    }
    _rdfParser.setXmlLang(_deferredElement.xmlLang);

    _rdfParser.startElement(
        _deferredElement.namespaceURI, _deferredElement.localName,
        _deferredElement.qName, _deferredElement.atts);

    _deferredElement = null;
  }

  public void endElement(String namespaceURI, String localName, String qName)
      throws SAXException {
    // FIXME: in parseLiteralMode we should also check
    // if start- and end-tags match.

    if (_rdfParser._verifyData && !_parseLiteralMode) {
      // Verify that the end tag matches the start tag.
      ElementInfo elInfo;

      if (null != _deferredElement) {
        elInfo = _deferredElement;
      }
      else {
        elInfo = _peekStack();
      }

      if (!qName.equals(elInfo.qName)) {
        _rdfParser.sendFatalError(
            "expected end tag </'" + elInfo.qName + ">, " +
            "found </" + qName + ">");
      }
    }

    if (!_inRdfContext) {
      _elInfoStack.pop();
      _charBuf.setLength(0);
      return;
    }

    if (null == _deferredElement && 0 == _rdfContextStackHeight) {
      // This end tag removes the element that signaled the start
      // of the RDF context (i.e. <rdf:RDF>) from the stack.
      _inRdfContext = false;

      _elInfoStack.pop();
      _charBuf.setLength(0);
      return;
    }

    // We're still in RDF context.

    if (_parseLiteralMode) {
      if (0 < _xmlLiteralStackHeight) {
        _appendEndTag(qName);
        _xmlLiteralStackHeight--;
        return;
      }
      else {
        // This tag is no longer part of the XML literal
        // and it ends the parseLiteral mode.
        _parseLiteralMode = false;

        // Insert any used namespace prefixes from the XML literal's
        // context that are not defined in the XML literal itself.
        _insertUsedContextPrefixes();

        // Continue after this if-statement to process
        // the end tag as a normal end tag
      }
    }

    // Check for any deferred start elements
    if (null != _deferredElement) {
      // Start element still deferred, this is an empty element
      try {
        _rdfParser.setBaseURI(new URI(_deferredElement.baseURI));
      }
      catch (URISyntaxException ex) {
        throw new SAXException("Bad URI", ex);
      }
      _rdfParser.setXmlLang(_deferredElement.xmlLang);

      _rdfParser.emptyElement(
          _deferredElement.namespaceURI, _deferredElement.localName,
          _deferredElement.qName, _deferredElement.atts);

      _deferredElement = null;
    }
    else {
      // Check if any character data has been collected in the _charBuf
      boolean literalValue = 0 < _charBuf.toString().trim().length();

      if (literalValue) {
        _rdfParser.text(_charBuf.toString());
      }

      _charBuf.setLength(0);
      _elInfoStack.pop();
      _rdfContextStackHeight--;

      _rdfParser.endElement(namespaceURI, localName, qName);
    }
  }

  public void characters(char[] ch, int start, int length) throws SAXException {

    if (_inRdfContext) {
      if (null != _deferredElement) {
        _reportDeferredStartElement();
      }

      _th.characters(ch, start, length);

      if (_parseLiteralMode) {
        // Characters like '<', '>', and '&' must be escaped to
        // prevent breaking the XML text.
        _charBuf.append(_os.toString());
      }
      else {
        _charBuf.append(ch, start, length);
      }

      _os.reset();
    }
  }

  public void ignorableWhitespace(char[] ch, int start, int length) throws
      SAXException {
    if (_parseLiteralMode) {
      _charBuf.append(ch, start, length);
    }
  }

  public void processingInstruction(String target, String data) throws
      SAXException {
    // ignore
  }

  public void skippedEntity(String name) throws SAXException {
    // ignore
  }

  private void _checkAndCopyAttributes(Attributes attributes,
      ElementInfo elInfo) throws SAXException {
    Atts atts = new Atts(attributes.getLength());

    int attCount = attributes.getLength();
    for (int i = 0; i < attCount; i++) {
      String qName = attributes.getQName(i);
      String value = attributes.getValue(i);

      // attributes starting with "xml" should be ignored, except
      // for the ones that are handled by this parser (xml:lang and
      // xml:base).
      if (qName.startsWith("xml")) {
        if ("xml:lang".equals(qName)) {
          elInfo.xmlLang = value;
        }
        else if ("xml:base".equals(qName)) {
          elInfo.baseURI = _createBaseURI(value);
        }
      }
      else {
        String namespace = attributes.getURI(i);
        String localName = attributes.getLocalName(i);

        if (_rdfParser._verifyData) {
          if ("".equals(namespace)) {
            _rdfParser.sendError(
                "unqualified attribute '" +
                qName + "' not allowed");
          }
          else if (-1 == qName.indexOf(":")) {
            _rdfParser.sendWarning(
                "unqualified attribute '" +
                qName + "' not allowed");
          }
        }

        Att att = new Att(namespace, localName, qName, value);
        atts.addAtt(att);
      }
    }

    elInfo.atts = atts;
  }

  public void setParseLiteralMode() {
    _parseLiteralMode = true;
    _xmlLiteralStackHeight = 0;

    // All currently known namespace prefixes are
    // new for this XML literal.
    _xmlLiteralPrefixes.clear();
    _unknownPrefixesInXmlLiteral.clear();
  }

  private String _createBaseURI(String uriString) throws SAXException {
    try {
      URI tmpURI = new URI(uriString);
      tmpURI.normalize();
      return tmpURI.toString();
    }
    catch (URISyntaxException use) {
      throw new SAXException("Base URI", use);
    }
  }

  private void _inheritXmlAttributes(ElementInfo elInfo) {
    // If baseURI has not been set, inherit it from the context.
    if (null == elInfo.baseURI) {
      ElementInfo parent = _peekStack();
      if (null == parent) {
        elInfo.baseURI = _documentURI.toString();
      }
      else {
        elInfo.baseURI = parent.baseURI;
      }
    }

    // If xml:lang attribute has not been set, inherit it from the context.
    if (null == elInfo.xmlLang) {
      ElementInfo parent = _peekStack();
      if (null == parent) {
        elInfo.xmlLang = "";
      }
      else {
        elInfo.xmlLang = parent.xmlLang;
      }
    }
  }

  /**
   * Appends a start tag to _charBuf. This method is used during the
   * parsing of an XML Literal.
   **/
  private void _appendStartTag(String qName, Attributes attributes)
      throws SAXException {
    // Write start of start tag
    _charBuf.append("<" + qName);

    // Write any new namespace prefix definitions
    Iterator prefixes = _newNamespaceMappings.keySet().iterator();
    while (prefixes.hasNext()) {
      String prefix = (String) prefixes.next();
      String namespace = (String) _newNamespaceMappings.get(prefix);
      _appendNamespaceDecl(_charBuf, prefix, namespace);
    }

    // Write attributes
    int attCount = attributes.getLength();
    for (int i = 0; i < attCount; i++) {
      _appendAttribute(_charBuf,
          attributes.getQName(i), attributes.getValue(i));
    }

    // Write end of start tag
    _charBuf.append(">");

    // Check for any used prefixes that are not
    // defined in the XML literal itself
    int colonIdx = qName.indexOf(':');
    String prefix = 0 < colonIdx ? qName.substring(0, colonIdx) : "";

    if (!_xmlLiteralPrefixes.contains(prefix) &&
        !_unknownPrefixesInXmlLiteral.contains(prefix)) {
      _unknownPrefixesInXmlLiteral.add(prefix);
    }
  }

  /**
   * Appends an end tag to _charBuf. This method is used during the
   * parsing of an XML Literal.
   **/
  private void _appendEndTag(String qName) {
    _charBuf.append("</" + qName + ">");
  }

  /**
   * Inserts prefix mappings from an XML Literal's context for all prefixes
   * that are used in the XML Literal and that are not defined in the XML
   * Literal itself.
   **/
  private void _insertUsedContextPrefixes() throws SAXException {
    int unknownPrefixesCount = _unknownPrefixesInXmlLiteral.size();

    if (0 < unknownPrefixesCount) {
      // Create a String with all needed context prefixes
      StringBuffer contextPrefixes = new StringBuffer(1024);
      ElementInfo topElement = _peekStack();

      for (int i = 0; i < unknownPrefixesCount; i++) {
        String prefix = (String) _unknownPrefixesInXmlLiteral.get(i);
        String namespace = topElement.getNamespace(prefix);
        if (null != namespace) {
          _appendNamespaceDecl(contextPrefixes, prefix, namespace);
        }
      }

      // Insert this String before the first '>' character

      // StringBuffer.indexOf(String) requires JDK1.4 or newer
      //int endOfFirstStartTag = _charBuf.indexOf(">");
      int endOfFirstStartTag = 0;
      while ('>' != _charBuf.charAt(endOfFirstStartTag)) {
        endOfFirstStartTag++;
      }
      _charBuf.insert(endOfFirstStartTag, contextPrefixes.toString());
    }

    _unknownPrefixesInXmlLiteral.clear();
  }

  private void _appendNamespaceDecl(StringBuffer sb, String prefix,
      String namespace) throws SAXException {
    String attName = "xmlns";

    if (!"".equals(prefix)) {
      attName += ":" + prefix;
    }

    _appendAttribute(sb, attName, namespace);
  }

  private void _appendAttribute(StringBuffer sb, String name, String value)
      throws SAXException {
    sb.append(" ");
    sb.append(name);
    sb.append("=\"");

    char[] c = new char[value.length()];
    value.getChars(0, c.length-1, c, 0);
    _th.setResult(new StreamResult(_os));
    _th.characters(c, 0, c.length);
    sb.append(_os.toString());
    _os.reset();

    sb.append("\"");
  }

  private ElementInfo _peekStack() {
    ElementInfo result = null;

    if (!_elInfoStack.empty()) {
      result = (ElementInfo) _elInfoStack.peek();
    }

    return result;
  }

  static class ElementInfo {
    public String qName;
    public String namespaceURI;
    public String localName;
    public Atts atts;

    public ElementInfo parent;
    private Map _namespaceMap;

    public String baseURI;
    public String xmlLang;

    ElementInfo(String newQName, String newNamespaceURI, String newLocalName) {
      this(null, newQName, newNamespaceURI, newLocalName);
    }

    ElementInfo(ElementInfo newParent, String newQName, String newNamespaceURI,
        String newLocalName) {
      parent = newParent;
      qName = newQName;
      namespaceURI = newNamespaceURI;
      localName = newLocalName;
    }

    public void setNamespaceMappings(Map namespaceMappings) {
      if (namespaceMappings.isEmpty()) {
        _namespaceMap = null;
      }
      else {
        _namespaceMap = new HashMap(namespaceMappings);
      }
    }

    public String getNamespace(String prefix) {
      String result = null;

      if (null != _namespaceMap) {
        result = (String) _namespaceMap.get(prefix);
      }

      if (null == result && null != parent) {
        result = parent.getNamespace(prefix);
      }

      return result;
    }
  }
}
