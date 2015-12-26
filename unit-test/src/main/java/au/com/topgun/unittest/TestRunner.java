package au.com.topgun.unittest;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.sql.DataSource;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.junit.Assert;

import au.com.topgun.common.exception.DataException;
import au.com.topgun.common.object.ColumnValuePair;
import au.com.topgun.common.object.IndexValuePair;
import au.com.topgun.common.object.MatchTracker;
import au.com.topgun.common.ref.Ref;
import au.com.topgun.common.ref.Ref.VerifyFileLineOption;
import au.com.topgun.common.util.ResultSetParser;
import au.com.topgun.common.util.ResultSetParser.VerifySQLResultSet;
import au.com.topgun.common.util.ResultSetParser.VerifySQLStruct;
import au.com.topgun.common.util.StringParser;
import au.com.topgun.common.util.ValidationUtil;

/**
 * TestRunner to unit test application
 * 
 * See defaultTestRunner.properties (for all configurations)
 * 
 */
public class TestRunner {

    public static final String INDENT = "  ";
    public final static String ENCODING = "UTF-8";
    private File propertiesFile;
    private final Properties props;
    private final TestRunnerAppBase apptoBeTested;
    private Database db;
    private Connection conn;
    private DataSource datasource;

    
    /**
     * Main method to unit test application
     * 
     * See defaultTestRunner.properties (for all configurations)
     * 
     * @param propertiesFile
     * @param apptoBeTested the java application to be tested (if equals null, the properties "RUN.javaclass" will be used)
     * @throws Exception on any error
     */
    public TestRunner(File propertiesFile, TestRunnerAppBase apptoBeTested, DataSource datasource) throws Exception {
        this.propertiesFile = propertiesFile;
        this.apptoBeTested = apptoBeTested;
        this.datasource = datasource;
        Assert.assertNotNull(this.propertiesFile);
        // Load test properties
        final InputStream is = new FileInputStream(propertiesFile);
        props = new Properties();
        props.load(is);
        is.close();
    }

    public TestRunner(Properties props, TestRunnerAppBase apptoBeTested, DataSource datasource) throws Exception {
        this.props = props;
        this.apptoBeTested = apptoBeTested;
        this.datasource = datasource;
        Assert.assertNotNull(this.props);
    }
    
    public void run() throws Exception {
        logHugeVisibleSeparator();
        log(String.format("[%s] started", this.getClass().getSimpleName()));
        init();
        BaseTestRun testRun = null;
        try {
            /**
             * Execute PRE-RUN.os.cmd in .properties
             */
            testRun = new OsCmd("PRE-RUN");
            testRun.run();
            /**
             * Execute PRE-RUN.SQL in .properties
             */
            testRun = new RunSQL("PRE-RUN.SQL");
            testRun.run();
            /**
             * Execute RUN.javaclass in .properties
             */
            testRun = new RunJavaclass(this.apptoBeTested);
            testRun.run();
            /**
             * Execute RUN.os.cmd in .properties
             */
            testRun = new OsCmd("RUN");
            testRun.run();
            /**
             * Execute VERIFY.SQL in .properties
             */
            testRun = new VerifySQL();
            testRun.run();
            /**
             * Execute VERIFY.file in .properties
             */
            testRun = new VerifyFile();
            testRun.run();
            /**
             * Execute VERIFY.javaclass in .properties
             */
            testRun = new VerifyJavaclass();
            testRun.run();
            /**
             * Execute FINAL-CLEANUP.SQL in .properties
             */
            testRun = new RunSQL("FINAL-CLEANUP.SQL");
            testRun.run();
            /**
             * Execute FINAL-CLEANUP.os.cmd in .properties
             */
            testRun = new OsCmd("FINAL-CLEANUP");
            testRun.run();
            /**
             * Execute FINAL-CLEANUP.javaclass in .properties
             */
            testRun = new FinalCleanupJavaclass();
            testRun.run();
            
            logHorizontalLine();
            log(String.format("[%s] Completed SUCCESSFULLY", this.getClass().getSimpleName()));
        } catch (Exception e) {
            logHorizontalLine();
            log(String.format(e.getMessage()));
            log(String.format("[%s] *** FAILED *** while running[%s]", this.getClass().getSimpleName(), testRun.getRunName()));
            throw e;
        } finally {
            close();
        }
    }

