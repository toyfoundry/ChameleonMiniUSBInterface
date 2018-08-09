package com.maxieds.chameleonminiusb;

import android.text.format.Time;
import org.apache.commons.lang3.ArrayUtils;
import java.util.Locale;
import java.util.Random;


/**
 * <h1>Utils</h1>
 * <h3>Utility class for misc functions and operations needed to operate the library code.</h3>
 *
 * @author  Maxie D. Schmidt
 * @email maxieds@gmail.com
 * @since 8/8/2018 (partially modified from the Chameleon Mini Live Debugger source since 12/31/17)
 * @ref
 * @ref
 */
public class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    public static final int FLAG_FALSE = 0;
    public static final int FLAG_TRUE = 1;
    public static boolean FLAG_IS_OTHER(int flagParam) { return flagParam != FLAG_FALSE && flagParam != FLAG_TRUE; }

    public static byte BYTE(int intFormat0xab) {
        return (byte) intFormat0xab;
    }

    public static byte MSB(int int32Param) {
        return BYTE((int32Param & 0xff000000) >> 24);
    }
    public static byte MSB(byte[] bytesArray) { return bytesArray[0]; }

    public static byte LSB(int int32Param) {
        return BYTE(int32Param & 0x000000ff);
    }
    public static byte LSB(byte[] bytesArray) { return bytesArray[bytesArray.length - 1]; }

    /**
     * Get random bytes seeded by the time. For use with generating random UID's.
     * @param numBytes
     * @return
     */
    public static byte[] generateRandomBytes(int numBytes) {
        Random rnGen = new Random(System.currentTimeMillis());
        byte[] randomBytes = new byte[numBytes];
        for(int b = 0; b < numBytes; b++)
            randomBytes[b] = (byte) rnGen.nextInt(0xff);
        return randomBytes;
    }

    /**
     * Generates a semi-random array of UID bytes with a pre-spefecified prefix set of bytes.
     * @param prefixBytes
     * @param desiredArrayLength
     * @return null (if parameters are non-sensical) or a prefixed byte array with randomly generated suffix bits
     */
    public static byte[] generateRandomBytes(byte[] prefixBytes, int desiredArrayLength) {
        int numRandomBytes = desiredArrayLength - prefixBytes.length;
        if(desiredArrayLength <= 0 || numRandomBytes < 0)
            return null;
        else if(numRandomBytes == 0)
            return prefixBytes;
        byte[] randomSuffixBytes = generateRandomBytes(numRandomBytes);
        return ArrayUtils.toPrimitive(arrayJoin(ArrayUtils.toObject(prefixBytes), ArrayUtils.toObject(randomSuffixBytes)));
    }

    /**
     * Returns a space-separated string of the input bytes in their two-digit
     * hexadecimal format.
     *
     * @param bytes
     * @return String hex string representation
     */
    public static String byteArrayToString(byte[] bytes) {
        if (bytes == null)
            return null;
        else if (bytes.length == 0)
            return "";
        StringBuilder hstr = new StringBuilder();
        hstr.append(String.format(Locale.ENGLISH, "%02x", bytes[0]));
        for (int b = 1; b < bytes.length; b++)
            hstr.append(" " + String.format(Locale.ENGLISH, "%02x", bytes[b]));
        return hstr.toString();
    }

    /**
     * Converts a string representation of a two-digit byte into a corresponding byte type.
     * @param byteStr
     * @return byte representation of the String
     */
    public static byte hexString2Byte(String byteStr) {
        if (byteStr.length() != 2) {
            return 0x00;
        }
        int lsb = Character.digit(byteStr.charAt(1), 16);
        int msb = Character.digit(byteStr.charAt(0), 16);
        return (byte) (lsb | msb << 4);
    }

    public static byte[] byteArrayFromString(String byteString) {

    }

    /**
     * Joins two arrays of the same arbitrary type <AType>. The order of the joined array is in the
     * identical order of the passed parameters (i.e., full prefix array first (MSB's) followed
     * by the suffix array bits.
     * @param prefixArray
     * @param suffixArray
     * @param <AType>
     * @ref System.arraycopy
     * @ref Arrays.*
     * @return The joined array (or null if the expected parameters are not sanely defined)
     */
    public static <AType> AType[] arrayJoin(AType[] prefixArray, AType[] suffixArray) {
        if(prefixArray == null || suffixArray == null)
            return null;
        else if(prefixArray.length == 0)
            return suffixArray;
        else if(suffixArray.length == 0)
            return prefixArray;
        @SuppressWarnings("unchecked")
        AType[] combinedArray = (AType[]) new Object[prefixArray.length + suffixArray.length];
        System.arraycopy(combinedArray, 0, prefixArray, 0, prefixArray.length);
        System.arraycopy(combinedArray, prefixArray.length, suffixArray, 0, suffixArray.length);
        return combinedArray;
    }

    /**
     * Determine whether an input string is in hex format and returned the clipped hex-only
     * part of the string if so (otherwise: returns null). For example,
     * "0xABCDEF" => "abcdef" and "0xABCDEFh" or "0x1234L" => "abcdef" or "1234".
     * @param str
     * @return boolean truth value: (== null) or clipped hex string
     */
    public static String getHexadecimalString(String str) {
        if(str.length() == 0)
            return "";
        else if(str.substring(0, 2) == "0x" || str.substring(0, 2) == "0X") {
            str = str.substring(2);
        }
        char lastChar = str.charAt(str.length() - 1);
        if(lastChar == 'L' || lastChar == 'l' || lastChar == 'h' || lastChar == 'H') {
            str = str.substring(0, str.length() - 1);
        }
        if(str.matches("[0-9a-fA-F]+")) {
            return str.toLowerCase();
        }
        return null;
    }

    /**
     * Returns a standard timestamp of the current Android device's time.
     * @return String timestamp (format: %Y-%m-%d-%T)
     */
    public static String getTimestamp() {
        Time currentTime = new Time();
        currentTime.setToNow();
        return currentTime.format("%Y-%m-%d-%T");
    }


}
