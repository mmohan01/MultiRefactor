/*
 * HumanResourceLoader.java
 *
 * Created on 28. Mai 2003, 10:33
 */

package net.sourceforge.ganttproject.resource;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;

/**
 *
 * @author  barmeier
 */
public class HumanResourceLoader implements ContentHandler {
    
    private ResourceManager rm;
    
    /** Creates a new instance of HumanResourceLoader */
    public HumanResourceLoader(ResourceManager resourceManager) {
        rm=resourceManager;
    }
    
    public void characters(char[] ch, int start, int length) throws org.xml.sax.SAXException {
    }
    
    public void endDocument() throws org.xml.sax.SAXException {
    }
    
    public void endElement(String namespaceURI, String localName, String qName) throws org.xml.sax.SAXException {
    }
    
    public void endPrefixMapping(String prefix) throws org.xml.sax.SAXException {
    }
    
    public void ignorableWhitespace(char[] ch, int start, int length) throws org.xml.sax.SAXException {
    }
    
    public void processingInstruction(String target, String data) throws org.xml.sax.SAXException {
    }
    
    public void setDocumentLocator(org.xml.sax.Locator locator) {
    }
    
    public void skippedEntity(String name) throws org.xml.sax.SAXException {
    }
    
    public void startDocument() throws org.xml.sax.SAXException {
    }
    
    public void startElement(String namespaceURI, String localName, String qName, Attributes atts) throws org.xml.sax.SAXException {
        if (localName.equals("resource")) {
            HumanResource hr = (HumanResource)rm.create(atts.getValue("name"));
            hr.setMail(atts.getValue("contacts"));
	    hr.setPhone(atts.getValue("phone"));
            try {
//                hr.setFunction(Integer.parseInt(atts.getValue("function")));
            }
            catch (NumberFormatException e) {
                System.out.println ("ERROR in parsing XML File function id is not numeric: "+e.toString());
            }
            try {
                hr.setId(Integer.parseInt(atts.getValue("id")));
            }
            catch (NumberFormatException e) {
                System.out.println ("ERROR in parsing XML File id is not numeric: "+e.toString());
            }
        }
    }
    
    public void startPrefixMapping(String prefix, String uri) throws org.xml.sax.SAXException {
    }
    
}
