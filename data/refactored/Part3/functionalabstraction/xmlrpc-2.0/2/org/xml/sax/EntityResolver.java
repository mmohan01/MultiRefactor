// SAX entity resolver.
// No warranty; no copyright -- use this as you will.
// $Id: EntityResolver.java,v 1.1 2001/11/06 18:33:40 hannes Exp $

package org.xml.sax;

import java.io.IOException;


/**
  * Basic interface for resolving entities.
  *
  * <p>If a SAX application needs to implement customized handling
  * for external entities, it must implement this interface and
  * register an instance with the SAX parser using the parser's
  * setEntityResolver method.</p>
  *
  * <p>The parser will then allow the application to intercept any
  * external entities (including the external DTD subset and external
  * parameter entities, if any) before including them.</p>
  *
  * <p>Many SAX applications will not need to implement this interface,
  * but it will be especially useful for applications that build
  * XML documents from databases or other specialised input sources,
  * or for applications that use URI types other than URLs.</p>
  *
  * <p>The following resolver would provide the application
  * with a special character stream for the entity with the system
  * identifier "http://www.myhost.com/today":</p>
  *
  * <pre>
  * import org.xml.sax.EntityResolver;
  * import org.xml.sax.InputSource;
  *
  * public class MyResolver implements EntityResolver {
  *   public InputSource resolveEntity (String publicId, String systemId)
  *   {
  *     if (systemId.equals("http://www.myhost.com/today")) {
  *              // return a special input source
  *       MyReader reader = new MyReader();
  *       return new InputSource(reader);
  *     } else {
  *              // use the default behaviour
  *       return null;
  *     }
  *   }
  * }
  * </pre>
  *
  * <p>The application can also use this interface to redirect system
  * identifiers to local URIs or to look up replacements in a catalog
  * (possibly by using the public identifier).</p>
  *
  * <p>The HandlerBase class implements the default behaviour for
  * this interface, which is simply always to return null (to request
  * that the parser use the default system identifier).</p>
  *
  * @author David Megginson (ak117@freenet.carleton.ca)
  * @version 1.0
  * @see org.xml.sax.Parser#setEntityResolver
  * @see org.xml.sax.InputSource
  * @see org.xml.sax.HandlerBase 
  */
public interface EntityResolver {
}
