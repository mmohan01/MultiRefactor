package edu.atilim.acma;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import redstone.xmlrpc.XmlRpcClient;

public class WikiBot {
	private static ExecutorService executor;
	private static XmlRpcClient client;
	
	public static boolean isActive() {
		return client != null && executor != null;
	}
	
	public static void start() {
		try {
			client = new XmlRpcClient("http://www.a-cma.com/lib/exe/xmlrpc.php", false);
			client.setRequestProperty("Authorization", "Basic YWNtYWJvdDozNTQ2NTYyMA==");
			String version = (String)client.invoke("dokuwiki.getTitle", new Object[] { });
			System.out.printf("[WikiBot] Connected to wiki. Name: %s\n", version);
			executor = Executors.newSingleThreadExecutor();
		} catch (Exception e) {
			client = null;
			executor = null;
			
			System.out.println("[WikiBot] Could not start wikibot instance.");
		}
	}
	
	public static void pushRun(String taskName, String runName, File data) {
		if (isActive() && data.exists() && data.isFile()) {
			System.out.println("[WikiBot] Pushing result to wiki.");
			pushFile(data, String.format("run:%s", runName), "Auto generated run result.", false);
			appendText(String.format("  * **%s** run: [[run:%s]]\n", taskName, runName), "run_results", "Adding new result to the list");
		}
	}
	
	private static void appendText(final String text, final String page, final String summary) {
		if (isActive() && text != null && text.trim().length() > 0) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					try {
						client.invoke("dokuwiki.appendPage", new Object[] { page, text, new Object[] { summary, false } });
					} catch (Exception e) {}
				}
			});
		}
	}
	
	private static void pushFile(final File file, final String page, final String summary, final boolean nowiki) {
		if (isActive() && file.exists() && file.isFile()) {
			executor.execute(new Runnable() {
				@Override
				public void run() {
					BufferedReader br = null;
					try {
						StringBuilder sb = new StringBuilder();
						if (nowiki) sb.append("<nowiki>");
						
						br = new BufferedReader(new FileReader(file));
						String line = null;
						while ((line = br.readLine()) != null) {
							sb.append(line).append("\r\n");
						}
						br.close();
						
						if (nowiki) sb.append("</nowiki>");
						
						client.invoke("wiki.putPage", new Object[] { page, sb.toString(), new Object[] { summary, false } });
					} catch (Exception e) {
					} finally {
						try { br.close(); } catch (Exception e) {}
					}
				}
			});
		}
	}
}
