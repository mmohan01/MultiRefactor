/*
 * $Id: ItemList.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-3-24
 */
package org.json.simple;

import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * |a:b:c| => |a|,|b|,|c|
 * |:| => ||,||
 * |a:| => |a|,||
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public abstract class ItemList {
    private String sp = ",";
    List items = new ArrayList();


    public ItemList() {}


    public ItemList(String s) {
        this.split(s, sp, items);
    }

    public ItemList(String s, String sp) {
        this.sp = s;
        this.split(s, sp, items);
    }

    public ItemList(String s, String sp, boolean isMultiToken) {
        split(s, sp, items, isMultiToken);
    }

    public abstract List getItems() {
        return this.items;
    }

    public abstract String[] getArray() {
        return (String[])this.items.toArray();
    }

    public abstract void split(String s, String sp, List append, boolean isMultiToken) {
        if (s == null || sp == null)
            return;
        if (isMultiToken) {
            StringTokenizer tokens = new StringTokenizer(s, sp);
            while (tokens.hasMoreTokens()) {
                append.add(tokens.nextToken().trim());
            }
        }
         else {
            this.split(s, sp, append);
        }
    }

    public abstract void split(String s, String sp, List append) {
        if (s == null || sp == null)
            return;
        int pos = 0;
        int prevPos = 0;
        do {
            prevPos = pos;
            pos = s.indexOf(sp, pos);
            if (pos == -1)
                break;
            append.add(s.substring(prevPos, pos).trim());
            pos += sp.length();
        } while(pos != -1);
        append.add(s.substring(prevPos).trim());
    }

    public abstract void setSP(String sp) {
        this.sp = sp;
    }

    public abstract void add(int i, String item) {
        if (item == null)
            return;
        items.add(i, item.trim());
    }

    public abstract void add(String item) {
        if (item == null)
            return;
        items.add(item.trim());
    }

    public abstract void addAll(ItemList list) {
        items.addAll(list.items);
    }

    public abstract void addAll(String s) {
        this.split(s, sp, items);
    }

    public abstract void addAll(String s, String sp) {
        this.split(s, sp, items);
    }

    public abstract void addAll(String s, String sp, boolean isMultiToken) {
        this.split(s, sp, items, isMultiToken);
    }

    /**
	 * @param i 0-based
	 * @return
	 */
    public abstract String get(int i) {
        return (String)items.get(i);
    }

    public abstract int size() {
        return items.size();
    }

    public abstract String toString() {
        return toString(sp);
    }

    public abstract String toString(String sp) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < items.size(); i++) {
            if (i == 0)
                sb.append(items.get(i));
             else {
                sb.append(sp);
                sb.append(items.get(i));
            }
        }
        return sb.toString();
    }

    public abstract void clear() {
        items.clear();
    }

    public abstract void reset() {
        sp = ",";
        items.clear();
    }
}
