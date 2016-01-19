package edu.atilim.acma.ws;

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.xmlrpc.XmlRpcException;
import org.apache.xmlrpc.metadata.XmlRpcSystemImpl;
import org.apache.xmlrpc.server.PropertyHandlerMapping;
import org.apache.xmlrpc.server.XmlRpcHandlerMapping;
import org.apache.xmlrpc.webserver.XmlRpcServlet;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;
import org.eclipse.jetty.servlet.FilterMapping;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.eclipse.jetty.servlets.MultiPartFilter;

import edu.atilim.acma.design.Design;
import edu.atilim.acma.design.io.ZIPDesignReader;

public class WebServiceEngine implements Runnable {
	private static WebServiceEngine instance;
	public static int port = 8081;
	
	public static WebServiceEngine getInstance() {
		if (instance == null) {
			try {
				instance = new WebServiceEngine();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}			
		}
		
		return instance;
	}
	
	Server webServer;
	
	private WebServiceEngine() throws Exception {
		initialize(port);
	}
	
	private void initialize(int port) throws Exception {
		System.out.println("Starting web server...");
		
		webServer = new Server(port);
		
		ServletContextHandler requestHandler = new ServletContextHandler();
		requestHandler.setContextPath("/");
		requestHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
		requestHandler.addServlet(new ServletHolder(new RequestServlet()), "/xmlrpc/*");
		
		ServletContextHandler uploadHandler = new ServletContextHandler();
		uploadHandler.setContextPath("/upload");
		uploadHandler.setClassLoader(Thread.currentThread().getContextClassLoader());
		uploadHandler.addFilter(MultiPartFilter.class, "/", FilterMapping.ALL);
		uploadHandler.addServlet(new ServletHolder(new DesignUploadServlet()), "/");
		
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setDirectoriesListed(false);
		resourceHandler.setResourceBase("./web");
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });
		
		HandlerList handlers = new HandlerList();
		handlers.addHandler(requestHandler);
		handlers.addHandler(uploadHandler);
		handlers.addHandler(resourceHandler);
		
		webServer.setHandler(handlers);
	}
	
	private static class RequestServlet extends XmlRpcServlet {
		private static final long serialVersionUID = 1L;

		@Override
		protected XmlRpcHandlerMapping newXmlRpcHandlerMapping() throws XmlRpcException {
			PropertyHandlerMapping phm = new PropertyHandlerMapping();
			phm.addHandler("acma", WebService.class);
			XmlRpcSystemImpl.addSystemHandler(phm);
			
			return phm;
		}
	}
	
	private static class DesignUploadServlet extends HttpServlet {
		private static final long serialVersionUID = 1L;
		
		@Override
		protected void doPost(HttpServletRequest req, HttpServletResponse resp)	throws ServletException, IOException {
			Object fo = req.getAttribute("design");
			
			if (fo != null && fo instanceof File) {
				File file = (File)fo;
				Design design = null;
				
				try {
					design = new ZIPDesignReader(file.getAbsolutePath()).read();
				} catch (Exception e) {
				}
				
				if (design == null) {
					resp.sendRedirect("/?error_upload");
				} else {
					Context context = Context.create(design);
					resp.sendRedirect("/?cid=" + context.getId().toString());
				}
			}
		}
	}

	@Override
	public void run() {
		ContextManager.initialize();
		new Thread(new edu.atilim.acma.Server(4501, 15)).start();
		new Thread(new edu.atilim.acma.Client("localhost", 4501)).start();
		
		try {
			webServer.start();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}
}
