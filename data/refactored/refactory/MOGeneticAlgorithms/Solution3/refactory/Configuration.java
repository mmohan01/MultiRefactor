package refactory;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import refactorings.Refactoring;

public abstract class Configuration {
    private ArrayList<Triple<String, Boolean, Float>> configuration;
    private List<Refactoring> refactorings;

    public Configuration(ArrayList<Triple<String, Boolean, Float>> configuration, List<Refactoring> refactorings)
     {
        this.configuration = configuration;
        this.refactorings = refactorings;
    }

    public Configuration(List<String> name, List<Boolean> maximise, List<Float> weight, List<Refactoring> refactorings)
     {
        this.refactorings = refactorings;
        this.configuration = new ArrayList<Triple<String, Boolean, Float>>();

        for (int i = 0; i < name.size(); i++)
         {
            if (i >= maximise.size())
                maximise.add(i, true);
            if (i >= weight.size())
                weight.add(i, 1.0f);

            Triple<String, Boolean, Float> element = new Triple<String, Boolean, Float>(name.get(i), maximise.get(i), weight.get(i));
            this.configuration.add(element);
        }
    }

    public Configuration(String[] name, boolean[] maximise, float[] weight, List<Refactoring> refactorings)
     {
        this.refactorings = refactorings;
        this.configuration = new ArrayList<Triple<String, Boolean, Float>>();

        for (int i = 0; i < name.length; i++)
         {
            if (i >= maximise.length)
                maximise[i - 1] = true;
            if (i >= weight.length)
                weight[i - 1] = 1;

            Triple<String, Boolean, Float> element = new Triple<String, Boolean, Float>(name[i], maximise[i], weight[i]);
            this.configuration.add(element);
        }
    }

    public Configuration(String pathway, List<Refactoring> refactorings)
     {
        this.refactorings = refactorings;
        this.configuration = new ArrayList<Triple<String, Boolean, Float>>();
        File file = new File(pathway);

        if (file.exists())
         {
            if (file.getName().endsWith(".txt"))
             {
                try
                 {
                    BufferedReader br = new BufferedReader(new FileReader(file));
                    String line;

                    while ((line = br.readLine()) != null)
                     {
                        // \\s+ means any number of white spaces between tokens
                        String[] tokens = line.split("\\s+");
                        Triple<String, Boolean, Float> element = new Triple<String, Boolean, Float>(tokens[0], tokens[1].equals("true"), Float.parseFloat(tokens[2]));
                        this.configuration.add(element);
                    }

                    br.close();
                }
                 catch (IOException e)
                 {
                    System.out.println("\nEXCEPTION: Cannot read metric configuration from text file.");
                    System.exit(1);
                }
            }
             else if (file.getName().endsWith(".xml"))
             {
                DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();

                try
                 {
                    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
                    Document doc = docBuilder.parse(file);
                    NodeList nodes = doc.getElementsByTagName("metric");

                    for (int i = 0; i < nodes.getLength(); i++)
                     {
                        String name = nodes.item(i).getAttributes().getNamedItem("name").getTextContent();
                        boolean maximise = nodes.item(i).getAttributes().getNamedItem("maximise").getTextContent().equals("true");
                        float weight = Float.parseFloat(nodes.item(i).getAttributes().getNamedItem("weight").getTextContent());
                        Triple<String, Boolean, Float> element = new Triple<String, Boolean, Float>(name, maximise, weight);
                        this.configuration.add(element);
                    }
                }
                 catch (ParserConfigurationException | SAXException | IOException e)
                 {
                    System.out.println("\nEXCEPTION: Cannot read metric configuration from xml file.");
                    System.exit(1);
                }
            }
        }
    }

    public  ArrayList<Triple<String, Boolean, Float>> getConfiguration()
     {
        return this.configuration;
    }

    public boolean setConfigurationElement(String name, boolean maximise, float weight)
     {
        boolean present = false;

        for (Triple<String, Boolean, Float> element: this.configuration)
         {
            if (element.getFirst().equals(name))
             {
                element.setSecond(maximise);
                element.setThird(weight);
                present = true;
            }
        }

        if (!present)
            this.configuration.add(new Triple<String, Boolean, Float>(name, maximise, weight));

        return !present;
    }

    public List<Refactoring> getRefactorings()
     {
        return this.refactorings;
    }

    public boolean setRefactorings(Refactoring r)
     {
        if (!this.refactorings.contains(r))
         {
            this.refactorings.add(r);
            return true;
        }
         else
            return false;
    }
}
