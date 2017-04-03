/***************************************************************************
                           GanttDialogDate.java  -  description
                             -------------------
    begin                : dec 2002
    copyright            : (C) 2002 by Thomas Alexandre
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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.PrinterJob;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JToolBar;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import net.sourceforge.ganttproject.GanttPrintable;
import net.sourceforge.ganttproject.GanttProject;
import net.sourceforge.ganttproject.language.GanttLanguage;

/** Preview the print aspect*/
public class GanttPreviewPrint extends JDialog implements ActionListener, ChangeListener{


	int [] zoomX = new int[] {40, 60, 100, 160, 200, 256, 320, 512, 640 };
	int [] zoomY = new int[] {30, 40,  75, 120, 150, 192, 240, 384, 480 };
	int index=3;


	private GanttLanguage language;
	PreviewPanel panel;
	JButton bPrint, bPortrait, bLandscape, bClose, bZoomP, bZoomM;
	JSpinner factorSpinner;
	SpinnerNumberModel spinnerModel;
	
	/** Constructor */
	public GanttPreviewPrint(GanttProject parent, BufferedImage image) {
		super(parent, GanttProject.correctLabel(GanttLanguage.getInstance().getText("preview")),true);
		language= GanttLanguage.getInstance();
		
		JToolBar top = new JToolBar ();
		top.setFloatable(false);
		bPrint = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/print_16.gif")));
		bPortrait = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/portrait_16.gif")));
		bLandscape = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/landscape_16.gif")));
		bClose = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/exit_16.gif")));
		bZoomM = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/zoomm_16.gif")));
		bZoomP = new TestGanttRolloverButton(new ImageIcon(getClass().getResource("/icons/zoomp_16.gif")));
		
		spinnerModel = new SpinnerNumberModel(1.0, 0.1, 2.0, 0.1);
		factorSpinner = new JSpinner(spinnerModel);
		JPanel tmppanel = new JPanel();
		tmppanel.add(new JLabel("Image Factor"),"RIGHT");
		tmppanel.add(factorSpinner,"CENTER");
		
		top.add(bClose);
		top.add(bPrint);
		top.add(bPortrait);
		top.add(bLandscape);
		top.add(bZoomP);
		top.add(bZoomM);
		top.addSeparator();
		top.add(tmppanel);
		top.addSeparator();
		
		bPrint.addActionListener(this);
		bPortrait.addActionListener(this);
		bLandscape.addActionListener(this);
		bClose.addActionListener(this);
		bZoomM.addActionListener(this);
		bZoomP.addActionListener(this);
		
		factorSpinner.addChangeListener(this);
		
		
		bPrint.setToolTipText(GanttProject.getToolTip(GanttProject.correctLabel(language.getText("printProject"))));
		bPortrait.setToolTipText(GanttProject.getToolTip(GanttProject.correctLabel(language.getText("portrait"))));
		bLandscape.setToolTipText(GanttProject.getToolTip(GanttProject.correctLabel(language.getText("landscape"))));
		bClose.setToolTipText(GanttProject.getToolTip(GanttProject.correctLabel(language.getText("close"))));
		bZoomM.setToolTipText(GanttProject.getToolTip(GanttProject.correctLabel(language.getText("zoomOut"))));
		bZoomP.setToolTipText(GanttProject.getToolTip(GanttProject.correctLabel(language.getText("zoomIn"))));
		
		bLandscape.setEnabled(false);
		
		getContentPane().add(top, BorderLayout.NORTH);
		panel = new PreviewPanel(image);
		getContentPane().add(new JScrollPane(panel), BorderLayout.CENTER);
		
		setSize(640,480);
		DialogAligner.center(this, getParent());
		applyComponentOrientation(language.getComponentOrientation());
		
	}
	
	/** Button event drag */
	public void actionPerformed(ActionEvent e){
		if(e.getSource() == bClose) {
			setVisible(false);
			dispose();
		} else if(e.getSource() == bPrint) {
			
			PrinterJob printJob = PrinterJob.getPrinterJob();

			PageFormat pageFormat = new PageFormat();
			pageFormat.setOrientation(panel.orientation);
			printJob.defaultPage(pageFormat);
			printJob.setPrintable(new GanttPrintable(panel.image));
			
			
			
			if(printJob.printDialog()){
	       try { 
						printJob.print(); 
				 
				 } catch (Exception PrintException) {
				 	System.out.println("Print Error" + PrintException);
					PrintException.printStackTrace(); 
				 }
	    }
			
			setVisible(false);
			
		} else if(e.getSource() == bPortrait) {
			panel.changeState(PageFormat.PORTRAIT, zoomX[index], zoomY[index]);
			bPortrait.setEnabled(false);
			bLandscape.setEnabled(true);
			
		} else if(e.getSource() == bLandscape) {
			panel.changeState(PageFormat.LANDSCAPE, zoomX[index], zoomY[index]);
			bPortrait.setEnabled(true);
			bLandscape.setEnabled(false);
			
		} else if(e.getSource() == bZoomM) {
			if(index>0) {
				index--;
				panel.changeState(panel.orientation, zoomX[index], zoomY[index]);
				if(index==0) bZoomM.setEnabled(false);
			}
			bZoomP.setEnabled(true);
			
		} else if(e.getSource() == bZoomP) {
			if(index<=zoomX.length-2) {
				index++;
				panel.changeState(panel.orientation, zoomX[index], zoomY[index]);
				if(index==zoomX.length-1) bZoomP.setEnabled(false);
			}
			bZoomM.setEnabled(true);
		}
	}
	
