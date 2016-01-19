package edu.atilim.acma;

import java.io.IOException;
import java.util.Arrays;
import java.util.UUID;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import edu.atilim.acma.ui.MainWindow;
import edu.atilim.acma.ws.WebServiceEngine;

public class Core {
    public static final String VERSION = "1.0.0.0a";
    public static final UUID NODE_ID = UUID.randomUUID();
    public static boolean paretoMode = false;

    public static void main(String[] args) throws IOException {
        System.out.println(Integer.MAX_VALUE);

        System.out.printf("A-CMA Software Refactoring Tool - version %s\n\n", VERSION);

        if (args.length > 0) {
            Runnable runner = null;
            if (args[0].equals("-s") || args[0].equals("-S")) {
                int port = 2593;
                try { port = Integer.parseInt(args[1]); } catch (Exception e) {}
                runner = new Server(port);
            } else if (args.length > 1 && (args[0].equals("-c") || args[0].equals("-C"))) {
                int port = 2593;
                try { port = Integer.parseInt(args[2]); } catch (Exception e) {}
                runner = new Client(args[1], port);
            } else if (args[0].equals("-ai")) {
                runner = new ActionImpactCalculator();
            } else if (args[0].equals("-pf")) {
                runner = new ParetoFrontCalculator();
            } else if (args[0].equals("-nx")) {
                runner = new NodeXPCalculator();
            } else if (args[0].equals("-ne")) {
                runner = new NodeExpansionRunGenerator();
            } else if (args[0].equals("-ct")) {
                runner = new CustomTaskQueueGenerator();
            } else if (args[0].equals("-ws")) {
                int port = -1;
                try { port = Integer.parseInt(args[1]); } catch (Exception e) {}
                if (port > 0) WebServiceEngine.port = port;

                runner = WebServiceEngine.getInstance();
            }

            if (runner == null) {
                System.out.println("Please use either -s <port> or -c hostname <port> arguments for server and client modes respectively.");
                System.exit(0);
            }

            if (Arrays.asList(args).contains("-wb")) {
                WikiBot.start();
            }

            runner.run();
        } else {
            SwingUtilities.invokeLater(new Runnable(){
                @Override
                 public void run() {
                    try {
                        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                    } catch (Exception e) {}
                    MainWindow.getInstance().setVisible(true);
                }
            });
        }
    }
}
