// SAX DTD handler.
// No warranty; no copyright -- use this as you will.
// $Id: DTDHandler.java,v 1.1 2001/11/06 18:33:40 hannes Exp $

package org.xml.sax;

/**
  * Receive notification of basic DTD-related events.
  *
  * <p>If a SAX application needs information about notations and
  * unparsed entities, then the application implements this 
  * interface and registers an instance with the SAX parser using 
  * the parser's setDTDHandler method.  The parser uses the 
  * instance to report notation and unparsed entity declarations to 
  * the application.</p>
  *
  * <p>The SAX parser may report these events in any order, regardless
  * of the order in which the notations and unparsed entities were
  * declared; however, all DTD events must be reported after the
  * document handler's startDocument event, and before the first
  * startElement event.</p>
  *
  * <p>It is up to the application to store the information for 
  * future use (perhaps in a hash table or object tree).
  * If the application encounters attributes of type "NOTATION",
  * "ENTITY", or "ENTITIES", it can use the information that it
  * obtained through this interface to find the entity and/or
  * notation corresponding with the attribute value.</p>
  *
  * <p>The HandlerBase class provides a default implementation
  * of this interface, which simply ignores the events.</p>
  *
  * @author David Megginson (ak117@freenet.carleton.ca)
  * @version 1.0
  * @see org.xml.sax.Parser#setDTDHandler
  * @see org.xml.sax.HandlerBase 
  */
public interface DTDHandler {
    public abstract void unparsedEntityDecl(String name,
                       String publicId,
                       String systemId,
                       String notationName)
     throws SAXException;
}
