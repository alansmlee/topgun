package au.com.topgun.unittest;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;

import junit.framework.Assert;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.junit.After;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;

import au.com.topgun.common.exception.DataException;

@RunWith(Arquillian.class)
public class TestRunnerJUnit {

    final static String databaseFolder = "./generated/db";
    final static String databaseName = "unit-test-DB";
    final static String user = "topgun";   // this is same as database schema
    final static String password = "password";
    final static String jdbcDriverClass = "org.apache.derby.jdbc.ClientDriver";
    private static EmbeddedDataSource dataSource = null;

    @Deployment
    public static JavaArchive createDeployment() {
        return ShrinkWrap.create(JavaArchive.class);
    }
    
    @BeforeClass
    public static void setup() throws SQLException {
        /**
         * ---- Setup database ----
         */
        System.setProperty("derby.system.home", databaseFolder);
        final File db = new File(databaseFolder);
        db.mkdirs();
        dataSource = new EmbeddedDataSource();
        dataSource.setCreateDatabase("create");
        dataSource.setDatabaseName(databaseName);
        final Connection conn = dataSource.getConnection();
        Assert.assertNotNull(conn);
        conn.close();
    }

    @After
    public void tearDown() {
    }

    protected String getTestDataFolder() throws IOException {
        final File projFolder = new File(".");
        final String testdataFolder = projFolder.getCanonicalPath() + "/src/test/resources/testdata";
        return testdataFolder;
    }

    protected void log(String str) {
        System.out.println(str);
    }

    @Test
    public void testDefaultTestRunner() throws Exception {
        /**
         * All tests should pass with defaultTestRunner.properties
         */
        log(String.format("Unit test : %s.%s", this.getClass().getSimpleName(), "testDefaultTestRunner"));
        final String TEST_RUNNER_PROPERTIES_FILENAME = "defaultTestRunner.properties";
        testRunnerAdaptor(TEST_RUNNER_PROPERTIES_FILENAME);
    }

    @Test (expected = DataException.class)
    public void testVerifySQL_bad_MatchOption() throws Exception {
        log(String.format("Unit test : %s.%s", this.getClass().getSimpleName(), "testVerifySQL_bad_MatchOption"));
        final String TEST_RUNNER_PROPERTIES_FILENAME = "VerifySQL_bad_MatchOption.properties";
        testRunnerAdaptor(TEST_RUNNER_PROPERTIES_FILENAME);
    }

    @Test (expected = SQLException.class)
    public void testVerifySQL_bad_SQLStatement() throws Exception {
        log(String.format("Unit test : %s.%s", this.getClass().getSimpleName(), "testVerifySQL_bad_SQLStatement"));
        final String TEST_RUNNER_PROPERTIES_FILENAME = "VerifySQL_bad_SQLStatement.properties";
        testRunnerAdaptor(TEST_RUNNER_PROPERTIES_FILENAME);
    }

    @Test (expected = DataException.class)
    public void testVerifySQL_bad_MATCH_ANY() throws Exception {
        log(String.format("Unit test : %s.%s", this.getClass().getSimpleName(), "testVerifySQL_bad_MATCH_ANY"));
        final String TEST_RUNNER_PROPERTIES_FILENAME = "VerifySQL_bad_MATCH_ANY.properties";
        testRunnerAdaptor(TEST_RUNNER_PROPERTIES_FILENAME);
    }

    @Test (expected = DataException.class)
    public void testVerifySQL_bad_MATCH_EXACT() throws Exception {
        log(String.format("Unit test : %s.%s", this.getClass().getSimpleName(), "testVerifySQL_bad_MATCH_EXACT"));
        final String TEST_RUNNER_PROPERTIES_FILENAME = "VerifySQL_bad_MATCH_EXACT.properties";
        testRunnerAdaptor(TEST_RUNNER_PROPERTIES_FILENAME);
    }

    @Test (expected = DataException.class)
    public void testVerifySQL_bad_MATCH_EXACT_tooFewCases() throws Exception {
        log(String.format("Unit test : %s.%s", this.getClass().getSimpleName(), "testVerifySQL_bad_MATCH_EXACT_tooFewCases"));
        final String TEST_RUNNER_PROPERTIES_FILENAME = "VerifySQL_bad_MATCH_EXACT_tooFewCases.properties";
        testRunnerAdaptor(TEST_RUNNER_PROPERTIES_FILENAME);
    }

    @Test (expected = DataException.class)
    public void testVerifySQL_bad_MATCH_EXACT_tooManyCases() throws Exception {
        log(String.format("Unit test : %s.%s", this.getClass().getSimpleName(), "testVerifySQL_bad_MATCH_EXACT_tooManyCases"));
        final String TEST_RUNNER_PROPERTIES_FILENAME = "VerifySQL_bad_MATCH_EXACT_tooManyCases.properties";
        testRunnerAdaptor(TEST_RUNNER_PROPERTIES_FILENAME);
    }

