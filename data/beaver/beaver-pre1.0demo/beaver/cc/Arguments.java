/**
 * Beaver: a compiler front-end compiler
 * Copyright (c) 2003-2012, Alexander Demenchuk
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 *
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. Neither the name of the copyright holders nor the names of its
 *    contributors may be used to endorse or promote products derived from
 *    this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */
package beaver.cc;

import java.io.File;
import java.io.IOException;

class Arguments {
    String  pkgName;
    String  clsNamePrefix;
    String  baseScannerName;
    String  baseParserName;
    boolean reduceViaDelegates;
    boolean tracingParser;
    File    specFile;
    File    srcDir;
    File    clsDir;
    File    scannerDFAFile;
    File    parserStatesFile;
    File    parserStatesConflictResolutionLogFile;

    Arguments(String[] args) throws IOException {
        for (int i = 0; i < args.length; i++) {
            if (!args[i].startsWith("-")) {
                specFile = new File(args[i]);
            } else if (args[i].equals("--src-dir")) {
                srcDir = new File(args[++i]);
            } else if (args[i].equals("--cls-dir")) {
                clsDir = new File(args[++i]);
            } else if (args[i].equals("--package")) {
                pkgName = args[++i];
            } else if (args[i].equals("--class-name-prefix")) {
                clsNamePrefix = args[++i];
            } else if (args[i].equals("--scanner-base")) {
                baseScannerName = args[++i];
            } else if (args[i].equals("--parser-base")) {
                baseParserName = args[++i];
            } else if (args[i].equals("--tracing-parser")) {
                tracingParser = true;
            } else if (args[i].equals("--scanner-dfa")) {
                scannerDFAFile = new File(args[++i]);
            } else if (args[i].equals("--parser-states")) {
                parserStatesFile = new File(args[++i]);
            } else if (args[i].equals("--resolved-conflicts")) {
                parserStatesConflictResolutionLogFile = new File(args[++i]);
            } else {
                System.err.println("Unknown argument " + args[i] + ": ignoring it");
            }
        }
        if (specFile == null) {
            throw new IllegalArgumentException("grammar file name was not found on the command line");
        }
        if (!specFile.canRead()) {
            throw new IllegalStateException("grammar file is not readable");
        }
        if (srcDir == null) {
            srcDir = new File(specFile.getParentFile(), "src");
        }
        if (clsDir == null) {
            clsDir = new File(specFile.getParentFile(), "bin");
        }
        if (!srcDir.exists()) {
            if (!srcDir.mkdirs()) {
                throw new IllegalStateException("cannot create output directory for the generated .java sources: "
                        + srcDir.getCanonicalPath());
            }
        } else if (!srcDir.canWrite()) {
            throw new IllegalStateException("output directory for .java sources (" + srcDir.getCanonicalPath()
                    + ") is not writable");
        }
        if (!clsDir.exists()) {
            if (!clsDir.mkdirs()) {
                throw new IllegalStateException("cannot create output directory for the generated .class files: "
                        + clsDir.getCanonicalPath());
            }
        } else if (!clsDir.canWrite()) {
            throw new IllegalStateException("output directory for .class files (" + clsDir.getCanonicalPath()
                    + ") is not writable");
        }
        if (baseScannerName == null) {
            throw new IllegalStateException("Mandatory scanner base class is not specified.");
        }
        if (baseParserName != null) {
            reduceViaDelegates = true;
        }
        if (pkgName == null) {
            pkgName = "";
        }
        if (clsNamePrefix == null) {
            clsNamePrefix = "";
        }
    }
}