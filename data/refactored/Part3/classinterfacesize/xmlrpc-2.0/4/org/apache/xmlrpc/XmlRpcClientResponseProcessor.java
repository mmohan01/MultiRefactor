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

import java.util.Hashtable;
import java.io.InputStream;

import org.xml.sax.AttributeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

/**
 * Process an XML-RPC server response from a byte array or an
 * InputStream into an Object. Optionally throw the result object
 * if it is an exception.
 *
 * @author <a href="mailto:hannes@apache.org">Hannes Wallnoefer</a>
 * @author <a href="mailto:andrew@kungfoocoder.org">Andrew Evers</a>
 * @since 2.0
 */
public class XmlRpcClientResponseProcessor extends XmlRpc {
    static final int FATAL = 2;
    /** The result of the XML-RPC operation. Possibly an XmlRpcException */
    protected Object result;

    /** Set to true if a fault occured on the server. */
    protected boolean fault;

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
     * Creates a new instance.
     */
    public XmlRpcClientResponseProcessor()
     {
    }

    /**
     * Decode an XML-RPC response from the specified InputStream.
     *
     * @param is The stream from which to read the response.
     * @return The response, which will be a XmlRpcException if an
     * error occured.
     * @exception XmlRpcClientException
     */
    public Object decodeResponse(InputStream is)
        throws XmlRpcClientException
     {
        result = null;
        fault = false;
        try
         {
            parse(is);
            if (fault)
             {
                return decodeException(result);
            }
             else
             {
                return result;
            }
        }
         catch (Exception x)
         {
            throw new XmlRpcClientException("Error decoding XML-RPC response", x);
        }
    }

    /**
     * Decode an exception from the result returned from the remote server.
     * This method both returns and throws an XmlRpcException. If it returns an
     * XmlRpcException then that is the exception thrown on the remote side. If
     * it throws an exception then an exception occured locally when decoding
     * the response
     *
     * @param result The response from the remote XML-RPC server.
     * @return A XmlRpcException describing the error which occurred.
     * @exception XmlRpcClientException if the result could not be processed.
     * @return XmlRpcException the processed response from the server.
     */
    protected XmlRpcException decodeException(Object result)
        throws XmlRpcClientException
     {
        Hashtable exceptionData;

        try
         {
            exceptionData = (Hashtable) result;
            return new org.apache.xmlrpc.applet.XmlRpcException(
                Integer.parseInt(exceptionData.get("faultCode").toString()),
                (String) exceptionData.get("faultString"));
        }
         catch (Exception x)
         {
            throw new XmlRpcClientException("Error decoding XML-RPC exception response", x);
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

    public void objectParsed(Object what)
     {
        result = what;
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
     * Overrides method in XmlRpc to handle fault repsonses.
     */
    public void startElement(String name, AttributeList atts)
            throws SAXException
     {
        if ("fault".equals(name))
         {
            fault = true;
        }
         else
         {
            super.startElement(name, atts);
        }
    }

    /**
     * Called by the worker management framework to see if this worker can be
     * re-used. Must attempt to clean up any state, and return true if it can
     * be re-used.
     *
     * @return boolean true if this worker has been cleaned up and may be re-used.
     */
    protected boolean canReUse()
     {
        result = null;
        fault = false;
        return true;
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
}