	/** Ineherit from changelistener. */
 	public void stateChanged(ChangeEvent e)
	{
		Double scale = (Double)spinnerModel.getValue();
		System.out.println("Computing...");
		panel.scale(scale.doubleValue());
		panel.changeState(panel.orientation, panel._x, panel._y);
		panel.repaint();
		System.out.println("...Done");
	}
	
	
	/**
	  * Class where the display will be painting
		*/
	class PreviewPanel extends JPanel {
		
		public BufferedImage save, image, rendering;
		int pageWidth;
		int pageHeight;
		int nbx,nby;

		public int _x, _y;
		int sizex;
		int sizey;
		
		public int orientation;
		boolean haveto;

		/** Constructor */
		public PreviewPanel(BufferedImage img) {
			super(new BorderLayout());

			save = img;
			scale(1.0f);

			changeState(PageFormat.LANDSCAPE, 160, 120);
		}
		
		
		/** Scale the image. */
		public void scale (double scale)
		{
			
			Image _img = save.getScaledInstance((int)(scale*save.getWidth()), (int)(scale*save.getHeight()), Image.SCALE_SMOOTH);
			image = new BufferedImage((int)(scale*save.getWidth()), (int)(scale*save.getHeight()), BufferedImage.TYPE_INT_RGB);
			
			Graphics g = image.getGraphics();
			g.drawImage(_img,0,0,null); 
			
		}
		
		
		
		/** Change the orientation of the print */
		public void changeState(int orientation, int x, int y) {
			
			_x = x; 
			_y = y;
			
			
			PageFormat pageFormat = new PageFormat();
			pageFormat.setOrientation(orientation);
			
			pageWidth = (int)pageFormat.getImageableWidth();
			pageHeight = (int)pageFormat.getImageableHeight();
			
			sizex = pageWidth * x / 648; //648 && 468 size (in pixels) of a default A4 page
			sizey = pageHeight * y / 468;
			
			
			nbx=image.getWidth()/pageWidth+1;
			nby=image.getHeight()/pageHeight+1;
			
			
			setPreferredSize(new Dimension(nbx*sizex,nby*sizey));
			setSize(nbx*100,nby*100);
			
			rendering = null;
			rendering = new BufferedImage(nbx*sizex,nby*sizey,BufferedImage.TYPE_INT_RGB);
			paint(rendering.getGraphics());
			haveto=true;
			this.orientation=orientation;
			
			add(new JLabel(new ImageIcon(rendering)), BorderLayout.CENTER); 
		}
		
		
		/** Repaint the panel */
		public void paint(Graphics g) {
			
			g.setColor(Color.LIGHT_GRAY);
			g.fillRect(0,0,getWidth(), getHeight());
			
			
			for(int j=0;j<nby;j++) {
				for(int i=0;i<nbx;i++) {
					
					//Drawing black border
					g.setColor(Color.BLACK);
					g.drawRect(i*sizex+3, j*sizey+3, sizex-6, sizey-6);
					//Drawing 3D effet
					g.drawLine((i+1)*sizex-2, j*sizey+4, (i+1)*sizex-2, (j+1)*sizey-2);
					g.drawLine(i*sizex+4, (j+1)*sizey-2, (i+1)*sizex-2, (j+1)*sizey-2);
					
					//Fill the page in white
					g.setColor(Color.WHITE);
					g.fillRect(i*sizex+4, j*sizey+4, sizex-8, sizey-8);
					
					int x = i*pageWidth;
					int y = j*pageHeight;
					
					int imageWidth=pageWidth+x<image.getWidth()?pageWidth:image.getWidth()-x;
					int imageHeight=pageHeight+y<image.getHeight()?pageHeight:image.getHeight()-y;
					
					//Draw the part of the image after scale it
					BufferedImage image2= image.getSubimage(x,y,imageWidth,imageHeight);
					g.drawImage(image2.getScaledInstance(imageWidth*(sizex-8)/pageWidth, 
                                               imageHeight*(sizey-8)/pageHeight, 
																							 Image.SCALE_FAST /*SCALE_SMOOTH*/), i*sizex+4,  j*sizey+4, null);
					
				}			
			}
		
		} //End of paintComponent
	
	}

}










