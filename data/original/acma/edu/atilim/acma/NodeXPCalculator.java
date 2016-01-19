package edu.atilim.acma;

import java.io.File;
import java.util.ArrayList;

public class NodeXPCalculator implements Runnable {
	@Override
	public void run() {
		ArrayList<RunResult> results = new ArrayList<RunResult>();
		
		System.out.println("Please enter the results folder location: ");
		String root = Console.readLine();
		
		if (root.length() == 0) {
			root = ".//results";
		}
		
		File rd = new File(root);
		
		try {
			if (rd.exists() && rd.isDirectory()) {
				for (File inner : rd.listFiles()) {
					if (inner.isDirectory()) {
						for (File result : inner.listFiles()) {
							if (!result.isFile() || !result.getName().endsWith(".txt")) continue;
							
							results.add(RunResult.readFrom(result.getAbsolutePath()));
						}
					}
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		
		StringBuilder sbfull = new StringBuilder();
		StringBuilder sbdata = new StringBuilder();
		
		for (int i = 0; i < results.size(); i++) {
			RunResult res = results.get(i);
			
			sbfull.append(res.getBenchmark()).append(';');
			sbfull.append(res.getAttribute("Algorithm")).append(';');
			
			sbfull.append(res.getAttribute("NodeExpansion")).append(';');
			sbdata.append(res.getAttribute("NodeExpansion")).append(',');
			
			if (res.getAttribute("Algorithm").contains("Hill")) {
				sbfull.append(res.getFinalDesign().getAppliedActions());
				sbdata.append(res.getFinalDesign().getAppliedActions());
			} else {
				sbfull.append(res.getAttribute("Iterations"));
				sbdata.append(res.getAttribute("Iterations"));
			}
			
			sbfull.append("\r\n");
			sbdata.append("\r\n");
		}
		
		System.out.println(sbfull.toString());
		System.out.println(sbdata.toString());
	}
}