    protected void init() throws Exception {
        log(String.format("Working folder[%s]", getWorkingDirectory().getCanonicalPath()));
        if (propertiesFile != null) {
            log(String.format("Config  file  [%s]", propertiesFile.getCanonicalPath()));
        }
    }
    
    protected File getWorkingDirectory() {
        return new File(".");
    }
    
    /**
     * It is the caller's responsibility to close connection 
     */
    private Connection getDBConnection() throws ClassNotFoundException, SQLException {
        if (db == null) {
            db = new Database(this.datasource);
        }
        if (conn == null || conn.isClosed()) {
            conn = db.getDBConnection();
        }
        return conn;
    }
    
    private void close() {
        if (conn != null) {
            try {
                conn.close();
                conn = null;
            } catch (SQLException e) {
                // close quietly
            }
        }
        if (db != null) {
            db.close();
            db = null;
        }
    }
    
    static void log(String str) {
        System.out.println(str);
    }
    static void logHorizontalLine() {
        System.out.println("----------------------------------------");
    }
    static void logHugeVisibleSeparator() {
        System.out.println();
        System.out.println();
        System.out.println();
        System.out.println("****************************************");
        System.out.println("****************************************");
        System.out.println("****************************************");
        System.out.println();
    }

    
    /**
     * **************** Inner classes *******************************
     */
    abstract class BaseTestRun {
        static final String EMPTY_STR = "";

        private Database db;
        private Connection conn;
        protected final String CLASS_NAME = this.getClass().getSimpleName();
        
        abstract void run() throws Exception;
        abstract String getRunName();

        String getProperty(String key) {
            return props.getProperty(key);
        }

        String getProperty(String key, String defaultValue) {
            return props.getProperty(key, defaultValue);
        }

        /**
         * It is the caller's responsibility to close connection 
         */
        Connection getDBConnection() throws ClassNotFoundException, SQLException {
            return TestRunner.this.getDBConnection();
        }
    }


    class OsCmd extends BaseTestRun {
        final String propPrefix;
        OsCmd(String propPrefix) {
            this.propPrefix = propPrefix;
        }
        
        @Override
        String getRunName() {
            return String.format("%s - %s", propPrefix, this.CLASS_NAME);
        }
        
        @Override
        void run() throws DataException, ClassNotFoundException, SQLException {
            logHorizontalLine();
            log(String.format("Running[%s%s]", propPrefix, CLASS_NAME));
            final String RunOsCmdEnabledKey = String.format(String.format("%s.os.cmd.enabled", propPrefix));
            final String enabled = getProperty(RunOsCmdEnabledKey);
            if (enabled == null) {
                log(String.format("%s%s : Missing[%s]. This will not be run.", propPrefix, CLASS_NAME, RunOsCmdEnabledKey));
                return;  // means no need to run this
            }
            log(String.format("%s%s : %s = %s", propPrefix, CLASS_NAME, RunOsCmdEnabledKey, enabled));
            if (!enabled.toLowerCase().equalsIgnoreCase("true")) {
                return;
            }
            int i = ResultSetParser.BASE_IDX;
            while (true) {
                final String key = String.format(String.format("%s.os.cmd[%d]", propPrefix), i);
                final String commandLine = getProperty(key);
                if (commandLine == null) {
                    break;  // no more to do
                }
                final String line = key + " = " + commandLine;
                i++;
                try {
                    OperatingSystemUtil.runOsCmd(commandLine);
                } catch (Exception e) {
                    throw new DataException(String.format("Error executing : %s", line), e); 
                }
            }
            log(String.format("Completed[%s%s]", propPrefix, CLASS_NAME));
        }
    }
    
    
    class RunSQL extends BaseTestRun {
        final String propPrefix;
        RunSQL(String propPrefix) {
            this.propPrefix = propPrefix;
        }

        @Override
        String getRunName() {
            return String.format("%s - %s", propPrefix, this.CLASS_NAME);
        }
        
