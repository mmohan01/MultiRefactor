package multirefactor;

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

import recoder.CrossReferenceServiceConfiguration;
import recoder.ParserException;
import recoder.io.PropertyNames;
import recoder.java.CompilationUnit;
import refactorings.Refactoring;
import refactorings.field.*;
import refactorings.method.*;
import refactorings.type.*;
import tasks.Tasks;

public class Configuration 
{
	private ArrayList<MetricSpecification> configuration;
	private ArrayList<Refactoring> refactorings;
	private ArrayList<String> priorityClasses;
	private ArrayList<String> nonPriorityClasses;
	private ArrayList<List<CompilationUnit>> previousUnits;
	
	public Configuration(ArrayList<MetricSpecification> configuration, ArrayList<Refactoring> refactorings)
	{
		this.configuration = configuration;
		this.refactorings = refactorings;
	}
	
	public Configuration(ArrayList<String> name, ArrayList<Boolean> maximise, ArrayList<Float> weight, ArrayList<Refactoring> refactorings)
	{
		this.refactorings = refactorings;
		this.configuration = new ArrayList<MetricSpecification>();
		
		for (int i = 0; i < name.size(); i++)
		{
			if (i >= maximise.size())
				maximise.add(i, true);
			if (i >= weight.size())
				weight.add(i, 1.0f);
			
			MetricSpecification element = new MetricSpecification(name.get(i), maximise.get(i), weight.get(i));
			this.configuration.add(element);
		}
	}
	
	public Configuration(String[] name, boolean[] maximise, float[] weight, ArrayList<Refactoring> refactorings)
	{
		this.refactorings = refactorings;
		this.configuration = new ArrayList<MetricSpecification>();
		
		for (int i = 0; i < name.length; i++)
		{
			if (i >= maximise.length)
				maximise[i-1] = true;
			if (i >= weight.length)
				weight[i-1] = 1;
			
			MetricSpecification element = new MetricSpecification(name[i], maximise[i], weight[i]);
			this.configuration.add(element);
		}
	}
	
	public Configuration(String pathway, ArrayList<Refactoring> refactorings)
	{
		this.refactorings = refactorings;
		this.configuration = new ArrayList<MetricSpecification>();
		File file = new File(pathway);
		
		if (file.exists())
		{
			if (file.getName().endsWith(".txt"))
			{
				try 
				{
					BufferedReader br = new BufferedReader(new FileReader(file));
					String line;
					boolean executeWhile = true;
					
					switch (br.readLine()) 
					{
					case "PRIORITY":
						readPriority(br);
						executeWhile = false;
						break;
					case "ELEMENT RECENTNESS":
						readElementRecentness(br);
						executeWhile = false;
						break;
					}
					
					br = new BufferedReader(new FileReader(file));
					
					while (((line = br.readLine()) != null) && executeWhile)
					{
						// \\s+ means any number of white spaces between tokens
						String [] tokens = line.split("\\s+");
						MetricSpecification element = new MetricSpecification(tokens[0], tokens[1].equals("true"), Float.parseFloat(tokens[2]));
						this.configuration.add(element);
					}

					br.close();
				} 
				catch (IOException e) 
				{
					System.out.println("\r\nEXCEPTION: Cannot read metric configuration from text file.");
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

					for (int i = 0 ; i < nodes.getLength(); i++) 
					{
						String name = nodes.item(i).getAttributes().getNamedItem("name").getTextContent();
						boolean maximise = nodes.item(i).getAttributes().getNamedItem("maximise").getTextContent().equals("true");
						float weight = Float.parseFloat(nodes.item(i).getAttributes().getNamedItem("weight").getTextContent());
						MetricSpecification element = new MetricSpecification(name, maximise, weight);
						this.configuration.add(element);
					}
				} 
				catch (ParserConfigurationException | SAXException | IOException e) 
				{
					System.out.println("\r\nEXCEPTION: Cannot read metric configuration from xml file.");
					System.exit(1);
				}
			}
		}
	}
	
