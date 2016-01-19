package edu.atilim.acma.design.io;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

import edu.atilim.acma.design.Design;
import edu.atilim.acma.util.Pair;

public abstract class DesignReader implements DesignLoader {
    private File classPath;

    public DesignReader(String classPath) {
        this.classPath = new File(classPath);
    }

    public Design read() {
        if (!classPath.exists() || !classPath.isDirectory())
            throw new RuntimeException("Class path for reading design does not exist.");

        Design design = new Design();
        design.setName(classPath.getName());
        List<File> files = findAllClassFiles();
        List<Pair<ClassReader, File>> readers = new ArrayList<Pair<ClassReader, File>>();

        for (File f: files) {
            ClassReader reader = new ClassReader(design);
            readers.add(Pair.create(reader, f));
        }

        for (int i = 0; i < ClassReader.STAGE_COUNT; i++) {
            for (Pair<ClassReader, File> r: readers) {
                try {
                    r.getFirst().readStage(i, new FileInputStream(r.getSecond()));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }
        }

        return design;
    }

    private List<File> findAllClassFiles() {
        Stack<File> dirs = new Stack<File>();
        ArrayList<File> files = new ArrayList<File>();

        dirs.push(classPath);

        while (dirs.size() > 0) {
            File dir = dirs.pop();
            File[] subfiles = dir.listFiles();

            for (File f: subfiles) {
                if (f.isDirectory())
                    dirs.push(f);
                 else if (f.getName().endsWith(".class")) {
                    files.add(f);
                }
            }
        }

        return files;
    }
}
