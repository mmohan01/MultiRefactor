import java.io.File;

//  // Original refactoring class that did all refactorings together
//  // and applied the refactoring for all available emelements.
//	public class Refactorings extends TwoPassTransformation 
//	{
//		private ArrayList<TwoPassTransformation> transformations;
//		private ArrayList<String> refactoringInfo = new ArrayList<String>();
//		
//		AbstractTreeWalker tw; 
//		
//		public Refactorings(CrossReferenceServiceConfiguration sc) 
//		{
//			super(sc);
//		}
//		
//		public ProblemReport analyze(int iteration) 
//		{
//			transformations = new ArrayList<TwoPassTransformation>();
//			CrossReferenceServiceConfiguration config = getServiceConfiguration();
//			ProblemReport report = EQUIVALENCE;		
//			int rand1 = (int) (Math.random() * getSourceFileRepository().getKnownCompilationUnits().size());
//			tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(rand1));
//			
//			int counter = 0;
//			while (tw.next(Declaration.class))
//				counter++;
//			
//			tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(rand1));
//			int rand2 = (int) (Math.random() * counter);
//			
//			for (int i = 0; i < rand2; i++)
//				tw.next(Declaration.class);
//				
//			ProgramElement pe = tw.getProgramElement();
//			TwoPassTransformation t;
//	
//			boolean flag = true;
//			while (flag)
//			{
//				if ((pe instanceof TypeDeclaration)||(pe instanceof MethodDeclaration)||(pe instanceof VariableDeclaration))	
//				{
//					flag = false;
//					//int index = pe.getClass().getName().lastIndexOf(".") + 1;
//					//System.out.printf("\n  ITERATION %d: Element is %s", iteration, pe.getClass().getName().substring(index));
//					//System.out.printf("\n  ITERATION %d: Also amount of declarations for this class in this iteration is %d", iteration, counter);
//				}
//				else
//				{
//					tw.next();
//					pe = tw.getProgramElement();
//					//System.out.printf("\n  ITERATION %d: Getting next applicable element", iteration);
//				}
//			}
//	
//			if (pe instanceof VariableDeclaration) 
//			{
//				VariableDeclaration vd = (VariableDeclaration) pe;
//				if (mayRefactor(vd)) 
//				{
//					t = new Modify(config, true, vd, visibilityDown(vd.getVisibilityModifier()));
//					report = t.analyze();
//					if (report instanceof Problem) 
//						return setProblemReport(report);
//					transformations.add(t);
//					
//					int last = pe.toString().lastIndexOf(">");
//					String refInfo = "Iteration " + iteration + ": Visibility reduction applied at class " 
//									 + getFileName(getSourceFileRepository().getKnownCompilationUnits().get(rand1).getName())
//							         + " to element " + pe.getClass().getSimpleName() + " (" + pe.toString().substring(last + 2)
//							         + ") from " + currentModifier(vd.getVisibilityModifier()) + " to " 
//							         + refactoredModifier(vd.getVisibilityModifier());
//					refactoringInfo.add(refInfo);
//				}
//			}
//			else if (pe instanceof MethodDeclaration) 
//			{
//				MethodDeclaration md = (MethodDeclaration) pe;
//				if (!(pe instanceof ConstructorDeclaration) && mayRefactor(md)) 
//				{
//					t = new Modify(config, true, md, visibilityDown(md.getVisibilityModifier()));
//					report = t.analyze();
//					if (report instanceof Problem) 
//						return setProblemReport(report);
//					transformations.add(t);
//					
//					String refInfo = "Iteration " + iteration + ": Visibility reduction applied at class " 
//							 		 + getFileName(getSourceFileRepository().getKnownCompilationUnits().get(rand1).getName())
//							 		 + " to element " + pe.getClass().getSimpleName() + " (" + ((MethodDeclaration) pe).getName()
//							 		 + ") from " + currentModifier(md.getVisibilityModifier()) + " to " 
//							 		 + refactoredModifier(md.getVisibilityModifier());
//					refactoringInfo.add(refInfo);
//				}
//			} 
//			else if (pe instanceof TypeDeclaration) 
//			{
//				TypeDeclaration td = (TypeDeclaration) pe;
//				if (!(pe instanceof ConstructorDeclaration) && mayRefactor(td)) 
//				{
//					t = new Modify(config, true, td, visibilityDown(td.getVisibilityModifier()));
//					report = t.analyze();
//					if (report instanceof Problem) 
//						return setProblemReport(report);
//					transformations.add(t);
//					
//					String refInfo = "Iteration " + iteration + ": Visibility reduction applied at class " 
//							 		 + getFileName(getSourceFileRepository().getKnownCompilationUnits().get(rand1).getName())
//							 		 + " to element " + pe.getClass().getSimpleName() + " (" + ((TypeDeclaration) pe).getName()
//							 		 + ") from " + currentModifier(td.getVisibilityModifier()) + " to " 
//							 		 + refactoredModifier(td.getVisibilityModifier());
//					refactoringInfo.add(refInfo);
//				}
//			} 
//			else
//				System.out.printf("\n  ITERATION %d: Element is not applicable for this refactoring", iteration);
//				//tw.next();
//			
//			return setProblemReport(EQUIVALENCE);
//		}
//		
//		public void transform(ProblemReport p) 
//		{
//			if(p instanceof Problem)
//	        {
//	        	System.out.println("\nPROBLEM REPORT: ");
//	        	System.err.println(p.toString());
//	        }
//			else
//			{
//				super.transform();
//	
//				for (int i = 0, s = transformations.size(); i < s; i += 1) 
//					transformations.get(i).transform();		
//			}
//		}
//		
//		public ProblemReport analyzeAll() 
//		{
//			transformations = new ArrayList<TwoPassTransformation>();
//			CrossReferenceServiceConfiguration config = getServiceConfiguration();
//			ProblemReport report = EQUIVALENCE;
//			
//			while (tw.next()) 
//			{
//				ProgramElement pe = tw.getProgramElement();
//				TwoPassTransformation t;
//				
//				if (pe instanceof VariableDeclaration) 
//				{
//					VariableDeclaration vd = (VariableDeclaration) pe;
//					if (mayRefactor(vd)) 
//					{
//						t = new Modify(config, true, vd, visibilityDown(vd.getVisibilityModifier()));
//						report = t.analyze();
//						if (report instanceof Problem) 
//							return setProblemReport(report);
//						transformations.add(t);
//					}
//				}
//				else if (pe instanceof MethodDeclaration) 
//				{
//					MethodDeclaration md = (MethodDeclaration) pe;
//					if (!(pe instanceof ConstructorDeclaration) && mayRefactor(md)) 
//					{
//						t = new Modify(config, true, md, visibilityDown(md.getVisibilityModifier()));
//						//t = new RenameMethod(config, md, "_2");
//						report = t.analyze();
//						if (report instanceof Problem) 
//							return setProblemReport(report);
//						transformations.add(t);
//					}
//				} 
//				else if (pe instanceof TypeDeclaration) 
//				{
//					TypeDeclaration td = (TypeDeclaration) pe;
//					if (!(pe instanceof ConstructorDeclaration) && mayRefactor(td)) 
//					{
//						t = new Modify(config, true, td, visibilityDown(td.getVisibilityModifier()));
//						report = t.analyze();
//						if (report instanceof Problem) 
//							return setProblemReport(report);
//						transformations.add(t);
//					}
//				} 
//			}
//			
//			return setProblemReport(EQUIVALENCE);
//		}
//		
//		public void transformAll() 
//		{
//			super.transform();
//			
//			for (int i = 0, s = transformations.size(); i < s; i += 1) 
//				transformations.get(i).transform();
//			
//			SourceFileRepository sfr = getServiceConfiguration().getSourceFileRepository();
//	        List<CompilationUnit> list = sfr.getKnownCompilationUnits();
//	
//	        for (CompilationUnit u : list)
//	        {
//	        	if(!sfr.isUpToDate(u))
//	        		System.out.println("\nClass changed");
//	        	
//	        	try 
//	        	{
//					sfr.print(u);
//				} 
//	        	catch (IOException e) 
//	        	{
//					e.printStackTrace();
//				}
//	        }
//			
//		}
//		
//		public ProblemReport reverseRefactoring()
//		{
//			transformations = new ArrayList<TwoPassTransformation>();
//			CrossReferenceServiceConfiguration config = getServiceConfiguration();
//			ProblemReport report = EQUIVALENCE;		
//			ProgramElement pe = tw.getProgramElement();
//			TwoPassTransformation t;
//	
//			if (pe instanceof VariableDeclaration) 
//			{
//				VariableDeclaration vd = (VariableDeclaration) pe;
//				if (mayRefactor(vd)) 
//				{
//					t = new Modify(config, true, vd, visibilityUp(vd.getVisibilityModifier()));
//					report = t.analyze();
//					if (report instanceof Problem) 
//						return setProblemReport(report);
//					transformations.add(t);
//				}
//			}
//			else if (pe instanceof MethodDeclaration) 
//			{
//				MethodDeclaration md = (MethodDeclaration) pe;
//				if (!(pe instanceof ConstructorDeclaration) && mayRefactor(md)) 
//				{
//					t = new Modify(config, true, md, visibilityUp(md.getVisibilityModifier()));
//					//t = new RenameMethod(config, md, "_2");
//					report = t.analyze();
//					if (report instanceof Problem) 
//						return setProblemReport(report);
//					transformations.add(t);
//				}
//			} 
//			else if (pe instanceof TypeDeclaration) 
//			{
//				TypeDeclaration td = (TypeDeclaration) pe;
//				if (!(pe instanceof ConstructorDeclaration) && mayRefactor(td)) 
//				{
//					t = new Modify(config, true, td, visibilityUp(td.getVisibilityModifier()));
//					report = t.analyze();
//					if (report instanceof Problem) 
//						return setProblemReport(report);
//					transformations.add(t);
//				}
//			} 
//			
//			return setProblemReport(EQUIVALENCE);
//		}
//		
//		public String currentModifier(VisibilityModifier vm)
//		{		
//			if (vm instanceof Public)
//				return "public";
//			else if (vm instanceof Protected)
//				return "protected";
//			else if (vm instanceof Private)
//				return "private";
//			else
//				return "package";
//		}
//		
//		public String refactoredModifier(VisibilityModifier vm)
//		{		
//			if (vm instanceof Public)
//				return "protected";
//			else
//				return "private";
//		}
//		
//		public int visibilityDown(VisibilityModifier vm)
//		{
//			if (vm instanceof Public)
//				return AccessFlags.PROTECTED;
//			else if (vm instanceof Protected)
//				//return AccessFlags.PACKAGE;
//				return AccessFlags.PRIVATE;
//			else
//				return AccessFlags.PRIVATE;
//		}
//		
//		public int visibilityUp(VisibilityModifier vm)
//		{
//			if ((vm instanceof Public) || (vm instanceof Protected))
//				return AccessFlags.PUBLIC;
//			//else if (vm instanceof Private)
//				//return AccessFlags.PACKAGE;
//			else
//				return AccessFlags.PROTECTED;
//		}
//		
//		public boolean mayRefactor(MethodDeclaration md)
//		{
//			if (md.getVisibilityModifier() instanceof Private)
//				return false;
//			else
//				return true;
//		}
//		
//		public boolean mayRefactor(TypeDeclaration td)
//		{
//			if (td.getVisibilityModifier() instanceof Private)
//				return false;
//			else
//				return true;	
//		}
//		
//		public boolean mayRefactor(VariableDeclaration vd)
//		{
//			if (vd.getVisibilityModifier() instanceof Private)
//				return false;
//			else
//				return true;	
//		}
//		
//		// Truncates a path name to just the element name.
//		public String getFileName(String input)
//		{
//			int lastDash = input.lastIndexOf("\\");
//			int lastDot = input.lastIndexOf(".");
//			if ((lastDash >= 0) && (lastDot >= 0)) 
//				input = input.substring(lastDash + 1, lastDot);
//	
//			return input;
//		}
//		
//		public ArrayList<String> getRefactoringInfo()
//		{
//			return refactoringInfo;
//		}
//	}
//
//
//	// Prototype method to copy a project from one directory to another
//	// The destination file names are not being copied right - need to gain access to the folder pathway.
//	File[] files = filePath.listFiles();
//	String output = "./data/refactored/" + filePath.getName();
//	File dir = new File(output);
//	if (!dir.exists()) 
//		dir.mkdirs();
//	try 
//	{
//		for(File f : files)
//		{
//			if (!f.isDirectory())
//			{
//				System.out.println(f.getName());
//				InputStream in = new FileInputStream(f);
//				OutputStream out = new FileOutputStream(dir+"/"+f.getName());
//
//				// Transfer bytes from source files to duplicate files.
//				byte[] buf = new byte[1024];
//				int len;
//				while ((len = in.read(buf)) > 0) 
//					out.write(buf, 0, len);
//
//				in.close();
//				out.close();
//			}
//		}
//	} 
//	catch (FileNotFoundException e) 
//	{
//		System.out.println("\nEXCEPTION: Cannot open Input/Output stream.");
//		System.exit(1);
//	} 
//	catch (IOException e) 
//	{
//		System.out.println("\nEXCEPTION: Cannot create copy of input.");
//		System.exit(1);
//	}
//
//
//	// Read can still be used to use the original input instead of creating a copy to use.
//  // This could be useful if I knew I didn't need the original project for multiple runs
//  // (for example in a GUI where the program is only run once). This would save
//  // a few seconds at initialisation copying the project.
//	// String[] sourceFiles = example.read();
//
//
//	// Method used to analyze and transform a transformation to
//	// the Compilation Units using the RECODER example programs.
//	public static void execute(TwoPassTransformation transformation, List<CompilationUnit> units)
//	{
//		CrossReferenceServiceConfiguration crsc = transformation.getServiceConfiguration();
//		// ProblemReport problemreport = transformation.execute();
//		ProblemReport problemreport = transformation.analyze();
//		transformation.transform();
//		if(problemreport instanceof Problem)
//		{
//			System.out.println("\nPROBLEM REPORT:");
//			System.err.println(problemreport.toString());
//		}
//		else
//		{
//			System.out.println("\n\nTransformation succeeded - writing results");
//			SourceFileRepository sfr = crsc.getSourceFileRepository();
//	
//			List<CompilationUnit> list = sfr.getKnownCompilationUnits();
//			for (CompilationUnit u :list)
//			{
//				if(!sfr.isUpToDate(u))
//					System.out.println("\nClass changed");
//	
//				try {
//					sfr.print(u);
//				} catch (IOException e) {
//					e.printStackTrace();
//				}
//			}
//			for(int i = 0; i < list.size(); i++)
//			{
//				CompilationUnit cu = (CompilationUnit)list.get(i);
//				if(sfr.isUpToDate(cu))
//					continue;
//	
//				System.out.println(Format.toString("%u [%f] ??", cu));
//				try
//				{
//					//sfr.print(cu);
//					sfr.printAll(true);
//					//sfr.print(units.get(i));
//				}
//				catch(IOException e)
//				{
//					System.out.println("\nEXCEPTION: Cannot write transformation to source.");
//					System.exit(1);
//				}
//			}
//		}
//	}
//	
//
//	 // Used to run the RECODER example from the 
//	 // main with the same read input class.
//	 String[] m = new String[2];
//	 m[0] = "refactory/refactory.jar";
//	 m[1] = "refactory/src";
//	 RecoderProgram.execute(new Obfuscate(new CrossReferenceServiceConfiguration()), m);
//
//
//	 // Used to run RECODER example form the
//	 // main with the data input directly.
//	 // Reads the source code from the specified directory.
//	 PathReader example = new PathReader("./data/original/acma");
//	 String[] sourceFiles = example.cloneSource();
//	 CrossReferenceServiceConfiguration sc = new CrossReferenceServiceConfiguration();
//	
//	 try 
//	 { 
//		 sc.getSourceFileRepository().getCompilationUnitsFromFiles(sourceFiles);
//		 Obfuscate bro = new Obfuscate(sc);
//		 bro.analyze();
//		 bro.transform();
//	 }
//	 catch (ParserException e) 
//	 {
//	   System.out.println("\nEXCEPTION: Cannot read input.");
//		 System.exit(1);
//	 } 
//
//
//	// Used to apply refactorings in the Tasks file directly. Refactorings are
//	// applied across all available possibilities instead of just one random location.
//	// Also outputs a list of source files in the input and the children of each.
//	try 
//	{ 
//		List<CompilationUnit> units = sc.getSourceFileRepository().getCompilationUnitsFromFiles(sourceFiles);
//	
//		// Lists the file names and their child classes.
//		Metrics metric = new Metrics(units);
//		for (CompilationUnit u : units)
//			System.out.printf("\nFile: %s - %d Children", getFileName(u.getName()), metric.children(u)); 
//	
//		// Outputs the initial metric values for the 
//		// project to a text file and the console.
//		outputMetrics(metric, "Initial");
//	
//		// Reduces the visibility of any method or variable declaration by one unit.
//		//execute(new Refactorings(sc, units), units);
//		Refactorings r = new Refactorings(sc);
//		ProblemReport problemreport = r.analyze();
//		if(problemreport instanceof Problem)
//		{
//			System.out.println("\nPROBLEM REPORT: ");
//			System.err.println(problemreport.toString());
//		}
//		else
//			r.transform();
//	
//		// Outputs the final metric values for the 
//		// project to a text file and the console.
//		outputMetrics(metric, "Final");
//	}
//	catch (ParserException e) 
//	{
//		System.out.println("\nEXCEPTION: Cannot read input.");
//		System.exit(1);
//	} 
//
//
//  // Prints out the refactoring information of the visibility down
//  // refactoring to the with the three different declaration types.
//	System.out.printf("\n  Iteration %d: Visibility reduction applied at class %s to element %s (%s) from %s to %s.", iteration, 
//			getFileName(getSourceFileRepository().getKnownCompilationUnits().get(rand1).getName()), 
//			pe.getClass().getSimpleName(), pe.toString().substring(last + 2), 
//			currentModifier(vd.getVisibilityModifier()), refactoredModifier(vd.getVisibilityModifier()));
//	System.out.printf("\n  Iteration %d: Visibility reduction applied at class %s to element %s (%s) from %s to %s.", iteration, 
//			getFileName(getSourceFileRepository().getKnownCompilationUnits().get(rand1).getName()), 
//			pe.getClass().getSimpleName(), ((MethodDeclaration) pe).getName(), 
//			currentModifier(md.getVisibilityModifier()), refactoredModifier(md.getVisibilityModifier()));
//	System.out.printf("\n  Iteration %d: Visibility reduction applied at class %s to element %s (%s) from %s to %s.", iteration, 
//			getFileName(getSourceFileRepository().getKnownCompilationUnits().get(rand1).getName()), 
//			pe.getClass().getSimpleName(), ((TypeDeclaration) pe).getName(), 
//			currentModifier(td.getVisibilityModifier()), refactoredModifier(td.getVisibilityModifier()));
//
//
// // Line used to navigate to the next compilation unit when there 
// // are no instances of the specified program element in the current unit.
// // The problem with this was that it may run out of units and no check was 
// // put in place to handle this if it happened so a random unit was chosen again.
// tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(rand1 + repeat));
//
//
//  // Applies each type of refactoring in order across the iterations of a search.
//	if (i%9 == 0)
//	{
//		dcs.transform(dcs.analyze(i));
//		this.refactoringInfo.add(dcs.getRefactoringInfo());
//	}
//	if (i%9 == 1)
//	{
//		dms.transform(dms.analyze(i));
//		this.refactoringInfo.add(dms.getRefactoringInfo());
//	}
//	if (i%9 == 2)
//	{
//		dfs.transform(dfs.analyze(i));
//		this.refactoringInfo.add(dfs.getRefactoringInfo());
//	}
//	if (i%9 == 3)
//	{
//		mca.transform(mca.analyze(i));
//		this.refactoringInfo.add(mca.getRefactoringInfo());
//	}
//	
//	if (i%9 == 4)
//	{
//		mcf.transform(mcf.analyze(i));
//		this.refactoringInfo.add(mcf.getRefactoringInfo());
//	}
//	if (i%9 == 5)
//	{
//		mff.transform(mff.analyze(i));
//		this.refactoringInfo.add(mff.getRefactoringInfo());
//	}
//	if (i%9 == 6)
//	{
//		mfs.transform(mfs.analyze(i));
//		this.refactoringInfo.add(mfs.getRefactoringInfo());
//	}
//	if (i%9 == 7)
//	{
//		mff.transform(mmf.analyze(i));
//		this.refactoringInfo.add(mff.getRefactoringInfo());
//	}
//	if (i%9 == 8)
//	{
//		mms.transform(mms.analyze(i));
//		this.refactoringInfo.add(mms.getRefactoringInfo());
//	}
//
//
//  // Outputs the iteration and relevant program element for the refactoring of a search.
//  int index = pe.getClass().getName().lastIndexOf(".") + 1;
//  System.out.printf("\n  Iteration %d: Element is %s", iteration, pe.getClass().getName().substring(index));
//
//
//	// Copies the input project to another directory 
//	// and returns the java files from the copied directory.
//  // No longer needed after finding out that you can specify an 
//  // output directory for printing transformations in RECODER.
//	public String[] cloneSource()
//	{
//		String output = "./data/refactored/" + this.filePath.getName();
//		File dir = new File(output);
//		if (!dir.exists()) 
//			dir.mkdirs();
//
//		try 
//		{
//			FileUtils.copyDirectory(this.filePath, dir);
//		} 
//		catch (IOException e1) 
//		{
//			System.out.println("\nEXCEPTION: Cannot create copy of input.");
//			System.exit(1);
//		}
//	
//		List<File> newfiles	= findAllSourceFiles(dir);
//		String[] fileList = new String[newfiles.size()];
//		
//		for (int i = 0; i < newfiles.size(); i++)
//			fileList[i] = newfiles.get(i).getAbsolutePath();
//		
//		return fileList;
//	}	
//
//
//	// How to create a metric configuration directly by either 
//  // passing in a set of arrays or an array list of triples.
//	String[] s = {"classDeclarationAmount", "methodDeclarationAmount", "methodsPerType", 
//			"abstractness", "abstractAmount", "staticAmount", "finalAmount", "innerClassAmount", 
//			"childAmount", "linesOfCode", "fileAmount", "visibility"};
//	boolean[] b = {true, false, true, true, true, true, true, true, true, false, true, false};
//	float[] f = { 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
//	ArrayList<Triple<String, Boolean, Float>> configuration = new ArrayList<Triple<String, Boolean, Float>>();
//	for (int i = 0; i < s.length; i++)
//	{
//		Triple<String, Boolean, Float> element = new Triple<String, Boolean, Float> (s[i], b[i], f[i]);
//		configuration.add(element);
//	}
//
//
//	// Methods previously used with the DecreaseFieldSecurity class.
//  // The random checks and elements chosen were incorporated into the methods,
//  // so these aspects were taken out to make the methods more generic for other searches.
//	public ProblemReport analyze(int iteration) 
//	{
//		// Initialise and pick a random class to visit.
//		CrossReferenceServiceConfiguration config = getServiceConfiguration();
//		ProblemReport report = EQUIVALENCE;		
//		super.randomUnit = (int) (Math.random() * getSourceFileRepository().getKnownCompilationUnits().size());
//		AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(super.randomUnit));
//		
//		// Count the amount of available elements in the chosen class for refactoring.
//		// If none are available pick another random class until there are.
//		int counter = 0;
//		while (counter <= 0)
//		{
//			// Only counts the relevant program element.
//			while (tw.next(VariableDeclaration.class))
//				counter++;
//			
//			if (counter == 0)
//			{
//				super.randomUnit = (int) (Math.random() * getSourceFileRepository().getKnownCompilationUnits().size());
//				tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(super.randomUnit));
//			}
//		}
//		
//		// Reset tree walker to the beginning of the class. Random value is given between 1 and the 
//		// amount of elements available in the class. Tree walker needs to iterate at least once to 
//		// access the relevant program element, hence the lower bound.
//		tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(super.randomUnit));
//		super.randomElement = (int) (Math.random() * (counter-1)) + 1;
//		
//		for (int i = 0; i < super.randomElement; i++)
//			tw.next(VariableDeclaration.class);
//			
//		ProgramElement pe = tw.getProgramElement();
//		VariableDeclaration vd = (VariableDeclaration) pe;
//		int last = pe.toString().lastIndexOf(">");
//	
//		// If element is suitable for refactoring.
//		if (mayRefactor(vd)) 
//		{
//			// Construct refactoring transformation.
//			super.transformation = new Modify(config, true, vd, super.visibilityDown(vd.getVisibilityModifier()));
//			report = super.transformation.analyze();
//			if (report instanceof Problem) 
//				return setProblemReport(report);
//			
//			// Specify refactoring information for results information.
//			super.refactoringInfo = "Iteration " + iteration + ": Visibility reduction applied at class " 
//					+ super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(super.randomUnit).getName())
//					+ " to element " + pe.getClass().getSimpleName() + " (" + pe.toString().substring(last + 2)
//					+ ") from " + super.currentModifier(vd.getVisibilityModifier()) + " to " 
//					+ refactoredModifier(vd.getVisibilityModifier());
//		} 
//		// No transformation will be performed.
//		else
//		{
//			super.transformation = null;
//			
//			// Specify refactoring information of non applicable refactoring.
//			super.refactoringInfo = "Iteration " + iteration + ": Visibility reduction NOT applied at class " 
//					+ super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(super.randomUnit).getName())
//					+ " to element " + pe.getClass().getSimpleName() + " (" + pe.toString().substring(last + 2)
//					+ ") from " + super.currentModifier(vd.getVisibilityModifier()) + " as it is not applicable";
//		}
//		
//		return setProblemReport(EQUIVALENCE);
//	}
//	
//	public ProblemReport analyzeReverse(int randomUnit, int randomElement) 
//	{
//		// Initialise and pick a revisit random class.
//		CrossReferenceServiceConfiguration config = getServiceConfiguration();
//		ProblemReport report = EQUIVALENCE;
//		AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(randomUnit));
//		
//		for (int i = 0; i < randomElement; i++)
//			tw.next(VariableDeclaration.class);
//			
//		ProgramElement pe = tw.getProgramElement();
//		VariableDeclaration vd = (VariableDeclaration) pe;
//	
//		// Construct refactoring transformation.	
//		super.transformation = new Modify(config, true, vd, super.visibilityDown(vd.getVisibilityModifier()));
//		report = super.transformation.analyze();
//		if (report instanceof Problem) 
//			return setProblemReport(report);
//	
//		return setProblemReport(EQUIVALENCE);
//	}
//	
//	public void transformReverse() 
//	{
//		IncreaseFieldSecurity dfs = new IncreaseFieldSecurity(getServiceConfiguration());
//		dfs.transform(dfs.analyzeReverse(super.randomUnit, super.randomElement));
//	}
//
//
//  // Code to use an array to find out where applicable
//  // elements are and save having to check twice.
//	// Count the amount of available elements in the chosen class for refactoring.
//	// If an element is not applicable for the current refactoring it is not counted.
//	public int[] getAmount(int unit)
//	{
//		int counter = 0;
//		int iterator = 0;
//		int [] positions = null;
//		AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
//	
//		// Only counts the relevant program element.
//		while (tw.next(VariableDeclaration.class))
//		{
//			counter++;
//			VariableDeclaration vd = (VariableDeclaration) tw.getProgramElement();
//			if (mayRefactor(vd))
//			{
//				positions[iterator] = counter;
//				iterator++;
//			}
//		}
//	
//		return positions;
//	}
//	
//	int [] positions = null;
//	
//	// Count the amount of available elements in the chosen class for refactoring.
//	// If none are available pick another random class until there are.
//	int amount = 0;
//	while (positions == null)
//	{
//		// Only counts the relevant program element.
//		positions = refactorings.get(randomRef).getAmount(randomUnit);
//	
//		if (amount == 0)
//			randomUnit = (int) (Math.random() * super.sc.getSourceFileRepository().getKnownCompilationUnits().size());
//	}
//	
//	// Random element chosen from the available amount.
//	// Refactoring is applied to that element.
//	int randomElement = (int) (Math.random() * (positions.length-1)) + 1;
//	refactorings.get(randomRef).transform(refactorings.get(randomRef).analyze(i, randomUnit, randomElement));
//	super.refactoringInfo.add(refactorings.get(randomRef).getRefactoringInfo());	
//	
//	int positions[] = getAmount(unit);
//	for (int i = 0; i < positions[element]; i++)
//		tw.next(VariableDeclaration.class);
//
//
//  // Get next applicable element either above or below 
//  // the current position, with a chosen refactoring.
//	else
//	{
//		int[] positionDup = position;
//	
//		if (Math.random() >= 0.5)
//		{
//			positionDup = super.nextElementUp(positionDup[0], positionDup[1], refactorings.get(randomRef));
//	
//			if ((positionDup[0] == -1) && (positionDup[1] == -1))
//				position = super.nextElementDown(position[0], position[1], refactorings.get(randomRef));
//			else
//				position = positionDup;
//		}
//		else
//		{
//			positionDup = super.nextElementDown(positionDup[0], positionDup[1], refactorings.get(randomRef));
//	
//			if ((positionDup[0] == -1) && (positionDup[1] == -1))
//				position = super.nextElementUp(position[0], position[1], refactorings.get(randomRef));
//			else
//				position = positionDup;
//		}
//	}
//
//
//  // Path reader class used to read in source files from
//  // a pathway. The class wasn't needed as this was only
//  // done in one class so it was retooled as a method.
//	package refactory;
//	
//	import java.io.File;
//	import java.util.ArrayList;
//	import java.util.List;
//	import java.util.Stack;
//	
//	public class PathReader 
//	{
//		private File filePath;
//		
//		public PathReader(String filePath) 
//		{
//			this.filePath = new File(filePath);
//		}
//		
//		// Returns an array of file paths representing the project.
//		public String[] read() 
//		{
//			if (!this.filePath.exists() || !this.filePath.isDirectory())
//				throw new RuntimeException("Path to files does not exist.");
//	
//			List<File> files = findAllSourceFiles(this.filePath);
//			String[] fileList = new String[files.size()];
//			
//			for (int i = 0; i < files.size(); i++)
//				fileList[i] = files.get(i).getAbsolutePath();
//			
//			return fileList;
//		}
//		
//		// Returns an array of file paths representing the project.
//		// This time the path directory is passed into the method.
//		public String[] read(String pathName) 
//		{
//			File filePath = new File(pathName);
//			if (!filePath.exists() || !filePath.isDirectory())
//				throw new RuntimeException("Path to files does not exist.");
//	
//			List<File> files = findAllSourceFiles(filePath);
//			String[] fileList = new String[files.size()];
//	
//			for (int i = 0; i < files.size(); i++)
//				fileList[i] = files.get(i).getAbsolutePath();
//	
//			return fileList;
//		}
//		
//		// Extracts only the java files from the input parameter.
//		private List<File> findAllSourceFiles(File pathway) 
//		{
//			Stack<File> dirs  = new Stack<File>();
//			ArrayList<File> files = new ArrayList<File>();
//			dirs.push(pathway);
//			
//			while (dirs.size() > 0) 
//			{
//				File dir = dirs.pop();
//				File[] subfiles = dir.listFiles();
//				
//				for (File f : subfiles) 
//				{
//					if (f.isDirectory())
//						dirs.push(f);
//					else if (f.getName().endsWith(".java")) 
//						files.add(f);
//				}
//			}
//			
//			return files;
//		} 
//		
//		public void setFilePath(String pathName)
//		{
//			this.filePath = new File(pathName);
//		}
//	}
//
//
//  // Original way to make method non final without updating the change history
//	public ProblemReport analyze(int iteration, int unit, int element) 
//	{
//		// Initialise and pick the element to visit.
//		super.tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
//	
//		for (int i = 0; i < element; i++)
//		{
//			super.tw.next(MethodDeclaration.class);
//			MethodDeclaration md = (MethodDeclaration) super.tw.getProgramElement();
//			if (!mayRefactor(md))
//				i--;
//		}
//	
//		ProgramElement pe = super.tw.getProgramElement();
//		MethodDeclaration md = (MethodDeclaration) pe;
//	
//		// Find iterator in declaration list.
//		int counter = -1;
//		for (int i = 0; i < md.getDeclarationSpecifiers().size(); i++)
//			if (md.getDeclarationSpecifiers().get(i).toString().contains("Final"))
//				counter = i;
//	
//		// Construct refactoring transformation.
//		md.getDeclarationSpecifiers().remove(counter);
//		super.transformation = null;
//	
//		// Specify refactoring information for results information.
//		super.refactoringInfo = "Iteration " + iteration + ": \"Make Method Non Final\" applied at class " 
//				+ super.getFileName(getSourceFileRepository().getKnownCompilationUnits().get(unit).getName())
//				+ " to element " + pe.getClass().getSimpleName() + " (" + ((MethodDeclaration) pe).getName()
//				+ ")";
//	
//		return setProblemReport(EQUIVALENCE);
//	}
//
//
//  // Original version of this method that created the directory
//  // before the search was run. Found out that it wasn't necessary
//  // to do that first as the model would handle it automatically.
//	public String createOutputDirectory(String pathName, String name)
//	{
//		File filePath = new File(pathName);
//		String output = "./data/refactored/" + filePath.getName() + "/" + name;
//		File dir = new File(output);
//		if (!dir.exists()) 
//			dir.mkdirs();
//		
//		return output;
//	}
//
//
//	// Sorts the population by fitness using orthogonal distances between the objectives.
//	private ArrayList<RefactoringSequence> fitness(ArrayList<RefactoringSequence> population)
//	{
//		FitnessFunction ff = new FitnessFunction();
//		
//		for (int i = 0; i < population.size(); i++)
//		{
//			Metrics m = new Metrics(population.get(i).getServiceConfiguration().getSourceFileRepository().getKnownCompilationUnits());		
//			float fitness = 0.0f;
//			
//			// Finds the fitness for each objective and creates a multi-objective 
//			// fitness  value by finding the orthogonal distance between them.
//			for (int j = 0; j < this.c.length; j++)
//				fitness += Math.pow(ff.calculateScore(m, this.c[j].getConfiguration()), 2);
//			
//			if (fitness > 0)
//				fitness = (float) Math.sqrt(fitness);
//			else
//				System.out.printf("Overall Fitness is a negative value - Pythagorean distance cannot be calculated.");
//			
//			population.get(i).setFitness(fitness);
//		}
//		
//		ArrayList<RefactoringSequence> sortedPopulation = new ArrayList<RefactoringSequence>();
//		boolean lowest;
//		
//		for (int i = 0; i < population.size(); i++)
//		{
//			lowest = true;
//			
//			for (int j = 0; j < sortedPopulation.size(); j++)
//			{
//				if (sortedPopulation.get(j).getFitness() < population.get(i).getFitness())
//				{
//					sortedPopulation.add(j, population.get(i));
//					lowest = false;
//					j = sortedPopulation.size();
//				}
//			}
//			
//			if (lowest == true)
//				sortedPopulation.add(population.get(i));
//		}
//
//		sortedPopulation.subList(this.populationSize, sortedPopulation.size()).clear();
//      return sortedPopulation;
//  }
//
//
//	// Selection operator used to choose two 'parent' solutions in the current 
//	// population for crossover. The method chooses randomly between the solutions
//	// but it is more likely to choose a solution with a larger fitness value.
//	private int[] rouletteSelection(ArrayList<RefactoringSequence> population)
//	{
//		int[] parents = new int[2];
//		float fitnessSum = 0;
//		float dynamicSum = 0;
//
//		// Find the sum of all the fitness values.
//		for (int i = 0; i < population.size(); i++)
//			fitnessSum += population.get(i).getFitness();
//
//		float rouletteSelection = (float)(Math.random()*fitnessSum);
//
//		// Find the first parent iteration.
//		for (int i = 0; i < population.size(); i++)
//		{
//			dynamicSum += population.get(i).getFitness();
//
//			if (dynamicSum >= rouletteSelection)
//			{
//				parents[0] = i;
//				i = population.size();
//			}
//		}
//		
//		rouletteSelection = (float)(Math.random()*fitnessSum);
//		dynamicSum = 0;
//
//		// Find the second parent iteration.
//		// Makes sure it isn't equal to the first.
//		for (int i = 0; i < population.size(); i++)
//		{
//			dynamicSum += population.get(i).getFitness();
//
//			if (dynamicSum >= rouletteSelection)
//			{
//				if (i != parents[0])
//					parents[1] = i;
//				else if (i < (population.size() - 1))
//					parents[1] = (i + 1);
//				else
//					parents[1] = (i - 1);
//					
//				i = population.size();
//			}
//		}		
//		
//		return parents;
//	}
//
//
//  // Sorts the population so that the more fit solutions are at the front of the list.
//	private ArrayList<RefactoringSequence> sortByFitness(ArrayList<RefactoringSequence> population)
//	{
//		ArrayList<RefactoringSequence> sortedPopulation = new ArrayList<RefactoringSequence>();
//		boolean lowest;
//
//		for (int i = 0; i < population.size(); i++)
//		{
//			lowest = true;
//
//			for (int j = 0; j < sortedPopulation.size(); j++)
//			{
//				if (sortedPopulation.get(j).getFitness() > population.get(i).getFitness())
//				{
//					sortedPopulation.add(j, population.get(i));
//					lowest = false;
//					j = sortedPopulation.size();
//				}
//			}
//
//			if (lowest == true)
//				sortedPopulation.add(population.get(i));
//		}
//
//		sortedPopulation.subList(this.populationSize, sortedPopulation.size()).clear();
//		return sortedPopulation;	
//	}	
//
//
//  // Original check for whether a refactoring for one model was applicable in another.
//	// If the element provided is less than or equal to the amount of applicable refactorings of the relevant type
//	// for that class, the refactoring is applied to this model and saved in the sequence.
//	if (p2.getPositions().get(cutPoint2 + (i - cutPoint1))[1] <=
//	refactorings.get(p2.getRefactorings().get(cutPoint2 + (i - cutPoint1)))
//	.getAmount(p2.getPositions().get(cutPoint2 + (i - cutPoint1))[0]))
//	{
//		refactorings.get(p2.getRefactorings().get(cutPoint2 + (i - cutPoint1))).setServiceConfiguration(sc1);
//		refactorings.get(p2.getRefactorings().get(cutPoint2 + (i - cutPoint1)))
//		.transform(refactorings.get(p2.getRefactorings().get(cutPoint2 + (i - cutPoint1)))
//				.analyze((i + 1), p2.getPositions().get(cutPoint2 + (i - cutPoint1))[0], 
//						p2.getPositions().get(cutPoint2 + (i - cutPoint1))[1]));
//		refactoringInfo1.add(refactorings.get(p2.getRefactorings().get(cutPoint2 + (i - cutPoint1))).getRefactoringInfo());
//		c1Refactorings.add(p2.getRefactorings().get(cutPoint2 + (i - cutPoint1)));
//		c1Positions.add(p2.getPositions().get(cutPoint2 + (i - cutPoint1)));
//		sc1 = refactorings.get(p2.getRefactorings().get(cutPoint2 + (i - cutPoint1))).getServiceConfiguration();
//	}
//
//
//  // Original method to the metric values for the project.
//	protected void outputMetrics(float score, Metrics metric, boolean initial, boolean log, String pathName)
//	{
//		// Create a location for the results output.
//		String runName = String.format("%sresults.txt", pathName);
//		File dir = new File(pathName);
//		if (!dir.exists()) 
//			dir.mkdirs();
//
//		try 
//		{
//			BufferedWriter bw = new BufferedWriter(new FileWriter(runName, true));
//			
//			if (initial)
//				bw.append(String.format("\r\n\r\n======== Initial Metric Info ========"));
//			else
//				bw.append(String.format("\r\n\r\n======== Final Metric Info ========"));
//			
//			// Outputs the metric values for the project to a text file.
//			bw.append(String.format("\r\nAmount of classes in project: %d", metric.classDesignSize()));
//			bw.append(String.format("\r\nAmount of methods in project: %d", metric.numberOfMethods()));
//			bw.append(String.format("\r\nAmount of methods per class: %f", metric.methodsPerType()));
//			bw.append(String.format("\r\nRatio of interfaces to overall amount of classes: %.2f%%", metric.abstractness()));
//			bw.append(String.format("\r\nAmount of abstract classes/methods in project: %d", metric.abstractAmount()));
//			bw.append(String.format("\r\nAmount of static methods/variables in project: %d", metric.staticAmount()));
//			bw.append(String.format("\r\nAmount of final classes/methods/variables in project: %d", metric.finalAmount()));
//			bw.append(String.format("\r\nAmount of inner classes in project: %d", metric.innerClassAmount()));
//			bw.append(String.format("\r\nAmount of hierarchies in project: %d", metric.numberOfHierarchies()));
//			bw.append(String.format("\r\nAmount of ancestors per class: %f", metric.averageNumberOfAncestors()));
//			bw.append(String.format("\r\nAmount of cohesion among methods per class: %f", metric.cohesionAmongMethods()));
//			bw.append(String.format("\r\nAmount of coupling in project: %d", metric.directClassCoupling ()));
//			bw.append(String.format("\r\nAmount of child classes in project: %d", metric.childAmount()));
//			bw.append(String.format("\r\nAmount lines of code in project: %d", metric.linesOfCode()));
//			bw.append(String.format("\r\nAmount of files in project: %d", metric.fileAmount()));
//			bw.append(String.format("\r\nAmount of visibility in project: %d", metric.visibility()));
//			bw.append(String.format("\r\nAmount of public methods in project: %d", metric.classInterfaceSize ()));
//			bw.append(String.format("\r\nAccumulative ratio of private/protected attributes to overall attributes per class: %f", metric.dataAccessMetric ()));
//			bw.append(String.format("\r\nOverall score: %f", score));
//			bw.close();
//		}
//		catch (IOException e) 
//		{
//			System.out.println("\nEXCEPTION: Cannot export results to text file.");
//			System.exit(1);
//		}
//
//		if (log)
//		{
//			// Outputs the metric values for the project to the console for immediate feedback.
//			System.out.printf("\n\nAmount of classes in project: %d", metric.classDesignSize ());
//			System.out.printf("\nAmount of methods in project: %d", metric.numberOfMethods ());
//			System.out.printf("\nAmount of methods per class: %.2f", metric.methodsPerType());
//			System.out.printf("\nRatio of interfaces to overall amount of classes: %.2f%%", metric.abstractness());
//			System.out.printf("\nAmount of abstract classes/methods in project: %d", metric.abstractAmount());
//			System.out.printf("\nAmount of static methods/variables in project: %d", metric.staticAmount());
//			System.out.printf("\nAmount of final classes/methods/variables in project: %d", metric.finalAmount());
//			System.out.printf("\nAmount of inner classes in project: %d", metric.innerClassAmount());
//			System.out.printf("\nAmount of hierarchies in project: %d", metric.numberOfHierarchies());
//			System.out.printf("\nAmount of ancestors per class: %.2f", metric.averageNumberOfAncestors());
//			System.out.printf("\nAmount of cohesion among methods per class: %.2f", metric.cohesionAmongMethods());
//			System.out.printf("\nAmount of coupling in project: %d", metric.directClassCoupling());
//			System.out.printf("\nAmount of child classes in project: %d", metric.childAmount());
//			System.out.printf("\nAmount lines of code in project: %d", metric.linesOfCode());
//			System.out.printf("\nAmount of files in project: %d", metric.fileAmount());
//			System.out.printf("\nAmount of visibility in project: %d", metric.visibility());
//			System.out.printf("\nAmount of public methods in project: %d", metric.classInterfaceSize ());
//			System.out.printf("\nAccumulative ratio of private/protected attributes to overall attributes per class: %.2f", metric.dataAccessMetric ());
//			System.out.printf("\nOverall score: %.2f", score);
//		}
//	}
//
//
//	// Sorts the population so that the more fit solutions are at the front of the list.
//	// After the list is sorted, it is truncated to remove the weakest solutions.
//	// Was updated to hopefully improve garbage collection of array lists in method.
//	private ArrayList<RefactoringSequence> sort(ArrayList<RefactoringSequence> population) 
//	{
//		RefactoringSequence[] arrayPopulation = population.toArray(new RefactoringSequence[0]);
//		Arrays.sort(arrayPopulation, new FitnessComparator());
//		population.clear();
//	
//		for (RefactoringSequence s : arrayPopulation)
//			population.add(s);
//		
//		population.subList(this.populationSize, population.size()).clear();
//		return population;
//	}
//
//
// Original version of genetic algorithm with multiple models stored.
// This caused problems with larger populations/inputs but the more 
// efficient algorithm had a trade off in performance due to the 
// need to reconstruct models during mutation and printing the output.
//public class GeneticAlgorithmSearch extends Search
//{
//	private String[] sourceFiles;
//	
//	private boolean printAll;	
//	private int generations;
//	private int populationSize;
//	private float crossoverProbability;
//	private float mutationProbability;
//	private int initialRefactoringRange = 10;
//
//	public GeneticAlgorithmSearch(CrossReferenceServiceConfiguration sc, Configuration c, String[] sourceFiles) 
//	{
//		super(sc, c);
//		this.sourceFiles = sourceFiles;
//		this.printAll = true;
//		this.generations = 5;
//		this.populationSize = 10;
//		this.crossoverProbability = 0.5f;
//		this.mutationProbability = 0.5f;	
//	}
//
//	public GeneticAlgorithmSearch(CrossReferenceServiceConfiguration sc, Configuration c, String[] sourceFiles,
//								  boolean printAll, int generations, int populationSize, float crossoverProbability, float mutationProbability) 
//	{
//		super(sc, c);	
//		this.sourceFiles = sourceFiles;
//		this.printAll = printAll;
//		this.generations = generations;
//		this.populationSize = populationSize;
//		this.crossoverProbability = crossoverProbability;
//		this.mutationProbability = mutationProbability;
//	}
//
//	// Executes the Genetic Algorithm.
//	public void run() 
//	{
//		String runInfo = String.format("Search: Genetic Algorithm\r\nGenerations: %d\r\nPopulation Size: %d"
//									   + "\r\nCrossover Probability: %f\r\nMutation Probability: %f",
//				                       this.generations, this.populationSize, this.crossoverProbability, this.mutationProbability);
//		Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());	
//		FitnessFunction ff = new FitnessFunction();
//		float benchmark = ff.calculateScore(m, super.c.getConfiguration());
//		
//		if (this.printAll)
//		{
//			for (int i = 1; i <= populationSize; i++)
//			{
//				outputSearchInfo(super.resultsPath, i, runInfo);
//				outputMetrics(benchmark, m, true, false, i, super.resultsPath);
//			}
//		}
//		else
//		{
//			super.outputSearchInfo(super.resultsPath, runInfo);
//			super.outputMetrics(benchmark, m, true, true, super.resultsPath);
//		}
//		
//		long timeTaken, startTime = System.currentTimeMillis();
//		double time;
//		
//		System.out.printf("\n\nCreating Initial Population...");
//		ArrayList<RefactoringSequence> population = new ArrayList<RefactoringSequence>(this.populationSize);
//		ArrayList<RefactoringSequence> newGeneration = new ArrayList<RefactoringSequence>();
//		population = initialize();
//		
//		// At each generation, crossover is applied to produce a number of child solutions.
//		// Then, mutation is applied amongst these new solutions to introduce variety.
//		// Fitness is measured for the new solutions and they are sorted accordingly.
//		for (int i = 1; i <= this.generations; i++)
//		{
//			System.out.printf("\n\nIteration %d:", i);
//			newGeneration = new ArrayList<RefactoringSequence>();
//			
//			// Crossover is always done once for each generation but beyond that the
//			// amount of times it is executed depends on the crossover probability.
//			do
//			{
//				System.out.printf("\nCrossover...");
//				int[] parents = rankSelection(population.size(), 2);
//				newGeneration.addAll(crossover(population.get(parents[0]), population.get(parents[1])));
//			}
//			while (Math.random() < this.crossoverProbability);
//			
//			// The amount of times if, at all, mutation is applied depends on the
//			// mutation probability. This will mutate the children of the current generation.
//			while (Math.random() < this.mutationProbability)
//			{
//				System.out.printf("\nMutation...");
//				int randomChild = (int)(Math.random()*newGeneration.size());
//				newGeneration.set(randomChild, mutation(newGeneration.get(randomChild)));
//			}
//			
//			// Increases the size of the array list so it can add the new solutions before 
//			// calculating the fitnesses and being trimmed back down to the population size.
//			newGeneration.trimToSize();
//			population.ensureCapacity(this.populationSize + newGeneration.size());
//			
//			// On the first generation the initial population 
//			// and children are all measured for fitness.
//			if (i == 1)
//			{
//				population.addAll(newGeneration);
//				population = fitness(population);
//			}
//			// Only the new solutions need to be measured for fitness.
//			else
//			{
//				newGeneration = fitness(newGeneration);
//				population.addAll(newGeneration);
//			}
//
//			// Sort new population by fitness and truncate it to remove weakest solutions.
//			population = new ArrayList<RefactoringSequence>(sort(population));
//			population.trimToSize();
//		}
//		
//		newGeneration = null;
//		timeTaken = System.currentTimeMillis() - startTime;
//		time = timeTaken / 1000.0;
//		
//		if (this.printAll)
//		{
//			for (int i = 0; i < population.size(); i++)
//			{
//				outputRefactoringInfo(super.resultsPath, time, population.get(i).getFitness() - benchmark, i + 1, population.get(i).getRefactoringInfo());
//				m = new Metrics(population.get(i).getServiceConfiguration().getSourceFileRepository().getKnownCompilationUnits());	
//				outputMetrics(population.get(i).getFitness(), m, false, false, i + 1, super.resultsPath);
//			}
//				
//			System.out.printf("\n\nPrinting Population");
//			
//			for (int i = 0; i < population.size(); i++)
//			{
//				String newOutputPath = super.sc.getProjectSettings().getProperty(PropertyNames.OUTPUT_PATH) + "s/Solution" + (i + 1);
//				population.get(i).getServiceConfiguration().getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, newOutputPath);
//				super.print(population.get(i).getServiceConfiguration().getSourceFileRepository());	
//			}
//		}
//		else
//		{
//			outputRefactoringInfo(super.resultsPath, time, population.get(0).getFitness() - benchmark, -1, population.get(0).getRefactoringInfo());
//			m = new Metrics(population.get(0).getServiceConfiguration().getSourceFileRepository().getKnownCompilationUnits());	
//			super.outputMetrics(population.get(0).getFitness(), m, false, true, super.resultsPath);
//			System.out.printf("\n\nScore has improved overall by %f", population.get(0).getFitness() - benchmark);
//			System.out.printf("\nPrinting Top Solution");
//			super.print(population.get(0).getServiceConfiguration().getSourceFileRepository());	
//		}
//		
//		// Output time taken to console and refactoring information to results file.
//		population = null;
//		timeTaken = System.currentTimeMillis() - startTime;
//		time = timeTaken / 1000.0;
//		System.out.printf("\nOverall time taken for search: %.2fs", time);
//	}
//	
//	// Creates an initial population of refactoring solutions at random.
//	// Using the initial refactoring range a random amount of refactorings will
//	// be applied in that range to create a possible solution and this will be 
//	// repeated until the population size has been satisfied.
//	private ArrayList<RefactoringSequence> initialize()
//	{
//		ArrayList<Refactoring> refactorings = new ArrayList<Refactoring>(super.c.getRefactorings().size());
//		refactorings = super.c.getRefactorings();
//		ArrayList<RefactoringSequence> population = new ArrayList<RefactoringSequence>(this.populationSize);
//		CrossReferenceServiceConfiguration[] scArray = new CrossReferenceServiceConfiguration[this.populationSize];
//		
//		for (int i = 0; i < this.populationSize; i++)
//		{
//			// Create copy of the initial program model.
//			CrossReferenceServiceConfiguration scCopy = new CrossReferenceServiceConfiguration();
//
//			try 
//			{
//				// Read the original input.
//				scCopy.getSourceFileRepository().getCompilationUnitsFromFiles(this.sourceFiles);
//			}
//			catch (ParserException e) 
//			{
//				System.out.println("\nEXCEPTION: Cannot read input.");
//				System.exit(1);
//			}
//			
//			// Set up initial properties of service configuration.
//			// Saves new model into array so it can be updated and passed to the relevant refactoring.
//			scCopy.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, super.sc.getProjectSettings().getProperty(PropertyNames.INPUT_PATH));
//			scCopy.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, super.sc.getProjectSettings().getProperty(PropertyNames.OUTPUT_PATH));
//			scCopy.getProjectSettings().ensureSystemClassesAreInPath();
//			scArray[i] = scCopy;
//
//			// Applies random refactorings to each solution to create an initial population.
//			// The amount of refactorings applied in each case is chosen randomly within the range supplied.
//			int refactoringAmount = ((int)(Math.random() * this.initialRefactoringRange)) + 1;
//			ArrayList<int[]> posSequence = new ArrayList<int[]>(refactoringAmount);
//			ArrayList<Integer> refSequence = new ArrayList<Integer>(refactoringAmount);
//			ArrayList<Integer> IDSequence = new ArrayList<Integer>(refactoringAmount);
//			ArrayList<String> refactoringInfo = new ArrayList<String>(refactoringAmount);
//			
//			for (int j = 0; j < refactoringAmount; j++)
//			{				
//				int[] result = randomRefactoring(scArray[i]);
//				int[] position = {result[1], result[2]};
//
//				if (result[0] == -1)
//				{
//					System.out.printf("\nThere are no refactorings available for the rest of the sequence.");
//					j = refactoringAmount;
//				}
//				else
//				{
//					IDSequence.add(refactorings.get(result[0]).getID(position[0], position[1]));
//					refactorings.get(result[0]).transform(refactorings.get(result[0]).analyze((j + 1), position[0], position[1]));
//					refactoringInfo.add(refactorings.get(result[0]).getRefactoringInfo());
//					refSequence.add(result[0]);
//					posSequence.add(position);
//					scArray[i] = refactorings.get(result[0]).getServiceConfiguration();
//				}
//			}
//			
//			refSequence.trimToSize();
//			posSequence.trimToSize();
//			IDSequence.trimToSize();
//			refactoringInfo.trimToSize();
//			population.add(new RefactoringSequence(scArray[i], refSequence, posSequence, IDSequence, refactoringInfo));
//		}
//		
//		return population;
//	}
//	
//	// Method uses single-point crossover. For each refactoring sequence passed in,
//	// a cut point is randomly chosen. The segments of each sequence are then switched to
//	// create two child solutions. After this the refactorings are applied for each child
//	// and any inapplicable refactorings are removed form the new sequences.
//	private ArrayList<RefactoringSequence> crossover(RefactoringSequence p1, RefactoringSequence p2)
//	{
//		ArrayList<RefactoringSequence> children = new ArrayList<RefactoringSequence>(2);
//		ArrayList<Refactoring> refactorings = new ArrayList<Refactoring>(super.c.getRefactorings().size());
//		refactorings = super.c.getRefactorings();
//		int cutPoint1 = ((int)(Math.random() * (p1.getRefactorings().size() - 1))) + 1;
//		int cutPoint2 = ((int)(Math.random() * (p2.getRefactorings().size() - 1))) + 1;
//		int elementPosition, i2;
//		
//		int c1Size = cutPoint1 + (p2.getRefactorings().size() - cutPoint2);
//		ArrayList<Integer> c1Refactorings = new ArrayList<Integer>(c1Size);
//		ArrayList<int[]> c1Positions = new ArrayList<int[]>(c1Size);
//		ArrayList<Integer> c1IDs = new ArrayList<Integer>(c1Size);
//		ArrayList<String> refactoringInfo1 = new ArrayList<String>(c1Size);
//		
//		// Create copies of the initial program model.
//		CrossReferenceServiceConfiguration sc1 = new CrossReferenceServiceConfiguration();
//		CrossReferenceServiceConfiguration sc2 = new CrossReferenceServiceConfiguration();
//
//		try 
//		{
//			// Read the original input.
//			sc1.getSourceFileRepository().getCompilationUnitsFromFiles(this.sourceFiles);
//			sc2.getSourceFileRepository().getCompilationUnitsFromFiles(this.sourceFiles);
//		}
//		catch (ParserException e) 
//		{
//			System.out.println("\nEXCEPTION: Cannot read input.");
//			System.exit(1);
//		}
//		
//		// Set up initial properties of service configurations.
//		sc1.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, super.sc.getProjectSettings().getProperty(PropertyNames.INPUT_PATH));
//		sc1.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, super.sc.getProjectSettings().getProperty(PropertyNames.OUTPUT_PATH));
//		sc1.getProjectSettings().ensureSystemClassesAreInPath();
//		sc2.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, super.sc.getProjectSettings().getProperty(PropertyNames.INPUT_PATH));
//		sc2.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, super.sc.getProjectSettings().getProperty(PropertyNames.OUTPUT_PATH));
//		sc2.getProjectSettings().ensureSystemClassesAreInPath();
//
//		for (int i = 0; i < c1Size; i++)
//		{				
//			// The first sequence in each solution will be applicable so 
//			// refactorings can be applied without checking.
//			if (i < cutPoint1)
//			{	
//				refactorings.get(p1.getRefactorings().get(i)).setServiceConfiguration(sc1);
//				refactorings.get(p1.getRefactorings().get(i)).transform(refactorings.get(p1.getRefactorings().get(i))
//						    .analyze((i + 1), p1.getPositions().get(i)[0], p1.getPositions().get(i)[1]));
//				refactoringInfo1.add(refactorings.get(p1.getRefactorings().get(i)).getRefactoringInfo());
//				c1Refactorings.add(p1.getRefactorings().get(i));
//				c1Positions.add(p1.getPositions().get(i));	
//				c1IDs.add(p1.getIDs().get(i));
//				sc1 = refactorings.get(p1.getRefactorings().get(i)).getServiceConfiguration();
//			}
//			// For the second sequence, a check will have 
//			// to be made for each contiguous refactoring.
//			else
//			{
//				elementPosition = -1;
//				i2 = cutPoint2 + (i - cutPoint1);
//				refactorings.get(p2.getRefactorings().get(i2)).setServiceConfiguration(sc1);
//				
//				// Checks for the relevant program element by comparing the names of 
//				// each applicable element in the class with the desired element name.
//				for (int j = 1; j <= refactorings.get(p2.getRefactorings().get(i2)).getAmount(p2.getPositions().get(i2)[0]); j++)
//				{					
//					if (refactorings.get(p2.getRefactorings().get(i2)).getID(p2.getPositions().get(i2)[0], j) == (p2.getIDs().get(i2)))
//					{
//						elementPosition = j;
//						break;
//					}
//				}
//				
//				// If the element exists and can be refactored.
//				if (elementPosition != -1)
//				{
//					refactorings.get(p2.getRefactorings().get(i2)).transform(refactorings.get(p2.getRefactorings().get(i2))
//							    .analyze((i + 1), p2.getPositions().get(i2)[0], elementPosition));
//					refactoringInfo1.add(refactorings.get(p2.getRefactorings().get(i2)).getRefactoringInfo());
//					c1Refactorings.add(p2.getRefactorings().get(i2));
//					c1Positions.add(new int[] {p2.getPositions().get(i2)[0], elementPosition});
//					c1IDs.add(p2.getIDs().get(i2));
//					sc1 = refactorings.get(p2.getRefactorings().get(i2)).getServiceConfiguration();
//				}
//			}
//		}
//
//		c1Refactorings.trimToSize();
//		c1Positions.trimToSize();
//		c1IDs.trimToSize();
//		refactoringInfo1.trimToSize();
//		children.add(new RefactoringSequence(sc1, c1Refactorings, c1Positions, c1IDs, refactoringInfo1));
//		
//		int c2Size = cutPoint2 + (p1.getRefactorings().size() - cutPoint1);
//		ArrayList<Integer> c2Refactorings = new ArrayList<Integer>(c2Size);
//		ArrayList<int[]> c2Positions = new ArrayList<int[]>(c2Size);
//		ArrayList<Integer> c2IDs = new ArrayList<Integer>(c2Size);
//		ArrayList<String> refactoringInfo2 = new ArrayList<String>(c2Size);
//					
//		for (int i = 0; i < c2Size; i++)
//		{			
//			// The first sequence in each solution will be applicable so 
//			// refactorings can be applied without checking.
//			if (i < cutPoint2)
//			{
//				refactorings.get(p2.getRefactorings().get(i)).setServiceConfiguration(sc2);
//				refactorings.get(p2.getRefactorings().get(i)).transform(refactorings.get(p2.getRefactorings().get(i))
//						    .analyze((i + 1), p2.getPositions().get(i)[0], p2.getPositions().get(i)[1]));
//				refactoringInfo2.add(refactorings.get(p2.getRefactorings().get(i)).getRefactoringInfo());
//				c2Refactorings.add(p2.getRefactorings().get(i));
//				c2Positions.add(p2.getPositions().get(i));
//				c2IDs.add(p2.getIDs().get(i));
//				sc2 = refactorings.get(p2.getRefactorings().get(i)).getServiceConfiguration();
//			}	
//			// For the second sequence, a check will have 
//			// to be made for each contiguous refactoring.
//			else
//			{
//				elementPosition = -1;
//				i2 = cutPoint1 + (i - cutPoint2);
//				refactorings.get(p1.getRefactorings().get(i2)).setServiceConfiguration(sc2);
//				
//				// Checks for the relevant program element by comparing the names of 
//				// each applicable element in the class with the desired element name.
//				for (int j = 1; j <=  refactorings.get(p1.getRefactorings().get(i2)).getAmount(p1.getPositions().get(i2)[0]); j++)
//				{
//					if (refactorings.get(p1.getRefactorings().get(i2)).getID(p1.getPositions().get(i2)[0], j) == (p1.getIDs().get(i2)))
//					{
//						elementPosition = j;
//						break;
//					}
//				}
//				
//				// If the element exists and can be refactored.
//				if (elementPosition != -1)
//				{
//					refactorings.get(p1.getRefactorings().get(i2)).transform(refactorings.get(p1.getRefactorings().get(i2))
//							    .analyze((i + 1), p1.getPositions().get(i2)[0], elementPosition));
//					refactoringInfo2.add(refactorings.get(p1.getRefactorings().get(i2)).getRefactoringInfo());
//					c2Refactorings.add(p1.getRefactorings().get(i2));
//					c2Positions.add(new int[] {p1.getPositions().get(i2)[0], elementPosition});
//					c2IDs.add(p1.getIDs().get(i2));
//					sc2 = refactorings.get(p1.getRefactorings().get(i2)).getServiceConfiguration();
//				}
//			}
//		}
//		
//		c2Refactorings.trimToSize();
//		c2Positions.trimToSize();
//		c2IDs.trimToSize();
//		refactoringInfo2.trimToSize();
//		children.add(new RefactoringSequence(sc2, c2Refactorings, c2Positions, c2IDs, refactoringInfo2));
//		
//		return children;
//	}
//	
//	// Applies a random refactoring to the end of the refactoring sequence passed in.
//	// If the refactoring is not applicable it will keep trying until an applicable	
//	// refactoring is found or it runs out of possibilities. In this case the original 
//	// sequence is returned.
//	private RefactoringSequence mutation(RefactoringSequence p)
//	{			
//		int[] result = randomRefactoring(p.getServiceConfiguration());
//		int[] position = {result[1], result[2]};
//		
//		// Applies refactoring to model and adds it to the sequence.
//		if (result[0] != -1)
//		{			
//			ArrayList<Refactoring> refactorings = new ArrayList<Refactoring>(super.c.getRefactorings().size());
//			refactorings = super.c.getRefactorings();
//			ArrayList<Integer> refSequence = new ArrayList<Integer>(p.getRefactorings().size() + 1);
//			refSequence = p.getRefactorings();
//			ArrayList<int[]> posSequence = new ArrayList<int[]>(p.getPositions().size() + 1);
//			posSequence = p.getPositions();
//			ArrayList<Integer> IDSequence = new ArrayList<Integer>(p.getIDs().size() + 1);
//			IDSequence = p.getIDs();
//			ArrayList<String> refactoringInfo = new ArrayList<String>(p.getRefactoringInfo().size() + 1);
//			refactoringInfo = p.getRefactoringInfo();
//			
//			refSequence.add(result[0]);
//			posSequence.add(position);
//			IDSequence.add(refactorings.get(result[0]).getID(position[0], position[1]));
//			refactorings.get(result[0]).transform(refactorings.get(result[0]).analyze((refSequence.size()), position[0], position[1]));
//			refactoringInfo.add(refactorings.get(result[0]).getRefactoringInfo());
//			p.setRefactorings(refSequence);
//			p.setPositions(posSequence);
//			p.setIDs(IDSequence);
//			p.setRefactoringInfo(refactoringInfo);
//			p.setServiceConfiguration(refactorings.get(result[0]).getServiceConfiguration());
//		}
//		
//		return p;
//	}
//
//	// Calculates the fitness values for the solutions passed in.
//	private ArrayList<RefactoringSequence> fitness(ArrayList<RefactoringSequence> population)
//	{
//		FitnessFunction ff = new FitnessFunction();
//		
//		for (int i = 0; i < population.size(); i++)
//		{
//			Metrics m = new Metrics(population.get(i).getServiceConfiguration().getSourceFileRepository().getKnownCompilationUnits());		
//			population.get(i).setFitness(ff.calculateScore(m, super.c.getConfiguration()));
//		}
//		
//		return population;	
//	}
//	
//	// Sorts the population so that the more fit solutions are at the front of the list.
//	// After the list is sorted, it is truncated to remove the weakest solutions.
//	private ArrayList<RefactoringSequence> sort(ArrayList<RefactoringSequence> population) 
//	{
//		RefactoringSequence[] arrayPopulation = population.toArray(new RefactoringSequence[0]);
//		Arrays.sort(arrayPopulation, new FitnessComparator());
//		ArrayList<RefactoringSequence> sortedPopulation = new ArrayList<RefactoringSequence>(this.populationSize);
//		
//		for (int i = 0; i < this.populationSize; i++)
//			sortedPopulation.add(arrayPopulation[i]);
//		
//		population = null;
//		arrayPopulation = null;
//		return sortedPopulation;
//	}
//
//	// This inner class allows sorting by fitness so that the more fit solutions are at the front of the list.
//	private static class FitnessComparator implements Comparator<RefactoringSequence> 
//	{
//		// Compares the two specified individuals using the fitness operator.
//		// Returns -1, 0 or 1 as the first argument is greater than, equal to, or less than the second.
//		public int compare(RefactoringSequence s1, RefactoringSequence s2) 
//		{   
//			if (s1.getFitness() > s2.getFitness())
//				return -1;
//			else if (s1.getFitness() < s2.getFitness())
//				return 1;
//			else
//				return 0;
//		}
//	}
//	
//	// Finds a random available refactoring in the specified model and passes back the
//	// refactoring used and the position of the applicable program element in the model.
//	private int[] randomRefactoring(CrossReferenceServiceConfiguration sc)
//	{
//		ArrayList<Refactoring> refactorings = new ArrayList<Refactoring>(super.c.getRefactorings().size());
//		refactorings = super.c.getRefactorings();
//		int[] position = new int[2];
//		int r = -1;
//
//		// Find element to refactor.
//		if (refactorings.size() > 0)
//		{
//			r = (int)(Math.random() * refactorings.size());
//			refactorings.get(r).setServiceConfiguration(sc);
//			position = super.randomElement(refactorings.get(r));
//		}
//		else
//		{
//			position[0] = -1;
//			position[1] = -1;
//		}
//
//		// Checks in case no elements are returned for the refactoring.
//		// Will check again for each available refactoring in the search and 
//		// if there are still no applicable elements returned the search will terminate.
//		if ((position[0] == -1) && (position[1] == -1))
//		{
//			int exclude = r;
//			for (r = 0; r < refactorings.size(); r++)
//			{
//				// Stops the loop from repeating the check for the previous refactoring.
//				if ((r == exclude) && ((r + 1) < refactorings.size()))
//					r++;
//				else if (r == exclude)
//					break;
//
//				refactorings.get(r).setServiceConfiguration(sc);
//				position = super.randomElement(refactorings.get(r));
//
//				if ((position[0] != -1) && (position[1] != -1))
//					break;
//			}
//
//			if ((position[0] == -1) && (position[1] == -1))
//				r = -1;
//		}
//		
//		return new int[]{r, position[0], position[1]};
//	}
//	
//	// Uses Roulette Selection approach but instead of appropriating the fitness values of the solutions 
//	// it creates manually more standard proportions in case single fitness values are not available. 
//	// sp is the selective pressure and has been added as an input parameter in case it is desired to change it.
//	private int[] rankSelection(int populationSize, float sp)
//	{
//		float[] rankProportions = new float[populationSize];
//		int[] parents = new int[2];
//		float fitnessSum = 0;
//		float dynamicSum = 0;
//
//		// Formula creates a balanced set of proportions for each rank.
//		// This overcomes the scaling problems of the proportional fitness assignment.
//		// The linear ranking formula takes in sp values in the range (1,2]. 
//		for (int i = 0; i < populationSize; i++)
//		{
//			rankProportions[i] = (2 - sp) + 2 * (sp - 1) * (((populationSize - 1) - i)/(populationSize - 1));
//			fitnessSum += rankProportions[i];
//		}
//
//		float rouletteSelection = (float)(Math.random()*fitnessSum);
//
//		// Find the first parent iteration.
//		for (int i = 0; i < populationSize; i++)
//		{
//			dynamicSum += rankProportions[i];
//
//			if (dynamicSum >= rouletteSelection)
//			{
//				parents[0] = i;
//				i = populationSize;
//			}
//		}
//
//		rouletteSelection = (float)(Math.random()*fitnessSum);
//		dynamicSum = 0;
//
//		// Find the second parent iteration.
//		for (int i = 0; i < populationSize; i++)
//		{
//			dynamicSum += rankProportions[i];
//
//			if (dynamicSum >= rouletteSelection)
//			{
//				if (i != parents[0])
//					parents[1] = i;
//				else if (i < (populationSize - 1))
//					parents[1] = (i + 1);
//				else
//					parents[1] = (i - 1);
//
//				i = populationSize;
//			}
//		}		
//
//		return parents;
//	}
//
//	// Output search information to results file.
//	// Can be used for a population of solutions to generate separate results files.
//	private void outputSearchInfo(String pathName, int solution, String runInfo)
//	{
//		pathName = pathName.substring(0, (pathName.length() - 1));
//		pathName += "s/";
//		String runName = String.format("%sresultsSolution%d.txt", pathName, solution);
//		File dir = new File(pathName);
//		if (!dir.exists()) 
//			dir.mkdirs();
//
//		try 
//		{
//			BufferedWriter bw = new BufferedWriter(new FileWriter(runName, false));
//			bw.write(String.format("======== Search Information ========"));
//			bw.write(String.format("\r\n%s", runInfo));
//			bw.close();
//		}
//		catch (IOException e) 
//		{
//			System.out.println("\nEXCEPTION: Cannot export results to text file.");
//			System.exit(1);
//		}
//	}
//	
//	// Output refactoring information to results file for a solution.
//	// Can be used for when printing out the whole population.
//	private void outputRefactoringInfo(String pathName, double time, double qualityGain, int solution, ArrayList<String> refactoringInfo)
//	{	
//		String runName;
//
//		// Create a location for the results output.
//		if (solution == -1)
//			runName = String.format("%sresults.txt", pathName);
//		else
//		{
//			pathName = pathName.substring(0, (pathName.length() - 1));
//			pathName += "s/";
//			runName = String.format("%sresultsSolution%d.txt", pathName, solution);
//		}
//
//		File dir = new File(pathName);
//		if (!dir.exists()) 
//			dir.mkdirs();
//
//		try 
//		{
//			BufferedWriter bw = new BufferedWriter(new FileWriter(runName, true));
//			bw.append("\r\n\r\n======== Applied Refactorings ========");
//
//			for (int i = 0; i < refactoringInfo.size(); i++) 
//				bw.append(String.format("\r\n%s", refactoringInfo.get(i)));
//
//			bw.append(String.format("\r\n\r\nScore has improved overall by %f", qualityGain));
//			bw.append(String.format("\r\nTime taken to refactor: %.2fs", time));
//			bw.close();
//		}
//		catch (IOException e) 
//		{
//			System.out.println("\nEXCEPTION: Cannot export results to text file.");
//			System.exit(1);
//		}
//	}
//
//	// Outputs the metric values for a solution.
//	// Can be used for when printing out the whole population.
//	private void outputMetrics(float score, Metrics metric, boolean initial, boolean log, int solution, String pathName)
//	{
//		FitnessFunction ff = new FitnessFunction();
//		String[] outputs = ff.createOutput(metric, super.c.getConfiguration());
//		
//		// Create a location for the results output.
//		pathName = pathName.substring(0, (pathName.length() - 1));
//		pathName += "s/";
//		String runName = String.format("%sresultsSolution%d.txt", pathName, solution);
//		File dir = new File(pathName);
//		if (!dir.exists()) 
//			dir.mkdirs();
//
//		try 
//		{
//			BufferedWriter bw = new BufferedWriter(new FileWriter(runName, true));
//
//			if (initial)
//				bw.append(String.format("\r\n\r\n======== Initial Metric Info ========"));
//			else
//				bw.append(String.format("\r\n\r\n======== Final Metric Info ========"));
//
//			// Outputs the metric values for the project to a text file.
//			for (int i = 0; i < outputs.length; i++)
//				bw.append("\r\n" + outputs[i]);
//			
//			bw.append(String.format("\r\nOverall fitness function score: %f", score));
//			bw.close();
//		}
//		catch (IOException e) 
//		{
//			System.out.println("\nEXCEPTION: Cannot export results to text file.");
//			System.exit(1);
//		}
//
//		if (log)
//		{
//			// Outputs the metric values for the project to the console for immediate feedback.
//			System.out.printf("\n");
//			
//			for (int i = 0; i < outputs.length; i++)
//			{
//				if(outputs[i].charAt(outputs[i].length() - 7) == '.')
//					outputs[i] = outputs[i].substring(0, outputs[i].length() - 4);
//				
//				System.out.printf("\n%s", outputs[i]);
//			}
//			
//			System.out.printf("\nOverall fitness function score: %.2f", score);
//		}
//	}
//}
//
//
// // Original version of multi-objective genetic algorithm with multiple models stored.
// // This caused problems with larger populations/inputs but the more 
// // efficient algorithm had a trade off in performance due to the 
// // need to reconstruct models duting mutation and printing the output.
// // The improved algorithm may possibly have saved performance by calculating the 
// // fitness values up front, avoiding the need to repeat fitness calculations.
//public class MultiObjectiveSearch
//{
//	private Configuration[] c;
//	private String[] sourceFiles;
//	private String inputPath;
//	private String outputPath;
//		
//	private int generations;
//	private int populationSize;
//	private float crossoverProbability;
//	private float mutationProbability;
//	private int initialRefactoringRange = 10;
//	private String resultsPath = "./results/MOGeneticAlgorithm/";
//
//	public MultiObjectiveSearch(Configuration[] c, String[] sourceFiles, String inputPath, String outputPath) 
//	{
//		this.c = c;
//		this.sourceFiles = sourceFiles;
//		this.inputPath = inputPath;
//		this.outputPath = outputPath;
//		this.generations = 5;
//		this.populationSize = 10;
//		this.crossoverProbability = 0.5f;
//		this.mutationProbability = 0.5f;
//	}
//
//	public MultiObjectiveSearch(Configuration[] c, String[] sourceFiles, String inputPath, String outputPath, 
//								int generations, int populationSize, float crossoverProbability, float mutationProbability) 
//	{
//		this.c = c;
//		this.sourceFiles = sourceFiles;
//		this.inputPath = inputPath;
//		this.outputPath = outputPath;
//		this.generations = generations;
//		this.populationSize = populationSize;
//		this.crossoverProbability = crossoverProbability;
//		this.mutationProbability = mutationProbability;
//	}
//
//	// Executes the Genetic Algorithm.
//	public void run() 
//	{
//		String runInfo = String.format("Search: Multi-Objective Genetic Algorithm\r\nGenerations: %d\r\nPopulation Size: %d"
//				+ "\r\nCrossover Probability: %f\r\nMutation Probability: %f",
//				this.generations, this.populationSize, this.crossoverProbability, this.mutationProbability);
//		
//		for (int i = 1; i <= populationSize; i++)
//			outputSearchInfo(this.resultsPath, i, runInfo);
//	
//		long timeTaken, startTime = System.currentTimeMillis();
//		double time;
//
//		System.out.printf("\n\nCreating Initial Population...");
//		ArrayList<RefactoringSequence> population = new ArrayList<RefactoringSequence>(this.populationSize);
//		ArrayList<RefactoringSequence> newGeneration = new ArrayList<RefactoringSequence>();
//		population = fitness(initialize());
//		
//		// At each generation, crossover is applied to produce a number of child solutions.
//		// Then, mutation is applied amongst these new solutions to introduce variety.
//		// Fitness is measured for the new solutions and they are sorted accordingly.
//		for (int i = 1; i <= this.generations; i++)
//		{
//			System.out.printf("\n\nIteration %d:", i);
//			newGeneration = new ArrayList<RefactoringSequence>();
//			
//			// Crossover is always done once for each generation but beyond that the
//			// amount of times it is executed depends on the crossover probability.
//			do
//			{
//				System.out.printf("\nCrossover...");
//				int randomS1, randomS2;
//				RefactoringSequence[] parents = new RefactoringSequence[2];
//				
//				for (int j = 0; j < 2; j++)
//				{
//					randomS1 = (int)(Math.random()*population.size());
//					
//					do 
//						randomS2 = (int)(Math.random()*population.size());
//					while (randomS2 == randomS1);
//
//					parents[j] = binaryTournament(population.get(randomS1), population.get(randomS2));
//				}
//				
//				newGeneration.addAll(crossover(parents[0], parents[1]));
//			}
//			while (Math.random() < this.crossoverProbability);
//			
//			// The amount of times if, at all, mutation is applied depends on the
//			// mutation probability. This will mutate the children of the current generation.
//			while (Math.random() < this.mutationProbability)
//			{
//				System.out.printf("\nMutation...");
//				int randomChild = (int)(Math.random()*newGeneration.size());
//				newGeneration.set(randomChild, mutation(newGeneration.get(randomChild)));
//			}
//			
//			// The current population is measured and sorted accordingly.
//			System.out.printf("\nFitness...");
//			newGeneration.trimToSize();
//			population.ensureCapacity(this.populationSize + newGeneration.size());
//			population.addAll(newGeneration);
//			population = new ArrayList<RefactoringSequence>(fitness(population));
//			population.trimToSize();
//		}
//		
//		newGeneration = null;
//		timeTaken = System.currentTimeMillis() - startTime;
//		time = timeTaken / 1000.0;
//		Metrics m = new Metrics(population.get(0).getServiceConfiguration().getSourceFileRepository().getKnownCompilationUnits());
//		FitnessFunction ff = new FitnessFunction();
//		float best[] = new float[this.c.length];
//		
//		for (int i = 0; i < population.size(); i++)
//		{
//			m = new Metrics(population.get(i).getServiceConfiguration().getSourceFileRepository().getKnownCompilationUnits());	
//
//			for (int j = 0; j < this.c.length; j++)
//				best[j] = ff.calculateScore(m, this.c[j].getConfiguration());
//
//			outputRefactoringInfo(this.resultsPath, time, i + 1, population.get(i).getRefactoringInfo());
//			outputMetrics(best, false, false, i + 1, this.resultsPath);
//		}
//
//		System.out.printf("\n\nPrinting Population");
//
//		for (int i = 0; i < population.size(); i++)
//		{
//			String newOutputPath = this.outputPath + "s/Solution" + (i + 1);
//			population.get(i).getServiceConfiguration().getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, newOutputPath);
//			print(population.get(i).getServiceConfiguration().getSourceFileRepository());	
//		}
//
//		// Output time taken to console and refactoring information to results file.
//		population = null;
//		timeTaken = System.currentTimeMillis() - startTime;
//		time = timeTaken / 1000.0;
//		System.out.printf("\nOverall time taken for search: %.2fs", time);
//	}
//	
//	// Creates an initial population of refactoring solutions at random.
//	// Using the initial refactoring range a random amount of refactorings will
//	// be applied in that range to create a possible solution and this will be 
//	// repeated until the population size has been satisfied.
//	private ArrayList<RefactoringSequence> initialize()
//	{
//		ArrayList<Refactoring> refactorings = new ArrayList<Refactoring>(this.c[0].getRefactorings().size());
//		refactorings = this.c[0].getRefactorings();
//		ArrayList<RefactoringSequence> population = new ArrayList<RefactoringSequence>(this.populationSize);
//		CrossReferenceServiceConfiguration[] scArray = new CrossReferenceServiceConfiguration[this.populationSize];
//		
//		for (int i = 0; i < this.populationSize; i++)
//		{
//			// Create copy of the initial program model.
//			CrossReferenceServiceConfiguration scCopy = new CrossReferenceServiceConfiguration();
//
//			try 
//			{
//				// Read the original input.
//				scCopy.getSourceFileRepository().getCompilationUnitsFromFiles(this.sourceFiles);
//			}
//			catch (ParserException e) 
//			{
//				System.out.println("\nEXCEPTION: Cannot read input.");
//				System.exit(1);
//			}
//			
//			// Set up initial properties of service configuration.
//			// Saves new model into array so it can be updated and passed to the relevant refactoring.
//			scCopy.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, this.inputPath);
//			scCopy.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, this.outputPath);
//			scCopy.getProjectSettings().ensureSystemClassesAreInPath();
//			scArray[i] = scCopy;
//			
//			Metrics m = new Metrics(scCopy.getSourceFileRepository().getKnownCompilationUnits());	
//			FitnessFunction ff = new FitnessFunction();
//			float benchmark[] = new float[this.c.length];
//			
//			for (int j = 0; j < this.c.length; j++)
//				benchmark[j] = ff.calculateScore(m, this.c[j].getConfiguration());
//
//			outputMetrics(benchmark, true, false, i + 1, this.resultsPath);
//
//			// Applies random refactorings to each solution to create an initial population.
//			// The amount of refactorings applied in each case is chosen randomly within the range supplied.
//			int refactoringAmount = ((int)(Math.random() * this.initialRefactoringRange)) + 1;
//			ArrayList<int[]> posSequence = new ArrayList<int[]>(refactoringAmount);
//			ArrayList<Integer> refSequence = new ArrayList<Integer>(refactoringAmount);
//			ArrayList<Integer> IDSequence = new ArrayList<Integer>(refactoringAmount);
//			ArrayList<String> refactoringInfo = new ArrayList<String>(refactoringAmount);
//			
//			for (int j = 0; j < refactoringAmount; j++)
//			{				
//				int[] result = randomRefactoring(scArray[i]);
//				int[] position = {result[1], result[2]};
//
//				if (result[0] == -1)
//				{
//					System.out.printf("\nThere are no refactorings available for the rest of the sequence.");
//					j = refactoringAmount;
//				}
//				else
//				{
//					IDSequence.add(refactorings.get(result[0]).getID(position[0], position[1]));
//					refactorings.get(result[0]).transform(refactorings.get(result[0]).analyze((j + 1), position[0], position[1]));
//					refactoringInfo.add(refactorings.get(result[0]).getRefactoringInfo());
//					refSequence.add(result[0]);
//					posSequence.add(position);
//					scArray[i] = refactorings.get(result[0]).getServiceConfiguration();
//				}
//			}
//			
//			refSequence.trimToSize();
//			posSequence.trimToSize();
//			IDSequence.trimToSize();
//			refactoringInfo.trimToSize();
//			population.add(new RefactoringSequence(scArray[i], refSequence, posSequence, IDSequence, refactoringInfo));
//		}
//		
//		return population;
//	}
//	
//	// Method uses single-point crossover. For each refactoring sequence passed in,
//	// a cut point is randomly chosen. The segments of each sequence are then switched to
//	// create two child solutions. After this the refactorings are applied for each child
//	// and any inapplicable refactorings are removed form the new sequences.
//	private ArrayList<RefactoringSequence> crossover(RefactoringSequence p1, RefactoringSequence p2)
//	{
//		ArrayList<RefactoringSequence> children = new ArrayList<RefactoringSequence>(2);
//		ArrayList<Refactoring> refactorings = new ArrayList<Refactoring>(this.c[0].getRefactorings().size());
//		refactorings = this.c[0].getRefactorings();
//		int cutPoint1 = ((int)(Math.random() * (p1.getRefactorings().size() - 1))) + 1;
//		int cutPoint2 = ((int)(Math.random() * (p2.getRefactorings().size() - 1))) + 1;
//		int elementPosition, i2;
//		
//		int c1Size = cutPoint1 + (p2.getRefactorings().size() - cutPoint2);
//		ArrayList<Integer> c1Refactorings = new ArrayList<Integer>(c1Size);
//		ArrayList<int[]> c1Positions = new ArrayList<int[]>(c1Size);
//		ArrayList<Integer> c1IDs = new ArrayList<Integer>(c1Size);
//		ArrayList<String> refactoringInfo1 = new ArrayList<String>(c1Size);
//		
//		// Create copies of the initial program model.
//		CrossReferenceServiceConfiguration sc1 = new CrossReferenceServiceConfiguration();
//		CrossReferenceServiceConfiguration sc2 = new CrossReferenceServiceConfiguration();
//
//		try 
//		{
//			// Read the original input.
//			sc1.getSourceFileRepository().getCompilationUnitsFromFiles(this.sourceFiles);
//			sc2.getSourceFileRepository().getCompilationUnitsFromFiles(this.sourceFiles);
//		}
//		catch (ParserException e) 
//		{
//			System.out.println("\nEXCEPTION: Cannot read input.");
//			System.exit(1);
//		}
//		
//		// Set up initial properties of service configurations.
//		sc1.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, this.inputPath);
//		sc1.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, this.outputPath);
//		sc1.getProjectSettings().ensureSystemClassesAreInPath();
//
//		sc2.getProjectSettings().setProperty(PropertyNames.INPUT_PATH, this.inputPath);
//		sc2.getProjectSettings().setProperty(PropertyNames.OUTPUT_PATH, this.outputPath);
//		sc2.getProjectSettings().ensureSystemClassesAreInPath();
//
//		for (int i = 0; i < c1Size; i++)
//		{				
//			// The first sequence in each solution will be applicable so 
//			// refactorings can be applied without checking.
//			if (i < cutPoint1)
//			{	
//				refactorings.get(p1.getRefactorings().get(i)).setServiceConfiguration(sc1);
//				refactorings.get(p1.getRefactorings().get(i)).transform(refactorings.get(p1.getRefactorings().get(i))
//						    .analyze((i + 1), p1.getPositions().get(i)[0], p1.getPositions().get(i)[1]));
//				refactoringInfo1.add(refactorings.get(p1.getRefactorings().get(i)).getRefactoringInfo());
//				c1Refactorings.add(p1.getRefactorings().get(i));
//				c1Positions.add(p1.getPositions().get(i));	
//				c1IDs.add(p1.getIDs().get(i));
//				sc1 = refactorings.get(p1.getRefactorings().get(i)).getServiceConfiguration();
//			}
//			// For the second sequence, a check will have 
//			// to be made for each contiguous refactoring.
//			else
//			{
//				elementPosition = -1;
//				i2 = cutPoint2 + (i - cutPoint1);
//				refactorings.get(p2.getRefactorings().get(i2)).setServiceConfiguration(sc1);
//				
//				// Checks for the relevant program element by comparing the names of 
//				// each applicable element in the class with the desired element name.
//				for (int j = 1; j <= refactorings.get(p2.getRefactorings().get(i2)).getAmount(p2.getPositions().get(i2)[0]); j++)
//				{
//					if (refactorings.get(p2.getRefactorings().get(i2)).getID(p2.getPositions().get(i2)[0], j) == (p2.getIDs().get(i2)))
//					{
//						elementPosition = j;
//						break;
//					}
//				}
//				
//				// If the element exists and can be refactored.
//				if (elementPosition != -1)
//				{
//					refactorings.get(p2.getRefactorings().get(i2)).transform(refactorings.get(p2.getRefactorings().get(i2))
//							    .analyze((i + 1), p2.getPositions().get(i2)[0], elementPosition));
//					refactoringInfo1.add(refactorings.get(p2.getRefactorings().get(i2)).getRefactoringInfo());
//					c1Refactorings.add(p2.getRefactorings().get(i2));
//					c1Positions.add(new int[] {p2.getPositions().get(i2)[0], elementPosition});
//					c1IDs.add(p2.getIDs().get(i2));
//					sc1 = refactorings.get(p2.getRefactorings().get(i2)).getServiceConfiguration();
//				}
//			}
//		}
//
//		c1Refactorings.trimToSize();
//		c1Positions.trimToSize();
//		c1IDs.trimToSize();
//		refactoringInfo1.trimToSize();
//		children.add(new RefactoringSequence(sc1, c1Refactorings, c1Positions, c1IDs, refactoringInfo1));
//		
//		int c2Size = cutPoint2 + (p1.getRefactorings().size() - cutPoint1);
//		ArrayList<Integer> c2Refactorings = new ArrayList<Integer>(c2Size);
//		ArrayList<int[]> c2Positions = new ArrayList<int[]>(c2Size);
//		ArrayList<Integer> c2IDs = new ArrayList<Integer>(c2Size);
//		ArrayList<String> refactoringInfo2 = new ArrayList<String>(c2Size);
//					
//		for (int i = 0; i < c2Size; i++)
//		{			
//			// The first sequence in each solution will be applicable so 
//			// refactorings can be applied without checking.
//			if (i < cutPoint2)
//			{
//				refactorings.get(p2.getRefactorings().get(i)).setServiceConfiguration(sc2);
//				refactorings.get(p2.getRefactorings().get(i)).transform(refactorings.get(p2.getRefactorings().get(i))
//						    .analyze((i + 1), p2.getPositions().get(i)[0], p2.getPositions().get(i)[1]));
//				refactoringInfo2.add(refactorings.get(p2.getRefactorings().get(i)).getRefactoringInfo());
//				c2Refactorings.add(p2.getRefactorings().get(i));
//				c2Positions.add(p2.getPositions().get(i));
//				c2IDs.add(p2.getIDs().get(i));
//				sc2 = refactorings.get(p2.getRefactorings().get(i)).getServiceConfiguration();
//			}	
//			// For the second sequence, a check will have 
//			// to be made for each contiguous refactoring.
//			else
//			{
//				elementPosition = -1;
//				i2 = cutPoint1 + (i - cutPoint2);
//				refactorings.get(p1.getRefactorings().get(i2)).setServiceConfiguration(sc2);
//				
//				// Checks for the relevant program element by comparing the names of 
//				// each applicable element in the class with the desired element name.
//				for (int j = 1; j <=  refactorings.get(p1.getRefactorings().get(i2)).getAmount(p1.getPositions().get(i2)[0]); j++)
//				{					
//					if (refactorings.get(p1.getRefactorings().get(i2)).getID(p1.getPositions().get(i2)[0], j) == (p1.getIDs().get(i2)))
//					{
//						elementPosition = j;
//						break;
//					}
//				}
//				
//				// If the element exists and can be refactored.
//				if (elementPosition != -1)
//				{
//					refactorings.get(p1.getRefactorings().get(i2)).transform(refactorings.get(p1.getRefactorings().get(i2))
//							    .analyze((i + 1), p1.getPositions().get(i2)[0], elementPosition));
//					refactoringInfo2.add(refactorings.get(p1.getRefactorings().get(i2)).getRefactoringInfo());
//					c2Refactorings.add(p1.getRefactorings().get(i2));
//					c2IDs.add(p1.getIDs().get(i2));
//					c2Positions.add(new int[] {p1.getPositions().get(i2)[0], elementPosition});
//					sc2 = refactorings.get(p1.getRefactorings().get(i2)).getServiceConfiguration();
//				}
//			}
//		}
//		
//		c2Refactorings.trimToSize();
//		c2Positions.trimToSize();
//		c2IDs.trimToSize();
//		refactoringInfo2.trimToSize();
//		children.add(new RefactoringSequence(sc2, c2Refactorings, c2Positions, c2IDs, refactoringInfo2));
//		
//		return children;
//	}
//	
//	// Applies a random refactoring to the end of the refactoring sequence passed in.
//	// If the refactoring is not applicable it will keep trying until an applicable
//	// refactoring is found or it runs out of possibilities. In this case the original 
//	// sequence is returned.
//	private RefactoringSequence mutation(RefactoringSequence p)
//	{			
//		int[] result = randomRefactoring(p.getServiceConfiguration());
//		int[] position = {result[1], result[2]};
//		
//		// Applies refactoring to model and adds it to the sequence.
//		if (result[0] != -1)
//		{			
//			ArrayList<Refactoring> refactorings = new ArrayList<Refactoring>(this.c[0].getRefactorings().size());
//			refactorings = this.c[0].getRefactorings();
//			ArrayList<Integer> refSequence = new ArrayList<Integer>(p.getRefactorings().size() + 1);
//			refSequence = p.getRefactorings();
//			ArrayList<int[]> posSequence = new ArrayList<int[]>(p.getPositions().size() + 1);
//			posSequence = p.getPositions();
//			ArrayList<Integer> IDSequence = new ArrayList<Integer>(p.getIDs().size() + 1);
//			IDSequence = p.getIDs();
//			ArrayList<String> refactoringInfo = new ArrayList<String>(p.getRefactoringInfo().size() + 1);
//			refactoringInfo = p.getRefactoringInfo();
//			
//			refSequence.add(result[0]);
//			posSequence.add(position);
//			IDSequence.add(refactorings.get(result[0]).getID(position[0], position[1]));
//			refactorings.get(result[0]).transform(refactorings.get(result[0]).analyze((refSequence.size()), position[0], position[1]));
//			refactoringInfo.add(refactorings.get(result[0]).getRefactoringInfo());
//			p.setRefactorings(refSequence);
//			p.setPositions(posSequence);
//			p.setIDs(IDSequence);
//			p.setRefactoringInfo(refactoringInfo);
//			p.setServiceConfiguration(refactorings.get(result[0]).getServiceConfiguration());
//		}
//		
//		return p;
//	}
//	
//	// Sorts the population by fitness depending on ranks of non dominated 
//	// solutions and calculation of crowding distance for last rank added.
//	private ArrayList<RefactoringSequence> fitness(ArrayList<RefactoringSequence> population)
//	{		
//		ArrayList<ArrayList<RefactoringSequence>> dominationFronts = fastNonDominatedSort(population);
//
//		ArrayList<RefactoringSequence> sortedPopulation = new ArrayList<RefactoringSequence>(population.size());
//		int i = 0;
//		
//		while ((dominationFronts.size() > i) && ((sortedPopulation.size() + dominationFronts.get(i).size()) <= this.populationSize)) 
//		{
//			dominationFronts.set(i, crowdingDistanceAssignment(dominationFronts.get(i)));
//			sortedPopulation.addAll(dominationFronts.get(i));
//			i++;
//		}
//		
//		if (sortedPopulation.size() != this.populationSize) 
//		{
//			ArrayList<RefactoringSequence> front = sort(crowdingDistanceAssignment(dominationFronts.get(i)), false);
//			int remainingSolutions = this.populationSize - sortedPopulation.size();
//			
//			for (i = 0; i < remainingSolutions; i++) 
//				sortedPopulation.add(front.get(i));
//		}
//
//		sortedPopulation.trimToSize();
//		return sortedPopulation;
//	}
//
//	
//	// Output search information to results file.
//	// Can be used for a population of solutions to generate separate results files.
//	private void outputSearchInfo(String pathName, int solution, String runInfo)
//	{
//		String runName;
//
//		// Create a location for the results output.
//		pathName = pathName.substring(0, (pathName.length() - 1));
//		pathName += "s/";
//		runName = String.format("%sresultsSolution%d.txt", pathName, solution);
//
//		File dir = new File(pathName);
//		if (!dir.exists()) 
//			dir.mkdirs();
//
//		try 
//		{
//			BufferedWriter bw = new BufferedWriter(new FileWriter(runName, false));
//			bw.write(String.format("======== Search Information ========"));
//			bw.write(String.format("\r\n%s", runInfo));
//			bw.close();
//		}
//		catch (IOException e) 
//		{
//			System.out.println("\nEXCEPTION: Cannot export results to text file.");
//			System.exit(1);
//		}
//	}
//
//	// Output refactoring information to results file for a solution.
//	private void outputRefactoringInfo(String pathName, double time, int solution, ArrayList<String> refactoringInfo)
//	{
//		String runName;
//
//		// Create a location for the results output.
//		pathName = pathName.substring(0, (pathName.length() - 1));
//		pathName += "s/";
//		runName = String.format("%sresultsSolution%d.txt", pathName, solution);
//
//		File dir = new File(pathName);
//		if (!dir.exists()) 
//			dir.mkdirs();
//
//		try 
//		{
//			BufferedWriter bw = new BufferedWriter(new FileWriter(runName, true));
//			bw.append("\r\n\r\n======== Applied Refactorings ========");
//
//			for (int i = 0; i < refactoringInfo.size(); i++) 
//				bw.append(String.format("\r\n%s", refactoringInfo.get(i)));
//
//			bw.append(String.format("\r\n\r\nTime taken to refactor: %.2fs", time));
//			bw.close();
//		}
//		catch (IOException e) 
//		{
//			System.out.println("\nEXCEPTION: Cannot export results to text file.");
//			System.exit(1);
//		}
//	}
//
//	// Outputs the metric values for a solution.
//	private void outputMetrics(float[] scores, boolean initial, boolean log, int solution, String pathName)
//	{
//		String runName;
//		
//		// Create a location for the results output.
//		pathName = pathName.substring(0, (pathName.length() - 1));
//		pathName += "s/";
//		runName = String.format("%sresultsSolution%d.txt", pathName, solution);
//
//		File dir = new File(pathName);
//		if (!dir.exists()) 
//			dir.mkdirs();
//
//		try 
//		{
//			BufferedWriter bw = new BufferedWriter(new FileWriter(runName, true));
//
//			if (initial)
//				bw.append(String.format("\r\n\r\n======== Initial Metric Info ========"));
//			else
//				bw.append(String.format("\r\n\r\n======== Final Metric Info ========"));
//
//			// Outputs the fitness function values for the project to a text file.
//			for (int i = 0; i < scores.length; i++)
//				bw.append(String.format("\r\nFitness function %d score: %f", (i + 1), scores[i]));
//
//			bw.close();
//		}
//		catch (IOException e) 
//		{
//			System.out.println("\nEXCEPTION: Cannot export results to text file.");
//			System.exit(1);
//		}
//
//		if (log)
//		{
//			// Outputs the fitness function values for the project to the console for immediate feedback.
//			for (int i = 0; i < scores.length; i++)
//				System.out.printf("\n\nFitness function %d score: %.2f", (i + 1), scores[i]);
//		}
//	}
//
//	// Selects one out of two individuals using a binary
//	// tournament selection with the crowded comparison operator.
//	private RefactoringSequence binaryTournament(RefactoringSequence s1, RefactoringSequence s2) 
//	{
//		if (s1.getRank() < s2.getRank())
//			return s1;				
//		else if (s1.getRank() > s2.getRank())
//			return s2;
//		else
//		{
//			if (s1.getCrowdingDistance() > s2.getCrowdingDistance()) 
//				return s1;
//			else if (s2.getCrowdingDistance() > s1.getCrowdingDistance()) 
//				return s2;
//			else
//			{
//				// Both solutions are "equal". Select one at random.
//				if (Math.random() < 0.5) 
//					return s1;
//				else 
//					return s2;
//			}
//		}
//	}
//
//	// Finds a random available refactoring in the specified model and passes back the
//	// refactoring used and the position of the applicable program element in the model.
//	private int[] randomRefactoring(CrossReferenceServiceConfiguration sc)
//	{
//		ArrayList<Refactoring> refactorings = new ArrayList<Refactoring>(this.c[0].getRefactorings().size());
//		refactorings = this.c[0].getRefactorings();
//		int[] position = new int[2];
//		int r = -1;
//
//		// Find element to refactor.
//		if (refactorings.size() > 0)
//		{
//			r = (int)(Math.random() * refactorings.size());
//			refactorings.get(r).setServiceConfiguration(sc);
//			position = randomElement(refactorings.get(r));
//		}
//		else
//		{
//			position[0] = -1;
//			position[1] = -1;
//		}
//
//		// Checks in case no elements are returned for the refactoring.
//		// Will check again for each available refactoring in the search and 
//		// if there are still no applicable elements returned the search will terminate.
//		if ((position[0] == -1) && (position[1] == -1))
//		{
//			int exclude = r;
//			for (r = 0; r < refactorings.size(); r++)
//			{
//				// Stops the loop from repeating the check for the previous refactoring.
//				if ((r == exclude) && ((r + 1) < refactorings.size()))
//					r++;
//				else if (r == exclude)
//					break;
//
//				refactorings.get(r).setServiceConfiguration(sc);
//				position = randomElement(refactorings.get(r));
//
//				if ((position[0] != -1) && (position[1] != -1))
//					break;
//			}
//
//			if ((position[0] == -1) && (position[1] == -1))
//				r = -1;
//		}
//		
//		return new int[]{r, position[0], position[1]};
//	}
//	
//	// Makes a fast non-domination sort of the specified individuals. The method returns the different
//	// domination fronts in ascending order by their rank and sets their rank value.
//	private ArrayList<ArrayList<RefactoringSequence>> fastNonDominatedSort(ArrayList<RefactoringSequence> population) 
//	{
//		ArrayList<ArrayList<RefactoringSequence>> dominationFronts = new ArrayList<ArrayList<RefactoringSequence>>();
//		HashMap<RefactoringSequence, ArrayList<RefactoringSequence>> dominatedSolutions = new HashMap<RefactoringSequence, ArrayList<RefactoringSequence>>();
//		HashMap<RefactoringSequence, Integer> numberOfDominatingSolutions = new HashMap<RefactoringSequence, Integer>();
//		int i = 1;
//		int amount = 0;
//		
//		for (RefactoringSequence s1 : population) 
//		{
//			dominatedSolutions.put(s1, new ArrayList<RefactoringSequence>());
//			numberOfDominatingSolutions.put(s1, 0);
//
//			for (RefactoringSequence s2 : population) 
//			{
//				if (isDominant(s1, s2)) 
//					dominatedSolutions.get(s1).add(s2);
//				else if (isDominant(s2, s1)) 
//					numberOfDominatingSolutions.put(s1, numberOfDominatingSolutions.get(s1) + 1);
//			}
//
//			if (numberOfDominatingSolutions.get(s1) == 0) 
//			{
//				s1.setRank(1);
//				
//				if (dominationFronts.isEmpty()) 
//				{
//					ArrayList<RefactoringSequence> firstDominationFront = new ArrayList<RefactoringSequence>();
//					firstDominationFront.add(s1);
//					dominationFronts.add(firstDominationFront);
//				} 
//				else 
//					dominationFronts.get(0).add(s1);
//			}
//		}
//		
//		if (!dominationFronts.isEmpty()) 
//			amount += dominationFronts.get(0).size();
//		
//		// Improved the efficiency of the method here by adding fronts according to need as outlined in
//		// "Reducing The Run-Time Complexity Of NSGA-II For Bi-Objective Optimization Problem".
//		while ((dominationFronts.size() == i) && (amount < this.populationSize))
//		{
//			ArrayList<RefactoringSequence> nextDominationFront = new ArrayList<RefactoringSequence>();
//			
//			for (RefactoringSequence s1 : dominationFronts.get(i - 1)) 
//			{
//				for (RefactoringSequence s2 : dominatedSolutions.get(s1)) 
//				{
//					numberOfDominatingSolutions.put(s2, numberOfDominatingSolutions.get(s2) - 1);
//					
//					if (numberOfDominatingSolutions.get(s2) == 0) 
//					{
//						s2.setRank(i + 1);
//						nextDominationFront.add(s2);
//					}
//				}
//			}
//			
//			i++;
//			
//			if (!nextDominationFront.isEmpty()) 
//			{
//				nextDominationFront.trimToSize();
//				dominationFronts.add(nextDominationFront);
//				amount += dominationFronts.get(i - 1).size();
//			}
//		}
//		
//		dominationFronts.trimToSize();
//		return dominationFronts;
//	}
//
//	// Returns a boolean to represent whether the first solution parameter dominates the second.
//	// The solution is dominant if no individual fitness function values are worse and at least one is better.
//	private boolean isDominant(RefactoringSequence s1, RefactoringSequence s2)
//	{
//		boolean better = false;
//		FitnessFunction ff = new FitnessFunction();
//		Metrics m1 = new Metrics(s1.getServiceConfiguration().getSourceFileRepository().getKnownCompilationUnits());
//		Metrics m2 = new Metrics(s2.getServiceConfiguration().getSourceFileRepository().getKnownCompilationUnits());
//		float value1, value2;
//
//		for (int i = 0; i < this.c.length; i++) 
//		{
//			value1 = ff.calculateScore(m1, this.c[i].getConfiguration());
//			value2 = ff.calculateScore(m2, this.c[i].getConfiguration());
//
//			if (value1 < value2) 
//				return false;
//			else if (value1 > value2) 
//				better = true;
//		}
//
//		return better;
//	}
//
//	// Executes the crowding distance assignment for the specified individuals.
//	private ArrayList<RefactoringSequence> crowdingDistanceAssignment(ArrayList<RefactoringSequence> paretoFront) 
//	{
//		if (paretoFront.size() < 3) 
//		{
//			for (RefactoringSequence s : paretoFront) 
//				s.setCrowdingDistance(Float.POSITIVE_INFINITY);
//		}
//		else
//		{
//			FitnessFunction ff = new FitnessFunction();
//			Metrics m;
//
//			// Initialize crowding distance.
//			for (RefactoringSequence s : paretoFront) 
//				s.setCrowdingDistance(0);
//
//			for (int i = 0; i < this.c.length; i++) 
//			{
//				for (RefactoringSequence s : paretoFront)
//				{
//					m = new Metrics(s.getServiceConfiguration().getSourceFileRepository().getKnownCompilationUnits());		
//					s.setFitness(ff.calculateScore(m, this.c[i].getConfiguration()));
//				}
//
//				// Sort solutions using the current fitness objective.
//				paretoFront = sort(paretoFront, true);
//
//				// So that boundary points are always selected.
//				paretoFront.get(0).setCrowdingDistance(Float.POSITIVE_INFINITY);
//				paretoFront.get(paretoFront.size() - 1).setCrowdingDistance(Float.POSITIVE_INFINITY);
//
//				// If minimal and maximal fitness value for this
//				// objective are equal, do not change crowding distance. 
//				if (paretoFront.get(0).getFitness() != paretoFront.get(paretoFront.size() - 1).getFitness()) 
//				{
//					for (int j = 1; j < paretoFront.size() - 1; j++) 
//					{
//						float newCrowdingDistance = paretoFront.get(j).getCrowdingDistance();
//
//						newCrowdingDistance += (paretoFront.get(j - 1).getFitness() - paretoFront.get(j + 1).getFitness()) /
//					               			   (paretoFront.get(0).getFitness() - paretoFront.get(paretoFront.size() - 1).getFitness());
//
//						paretoFront.get(j).setCrowdingDistance(newCrowdingDistance);
//					}
//				}
//			}
//		}
//
//		return paretoFront;
//	}
//	
//	// Sorts the population by fitness or crowding distance.
//	private ArrayList<RefactoringSequence> sort(ArrayList<RefactoringSequence> population, boolean fitness) 
//	{
//		RefactoringSequence[] arrayPopulation = population.toArray(new RefactoringSequence[0]);
//		population.clear();
//
//		if (fitness)
//			Arrays.sort(arrayPopulation, new FitnessComparator());
//		else
//			Arrays.sort(arrayPopulation, new CrowdingDistanceComparator());
//		
//		for (RefactoringSequence s : arrayPopulation)
//			population.add(s);
//
//		return population;
//	}
//	
//	// This inner class allows sorting by crowding distance so higher distances are at the front of the list.
//	private class CrowdingDistanceComparator implements Comparator<RefactoringSequence> 
//	{
//		// Compares the two specified individuals using the crowding distance operator.
//		// Returns -1, 0 or 1 as the first argument is greater than, equal to, or less than the second.
//		public int compare(RefactoringSequence s1, RefactoringSequence s2) 
//		{   
//			if (s1.getCrowdingDistance() > s2.getCrowdingDistance())
//				return -1;
//			else if (s1.getCrowdingDistance() < s2.getCrowdingDistance())
//				return 1;
//			else
//				return 0;
//		}
//	}
//
//	// This inner class allows sorting by fitness so that the more fit solutions are at the front of the list.
//	private class FitnessComparator implements Comparator<RefactoringSequence> 
//	{
//		// Compares the two specified individuals using the fitness operator.
//		// Returns -1, 0 or 1 as the first argument is greater than, equal to, or less than the second.
//		public int compare(RefactoringSequence s1, RefactoringSequence s2) 
//		{   
//			if (s1.getFitness() > s2.getFitness())
//				return -1;
//			else if (s1.getFitness() < s2.getFitness())
//				return 1;
//			else
//				return 0;
//		}
//	}
//	
//	
//	private int[] randomElement(Refactoring r)
//	{
//		int[] position = new int[2];
//		position[0] = (int)(Math.random() * r.getServiceConfiguration().getSourceFileRepository().getKnownCompilationUnits().size());
//			
//		// Only counts the relevant program element.
//		int amount = r.getAmount(position[0]);
//		
//		// Count the amount of available elements in the chosen class for refactoring.
//		// If none are available find the next element up that is available.
//		// If there are still no elements found look in the other direction and if there 
//		// are no elements available at all return -1 so it can be handled in the search.
//		if (amount == 0)
//		{
//			int[] temp = new int[2];
//			position[1] = 1;
//			temp = nextElementUp(position[0], position[1], r);
//			
//			if ((temp[0] == -1) && (temp[1] == -1))
//			{
//				temp = nextElementDown(position[0], position[1], r);
//			}	
//			
//			position = temp;
//		}
//		else
//			// Random element chosen from the available amount.
//			position[1] = (int)(Math.random() * (amount-1)) + 1;
//		
//		return position;
//	}	
//	
//	private int[] nextElementUp(int currentUnit, int currentElement, Refactoring r) 
//	{
//		int[] position = new int[2];
//		
//		if  (currentElement >= r.getAmount(currentUnit))
//		{
//			if (currentUnit == (r.getServiceConfiguration().getSourceFileRepository().getKnownCompilationUnits().size() - 1))
//			{
//				position[0] = -1;
//				position[1] = -1;
//			}
//			else
//			{				
//				currentUnit++;
//
//				int amount = 0;
//				while (amount == 0)
//				{
//					if (currentUnit >= r.getServiceConfiguration().getSourceFileRepository().getKnownCompilationUnits().size())
//					{
//						position[0] = -1;
//						position[1] = -1;
//						amount = -1;
//					}
//					// Only counts the relevant program element.
//					else
//						amount = r.getAmount(currentUnit);
//
//					if (amount == 0)
//						currentUnit++;
//				}
//				if (amount != -1)
//				{
//					position[0] = currentUnit;
//					position[1] = 1;
//				}
//			}
//		}
//		else
//		{
//			position[0] = currentUnit;
//			position[1] = currentElement + 1;
//		}
//
//		return position;
//	}
//	
//	private int[] nextElementDown(int currentUnit, int currentElement, Refactoring r) 
//	{
//		int[] position = new int[2];
//		
//		if (currentElement <= 1)
//		{
//			if (currentUnit == 0)
//			{
//				position[0] = -1;
//				position[1] = -1;
//			}
//			else
//			{
//				currentUnit--;
//
//				int amount = 0;
//				while (amount == 0)
//				{
//					if (currentUnit < 0)
//					{
//						position[0] = -1;
//						position[1] = -1;
//						amount = -1;
//					}
//					// Only counts the relevant program element.
//					else
//						amount = r.getAmount(currentUnit);
//
//					if (amount == 0)
//						currentUnit--;
//				}
//				if (amount != -1)
//				{
//					position[0] = currentUnit;
//					position[1] = amount;
//				}
//			}
//		}
//		// This is a makeshift solution to using the method consecutively 
//		// for different refactorings. If the element from the previous refactoring
//		// is out of bounds of the current refactoring, the last element in the class
//		// of the previous refactoring is used. This may not be the closest element
//		// to the previous point in the program, but getting that would be awkward.
//		else if (currentElement >= r.getAmount(currentUnit))
//		{
//			int amount = 0;
//			while (amount == 0)
//			{
//				if (currentUnit < 0)
//				{
//					position[0] = -1;
//					position[1] = -1;
//					amount = -1;
//				}
//				// Only counts the relevant program element.
//				else
//					amount = r.getAmount(currentUnit);
//
//				if (amount == 0)
//					currentUnit--;
//			}
//			if (amount != -1)
//			{
//				position[0] = currentUnit;
//				position[1] = amount;
//			}
//		}
//		else
//		{
//			position[0] = currentUnit;
//			position[1] = currentElement - 1;
//		}
//
//		return position;
//	}
//
//	private void print(SourceFileRepository sfr)
//	{
//		List<CompilationUnit> list = sfr.getKnownCompilationUnits();
//
//		for (CompilationUnit cu : list)
//		{	
//			try 
//			{
//				sfr.print(cu);
//			} 
//			catch (IOException e) 
//			{
//				System.out.println("\nEXCEPTION: Cannot print changes to output.");
//				System.exit(1);
//			}
//		}
//	}
//	
//	public void setConfigurations(Configuration[] c)
//	{
//		this.c = c;
//	}
//	
//	public void setResultsPath(String resultsPath)
//	{
//		this.resultsPath = resultsPath;
//	}
//}
//
//
//  // Was used in random search to make sure search doesn't yield a solution that is worse than the initial input.
//	// super.c.getRefactorings().get(randomRef).transform(super.c.getRefactorings().get(randomRef).analyze(i, position[0], position[1]));
//	newValue = ff.calculateScore(m, super.c.getConfiguration());
//	
//	if (newValue < benchmark)
//	{
//		super.c.getRefactorings().get(randomRef).transform(super.c.getRefactorings().get(randomRef).analyzeReverse());
//		i--;
//	}
//
//
// // Original version of the simulated annealing algorithm using the defunct getNeighbour method
// // to find the nearest refactorings for each available refactoring object and find from that set the 
// // next solution in each iteration. In this version a new solution will be chosen in every iteration.
//public class SimulatedAnnealingSearchOld extends Search
//{
//	private int iterations;
//	private float temperature;
//
//	public SimulatedAnnealingSearchOld(CrossReferenceServiceConfiguration sc, Configuration c) 
//	{
//		super(sc, c);
//		this.iterations = 100;
//		this.temperature = 4.0f;
//	}
//
//	public SimulatedAnnealingSearchOld(CrossReferenceServiceConfiguration sc, Configuration c, int iterations, float temperature) 
//	{
//		super(sc, c);
//		this.iterations = iterations;
//		this.temperature = temperature;
//	}
//	
//	public void run()
//	{
//		Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());		
//		FitnessFunction ff = new FitnessFunction();
//		super.refactoringInfo = new ArrayList<String>();
//
//		float benchmark = ff.calculateScore(m, super.c.getConfiguration());
//		float best = benchmark;
//		float currentTemperature = this.temperature;
//		int bestIteration = 1; 
//		int r = -1;
//		int[] position = new int[2];
//		int[] neighbour = new int[3];
//
//		String runInfo = String.format("Search: Simulated Annealing\r\nIterations: %d\r\nStarting Temperature: %f", this.iterations, this.temperature);
//		super.outputSearchInfo(super.resultsPath, runInfo);
//		super.outputMetrics(benchmark, m, true, true, super.resultsPath);
//		System.out.printf("\n\nRefactoring...");
//		long timeTaken, startTime = System.currentTimeMillis();
//		double time;
//
//		// Applies refactorings by starting at a random point and find all the neighbouring 
//		// refactorings. These are then compared with the current score and the improved options
//		// are chosen until there are no longer any available.
//		for (int i = 1; i <= this.iterations; i++)
//		{
//			// Find element to refactor. If the search has only 
//			// started a random element will be chosen as a start point.
//			if (i == 1)
//			{
//				if (super.c.getRefactorings().size() > 0)
//				{
//					r = (int) (Math.random() * super.c.getRefactorings().size());
//					position = super.randomElement(super.c.getRefactorings().get(r));
//				}
//				else
//				{
//					position[0] = -1;
//					position[1] = -1;
//				}
//				
//				// Checks in case no elements are returned for the refactoring.
//				// Will check again for each available refactoring in the search and 
//				// if there are still no applicable elements returned the search will terminate.
//				if ((position[0] == -1) && (position[1] == -1))
//				{
//					int exclude = r;
//					for (r = 0; r < super.c.getRefactorings().size(); r++)
//					{
//						// Stops the loop from repeating the check for the previous refactoring.
//						if ((r == exclude) && ((r + 1) < super.c.getRefactorings().size()))
//							r++;
//						else if (r == exclude)
//							break;
//						
//						position = super.randomElement(super.c.getRefactorings().get(r));
//						if ((position[0] != -1) && (position[1] != -1))
//							break;
//					}
//					
//					if ((position[0] == -1) && (position[1] == -1))
//						r = -1;
//				}
//			}
//			
//			// If a random element could be found for a starting point.
//			if ((position[0] != -1) && (position[1] != -1))
//			{
//				neighbour = super.getNeighbour(position[0], position[1], i, super.c.getRefactorings(), currentTemperature, ff, m, best);
//				position[0] = neighbour[0];
//				position[1] = neighbour[1];
//				r = neighbour[2];
//			}
//			
//			if (r == -1)
//			{
//				System.out.printf("\nThere are no refactorings available - search terminating.");
//				i = this.iterations;
//			}
//			else
//			{
//				super.c.getRefactorings().get(r).transform(super.c.getRefactorings().get(r).analyze(i, position[0], position[1]));
//				super.refactoringInfo.add(super.c.getRefactorings().get(r).getRefactoringInfo());
//				best = ff.calculateScore(m, super.c.getConfiguration());
//				bestIteration = i;
//			}
//
//			if (i % 25 == 0)
//			{
//				timeTaken = System.currentTimeMillis() - startTime;
//				time = timeTaken / 1000.0;
//				int percent = (int) ((float)i / this.iterations*100);
//				System.out.printf("\n  Search has taken %.2fs so far (%d%% complete)", time, percent);
//			}
//			
//			double step = (this.iterations - i) / (double)this.iterations;
//			currentTemperature = (float) (this.temperature * step * step);
//		}
//
//		// Output time taken to console and refactoring information to results file.
//		timeTaken = System.currentTimeMillis() - startTime;
//		time = timeTaken / 1000.0;
//		System.out.printf("\nTime taken to refactor: %.2fs", time);
//		super.outputRefactoringInfo(super.resultsPath, time, best - benchmark);
//
//		// Output the final metric values to the results file.
//		super.outputMetrics(best, m, false, true, super.resultsPath);
//
//		// Output the lowest visibility value measured during the search and at what iteration it was acquired.
//		System.out.printf("\n\nBest score acquired was %f at iteration %d", best, bestIteration);
//		System.out.printf("\nScore has improved overall by %f", best - benchmark);
//
//		// Apply refactorings from the AST to the source code.
//		System.out.printf("\nApplying Transformations...");   	
//		super.print(super.sc.getSourceFileRepository());
//
//		timeTaken = System.currentTimeMillis() - startTime;
//		time = timeTaken / 1000.0;
//		System.out.printf("\nOverall time taken for search: %.2fs", time);
//	}
//}
//
//
// // Original version of the hill climbing algorithm. It uses neighbouring refactorings from a random
// // point and chooses between them. Due to the design of the getNeighbours method, this results in 
// // predictable movements, executing each refactoring type until there are none left for that type.
//public class HillClimbingSearchOld extends Search
//{
//	private int restartCount;
//	private boolean steepestAscent;
//	private int maxIterations = -1;
//
//	public HillClimbingSearchOld(CrossReferenceServiceConfiguration sc, Configuration c, int restartCount, boolean steepestAscent) 
//	{
//		super(sc, c);
//		this.restartCount = restartCount;
//		this.steepestAscent = steepestAscent;
//	}
//	
//	public HillClimbingSearchOld(CrossReferenceServiceConfiguration sc, Configuration c, int restartCount, boolean steepestAscent, int maxIterations)
//	{
//		super(sc, c);
//		this.restartCount = restartCount;
//		this.steepestAscent = steepestAscent;
//		this.maxIterations = maxIterations;
//	}
//	
//	public void run()
//	{
//		ArrayList<Refactoring> refactorings = super.c.getRefactorings();
//		Metrics m = new Metrics(super.sc.getSourceFileRepository().getKnownCompilationUnits());		
//		FitnessFunction ff = new FitnessFunction();
//		super.refactoringInfo = new ArrayList<String>();
//
//		float benchmark = ff.calculateScore(m, super.c.getConfiguration());
//		float best = benchmark;
//		int iteration = 1;
//		int bestIteration = iteration; 
//		int r = -1;
//		int[] position = new int[2];
//		int[] neighbour = new int[3];
//
//		String ascent = (this.steepestAscent) ? "Steepest" : "First";
//		String runInfo = String.format("Search: Hill Climbing\r\nAscent: %s", ascent);
//		super.outputSearchInfo(super.resultsPath, runInfo);
//		super.outputMetrics(benchmark, m, true, true, super.resultsPath);
//		System.out.printf("\n\nRefactoring...");
//		long timeTaken, startTime = System.currentTimeMillis();
//		double time;
//
//		// Applies refactorings by starting at a random point and find all the neighbouring 
//		// refactorings. These are then compared with the current score and the improved options
//		// are chosen until there are no longer any available.
//		for (int i = 0; i <= this.restartCount; i++)
//		{
//			if ((this.restartCount > 0) && (i > 0))
//				System.out.printf("\n Restarting %d of %d", i, this.restartCount);
//			
//			boolean keepGoing = true;
//			
//			while (keepGoing)
//			{						
//				// Find element to refactor. If the search has only started or
//				// has restarted a random element will be chosen as a start point.
//				if (refactorings.size() > 0)
//				{
//					r = (int) (Math.random() * refactorings.size());
//					position = super.randomElement(refactorings.get(r));
//				}
//				else
//				{
//					position[0] = -1;
//					position[1] = -1;
//				}
//
//				// Checks in case no elements are returned for the refactoring.
//				// Will check again for each available refactoring in the search and 
//				// if there are still no applicable elements returned the search will terminate.
//				if ((position[0] == -1) && (position[1] == -1))
//				{
//					int exclude = r;
//					for (r = 0; r < refactorings.size(); r++)
//					{
//						// Stops the loop from repeating the check for the previous refactoring.
//						if (r == exclude)
//							if ((r + 1) < refactorings.size())
//								r++;
//							else
//								break;
//
//						position = super.randomElement(refactorings.get(r));
//						if ((position[0] != -1) && (position[1] != -1))
//							break;
//					}
//
//					if ((position[0] == -1) && (position[1] == -1))
//						r = -1;
//				}
//
//				// If a random element could be found for a starting point.
//				if ((position[0] != -1) && (position[1] != -1))
//				{
//					neighbour = super.getNeighbour(position[0], position[1], iteration, refactorings, this.steepestAscent, ff, m, best);
//					position[0] = neighbour[0];
//					position[1] = neighbour[1];
//					r = neighbour[2];
//				}
//
//				// Either there were no initial refactorings available 
//				// or there are no refactorings left to apply.
//				if (r == -1)
//				{
//					if (iteration == 1)
//						System.out.printf("\nThere are no refactorings available - search terminating.");
//
//					keepGoing = false;
//				}
//				else
//				{
//					refactorings.get(r).transform(refactorings.get(r).analyze(iteration, position[0], position[1]));
//					super.refactoringInfo.add(refactorings.get(r).getRefactoringInfo());
//					best = ff.calculateScore(m, super.c.getConfiguration());
//					bestIteration = iteration;
//				}
//
//				if (iteration % 25 == 0)
//				{
//					timeTaken = System.currentTimeMillis() - startTime;
//					time = timeTaken / 1000.0;
//					System.out.printf("\n  Search has taken %.2fs so far (%d iterations)", time, iteration);
//				}
//
//				if ((maxIterations != -1) && (iteration == maxIterations))
//					keepGoing = false;
//				else
//					iteration++;
//			}
//			
//			// Stops search if the specified maximum iterations have been reached.
//			if ((maxIterations != -1) && (iteration == maxIterations))
//				break;
//		}
//
//		// Output time taken to console and refactoring information to results file.
//		timeTaken = System.currentTimeMillis() - startTime;
//		time = timeTaken / 1000.0;
//		System.out.printf("\nTime taken to refactor: %.2fs", time);
//		super.outputRefactoringInfo(super.resultsPath, time, best - benchmark);
//
//		// Output the final metric values to the results file.
//		super.outputMetrics(best, m, false, true, super.resultsPath);
//
//		// Output the lowest visibility value measured during the search and at what iteration it was acquired.
//		System.out.printf("\n\nBest score acquired was %f at iteration %d", best, bestIteration);
//		System.out.printf("\nScore has improved overall by %f", best - benchmark);
//
//		// Apply refactorings from the AST to the source code.
//		System.out.printf("\nApplying Transformations...");	
//		super.print(super.sc.getSourceFileRepository());
//
//		timeTaken = System.currentTimeMillis() - startTime;
//		time = timeTaken / 1000.0;
//		System.out.printf("\nOverall time taken for search: %.2fs", time);
//	}
//}
//
//
//  // The method used by the original hill climbing algorithm to get neighbouring refactorings.
//	// A neighbour will be chosen using the list of available refactorings and
//	// the applicable elements on either side of the current position. If the 
//	// element is at the edge of a compilation unit the next class will be used.
//	// The neighbour returned will contain the element and refactoring that improves 
//	// the current score, and the level of improvement will depend on whether the search is
//	// first ascent or steepest ascent.
//	protected int[] getNeighbour(int currentUnit, int currentElement, int iteration, ArrayList<Refactoring> refactorings, 
//			boolean steepestAscent, FitnessFunction ff, Metrics m, float currentScore)
//	{
//		float newScore = currentScore;
//		int[] position = new int[2];
//		int[] neighbour = new int[3];
//		neighbour[0] = currentUnit;
//		neighbour[1] = currentElement;
//		neighbour[2] = -1;
//
//		for (int i = 0; i < refactorings.size(); i++)
//		{
//			double random = Math.random();
//			if (random >= 0.5)
//				position = nextElementUp(currentUnit, currentElement, refactorings.get(i));
//			else
//				position = nextElementDown(currentUnit, currentElement, refactorings.get(i));
//
//			if ((position[0] != -1) && (position[1] != -1))
//			{
//				refactorings.get(i).transform(refactorings.get(i).analyze(iteration, position[0], position[1]));
//				newScore = ff.calculateScore(m, this.c.getConfiguration());
//				refactorings.get(i).transform(refactorings.get(i).analyzeReverse());
//
//				if (newScore > currentScore)
//				{
//					neighbour[0] = position[0];
//					neighbour[1] = position[1];
//					neighbour[2] = i;
//					
//					if (steepestAscent)
//						currentScore = newScore;
//					else
//						break;
//				}
//			}
//
//			if (random >= 0.5)
//				position = nextElementDown(currentUnit, currentElement, refactorings.get(i));
//			else
//				position = nextElementUp(currentUnit, currentElement, refactorings.get(i));
//
//			if ((position[0] != -1) && (position[1] != -1))
//			{
//				refactorings.get(i).transform(refactorings.get(i).analyze(iteration, position[0], position[1]));
//				newScore = ff.calculateScore(m, this.c.getConfiguration());
//				refactorings.get(i).transform(refactorings.get(i).analyzeReverse());
//
//				if (newScore > currentScore)
//				{
//					neighbour[0] = position[0];
//					neighbour[1] = position[1];
//					neighbour[2] = i;
//					
//					if (steepestAscent)
//						currentScore = newScore;
//					else
//						break;
//				}
//			}
//		}
//		
//		return neighbour;
//	}
//
//
//  // The method used by the original simulated annealing algorithm to get neighbouring refactorings.
//	// A neighbour will be chosen using the list of available refactorings and
//	// the applicable elements on either side of the current position. If the 
//	// element is at the edge of a compilation unit the next class will be used.
//	// The neighbour returned will contain the element and refactoring that improves 
//	// the current score, or if the refactoring doesn't improve the score it may still
//	// be returned, depending on the current temperature value and the magnitude of the difference.
//	protected int[] getNeighbour(int currentUnit, int currentElement, int iteration, ArrayList<Refactoring> refactorings, 
//			float currentTemperature, FitnessFunction ff, Metrics m, float currentScore)
//	{
//		float newScore = currentScore;
//		int[] position = new int[2];
//		int[] neighbour = new int[3];
//		neighbour[0] = currentUnit;
//		neighbour[1] = currentElement;
//		neighbour[2] = -1;
//
//		for (int i = 0; i < refactorings.size(); i++)
//		{
//			double random = Math.random();
//			if (random >= 0.5)
//				position = nextElementUp(currentUnit, currentElement, refactorings.get(i));
//			else
//				position = nextElementDown(currentUnit, currentElement, refactorings.get(i));
//
//			if ((position[0] != -1) && (position[1] != -1))
//			{
//				refactorings.get(i).transform(refactorings.get(i).analyze(iteration, position[0], position[1]));
//				newScore = ff.calculateScore(m, this.c.getConfiguration());
//				refactorings.get(i).transform(refactorings.get(i).analyzeReverse());
//
//				if (newScore > currentScore)
//				{
//					neighbour[0] = position[0];
//					neighbour[1] = position[1];
//					neighbour[2] = i;
//					break;
//				}
//				else
//				{
//					// Probability of accepting negative move = exponential((-)difference/current temperature).
//					// Exponential of a negative value is confined to the range 0 -> 1 as the negative value approaches 0. 
//					float probability = (float) Math.exp((newScore - currentScore) / currentTemperature);
//					
//					if (probability > Math.random())
//					{
//						neighbour[0] = position[0];
//						neighbour[1] = position[1];
//						neighbour[2] = i;
//						break;
//					}
//				}
//			}
//
//			if (random >= 0.5)
//				position = nextElementDown(currentUnit, currentElement, refactorings.get(i));
//			else
//				position = nextElementUp(currentUnit, currentElement, refactorings.get(i));
//
//			if ((position[0] != -1) && (position[1] != -1))
//			{
//				refactorings.get(i).transform(refactorings.get(i).analyze(iteration, position[0], position[1]));
//				newScore = ff.calculateScore(m, this.c.getConfiguration());
//				refactorings.get(i).transform(refactorings.get(i).analyzeReverse());
//				
//				if (newScore > currentScore)
//				{
//					neighbour[0] = position[0];
//					neighbour[1] = position[1];
//					neighbour[2] = i;
//					break;
//				}
//				else
//				{
//					// Probability of accepting negative move = exponential((-)difference/current temperature).
//					// Exponential of a negative value is confined to the range 0 -> 1 as the negative value approaches 0. 
//					float probability = (float) Math.exp((newScore - currentScore) / currentTemperature);
//					
//					if (probability > Math.random())
//					{
//						neighbour[0] = position[0];
//						neighbour[1] = position[1];
//						neighbour[2] = i;
//						break;
//					}
//				}
//			}
//		}
//
//		return neighbour;
//	}
//
//
//  // Older method for calculating the amount of distinct class hierarchies in a project.
//	// Uses getBaseClassType although this will return the class type and not the parent classes.
//	public int numberOfHierarchies()
//	{
//		ArrayList<String> types = new ArrayList<String>();
//
//		for (int i = 0; i < this.units.size(); i++)
//			for (TypeDeclaration td : this.units.get(i).getDeclarations())
//				if ((!td.isInner()) && !(td instanceof EnumDeclaration) && !(td instanceof TypeParameterDeclaration))
//					types.add(td.getBaseClassType().getName());
//					
//		Set<String> distinctTypes = new HashSet<String>(types);
//		return distinctTypes.size();
//	}
//
//
//	// Older method to caulate average amount of classes away from the root per class.
//	// Uses getContainingClassType although this will return the class type and not the parent classes.
//	public float averageNumberOfAncestors()
//	{
//		int counter;
//		int classCounter = 0;
//		int accumulativeCounter = 0;
//
//		for (int i = 0; i < this.units.size(); i++)
//		{
//			for (TypeDeclaration td : this.units.get(i).getDeclarations())
//			{
//				if ((!td.isInner()) && !(td instanceof EnumDeclaration) && !(td instanceof TypeParameterDeclaration))
//				{					
//					classCounter++;
//					counter = 0;
//					while (td.getContainingClassType() != null)
//					{
//						td = td.getContainingClassType();
//						counter++;
//					}
//					accumulativeCounter += counter;
//				}
//			}
//		}
//
//		return accumulativeCounter/classCounter;
//	}
//
//
//	// Find type type declaration that relates to the class type.
//	// This was used in the move method refactoring but it was inefficient and 
//  // I can use the compilation unit itself to inpsect the type declarations so it was replaced.
//	TreeWalker tw2 = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(0));
//	tw2.next(TypeDeclaration.class);
//	TypeDeclaration st = (TypeDeclaration) tw2.getProgramElement();
//	while (!st.getFullName().equals(ct.getFullName()))
//		tw2.next(TypeDeclaration.class);
//
//  // Used to find the type declaration that corresponds to a class type and to 
//  // deduce whether the class type is or is not part of an external library.
//  // No longer needed after finding out you can check the class type itself.
//	else
//	{
//		ClassType ct = td.getSupertypes().get(0);
//		
//		for (int i = 0; i < getSourceFileRepository().getKnownCompilationUnits().size(); i++)
//		{
//			for (int j = 0; j < getSourceFileRepository().getKnownCompilationUnits().get(i).getTypeDeclarationCount(); j++)
//			{
//				std = getSourceFileRepository().getKnownCompilationUnits().get(i).getTypeDeclarationAt(j);
//				
//				if (std.getFullName().equals(ct.getFullName()))
//				{
//					this.superUnit = getSourceFileRepository().getKnownCompilationUnits().get(i);
//					this.superDeclaration = std;
//					break;
//				}
//			}
//			
//			if (std.getFullName().equals(ct.getFullName()))
//				break;
//		}
//		
//		if (!std.getFullName().equals(ct.getFullName()))
//			return false;
//
//
//  // Used to find if the super class has a declaration of the same method
//  // (i.e. an abstract declaration) and if so, to remove it. Used MethodKit.getRedefinedMethods
//  // instead to find the methods that the current method redefines or overwrites and checks from them.
//	for (MemberDeclaration m : this.superDeclaration.getMembers())
//	{
//		if (m instanceof MethodDeclaration) 
//		{
//			if (((MethodDeclaration) m).getName().equals(md.getName()))
//			{
//				detach(m);
//				break;
//			}
//		}
//	}
//
//
//  // Attempt to extract from a class the imports that relate to a specific method in the class.
//	ASTList<Import> currentUnitImports = getSourceFileRepository().getKnownCompilationUnits().get(unit).getImports();
//	ASTList<Import> methodImports = currentUnitImports;
//	methodImports.clear();
//	for (int i = 0; i < currentUnitImports.size(); i++)
//	{
//		boolean add = false;
//		for (int j = 0; i < currentUnitImports.get(i).getTypeReferenceCount(); j++)
//		{
//			// there must be at least one reference to this import
//			if (TypeKit.getReferences(getServiceConfiguration().getCrossReferenceSourceInfo(), getServiceConfiguration().
//				getCrossReferenceSourceInfo().getType(currentUnitImports.get(i).getTypeReferenceAt(j)), md, false).size() >= 1) 
//				add = true;
//		}
//	
//		if (add)
//			methodImports.add(currentUnitImports.get(i));
//			
//	}
//
//
//  An attempt to use getUnnecessaryImports to remove unnecessary imports in a class.
//	UnitKit.getCompilationUnit(this.superDeclaration).setImports(imports);
//	ASTList<Import> newImports = UnitKit.getCompilationUnit(this.superDeclaration).getImports();
//	newImports.removeAll(UnitKit.getUnnecessaryImports(getServiceConfiguration().getCrossReferenceSourceInfo(), 
//			                                           UnitKit.getCompilationUnit(this.superDeclaration)));
//	UnitKit.getCompilationUnit(this.superDeclaration).setImports(newImports);
//
//
//  // Originally used to check whether a type can be accessed from a different class.
//	if ((!((TypeDeclaration) t).isPublic() && !((TypeDeclaration) t).isProtected() && !((TypeDeclaration) t).isPrivate() && !(((TypeDeclaration) t).getPackage().equals(std.getPackage()))) ||
//			(((TypeDeclaration) t).isProtected() &&  (!(((TypeDeclaration) t).getPackage().equals(std.getPackage())) || !std.getAllSupertypes().contains(t))) ||
//			((((TypeDeclaration) t).isPrivate()) && !(t.equals(std))))
//		return false;
//	if ((!((ClassFile) t).isPublic() && !((ClassFile) t).isProtected() && !((ClassFile) t).isPrivate() && !(((ClassFile) t).getPackage().equals(std.getPackage()))) ||
//			(((ClassFile) t).isProtected() &&  (!(((ClassFile) t).getPackage().equals(std.getPackage())) || !std.getAllSupertypes().contains(t))) ||
//			(((ClassFile) t).isPrivate()))
//		return false;
//
//
//    // Returns all the methods referenced in a method declaration.
//    // Replaced by version of method using ExpressionContainer to represent all the different cases.
//    protected ArrayList<Method> getMethods(MethodDeclaration md, CrossReferenceSourceInfo si)
//	{
//		ArrayList<Method> methods = new ArrayList<Method>();
//		StatementKit.ControlFlowWalker cfw = new StatementKit.ControlFlowWalker(md, si.getServiceConfiguration().getSourceInfo());
//		ProgramElement pe;
//		
//		while (cfw.next())
//		{
//			pe = cfw.getProgramElement();
//			
//			if (pe instanceof MethodReference)
//			{
//				methods.add(si.getMethod((MethodReference) pe));
//				
//				for (int i = 0; i < ((MethodReference) pe).getExpressionCount(); i++)
//					if (((MethodReference) pe).getExpressionAt(i) instanceof MethodReference)
//						methods.add(si.getMethod((MethodReference) ((MethodReference) pe).getExpressionAt(i)));
//			}
//			else if (pe instanceof If)
//			{
//				for (int i = 0; i < ((If) pe).getExpressionCount(); i++)
//					if (((If) pe).getExpressionAt(i) instanceof MethodReference)
//						methods.add(si.getMethod((MethodReference) ((If) pe).getExpressionAt(i)));
//			}	
//			else if (pe instanceof Switch)
//			{
//				for (int i = 0; i < ((Switch) pe).getExpressionCount(); i++)
//					if (((Switch) pe).getExpressionAt(i) instanceof MethodReference)
//						methods.add(si.getMethod((MethodReference) ((Switch) pe).getExpressionAt(i)));
//			}	
//			else if (pe instanceof Assert)
//			{
//				for (int i = 0; i < ((Assert) pe).getExpressionCount(); i++)
//					if (((Assert) pe).getExpressionAt(i) instanceof MethodReference)
//						methods.add(si.getMethod((MethodReference) ((Assert) pe).getExpressionAt(i)));
//			}
//			else if (pe instanceof SynchronizedBlock)
//			{
//				for (int i = 0; i < ((SynchronizedBlock) pe).getExpressionCount(); i++)
//					if (((SynchronizedBlock) pe).getExpressionAt(i) instanceof MethodReference)
//						methods.add(si.getMethod((MethodReference) ((SynchronizedBlock) pe).getExpressionAt(i)));
//			}
//			else if (pe instanceof EnumConstructorReference)
//			{
//				for (int i = 0; i < ((EnumConstructorReference) pe).getExpressionCount(); i++)
//					if (((EnumConstructorReference) pe).getExpressionAt(i) instanceof MethodReference)
//						methods.add(si.getMethod((MethodReference) ((EnumConstructorReference) pe).getExpressionAt(i)));
//			}
//			else if (pe instanceof LoopStatement)
//			{
//				for (int i = 0; i < ((LoopStatement) pe).getExpressionCount(); i++)
//					if (((LoopStatement) pe).getExpressionAt(i) instanceof MethodReference)
//						methods.add(si.getMethod((MethodReference) ((LoopStatement) pe).getExpressionAt(i)));				
//			}
//			else if (pe instanceof ExpressionJumpStatement)
//			{
//				for (int i = 0; i < ((ExpressionJumpStatement) pe).getExpressionCount(); i++)
//					if (((ExpressionJumpStatement) pe).getExpressionAt(i) instanceof MethodReference)
//						methods.add(si.getMethod((MethodReference) ((ExpressionJumpStatement) pe).getExpressionAt(i)));
//			}
//			else if (pe instanceof SpecialConstructorReference)
//			{
//				for (int i = 0; i < ((SpecialConstructorReference) pe).getExpressionCount(); i++)
//					if (((SpecialConstructorReference) pe).getExpressionAt(i) instanceof MethodReference)
//						methods.add(si.getMethod((MethodReference) ((SpecialConstructorReference) pe).getExpressionAt(i)));
//			}
//			else if ((pe instanceof Assignment) || (pe instanceof New) || (pe instanceof ParenthesizedExpression))
//			{
//				for (int i = 0; i < ((Operator) pe).getExpressionCount(); i++)
//					if (((Operator) pe).getExpressionAt(i) instanceof MethodReference)
//						methods.add(si.getMethod((MethodReference) ((Operator) pe).getExpressionAt(i)));
//			}
//		}
//		
//		return methods;
//	}
//
//
//  // Code written to check for all the types referenced in a method declaration 
//  // whether they can be accessed after moving the method to its supertype.
//  // Get types accessed in method.
//	ArrayList<Type> types = super.getTypes(md, si);
//	
//	// Check if types can be accessed in super type.
//	for (Type t : types)
//	{
//		if (!((TypeDeclaration) t).isInner())
//			continue;			
//	
//		if (t instanceof TypeDeclaration)
//		{
//			if (((TypeDeclaration) t).isPrivate())
//			{
//				if (!(t.equals(std)))
//					return false;
//			}
//			else if (!((TypeDeclaration) t).isPublic())
//			{
//				if (!((TypeDeclaration) t).getPackage().equals(std.getPackage()))
//					return false;
//	
//				if (((TypeDeclaration) t).isProtected())
//					if (!(std.getAllSupertypes().contains(t)))
//						return false;
//			}					
//		}
//		else if (t instanceof ClassFile)
//		{
//			if (((ClassFile) t).isPrivate())
//				return false;
//			else if (!((ClassFile) t).isPublic())
//			{
//				if (!((ClassFile) t).getPackage().equals(std.getPackage()))
//					return false;
//	
//				if (((ClassFile) t).isProtected())
//					if (!(std.getAllSupertypes().contains(t)))
//						return false;
//			}					
//		}
//	}
//
//
//	// Original attempt to remove redundant imports from the current class in the
//	// super class after moving a method there by using a recoder transformation.
//	// Class didn't seem to work so found the relevant imports manually using 
//	// the list of method types referenced.
//	// Construct refactoring transformation.
//	ArrayList<CompilationUnit> classPair = new ArrayList<CompilationUnit>();
//	classPair.add(UnitKit.getCompilationUnit(this.currentDeclaration));
//	classPair.add(UnitKit.getCompilationUnit(this.superDeclaration));
//	super.transformation = new RemoveUnusedImports(config);
//	report = super.transformation.analyze();
//	if (report instanceof Problem) 
//		return setProblemReport(report);
//
//
//  // Originally to be used to remove redundant imports from a class after moving a method in the class elsewhere.
//  // Problematic because the imports may be needed for other methods in the class so it isn't eorth bothering to remove them.
//	ASTList<Import> classImports = UnitKit.getCompilationUnit(this.currentDeclaration).getImports();
//	classImports.removeAll(methodImports);
//	UnitKit.getCompilationUnit(this.superDeclaration).setImports(classImports);
//
//
//  // One possible way to get the method that a program element is in, although the 
//  // command "MiscKit.getParentMemberDeclaration()" seems to work also. 
//	NonTerminalProgramElement method = mr;      
//	while (!(method.getASTParent() instanceof MethodDeclaration) && (method.getASTParent() != null))
//	  method = mr.getASTParent();
//
//
//  // Was going to be used to find fields in a class relating to a selection of methods
//  // by finding the fields referenced by each method, but I needed to chekc if the fields where 
//  // going to be used by any of the remaining methods. Doing this removed the need to check the references
//  // as I could just find the fields that couldn't be moved and remove them from the list.
//	HashSet<FieldDeclaration> fieldList = new HashSet<FieldDeclaration>();
//	for (MethodDeclaration md : this.methods)
//	{
//		for (Field f : super.getFields(md, si))
//		{
//			for (FieldDeclaration fd : this.fields)
//			{
//				if (fd.getFieldSpecifications().get(0).equals(f))
//				{
//					fieldList.add(fd);
//					break;
//				}
//			}
//		}
//	}
//
//
//  // Original code used to find applicable fields from a list that can be moved. This was originally
//  // in the analyze method of the ExtractSubclass refactoring but after realising I have to check the fields in the
//  // mayRefactor method to know if the methods being moved would have access, I returned the applicable fields from there.
//	ArrayList<FieldDeclaration> fieldsToExclude = new ArrayList<FieldDeclaration>(this.fields.size());
//	
//	for (FieldDeclaration fd : this.fields)
//	{
//		for (VariableReference vr : VariableKit.getReferences(si, fd.getFieldSpecifications().get(0), this.currentDeclaration, true))
//		{
//			if ((MiscKit.getParentMemberDeclaration(vr) == null) || !(MiscKit.getParentMemberDeclaration(vr) instanceof MethodDeclaration))
//			{
//				fieldsToExclude.add(fd);
//				break;
//			}
//			else if (!(this.methods.contains(MiscKit.getParentMemberDeclaration(vr))))
//			{
//				fieldsToExclude.add(fd);
//				break;
//			}
//		}
//	}
//		
//	this.fields.removeAll(fieldsToExclude);
//
//
//  // Used in MakeClassAbstract class to check if a reference to a type is part of a New statement.
//  // The toSource() method was yielding errors when the parent was an extends statement so changed
//  // the command to check whether the parent statement was of type New instead of checking the code directly. 
//	if (tr.getParent().toSource().contains("new " + td.getName()))
//		return false;
//
//
//  // Used to find the position of the variable in the RemoveField refactoring when it was allowed to remove local variables.
//  // Had to be removed as the cde was not applicable with local variables and would cause errors.
//	if (fd instanceof LocalVariableDeclaration)
//	{
//		this.position = 0;
//	
//		MethodDeclaration parent =  ((MethodDeclaration) MiscKit.getParentMemberDeclaration(vd));
//		for (int i = 0; i < parent.getBody().getStatementCount(); i++)
//			if (parent.getBody().getStatementAt(i).toSource().contains(vd.toSource()))
//				this.position = i;
//	
//	}
//	else
//		this.position = super.getPosition(this.type, (FieldDeclaration) vd);
//
//
//  // Also used for local variables in the RemoveField refactoring. This code would allow
//  // for the refactoring to be reversed in the case of a local variable or a global field.
//	public ProblemReport analyzeReverse() 
//	{
//		// Construct refactoring transformation.
//		super.transformation = null;
//		
//		if (this.variable instanceof FieldDeclaration)
//			attach(((FieldDeclaration) this.field), this.type, this.position);
//		else
//			this.attach((Statement) this.variable, ((MethodDeclaration) this.variable.getASTParent()).getBody(), this.position);
//		
//		return setProblemReport(EQUIVALENCE);
//	}
//
//
//  // Original methods to find all the methods referenced in a method declaration. 
//  // Used to see if another class could access all the references in a method to know if it could be moved.
//  // Noticed after the fact that an easier way to do this and make sure all the nested references are found is to use 
//  // a tree walker like in the analyze method to find program elements in a class. Also noticed for the getFIelds method
//  // that this wasn't catching every referenced so replaced it.
//	// Returns all the method references in a method declaration.
//	protected ArrayList<MethodReference> getMethods(MethodDeclaration md, CrossReferenceSourceInfo si)
//	{
//		ArrayList<MethodReference> methods = new ArrayList<MethodReference>();
//		StatementKit.ControlFlowWalker cfw = new StatementKit.ControlFlowWalker(md, si.getServiceConfiguration().getSourceInfo());
//		ProgramElement pe;
//
//		while (cfw.next())
//		{
//			pe = cfw.getProgramElement();
//
//			if (pe instanceof MethodReference)
//				methods.add((MethodReference) pe);
//
//			if (pe instanceof ExpressionContainer)
//			{				
//				for (int i = 0; i < ((ExpressionContainer) pe).getExpressionCount(); i++)
//				{
//					if (((ExpressionContainer) pe).getExpressionAt(i) instanceof ExpressionContainer)
//						methods.addAll(getMethodsRecursive((ExpressionContainer) ((ExpressionContainer) pe).getExpressionAt(i)));
//					else if (((ExpressionContainer) pe).getExpressionAt(i) instanceof MethodReference)
//						methods.add((MethodReference) ((ExpressionContainer) pe).getExpressionAt(i));
//				}
//			}
//		}
//
//		return methods;
//	}
//
//	// Returns all the method references in an expression container.
//	protected ArrayList<MethodReference> getMethodsRecursive(ExpressionContainer ec)
//	{
//		ArrayList<MethodReference> methods = new ArrayList<MethodReference>();
//		
//		if (ec instanceof MethodReference)
//			methods.add((MethodReference) ec);
//			
//		for (int i = 0; i <  ec.getExpressionCount(); i++)
//		{
//			if (ec.getExpressionAt(i) instanceof ExpressionContainer)
//				methods.addAll(getMethodsRecursive((ExpressionContainer) ec.getExpressionAt(i)));
//			else if (ec.getExpressionAt(i) instanceof MethodReference)
//				methods.add((MethodReference) ec.getExpressionAt(i));
//		}
//
//		return methods;
//	}
//
//
//  // Original methods to find all the fields referenced in a method declaration. 
//  // same as the above reason for the getMEthods() method. Replaced it with tree walker approach.
//	protected ArrayList<Field> getFields(MethodDeclaration md, CrossReferenceSourceInfo si)
//	{
//		ArrayList<Field> fields = new ArrayList<Field>();
//		StatementKit.ControlFlowWalker cfw = new StatementKit.ControlFlowWalker(md, si.getServiceConfiguration().getSourceInfo());
//		ProgramElement pe;
//
//		while (cfw.next())
//		{
//			pe = cfw.getProgramElement();
//			
//			if (pe instanceof FieldReference)
//				fields.add(si.getField((FieldReference) pe));
//			
//			if (pe instanceof ExpressionContainer)
//			{				
//				for (int i = 0; i < ((ExpressionContainer) pe).getExpressionCount(); i++)
//				{
//					if (((ExpressionContainer) pe).getExpressionAt(i) instanceof ExpressionContainer)
//						fields.addAll(getFieldsRecursive((ExpressionContainer) ((ExpressionContainer) pe).getExpressionAt(i), si));
//					else if (((ExpressionContainer) pe).getExpressionAt(i) instanceof FieldReference)
//						fields.add(si.getField((FieldReference) ((ExpressionContainer) pe).getExpressionAt(i)));
//				}
//			}
//		}
//
//		return fields;
//	}
//	
//	// Returns all the fields referenced in an expression container.
//	protected ArrayList<Field> getFieldsRecursive(ExpressionContainer ec, CrossReferenceSourceInfo si)
//	{
//		ArrayList<Field> fields = new ArrayList<Field>();
//
//		if (ec instanceof FieldReference)
//			fields.add(si.getField((FieldReference) ec));
//
//		for (int i = 0; i <  ec.getExpressionCount(); i++)
//		{
//			if (ec.getExpressionAt(i) instanceof ExpressionContainer)
//				fields.addAll(getFieldsRecursive((ExpressionContainer) ec.getExpressionAt(i), si));
//			else if (ec.getExpressionAt(i) instanceof FieldReference)
//				fields.add(si.getField((FieldReference) ec.getExpressionAt(i)));
//		}
//
//		return fields;
//	}
//
//
//	// Returns all the other classes declared in a compilation unit that 
//	// aren't equal to the current type or a directly nested class of that type.
//  // found an easier way to find out whether there are other classes in a compilation unit
//	protected int getOtherTypesCount(int unit, TypeDeclaration type)
//	{
//		int counter = 0;
//		AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
//		
//		// Only counts the relevant program elements.
//		while (tw.next(TypeDeclaration.class))
//		{
//			TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
//			
//			if ((type.getName() != null) && !(td.equals(type)) && 
//				((td.getContainingClassType() == null) || !(td.getContainingClassType().equals(type))))
//				counter++;
//		}
//
//		return counter;
//	}
//
//
//  // Used for the ExtractSubclass/MoveMethodDown/MoveFieldDown refactorings to 
//  // shuffle the class members picked from a possible selection so that it would  
//  // be semi random. The problem with this stemmed from the steepest ascent hill 
//  // climbing search. This may need to option to undo a refactoring and then redo 
//  // the exact refactoring at a later point and if it is reapplied with different 
//  // members it will be a different refactoring.
//	java.util.Collections.shuffle(availableMethods);
//
//
//  // Originally used in class refactorings in the getName method as
//  // td.getName() would sometimes cause run times errors. This has 
//  // seemingly been reoslved over time as the issue no longer appears.
//	TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
//	
//	if (td.getName() == null)
//	{
//		td.getFactory().getServiceConfiguration().getChangeHistory().updateModel();
//		return td.getFullName().replaceAll("\\d","");
//	}
//	else
//		return td.getName();
//
//  // Previous implementation for averageNumberOfAncestors metric.
//  // This included classes outside of the project when calulating the 
//  // amount of levels away from the root a class is, without including
//  // those extra classes in the list of classes counting the ancestors.
//	// Average amount of classes away from the root per class.
//	public float averageNumberOfAncestors()
//	{
//		int classCounter = 0;
//		int accumulativeCounter = 0;
//		int superTypeCounter;
// 
//		for (int i = 0; i < this.units.size(); i++)
//		{
//			for (ClassType ct : getAllTypes(this.units.get(i)))
//			{
//				if (ct.isOrdinaryClass())
//				{				
//					// Prevents "Zero Service" outputs logged to the console.
//					if (ct.getProgramModelInfo() == null)
//						((ClassDeclaration) ct).getFactory().getServiceConfiguration().getChangeHistory().updateModel();
//
//					classCounter++;
//					superTypeCounter = 0;
//					
//					while (!(ct.getSupertypes().get(0).getFullName().equals("java.lang.Object")))
//					{
//						superTypeCounter++;
//						ct = ct.getSupertypes().get(0);
//					}
//					
//					System.out.printf("|%d", superTypeCounter);
//					accumulativeCounter += superTypeCounter;
//				}
//			}
//		}
//
//		return (float) accumulativeCounter / (float) classCounter;
//	}
//
//  // Added for the normalization function to give a quick way to calculate the 
//  // original score of the fitness function. Has become redundant after changing 
//  // normalization function as the benchmark will now be 0.
//	public float calculateBenchmark()
//	{
//		float amount = 0;
//
//		for (Triple<String, Boolean, Float> metric : this.configuration)
//			if (this.initialMetrics.get(metric.getFirst()) != 0)
//				amount += (metric.getSecond() == true) ? 1 : -1;
//		
//		return amount;
//	}
//
//
//	// Finds the boundary points of the hyperplane used in NSGA-III using the individual worst
//	// objective values and translating them from the origin (i.e. the ideal point) on each axis. 
//	for (int i = 0; i <= this.c.length; i++)
//	{
//		hyperplane[i] = idealPoint;
//	
//		if (i > 0)
//			hyperplane[i][i - 1] = worstPoint[i - 1];
//	}
//
//
//  // Code was used to add an import to the package of the superclass in subclasses that were
//  // not in the same package as the superclass when a superclass was being created to derive
//  // from another class with the ExtractHierarchy refactoring. The code isn't needed as the 
//  // newly extracted class will be default be located in the same package as the original class.
//	for (ClassDeclaration cd : this.subClasses)
//	{
//		this.importSizes[this.subClasses.indexOf(cd)] = UnitKit.getCompilationUnit(cd).getImports().size();
//	
//		if (!(cd.getPackage().equals(this.subDeclaration.getPackage())))
//		{
//			Import i = getProgramFactory().createImport(PackageKit.createPackageReference(getProgramFactory(), 
//					this.subDeclaration.getPackage()));
//	
//			if (!(UnitKit.getCompilationUnit(cd).getImports().contains(i)))
//				attach(i, UnitKit.getCompilationUnit(cd), UnitKit.getCompilationUnit(cd).getImports().size());
//		}
//	}
//
//
//	// Checks priority classes that have been input to remove classes that
//  // don't exist in the model. Removed this as it wasn't really necessary.
//  // Even if nonexistent classes are included the metric will come out the same.
//	ArrayList<String> temp = new ArrayList<String>(priorityClasses);
//	
//	for (int i = 0; i < priorityClasses.size(); i++)
//	{
//		boolean breakout = false;
//	
//		for (int j = 0; j < this.units.size(); j++)
//		{
//			for (TypeDeclaration td : getAllTypes(this.units.get(j)))
//			{
//				if (td.getName().equals(temp.get(i)))
//				{
//					breakout = true;
//					break;
//				}
//			}
//	
//			if (breakout)
//				break;
//		}
//	
//		if (!breakout)
//			priorityClasses.remove(temp.get(i));				
//	}
//	
//	priorityClasses.trimToSize();
//
//
//  // Checks any reference to the method in the program and if it
//  // refers explicitly to the current class it will not be applicable.
//  // This was used in ExtractSubclass but was replaced by more detailed code
//  // that concerned itself also with subclasses referencing the method.
//	for (MemberReference mr : si.getReferences((Method) md))
//	{
//		if (!(mr instanceof MethodReference))
//		{
//			next = true;
//			break;
//		}
//	
//		if ((((MethodReference) mr).getReferencePrefix() instanceof TypeReference) || 
//				((si.getType(((MethodReference) mr).getReferencePrefix()) != null) && 
//						!(((MethodReference) mr).getReferencePrefix() instanceof ThisReference) && 
//						!(((MethodReference) mr).getReferencePrefix() instanceof SuperReference) && 
//						(si.getType(((MethodReference) mr).getReferencePrefix()).equals(td))))
//		{  
//			next = true;
//			break;
//		}						
//	}
//
//
//	// Modified version of the number of methods metric that output the 
//  // amount of methods per class. Used in order to choose priority and 
//  // non priority classes with the inputs used in the priority experiment.
//	public float numberOfMethods()
//	{
//		int classCounter = 0;
//		int methodCounter = 0;
//		System.out.printf("\n\nINPUT, Number Of Methods");
//
//		for (int i = 0; i < this.units.size(); i++)
//		{			
//			for (TypeDeclaration td : getAllTypes(this.units.get(i)))
//			{
//				if ((td instanceof ClassDeclaration) || (td instanceof InterfaceDeclaration))
//				{
//					int classMethodCounter = 0;
//					classCounter++;
//					
//					for (MemberDeclaration md : td.getMembers())
//					{
//						if (md instanceof MethodDeclaration)
//						{
//							methodCounter++;
//							classMethodCounter++;
//						}
//					}
//					
//					System.out.printf("\n%s, %d", td.getName(), classMethodCounter);
//				}
//			}
//		}
//
//		return (float) methodCounter / (float) classCounter;
//	}
//
//
//  // Original code used to name sub classes created in the ExtractSubclass refactoring.
//  // Would add a random alphanumeric character each time the class name already existed.
//  // This would create different class names when a refactoring sequence was reconstructed.
//  // Therefore it was changed to ad a number that would gradually increment instead.
//	while (typeNames.contains(name))
//	{
//		if (name.equals(this.currentDeclaration.getIdentifier().getText() + "_SubClass"))			
//			name = name + "_";
//	
//		name = name + (char)(((int)(Math.random() * 26)) + 'a');
//	}
//
//
//  // Code was used in the move refactorings to place the element at a pseudo-random point in the class.
//  // This was changed after realising that this could affect how the refactoring is applied when the model 
//  // is reconstructed. The refactoring needs to be applied the same way or it could cause trouble later on.
//	if ((this.randomPosition == -1) && !(this.superDeclaration.getMembers().isEmpty()))
//	{
//		ASTList<MemberDeclaration> members = this.superDeclaration.getMembers();
//		for (int i = members.size() - 1; i >= 0; i--)
//		{
//			if (members.get(i) instanceof FieldDeclaration)
//			{
//				this.randomPosition = i + 1;
//				break;
//			}
//			else if (i == 0)
//				this.randomPosition = 0;
//		}
//	
//		// Generate random position between the last field declaration in the class and the end of the class.
//		this.randomPosition = this.randomPosition + (int)(Math.random() * (this.superDeclaration.getMembers().size() + 1 - this.randomPosition));
//	}
//	else if (this.randomPosition == -1)
//		this.randomPosition = 0;
//
//
//  // Two methods originally used for priority metric. The ratio meant that less refactorings translated to a better score
//  // and therefore, the results would use less refactorings and give a worse score for the other quality objective.
//	public float priorityClassRatio(ArrayList<String> priorityClasses)
//	{		
//		int priorityAmount = 0;
//		
//		if (this.affectedClasses.size() == 0)
//			return 0;
//		
//		for (String s : this.affectedClasses)
//			if (priorityClasses.contains(s))
//				priorityAmount++;
//		
//		return priorityAmount / (float) this.affectedClasses.size();	
//	}
//	
//	public float priorityClassRatio(ArrayList<String> priorityClasses, ArrayList<String> nonPriorityClasses)
//	{				
//		int nonPriorityAmount = 0;
//		float nonPriorityRatio;
//		float priorityRatio = priorityClassRatio(priorityClasses);
//
//		for (String s : this.affectedClasses)
//			if (nonPriorityClasses.contains(s))
//				nonPriorityAmount++;
//		
//		nonPriorityRatio = (this.affectedClasses.size() == 0) ? 0 : nonPriorityAmount / (float) this.affectedClasses.size();
//		return priorityRatio - nonPriorityRatio;
//	}
//
//
//  // Older version of mayRefactor method for MakeFieldFinal refactoring. There was a check to the constructors to see if
//  // they initialize the variable but this shouldn't be necessary because it would be included in the getReferences method call.
//	public boolean mayRefactor(VariableDeclaration vd)
//	{				
//		if ((vd.isFinal()) || (MiscKit.getParentTypeDeclaration(vd) instanceof EnumDeclaration))
//			return false;
//		else
//		{			
//			if (!(vd.toSource().contains("=")))
//			{	
//				int references = 0;
//				boolean check = false;
//	
//				for (VariableReference vr : getCrossReferenceSourceInfo().getReferences(vd.getVariables().get(0)))
//				{
//					if (vr.getASTParent().toSource().contains(vd.getVariables().get(0).getName() + " ="))
//					{
//						references++;
//						
//						if (references > 1)
//							return false;
//					}
//				}
//						
//				for (MemberDeclaration md : MiscKit.getParentTypeDeclaration(vd).getMembers())
//				{
//					if (md instanceof ConstructorDeclaration)
//					{						
//						for (int i = 0; i < ((ConstructorDeclaration) md).getStatementCount(); i++)
//						{
//							if (((ConstructorDeclaration) md).getStatementAt(i).toSource().contains(vd.getVariables().get(0).getName() + " ="))
//							{
//								check = true;
//								break;
//							}
//						}
//	
//						if (check)
//							break;
//					}
//				}
//	
//				if ((check) && (references > 1))
//					return false;
//			}
//			else
//			{
//				for (VariableReference vr : getCrossReferenceSourceInfo().getReferences(vd.getVariables().get(0)))
//					if (vr.getASTParent().toSource().contains(vd.getVariables().get(0).getName() + " ="))
//						return false;
//			}
//	
//			return true;
//		}
//	}
//
//
//  // Originally used during refactoring info output to update the iterations in 
//  // relation to the final solution. This is no longer needed as in order to implement 
//  // the refactoring choices information, this had to be updated as the search progressed
//  // and so the iterations are accurate when the refactoring info is being output.
//	for (int i = 0; i < refactoringInfo.size(); i++) 
//	{
//		String info = "Iteration " + (i + 1) + refactoringInfo.get(i).substring(refactoringInfo.get(i).indexOf(':'));
//		bw.append(String.format("\r\n%s", info));
//	}
//
//
//  // Original version of method to check element and return whether is is refactorable
//  // without the need to otherwise use the mayRefactor check. It turned out I was using 
//  // getAmount to find the element number so it would've happened anyway. I modified the
//  // method in order to fit in with the outside code and reduce the use of mayRefactor.
//	public boolean isRefactorable(int unit, int element)
//	{		
//		AbstractTreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
//
//		for (int i = 0; i < element; i++)
//			tw.next(FieldDeclaration.class);
//
//		FieldDeclaration fd = (FieldDeclaration) tw.getProgramElement();
//		return mayRefactor(fd);
//	}
//
//  // Code was used in nextElementDown but wasn't actually being reached or 
//  // needed in the program. nextElementDown and nextElementUp was changed
//  // to find the next element in the next unit, as if there were other elements
//  // available in the current unit, the methods woulddn't need to be called.
//
//  // This is a makeshift solution to using the method consecutively 
//	// for different refactorings. If the element from the previous refactoring
//	// is out of bounds of the current refactoring, the last element in the class
//	// of the previous refactoring is used. This may not be the closest element
//	// to the previous point in the program, but getting that would be awkward.
//	else if (currentElement > 0)
//	{
//		int amount = 0;
//		
//		while (amount == 0)
//		{
//			currentUnit--;
//			
//			if (currentUnit < 0)
//			{
//				position[0] = -1;
//				position[1] = -1;
//				amount = -1;
//			}
//			// Only counts the relevant program element.
//			else
//				amount = r.getAmount(currentUnit);
//		}
//		
//		if (amount != -1)
//		{
//			position[0] = currentUnit;
//			position[1] = amount;
//		}
//	}
//	else
//	{
//		position[0] = currentUnit;
//		position[1] = currentElement - 1;
//	}
//
//
//  // Code was used to sort the list of sub types of a class by alphabetical order of name.
//  // The sorting isn't needed as the list order will be the same on refactoring reconstruction anyway.
//	Collections.sort(subtypes,  new NameComparator());
//	
//	// This inner class allows sorting by name so that the list is sorted alphanumerically by the class names.
//	private class NameComparator implements Comparator<ClassType> 
//	{
//		// Compares the two specified individuals using the fitness
//		// operator. Returns less than 1, 0 or more than 1 as the first
//		// argument is less than, equal to, or greater than the second.
//		public int compare(ClassType ct1, ClassType ct2) 
//		{   
//			return ct1.getName().compareTo(ct2.getName());
//		}
//	}
//
//
//  // Different variations of the method that was used to find the name relating to the element
//  // corresponding to a refactoring. It was necessary to check for the element in a unit when
//  // reconstructing the refactoring in case it is present in a different position, although this
//  // was changed to use the refactoringInfo string instead to extract the element name.
//	public String getName(int unit, int element)
//	{		
//		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
//	
//		for (int i = 0; i < element; i++)
//			tw.next(FieldDeclaration.class);
//	
//		FieldDeclaration fd = (FieldDeclaration) tw.getProgramElement();
//		return fd.toString();
//	}
//
//	public String getName(int unit, int element)
//	{
//		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
//	
//		for (int i = 0; i < element; i++)
//			tw.next(VariableDeclaration.class);
//	
//		VariableDeclaration vd = (VariableDeclaration) tw.getProgramElement();
//		return vd.toString();
//	}
//	
//	public String getName(int unit, int element)
//	{		
//		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
//	
//		for (int i = 0; i < element; i++)
//			tw.next(MethodDeclaration.class);
//	
//		MethodDeclaration md = (MethodDeclaration) tw.getProgramElement();
//		return md.getName();
//	}
//	
//	public String getName(int unit, int element)
//	{
//		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
//	
//		for (int i = 0; i < element; i++)
//			tw.next(TypeDeclaration.class);
//		
//		TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
//		return td.getName();
//	}
//
//	public String getName(int unit, int element)
//	{
//		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
//
//		for (int i = 0; i < element; i++)
//			tw.next(InterfaceDeclaration.class);
//		
//		InterfaceDeclaration id = (InterfaceDeclaration) tw.getProgramElement();
//		return id.getName();
//	}
//
//
//  Version of method override for ExtractSubclass that could be used
//  to check that the subclasses being changed to derive from the new  
//  class, if any, are the same when the refactoring is reconstructed.
//	public int checkElements(int unit, String refactoringInfo)
//	{		
//		TreeWalker tw = new TreeWalker(getSourceFileRepository().getKnownCompilationUnits().get(unit));
//		int element = 0, last;
//		int from  = refactoringInfo.indexOf(" class ") + 7;
//		int to = refactoringInfo.indexOf(' ', from);
//		String name = refactoringInfo.substring(from,  to);
//		
//		from = refactoringInfo.indexOf(" element(s) ") + 13; 
//		to = refactoringInfo.indexOf(" in class ") - 1;
//		String[] elementNames = refactoringInfo.substring(from, to).split(", ");
//		Arrays.sort(elementNames);
//		
//		from = refactoringInfo.indexOf(" --- ") + 5; 
//		to = refactoringInfo.length();
//		String[] subclassNames = refactoringInfo.substring(from, to).split(", ");
//		Arrays.sort(subclassNames);
//		
//		while (tw.next(TypeDeclaration.class))
//		{
//			element++;
//			TypeDeclaration td = (TypeDeclaration) tw.getProgramElement();
//			
//			if ((td.getName() != null) && (td.getName().equals(name)) && (mayRefactor(td)))
//			{
//				ArrayList<String> elements = new ArrayList<String>();		
//				ArrayList<String> subclasses = new ArrayList<String>();
//	
//				for (MethodDeclaration md : this.methods)
//					elements.add(md.getName());
//	
//				for (FieldDeclaration fd : this.fields)
//				{
//					last = fd.toString().lastIndexOf(">");
//					elements.add(fd.toString().substring(last + 2));
//				}
//				
//				for (ClassDeclaration ct : this.subclasses)
//					subclasses.add(ct.getName());
//	
//				Collections.sort(elements);
//				Collections.sort(subclasses);
//				if ((Arrays.equals(elements.toArray(), elementNames)) && (Arrays.equals(subclasses.toArray(), subclassNames)))
//					return element;
//				else 
//					return -1;
//				return (Arrays.equals(elements.toArray(), elementNames)) ? element : -1;
//			}
//		}
//	
//		return -1;
//	}
//
//
//  // Was used in the Tasks class to create the data output directory, but wasn't
//  // really necessary as I could just specify the directory as it was added in.
//	public String createOutputDirectory(String pathName, String name)
//	{
//		File filePath = new File(pathName);
//		String output = "./data/refactored/" + filePath.getName() + "/" + name;	
//		return output;
//	}