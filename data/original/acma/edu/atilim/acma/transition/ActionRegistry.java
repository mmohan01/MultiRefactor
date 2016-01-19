package edu.atilim.acma.transition;

import java.io.File;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import edu.atilim.acma.transition.actions.ActionChecker;
import edu.atilim.acma.util.Log;

public final class ActionRegistry {
	static {
		initialize();
	}
	
	private static HashSet<Entry> entries;
	
	public static Set<Entry> entries() {
		return Collections.unmodifiableSet(entries);
	}
	
	private static void initialize() {
		entries = new HashSet<Entry>();
		
		try {
			Log.config("Loading actions.xml for action configuration.");
			
			DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
	        DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	        Document doc = docBuilder.parse(new File("./data/actions.xml"));
	        
	        NodeList actions = doc.getElementsByTagName("action");
	        for (int i = 0 ; i < actions.getLength(); i++) {
	        	Node action = actions.item(i);

	        	String className = action.getAttributes().getNamedItem("class").getTextContent();
	        	Class<?> actionClass = Class.forName("edu.atilim.acma.transition.actions." + className);
	        	
	        	for (Class<?> innerClass : actionClass.getClasses()) {
	        		if (ActionChecker.class.isAssignableFrom(innerClass)) {
	        			ActionChecker checker = (ActionChecker)innerClass.newInstance();
	        			entries.add(new Entry(className, checker));
	        		}
	        	}
	        }
	        
	        Log.config("Found %d action configurers. Action manager is ready.", entries.size());
		} catch (Exception e) {
			System.out.println("Could not initialize transition manager. Details:");
			e.printStackTrace();
		}
	}
	
	public static class Entry {
		private String name;
		private ActionChecker checker;
		
		public String getName() {
			return name;
		}
		
		ActionChecker getChecker() {
			return checker;
		}

		private Entry(String name, ActionChecker checker) {
			this.name = name;
			this.checker = checker;
		}
		
		@Override
		public int hashCode() {
			return name.hashCode();
		}
		
		@Override
		public boolean equals(Object obj) {
			return obj != null && obj instanceof Entry && ((Entry)obj).name.equals(name);
		}
	}
}
