/***************************************************************************
               GanttStatusBar.java 
------------------------------------------
begin                : 5 juil. 2004
copyright            : (C) 2004 by Thomas Alexandre
email                : alexthomas@ganttproject.org
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;

import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.language.GanttLanguage;

/**
 * @author athomas
 *
 * Simulate a status bar under the main frame
 */
public class GanttStatusBar extends JPanel implements Runnable {

	protected ProgressBarPanel pbp;
	protected MessagePanel message0;
	
	/** Message 1. */
	protected MessagePanel message1;
	
	/** Message 2. */
	protected MessagePanel message2;

	private static final int NO_MESSAGE = 1;
	private static final int MESSAGE_1 = 0;
	private static final int MESSAGE_2 = 1;
	private static final int PROGRESS_FINISH = 2;
	int mode = NO_MESSAGE;
	
	boolean bRunning = false;
	
	/** Default constructor. */
	public GanttStatusBar()
	{
		super(new BorderLayout());
		
		pbp = new ProgressBarPanel();
		message0 = new MessagePanel(160, false);
		message1 = new MessagePanel(500, true);
		message2 = new MessagePanel(250, true);
		
		
		pbp.setPreferredSize(new Dimension(160,16));
		//message1.setPreferredSize(new Dimension(360,25));
		//message2.setPreferredSize(new Dimension(210,25));
		
		//add(pbp, BorderLayout.WEST);
		add(message0, BorderLayout.WEST);
		add(message1, BorderLayout.CENTER);
		add(message2, BorderLayout.EAST);
		
		message0.setText("GanttProject.Org ("+GanttProject.version+")");
		pbp.setValue(0);
		setFirstText(GanttLanguage.getInstance().getText("welcome"), 500);		
	}
	
	public void setProgressValue(int value)
	{
		if(!isVisible())return;
		pbp.setValue(value);
		if(value==100) {
			mode = PROGRESS_FINISH;
			if(!bRunning) {
				bRunning = true;
				new Thread(this).start();
			}
		}
	}
	public void setFirstText(String text)
	{
		message1.setText(text);
	}
	public void setSecondText(String text)
	{
		message2.setText(text);
	}
	public void setFirstText(String text, int mlTimer)
	{
		if(!isVisible())return;
		message1.setText(text, mlTimer);
		mode = MESSAGE_1;
		if(!bRunning) {
			bRunning = true;
			new Thread(this).start();
		}
	}
	public void setSecondText(String text, int mlTimer)
	{
		if(!isVisible())return;
		message2.setText(text, mlTimer);
		mode = MESSAGE_2;
		if(!bRunning) {
			bRunning = true;
			new Thread(this).start();
		}
	}
	
	/** @return the preferred size of this component. */
	public Dimension getPreferredSize()
	{
		return new Dimension(getWidth(), 24);
	}
	
	public void run()
	{
		try 
		{
			//while(true){
				switch(mode)
				{
					case MESSAGE_1 :
						Thread.sleep(message1.getTimer());
						message1.hideText();
						message1.setText("");
						break;
					case MESSAGE_2 :
						Thread.sleep(message2.getTimer());
						message2.hideText();
						message2.setText("");
						break;
					case PROGRESS_FINISH :
						Thread.sleep(500);
						pbp.setValue(0);
						break;
				}
				mode = NO_MESSAGE;
			//}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}	
		bRunning=false;
	}
	
	//! Class to display a message
	private class MessagePanel extends JPanel
	{
		JLabel message;
		Color textColor = Color.BLACK;
		
		int timer = 0;
		
		/** Constructor. */
		public MessagePanel(int size, boolean separator)
		{
			super (new FlowLayout());
			message = new JLabel() {
	        	public void paint (Graphics g) {
	        		Graphics2D g2 = (Graphics2D) g;
	        		g2.setColor(textColor);
	        		g2.drawString(getText(), 0, 12);
	        	}
	        };
			message.setPreferredSize(new Dimension(size,16));
			if(separator) add(new JLabel("|"));
			add(message);
		}

		
		/** set a new text to the message. */
		public void setText(String text)
		{
			message.setText(text);
			timer = 0;
		}
		
		/** set a new text to the message. */
		public void setText(String text, int mltimer)
		{
			message.setText(text);
			timer = mltimer;
		}
		
		/** clear the panel. */
		public void clear()
		{
			message.setText("");
		}
		
		/** Hide the text by decrease the color. */
		public void hideText()
		{
			try {
				Color cPanel= getBackground();
				Color cBlack = Color.BLACK;
			
				int step = 50;
			
				float dRed = (float)cPanel.getRed() / (float)step;
				float dGreen = (float)cPanel.getGreen() / (float)step;
				float dBlue = (float)cPanel.getBlue() / (float)step;
			
			
				for(int i=0; i<step; i++)
				{
					textColor = new Color((int)(dRed*(float)i), 
							(int)(dGreen*(float)i),
							(int)(dBlue*(float)i));
					repaint();
					Thread.sleep(20);
				}
				textColor = Color.BLACK;
			} catch (Exception e)
			{
				//exception for the thread.sleep
			}
		}
		
		/** @return the timer. */
		public int getTimer() {
			return timer;
		}
	}
	
	//! class to manage the progree bar
	private class ProgressBarPanel extends JPanel
	{
		/** A progress bar */
		private JProgressBar progressBar;
		
		/** Indicate the precent complete. */
		private JLabel progressLabel;
		
		/** Constructor. */
		public ProgressBarPanel()
		{
			super (new FlowLayout());
			
			progressBar = new JProgressBar(0,100);
			progressBar.setPreferredSize(new Dimension(100,16));
			
			progressLabel = new JLabel();
			progressLabel.setPreferredSize(new Dimension(30,16));
			
			add(progressBar);
			add(new JLabel("|"));
			add(progressLabel);
					
		}
		
		/** set a new value for the panel. */
		public void setValue(int value)
		{
			progressBar.setValue(value);
			if(value>0)
				progressLabel.setText(value+"%");
			else progressLabel.setText("");
		}
		
		/** reset the component. */
		public void reset()
		{
			progressBar.setValue(0);
			progressLabel.setText("");
		}
	}
}
