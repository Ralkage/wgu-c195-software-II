package utils;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * The DB Connection utility class.
 *
 * @author Christian Lopez
 * Software II - C195
 */
public class DBConnection {
    private static final String protocol = "jdbc";
    private static final String vendorName = ":mysql:";
    private static final String ipAddress = "//wgudb.ucertify.com:3306/";
    private static final String dbName = "WJ04Vxv";

    private static final String jdbcUrl = protocol + vendorName + ipAddress + dbName;

    private static final String MYSQLJDBCDriver = "com.mysql.cj.jdbc.Driver";

    private static final String username = "U04Vxv";
    private static Connection conn = null;

    /**
     * Start the DB connection.
     */
    public static void startConnection() {
        try {
            Class.forName(MYSQLJDBCDriver);

            conn = DriverManager.getConnection(jdbcUrl, username, Password.getPassword());

            System.out.println("Connection to the DB was successful!");
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }

    }

    /**
     * Close the DB connection.
     */
    public static void closeConnection() {
        try {
            conn.close();
        } catch (Exception e) {
            // Do nothing otherwise this can cause a race condition which the connection
            // could already be closed.
        }
    }

    /**
     * Gets the DB connection.
     *
     * @return the DB connection.
     */
    public static Connection getConn() {
        return conn;
    }
}
