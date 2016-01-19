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
//  // elements areand save having to check twice.
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