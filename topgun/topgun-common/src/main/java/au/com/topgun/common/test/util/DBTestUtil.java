package au.com.topgun.common.test.util;



/**
 * A base test class to mock EJB, Struts, DataSource (connects to real database)
 * 
 * Usage:
 * class YourTestClass extends DBTestUtil {
 *      YourTestClass() {
 *          super();
 *          ...
 *      }
 * 
 *      @After
 *      public void tearDown() {
 *          super.close();
 *      }
 * 
 *      @Test
 *      public void test01() throws Exception {
 *          ...
 *      }
 * }
 *  
 */
public class DBTestUtil {
    
//public class DBTestUtil extends BaseTestCase {
//
//    /**
//     * Create JDBC and EJB mock modules
//     */
//    private EJBTestModule ejbModule;
//
//    public DBTestUtil() throws Exception {
//        super();
//        setUp();
//    }
//
//    /**
//     * Call this when you finish this instance to cleanup
//     */
//    protected void close() {
//        
//    }
//    
//    protected void setUp() throws Exception {
//        
//        /**
//         * Create the EJB Container
//         */
//        ejbModule = createEJBTestModule();
//
//        // Quick test
//        final Context ctx = new InitialContext();
//        Assert.assertNotNull(ctx);
//    }
//
//    /**
//     * Bind the datasource
//     * 
//     * To retrieve datasource after this binding operation:
//     *      final Context ctx = new InitialContext();
//     *      final DataSource dataSource = (DataSource) ctx.lookup(jndiName);
//     *      Assert.assertNotNull(dataSource);
//     * 
//     * @param jndiName
//     * @param dbConfig
//     * @throws SQLException
//     */
//    public void bindDataSource(String jndiName, DataSource datasource) throws SQLException {
//        ejbModule.bindToContext(jndiName, datasource);
//    }
//    
////    public void bindOracleDataSource(String jndiName, DBConfiguration dbConfig) throws SQLException {
////        final String dbUrl = dbConfig.getUrl();
////        final String dbUsername = dbConfig.getUsername();
////        final String dbPassword = dbConfig.getPassword();
////        final OracleDataSource ds = new OracleDataSource();
////        ds.setURL(dbUrl);
////        ds.setUser(dbUsername);
////        ds.setPassword(dbPassword);
////        bindDataSource(jndiName, ds);
////    }
//    
//    public void deploySessionEJB(String ejbName, Class ejbLocalHome, Class ejbLocalObject, Class ejbSessionBean) {
//        /**
//         * Inject EJB(s) into mock ejb module
//         */
//        /*
//         final BasicEjbDescriptor sessionBeanDescriptor = new SessionBeanDescriptor(
//                JNDI_TAG_RULES_BEAN, 
//                TagRulesLocalHome.class, 
//                TagRulesLocal.class, 
//                TagRulesBean.class);
//        ejbModule.deploy(sessionBeanDescriptor);
//        */
//        final BasicEjbDescriptor sessionBeanDescriptor = new SessionBeanDescriptor(
//                ejbName, 
//                ejbLocalHome.getClass(), 
//                ejbLocalObject.getClass(), 
//                ejbSessionBean.getClass());
//        ejbModule.deploy(sessionBeanDescriptor);
//    }
//    
//    /**
//     * ***********************************************************************
//     * Inner class
//     * ***********************************************************************
//     */
//    public static class DBConfiguration {
//        /**
//         * Example
//         * dbUrl = "jdbc:oracle:thin:@ONE7-FAL131155.au.fjanz.com:1521:RMS11G";
//         * dbUsername = "tirms";
//         * dbPassword = "TIRMS";
//         * dbDriverClass = "oracle.jdbc.OracleDriver";
//         */
//        private String dbUrl = "jdbc:oracle:thin:@ONE7-FAL131155.au.fjanz.com:1521:RMS11G";
//        private String dbUsername = "tirms";
//        private String dbPassword = "TIRMS";
//        private String dbDriverClass = "oracle.jdbc.OracleDriver";
//        
//        public DBConfiguration(String dbUrl, String dbDriverClass, String dbUsername, String dbPassword) {
//            this.dbUrl = dbUrl;
//            this.dbDriverClass = dbDriverClass;
//            this.dbUsername = dbUsername;
//            this.dbPassword = dbPassword;
//        }
//        
//        public String getUrl() {
//            return dbUrl;
//        }
//        public String getUsername() {
//            return dbUsername;
//        }
//        public String getPassword() {
//            return dbPassword;
//        }
//        public String getDriverClass() {
//            return dbDriverClass;
//        }
//    }
    
}
