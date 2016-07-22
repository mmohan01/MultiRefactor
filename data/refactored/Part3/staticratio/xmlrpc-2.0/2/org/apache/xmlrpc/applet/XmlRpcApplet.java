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
import java.util.Hashtable;
import java.util.Vector;

import java.util.Date;


/**
 * An applet that provides basic XML-RPC client functionality.
 *
 * @version $Id: XmlRpcApplet.java,v 1.3 2005/04/22 10:25:58 hgomez Exp $
 */
public abstract class XmlRpcApplet extends Applet {
    public Object loaded = null;

    SimpleXmlRpcClient client;

    public void addStringArgToStruct(Hashtable struct, String key, String str)
     {
        struct.put(key, str);
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

    public void addStringArgToArray(Vector ary, String str)
     {
        ary.addElement(str);
    }
}
