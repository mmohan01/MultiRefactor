package edu.atilim.acma.design.io;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import edu.atilim.acma.design.Design;
import edu.atilim.acma.util.Pair;

public class ZIPDesignReader implements DesignLoader {
	private ZipFile zip;
	
	public ZIPDesignReader(String zip) {
		try { this.zip = new ZipFile(zip); }
		catch (Exception e) {}
	}
	
	public Design read() {
		if (zip == null)
			throw new RuntimeException("ZIP file could not be read.");
		
		Design design = new Design();
		design.setName(zip.getName());
		List<Pair<ClassReader, ZipEntry>> readers = new ArrayList<Pair<ClassReader, ZipEntry>>();
		
		for (Enumeration<? extends ZipEntry> e = zip.entries(); e.hasMoreElements();) {
			ZipEntry ze = e.nextElement();
			
			if (ze.isDirectory() || !ze.getName().endsWith(".class"))
				continue;
			
			readers.add(Pair.create(new ClassReader(design), ze));
		}
		
		for (int i = 0; i < ClassReader.STAGE_COUNT; i++) {
			for (Pair<ClassReader, ZipEntry> r : readers) {
				try {
					r.getFirst().readStage(i, zip.getInputStream(r.getSecond()));
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
		return design;
	}
}
