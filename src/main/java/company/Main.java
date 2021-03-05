package company;

import company.database.Database;
import company.entity.User;
import company.enums.Gender;
import company.utill.Util;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Database dat = new Database();
        Util util = new Util();
        //System.out.println(util.getJson(dat.getMales()));
        User user = new User("jANKO      ", "     hRaSkO      ", 10, Gender.MALE.getValue());
        System.out.println(user.toString());
        //dat.insertNewUser(new User("John", "Wick", 30, Gender.MALE.getValue()));
        //dat.getUser("John");
    }
}