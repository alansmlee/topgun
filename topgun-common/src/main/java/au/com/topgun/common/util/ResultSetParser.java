package au.com.topgun.common.util;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import au.com.topgun.common.constant.Constant;
import au.com.topgun.common.exception.DataException;
import au.com.topgun.common.object.ColumnValuePair;
import au.com.topgun.common.object.MatchTracker;
import au.com.topgun.common.ref.Ref;

public class ResultSetParser {

    public static final int BASE_IDX = 1;

    public static String currentResultsetToString(ResultSet rs) throws SQLException {
        // get result set meta data
        final ResultSetMetaData rsMetaData = rs.getMetaData();
        final int numberOfColumns = rsMetaData.getColumnCount();
        String tableName = "";
        String schemaName = "";

        final StringBuilder sb = new StringBuilder();
        // get the column names; column indexes start from 1
        for (int i = 1; i < numberOfColumns + 1; i++) {
          final String columnName = rsMetaData.getColumnName(i);
          tableName = rsMetaData.getTableName(i);
          schemaName = rsMetaData.getSchemaName(i);
          if (sb.length() != 0) {
              sb.append(", ");
          }
          sb.append(columnName + "[" + rs.getString(i)+ "]");
        }
        sb.insert(0, String.format("Table[%s.%s] -> ", schemaName, tableName));
        return sb.toString();
    }
    
    /**
     * @param rs
     * @param set
     * @return true if all column values in set matches corresponding rs column values 
     * @throws SQLException
     */
    public static boolean checkMatchedAllColumns(ResultSet rs, List<ColumnValuePair> set) throws SQLException {
        for (ColumnValuePair cv : set) {
            // Must test every ColumnValue in this set for ALL MATCH
            final String dbValue = rs.getString(cv.getColumn());
            if (!cv.getValue().equals(dbValue)) {
                return false;
            }
        }
        return true;
    }
    
    public static void checkMatchExact(ResultSet rs, List<VerifySQLResultSet> list) throws SQLException, DataException {
        final StringBuilder sb = new StringBuilder();
        while (rs.next()) {
            // Given a given resultset, iterate through all tests
            boolean isMatched = false;
            for (VerifySQLResultSet expected : list) {
                final MatchTracker<List<ColumnValuePair>> colValSet = expected.expectedResultset;
                try {
                    if (colValSet.isMatched()) {
                        continue;   // this one is already done
                    }
                    if (ResultSetParser.checkMatchedAllColumns(rs, colValSet.getObj())) {
                        colValSet.setMatched();
                        isMatched = true;
                        break; // ensure only one match per record
                    }
                } catch (SQLException e) {
                    final String err = String.format("Error[%s] in line [%s]", e.getMessage(), expected.originalVerifyLine);
                    throw new DataException(err, e);
                }
            }
            if (!isMatched) {
                final String notCateredResultSet = currentResultsetToString(rs);
                sb.append(notCateredResultSet).append("\n");
            }
        }
        
        final StringBuilder sbErr = new StringBuilder();
        for (VerifySQLResultSet expected : list) {
            final MatchTracker<List<ColumnValuePair>> colValSet = expected.expectedResultset;
            if (!colValSet.isMatched()) {
                sbErr.append(Constant.DEFAULT_INDENT).append(expected.originalVerifyLine).append("\n");
            }
        }
        if (sbErr.length() > 0) {
            throw new DataException(String.format("Failed[%s]. Following cases are not matched:\n%s", Ref.VerifySQLResultSetOptions.MATCH_EXACT.name(), sbErr.toString()));
        }
        
        if (sb.length() > 0) {
            throw new DataException(String.format("Failed[%s]. Following ResultSet are not catered:\n%s", Ref.VerifySQLResultSetOptions.MATCH_EXACT.name(), sb.toString()));
        }
    }
    
    public static void checkMatchExactInOrder(ResultSet rs, List<VerifySQLResultSet> list) throws SQLException, DataException {
        while (rs.next()) {
            // Given a record, iterate through all sets
            final VerifySQLResultSet expected = list.get(0);
            final MatchTracker<List<ColumnValuePair>> colValSet = expected.expectedResultset;

            boolean bCheck;
            try {
                bCheck = ResultSetParser.checkMatchedAllColumns(rs, colValSet.getObj());
            } catch (SQLException e) {
                final String err = String.format("Error[%s] in line [%s]", e.getMessage(), expected.originalVerifyLine);
                throw new DataException(err, e);
            }
            if (!bCheck) {
                throw new DataException(String.format("Failed[%s]. Failed line : %s", Ref.VerifySQLResultSetOptions.MATCH_EXACT_IN_ORDER.name(), expected.originalVerifyLine));
            }
            colValSet.setMatched();
            list.remove(0);
            if (list.isEmpty()) {
                break;  // no more to process
            }
        }
        // For EXACT MATCH, we also need to make sure there is no more Result Set left
        if (!list.isEmpty()) {
            throw new DataException(String.format("Failed[%s]. Failed to match following :\n%s", Ref.VerifySQLResultSetOptions.MATCH_EXACT_IN_ORDER.name(), toString(list)));
        }
        if (rs.next()) {
            final StringBuilder sb = new StringBuilder();
            sb.append(Constant.DEFAULT_INDENT).append(currentResultsetToString(rs)).append("\n");
            while (rs.next()) {
                sb.append(Constant.DEFAULT_INDENT).append(currentResultsetToString(rs)).append("\n");
            }
            throw new DataException(String.format("Failed[%s]. Following Resultset are not matched\n%s", Ref.VerifySQLResultSetOptions.MATCH_EXACT_IN_ORDER.name(), sb.toString()));
        }
    }
    
