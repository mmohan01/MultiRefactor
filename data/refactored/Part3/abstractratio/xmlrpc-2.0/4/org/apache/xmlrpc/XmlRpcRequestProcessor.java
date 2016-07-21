/*
 * Copyright 1999,2005 The Apache Software Foundation.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


package org.apache.xmlrpc;

import java.io.InputStream;
import java.util.Vector;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Process an InputStream and produce an XmlRpcServerRequest.  This class
 * is NOT thread safe.
 *
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author Daniel L. Rall
 * @since 1.2
 */
public class XmlRpcRequestProcessor extends XmlRpc {
    static final int FATAL = 2;
    private Vector requestParams;

    /**
     * Method called by SAX driver.
     */
    public void characters(char ch[], int start, int length)
            throws SAXException
     {
        if (readCdata)
         {
            cdata.append(ch, start, length);
        }
    }

    /**
     *
     * @param e
     * @throws SAXException
     */
    public void error(SAXParseException e) throws SAXException
     {
        System.err.println("Error parsing XML: " + e);
        errorLevel = RECOVERABLE;
        errorMsg = e.toString();
    }

    /**
     * Set the encoding of the XML.
     *
     * @param enc The Java name of the encoding.
     */
    public static void setEncoding(String enc)
     {
        encoding = enc;
    }

    /**
     * Method called by SAX driver.
     */
    public void endElement(String name) throws SAXException
     {

        if (debug)
         {
            System.out.println("endElement: " + name);
        }

        // finalize character data, if appropriate
        if (currentValue != null && readCdata)
         {
            currentValue.characterData(cdata.toString());
            cdata.setLength(0);
            readCdata = false;
        }

        if ("value".equals(name))
         {
            // Only handle top level objects or objects contained in
            // arrays here.  For objects contained in structs, wait
            // for </member> (see code below).
            int depth = values.size();
            if (depth < 2 || values.elementAt(depth - 2).hashCode() != STRUCT)
             {
                Value v = currentValue;
                values.pop();
                if (depth < 2)
                 {
                    // This is a top-level object
                    objectParsed(v.value);
                    currentValue = null;
                }
                 else
                 {
                    // Add object to sub-array; if current container
                    // is a struct, add later (at </member>).
                    currentValue = (Value) values.peek();
                    currentValue.endElement(v);
                }
            }
        }

        // Handle objects contained in structs.
        if ("member".equals(name))
         {
            Value v = currentValue;
            values.pop();
            currentValue = (Value) values.peek();
            currentValue.endElement(v);
        }

         else if ("methodName".equals(name))
         {
            methodName = cdata.toString();
            cdata.setLength(0);
            readCdata = false;
        }
    }

    /**
     *
     * @param e
     * @throws SAXException
     */
    public void fatalError(SAXParseException e) throws SAXException
     {
        System.err.println("Fatal error parsing XML: " + e);
        errorLevel = FATAL;
        errorMsg = e.toString();
    }

    /**
     * Creates a new instance.
     */
    public XmlRpcRequestProcessor()
     {
        requestParams = new Vector();
    }

    /**
     * Return the default input encoding. This may be null.
     * This is always a Java encoding name, it is not transformed.
     *
     * @return the Java encoding name to use, if set, otherwise null.
     * @see #getInputEncoding()
     */
    public static String getDefaultInputEncoding()
     {
        return defaultInputEncoding;
    }

    /**
     * Decode a request from an InputStream to the internal XmlRpcRequest
     * implementation. This method must read data from the specified stream and
     * return an XmlRpcRequest object, or throw an exception.
     *
     * @param is the stream to read the request from.
     * @returns XMLRpcRequest the request.
     * @throws ParseFailed if unable to parse the request.
     */
    public XmlRpcServerRequest decodeRequest(InputStream is)
     {
        long now = 0;

        if (XmlRpc.debug)
         {
            now = System.currentTimeMillis();
        }
        try
         {
            try
             {
                parse(is);
            }
             catch (Exception e)
             {
                throw new ParseFailed(e);
            }
            if (XmlRpc.debug)
             {
                System.out.println("XML-RPC method name: " + methodName);
                System.out.println("Request parameters: " + requestParams);
            }
            // check for errors from the XML parser
            if (
            errorLevel > NONE)
             {
                throw new ParseFailed(errorMsg);
            }

            return new XmlRpcRequest(methodName, (Vector) requestParams.clone());
        }
         finally
         {
            requestParams.removeAllElements();
            if (XmlRpc.debug)
             {
                System.out.println("Spent " + (System.currentTimeMillis() - now) + " millis decoding request");
            }
        }
    }

    /** Set the default input encoding of the XML.
     * This is used only if set.
     *
     * @param enc The Java name of the encoding.
     * @see #setInputEncoding(String)
     */
    public static void setDefaultInputEncoding(String enc)
     {
        defaultInputEncoding = enc;
    }

    /**
     * Called when an object to be added to the argument list has been
     * parsed.
     *
     * @param what The parameter parsed from the request.
     */
    protected void objectParsed(Object what)
     {
        requestParams.addElement(what);
    }
}
