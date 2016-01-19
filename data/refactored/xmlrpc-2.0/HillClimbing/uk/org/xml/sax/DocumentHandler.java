package uk.org.xml.sax;

import java.io.Writer;
import org.xml.sax.SAXException;
import org.xml.sax.AttributeList;

public interface DocumentHandler extends org.xml.sax.DocumentHandler {
    Writer startDocument( Writer writer) throws SAXException;
    Writer startElement( String name, AttributeList attributes, Writer writer)
        throws SAXException;
}
