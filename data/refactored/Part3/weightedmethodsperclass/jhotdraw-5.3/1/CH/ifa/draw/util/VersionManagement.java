/*
 * @(#)VersionManagement.java
 *
 * Project:		JHotdraw - a GUI framework for technical drawings
 *				http://www.jhotdraw.org
 *				http://jhotdraw.sourceforge.net
 * Copyright:	© by the original author(s) and all contributors
 * License:		Lesser GNU Public License (LGPL)
 *				http://www.opensource.org/licenses/lgpl-license.html
 */

package CH.ifa.draw.util;

import CH.ifa.draw.framework.JHotDrawRuntimeException;
import java.io.*;
import java.util.*;
import java.util.jar.*;

/**
 * Collection of utility methods that are useful for dealing with version information
 * retrieved from reading jar files or package loaded by the class manager. Some
 * methods also help comparing version information. The method getJHotDrawVersion()
 * can be used to retrieve the current version of JHotDraw as loaded by the class manager.
 *
 * @author Wolfram Kaiser
 * @version <$CURRENT_VERSION$>
 */
public class VersionManagement {
    public static String JHOTDRAW_COMPONENT = "CH.ifa.draw/";
    public static String JHOTDRAW_JAR = "jhotdraw.jar";

    public static Package[] packages = {
            Package.getPackage("CH.ifa.draw.applet"),
            Package.getPackage("CH.ifa.draw.application"),
            Package.getPackage("CH.ifa.draw.contrib"),
            Package.getPackage("CH.ifa.draw.figures"),
            Package.getPackage("CH.ifa.draw.framework"),
            Package.getPackage("CH.ifa.draw.standard"),
            Package.getPackage("CH.ifa.draw.util") };

    /**
	 * Return the version of the main package of the framework. A version number is
	 * available if there is a corresponding version entry in the JHotDraw jar file
	 * for the framework package.
	 */
    public static String getJHotDrawVersion() {
        // look for the framework main package
        Package pack = packages[4];
        return pack.getSpecificationVersion();
    }

    /**
	 *
	 */
    public static String getPackageVersion(final Package lookupPackage) {
        if (lookupPackage == null) {
            return null;
        }

        String specVersion = lookupPackage.getSpecificationVersion();
        if (specVersion != null) {
            return specVersion;
        }
         else {
            // search in parent package
            String normalizedPackageName = normalizePackageName(lookupPackage.getName());
            String nextPackageName = getNextPackage(normalizedPackageName);
            return getPackageVersion(Package.getPackage(nextPackageName));
        }
    }

    /**
	 * Check whether a given application version is compatible with the version
	 * of JHotDraw currently loaded in the Java VM. A version number is
	 * available if there is a corresponding version entry in the JHotDraw jar file
	 * for the framework package.
	 */
    public static boolean isCompatibleVersion(String compareVersionString) {
//		Package pack = VersionManagement.class.getPackage();
        Package pack = packages[4];
        if (compareVersionString == null) {
            return pack.getSpecificationVersion() == null;
        }
         else {
            return pack.isCompatibleWith(compareVersionString);
        }
    }

    /**
	 * Get the super package of a package specifier. The super package is the package
	 * specifier without the latest package name, e.g. the next package for "org.jhotdraw.tools"
	 * would be "org.jhotdraw".
	 *
	 * @param searchPackage package specifier
	 * @return next package if one is available, null otherwise
	 */
    protected String getNextPackage(String searchPackage) {
        if (searchPackage == null) {
            return null;
        }

        int foundNextPackage = searchPackage.lastIndexOf('.');
        if (foundNextPackage > 0) {
            return searchPackage.substring(0, foundNextPackage);
        }
         else {
            return null;
        }
    }

    /**
	 * A package name is normalized by replacing all path delimiters by "." to retrieve
	 * a valid standardized package specifier used in package declarations in Java source
	 * files.
	 *
	 * @param toBeNormalized package name to be normalized
	 * @return normalized package name
	 */
    public static String normalizePackageName(String toBeNormalized) {
        // first, replace the standard package delimiter used in jars
        String replaced = toBeNormalized.replace('/', '.');
        // then, replace the default path separator in case this one was used as well
        replaced
         = replaced.replace(File.pathSeparatorChar, '.');
        if (replaced.endsWith(".")) {
            int lastSeparator = replaced.lastIndexOf('.');
            return replaced.substring(0, lastSeparator);
        }
         else {
            return replaced;
        }
    }

    /**
	 * Get the version information specified in a jar manifest file without
	 * any leading or trailing "\"".
	 *
	 * @param versionString a version string with possibly leading or trailing "\""
	 * @return stripped version information
	 */
    public static String extractVersionInfo(String versionString) {
        // guarding conditions
        if (versionString == null) {
            return null;
        }
        if (versionString.length() == 0) {
            return "";
        }

        int startIndex = versionString.indexOf("\"");
        if (startIndex < 0) {
            startIndex = 0;
        }
         else {
            // start from next character
            startIndex++;
        }

        int endIndex = versionString.lastIndexOf("\"");
        if (endIndex < 0) {
            endIndex = versionString.length();
        }

        return versionString.substring(startIndex, endIndex);
    }
}
