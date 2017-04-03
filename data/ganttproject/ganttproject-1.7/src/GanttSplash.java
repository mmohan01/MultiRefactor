
/**
  * @author THOMAS Alexandre
  * @author AUDRU Cedric
  */



import javax.swing.*;
import java.awt.*;
import javax.swing.border.LineBorder;

/**
 * Class to put a splash befor lunch the soft
 */

class GanttSplash extends JWindow {

    public GanttSplash(){
        JLabel l = new JLabel(new ImageIcon("icons/splash.gif"));
        getContentPane().add(l, BorderLayout.CENTER);
        pack();

        Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
        Dimension labelSize = l.getPreferredSize();

        // Put image at the middle of the screen
        setLocation(screenSize.width/2 - (labelSize.width/2),
                    screenSize.height/2 - (labelSize.height/2));
		
    }

    public void close() {
       //try{
         //Thread.sleep(2000); //wait
          setVisible(false);
    	  dispose();
        //} catch (InterruptedException e) {}
    }

}