        @Override
        void run() throws DataException, ClassNotFoundException, SQLException {
            logHorizontalLine();
            log(String.format("Running[%s]", propPrefix));
            final String preRunSQLEnabledKey = String.format(String.format("%s.enabled", propPrefix));
            final String enabled = getProperty(preRunSQLEnabledKey);
            if (enabled == null) {
                log(String.format("%s : Missing[%s]. This will not be run.", propPrefix, preRunSQLEnabledKey));
                return;  // means no need to run this
            }
            log(String.format("%s : %s = %s", propPrefix, preRunSQLEnabledKey, enabled));
            if (!enabled.toLowerCase().equalsIgnoreCase("true")) {
                return;
            }
            final Connection conn = getDBConnection();
            final String defaultOptionStr = getProperty(String.format("%s.option.default", propPrefix));
            Ref.RunSQLOption.validate(defaultOptionStr);
            final Ref.RunSQLOption defaultOption = Ref.RunSQLOption.valueOf(defaultOptionStr);
            int i = ResultSetParser.BASE_IDX;
            while (true) {
                String key = null;
                String sql = null;
                Ref.RunSQLOption option = null;
                // Work out the sql option
                for (Ref.RunSQLOption tempOption : Ref.RunSQLOption.values()) {
                    /**
                     * Try to find
                     * xxx[1].option[Ref.RunSQLOption] = a sql
                     */
                    key = String.format("%s[%d].option[%s]", propPrefix, i, tempOption.name());
                    sql = getProperty(key);
                    if (sql != null) {
                        // found a match
                        sql = sql.trim();
                        if (Ref.RunSQLOption.DEFAULT == tempOption) {
                            option = defaultOption;
                        } else {
                            option = tempOption;
                        }
                        break;
                    }
                }
                if (sql == null) {
                    break;  // no more to do
                }
                final String line = key + " = " + sql;
                i++;
                try {
                    final Statement stmt = conn.createStatement();
                    stmt.execute(sql);
                    stmt.close();
                } catch (SQLException e) {
                  if (!(Ref.RunSQLOption.IGNORE_SQLEXCEPTION == option)) {
                      throw e;
                  }
                } catch (Exception e) {
                    throw new DataException(String.format("Error executing : %s", line), e); 
                }
            }
            conn.close();
            log(String.format("Completed[%s]", propPrefix));
        }
    }

    
    class RunJavaclass extends BaseTestRun {
        
        private final TestRunnerAppBase appbase;
        
        RunJavaclass(TestRunnerAppBase appbase) {
            this.appbase = appbase;
        }

        @Override
        String getRunName() {
            return CLASS_NAME;
        }
        
        @Override
        void run() throws Exception {
            logHorizontalLine();
            log(String.format("Running[%s]", CLASS_NAME));

            TestRunnerAppBase apptoBeTested = appbase;

            if (appbase == null) {
                final String runJavaclassEnabledKey = String.format("RUN.javaclass.enabled");
                final String enabled = getProperty(runJavaclassEnabledKey);
                if (enabled == null) {
                    log(String.format("%s : Missing[%s]. This will not be run.", CLASS_NAME, runJavaclassEnabledKey));
                    return;  // means no need to run this
                }
                log(String.format("%s : %s = %s", CLASS_NAME, runJavaclassEnabledKey, enabled));
                if (!enabled.toLowerCase().equalsIgnoreCase("true")) {
                    return;
                }
                apptoBeTested = null;   // FIXME: to be implemented. Use classloader to load application from "RUN.javaclass"
                log(String.format("%s : This Java class so invoked was taken from .properties", CLASS_NAME));
            } else {
                log(String.format("%s : This Java class so invoked was programmetically injected (i.e. not from .properties)", CLASS_NAME));
            }
            
            log(String.format("%s : Invoking application[%s]. This is the application we are testing against", CLASS_NAME, apptoBeTested.getClass().getName()));
            apptoBeTested.run();
            log(String.format("%s : Application finished running", CLASS_NAME));
        }
    }

