package au.com.topgun.common.object;

/**
 * Cater for database Column, Value pair 
 */
public class ColumnValuePair extends KeyValuePair<String, String> {
    public ColumnValuePair(String columnName, String value) {
        super(columnName, value);
    }
    public String getColumn() {
        return this.getKey();
    }
}

//public class ColumnValue {
//    final public String columnName;
//    final public String value;
//    public ColumnValue(String columnName, String value) {
//        this.columnName = columnName;
//        this.value = value;
//    }
//}
