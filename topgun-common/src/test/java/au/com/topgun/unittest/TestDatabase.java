package au.com.topgun.unittest;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import junit.framework.Assert;

import org.apache.derby.jdbc.EmbeddedDataSource;
import org.junit.Before;
import org.junit.Test;

import au.com.topgun.common.database.DBConnection;

public class TestDatabase {

    final String databaseFolder = "generated/db";
    final String databaseName = "unit-test-DB";
    final String user = "topgun";   // this is same as database schema
    final String password = "password";
    final String jdbcDriverClass = "org.apache.derby.jdbc.ClientDriver";
    private EmbeddedDataSource dataSource = null;
    
    @Before
    public void setup() throws SQLException {
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
    
    @Test
    public void test() {
        
    }
    
//    @Test
    public void testDerbyFileDatabase() throws SQLException, ClassNotFoundException, IOException {
        /**
         * http://www.javased.com/index.php?api=org.apache.derby.jdbc.EmbeddedDataSource
         */
        Connection conn = dataSource.getConnection();
        Assert.assertNotNull(conn);
        
        final String TABLE_NAME = "UNIT_TEST";
        final String COL_ITEM_NO = "ITEM_NO";
        final String COL_NAME = "NAME";
        final String COL_PRICE = "PRICE";
        
        // Drop table
        Statement stmt = conn.createStatement();
        final String drop_table_sql = String.format("DROP TABLE %s.%s", user, TABLE_NAME);
        try {
            stmt.execute(drop_table_sql);
        } catch (SQLException e) {
            if(!e.getSQLState().equals("42Y55") && !e.getSQLState().equals("42Y07")) {
                throw e;
            }
        } finally {
            stmt.close();
        }
        
        // Create TABLE
        stmt = conn.createStatement();
        try {
            final String create_table_sql = String.format("CREATE TABLE %s.%s ("
                    + "%s INT, "
                    + "%s VARCHAR(32), "
                    + "%s DOUBLE PRECISION)",
                    user, TABLE_NAME, COL_ITEM_NO, COL_NAME, COL_PRICE); 
            stmt.execute(create_table_sql);
            System.out.println("Created table");
        } catch (SQLException e) {
            if(!e.getSQLState().equals("X0Y32")) {
                throw e; // table does not exists
            }
            // Here means table exists, so just quietly ignore exception
        } finally {
            stmt.close();
        }
        
        // Insert record
        final int item_no_01 = 1;
        final String name_01 = "Ice Cream";
        final float price_01 = 4.5f;
        
        final String insert_record_sql_01 = String.format("INSERT INTO %s.%s VALUES (%d, '%s', %.2f)", user, TABLE_NAME, item_no_01, name_01, price_01);
        stmt = conn.createStatement();
        stmt.execute(insert_record_sql_01);
        System.out.println("Inserted record");
        stmt.close();
        
        final int item_no_02 = 2;
        final String name_02 = "Water Melon";
        final float price_02 = 5.65f;
        
        final String insert_record_sql_02 = String.format("INSERT INTO %s.%s VALUES (%d, '%s', %.2f)", user, TABLE_NAME, item_no_02, name_02, price_02);
        stmt = conn.createStatement();
        stmt.execute(insert_record_sql_02);
        System.out.println("Inserted record");
        stmt.close();
        
        // Select record
        final String select_record_sql = String.format("SELECT * FROM %s.%s ", user, TABLE_NAME, item_no_01, name_01, price_01);
        stmt = conn.createStatement();
        final ResultSet rs = stmt.executeQuery(select_record_sql);
        System.out.println("Selecting record(s)");
        while (rs.next()) {
            final String item = rs.getString(COL_ITEM_NO);
            Assert.assertNotNull(item);
            final String name = rs.getString(COL_NAME);
            Assert.assertNotNull(name);
            final String price = rs.getString(COL_PRICE);
            Assert.assertNotNull(price);
            System.out.println(String.format("Table[%s] -> ITEM[%s], NAME[%s], PRICE[%s]", TABLE_NAME, item, name, price));
        }
        stmt.close();
        conn.close();
        System.out.println("Finished database setup ...");
        
        /**
         * ---- Access database via jdbc ----
         */
        {
            /**
             * If a database is created in Derby using the embedded driver and no user name is specified, the default schema used becomes APP. Therefore any tables created in the database have a schema name of APP. However, when creating a Derby database using the Network Server, the value for the schema becomes the value of the username used to connect with as part of the database URL. In our example we first created the myDB database using the user me.
             * When we change the application to connect using the embedded driver, the schema will default to APP unless we explicitly specify a schema, or pass the username as part of the Database connection URL. To access the table without passing the username as part of the embedded driver Database URL we would refer to the table as ME.restaurants.
             */
            System.out.println("Attempting to access database via JDBC");

            final File projFolder = new File(".");
            final String databaseFullpath = projFolder.getCanonicalPath() + "/" + databaseFolder;
            
//          final String dbURL = "jdbc:derby://localhost:1527/myDB;create=true;user=me;password=mine";
//            final String dbURL = String.format("jdbc:derby:%s/%s;user=%s;password=%s", databaseFullpath, databaseName, user, password);
            final String dbURL = String.format("jdbc:derby:%s/%s", databaseFullpath, databaseName);
            
            final DBConnection dbConn = new DBConnection(jdbcDriverClass, dbURL, user, password);
            Assert.assertNotNull(dbConn);
            final Connection conn2 = dbConn.getDBConnection();
            Assert.assertNotNull(conn2);

            // Select record
            final Statement stmt2 = conn2.createStatement();
            System.out.println("Selecting records");
            final ResultSet rs2 = stmt2.executeQuery(select_record_sql);
            while (rs2.next()) {
                final String item = rs2.getString(COL_ITEM_NO);
                Assert.assertNotNull(item);
                final String name = rs2.getString(COL_NAME);
                Assert.assertNotNull(name);
                final String price = rs2.getString(COL_PRICE);
                Assert.assertNotNull(price);
                System.out.println(String.format("Table[%s] -> ITEM[%s], NAME[%s], PRICE[%s]", TABLE_NAME, item, name, price));
            }
            stmt2.close();
            conn2.close();
            System.out.println("Finished database test");
        }
    }
}
