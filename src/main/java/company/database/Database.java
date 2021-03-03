package company.database;

import company.entity.User;
import company.log.Log;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Properties;

public class Database {
    Log log = new Log();
    private final String insertQuery = "INSERT INTO user (fname, lname, age, gender) " +
            "VALUES (?, ?, ?, ?)";

    public Connection getConnection(){
        try {
            Properties prop = new Properties();
            InputStream loader = getClass().getClassLoader().getResourceAsStream("dat.properties");
            prop.load(loader);
            String url = prop.getProperty("url");
            String user = prop.getProperty("name");
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

    public boolean insertNewUser(User user){
        try (Connection connection = getConnection()){
            PreparedStatement ps = connection.prepareStatement(insertQuery);
            ps.setString(1, user.getFname());
            ps.setString(2, user.getLname());
            ps.setInt(3, user.getAge());
            ps.setInt(4, user.getGender().getValue() == 0 ? 0 : user.getGender().getValue() == 1 ? 1 : 2);
            int result = ps.executeUpdate();
            log.print("Added new user");
            close(connection);
            return result == 1;
        } catch (Exception e) { log.error(e.toString()); }
        return false;
    }
}
