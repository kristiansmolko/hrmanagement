package company.entity;

import company.enums.Gender;
import company.utill.Util;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "user")
public class User {
    private int id;
    private String fname;
    private String lname;
    private int age;
    private Gender gender;
    Util util = new Util();

    public User(){}

    public User(int id, String fname, String lname, int age, int gender) {
        this(fname, lname, age, gender);
        this.id = id;
    }

    public User(String fname, String lname, int age, int gender) {
        this.fname = util.normalizeName(fname);
        this.lname = util.normalizeName(lname);
        this.age = age;
        this.gender = gender == 0?Gender.MALE:gender == 1?Gender.FEMALE:Gender.OTHER;
    }

    @XmlElement
    public int getId() {
        return id;
    }
    @XmlElement
    public String getFname() {
        return fname;
    }
    @XmlElement
    public String getLname() {
        return lname;
    }
    @XmlElement
    public int getAge() {
        return age;
    }
    @XmlElement
    public Gender getGender() {
        return gender;
    }

    @Override
    public String toString() {
        return "\n--------------------------------------\n" +
                "First name: " + fname + "\nLast name: " + lname +
                "\nAge: " + age + "\nGender: " + gender +
                "\n---------------------------------------\n";
    }
}
