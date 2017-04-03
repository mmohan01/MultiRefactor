/*  Sesame - Storage and Querying architecture for RDF and RDF Schema
 *  Copyright (C) 2001-2004 Aduna
 *  Copyright (C) 2004-2005 Andrew Newman - Conversion to JRDF
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */
 package org.jrdf.parser.rdfxml;

/**
 * Provides methods for handling the standard XML Schema datatypes.
 **/
public class XmlDatatypeUtil {

  /**
   * Checks whether the supplied datatype is a primitive XML Schema
   * datatype.
   **/
  public static boolean isPrimitiveDatatype(String datatype) {
    return
        datatype.equals(XmlSchema.DURATION) ||
        datatype.equals(XmlSchema.DATETIME) ||
        datatype.equals(XmlSchema.TIME) ||
        datatype.equals(XmlSchema.DATE) ||
        datatype.equals(XmlSchema.GYEARMONTH) ||
        datatype.equals(XmlSchema.GYEAR) ||
        datatype.equals(XmlSchema.GMONTHDAY) ||
        datatype.equals(XmlSchema.GDAY) ||
        datatype.equals(XmlSchema.GMONTH) ||
        datatype.equals(XmlSchema.STRING) ||
        datatype.equals(XmlSchema.BOOLEAN) ||
        datatype.equals(XmlSchema.BASE64BINARY) ||
        datatype.equals(XmlSchema.HEXBINARY) ||
        datatype.equals(XmlSchema.FLOAT) ||
        datatype.equals(XmlSchema.DECIMAL) ||
        datatype.equals(XmlSchema.DOUBLE) ||
        datatype.equals(XmlSchema.ANYURI) ||
        datatype.equals(XmlSchema.QNAME) ||
        datatype.equals(XmlSchema.NOTATION);
  }

  /**
   * Checks whether the supplied datatype is a derived XML Schema
   * datatype.
   **/
  public static boolean isDerivedDatatype(String datatype) {
    return
        datatype.equals(XmlSchema.NORMALIZEDSTRING) ||
        datatype.equals(XmlSchema.TOKEN) ||
        datatype.equals(XmlSchema.LANGUAGE) ||
        datatype.equals(XmlSchema.NMTOKEN) ||
        datatype.equals(XmlSchema.NMTOKENS) ||
        datatype.equals(XmlSchema.NAME) ||
        datatype.equals(XmlSchema.NCNAME) ||
        datatype.equals(XmlSchema.ID) ||
        datatype.equals(XmlSchema.IDREF) ||
        datatype.equals(XmlSchema.IDREFS) ||
        datatype.equals(XmlSchema.ENTITY) ||
        datatype.equals(XmlSchema.ENTITIES) ||
        datatype.equals(XmlSchema.INTEGER) ||
        datatype.equals(XmlSchema.LONG) ||
        datatype.equals(XmlSchema.INT) ||
        datatype.equals(XmlSchema.SHORT) ||
        datatype.equals(XmlSchema.BYTE) ||
        datatype.equals(XmlSchema.NON_POSITIVE_INTEGER) ||
        datatype.equals(XmlSchema.NEGATIVE_INTEGER) ||
        datatype.equals(XmlSchema.NON_NEGATIVE_INTEGER) ||
        datatype.equals(XmlSchema.POSITIVE_INTEGER) ||
        datatype.equals(XmlSchema.UNSIGNED_LONG) ||
        datatype.equals(XmlSchema.UNSIGNED_INT) ||
        datatype.equals(XmlSchema.UNSIGNED_SHORT) ||
        datatype.equals(XmlSchema.UNSIGNED_BYTE);
  }

  /**
   * Checks whether the supplied datatype is a built-in XML Schema
   * datatype.
   **/
  public static boolean isBuiltInDatatype(String datatype) {
    return isPrimitiveDatatype(datatype) || isDerivedDatatype(datatype);
  }

  /**
   * Checks whether the supplied datatype is equal to xsd:decimal or
   * one of the built-in datatypes that is derived from xsd:decimal.
   **/
  public static boolean isDecimalDatatype(String datatype) {
    return
        datatype.equals(XmlSchema.DECIMAL) ||
        isIntegerDatatype(datatype);
  }

  /**
   * Checks whether the supplied datatype is equal to xsd:integer
   * or one of the built-in datatypes that is derived from
   * xsd:integer.
   **/
  public static boolean isIntegerDatatype(String datatype) {
    return
        datatype.equals(XmlSchema.INTEGER) ||
        datatype.equals(XmlSchema.LONG) ||
        datatype.equals(XmlSchema.INT) ||
        datatype.equals(XmlSchema.SHORT) ||
        datatype.equals(XmlSchema.BYTE) ||
        datatype.equals(XmlSchema.NON_POSITIVE_INTEGER) ||
        datatype.equals(XmlSchema.NEGATIVE_INTEGER) ||
        datatype.equals(XmlSchema.NON_NEGATIVE_INTEGER) ||
        datatype.equals(XmlSchema.POSITIVE_INTEGER) ||
        datatype.equals(XmlSchema.UNSIGNED_LONG) ||
        datatype.equals(XmlSchema.UNSIGNED_INT) ||
        datatype.equals(XmlSchema.UNSIGNED_SHORT) ||
        datatype.equals(XmlSchema.UNSIGNED_BYTE);
  }