    /**
     * Handles
     * 
     * VerifySQLStruct
     * VERIFY.SQL[1].enabled = true
     * VERIFY.SQL[1].select= select * from RATED_TRIP where INPUT_BATCH_FILE_NO = 'abc'
     * VERIFY.SQL[1].resultset.option=MATCH_EXACT_RECORDS | MATCH_ATLEAST_RECORDS | MATCH_ALL_RECORDS (See VerifyFileLineOption)
     * VERIFY.SQL[1].resultset[1]=TRARM_UID[abc],TIRMS_UID[1234]
     * VERIFY.SQL[1].resultset[2]=TRARM_UID[XYZ],TIRMS_UID[5678]
     * 
     * VerifySQLStruct
     * VERIFY.SQL[2].enabled = true
     * VERIFY.SQL[2].select= select * from RATED_TRIP where INPUT_BATCH_FILE_NO = 'abc' 
     * VERIFY.SQL[2].resultset.option=MATCH_EXACT_RECORDS | MATCH_ATLEAST_RECORDS | MATCH_ALL_RECORDS (See VerifyFileLineOption)
     * VERIFY.SQL[2].resultset[1]=TRARM_UID[abc],TIRMS_UID[1234],
     * VERIFY.SQL[2].resultset[2]=TRARM_UID[XYZ],TIRMS_UID[5678],
     */
    class VerifySQL extends BaseTestRun {
        VerifySQL() {
        }

        @Override
        String getRunName() {
            return CLASS_NAME;
        }
        
        @Override
        void run() throws Exception {
            logHorizontalLine();
            log(String.format("Running[%s]", CLASS_NAME));
            final List<VerifySQLStruct> testSets = buildTestSet();
            if (testSets.isEmpty()) {
                log(String.format("%s : No test cases to run", CLASS_NAME));
                return;
            }
            if (!testSets.isEmpty()) {
                final Connection conn = getDBConnection();
                for (VerifySQLStruct test : testSets) {
                    // Execute sql select
                    final Statement stmt = conn.createStatement();
                    final ResultSet rs = stmt.executeQuery(test.sqlSelect);
                    // Test result using specified option
                    if (Ref.VerifySQLResultSetOptions.MATCH_TOP_N_IN_ORDER == test.resultsetOption) {
                        ResultSetParser.checkMatchTopNInOrder(rs, test.expectedVerifyResultsets);
                    } else if (Ref.VerifySQLResultSetOptions.MATCH_ANY == test.resultsetOption) {
                        ResultSetParser.checkMatchAny(rs, test.expectedVerifyResultsets);
                    } else if (Ref.VerifySQLResultSetOptions.MATCH_EXACT == test.resultsetOption) {
                        ResultSetParser.checkMatchExact(rs, test.expectedVerifyResultsets);
                    } else if (Ref.VerifySQLResultSetOptions.MATCH_EXACT_IN_ORDER == test.resultsetOption) {
                        ResultSetParser.checkMatchExactInOrder(rs, test.expectedVerifyResultsets);
                    } else {
                        throw new DataException(String.format("No current implementation for VerifySQLResultSetOptions[%s]", test.resultsetOption));
                    }
                }
                conn.close();
            }
            log(String.format("Completed[%s]", CLASS_NAME));
        }

        List<VerifySQLStruct> buildTestSet() throws DataException {
            final List<VerifySQLStruct> result = new ArrayList<VerifySQLStruct>();
            int i = ResultSetParser.BASE_IDX;
            while (true) {
                int k = ResultSetParser.BASE_IDX;
                final String verifySQLEnabledKey = String.format("VERIFY.SQL[%d].enabled", i);
                final String enabled = getProperty(verifySQLEnabledKey);
                if (enabled == null) {
                    break; // means no more
                }
                log(String.format("%s : %s = %s", CLASS_NAME, verifySQLEnabledKey, enabled));
                if (!enabled.toLowerCase().equalsIgnoreCase("true")) {
                    i++;
                    continue;
                }
                final boolean bEnabled = Boolean.parseBoolean(enabled);
                final String select = getProperty(String.format("VERIFY.SQL[%d].select", i));
                final String optionStr = getProperty(String.format("VERIFY.SQL[%d].resultset.option", i)).trim();
                Ref.VerifySQLResultSetOptions.validate(optionStr);
                final Ref.VerifySQLResultSetOptions option = Ref.VerifySQLResultSetOptions.valueOf(optionStr);
                
                final List<VerifySQLResultSet> expectedVerifyResultsets = new ArrayList<VerifySQLResultSet>();
                final VerifySQLStruct data = new VerifySQLStruct(bEnabled, select, option, expectedVerifyResultsets);
                result.add(data);
                while (true) {
                    final String verifyResultSetKey = String.format("VERIFY.SQL[%d].resultset[%d]", i, k++);
                    final String verifyResultSet = getProperty(verifyResultSetKey);
                    if (verifyResultSet == null) {
                        break; // means no more
                    }
                    final String originalVerifyLine = verifyResultSetKey + " = " + verifyResultSet;
                    /**
                     * Decode resultset into COLUMN[value], COLUMN[value], ...
                     */
                    final List<ColumnValuePair> columnValueSet = StringParser.buildColumnValueSet(verifyResultSet);
                    final VerifySQLResultSet expectedVerifyResultset = new VerifySQLResultSet(new MatchTracker<List<ColumnValuePair>>(columnValueSet), originalVerifyLine);
                    expectedVerifyResultsets.add(expectedVerifyResultset);
                }
                i++;
            }
            return result;
        }

    }

