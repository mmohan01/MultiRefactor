/*
 * Created on 17.06.2004
 *
 */
package net.sourceforge.ganttproject.chart;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.util.ArrayList;

/**
 * @author bard
 *
 */
class GraphicPrimitiveContainer {
    private ArrayList myRectangles = new ArrayList();
    private ArrayList myLines = new ArrayList();
    private ArrayList myTexts = new ArrayList();

    static class GraphicPrimitive {
        private Color myBackgroundColor;
        private Color myForegroundColor;
        
        
        public Color getBackgroundColor() {
            return myBackgroundColor;
        }
        public void setBackgroundColor(Color myBackgroundColor) {
            this.myBackgroundColor = myBackgroundColor;
        }
        public Color getForegroundColor() {
            return myForegroundColor;
        }
        public void setForegroundColor(Color myForegroundColor) {
            this.myForegroundColor = myForegroundColor;
        }
    }
    
    static class Rectangle extends GraphicPrimitive {
        private final int myLeftX;
        private final int myTopY;
        private final int myWidth;
        private final int myHeight;
        
        private Rectangle(int leftx, int topy, int width, int height) {
            myLeftX = leftx;
            myTopY = topy;
            myWidth = width;
            myHeight = height;
        }
    }
    
    static class Line extends GraphicPrimitive {
        private final int myStartX;
        private final int myStartY;
        private final int myFinishX;
        private final int myFinishY;
        
        private Line(int startx, int starty, int finishx, int finishy) {
        	myStartX = startx;
        	myStartY = starty;
        	myFinishX = finishx;
        	myFinishY = finishy;
        }
    }
    
    static class Text extends GraphicPrimitive {
    	private final int myLeftX;
    	private final int myBottomY;
    	private final String myText;
    	private Font myFont;
    	
    	Text(int leftX, int bottomY, String text) {
    		myLeftX = leftX;
    		myBottomY = bottomY;
    		myText = text;
    	}
    	
    	public void setFont(Font font) {
    		myFont = font;
    	}
    }
    Rectangle createRectangle(int leftx, int topy, int width, int height) {
        Rectangle result = new Rectangle(leftx,topy, width, height);
        myRectangles.add(result);
        return result;
    }
    
    Line createLine(int startx, int starty, int finishx, int finishy) {
    	Line result = new Line(startx, starty, finishx, finishy);
    	myLines.add(result);
    	return result;
    }
    
    Text createText(int leftx, int bottomy, String text) {
    	Text result = new Text(leftx, bottomy, text);
    	myTexts.add(result);
    	return result;
    }
    
    void paint(Graphics g) {
    	for (int i=0; i<myRectangles.size(); i++) {
    		Rectangle next = (Rectangle) myRectangles.get(i);
    		if (next.getBackgroundColor()==null) {
        		Color foreColor = next.getForegroundColor();
        		if (foreColor==null) {
        			foreColor = Color.BLACK;
        		}
        		g.setColor(foreColor);
    			g.drawRect(next.myLeftX, next.myTopY, next.myWidth, next.myHeight);
    		}
    		else {
    			g.setColor(next.getBackgroundColor());
    			g.fillRect(next.myLeftX, next.myTopY, next.myWidth, next.myHeight);
    		}
    	}
    	for (int i=0; i<myLines.size(); i++) {
    		Line next = (Line) myLines.get(i);
    		Color foreColor = next.getForegroundColor();
    		if (foreColor==null) {
    			foreColor = Color.BLACK;
    		}
    		g.setColor(foreColor);
    		g.drawLine(next.myStartX, next.myStartY, next.myFinishX, next.myFinishY);
    	}
    	for (int i=0; i<myTexts.size(); i++) {
    		Text next = (Text) myTexts.get(i);
    		Color foreColor = next.getForegroundColor();
    		if (foreColor==null) {
    			foreColor = Color.BLACK;
    		}
    		g.setColor(foreColor);
    		if (next.myFont!=null) {
    			g.setFont(next.myFont);
    		}
    		g.drawString(next.myText, next.myLeftX, next.myBottomY);
    	}
    }
}
