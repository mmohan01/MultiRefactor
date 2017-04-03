/*  Sesame - Storage and Querying architecture for RDF and RDF Schema
 *  Copyright (C) 2001-2005 Aduna
 *  Copyright (C) 2005 Andrew Newman - Conversion to JRDF.
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

import org.jrdf.parser.NamespaceListener;
import org.jrdf.parser.ParseLocationListener;
import org.jrdf.vocabulary.RDF;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.sax.SAXTransformerFactory;
import javax.xml.transform.sax.TransformerHandler;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Stack;

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
    private TransformerHandler th;

    /**
     * Byte array for escaping XML.
     */
    private ByteArrayOutputStream os = new ByteArrayOutputStream();

    /**
     * Output stream for escaping XML.
     */
    private OutputStreamWriter osw = new OutputStreamWriter(os);

    /**
     * The RDF parser to supply the filtered SAX events to.
     */
    private RdfXmlParser rdfParser;

    /**
     * A Locator indicating a position in the text that is currently being
     * parsed by the SAX parser.
     */
    private Locator locator;

    /**
     * A listener that is interested in the progress of the SAX parser.
     */
    private ParseLocationListener locListener;

    /**
     * A listener that is interested in the namespaces that are defined in
     * the parsed RDF.
     */
    private NamespaceListener nsListener;

    /**
     * Stack of ElementInfo objects.
     */
    private Stack elInfoStack = new Stack();

    /**
     * StringBuffer used to collect text during parsing.
     */
    private StringBuffer charBuf = new StringBuffer(512);

    /**
     * The document's URI.
     */
    private URI documentURI;

    /**
     * Flag indicating whether the parser parses stand-alone RDF
     * documents. In stand-alone documents, the rdf:RDF element is
     * optional if it contains just one element.
     */
    private boolean parseStandAloneDocuments;

    /**
     * Variable used to defer reporting of start tags. Reporting start
     * tags is deferred to be able to combine a start tag and an immediately
     * following end tag to a single call to emptyElement().
     */
    private ElementInfo deferredElement;

    /**
     * New namespace mappings that have been reported for the next start tag
     * by the SAX parser, but that are not yet assigned to an ElementInfo
     * object.
     */
    private Map newNamespaceMappings = new HashMap();

    /**
     * Flag indicating whether we're currently parsing RDF elements.
     */
    private boolean inRdfContext;

    /**
     * The number of elements on the stack that are in the RDF context.
     */
    private int rdfContextStackHeight;

    /**
     * Flag indicating whether we're currently parsing an XML literal.
     */
    private boolean parseLiteralMode;

    /**
     * The number of elements on the stack that are part of an XML literal.
     */
    private int xmlLiteralStackHeight;

    /**
     * The prefixes that are defined in the XML literal itself (this in
     * contrast to the namespaces from the XML literal's context).
     */
    private List xmlLiteralPrefixes = new ArrayList();

    /**
     * The prefixes that were used in an XML literal, but that were not
     * defined in it (but rather in the XML literal's context).
     */
    private List unknownPrefixesInXmlLiteral = new ArrayList();

    SAXFilter(RdfXmlParser rdfParser) throws TransformerConfigurationException {
        this.rdfParser = rdfParser;
        th = ((SAXTransformerFactory) SAXTransformerFactory.newInstance()).
                newTransformerHandler();
        th.setResult(new StreamResult(os));
    }

    public Locator getLocator() {
        return locator;
    }

    public void setParseLocationListener(ParseLocationListener el) {
        locListener = el;

        if (null != locator) {
            locListener.parseLocationUpdate(locator.getLineNumber(),
                    locator.getColumnNumber());
        }
    }

    public ParseLocationListener getParseLocationListener() {
        return locListener;
    }

    public void setNamespaceListener(NamespaceListener nl) {
        nsListener = nl;
    }

    public NamespaceListener getNamespaceListener() {
        return nsListener;
    }

    public void clear() {
        locator = null;
        elInfoStack.clear();
        charBuf.setLength(0);
        documentURI = null;
        deferredElement = null;

        newNamespaceMappings.clear();

        inRdfContext = false;
        rdfContextStackHeight = 0;

        parseLiteralMode = false;
        xmlLiteralStackHeight = 0;

        xmlLiteralPrefixes.clear();
        unknownPrefixesInXmlLiteral.clear();
    }

    public void setDocumentURI(String documentURI) {
        this.documentURI = createBaseURI(documentURI);
    }

    public void setParseStandAloneDocuments(boolean standAloneDocs) {
        parseStandAloneDocuments = standAloneDocs;
    }

    public boolean getParseStandAloneDocuments() {
        return parseStandAloneDocuments;
    }

    public void setDocumentLocator(Locator locator) {
        this.locator = locator;
        if (null != locListener) {
            locListener.parseLocationUpdate(locator.getLineNumber(),
                    locator.getColumnNumber());
        }
    }

    public void startDocument() throws SAXException {
        th.startDocument();
        escapeXml(new char[]{}, 0, 0, new StringBuffer());

    }

    public void endDocument() throws SAXException {
        // ignore
    }

    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        if (null != deferredElement) {
            // This new prefix mapping must come from a new start tag
            reportDeferredStartElement();
        }

        newNamespaceMappings.put(prefix, uri);

        if (parseLiteralMode) {
            // This namespace is introduced inside an XML literal
            xmlLiteralPrefixes.add(prefix);
        }

        if (null != nsListener) {
            nsListener.handleNamespace(prefix, uri);
        }
    }

    public void endPrefixMapping(String prefix) throws SAXException {
        if (parseLiteralMode) {
            xmlLiteralPrefixes.remove(prefix);
        }
    }

    public void startElement(String namespaceURI, String localName,
            String qName, Attributes attributes) throws SAXException {

        if (null != deferredElement) {
            // The next call could set parseLiteralMode to true!
            reportDeferredStartElement();
        }

        if (parseLiteralMode) {
            appendStartTag(qName, attributes);
            xmlLiteralStackHeight++;
        } else {
            ElementInfo parent = peekStack();
            ElementInfo elInfo = new ElementInfo(parent, qName, namespaceURI,
                    localName);

            elInfo.setNamespaceMappings(newNamespaceMappings);
            newNamespaceMappings.clear();

            if (!inRdfContext && parseStandAloneDocuments &&
                    (!"RDF".equals(localName) ||
                            !namespaceURI.equals(RDF.baseURI.toString()))) {
                // Stand-alone document that does not start with an rdf:RDF root
                // element. Assume this root element is omitted.
                inRdfContext = true;
            }

            if (!inRdfContext) {
                // Check for presence of xml:base and xlm:lang attributes.
                for (int i = 0; i < attributes.getLength(); i++) {
                    String attQName = attributes.getQName(i);

                    if ("xml:base".equals(attQName)) {
                        elInfo.setBaseURI(attributes.getValue(i));
                    } else if ("xml:lang".equals(attQName)) {
                        elInfo.setXmlLang(attributes.getValue(i));
                    }
                }

                elInfoStack.push(elInfo);

                // Check if we are entering RDF context now.
                if ("RDF".equals(localName) &&
                        namespaceURI.equals(RDF.baseURI.toString())) {
                    inRdfContext = true;
                    rdfContextStackHeight = 0;
                }
            } else {
                // We're parsing RDF elements.
                checkAndCopyAttributes(attributes, elInfo);

                // Don't report the new element to the RDF parser just yet.
                deferredElement = elInfo;
            }
        }
    }

    private void reportDeferredStartElement() throws SAXException {
        /*
          // Only useful for debugging.
          if (deferredElement == null) {
           throw new RuntimeException("no deferred start element available");
          }
         */

        elInfoStack.push(deferredElement);
        rdfContextStackHeight++;

        rdfParser.setBaseURI(deferredElement.getBaseURI());
        rdfParser.setXmlLang(deferredElement.getXmlLang());

        rdfParser.startElement(deferredElement.getNamespaceURI(), deferredElement.getLocalName(),
                deferredElement.getqName(), deferredElement.getAtts());

        deferredElement = null;
    }

    public void endElement(String namespaceURI, String localName, String qName) throws SAXException {
        // FIXME: in parseLiteralMode we should also check
        // if start- and end-tags match.

        if (rdfParser._verifyData && !parseLiteralMode) {
            // Verify that the end tag matches the start tag.
            ElementInfo elInfo;

            if (null != deferredElement) {
                elInfo = deferredElement;
            } else {
                elInfo = peekStack();
            }

            if (!qName.equals(elInfo.getqName())) {
                rdfParser.sendFatalError("expected end tag </'" + elInfo.getqName() +
                        ">, " +
                        "found </" + qName + ">");
            }
        }

        if (!inRdfContext) {
            elInfoStack.pop();
            charBuf.setLength(0);
            return;
        }

        if (null == deferredElement && 0 == rdfContextStackHeight) {
            // This end tag removes the element that signaled the start
            // of the RDF context (i.e. <rdf:RDF>) from the stack.
            inRdfContext = false;

            elInfoStack.pop();
            charBuf.setLength(0);
            return;
        }

        // We're still in RDF context.

        if (parseLiteralMode) {
            if (0 < xmlLiteralStackHeight) {
                appendEndTag(qName);
                xmlLiteralStackHeight--;
                return;
            } else {
                // This tag is no longer part of the XML literal
                // and it ends the parseLiteral mode.
                parseLiteralMode = false;

                // Insert any used namespace prefixes from the XML literal's
                // context that are not defined in the XML literal itself.
                insertUsedContextPrefixes();

                // Continue after this if-statement to process
                // the end tag as a normal end tag
            }
        }

        // Check for any deferred start elements
        if (null != deferredElement) {
            // Start element still deferred, this is an empty element
            rdfParser.setBaseURI(deferredElement.getBaseURI());
            rdfParser.setXmlLang(deferredElement.getXmlLang());

            rdfParser.emptyElement(deferredElement.getNamespaceURI(), deferredElement.getLocalName(),
                    deferredElement.getqName(), deferredElement.getAtts());

            deferredElement = null;
        } else {
            if (parseLiteralMode) {
                // Insert any used namespace prefixes from the XML literal's
                // context that are not defined in the XML literal itself.
                insertUsedContextPrefixes();
            }

            // Check if any character data has been collected in the _charBuf
            String s = charBuf.toString().trim();
            charBuf.setLength(0);

            if (s.length() > 0 || parseLiteralMode) {
                rdfParser.text(s);
                parseLiteralMode = false;
            }

            elInfoStack.pop();
            rdfContextStackHeight--;

            rdfParser.endElement(namespaceURI, localName, qName);
        }
    }

    public void characters(char[] ch, int start, int length) throws SAXException {

        if (inRdfContext) {
            if (null != deferredElement) {
                reportDeferredStartElement();
            }

            if (parseLiteralMode) {
                // Characters like '<', '>', and '&' must be escaped to
                // prevent breaking the XML text.
                escapeXml(ch, start, length, charBuf);
            } else {
                charBuf.append(ch, start, length);
            }

            os.reset();
        }
    }

    public void ignorableWhitespace(char[] ch, int start, int length) throws
            SAXException {
        if (parseLiteralMode) {
            charBuf.append(ch, start, length);
        }
    }

    public void processingInstruction(String target, String data) throws
            SAXException {
        // ignore
    }

    public void skippedEntity(String name) throws SAXException {
        // ignore
    }

    private void checkAndCopyAttributes(Attributes attributes,
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
                    elInfo.setXmlLang(value);
                } else if ("xml:base".equals(qName)) {
                    elInfo.setBaseURI(value);
                }
            } else {
                String namespace = attributes.getURI(i);
                String localName = attributes.getLocalName(i);

                if (rdfParser._verifyData) {
                    if ("".equals(namespace)) {
                        rdfParser.sendError("unqualified attribute '" +
                                qName + "' not allowed");
                    } else if (-1 == qName.indexOf(":")) {
                        rdfParser.sendWarning("unqualified attribute '" +
                                qName + "' not allowed");
                    }
                }

                Att att = new Att(namespace, localName, qName, value);
                atts.addAtt(att);
            }
        }

        elInfo.setAtts(atts);
    }

    public void setParseLiteralMode() {
        parseLiteralMode = true;
        xmlLiteralStackHeight = 0;

        // All currently known namespace prefixes are
        // new for this XML literal.
        xmlLiteralPrefixes.clear();
        unknownPrefixesInXmlLiteral.clear();
    }

    private URI createBaseURI(String uriString) {
        return URI.create(uriString).normalize();
    }

    /**
     * Appends a start tag to charBuf. This method is used during the
     * parsing of an XML Literal.
     */
    private void appendStartTag(String qName, Attributes attributes) throws SAXException {
        // Write start of start tag
        charBuf.append("<").append(qName);

        // Write any new namespace prefix definitions
        Iterator prefixes = newNamespaceMappings.keySet().iterator();
        while (prefixes.hasNext()) {
            String prefix = (String) prefixes.next();
            String namespace = (String) newNamespaceMappings.get(prefix);
            appendNamespaceDecl(charBuf, prefix, namespace);
        }

        // Write attributes
        int attCount = attributes.getLength();
        for (int i = 0; i < attCount; i++) {
            appendAttribute(charBuf,
                    attributes.getQName(i), attributes.getValue(i));
        }

        // Write end of start tag
        charBuf.append(">");

        // Check for any used prefixes that are not
        // defined in the XML literal itself
        int colonIdx = qName.indexOf(':');
        String prefix = 0 < colonIdx ? qName.substring(0, colonIdx) : "";

        if (!xmlLiteralPrefixes.contains(prefix) &&
                !unknownPrefixesInXmlLiteral.contains(prefix)) {
            unknownPrefixesInXmlLiteral.add(prefix);
        }
    }

    /**
     * Appends an end tag to charBuf. This method is used during the
     * parsing of an XML Literal.
     */
    private void appendEndTag(String qName) {
        charBuf.append("</").append(qName).append(">");
    }

    /**
     * Inserts prefix mappings from an XML Literal's context for all prefixes
     * that are used in the XML Literal and that are not defined in the XML
     * Literal itself.
     */
    private void insertUsedContextPrefixes() throws SAXException {
        int unknownPrefixesCount = unknownPrefixesInXmlLiteral.size();

        if (0 < unknownPrefixesCount) {
            // Create a String with all needed context prefixes
            StringBuffer contextPrefixes = new StringBuffer(1024);
            ElementInfo topElement = peekStack();

            for (int i = 0; i < unknownPrefixesCount; i++) {
                String prefix = (String) unknownPrefixesInXmlLiteral.get(i);
                String namespace = topElement.getNamespace(prefix);
                if (null != namespace) {
                    appendNamespaceDecl(contextPrefixes, prefix, namespace);
                }
            }

            // Insert this String before the first '>' character

            // StringBuffer.indexOf(String) requires JDK1.4 or newer
            //int endOfFirstStartTag = charBuf.indexOf(">");
            int endOfFirstStartTag = 0;
            while ('>' != charBuf.charAt(endOfFirstStartTag)) {
                endOfFirstStartTag++;
            }
            charBuf.insert(endOfFirstStartTag, contextPrefixes.toString());
        }

        unknownPrefixesInXmlLiteral.clear();
    }

    private void appendNamespaceDecl(StringBuffer sb, String prefix,
            String namespace) throws SAXException {
        String attName = "xmlns";

        if (!"".equals(prefix)) {
            attName += ":" + prefix;
        }

        appendAttribute(sb, attName, namespace);
    }

    private void appendAttribute(StringBuffer sb, String name, String value) throws SAXException {
        sb.append(" ");
        sb.append(name);
        sb.append("=\"");

        char[] c = new char[value.length()];
        value.getChars(0, c.length, c, 0);
        escapeXml(c, 0, c.length, sb);
        os.reset();

        sb.append("\"");
    }

    private ElementInfo peekStack() {
        ElementInfo result = null;

        if (!elInfoStack.empty()) {
            result = (ElementInfo) elInfoStack.peek();
        }

        return result;
    }

    private void escapeXml(char[] c, int start, int length, StringBuffer sb) throws SAXException {
        try {
            th.characters(c, start, length);
            osw.flush();
            sb.append(os.toString());
        } catch (IOException e) {
            throw new SAXException("Error occurred escaping attribute text ", e);
        }
    }

    private class ElementInfo {
        private String qName;
        private String namespaceURI;
        private String localName;
        private Atts atts;

        private ElementInfo parent;
        private Map namespaceMap;

        private URI baseURI;
        private String xmlLang;

        ElementInfo(String newQName, String newNamespaceURI, String newLocalName) {
            this(null, newQName, newNamespaceURI, newLocalName);
        }

        ElementInfo(ElementInfo newParent, String newQName, String newNamespaceURI,
                String newLocalName) {
            parent = newParent;
            qName = newQName;
            namespaceURI = newNamespaceURI;
            localName = newLocalName;

            if (parent != null) {
                baseURI = parent.baseURI;
                xmlLang = parent.xmlLang;
            } else {
                baseURI = documentURI;
                xmlLang = "";
            }
        }

        public void setNamespaceMappings(Map namespaceMappings) {
            if (namespaceMappings.isEmpty()) {
                setNamespaceMap(null);
            } else {
                setNamespaceMap(new HashMap(namespaceMappings));
            }
        }

        public String getNamespace(String prefix) {
            String result = null;

            if (null != getNamespaceMap()) {
                result = (String) getNamespaceMap().get(prefix);
            }

            if (null == result && null != getParent()) {
                result = getParent().getNamespace(prefix);
            }

            return result;
        }

        public String getXmlLang() {
            return xmlLang;
        }

        public void setXmlLang(String xmlLang) {
            this.xmlLang = xmlLang;
        }

        public String getqName() {
            return qName;
        }

        public void setqName(String qName) {
            this.qName = qName;
        }

        public String getNamespaceURI() {
            return namespaceURI;
        }

        public void setNamespaceURI(String namespaceURI) {
            this.namespaceURI = namespaceURI;
        }

        public String getLocalName() {
            return localName;
        }

        public void setLocalName(String localName) {
            this.localName = localName;
        }

        public Atts getAtts() {
            return atts;
        }

        public void setAtts(Atts atts) {
            this.atts = atts;
        }

        public ElementInfo getParent() {
            return parent;
        }

        public void setParent(ElementInfo parent) {
            this.parent = parent;
        }

        public Map getNamespaceMap() {
            return namespaceMap;
        }

        public void setNamespaceMap(Map namespaceMap) {
            this.namespaceMap = namespaceMap;
        }

        public URI getBaseURI() {
            return baseURI;
        }

        public void setBaseURI(String uriString) {
            this.baseURI = baseURI.resolve(createBaseURI(uriString));
        }
    }
}