    /**
     * Handles
     * 
     * VERIFY.file[1].enabled = true
     * VERIFY.file[1].path = .
     * VERIFY.file[1].line.option.default = MATCH_EXACT (See VerifySQLResultSetOptions)
     * VERIFY.file[1].line[1].option[MATCH_INDEX_FROM_0] = 0[03], 12[abc]
     * VERIFY.file[1].line[2].option[DEFAULT] = this line is defaulted to MATCH_EXACT
     */
    class VerifyFile extends BaseTestRun {
        VerifyFile() {
        }
        
        @Override
        String getRunName() {
            return CLASS_NAME;
        }
        
        @Override
        void run() throws DataException, IOException {
            logHorizontalLine();
            log(String.format("Running[%s]", CLASS_NAME));
            final List<VerifyFileStruct> testSets = buildTestSet();
            for (VerifyFileStruct vfs : testSets) {
                if (vfs.enabled) {
                    final String filepath = vfs.path;
                    final List<MatchTracker<VerifyFileLineStruct>> lineStructs = vfs.lineStructs;
                    final File file = new File(filepath);
                    log(String.format("Verifying File [%s]", file.getCanonicalPath()));
                    //Open the file for reading
                    final LineIterator it = FileUtils.lineIterator(file, ENCODING);
                    while (it.hasNext()) {
                        final String fileLine = it.nextLine();
                        final MatchTracker<VerifyFileLineStruct> matchTrackerLineStruct = lineStructs.get(0);
                        final VerifyFileLineStruct lineStruct = matchTrackerLineStruct.obj;
                        final VerifyFileLineOption option = lineStruct.option;
                        /**
                         * Process the line read in from file according to option
                         */
                        lineStruct.verifyLine(fileLine);
//                        final boolean verifyResult = lineStruct.verifyLine(fileLine);
//                        if (!verifyResult) {
//                            throw new DataException(String.format("Failed[%s]. Failed line : %s", option.name(), lineStruct.originalVerifyLine));
//                        }
                        //
                        lineStructs.remove(0);
                        /**
                         * Check if there is anymore to verify
                         */
                        if (lineStructs.isEmpty()) {
                            break;  // no more to verify
                        }
                    } // end while process line
                    it.close();
                    if (lineStructs.size() > 0) {
                        final StringBuilder sb = new StringBuilder();
                        for (MatchTracker<VerifyFileLineStruct> lineStruct : lineStructs) {
                            if (sb.length() > 0) {
                                sb.append("\n ");
                            }
                            sb.append(lineStruct.getObj().originalVerifyLine);
                        }
                        throw new DataException(String.format("Failed VerifyFile[%s]. The following are not processed\n %s", filepath, sb.toString()));
                    }
                }
            }
            log(String.format("Completed[%s]", CLASS_NAME));
        }