    public static void checkMatchAny(ResultSet rs, List<VerifySQLResultSet> list) throws SQLException, DataException {
        while (rs.next()) {
            for (VerifySQLResultSet expected : list) {
                final MatchTracker<List<ColumnValuePair>> colValSet = expected.expectedResultset;
                try {
                    if (ResultSetParser.checkMatchedAllColumns(rs, colValSet.getObj())) {
                        return; // matched one record
                    }
                } catch (SQLException e) {
                    final String err = String.format("Error[%s] in line [%s]", e.getMessage(), expected.originalVerifyLine);
                    throw new DataException(err, e);
                }
            }
        }
        throw new DataException(String.format("Failed[%s]. Failed to match any of the following :\n%s", Ref.VerifySQLResultSetOptions.MATCH_ANY.name(), toString(list)));
    }

    public static void checkMatchTopNInOrder(ResultSet rs, List<VerifySQLResultSet> list) throws DataException, SQLException {
        while (rs.next()) {
            // Given a record, iterate through all sets
            final VerifySQLResultSet expected = list.get(0);
            final MatchTracker<List<ColumnValuePair>> colValSet = expected.expectedResultset;
            
            boolean bCheck;
            try {
                bCheck = ResultSetParser.checkMatchedAllColumns(rs, colValSet.getObj());
            } catch (SQLException e) {
                final String err = String.format("Error[%s] in line [%s]", e.getMessage(), expected.originalVerifyLine);
                throw new DataException(err, e);
            }
            if (!bCheck) {
                throw new DataException(String.format("Failed[%s]. Failed line : %s", Ref.VerifySQLResultSetOptions.MATCH_TOP_N_IN_ORDER.name(), expected.originalVerifyLine));
            }
            colValSet.setMatched();
            list.remove(0);
            if (list.isEmpty()) {
                break;  // no more to process
            }
        }
        if (!list.isEmpty()) {
            throw new DataException(String.format("Failed[%s]. Failed to match following :\n%s", Ref.VerifySQLResultSetOptions.MATCH_TOP_N_IN_ORDER.name(), toString(list)));
        }
    }

    protected static String toString(List<VerifySQLResultSet> list) {
        final StringBuilder sb = new StringBuilder();
        for (VerifySQLResultSet expected : list) {
            final MatchTracker<List<ColumnValuePair>> colValSet = expected.expectedResultset;
            if (!colValSet.isMatched()) {
                if (sb.length() == 0) {
                    sb.append("\n ");
                }
                sb.append(Constant.DEFAULT_INDENT).append(expected.originalVerifyLine);
            }
        }
        return sb.toString();
    }
    
//    protected static String toString(ResultSet rs, List<ColumnValuePair> expectedSet) {
//        final StringBuilder sb = new StringBuilder();
//        final List<String> colNameList = new ArrayList<String>();
//        final List<String> expectedList = new ArrayList<String>();
//        final List<String> actualList = new ArrayList<String>();
//        for (ColumnValuePair cv : expectedSet) {
//            try {
//                final String dbValue = rs.getString(cv.getColumn());
//                if (!cv.getValue().equals(dbValue)) {
//                    colNameList.add(cv.getColumn());
//                    expectedList.add(cv.getValue());
//                    actualList.add(dbValue);
//                }
//            } catch (SQLException e) {
//                colNameList.add(cv.getColumn());
//                expectedList.add(cv.getValue());
//                actualList.add(e.getMessage());
//            }
//        }
//        // FIXME: to be continued to show in parallel ...
//    }

    
    /**
     * *************************************************************
     * Inner classes 
     * *************************************************************
     */
    public static class VerifySQLStruct {
        public final boolean enabled;
        public final String sqlSelect;
        public final Ref.VerifySQLResultSetOptions resultsetOption;
        public final List<VerifySQLResultSet> expectedVerifyResultsets;
        public VerifySQLStruct(boolean enabled, String sqlSelect, Ref.VerifySQLResultSetOptions resultsetOption,
                
                List<VerifySQLResultSet> expectedVerifyResultsets) {
            this.enabled = enabled;
            this.sqlSelect = sqlSelect;
            this.resultsetOption = resultsetOption;
            this.expectedVerifyResultsets = expectedVerifyResultsets;
        }
    }
    
    public static class VerifySQLResultSet {
        public final MatchTracker<List<ColumnValuePair>> expectedResultset;
        public final String originalVerifyLine;    // mainly used for reporting to show the error occurs in this line
        public VerifySQLResultSet(MatchTracker<List<ColumnValuePair>> expectedResultset, String originalVerifyLine) {
            this.expectedResultset = expectedResultset;
            this.originalVerifyLine = originalVerifyLine;
        }
    }
    
}
