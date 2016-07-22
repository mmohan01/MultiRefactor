/*
 * @(#)GraphLayout.java
 *
 * The original file Graph.java (1.5 99/11/29) is
 * Copyright (c) 1997 Sun Microsystems, Inc. All Rights Reserved.
 * Adapted by F. Wienberg, 1999
 *
 * Sun grants you ("Licensee") a non-exclusive, royalty free, license to use,
 * modify and redistribute this software in source and binary code form,
 * provided that i) this copyright notice and license appear on all copies of
 * the software; and ii) Licensee does not utilize the software in a manner
 * which is disparaging to Sun.
 *
 * This software is provided "AS IS," without a warranty of any kind. ALL
 * EXPRESS OR IMPLIED CONDITIONS, REPRESENTATIONS AND WARRANTIES, INCLUDING ANY
 * IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR
 * NON-INFRINGEMENT, ARE HEREBY EXCLUDED. SUN AND ITS LICENSORS SHALL NOT BE
 * LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE AS A RESULT OF USING, MODIFYING
 * OR DISTRIBUTING THE SOFTWARE OR ITS DERIVATIVES. IN NO EVENT WILL SUN OR ITS
 * LICENSORS BE LIABLE FOR ANY LOST REVENUE, PROFIT OR DATA, OR FOR DIRECT,
 * INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
 * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF
 * OR INABILITY TO USE SOFTWARE, EVEN IF SUN HAS BEEN ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGES.
 *
 * This software is not designed or intended for use in on-line control of
 * aircraft, air traffic, aircraft navigation or aircraft communications; or in
 * the design, construction, operation or maintenance of any nuclear
 * facility. Licensee represents and warrants that it will not use or
 * redistribute the Software for such purposes.
 */

package CH.ifa.draw.util;

import java.util.*;
import CH.ifa.draw.framework.*;
import CH.ifa.draw.standard.*;
import java.awt.*;

/**
 * @version <$CURRENT_VERSION$>
 */
public class GraphLayout extends FigureChangeAdapter {
    public double LENGTH_FACTOR = 1.0;
    public double REPULSION_STRENGTH = 0.5;
    public double REPULSION_LIMIT = 200.0;
    int REPULSION_TYPE = 0; // 0: (1-r)/r   1: 1-r   2: (1-r)^2
    public double SPRING_STRENGTH = 0.1;
    public double TORQUE_STRENGTH = 0.25;
    public double FRICTION_FACTOR = 0.75;

    private Hashtable nodes = new Hashtable(),
                  edges = new Hashtable();

    public GraphLayout() {}

    private GraphNode getGraphNode(Figure node) {
          return (GraphNode)nodes.get(node);
    }

    private double len(Figure edge) {
          return ((Double)edges.get(edge)).doubleValue() * LENGTH_FACTOR;
    }

    public void addNode(Figure node) {
          nodes.put(node, new GraphNode(node));
          node.addFigureChangeListener(this);
    }

    public void addEdge(ConnectionFigure edge, int addlen) {
          Dimension d1 = edge.getStartConnector().owner().size();
          Dimension d2 = edge.getEndConnector().owner().size();
          int len = Math.max(d1.width, d1.height) / 2 +
                Math.max(d2.width, d2.height) / 2 + addlen;
          edges.put(edge, new Double(len));
    }

    /**
	 * Sent when a figure changed
	 */
    synchronized public void figureChanged(FigureChangeEvent e) {
          if (nodes != null) {
              Figure node = e.getFigure();
              if (nodes.containsKey(node)) {
                  getGraphNode(node).update();
            }
        }
    }

    public void remove() {
          if (nodes != null) {
              Enumeration nodeEnum = nodes.keys();
              while (nodeEnum.hasMoreElements()) {
                    Figure node = (Figure)nodeEnum.nextElement();
                    node.removeFigureChangeListener(this);
            }
              nodes = null;
              edges = null;
        }
    }
}

class GraphNode {
    double x = 0.0, y = 0.0;
    double dx = 0.0;
    double dy = 0.0;
    final Figure node;

    GraphNode(Figure node) {
          this.node = node;
          update();
    }

    void update() {
          Point p = node.center();
          if (Math.abs(p.x - Math.round(x)) > 1 ||
              Math.abs(p.y - Math.round(y)) > 1) {
              x = p.x;
              y = p.y;
        }
              //System.out.println(this+" has new coords: "+x+","+y+"\n");
    }
}