    @Test (expected = DataException.class)
    public void testVerifySQL_bad_MATCH_EXACT_IN_ORDER() throws Exception {
        log(String.format("Unit test : %s.%s", this.getClass().getSimpleName(), "testVerifySQL_bad_MATCH_EXACT_IN_ORDER"));
        final String TEST_RUNNER_PROPERTIES_FILENAME = "VerifySQL_bad_MATCH_EXACT_IN_ORDER.properties";
        testRunnerAdaptor(TEST_RUNNER_PROPERTIES_FILENAME);
    }

    @Test (expected = DataException.class)
    public void testVerifySQL_bad_MATCH_EXACT_IN_ORDER_tooManyCases() throws Exception {
        log(String.format("Unit test : %s.%s", this.getClass().getSimpleName(), "testVerifySQL_bad_MATCH_EXACT_IN_ORDER_tooManyCases"));
        final String TEST_RUNNER_PROPERTIES_FILENAME = "VerifySQL_bad_MATCH_EXACT_IN_ORDER_tooManyCases.properties";
        testRunnerAdaptor(TEST_RUNNER_PROPERTIES_FILENAME);
    }

    @Test (expected = DataException.class)
    public void testVerifySQL_bad_MATCH_EXACT_IN_ORDER_tooFewCases() throws Exception {
        log(String.format("Unit test : %s.%s", this.getClass().getSimpleName(), "testVerifySQL_bad_MATCH_EXACT_IN_ORDER_tooFewCases"));
        final String TEST_RUNNER_PROPERTIES_FILENAME = "VerifySQL_bad_MATCH_EXACT_IN_ORDER_tooFewCases.properties";
        testRunnerAdaptor(TEST_RUNNER_PROPERTIES_FILENAME);
    }

    @Test (expected = DataException.class)
    public void testVerifySQL_bad_MATCH_TOP_N_IN_ORDER() throws Exception {
        log(String.format("Unit test : %s.%s", this.getClass().getSimpleName(), "testVerifySQL_bad_MATCH_TOP_N_IN_ORDER"));
        final String TEST_RUNNER_PROPERTIES_FILENAME = "VerifySQL_bad_MATCH_TOP_N_IN_ORDER.properties";
        testRunnerAdaptor(TEST_RUNNER_PROPERTIES_FILENAME);
    }

    @Test (expected = DataException.class)
    public void testVerifySQL_bad_MATCH_TOP_N_IN_ORDER_tooManagCases() throws Exception {
        log(String.format("Unit test : %s.%s", this.getClass().getSimpleName(), "testVerifySQL_bad_MATCH_TOP_N_IN_ORDER_tooManyCases"));
        final String TEST_RUNNER_PROPERTIES_FILENAME = "VerifySQL_bad_MATCH_TOP_N_IN_ORDER_tooManyCases.properties";
        testRunnerAdaptor(TEST_RUNNER_PROPERTIES_FILENAME);
    }

    @Test (expected = DataException.class)
    public void testVerifyFile_bad_MATCH_EXACT() throws Exception {
        log(String.format("Unit test : %s.%s", this.getClass().getSimpleName(), "testVerifyFile_bad_MATCH_EXACT"));
        final String TEST_RUNNER_PROPERTIES_FILENAME = "VerifyFile_bad_MATCH_EXACT.properties";
        testRunnerAdaptor(TEST_RUNNER_PROPERTIES_FILENAME);
    }

    @Test (expected = DataException.class)
    public void testVerifyFile_bad_MATCH_USING_INDEX() throws Exception {
        log(String.format("Unit test : %s.%s", this.getClass().getSimpleName(), "testVerifyFile_bad_MATCH_USING_INDEX"));
        final String TEST_RUNNER_PROPERTIES_FILENAME = "VerifyFile_bad_MATCH_USING_INDEX.properties";
        testRunnerAdaptor(TEST_RUNNER_PROPERTIES_FILENAME);
    }

    @Test (expected = DataException.class)
    public void testVerifyFile_bad_MATCH_USING_INDEX_outOfRange() throws Exception {
        log(String.format("Unit test : %s.%s", this.getClass().getSimpleName(), "testVerifyFile_bad_MATCH_USING_INDEX_outOfRange"));
        final String TEST_RUNNER_PROPERTIES_FILENAME = "VerifyFile_bad_MATCH_USING_INDEX_outOfRange.properties";
        testRunnerAdaptor(TEST_RUNNER_PROPERTIES_FILENAME);
    }

    protected void testRunnerAdaptor(String testRunnerPropertiesFilename) throws Exception {
        final String propertiesFilepath = getTestDataFolder() + "/" + testRunnerPropertiesFilename; 
        // Inject a mock javaclass for demonstration purposes only
        final TestRunnerAppBase mockAppToBeTested = new TestRunnerAppBase() {
            public void run() throws Exception {
                log("This is a DO NOTHING application for demonistration only.");
            }
        };
        // Run the tests
        final File propertiesFile = new File(propertiesFilepath);
        final TestRunner tr = new TestRunner(propertiesFile, mockAppToBeTested, dataSource);
        tr.run();
    }

}