	public Configuration(String pathway)
	{
		this.configuration = new ArrayList<MetricSpecification>();
		File file = new File(pathway);
		
		if (file.exists())
		{
			if (file.getName().endsWith(".txt"))
			{
				try 
				{
					BufferedReader br = new BufferedReader(new FileReader(file));
					String line;
					boolean executeWhile = true;

					switch (br.readLine()) 
					{
					case "PRIORITY":
						readPriority(br);
						executeWhile = false;
						break;
					case "ELEMENT RECENTNESS":
						readElementRecentness(br);
						executeWhile = false;
						break;
					}
					
					br = new BufferedReader(new FileReader(file));
					
					while (((line = br.readLine()) != null) && executeWhile)
					{
						// \\s+ means any number of white spaces between tokens
						String [] tokens = line.split("\\s+");
						MetricSpecification element = new MetricSpecification(tokens[0], tokens[1].equals("true"), Float.parseFloat(tokens[2]));
						this.configuration.add(element);
					}

					br.close();
				} 
				catch (IOException e) 
				{
					System.out.println("\r\nEXCEPTION: Cannot read metric configuration from text file.");
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

					for (int i = 0 ; i < nodes.getLength(); i++) 
					{
						String name = nodes.item(i).getAttributes().getNamedItem("name").getTextContent();
						boolean maximise = nodes.item(i).getAttributes().getNamedItem("maximise").getTextContent().equals("true");
						float weight = Float.parseFloat(nodes.item(i).getAttributes().getNamedItem("weight").getTextContent());
						MetricSpecification element = new MetricSpecification(name, maximise, weight);
						this.configuration.add(element);
					}
				} 
				catch (ParserConfigurationException | SAXException | IOException e) 
				{
					System.out.println("\r\nEXCEPTION: Cannot read metric configuration from xml file.");
					System.exit(1);
				}
			}
		}
	}
		
	// Reinitialises the refactorings used in the configuration which
	// is necessary when a model is reset to use in a genetic algorithm.
	public ArrayList<Refactoring> resetRefactorings(CrossReferenceServiceConfiguration sc, ArrayList<Refactoring> refactorings, boolean store)
	{
		ArrayList<Refactoring> newRefactorings = new ArrayList<Refactoring>(refactorings.size());
		
		for (Refactoring r : refactorings)
		{
			if (r instanceof DecreaseMethodVisibility)
			{
				DecreaseMethodVisibility dms = new DecreaseMethodVisibility(sc);
				newRefactorings.add(dms);
			}
			else if (r instanceof DecreaseFieldVisibility)
			{
				DecreaseFieldVisibility dfs = new DecreaseFieldVisibility(sc);
				newRefactorings.add(dfs);
			}
			else if (r instanceof IncreaseMethodVisibility)
			{
				IncreaseMethodVisibility ims = new IncreaseMethodVisibility(sc);
				newRefactorings.add(ims);
			}
			else if (r instanceof IncreaseFieldVisibility)
			{
				IncreaseFieldVisibility ifs = new IncreaseFieldVisibility(sc);
				newRefactorings.add(ifs);
			}
			else if (r instanceof MakeClassAbstract)
			{
				MakeClassAbstract mca = new MakeClassAbstract(sc);
				newRefactorings.add(mca);
			}
			else if (r instanceof MakeClassConcrete)
			{
				MakeClassConcrete mcc = new MakeClassConcrete(sc);
				newRefactorings.add(mcc);
			}
			else if (r instanceof MakeClassFinal)
			{
				MakeClassFinal mcf = new MakeClassFinal(sc);
				newRefactorings.add(mcf);
			}
			else if (r instanceof MakeMethodFinal)
			{
				MakeMethodFinal mmf = new MakeMethodFinal(sc);
				newRefactorings.add(mmf);
			}
			else if (r instanceof MakeFieldFinal)
			{
				MakeFieldFinal mff = new MakeFieldFinal(sc);
				newRefactorings.add(mff);
			}
			else if (r instanceof MakeClassNonFinal)
			{
				MakeClassNonFinal mcnf = new MakeClassNonFinal(sc);
				newRefactorings.add(mcnf);
			}
			else if (r instanceof MakeMethodNonFinal)
			{
				MakeMethodNonFinal mmnf = new MakeMethodNonFinal(sc);
				newRefactorings.add(mmnf);
			}
			else if (r instanceof MakeFieldNonFinal)
			{
				MakeFieldNonFinal mfnf = new MakeFieldNonFinal(sc);
				newRefactorings.add(mfnf);
			}
			else if (r instanceof MakeMethodStatic)
			{
				MakeMethodStatic mms = new MakeMethodStatic();
				newRefactorings.add(mms);
			}
			else if (r instanceof MakeFieldStatic)
			{
				MakeFieldStatic mfs = new MakeFieldStatic(sc);
				newRefactorings.add(mfs);
			}
			else if (r instanceof MakeMethodNonStatic)
			{
				MakeMethodNonStatic mmns = new MakeMethodNonStatic(sc);
				newRefactorings.add(mmns);
			}
			else if (r instanceof MakeFieldNonStatic)
			{
				MakeFieldNonStatic mfns = new MakeFieldNonStatic(sc);
				newRefactorings.add(mfns);
			}

			else if (r instanceof MoveMethodUp)
			{
				MoveMethodUp mmu = new MoveMethodUp(sc);
				newRefactorings.add(mmu);
			}
			else if (r instanceof MoveMethodDown)
			{
				MoveMethodDown mmd = new MoveMethodDown(sc);
				newRefactorings.add(mmd);
			}
			else if (r instanceof MoveFieldUp)
			{
				MoveFieldUp mfu = new MoveFieldUp(sc);
				newRefactorings.add(mfu);
			}
			else if (r instanceof MoveFieldDown)
			{
				MoveFieldDown mfd = new MoveFieldDown(sc);
				newRefactorings.add(mfd);
			}
			else if (r instanceof RemoveInterface)
			{
				RemoveInterface ri = new RemoveInterface(sc);
				newRefactorings.add(ri);
			}
			else if (r instanceof RemoveClass)
			{
				RemoveClass rc = new RemoveClass(sc);
				newRefactorings.add(rc);
			}
			else if (r instanceof RemoveMethod)
			{
				RemoveMethod rm = new RemoveMethod(sc);
				newRefactorings.add(rm);
			}
			else if (r instanceof RemoveField)
			{
				RemoveField rf = new RemoveField(sc);
				newRefactorings.add(rf);
			}
			else if (r instanceof ExtractSubclass)
			{
				ExtractSubclass es = new ExtractSubclass(sc);
				newRefactorings.add(es);
			}
			else if (r instanceof CollapseHierarchy)
			{
				CollapseHierarchy ch = new CollapseHierarchy(sc);
				newRefactorings.add(ch);
			}			
		}
		
		for (Refactoring r : newRefactorings)
			r.setServiceConfiguration(sc);
		
		if (store)
			this.refactorings = new ArrayList<Refactoring>(newRefactorings);
		
		return newRefactorings;
	}

