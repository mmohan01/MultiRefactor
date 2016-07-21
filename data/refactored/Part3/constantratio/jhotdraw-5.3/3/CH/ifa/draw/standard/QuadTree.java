/*
 * @(#)QuadTree.java
 *
 * Project:		JHotdraw - a GUI framework for technical drawings
 *				http://www.jhotdraw.org
 *				http://jhotdraw.sourceforge.net
 * Copyright:	� by the original author(s) and all contributors
 * License:		Lesser GNU Public License (LGPL)
 *				http://www.opensource.org/licenses/lgpl-license.html
 */


package CH.ifa.draw.standard;

import java.awt.geom.Rectangle2D;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Vector;

/**
 * @author: WMG (INIT Copyright (C) 2000 All rights reserved)
 * @version <$CURRENT_VERSION$>
 */
class QuadTree {

    //_________________________________________________________VARIABLES

    private Rectangle2D  _absoluteBoundingRectangle2D = new Rectangle2D.Double();
    private int          _nMaxTreeDepth;
    private Hashtable    _theHashtable = new Hashtable();
    private Hashtable    _outsideHashtable = new Hashtable();
    private QuadTree     _nwQuadTree;
    private QuadTree     _neQuadTree;
    private QuadTree     _swQuadTree;
    private QuadTree     _seQuadTree;

    //______________________________________________________CONSTRUCTORS

    public QuadTree(Rectangle2D absoluteBoundingRectangle2D) {
        this(6, absoluteBoundingRectangle2D);
    }

    public QuadTree(int nMaxTreeDepth, Rectangle2D
        absoluteBoundingRectangle2D) {
        _init(nMaxTreeDepth, absoluteBoundingRectangle2D);
    }

    private QuadTree() {
    }

    //____________________________________________________PUBLIC METHODS

    public void add(Object anObject, Rectangle2D absoluteBoundingRectangle2D) {
        if (_nMaxTreeDepth == 1) {
            if (absoluteBoundingRectangle2D.intersects(_absoluteBoundingRectangle2D)) {
                _theHashtable.put(anObject, absoluteBoundingRectangle2D);
            }
             else {
                _outsideHashtable.put(anObject, absoluteBoundingRectangle2D);
            }
            return;
        }

        boolean bNW = absoluteBoundingRectangle2D.intersects(
            _nwQuadTree.getAbsoluteBoundingRectangle2D());

        boolean bNE = absoluteBoundingRectangle2D.intersects(
            _neQuadTree.getAbsoluteBoundingRectangle2D());

        boolean bSW = absoluteBoundingRectangle2D.intersects(
            _swQuadTree.getAbsoluteBoundingRectangle2D());

        boolean bSE = absoluteBoundingRectangle2D.intersects(
            _seQuadTree.getAbsoluteBoundingRectangle2D());

        int nCount = 0;

        if (bNW == true) {
            nCount++;
        }
        if (bNE == true) {
            nCount++;
        }
        if (bSW == true) {
            nCount++;
        }
        if (bSE == true) {
            nCount++;
        }

        if (nCount > 1) {
            _theHashtable.put(anObject, absoluteBoundingRectangle2D);
            return;
        }
        if (nCount == 0) {
            _outsideHashtable.put(anObject, absoluteBoundingRectangle2D);
            return;
        }

        if (bNW == true) {
            _nwQuadTree.add(anObject, absoluteBoundingRectangle2D);
        }
        if (bNE == true) {
            _neQuadTree.add(anObject, absoluteBoundingRectangle2D);
        }
        if (bSW == true) {
            _swQuadTree.add(anObject, absoluteBoundingRectangle2D);
        }
        if (bSE == true) {
            _seQuadTree.add(anObject, absoluteBoundingRectangle2D);
        }
    }

    public Object remove(Object anObject) {
        Object returnObject = _theHashtable.remove(anObject);
        if (returnObject != null) {
            return returnObject;
        }

        if (_nMaxTreeDepth > 1) {
            returnObject = _nwQuadTree.remove(anObject);
            if (returnObject != null) {
                return returnObject;
            }

            returnObject = _neQuadTree.remove(anObject);
            if (returnObject != null) {
                return returnObject;
            }

            returnObject = _swQuadTree.remove(anObject);
            if (returnObject != null) {
                return returnObject;
            }

            returnObject = _seQuadTree.remove(anObject);
            if (returnObject != null) {
                return returnObject;
            }
        }

        returnObject = _outsideHashtable.remove(anObject);
        if (returnObject != null) {
            return returnObject;
        }

        return null;
    }


