package au.com.topgun.common.util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import au.com.topgun.common.object.ColumnValuePair;
import au.com.topgun.common.object.IndexValuePair;
import au.com.topgun.common.object.KeyValuePair;

public class StringParser {

    /**
     * @param str = "key[value]";
     * @return KeyValuePair<String key, String value>
     */
    public static KeyValuePair<String,String> parserKeyValue(final String str) {
        final String _str = str.trim();
        final int openBraceIdx = _str.indexOf('[');
        final String key = _str.substring(0, openBraceIdx);
        final int closeBraceIdx = _str.indexOf(']');
        final String value = _str.substring(openBraceIdx + 1, closeBraceIdx);
        final KeyValuePair<String,String> result = new KeyValuePair<String,String>(key.trim(), value);
        return result;
    }
    
    /**
     * @param str = "index[value]";
     * @return IndexValuePair<Index key, String value>
     */
    public static IndexValuePair parserIndexValuePair(final String str) {
        final String _str = str.trim();
        final int openBraceIdx = _str.indexOf('[');
        final String index = _str.substring(0, openBraceIdx);
        final int closeBraceIdx = _str.indexOf(']');
        final String value = _str.substring(openBraceIdx + 1, closeBraceIdx);
        final IndexValuePair result = new IndexValuePair(Integer.parseInt(index.trim()), value);
        return result;
    }
    
    /**
     * str = col_01[value_01], col_02[value_02], ...
     */
    public static List<ColumnValuePair> buildColumnValueSet(String str) {
        final List<ColumnValuePair> set = new ArrayList<ColumnValuePair>();
        final String[] columnValueTokens = str.split(",");
        for (String columnValueToken : columnValueTokens) {
            final KeyValuePair<String, String> pair = StringParser.parserKeyValue(columnValueToken.trim());
            final ColumnValuePair cv = new ColumnValuePair(pair.getKey(), pair.getValue());
            set.add(cv);
        }
        return set;
    }
    
    /**
     * str = index_01[value_01], index_02[value_02], ...
     */
    public static List<IndexValuePair> buildIndexValueList(String str) {
        final List<IndexValuePair> list = new ArrayList<IndexValuePair>();
        final String[] tokens = str.split(",");
        for (String token : tokens) {
            final IndexValuePair pair = StringParser.parserIndexValuePair(token.trim());
            list.add(pair);
        }
        return list;
    }
    
}
