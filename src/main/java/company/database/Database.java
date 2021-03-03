package company.database;

import company.entity.User;
import company.log.Log;

import java.io.InputStream;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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
            if (connection != null) {
                PreparedStatement ps = connection.prepareStatement(insertQuery);
                ps.setString(1, user.getFname());
                ps.setString(2, user.getLname());
                ps.setInt(3, user.getAge());
                ps.setInt(4, user.getGender().getValue() == 0 ? 0 : user.getGender().getValue() == 1 ? 1 : 2);
                int result = ps.executeUpdate();
                log.print("Added new user");
                return result == 1;
            }
        } catch (Exception e) { log.error(e.toString()); }
        return false;
    }

    public List<User> getFemales(){
        String query = "SELECT * FROM user WHERE gender = 1";
        try (Connection connection = getConnection()){
            if (connection != null) {
                PreparedStatement ps = connection.prepareStatement(query);
                return executeSelect(ps);
            }
        } catch (Exception e) { log.error(e.toString()); }
        return null;
    }

    public List<User> getMales(){
        String query = "SELECT * FROM user WHERE gender = 0";
        try (Connection connection = getConnection()){
            if (connection != null) {
                PreparedStatement ps = connection.prepareStatement(query);
                return executeSelect(ps);
            }
        } catch (Exception e) { log.error(e.toString()); }
        return null;
    }

    public List<User> getUsersByAge(int from, int to){
        return null;
    }

    private List<User> executeSelect(PreparedStatement ps) throws SQLException {
        ResultSet rs =  ps.executeQuery();
        List<User> list = new ArrayList<>();
        while (rs.next()){
            String fname = rs.getString("fname");
            String lname = rs.getString("lname");
            int age = rs.getInt("age");
            int gender = rs.getInt("gender");
            int id = rs.getInt("id");
            list.add(new User(id, fname, lname, age, gender));
        }
        log.info("Number of records: " + list.size());
        return list;
    }
}
