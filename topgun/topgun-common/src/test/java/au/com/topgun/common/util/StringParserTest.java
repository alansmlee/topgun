package au.com.topgun.common.util;

import junit.framework.Assert;

import org.junit.Test;

import au.com.topgun.common.object.KeyValuePair;

public class StringParserTest {

    @Test
    public void testStringParser()  {
        final String str = "abc123[xyz789]";
        final KeyValuePair<String, String> result = StringParser.parserKeyValue(str);
        Assert.assertEquals("abc123", result.getKey());
        Assert.assertEquals("xyz789", result.getValue());
    }
}
