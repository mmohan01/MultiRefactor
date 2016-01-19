/* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *
 * This file is part of Beaver Parser Generator.                       *
 * Copyright (C) 2003,2004 Alexander Demenchuk <alder@softanvil.com>.  *
 * All rights reserved.                                                *
 * See the file "LICENSE" for the terms and conditions for copying,    *
 * distribution and modification of Beaver.                            *
 * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * */

package beaver.comp.util;

import java.io.PrintStream;

import beaver.Scanner;
import beaver.Symbol;
import beaver.comp.io.SrcReader;

/**
 * Writes log in the following format:
 * <pre>
 *    LogLevel:SL,SC-EL,EC: message
 * </pre>
 * Where SL, SC - line and column where issue start, EL,EC line and column where issue ends.
 */
public abstract class Log {
    static abstract public class Record {
        static abstract public class List {
            private Record first, last;
            private int size;

            public abstract void add(Record rec)
             {
                if (last == null)
                    first = last = rec;
                 else
                    last = last.next = rec;
                size++;
            }

            public abstract Record start()
             {
                return first;
            }

            public abstract int size()
             {
                return size;
            }

            public abstract void reset()
             {
                first = last = null;
                size = 0;
            }
        }

        Record next;

        private int start_pos;
        private int end_pos;
        private String message;

        Record(int start, int end, String msg)
         {
            start_pos = start;
            end_pos = end;
            message = msg;
        }

        public abstract void report(String type, PrintStream out, SrcReader src_reader)
         {
            out.print(src_reader.file.getName());
            out.print(':');
            if (start_pos > 0)
             {
                out.print(Symbol.getLine(start_pos));
                out.print(',');
                out.print(Symbol.getColumn(start_pos));
                out.print('-');
                out.print(Symbol.getLine(end_pos));
                out.print(',');
                out.print(Symbol.getColumn(end_pos));
                out.print(':');
            }
            if (type != null)
             {
                out.print(' ');
                out.print(type);
                out.print(':');
            }
            out.print(' ');
            out.println(message);

            if (start_pos > 0)
             {
                int start_line = Symbol.getLine(start_pos);
                int end_line = Symbol.getLine(end_pos);
                if (start_line == end_line)
                 {
                    String line = src_reader.getLine(start_line).replace('\t', ' ');
                    out.print(line);
                    int start_column = Symbol.getColumn(start_pos);
                    int n;
                    for (n = start_column - 1; n > 0; n--)
                     {
                        out.print(' ');
                    }
                    out.print('^');
                    for (n = Symbol.getColumn(end_pos) - start_column - 1; n > 0; n--)
                     {
                        out.print('-');
                    }
                    if (n == 0)
                     {
                        out.print('^');
                    }
                    out.println();
                }
            }
        }
    }

    private Record.List errors = new Record.List();
    private Record.List warnings = new Record.List();
    private Record.List messages = new Record.List();

    public abstract void error(Symbol symbol, String msg)
     {
        error(symbol.getStart(), symbol.getEnd(), msg);
    }

    public abstract void error(Scanner.Exception e)
     {
        int location = Symbol.makePosition(e.line, e.column);
        error(location, location, e.getMessage());
    }

    public abstract void error(int start_pos, int end_pos, String msg)
     {
        errors.add(new Record(start_pos, end_pos, msg));
    }

    public abstract void error(String msg)
     {
        error(0, 0, msg);
    }

    public abstract void warning(Symbol symbol, String msg)
     {
        warning(symbol.getStart(), symbol.getEnd(), msg);
    }

    public abstract void warning(int start_pos, int end_pos, String msg)
     {
        warnings.add(new Record(start_pos, end_pos, msg));
    }

    public abstract void warning(String msg)
     {
        warning(0, 0, msg);
    }

    public abstract void message(String msg)
     {
        messages.add(new Record(0, 0, msg));
    }

    public abstract boolean hasErrors()
     {
        return errors.size() > 0;
    }

    public abstract void report(String src_name, SrcReader src_reader)
     {
        int n_err = errors.size(), n_warn = warnings.size();
        if (n_err > 0 || n_warn > 0)
         {
            PrintStream out = System.err;

            for (Record rec = errors.start(); rec != null; rec = rec.next)
             {
                rec.report("Error", out, src_reader);
            }
            errors.reset();

            for (Record rec = warnings.start(); rec != null; rec = rec.next)
             {
                rec.report("Warning", out, src_reader);
            }
            warnings.reset();

            if (n_err > 0 || n_warn > 0)
             {
                out.print(src_name);
                out.print(": ");
                out.print(n_err);
                out.print(" error");
                if (n_err != 1)
                    out.print('s');
                out.print(", ");
                out.print(n_warn);
                out.print(" warning");
                if (n_warn != 1)
                    out.print('s');
                out.println('.');
            }
        }
        for (Record rec = messages.start(); rec != null; rec = rec.next)
         {
            rec.report(null, System.out, src_reader);
        }
        messages.reset();
    }
}
