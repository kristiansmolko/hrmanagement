package company;

import company.database.Database;

public class Main {
    public static void main(String[] args) {
        Database dat = new Database();
        dat.getConnection();
    }
}