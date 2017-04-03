/**
  * @author THOMAS Alexandre
  * @author AUDRU Cedric
  */


import java.awt.event.*;


/**
  * Class allow to close the window
  */
public class GanttWindowCloser extends WindowAdapter {

	/** 
	  * Constructor 
	  */
	public void windowClosing( WindowEvent e) {
		System. exit(0);
	}
}