        List<VerifyFileStruct> buildTestSet() throws DataException {
            final List<VerifyFileStruct> result = new ArrayList<VerifyFileStruct>();
            int i = ResultSetParser.BASE_IDX;
            while (true) {
                int k = ResultSetParser.BASE_IDX;
                final String verifyFileEnabledKey = String.format("VERIFY.file[%d].enabled", i);
                final String enabled = getProperty(verifyFileEnabledKey);
                if (enabled == null) {
                    break; // means no more
                }
                log(String.format("%s : %s = %s", CLASS_NAME, verifyFileEnabledKey, enabled));
                if (!enabled.toLowerCase().equalsIgnoreCase("true")) {
                    i++;
                    continue;
                }
                final boolean bEnabled = Boolean.parseBoolean(enabled);
                final String path = getProperty(String.format("VERIFY.file[%d].path", i)).trim();
                final String defaultOptionStr = getProperty(String.format("VERIFY.file[%d].line.option.default", i)).trim();
                Ref.VerifyFileLineOption.validate(defaultOptionStr);
                final Ref.VerifyFileLineOption defaultOption = Ref.VerifyFileLineOption.valueOf(defaultOptionStr);
                final List<MatchTracker<VerifyFileLineStruct>> lineStructList = new ArrayList<MatchTracker<VerifyFileLineStruct>>();
                final VerifyFileStruct vfc = new VerifyFileStruct(bEnabled, path, defaultOption, lineStructList);
                result.add(vfc);
                while (true) {
                    String key = null;
                    String expectedVerifyValue = null;
                    Ref.VerifyFileLineOption option = null;
                    // Work out the line option
                    for (Ref.VerifyFileLineOption tempOption : Ref.VerifyFileLineOption.values()) {
                        /**
                         * Trying to find
                         * VERIFY.file[i].line[k].option[Ref.VerifyFileLineOption] = a line
                         */
                        key = String.format("VERIFY.file[%d].line[%d].option[%s]", i, k, tempOption.name());
                        expectedVerifyValue = getProperty(key);
                        if (expectedVerifyValue != null) {
                            // found a match
                            expectedVerifyValue = expectedVerifyValue.trim();
                            if (Ref.VerifyFileLineOption.DEFAULT == tempOption) {
                                option = defaultOption;
                            } else {
                                option = tempOption;
                            }
                            break;
                        }
                    }
                    k++;
                    if (expectedVerifyValue == null) {
                        /**
                         * When reaches here can mean two things:
                         * 1) No more lines to read (which is the expected normal case)
                         * 2) FIXME: The value of Ref.VerifyFileLineOption in .option[Ref.VerifyFileLineOption]
                         *    is not a valid one. This case is not catered for.
                         *    
                         * It is assume #2 will not happen (i.e. user will makesure the option is entered correctly.
                         */
                        break;  // assumes no more line
                    }
                    final String originalVerifyLine = key + " = " + expectedVerifyValue;
                    /**
                     * Decode line into index[value], index[value], ...
                     */
                    final VerifyFileLineStruct lineStruct = new VerifyFileLineStruct(option, expectedVerifyValue, originalVerifyLine); 
                    lineStructList.add(new MatchTracker<VerifyFileLineStruct>(lineStruct));
                }
                i++;
            }
            return result;
        }

        class VerifyFileStruct {
            public final boolean enabled;
            public final String path;
            public final Ref.VerifyFileLineOption defaultLineOption;
            public final List<MatchTracker<VerifyFileLineStruct>> lineStructs;
            public VerifyFileStruct(boolean enabled, String path, Ref.VerifyFileLineOption defaultLineOption, List<MatchTracker<VerifyFileLineStruct>> lineStructs) {
                this.enabled = enabled;
                this.path = path;
                this.defaultLineOption = defaultLineOption;
                this.lineStructs = lineStructs;
            }
        }
        
        class VerifyFileLineStruct {
            public final Ref.VerifyFileLineOption option;
            public final String expectedVerifyValue;
            public final String originalVerifyLine;    // mainly used for reporting to show the error occurs in this line
            public VerifyFileLineStruct(Ref.VerifyFileLineOption option, String expectedVerifyValue, String originalVerifyLine) {
                this.option = option;
                this.expectedVerifyValue = expectedVerifyValue;
                this.originalVerifyLine = originalVerifyLine;
            }
            public void verifyLine(String fileLine) throws DataException {
                if (VerifyFileLineOption.MATCH_EXACT == option) {
                    if (!expectedVerifyValue.equals(fileLine)) {
                        throw new DataException(String.format("Failed[%s]. Failed case : %s. \nGiven line : ( %s )", option.name(), originalVerifyLine, fileLine));
                    }
                } else if (VerifyFileLineOption.MATCH_USING_INDEX == option) {
                    /**
                     * Handles: index[value], index[value], ...
                     */
                  final List<IndexValuePair> ivpList = StringParser.buildIndexValueList(expectedVerifyValue);
                  for (IndexValuePair ivp : ivpList) {
                      if (!ValidationUtil.validate(fileLine, ivp)) {
                          throw new DataException(String.format("Failed[%s]. Failed case : %s. \nGiven line : ( %s )", option.name(), originalVerifyLine, fileLine));
                      }
                  }
                } else {
                    throw new DataException(String.format("No current implementation for VerifyFileLineOption[%s]", option.name()));
                }
            }
        }
    }
    
