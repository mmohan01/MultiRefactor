package net.sourceforge.ganttproject.gui;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author not attributable
 * @version 1.0
 */


import java.awt.Container;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import java.net.URL;

import java.io.IOException;
import java.io.InputStream;

import net.sourceforge.ganttproject.GanttProject;


/** 
  * Ganttproject Applet
  */

public class GanttApplet extends JApplet implements ActionListener{
  private String fileLocation="testSimple.xml";;

  private JButton button=new JButton("Click to view the project planer");;
  public GanttApplet() {
  }

  public void init(){
    String fileLocationParam = getParameter("fileName");

    if(fileLocationParam!=null){
      fileLocation=fileLocationParam;
    }
    setContentPane(createContainer());
  }

  private Container createContainer(){
   JPanel panel=new JPanel();
   button.addActionListener(this);
   panel.add(button);
   return panel;
  }

  public void actionPerformed(ActionEvent e){
    GanttProject ganttFrame = new GanttProject(null, true);
    try{
      URL url=new URL(getCodeBase().toString()+fileLocation);
      InputStream inS=url.openConnection().getInputStream();//new FileInputStream("C:\\Documents and Settings\\xxx\\My Documents\\testSimple.xml");
      ganttFrame.openXMLStream(inS, getCodeBase().toString()+fileLocation);
    }catch(IOException ex){
      ex.printStackTrace();
    }
    ganttFrame.setVisible(true);
  }

  public static void main(String[] args) {
    GanttApplet applet=new GanttApplet();
    JFrame frame=new JFrame("Test GanttApplet");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setContentPane(applet.createContainer());
    frame.pack();
    frame.setVisible(true);
  }
}
