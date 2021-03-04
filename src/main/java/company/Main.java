package company;

import company.database.Database;
import company.entity.User;
import company.enums.Gender;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        Database dat = new Database();
        //dat.insertNewUser(new User("John", "Wick", 30, Gender.MALE.getValue()));
        dat.getUser("John");
    }
}