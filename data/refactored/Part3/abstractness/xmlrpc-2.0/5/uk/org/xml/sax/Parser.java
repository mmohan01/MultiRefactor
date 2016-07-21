package uk.org.xml.sax;

public interface Parser extends org.xml.sax.Parser {

    protected
     void setDocumentHandler(DocumentHandler handler);
}