  /**
   * Checks whether the supplied datatype is equal to xsd:float or
   * xsd:double.
   **/
  public static boolean isFloatingPointDatatype(String datatype) {
    return
        datatype.equals(XmlSchema.FLOAT) ||
        datatype.equals(XmlSchema.DOUBLE);
  }

  /**
   * Checks whether the supplied datatype is ordered. The values of
   * an ordered datatype can be compared to eachother using
   * operators like <tt>&lt;</tt> and <tt>&gt;</tt>.
   **/
  public static boolean isOrderedDatatype(String datatype) {
    return
        isDecimalDatatype(datatype) ||
        isFloatingPointDatatype(datatype);
  }

  /*--------------------+
   | Value checking      |
   +--------------------*/

  public static boolean isValidValue(String value, String datatype) {
    boolean result = true;

    if (datatype.equals(XmlSchema.DECIMAL)) {
      result = isValidDecimal(value);
    }
    else if (datatype.equals(XmlSchema.INTEGER)) {
      result = isValidInteger(value);
    }
    else if (datatype.equals(XmlSchema.NEGATIVE_INTEGER)) {
      result = isValidNegativeInteger(value);
    }
    else if (datatype.equals(XmlSchema.NON_POSITIVE_INTEGER)) {
      result = isValidNonPositiveInteger(value);
    }
    else if (datatype.equals(XmlSchema.NON_NEGATIVE_INTEGER)) {
      result = isValidNonNegativeInteger(value);
    }
    else if (datatype.equals(XmlSchema.POSITIVE_INTEGER)) {
      result = isValidPositiveInteger(value);
    }
    else if (datatype.equals(XmlSchema.LONG)) {
      result = isValidLong(value);
    }
    else if (datatype.equals(XmlSchema.INT)) {
      result = isValidInt(value);
    }
    else if (datatype.equals(XmlSchema.SHORT)) {
      result = isValidShort(value);
    }
    else if (datatype.equals(XmlSchema.BYTE)) {
      result = isValidByte(value);
    }
    else if (datatype.equals(XmlSchema.UNSIGNED_LONG)) {
      result = isValidUnsignedLong(value);
    }
    else if (datatype.equals(XmlSchema.UNSIGNED_INT)) {
      result = isValidUnsignedInt(value);
    }
    else if (datatype.equals(XmlSchema.UNSIGNED_SHORT)) {
      result = isValidUnsignedShort(value);
    }
    else if (datatype.equals(XmlSchema.UNSIGNED_BYTE)) {
      result = isValidUnsignedByte(value);
    }
    else if (datatype.equals(XmlSchema.FLOAT)) {
      result = isValidFloat(value);
    }
    else if (datatype.equals(XmlSchema.DOUBLE)) {
      result = isValidDouble(value);
    }
    else if (datatype.equals(XmlSchema.BOOLEAN)) {
      result = isValidBoolean(value);
    }

    return result;
  }

