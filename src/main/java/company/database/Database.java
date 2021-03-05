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
    private final String getUsersByAge = "SELECT * FROM user WHERE age BETWEEN ? AND ?";
    private final String getUserById = "SELECT * FROM user WHERE id = ?";
    private final String getAllUsers = "SELECT * from user";
    private final String updateAge = "UPDATE user SET age = ? WHERE id = ?";
    private final String getUserByPattern = "SELECT * FROM user WHERE lname LIKE ? OR fname LIKE ?";

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
        if (from < 0 || to < 0 || from > to)
            return null;
        try (Connection connection = getConnection()){
            if (connection != null) {
                PreparedStatement ps = connection.prepareStatement(getUsersByAge);
                ps.setInt(1, from);
                ps.setInt(2, to);
                return executeSelect(ps);
            }
        } catch (Exception e) { log.error(e.toString()); }
        return null;
    }

    public List<User> getAllUsers(){
        try (Connection connection = getConnection()){
            if (connection != null) {
                PreparedStatement ps = connection.prepareStatement(getAllUsers);
                return executeSelect(ps);
            }
        } catch (Exception e) { log.error(e.toString()); }
        return null;
    }

    public User getUserById(int id){
        if (id < 0) {
            log.error("Wrong id");
            return null;
        }
        try (Connection connection = getConnection()){
            if (connection != null) {
                PreparedStatement ps = connection.prepareStatement(getUserById);
                ps.setInt(1, id);
                List<User> list =  executeSelect(ps);
                if (list.isEmpty())
                    return null;
                else
                    return list.get(0);
            }
        } catch (Exception e) { log.error(e.toString()); }
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

    public boolean changeAge(int id, int age){
        if (id < 0 || age < 1 || age >= 100)
            return false;
        try (Connection connection = getConnection()){
            if (connection != null) {
                PreparedStatement ps = connection.prepareStatement(updateAge);
                ps.setInt(1, age);
                ps.setInt(2, id);
                int update = ps.executeUpdate();
                log.print("Updated age for id: " + id);
                return update == 1;
            }
        } catch (Exception e) { log.error(e.toString()); }
        return false;
    }

    public List<User> getUser(String pattern){
        try (Connection connection = getConnection()){
            if (connection != null){
                PreparedStatement ps = connection.prepareStatement(getUserByPattern);
                String newPattern = "%" + pattern + "%";
                ps.setString(1, newPattern);
                ps.setString(2, newPattern);
                return executeSelect(ps);
            }
        } catch (Exception e) { log.error(e.toString()); }

        return null;
    }
}