	// Reads in and stores classes from text input when the priority objective is being used.
	// Also checks if non priority classes are included and stores them separately if so.
	private void readPriority(BufferedReader br)
	{
		String line;
		boolean priority = true;
		this.priorityClasses = new ArrayList<String>();
		this.configuration.add(new MetricSpecification("priority", true, 1.0f));

		try
		{
			while ((line = br.readLine()) != null)
			{
				if ((line.equals("")) && (br.readLine().equals("NONPRIORITY")))
				{
					line = br.readLine();
					priority = false;
					this.nonPriorityClasses = new ArrayList<String>();
				}

				if (priority)
					this.priorityClasses.add(line);
				else
					this.nonPriorityClasses.add(line);
			}				
		} 
		catch (IOException e) 
		{
			System.out.println("\r\nEXCEPTION: Cannot read priority classes from text file.");
			System.exit(1);
		}
	}
	
	// Reads in program pathways from text input when the element recentness objective is being used.
	// These pathways are then used to read and store the set of strings representing the source code in each pathway.
	private void readElementRecentness(BufferedReader br)
	{
		String line;
		this.previousUnits = new ArrayList<List<CompilationUnit>>();
		this.configuration.add(new MetricSpecification("elementRecentness", true, 1.0f));
		System.out.printf("\r\n\r\nReading previous versions of project from the following pathways:");

		try
		{
			while ((line = br.readLine()) != null)
			{
				System.out.printf("\r\n  %s", line);
				CrossReferenceServiceConfiguration sc = new CrossReferenceServiceConfiguration();
				sc.getSourceFileRepository().getCompilationUnitsFromFiles(Tasks.read(line));
				sc.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, line + Tasks.readLibs(line));
				sc.getProjectSettings().ensureSystemClassesAreInPath();
				sc.getChangeHistory().updateModel();
				this.previousUnits.add(sc.getSourceFileRepository().getKnownCompilationUnits());				
			}
		} 
		catch (IOException | ParserException e) 
		{
			if (e instanceof IOException)
				System.out.println("\r\nEXCEPTION: Cannot read source code from previous version pathways in text file.");
			else
				System.out.println("\r\nEXCEPTION: Cannot extract compilation units from previous version pathways in text file.");
			
			System.exit(1);
		}
	}

	
	public ArrayList<MetricSpecification> getConfiguration()
	{
		return this.configuration;
	}
	
	public boolean setConfigurationElement(String name, boolean maximise, float weight)
	{
		boolean present = false;
		
		for (MetricSpecification element : this.configuration)
		{
			if (element.getName().equals(name))
			{
				element.setMaximise(maximise);
				element.setWeight(weight);
				present = true;
			}
		}
		
		if (!present)
			this.configuration.add(new MetricSpecification(name, maximise, weight));
			
		return !present;
	}
	
	public ArrayList<Refactoring> getRefactorings()
	{
		return this.refactorings;
	}
	
	public void setRefactorings(ArrayList<Refactoring> refactorings)
	{
		this.refactorings = refactorings;
	}
	
	public boolean setRefactoring(Refactoring r)
	{
		if (!this.refactorings.contains(r))
		{
			this.refactorings.add(r);
			return true;
		}
		else
			return false;
	}
	
	public ArrayList<String> getPriorityClasses()
	{
		return this.priorityClasses;
	}
	
	public ArrayList<String> getNonPriorityClasses()
	{
		return this.nonPriorityClasses;
	}
	
	public ArrayList<List<CompilationUnit>> getPreviousUnits()
	{
		return this.previousUnits;
	}
}