  public static boolean isValidDecimal(String value) {
    try {
      normalizeDecimal(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidInteger(String value) {
    try {
      normalizeInteger(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidNegativeInteger(String value) {
    try {
      normalizeNegativeInteger(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidNonPositiveInteger(String value) {
    try {
      normalizeNonPositiveInteger(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidNonNegativeInteger(String value) {
    try {
      normalizeNonNegativeInteger(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidPositiveInteger(String value) {
    try {
      normalizePositiveInteger(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidLong(String value) {
    try {
      normalizeLong(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidInt(String value) {
    try {
      normalizeInt(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidShort(String value) {
    try {
      normalizeShort(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidByte(String value) {
    try {
      normalizeByte(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidUnsignedLong(String value) {
    try {
      normalizeUnsignedLong(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidUnsignedInt(String value) {
    try {
      normalizeUnsignedInt(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidUnsignedShort(String value) {
    try {
      normalizeUnsignedShort(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidUnsignedByte(String value) {
    try {
      normalizeUnsignedByte(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidFloat(String value) {
    try {
      normalizeFloat(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidDouble(String value) {
    try {
      normalizeDouble(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  public static boolean isValidBoolean(String value) {
    try {
      normalizeBoolean(value);
      return true;
    }
    catch (IllegalArgumentException e) {
      return false;
    }
  }

  /*--------------------+
   | Value normalization |
   +--------------------*/

  /**
   * Normalizes the supplied value according to the normalization
   * rules for the supplied datatype.
   *
   * @param value The value to normalize.
   * @param datatype The value's datatype.
   * @return The normalized value if there are any (supported)
   * normalization rules for the supplied datatype, or the original
   * supplied value otherwise.
   * @exception IllegalArgumentException If the supplied value is
   * illegal considering the supplied datatype.
   **/
  public static String normalize(String value, String datatype) {
    String result = value;

    if (datatype.equals(XmlSchema.DECIMAL)) {
      result = normalizeDecimal(value);
    }
    else if (datatype.equals(XmlSchema.INTEGER)) {
      result = normalizeInteger(value);
    }
    else if (datatype.equals(XmlSchema.NEGATIVE_INTEGER)) {
      result = normalizeNegativeInteger(value);
    }
    else if (datatype.equals(XmlSchema.NON_POSITIVE_INTEGER)) {
      result = normalizeNonPositiveInteger(value);
    }
    else if (datatype.equals(XmlSchema.NON_NEGATIVE_INTEGER)) {
      result = normalizeNonNegativeInteger(value);
    }
    else if (datatype.equals(XmlSchema.POSITIVE_INTEGER)) {
      result = normalizePositiveInteger(value);
    }
    else if (datatype.equals(XmlSchema.LONG)) {
      result = normalizeLong(value);
    }
    else if (datatype.equals(XmlSchema.INT)) {
      result = normalizeInt(value);
    }
    else if (datatype.equals(XmlSchema.SHORT)) {
      result = normalizeShort(value);
    }
    else if (datatype.equals(XmlSchema.BYTE)) {
      result = normalizeByte(value);
    }
    else if (datatype.equals(XmlSchema.UNSIGNED_LONG)) {
      result = normalizeUnsignedLong(value);
    }
    else if (datatype.equals(XmlSchema.UNSIGNED_INT)) {
      result = normalizeUnsignedInt(value);
    }
    else if (datatype.equals(XmlSchema.UNSIGNED_SHORT)) {
      result = normalizeUnsignedShort(value);
    }
    else if (datatype.equals(XmlSchema.UNSIGNED_BYTE)) {
      result = normalizeUnsignedByte(value);
    }
    else if (datatype.equals(XmlSchema.FLOAT)) {
      result = normalizeFloat(value);
    }
    else if (datatype.equals(XmlSchema.DOUBLE)) {
      result = normalizeDouble(value);
    }
    else if (datatype.equals(XmlSchema.BOOLEAN)) {
      result = normalizeBoolean(value);
    }

    return result;
  }

  /**
   * Normalizes a boolean value to its canonical representation.
   * More specifically, the values <tt>1</tt> and <tt>0</tt> will be
   * normalized to the canonical values <tt>true</tt> and
   * <tt>false</tt>, respectively. Supplied canonical values will
   * remain as is.
   *
   * @param value The boolean value to normalize.
   * @return The normalized value.
   * @exception IllegalArgumentException If the supplied value is
   * not a legal boolean.
   **/
  public static String normalizeBoolean(String value) {
    if ("1".equals(value)) {
      return "true";
    }
    else if ("0".equals(value)) {
      return "false";
    }
    else if ("true".equals(value) || "false".equals(value)) {
      return value;
    }
    else {
      throw new IllegalArgumentException(
          "Not a legal boolean value: " + value);
    }
  }

  /**
   * Normalizes a decimal to its canonical representation.
   * For example: <tt>120</tt> becomes <tt>120.0</tt>,
   * <tt>+.3</tt> becomes <tt>0.3</tt>,
   * <tt>00012.45000</tt> becomes <tt>12.45</tt> and
   * <tt>-.0</tt> becomes <tt>0.0</tt>.
   *
   * @param decimal The decimal to normalize.
   * @return The canonical representation of <tt>decimal</tt>.
   * @throws IllegalArgumentException If one of the supplied strings
   * is not a legal decimal.
   **/
  public static String normalizeDecimal(String decimal) {
    String errMsg = "Not a legal decimal: " + decimal;

    int decLength = decimal.length();
    StringBuffer result = new StringBuffer(decLength + 2);

    if (0 == decLength) {
      _throwIAE(errMsg);
    }

    boolean isZeroPointZero = true;

    // process any sign info
    int idx = 0;
    if ('-' == decimal.charAt(idx)) {
      result.append('-');
      idx++;
    }
    else if ('+' == decimal.charAt(idx)) {
      idx++;
    }

    if (idx == decLength) {
      _throwIAE(errMsg);
    }

    // skip any leading zeros
    while (idx < decLength && '0' == decimal.charAt(idx)) {
      idx++;
    }

    // Process digits before the dot
    if (idx == decLength) {
      // decimal consists of zeros only
      result.append('0');
    }
    else if (idx < decLength && '.' == decimal.charAt(idx)) {
      // no non-zero digit before the dot
      result.append('0');
    }
    else {
      isZeroPointZero = false;

      // Copy any digits before the dot
      while (idx < decLength) {
        char c = decimal.charAt(idx);
        if ('.' == c) {
          break;
        }
        if (!_isDigit(c)) {
          _throwIAE(errMsg);
        }
        result.append(c);
        idx++;
      }
    }

    result.append('.');

    // Process digits after the dot
    if (idx == decLength) {
      // No dot was found in the decimal
      result.append('0');
    }
    else {
      idx++;

      // search last non-zero digit
      int lastIdx = decLength - 1;
      while (0 <= lastIdx && '0' == decimal.charAt(lastIdx)) {
        lastIdx--;
      }

      if (idx > lastIdx) {
        // No non-zero digits found
        result.append('0');
      }
      else {
        isZeroPointZero = false;

        while (idx <= lastIdx) {
          char c = decimal.charAt(idx);
          if (!_isDigit(c)) {
            _throwIAE(errMsg);
          }
          result.append(c);
          idx++;
        }
      }
    }

    if (isZeroPointZero) {
      // Make sure we don't return "-0.0"
      return "0.0";
    }
    else {
      return result.toString();
    }
  }

  /**
   * Normalizes an integer to its canonical representation.
   * For example: <tt>+120</tt> becomes <tt>120</tt> and
   * <tt>00012</tt> becomes <tt>12</tt>.
   *
   * @param value The value to normalize.
   * @return The canonical representation of <tt>value</tt>.
   * @throws IllegalArgumentException If the supplied value
   * is not a legal integer.
   **/
  public static String normalizeInteger(String value) {
    return _normalizeIntegerValue(value, null, null);
  }

  /**
   * Normalizes an xsd:negativeInteger.
   **/
  public static String normalizeNegativeInteger(String value) {
    return _normalizeIntegerValue(value, null, "-1");
  }

  /**
   * Normalizes an xsd:nonPositiveInteger.
   **/
  public static String normalizeNonPositiveInteger(String value) {
    return _normalizeIntegerValue(value, null, "0");
  }

  /**
   * Normalizes an xsd:nonNegativeInteger.
   **/
  public static String normalizeNonNegativeInteger(String value) {
    return _normalizeIntegerValue(value, "0", null);
  }

  /**
   * Normalizes an xsd:positiveInteger.
   **/
  public static String normalizePositiveInteger(String value) {
    return _normalizeIntegerValue(value, "1", null);
  }

  /**
   * Normalizes an xsd:long.
   **/
  public static String normalizeLong(String value) {
    return _normalizeIntegerValue(value, "-9223372036854775808",
        "9223372036854775807");
  }

  /**
   * Normalizes an xsd:int.
   **/
  public static String normalizeInt(String value) {
    return _normalizeIntegerValue(value, "-2147483648", "2147483647");
  }

  /**
   * Normalizes an xsd:short.
   **/
  public static String normalizeShort(String value) {
    return _normalizeIntegerValue(value, "-32768", "32767");
  }

  /**
   * Normalizes an xsd:byte.
   **/
  public static String normalizeByte(String value) {
    return _normalizeIntegerValue(value, "-128", "127");
  }

  /**
   * Normalizes an xsd:unsignedLong.
   **/
  public static String normalizeUnsignedLong(String value) {
    return _normalizeIntegerValue(value, "0", "18446744073709551615");
  }

  /**
   * Normalizes an xsd:unsignedInt.
   **/
  public static String normalizeUnsignedInt(String value) {
    return _normalizeIntegerValue(value, "0", "4294967295");
  }

  /**
   * Normalizes an xsd:unsignedShort.
   **/
  public static String normalizeUnsignedShort(String value) {
    return _normalizeIntegerValue(value, "0", "65535");
  }

  /**
   * Normalizes an xsd:unsignedByte.
   **/
  public static String normalizeUnsignedByte(String value) {
    return _normalizeIntegerValue(value, "0", "255");
  }

  /**
   * Normalizes an integer to its canonical representation and
   * checks that the value is in the range [minValue, maxValue].
   **/
  private static String _normalizeIntegerValue(
      String integer, String minValue, String maxValue) {
    String errMsg = "Not a legal integer: " + integer;

    int intLength = integer.length();

    if (0 == intLength) {
      _throwIAE(errMsg);
    }

    int idx = 0;

    // process any sign info
    boolean isNegative = false;
    if ('-' == integer.charAt(idx)) {
      isNegative = true;
      idx++;
    }
    else if ('+' == integer.charAt(idx)) {
      idx++;
    }

    if (idx == intLength) {
      _throwIAE(errMsg);
    }

    if ('0' == integer.charAt(idx) && idx < intLength - 1) {
      // integer starts with a zero followed by more characters,
      // skip any leading zeros
      idx++;
      while (idx < intLength - 1 && '0' == integer.charAt(idx)) {
        idx++;
      }
    }

    String norm = integer.substring(idx);

    // Check that all characters in 'norm' are digits
    for (int i = 0; i < norm.length(); i++) {
      if (!_isDigit(norm.charAt(i))) {
        _throwIAE(errMsg);
      }
    }

    if (isNegative && '0' != norm.charAt(0)) {
      norm = "-" + norm;
    }

    // Check lower and upper bounds, if applicable
    if (null != minValue) {
      if (0 > compareCanonicalIntegers(norm, minValue)) {
        _throwIAE("Value smaller than minimum value");
      }
    }
    if (null != maxValue) {
      if (0 < compareCanonicalIntegers(norm, maxValue)) {
        _throwIAE("Value larger than maximum value");
      }
    }

    return norm;
  }

  /**
   * Normalizes a float to its canonical representation.
   *
   * @param value The value to normalize.
   * @return The canonical representation of <tt>value</tt>.
   * @throws IllegalArgumentException If the supplied value
   * is not a legal float.
   **/
  public static String normalizeFloat(String value) {
    return _normalizeFPNumber(value,
        "-16777215.0", "16777215.0", "-149", "104");
  }

  /**
   * Normalizes a double to its canonical representation.
   *
   * @param value The value to normalize.
   * @return The canonical representation of <tt>value</tt>.
   * @throws IllegalArgumentException If the supplied value
   * is not a legal double.
   **/
  public static String normalizeDouble(String value) {
    return _normalizeFPNumber(value,
        "-9007199254740991.0", "9007199254740991.0", "-1075", "970");
  }

  /**
   * Normalizes a floating point number to its canonical
   * representation.
   *
   * @param value The value to normalize.
   * @return The canonical representation of <tt>value</tt>.
   * @throws IllegalArgumentException If the supplied value
   * is not a legal floating point number.
   **/
  public static String normalizeFPNumber(String value) {
    return _normalizeFPNumber(value, null, null, null, null);
  }

  /**
   * Normalizes a floating point number to its canonical
   * representation.
   *
   * @param value The value to normalize.
   * @param minMantissa A normalized decimal indicating the lowest
   * value that the mantissa may have.
   * @param maxMantissa A normalized decimal indicating the highest
   * value that the mantissa may have.
   * @param minExponent A normalized integer indicating the lowest
   * value that the exponent may have.
   * @param maxExponent A normalized integer indicating the highest
   * value that the exponent may have.
   * @return The canonical representation of <tt>value</tt>.
   * @throws IllegalArgumentException If the supplied value
   * is not a legal floating point number.
   **/
  private static String _normalizeFPNumber(
      String value,
      String minMantissa, String maxMantissa,
      String minExponent, String maxExponent) {
    // handle special values
    if ("INF".equals(value) || "-INF".equals(value) || "NaN".equals(value)) {
      return value;
    }

    // Search for the exponent character E or e
    int eIdx = value.indexOf('E');
    if (-1 == eIdx) {
      // try lower case
      eIdx = value.indexOf('e');
    }

    // Extract mantissa and exponent
    String mantissa, exponent;
    if (-1 == eIdx) {
      mantissa = normalizeDecimal(value);
      exponent = "0";
    }
    else {
      mantissa = normalizeDecimal(value.substring(0, eIdx));
      exponent = normalizeInteger(value.substring(eIdx + 1));
    }

    // Check lower and upper bounds, if applicable
    if (null != minMantissa) {
      if (0 > compareCanonicalDecimals(mantissa, minMantissa)) {
        _throwIAE("Mantissa smaller than minimum value (" + minMantissa + ")");
      }
    }
    if (null != maxMantissa) {
      if (0 < compareCanonicalDecimals(mantissa, maxMantissa)) {
        _throwIAE("Mantissa larger than maximum value (" + maxMantissa + ")");
      }
    }
    if (null != minExponent) {
      if (0 > compareCanonicalIntegers(exponent, minExponent)) {
        _throwIAE("Exponent smaller than minimum value (" + minExponent + ")");
      }
    }
    if (null != maxExponent) {
      if (0 < compareCanonicalIntegers(exponent, maxExponent)) {
        _throwIAE("Exponent larger than maximum value (" + maxExponent + ")");
      }
    }

    // Normalize mantissa to one non-zero digit before the dot
    int shift = 0;

    int dotIdx = mantissa.indexOf('.');
    int digitCount = dotIdx;
    if ('-' == mantissa.charAt(0)) {
      digitCount--;
    }

    if (1 < digitCount) {
      // more than one digit before the dot, e.g 123.45, -10.0 or 100.0
      StringBuffer buf = new StringBuffer(mantissa.length());
      int firstDigitIdx = 0;
      if ('-' == mantissa.charAt(0)) {
        buf.append('-');
        firstDigitIdx = 1;
      }
      buf.append(mantissa.charAt(firstDigitIdx));
      buf.append('.');
      buf.append(mantissa.substring(firstDigitIdx + 1, dotIdx));
      buf.append(mantissa.substring(dotIdx + 1));

      mantissa = buf.toString();

      // Check if the mantissa has excessive trailing zeros.
      // For example, 100.0 will be normalize to 1.000 and
      // -10.0 to -1.00.
      int nonZeroIdx = mantissa.length() - 1;
      while (3 <= nonZeroIdx && '0' == mantissa.charAt(nonZeroIdx)) {
        nonZeroIdx--;
      }

      if (3 > nonZeroIdx && '-' == mantissa.charAt(0)) {
        nonZeroIdx++;
      }

      if (nonZeroIdx < mantissa.length() - 1) {
        mantissa = mantissa.substring(0, nonZeroIdx + 1);
      }

      shift = 1 - digitCount;
    }
    else if (mantissa.startsWith("0.") || mantissa.startsWith("-0.")) {
      // Example mantissas: 0.0, -0.1, 0.00345 and 0.09
      // search first non-zero digit
      int nonZeroIdx = 2;
      while (nonZeroIdx < mantissa.length() &&
          '0' == mantissa.charAt(nonZeroIdx)) {
        nonZeroIdx++;
      }

      // 0.0 does not need any normalization:
      if (nonZeroIdx < mantissa.length()) {
        StringBuffer buf = new StringBuffer(mantissa.length());
        buf.append(mantissa.charAt(nonZeroIdx));
        buf.append('.');
        if (nonZeroIdx == mantissa.length() - 1) {
          // There was only one non-zero digit, e.g. as in 0.09
          buf.append('0');
        }
        else {
          buf.append(mantissa.substring(nonZeroIdx + 1));
        }

        mantissa = buf.toString();
        shift = nonZeroIdx - 1;
      }
    }

    if (0 != shift) {
      try {
        int exp = Integer.parseInt(exponent);
        exponent = String.valueOf(exp - shift);
      }
      catch (NumberFormatException e) {
        throw new RuntimeException(
            "NumberFormatException: " + e.getMessage());
      }
    }

    return mantissa + "E" + exponent;
  }

  /*--------------------+
   | Value comparison    |
   +--------------------*/

  public static int compare(String value1, String value2, String datatype) {
    if (datatype.equals(XmlSchema.DECIMAL)) {
      return compareDecimals(value1, value2);
    }
    else if (datatype.equals(XmlSchema.INTEGER)) {
      return compareIntegers(value1, value2);
    }
    else if (datatype.equals(XmlSchema.NEGATIVE_INTEGER)) {
      return compareNegativeIntegers(value1, value2);
    }
    else if (datatype.equals(XmlSchema.NON_POSITIVE_INTEGER)) {
      return compareNonPositiveIntegers(value1, value2);
    }
    else if (datatype.equals(XmlSchema.NON_NEGATIVE_INTEGER)) {
      return compareNonNegativeIntegers(value1, value2);
    }
    else if (datatype.equals(XmlSchema.POSITIVE_INTEGER)) {
      return comparePositiveIntegers(value1, value2);
    }
    else if (datatype.equals(XmlSchema.LONG)) {
      return compareLongs(value1, value2);
    }
    else if (datatype.equals(XmlSchema.INT)) {
      return compareInts(value1, value2);
    }
    else if (datatype.equals(XmlSchema.SHORT)) {
      return compareShorts(value1, value2);
    }
    else if (datatype.equals(XmlSchema.BYTE)) {
      return compareBytes(value1, value2);
    }
    else if (datatype.equals(XmlSchema.UNSIGNED_LONG)) {
      return compareUnsignedLongs(value1, value2);
    }
    else if (datatype.equals(XmlSchema.UNSIGNED_INT)) {
      return compareUnsignedInts(value1, value2);
    }
    else if (datatype.equals(XmlSchema.UNSIGNED_SHORT)) {
      return compareUnsignedShorts(value1, value2);
    }
    else if (datatype.equals(XmlSchema.UNSIGNED_BYTE)) {
      return compareUnsignedBytes(value1, value2);
    }
    else if (datatype.equals(XmlSchema.FLOAT)) {
      return compareFloats(value1, value2);
    }
    else if (datatype.equals(XmlSchema.DOUBLE)) {
      return compareDoubles(value1, value2);
    }
    else {
      throw new IllegalArgumentException("datatype is not ordered");
    }
  }

  /**
   * Compares two decimals to eachother.
   *
   * @return A negative number if <tt>dec1</tt> is smaller than
   * <tt>dec2</tt>, <tt>0</tt> if they are equal, or positive
   * (&gt;0) if <tt>dec1</tt> is larger than <tt>dec2</tt>.
   * @throws IllegalArgumentException If one of the supplied strings is
   * not a legal decimal.
   **/
  public static int compareDecimals(String dec1, String dec2) {
    dec1 = normalizeDecimal(dec1);
    dec2 = normalizeDecimal(dec2);

    return compareCanonicalDecimals(dec1, dec2);
  }

  /**
   * Compares two canonical decimals to eachother.
   *
   * @return A negative number if <tt>dec1</tt> is smaller than
   * <tt>dec2</tt>, <tt>0</tt> if they are equal, or positive
   * (&gt;0) if <tt>dec1</tt> is larger than <tt>dec2</tt>. The
   * result is undefined when one or both of the arguments is not
   * a canonical decimal.
   * @throws IllegalArgumentException If one of the supplied strings is
   * not a legal decimal.
   **/
  public static int compareCanonicalDecimals(String dec1, String dec2) {
    if (dec1.equals(dec2)) {
      return 0;
    }

    // Check signs
    if ('-' == dec1.charAt(0) && '-' != dec2.charAt(0)) {
      // dec1 is negative, dec2 is not
      return -1;
    }
    if ('-' == dec2.charAt(0) && '-' != dec1.charAt(0)) {
      // dec2 is negative, dec1 is not
      return 1;
    }

    int dotIdx1 = dec1.indexOf('.');
    int dotIdx2 = dec2.indexOf('.');

    // The decimal with the most digits before the dot is the largest
    int result = dotIdx1 - dotIdx2;

    if (0 == result) {
      // equal number of digits before the dot, compare them
      for (int i = 0; 0 == result && i < dotIdx1; i++) {
        result = dec1.charAt(i) - dec2.charAt(i);
      }

      // Continue comparing digits after the dot if necessary
      int dec1Length = dec1.length();
      int dec2Length = dec2.length();
      int lastIdx = dec1Length <= dec2Length ? dec1Length : dec2Length;

      for (int i = dotIdx1 + 1; 0 == result && i < lastIdx; i++) {
        result = dec1.charAt(i) - dec2.charAt(i);
      }

      // Still equal? The decimal with the most digits is the largest
      if (0 == result) {
        result = dec1Length - dec2Length;
      }
    }

    if ('-' == dec1.charAt(0)) {
      // reverse result for negative values
      result = -result;
    }

    return result;
  }

  /**
   * Compares two integers to eachother.
   *
   * @return A negative number if <tt>int1</tt> is smaller than
   * <tt>int2</tt>, <tt>0</tt> if they are equal, or positive
   * (&gt;0) if <tt>int1</tt> is larger than <tt>int2</tt>.
   * @throws IllegalArgumentException If one of the supplied strings is
   * not a legal integer.
   **/
  public static int compareIntegers(String int1, String int2) {
    int1 = normalizeInteger(int1);
    int2 = normalizeInteger(int2);

    return compareCanonicalIntegers(int1, int2);
  }

  /**
   * Compares two canonical integers to eachother.
   *
   * @return A negative number if <tt>int1</tt> is smaller than
   * <tt>int2</tt>, <tt>0</tt> if they are equal, or positive
   * (&gt;0) if <tt>int1</tt> is larger than <tt>int2</tt>. The
   * result is undefined when one or both of the arguments is not
   * a canonical integer.
   * @throws IllegalArgumentException If one of the supplied strings is
   * not a legal integer.
   **/
  public static int compareCanonicalIntegers(String int1, String int2) {
    if (int1.equals(int2)) {
      return 0;
    }

    // Check signs
    if ('-' == int1.charAt(0) && '-' != int2.charAt(0)) {
      // int1 is negative, int2 is not
      return -1;
    }
    if ('-' == int2.charAt(0) && '-' != int1.charAt(0)) {
      // int2 is negative, int1 is not
      return 1;
    }

    // The integer with the most digits is the largest
    int result = int1.length() - int2.length();

    if (0 == result) {
      // equal number of digits, compare them
      for (int i = 0; 0 == result && i < int1.length(); i++) {
        result = int1.charAt(i) - int2.charAt(i);
      }
    }

    if ('-' == int1.charAt(0)) {
      // reverse result for negative values
      result = -result;
    }

    return result;
  }

  public static int compareNegativeIntegers(String int1, String int2) {
    int1 = normalizeNegativeInteger(int1);
    int2 = normalizeNegativeInteger(int2);

    return compareCanonicalIntegers(int1, int2);
  }

  public static int compareNonPositiveIntegers(String int1, String int2) {
    int1 = normalizeNonPositiveInteger(int1);
    int2 = normalizeNonPositiveInteger(int2);

    return compareCanonicalIntegers(int1, int2);
  }

  public static int compareNonNegativeIntegers(String int1, String int2) {
    int1 = normalizeNonNegativeInteger(int1);
    int2 = normalizeNonNegativeInteger(int2);

    return compareCanonicalIntegers(int1, int2);
  }

  public static int comparePositiveIntegers(String int1, String int2) {
    int1 = normalizePositiveInteger(int1);
    int2 = normalizePositiveInteger(int2);

    return compareCanonicalIntegers(int1, int2);
  }

  public static int compareLongs(String int1, String int2) {
    int1 = normalizeLong(int1);
    int2 = normalizeLong(int2);

    return compareCanonicalIntegers(int1, int2);
  }

  public static int compareInts(String int1, String int2) {
    int1 = normalizeInt(int1);
    int2 = normalizeInt(int2);

    return compareCanonicalIntegers(int1, int2);
  }

  public static int compareShorts(String int1, String int2) {
    int1 = normalizeShort(int1);
    int2 = normalizeShort(int2);

    return compareCanonicalIntegers(int1, int2);
  }

  public static int compareBytes(String int1, String int2) {
    int1 = normalizeByte(int1);
    int2 = normalizeByte(int2);

    return compareCanonicalIntegers(int1, int2);
  }

  public static int compareUnsignedLongs(String int1, String int2) {
    int1 = normalizeUnsignedLong(int1);
    int2 = normalizeUnsignedLong(int2);

    return compareCanonicalIntegers(int1, int2);
  }

  public static int compareUnsignedInts(String int1, String int2) {
    int1 = normalizeUnsignedInt(int1);
    int2 = normalizeUnsignedInt(int2);

    return compareCanonicalIntegers(int1, int2);
  }

  public static int compareUnsignedShorts(String int1, String int2) {
    int1 = normalizeUnsignedShort(int1);
    int2 = normalizeUnsignedShort(int2);

    return compareCanonicalIntegers(int1, int2);
  }

  public static int compareUnsignedBytes(String int1, String int2) {
    int1 = normalizeUnsignedByte(int1);
    int2 = normalizeUnsignedByte(int2);

    return compareCanonicalIntegers(int1, int2);
  }

  /**
   * Compares two floats to eachother.
   *
   * @return A negative number if <tt>float1</tt> is smaller than
   * <tt>float2</tt>, <tt>0</tt> if they are equal, or positive
   * (&gt;0) if <tt>float1</tt> is larger than <tt>float2</tt>.
   * @throws IllegalArgumentException If one of the supplied strings
   * is not a legal float or if <tt>NaN</tt> is compared to a float
   * other than <tt>NaN</tt>.
   **/
  public static int compareFloats(String float1, String float2) {
    float1 = normalizeFloat(float1);
    float2 = normalizeFloat(float2);

    return compareCanonicalFloats(float1, float2);
  }

  /**
   * Compares two canonical floats to eachother.
   *
   * @return A negative number if <tt>float1</tt> is smaller than
   * <tt>float2</tt>, <tt>0</tt> if they are equal, or positive
   * (&gt;0) if <tt>float1</tt> is larger than <tt>float2</tt>. The
   * result is undefined when one or both of the arguments is not
   * a canonical float.
   * @throws IllegalArgumentException If one of the supplied strings
   * is not a legal float or if <tt>NaN</tt> is compared to a
   * float other than <tt>NaN</tt>.
   **/
  public static int compareCanonicalFloats(String float1, String float2) {
    return compareCanonicalFPNumbers(float1, float2);
  }

  /**
   * Compares two doubles to eachother.
   *
   * @return A negative number if <tt>double1</tt> is smaller than
   * <tt>double2</tt>, <tt>0</tt> if they are equal, or positive
   * (&gt;0) if <tt>double1</tt> is larger than <tt>double2</tt>.
   * @throws IllegalArgumentException If one of the supplied strings
   * is not a legal double or if <tt>NaN</tt> is compared to a
   * double other than <tt>NaN</tt>.
   **/
  public static int compareDoubles(String double1, String double2) {
    double1 = normalizeDouble(double1);
    double2 = normalizeDouble(double2);

    return compareCanonicalDoubles(double1, double2);
  }

  /**
   * Compares two canonical doubles to eachother.
   *
   * @return A negative number if <tt>double1</tt> is smaller than
   * <tt>double2</tt>, <tt>0</tt> if they are equal, or positive
   * (&gt;0) if <tt>double1</tt> is larger than <tt>double2</tt>. The
   * result is undefined when one or both of the arguments is not
   * a canonical double.
   * @throws IllegalArgumentException If one of the supplied strings
   * is not a legal double or if <tt>NaN</tt> is compared to a
   * double other than <tt>NaN</tt>.
   **/
  public static int compareCanonicalDoubles(String double1, String double2) {
    return compareCanonicalFPNumbers(double1, double2);
  }

  /**
   * Compares two floating point numbers to eachother.
   *
   * @return A negative number if <tt>float1</tt> is smaller than
   * <tt>float2</tt>, <tt>0</tt> if they are equal, or positive
   * (&gt;0) if <tt>float1</tt> is larger than <tt>float2</tt>.
   * @throws IllegalArgumentException If one of the supplied strings
   * is not a legal floating point number or if <tt>NaN</tt> is
   * compared to a floating point number other than <tt>NaN</tt>.
   **/
  public static int compareFPNumbers(String fp1, String fp2) {
    fp1 = normalizeFPNumber(fp1);
    fp2 = normalizeFPNumber(fp2);

    return compareCanonicalFPNumbers(fp1, fp2);
  }

  /**
   * Compares two canonical floating point numbers to eachother.
   *
   * @return A negative number if <tt>float1</tt> is smaller than
   * <tt>float2</tt>, <tt>0</tt> if they are equal, or positive
   * (&gt;0) if <tt>float1</tt> is larger than <tt>float2</tt>. The
   * result is undefined when one or both of the arguments is not
   * a canonical floating point number.
   * @throws IllegalArgumentException If one of the supplied strings
   * is not a legal floating point number or if <tt>NaN</tt> is
   * compared to a floating point number other than <tt>NaN</tt>.
   **/
  public static int compareCanonicalFPNumbers(String float1, String float2) {
    // Handle special case NaN
    if ("NaN".equals(float1) || "NaN".equals(float2)) {
      if (float1.equals(float2)) {
        // NaN is equal to itself
        return 0;
      }
      else {
        _throwIAE("NaN cannot be compared to other floats");
      }
    }

    // Handle special case INF
    if ("INF".equals(float1)) {
      return "INF".equals(float2) ? 0 : 1;
    }
    else if ("INF".equals(float2)) {
      return -1;
    }

    // Handle special case -INF
    if ("-INF".equals(float1)) {
      return "-INF".equals(float2) ? 0 : -1;
    }
    else if ("-INF".equals(float2)) {
      return 1;
    }

    // Check signs
    if ('-' == float1.charAt(0) && '-' != float2.charAt(0)) {
      // float1 is negative, float2 is not
      return -1;
    }
    if ('-' == float2.charAt(0) && '-' != float1.charAt(0)) {
      // float2 is negative, float1 is not
      return 1;
    }

    int eIdx1 = float1.indexOf('E');
    String mantissa1 = float1.substring(0, eIdx1);
    String exponent1 = float1.substring(eIdx1 + 1);

    int eIdx2 = float2.indexOf('E');
    String mantissa2 = float2.substring(0, eIdx2);
    String exponent2 = float2.substring(eIdx2 + 1);

    // Compare exponents
    int result = compareCanonicalIntegers(exponent1, exponent2);

    if (0 != result && '-' == float1.charAt(0)) {
      // reverse result for negative values
      result = -result;
    }

    if (0 == result) {
      // Equal exponents, compare mantissas
      result = compareCanonicalDecimals(mantissa1, mantissa2);
    }

    return result;
  }

  private static final boolean _isDigit(char c) {
    return '0' <= c && '9' >= c;
  }

  /**
   * Throws an IllegalArgumentException that contains the supplied
   * message.
   **/
  private static final void _throwIAE(String msg) {
    throw new IllegalArgumentException(msg);
  }

  /*
           public static void main(String[] args) {
          System.out.println(normalizeFloat(args[0]));
           }
   */
}
