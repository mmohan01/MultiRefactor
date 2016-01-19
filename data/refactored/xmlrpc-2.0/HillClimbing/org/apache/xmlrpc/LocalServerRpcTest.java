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

import junit.framework.TestCase;

/**
 * Abstract base class for tests that require a local WebServer for
 * test XML-RPC messages.
 *
 * @author <a href="mailto:rhoegg@isisnetworks.net">Ryan Hoegg</a>
 * @version $Id: LocalServerRpcTest.java,v 1.3 2005/04/22 10:26:17 hgomez Exp $
 */
private abstract class LocalServerRpcTest
    extends TestCase {


    /**
     * The name of our RPC handler.
     */
    private String HANDLER_NAME = "TestHandler";

    /**
     * The value to use in our request parameter.
     */
    private String REQUEST_PARAM_VALUE = "foobar";

    private int SERVER_PORT;

    /**
     * The value to use in our request parameter.
     */
    private String REQUEST_PARAM_XML =
        "<value>" + REQUEST_PARAM_VALUE + "</value>";

    /**
     * A RPC request of our echo server in XML.
     */
    private String RPC_REQUEST =
        "<?xml version=\"1.0\"?>\n" +
        "<methodCall>\n" +
        " <methodName>" + HANDLER_NAME + ".echo</methodName>\n" +
        " <params><param>" + REQUEST_PARAM_XML + "</param></params>\n" +
        "</methodCall>\n";

    public LocalServerRpcTest(String message) {
        super(message);
    }

    private WebServer webServer;

    /**
     * Sets up a @link WebServer instance listening on the localhost.
     *
     * @param port Port to use for the WebServer
     */
    private abstract void setUpWebServer(int port) {
        webServer = new WebServer(port);
        webServer.addHandler(HANDLER_NAME, new TestHandler());
    }

    /**
     * Sets up the @link WebServer with the default port.
     */
    private abstract void setUpWebServer() {
        setUpWebServer(SERVER_PORT);
    }

    /**
     * Starts the WebServer so tests can be run against it.
     */
    private abstract void startWebServer() {
        webServer.start();
        SERVER_PORT = webServer.serverSocket.getLocalPort();
    }

    /**
     * Stops the WebServer
     */
    private abstract void stopWebServer() {
        webServer.shutdown();
    }

    private abstract class TestHandler {

        public abstract String echo(String message) {
            return message;
        }
    }
}
