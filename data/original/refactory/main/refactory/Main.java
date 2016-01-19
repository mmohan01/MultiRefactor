package refactory;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

// Writes to console when program begins and ends.
// Starts a new instance of Tasks (this is where the 
// various tasks should be declared to be run automatically). 
public class Main 
{
	public static void main(String[] args)
	{
		System.out.printf("Automated Refactoring Tool Running");

		if (args.length > 0) 
		{
			if (args[0].equals("-f"))
			{
				Tasks run = new Tasks(args[1]);
				run.run();
			}
			else if (args[0].equals("-r"))
			{
				try 
				{
					BufferedReader br = new BufferedReader(new FileReader(args[1]));
					Tasks run = new Tasks(br.readLine());
					run.run();
					br.close();
				} 
				catch (IOException e) 
				{
					System.out.println("\nEXCEPTION: Cannot read source path from file.");
					System.exit(1);
				}
			}
			else
			{
				System.out.print("\n\nArgument not applicable. Input arguments must consist of one of the following:\n"
						+ " -f to pass in a directory containing the input\n"
						+ " -r to read in a file containing the input directory");
			}
		} 
		else 
		{
			Tasks run = new Tasks();
			run.run();
		}
		
		System.out.printf("\n\nFinished!");
	}
}
