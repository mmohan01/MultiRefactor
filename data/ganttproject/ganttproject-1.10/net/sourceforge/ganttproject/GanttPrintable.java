/***************************************************************************
                           GanttPrintable.java  -  description
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
 
package net.sourceforge.ganttproject;

import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Printable;



/**
  * Class able to print an image
	*/
public class GanttPrintable 
    implements Printable  {

	/** The image to print */
	private BufferedImage image;
	
	/** Position of the image */
	private int x,y;
	

	/** Constructor */
	public GanttPrintable (BufferedImage image) {
		super();
		this.image = image;
		this.x=this.y=0;
	}

	/** Print the page */
  public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) {

		//search for the best possition of the x, y coordinates
		int i,j=0;
		int cpt=0;
		while(j<image.getHeight()) {
			i=0;
			while(i<image.getWidth()) {
				
				if(cpt==pageIndex) {
					x=i;
					y=j;
				}
				i+=(int) pageFormat.getImageableWidth();				
				cpt++;
			}
			j+=(int) pageFormat.getImageableHeight();
		}
	
		if(cpt<=pageIndex) return Printable.NO_SUCH_PAGE;
		
		if(x>image.getWidth())x=0;
		if(y>image.getHeight())return Printable.NO_SUCH_PAGE;
		
		
		Graphics2D g2d = (Graphics2D) graphics;
		g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());	
		
		int imgw = (int)pageFormat.getImageableWidth();
		int imgh = (int)pageFormat.getImageableHeight();
		
		int width=imgw+x<image.getWidth()?imgw:image.getWidth()-x;
		int height=imgh+y<image.getHeight()?imgh:image.getHeight()-y;
		
		g2d.drawImage(image.getSubimage(x,y,width,height),0,0,null);
		
		

		return Printable.PAGE_EXISTS;
		
	}
}		
