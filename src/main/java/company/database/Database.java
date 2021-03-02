package company.database;

import company.log.Log;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
    Log log = new Log();
    public Connection getConnection(){
        try {
            Properties prop = new Properties();
            InputStream loader = getClass().getClassLoader().getResourceAsStream("dat.properties");
            prop.load(loader);
            String url = prop.getProperty("url");
            String user = prop.getProperty("user");
            String pass = prop.getProperty("password");
            Connection connection = DriverManager.getConnection(url, user, pass);
            if (connection != null){
                log.print("Connection : Success");
                return connection;
            }
        } catch (Exception e) { log.error(e.toString()); }
        return null;
    }

    public void close(Connection con) {
        if (con != null) {
            try {
                con.close();
                log.print("Connection closed");
            } catch (SQLException e) { log.error(e.toString()); }
        }
    }
}