    class VerifyJavaclass extends BaseTestRun {
        VerifyJavaclass() {
        }
        @Override
        void run() throws DataException {
        }
        @Override
        String getRunName() {
            return String.format("%s - not implemented", this.CLASS_NAME);
        }
    }

    class FinalCleanupJavaclass extends BaseTestRun {
        FinalCleanupJavaclass() {
        }
        @Override
        void run() throws DataException {
        }
        @Override
        String getRunName() {
            return String.format("%s - not implemented", this.CLASS_NAME);
        }
    }

    /**
     * jdbc.user =
     * jdbc.password =
     * jdbc.url =
     * jdbc.driverclass =
     */
    class Database {
        
        // http://www.programcreek.com/java-api-examples/index.php?api=org.apache.derby.jdbc.EmbeddedDriver
        
        private Connection conn;
        private final String user;
        private final String password;
        private final String url;
        
        private final DataSource datasource;
        
        Database(DataSource datasource) throws ClassNotFoundException, SQLException {
            this.datasource = datasource;
            user = props.getProperty("jdbc.user");
            password = props.getProperty("jdbc.password");
            url = props.getProperty("jdbc.url");
            if (datasource == null) {
                final String driverclass = props.getProperty("jdbc.driverclass");
                log(String.format("Database : user[%s]", user));
                log(String.format("Database : password[%s]", "***********"));
                log(String.format("Database : url[%s]", url));
                log(String.format("Database : driverclass[%s]", driverclass));
                // Register JDBC Driver
                Class.forName(driverclass);
            }
        }
        Connection getDBConnection() throws SQLException {
            if (conn == null || conn.isClosed()) {
                // Open connection
                if (datasource != null) {
                    conn = datasource.getConnection();
                } else {
                    conn = DriverManager.getConnection(url, user, password);
                }
            }
            return conn;
        }
        void close() {
            if (conn != null) {
                try {
                    conn.close();
                } catch (SQLException e) {
                    // Ignore
                }
            }
        }
    }

    static class OperatingSystemUtil {
        
        public static void runOsCmd(String commandLine) throws IOException, InterruptedException, DataException {
            final StringBuilder sb = new StringBuilder();
            final String OSName = System.getProperty("os.name").toLowerCase();
            if (OSName.indexOf("windows 9") > -1) {
                // If OS is Windows 9x use command.com
                sb.append("command.com /c ").append(commandLine);
            } else if (OSName.indexOf("windows") > -1) {
                // Any other Windows OS use cmd.exe
                sb.append("cmd.exe /c ").append(commandLine);
            }

            Process proc = null;
            InputStreamReader isr = null;
            BufferedReader br = null;

            final StringBuilder cmdOutMsg = new StringBuilder();
            final StringBuilder cmdErrMsg = new StringBuilder();

            try {
                final String finalCommandToRun = sb.toString();
                proc = Runtime.getRuntime().exec(finalCommandToRun);

                int exitVal = proc.waitFor();

                // Get the output message
                isr = new InputStreamReader(proc.getInputStream());
                br = new BufferedReader(isr);
                String line = null;
                while ((line = br.readLine()) != null) {
                    cmdOutMsg.append(line);
                }

                // Get the error message
                isr = new InputStreamReader(proc.getErrorStream());
                br = new BufferedReader(isr);
                line = null;
                while ((line = br.readLine()) != null) {
                    cmdErrMsg.append(line);
                }

                final String outStr = String.format("Cmd execute output[%s]", cmdOutMsg.toString());
                final String errStr = String.format("Cmd execute error[%s]", cmdErrMsg.toString());
                if (exitVal == 0) {
                    log(outStr);
                    log(errStr);
                } else {
                    log(outStr);
                    log(errStr);
                    throw new DataException(errStr);
                }
            } finally {
                proc.destroy();
                try {
                    isr.close();
                } catch (IOException e) {
                    // die quietly
                }
                try {
                    br.close();
                } catch (IOException e) {
                    // die quietly
                }
            }
        }
    }
}
