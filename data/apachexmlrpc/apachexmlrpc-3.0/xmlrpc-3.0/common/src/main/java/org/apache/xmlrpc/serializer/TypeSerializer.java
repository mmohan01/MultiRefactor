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
package org.apache.xmlrpc.serializer;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


/** A <code>TypeSerializer</code> is able to write a parameter
 * or result object to the XML RPC request or response.
 */
public interface TypeSerializer {
	/** Writes the object <code>pObject</code> to the SAX handler
	 * <code>pHandler</code>.
	 * @param pHandler The destination handler.
	 * @param pObject The object being written.
	 * @throws SAXException Writing the object failed.
	 */
	void write(ContentHandler pHandler, Object pObject) throws SAXException;
}
