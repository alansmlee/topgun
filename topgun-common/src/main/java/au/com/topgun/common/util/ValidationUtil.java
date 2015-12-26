package au.com.topgun.common.util;

import au.com.topgun.common.object.IndexValuePair;

public class ValidationUtil {

    public static boolean validate(String line, IndexValuePair ivp) {
        final int idx = ivp.getIndex();
        final String expectedValue = ivp.getValue();
        final int endPos = idx + expectedValue.length();
        if (endPos > line.length()) {
            return false;   // out of range
        }
        final String extractedStr = line.substring(idx, endPos);
        return expectedValue.equals(extractedStr);
    }
}
