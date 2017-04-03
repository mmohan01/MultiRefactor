/*
 * Created on 17.06.2004
 *
 */
package net.sourceforge.ganttproject.chart;

import java.awt.Graphics;

import net.sourceforge.ganttproject.time.TimeFrame;

/**
 * @author bard
 *
 */
public class ChartHeaderImpl implements ChartHeader {

    private final ChartModelImpl myChartModel;
    private final GraphicPrimitiveContainer myPrimitiveContainer;

    public ChartHeaderImpl(ChartModelImpl model) {
        myChartModel = model;
        myPrimitiveContainer = new GraphicPrimitiveContainer();
    }

    public void paint(Graphics g) {
    	preparePrimitives();
    	myPrimitiveContainer.paint(g);

    }
    
    private void preparePrimitives() {
        createGreyRectangleWithNiceBorders();
        createFrames();
    }

	private void createGreyRectangleWithNiceBorders() {
        int sizex = getWidth();
        int sizey = getHeight();        
        int spanningHeaderHeight = myChartModel.getChartUIConfiguration().getSpanningHeaderHeight();
        
        GraphicPrimitiveContainer.Rectangle headerRectangle = myPrimitiveContainer.createRectangle(0, 0, sizex, spanningHeaderHeight*2);
        headerRectangle.setBackgroundColor(myChartModel.getChartUIConfiguration().getSpanningHeaderBackgroundColor());
        //
        GraphicPrimitiveContainer.Rectangle spanningHeaderBorder = myPrimitiveContainer.createRectangle(0, 0, sizex-1, spanningHeaderHeight);
        spanningHeaderBorder.setForegroundColor(myChartModel.getChartUIConfiguration().getHeaderBorderColor());
        //
        GraphicPrimitiveContainer.Rectangle timeunitHeaderBorder = myPrimitiveContainer.createRectangle(0, spanningHeaderHeight, sizex-1, spanningHeaderHeight);
        timeunitHeaderBorder.setForegroundColor(myChartModel.getChartUIConfiguration().getHeaderBorderColor());
        //
        GraphicPrimitiveContainer.Line middleGutter1 = myPrimitiveContainer.createLine(1, spanningHeaderHeight-1, sizex-2, spanningHeaderHeight-1);
        middleGutter1.setForegroundColor(myChartModel.getChartUIConfiguration().getHorizontalGutterColor1());
        //
        GraphicPrimitiveContainer.Line bottomGutter = myPrimitiveContainer.createLine(0, spanningHeaderHeight*2-2, sizex-2, spanningHeaderHeight*2-2);
        bottomGutter.setForegroundColor(myChartModel.getChartUIConfiguration().getHorizontalGutterColor1());
        //
        GraphicPrimitiveContainer.Line topGutter = myPrimitiveContainer.createLine(1, 1, sizex - 2, 1);
        topGutter.setForegroundColor(myChartModel.getChartUIConfiguration().getHorizontalGutterColor2());
        //
        GraphicPrimitiveContainer.Line middleGutter2 = myPrimitiveContainer.createLine(0, spanningHeaderHeight+1, sizex-2, spanningHeaderHeight+1);
        topGutter.setForegroundColor(myChartModel.getChartUIConfiguration().getHorizontalGutterColor2());		
	}
	
    private void createFrames() {        
        int posX = 0;
        
        final int totalWidth = getWidth();
        final int topUnitHeight = myChartModel.getChartUIConfiguration().getSpanningHeaderHeight();
        final int bottomY = topUnitHeight*2 - 1;
        final int bottomUnitWidth = myChartModel.getBottomUnitWidth();
        final ChartUIConfiguration config = myChartModel.getChartUIConfiguration();

        class TopUnitTextBuilder {
            void createTopUnitText(int posFrameStart, TimeFrame timeFrame) {
                String unitText = timeFrame.getUnitText(timeFrame.getTopUnit(), 0);
                int posX = posFrameStart+2;
                int posY = topUnitHeight-5;
                GraphicPrimitiveContainer.Text text = myPrimitiveContainer.createText(posX+2, posY, unitText);
                text.setFont(config.getSpanningHeaderFont());
            }
        }
        
        class BottomUnitGridBuilder {
            int myWidth=0;
            
            void createBottomUnitGrid(int posFrameStart, TimeFrame timeFrame) {
                myWidth = 0;
                int posX = posFrameStart;
                for (int j=0; j<timeFrame.getUnitCount(timeFrame.getBottomUnit()); j++) {
                    if (posX>totalWidth) {
                        break;
                    }
                    GraphicPrimitiveContainer.Line nextLine = myPrimitiveContainer.createLine(posX, topUnitHeight, posX, bottomY);
                    nextLine.setForegroundColor(config.getBottomUnitGridColor());
                    //
                    String unitText = timeFrame.getUnitText(timeFrame.getBottomUnit(), j);
                    GraphicPrimitiveContainer.Text nextText = myPrimitiveContainer.createText(posX+2, bottomY-7, unitText);
                    nextText.setFont(config.getBottomUnitFont());
                    posX+=bottomUnitWidth;
                }   
                myWidth = posX - posFrameStart;
            }
            
            int getWidth() {
                return myWidth;
            }
        }
        
        TopUnitTextBuilder topUnitTextBuilder = new TopUnitTextBuilder();
        BottomUnitGridBuilder bottomUnitGridBuilder = new BottomUnitGridBuilder();
        
        TimeFrame[] timeFrames = myChartModel.getTimeFrames();
        for (int i=0; posX<=totalWidth && i<timeFrames.length; i++) {
            TimeFrame nextFrame = timeFrames[i];
            topUnitTextBuilder.createTopUnitText(posX, nextFrame);
            bottomUnitGridBuilder.createBottomUnitGrid(posX, nextFrame);
            posX+=bottomUnitGridBuilder.getWidth();
        }        
    }
    
	private int getHeight() {
        return (int) myChartModel.getBounds().getHeight();
    }

    private int getWidth() {
        return (int) myChartModel.getBounds().getWidth();
    }

}
