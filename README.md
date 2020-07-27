# Basic Guide To Run MultiRefactor

**MultiRefactor** was developed on a Windows PC and as such, can only be confirmed to be supported on the Windows platform. It may need to be amended to be able to run on other platforms.

The tool was developed in **Java** using the **Eclipse** Integrated Development Environment (IDE). To open the project within **Eclipse**, you can select *File > Import*, then *Existing Projects into Workspace*, then *Select root directory:*. From here, you can select the root directory of the **MultiRefactor** repository to import it into **Eclipse**. If not using **Eclipse** or another **Java** friendly IDE, you can build and run the tool using the command line. To do this, follow the steps below:

1. First, ensure you have **Java** installed on your computer. You will need to install the Java Development Kit (JDE) on your system, and be able to run **Java** commands on the command line (a tutorial for this can be followed [here](https://docs.oracle.com/javase/tutorial/getStarted/cupojava/win32.html)).

2. Open an **MSDOS** terminal and type `javac`, then `java`. If **Java** is installed successfully, both commands should generate a help menu explaining the capabilities of the command.

3. To compile the project on a windows machine, open an **MSDOS** terminal within the main project directory and enter `javac -cp lib/* -d bin src/multirefactor/* src/refactorings/*.java src/refactorings/field/* src/refactorings/method/* src/refactorings/type/* src/searches/* src/tasks/*.java src/tasks/objectivesexperiment/* src/tasks/toolexperiment/*`.
    - `javac`: Compiles the source code to bytecode using **Java**.
    - `-cp` (-classpath): Specifies where the jar files containing the external libraries (ASM, RECODER) are.
    - `-d`: Specifies where to place the bytecode files generated during compilation.
    - The other directories specify where the **Java** source files within the project reside.

4. To run the project, run the *MultiRefactor.bat* file within the root directory of the project. This will run **Java**, pointing to the location of the compiled bytecode files and the location of the jar files containing the external libraries. This will run the program from the *Main.java* class within the *src/multirefactor* directory.

5. By default the program will run on the json-1.1 source code. To specify a different codebase to run the program on, pass in the argument `-f` followed by the relative path to the input directory. Alternatively, use the argument `-r` followed by a file that contains the path to the desired input directory.
