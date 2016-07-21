/*
 * $Id: JSONParser.java,v 1.1 2006/04/15 14:10:48 platform Exp $
 * Created on 2006-4-15
 */
package org.json.simple.parser;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;


/**
 * Parser for JSON text. Please note that JSONParser is NOT thread-safe.
 * 
 * @author FangYidong<fangyidong@yahoo.com.cn>
 */
public class JSONParser {
    public static final int S_INIT = 0;
    public static final int S_IN_FINISHED_VALUE = 1;//string,number,boolean,null,object,array
    public
     static final int S_IN_OBJECT = 2;
    public static final int S_IN_ARRAY = 3;
    public static final int S_PASSED_PAIR_KEY = 4;
    public static final int S_END = 6;
    public static final int S_IN_ERROR = -1;

    public LinkedList handlerStatusStack;
    private Yylex lexer = new Yylex((Reader)null);
    private Yytoken token = null;
    private int status = S_INIT;

    public int peekStatus(LinkedList statusStack) {
        if (statusStack.size() == 0)
            return -1;
        Integer status = (Integer)statusStack.getFirst();
        return status.intValue();
    }

    /**
     *  Reset the parser to the initial state without resetting the underlying reader.
     *
     */
    public void reset() {
        token = null;
        status = S_INIT;
        handlerStatusStack = null;
    }

    /**
     * Reset the parser to the initial state with a new character reader.
     * 
     * @param in - The new character reader.
     * @throws IOException
     * @throws ParseException
     */
    public void reset(Reader in) {
        lexer.yyreset(in);
        reset();
    }

    /**
	 * @return The position of the beginning of the current token.
	 */
    public int getPosition() {
        return lexer.getPosition();
    }

    public void nextToken() throws ParseException, IOException {
        token = lexer.yylex();
        if (token == null)
            token = new Yytoken(Yytoken.TYPE_EOF, null);
    }
}
