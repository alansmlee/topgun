package au.com.topgun.common.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    // http://www.programcreek.com/java-api-examples/index.php?api=org.apache.derby.jdbc.EmbeddedDriver

    private Connection conn;
    private final String user;
    private final String password;
    private final String url;
    private final String jdbcDriverClass;

    public DBConnection(String jdbcDriverClass, String url, String user, String password) {
        this.user = user;
        this.password = password;
        this.url = url;
        this.jdbcDriverClass = jdbcDriverClass;
    }

    public Connection getDBConnection() throws SQLException, ClassNotFoundException {
        if (conn == null) {
            // Register JDBC Driver
            Class.forName(jdbcDriverClass);
            // Open connection
            conn = DriverManager.getConnection(url, user, password);
        }
        return conn;
    }

    public void close() {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                // Ignore
            }
        }
    }
}
