/***************************************************************************
                           GanttDialogDate.java  -  description
                             -------------------
    begin                : sep 2003
    copyright            : (C) 2003 by Thomas Alexandre
    email                : alexthomas(at)ganttproject.org
 ***************************************************************************/

/***************************************************************************
 *                                                                         *
 *   This program is free software; you can redistribute it and/or modify  *
 *   it under the terms of the GNU General Public License as published by  *
 *   the Free Software Foundation; either version 2 of the License, or     *
 *   (at your option) any later version.                                   *
 *                                                                         *
 ***************************************************************************/


package net.sourceforge.ganttproject.gui;

import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyleContext;

import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.language.GanttLanguage;

/**
 * Create a dialog box to show tips of the day
 * created September 10, 2003
 */
public class TipsDialog extends JDialog 
{


JPanel pnPanel0;
JTextPane taArea0;
JButton btBut0;
JButton btBut1;
JButton btBut2;
JCheckBox cbBox0;
JLabel lbImg;
GanttProject appli;
String [] tipsText;
int index;

/**Constructor
 */
public TipsDialog(JFrame parent, boolean tips) 
{
   super(parent,GanttProject.correctLabel(GanttLanguage.getInstance().getText("tipsOfTheDay")),true);

   appli=(GanttProject)parent;	
   GanttLanguage lang = GanttLanguage.getInstance();
   
	 tipsText = new String []{
	 	lang.getText("tips1"), lang.getText("tips2"), lang.getText("tips3"), lang.getText("tips4"), 
		lang.getText("tips5"), lang.getText("tips6"), lang.getText("tips7"), lang.getText("tips8"), 
		lang.getText("tips9"), lang.getText("tips10"), lang.getText("tips11"),lang.getText("tips12")	 ,
		lang.getText("tips13"),lang.getText("tips14"), lang.getText("tips15")
	 };
	 
   index=(int)(Math.random()*tipsText.length);
   
	
   pnPanel0 = new JPanel();
   GridBagLayout gbPanel0 = new GridBagLayout();
   GridBagConstraints gbcPanel0 = new GridBagConstraints();
   pnPanel0.setLayout( gbPanel0 );

   
	 //Light image
   lbImg = new JLabel(new ImageIcon(getClass().getResource("/icons/info.png")));
   gbcPanel0.gridx = 0;
   gbcPanel0.gridy = 0;
   gbcPanel0.gridwidth = 1;
   gbcPanel0.gridheight = 18;
   gbcPanel0.fill = GridBagConstraints.VERTICAL;
   gbcPanel0.weightx = 0;
   gbcPanel0.weighty = 1;
   gbcPanel0.anchor = GridBagConstraints.NORTH;
   gbPanel0.setConstraints( lbImg, gbcPanel0 );
   pnPanel0.add( lbImg );
   
   
	 //The panel
   taArea0 = new JTextPane();
	 taArea0.setPreferredSize(new Dimension(200,150));
   taArea0.setEditable(false);
	 //Set a random text !!
   changeText(tipsText[index]);
   gbcPanel0.gridx = 1;
   gbcPanel0.gridy = 0;
   gbcPanel0.gridwidth = 19;
   gbcPanel0.gridheight = 18;
   gbcPanel0.fill = GridBagConstraints.BOTH;
   gbcPanel0.weightx = 1;
   gbcPanel0.weighty = 1;
   gbcPanel0.anchor = GridBagConstraints.NORTH;
   JScrollPane scrollPane = new JScrollPane(taArea0);
   gbPanel0.setConstraints( scrollPane, gbcPanel0 );
   pnPanel0.add( scrollPane );

		//Close button,
   btBut0 = new JButton( lang.getText("close") );
   gbcPanel0.gridx = 15;
   gbcPanel0.gridy = 18;
   gbcPanel0.gridwidth = 5;
   gbcPanel0.gridheight = 2;
   gbcPanel0.fill = GridBagConstraints.HORIZONTAL;
   gbcPanel0.weightx = 1;
   gbcPanel0.weighty = 0;
   gbcPanel0.anchor = GridBagConstraints.NORTH;
   gbPanel0.setConstraints( btBut0, gbcPanel0 );
   pnPanel0.add( btBut0 );
   
	 	//Next button
   btBut1 = new JButton( lang.getText("next") );
   gbcPanel0.gridx = 10;
   gbcPanel0.gridy = 18;
   gbcPanel0.gridwidth = 5;
   gbcPanel0.gridheight = 2;
   gbcPanel0.fill = GridBagConstraints.HORIZONTAL;
   gbcPanel0.weightx = 1;
   gbcPanel0.weighty = 0;
   gbcPanel0.anchor = GridBagConstraints.NORTH;
   gbPanel0.setConstraints( btBut1, gbcPanel0 );
   pnPanel0.add( btBut1 );

		//Back button
   btBut2 = new JButton( lang.getText("back") );
   gbcPanel0.gridx = 5;
   gbcPanel0.gridy = 18;
   gbcPanel0.gridwidth = 5;
   gbcPanel0.gridheight = 2;
   gbcPanel0.fill = GridBagConstraints.HORIZONTAL;
   gbcPanel0.weightx = 1;
   gbcPanel0.weighty = 0;
   gbcPanel0.anchor = GridBagConstraints.NORTH;
   gbPanel0.setConstraints( btBut2, gbcPanel0 );
   pnPanel0.add( btBut2 );

		//Checkbox for open in the futur on startup ??
   cbBox0 = new JCheckBox( lang.getText("showTipsOnStartup") , tips);
   gbcPanel0.gridx = 0;
   gbcPanel0.gridy = 18;
   gbcPanel0.gridwidth = 5;
   gbcPanel0.gridheight = 1;
   gbcPanel0.fill = GridBagConstraints.HORIZONTAL;
   gbcPanel0.weightx = 1;
   gbcPanel0.weighty = 0;
   gbcPanel0.anchor = GridBagConstraints.NORTH;
   gbPanel0.setConstraints( cbBox0, gbcPanel0 );
   pnPanel0.add( cbBox0 );

   setDefaultCloseOperation( DISPOSE_ON_CLOSE );
   
   JPanel mainPanel = new JPanel(new FlowLayout());
   mainPanel.add(pnPanel0);
   
   setContentPane( mainPanel );
   
   //Listener for close event
   btBut0.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dispose();
				appli.getOptions().setOpenTips(cbBox0.isSelected());
			}
		});
   
   btBut1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				index = (index+1)%tipsText.length;
				changeText(tipsText[index]);
			}
		});
   
   btBut2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				index--;
				if(index==-1) index=tipsText.length-1;
				changeText(tipsText[index]);
			}
		});

   this.pack();
   setResizable(false);
   DialogAligner.center(this, getParent());
   applyComponentOrientation(lang.getComponentOrientation());
   
} 

	/** Change the text on the Editor Pane*/
	public void changeText(String text){
			
	 Style def = StyleContext.getDefaultStyleContext().getStyle(StyleContext.DEFAULT_STYLE);
	 
	 //Default style for text
	 Style regular = taArea0.addStyle("regular", def);
	 StyleConstants.setFontSize(regular, 14);
   StyleConstants.setFontFamily(def, "SansSerif");
	 
	 //Bold style for "Did you know ???"
	 Style s = taArea0.addStyle("bold", regular);
	 StyleConstants.setFontSize(s, 18);
   StyleConstants.setItalic(s, true);
	 
	 
	 String[] initString =
                { GanttLanguage.getInstance().getText("didYouKnow")+"\n\n",		//bold "Did you know??"
                  text					//regular  "the tips text"
                 };

   String[] initStyles =  { "bold", "regular"};
	 Document doc = new DefaultStyledDocument() ;
	 taArea0.cut();
	 //Insert text on the panel
	 try {
            for (int i=0; i < initString.length; i++) {
                doc.insertString(doc.getLength(), initString[i],
                                 taArea0.getStyle(initStyles[i]));
            }
						taArea0.setDocument(doc);
        } catch (BadLocationException ble) {
            System.err.println("Couldn't insert initial text.");
        }
		
	}

}