    public void clear() {
        _theHashtable.clear();
        _outsideHashtable.clear();
        if (_nMaxTreeDepth > 1) {
            _nwQuadTree.clear();
            _neQuadTree.clear();
            _swQuadTree.clear();
            _seQuadTree.clear();
        }
    }

    public int getMaxTreeDepth() {
        return _nMaxTreeDepth;
    }

    public Vector getAll() {
        Vector v = new Vector();
        v.addAll(_theHashtable.keySet());
        v.addAll(_outsideHashtable.keySet());

        if (_nMaxTreeDepth > 1) {
            v.addAll(_nwQuadTree.getAll());
            v.addAll(_neQuadTree.getAll());
            v.addAll(_swQuadTree.getAll());
            v.addAll(_seQuadTree.getAll());
        }

        return v;
    }

    public Vector getAllWithin(Rectangle2D r) {
        Vector v = new Vector();
        for (Iterator ii = _outsideHashtable.keySet().iterator(); ii.hasNext();) {
            Object anObject = ii.next();
            Rectangle2D itsAbsoluteBoundingRectangle2D = (Rectangle2D)
            _outsideHashtable.get(anObject);

            if (itsAbsoluteBoundingRectangle2D.intersects(r)) {
                v.addElement(anObject);
            }
        }

        if (_absoluteBoundingRectangle2D.intersects(r)) {
            for (Iterator i = _theHashtable.keySet().iterator(); i.hasNext();) {
                Object anObject = i.next();
                Rectangle2D itsAbsoluteBoundingRectangle2D = (Rectangle2D)
                _theHashtable.get(anObject);

                if (itsAbsoluteBoundingRectangle2D.intersects(r)) {
                    v.addElement(anObject);
                }
            }

            if (_nMaxTreeDepth > 1) {
                v.addAll(_nwQuadTree.getAllWithin(r));
                v.addAll(_neQuadTree.getAllWithin(r));
                v.addAll(_swQuadTree.getAllWithin(r));
                v.addAll(_seQuadTree.getAllWithin(r));
            }
        }

        return v;
    }

    public Rectangle2D getAbsoluteBoundingRectangle2D() {
        return _absoluteBoundingRectangle2D;
    }

    //___________________________________________________PRIVATE METHODS

    private void _init(int nMaxTreeDepth, Rectangle2D absoluteBoundingRectangle2D) {
        _absoluteBoundingRectangle2D.setRect(absoluteBoundingRectangle2D);
        _nMaxTreeDepth = nMaxTreeDepth;

        if (_nMaxTreeDepth > 1) {
            _nwQuadTree = new QuadTree(_nMaxTreeDepth - 1,
             _makeNorthwest(absoluteBoundingRectangle2D));
            _neQuadTree = new QuadTree(_nMaxTreeDepth - 1,
             _makeNortheast(absoluteBoundingRectangle2D));
            _swQuadTree = new QuadTree(_nMaxTreeDepth - 1,
             _makeSouthwest(absoluteBoundingRectangle2D));
            _seQuadTree = new QuadTree(_nMaxTreeDepth - 1,
             _makeSoutheast(absoluteBoundingRectangle2D));
        }
    }

    private Rectangle2D _makeNorthwest(Rectangle2D r) {
        return new Rectangle2D.Double(r.getX(), r.getY(), r.getWidth() / 2.0, r.getHeight() / 2.0);
    }

    private static Rectangle2D _makeNortheast(Rectangle2D r) {
        return new Rectangle2D.Double(r.getX() + r.getWidth() / 2.0,
            r.getY(), r.getWidth() / 2.0, r.getHeight() / 2.0);
    }

    private Rectangle2D _makeSouthwest(Rectangle2D r) {
        return new Rectangle2D.Double(r.getX(), r.getY() + r.getHeight() / 2.0,
            r.getWidth() / 2.0, r.getHeight() / 2.0);
    }

    private Rectangle2D _makeSoutheast(Rectangle2D r) {
        return new Rectangle2D.Double(r.getX() + r.getWidth() / 2.0,
            r.getY() + r.getHeight() / 2.0, r.getWidth() / 2.0,
            r.getHeight() / 2.0);
    }

//_______________________________________________________________END
} //end of class QuadTree
