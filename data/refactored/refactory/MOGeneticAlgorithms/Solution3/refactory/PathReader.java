package refactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Stack;

public class PathReader {
    private File filePath;

    public PathReader(String filePath)
     {
        this.filePath = new File(filePath);
    }

    // Returns an array of file paths representing the project.
    public String[] read()
     {

        if (!this.filePath.exists() || !this.filePath.isDirectory())
            throw new RuntimeException("Path to files does not exist.");

        List<File> files = findAllSourceFiles(this.filePath);
        String[] fileList = new String[files.size()];

        for (int i = 0; i < files.size(); i++)
            fileList[i] = files.get(i).getAbsolutePath();

        return fileList;
    }

    // Extracts only the java files from the input parameter.
    private List<File> findAllSourceFiles(File pathway)
     {
        Stack<File> dirs = new Stack<File>();
        ArrayList<File> files = new ArrayList<File>();
        dirs.push(pathway);

        while (dirs.size() > 0)
         {
            File dir = dirs.pop();
            File[] subfiles = dir.listFiles();

            for (File f: subfiles)
             {
                if (f.isDirectory())
                    dirs.push(f);
                 else if (f.getName().endsWith(".java"))
                    files.add(f);
            }
        }

        return files;
    }

    public String createOutputDirectory(String name)
     {
        String output = "./data/refactored/" + this.filePath.getName() + "/" + name;
        File dir = new File(output);
        if (!dir.exists())
            dir.mkdirs();

        return output;
    }
}
