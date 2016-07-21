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


package org.apache.xmlrpc.applet;

import java.applet.Applet;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Vector;

import java.util.Date;
import java.util.Hashtable;
import java.util.Vector;


/**
 * An applet that provides basic XML-RPC client functionality.
 *
 * @version $Id: XmlRpcApplet.java,v 1.3 2005/04/22 10:25:58 hgomez Exp $
 */
public class XmlRpcApplet extends Applet {

    SimpleXmlRpcClient client;
    public Object loaded = null;


    /**
     * Initialize the XML-RPC client, trying to get the port number from the
     * applet parameter tags. The default for port is 80. The client connects to
     * the server this applet came from.
     */
    public void initClient()
     {
        int port = 80;
        String p = getParameter("PORT");
        if (p != null)
         {
            try
             {
                port = Integer.parseInt(p);
            }
             catch (NumberFormatException nfx)
             {
                System.out.println("Error parsing port: " + nfx);
            }
        }
        initClient(port);
    }

    /**
     * Initialize the XML-RPC client with the specified port and the server this
     * applet came from.
     */
    public void initClient(int port)
     {
        String uri = getParameter("URI");
        if (uri == null)
         {
            uri = "/RPC2";
        }
         else if (!uri.startsWith("/"))
         {
            uri = "/" + uri;
        }
        initClient(port, uri);
    }

    /**
     * Initialize the XML-RPC client with the specified port and request path
     * and the server this applet came from.
     */
    public void initClient(int port, String uri)
     {
        String host = getCodeBase().getHost();
        try
         {
            URL url = new URL("http://" + host + ":" + port + uri);
            System.out.println("XML-RPC URL: " + url);
            client = new SimpleXmlRpcClient(url);
        }
         catch (MalformedURLException unlikely)
         {
            System.out.println("Error constructing XML-RPC client for "
                    + host
                    + ":"
                    + port
                    + ": "
                    + unlikely);
        }
    }

    private String errorMessage;
    private Vector arguments;

    /**
     *
     */
    public void init()
     {
        initClient();
        arguments = new Vector();
        loaded = Boolean.TRUE;
        System.out.println("JSXmlRpcApplet initialized");
    }

    // add ints (primitve != object) to structs, vectors
    public void addIntArg(int value)
     {
        arguments.addElement(new Integer(value));
    }

    public void addIntArgToStruct(Hashtable struct, String key, int value)
     {
        struct.put(key, new Integer(value));
    }

    public void addIntArgToArray(Vector ary, int value)
     {
        ary.addElement(new Integer(value));
    }

    // add floats/doubles to structs, vectors
    public void addDoubleArg(float value)
     {
        arguments.addElement(new Double(value));
    }

    /**
     * Calls the XML-RPC server with the specified methodname and argument list.
     */
    public Object execute(String methodName, Vector arguments)
            throws XmlRpcException, IOException
     {
        if (client == null)
         {
            initClient();
        }
        Object returnValue = null;
        return returnValue = client.execute(methodName, arguments);
    }

    public void addDoubleArgToStruct(Hashtable struct, String key, float value)
     {
        struct.put(key, new Double(value));
    }

    public void addDoubleArgToArray(Vector ary, float value)
     {
        ary.addElement(new Double(value));
    }

    public void addDoubleArg(double value)
     {
        arguments.addElement(new Double(value));
    }

    public void addDoubleArgToStruct(Hashtable struct, String key, double value)
     {
        struct.put(key, new Double(value));
    }

    public void addDoubleArgToArray(Vector ary, double value)
     {
        ary.addElement(new Double(value));
    }

    // add bools to structs, vectors
    public void addBooleanArg(boolean value)
     {
        arguments.addElement(new Boolean(value));
    }

    public void addBooleanArgToStruct(Hashtable struct, String key,
            boolean value)
     {
        struct.put(key, new Boolean(value));
    }

    public void addBooleanArgToArray(Vector ary, boolean value)
     {
        ary.addElement(new Boolean(value));
    }

    // add Dates to structs, vectors Date argument in SystemTimeMillis (seems to be the way)
    public void addDateArg(long dateNo)
     {
        arguments.addElement(new Date(dateNo));
    }

    public void addDateArgToStruct(Hashtable struct, String key, long dateNo)
     {
        struct.put(key, new Date(dateNo));
    }

    public void addDateArgToArray(Vector ary, long dateNo)
     {
        ary.addElement(new Date(dateNo));
    }

    // add String arguments
    public void addStringArg(String str)
     {
        arguments.addElement(str);
    }

    public void addStringArgToStruct(Hashtable struct, String key, String str)
     {
        struct.put(key, str);
    }

    public void addStringArgToArray(Vector ary, String str)
     {
        ary.addElement(str);
    }

    // add Array arguments
    public Vector addArrayArg()
     {
        Vector v = new Vector();
        arguments.addElement(v);
        return v;
    }

    public Vector addArrayArgToStruct(Hashtable struct, String key)
     {
        Vector v = new Vector();
        struct.put(key, v);
        return v;
    }

    public Vector addArrayArgToArray(Vector ary)
     {
        Vector v = new Vector();
        ary.addElement(v);
        return v;
    }

    // add Struct arguments
    public Hashtable addStructArg()
     {
        Hashtable ht = new Hashtable();
        arguments.addElement(ht);
        return ht;
    }

    public Hashtable addStructArgToStruct(Hashtable struct, String key)
     {
        Hashtable ht = new Hashtable();
        struct.put(key, ht);
        return ht;
    }

    public Hashtable addStructArgToArray(Vector ary)
     {
        Hashtable ht = new Hashtable();
        ary.addElement(ht);
        return ht;
    }

    // get the errorMessage, null if none
    public String getErrorMessage()
     {
        return errorMessage;
    }

    public void reset()
     {
        arguments = new Vector();
    }

    public Object execute(String methodName)
     {
        // XmlRpcSupport.setDebug (true);
        errorMessage = null;
        showStatus("Connecting to Server...");
        Object returnValue = null;
        try
         {
            returnValue = execute(methodName, arguments);
        }
         catch (Exception e)
         {
            errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage == "")
             {
                errorMessage = e.toString();
            }
        // reset argument array for reuse
        }
        arguments = new Vector();

        showStatus("");
        return returnValue;
    }
}
