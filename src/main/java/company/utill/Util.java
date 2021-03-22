package company.utill;

import company.entity.User;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.security.SecureRandom;
import java.util.Base64;

public class Util {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    public String getJson(List<User> list){
        if (list.isEmpty())
            return "{}";
        JSONObject object = new JSONObject();
        object.put("datetime", getCurrentDateTime());
        object.put("size", list.size());
        JSONArray array = new JSONArray();
        for (User user : list) {
            getJSONUser(array, user);
        }
        object.put("users", array);
        return object.toJSONString();
    }

    private void getJSONUser(JSONArray array, User user) {
        JSONObject jsonUser = new JSONObject();
        jsonUser.put("id", user.getId());
        jsonUser.put("fname", user.getFname());
        jsonUser.put("lname", user.getLname());
        jsonUser.put("age", user.getAge());
        jsonUser.put("gender", user.getGender().toString());
        array.add(jsonUser);
    }

    public String getJson(User user){
        if (user == null)
            return "{}";
        JSONObject object = new JSONObject();
        object.put("datetime", getCurrentDateTime());
        object.put("size", 1);
        JSONArray array = new JSONArray();
        getJSONUser(array, user);
        object.put("users", array);
        return object.toJSONString();
    }

    public String getCurrentDateTime(){
        Date today = new Date();
        DateFormat format = new SimpleDateFormat("yyyy-MM-dd hh-mm-ss");
        System.out.println(format.format(today));
        return format.format(today); //2021-03-05 15:07:23
    }

    public String normalizeName(String name){
        if (name == null || name.equals(""))
            return "";
        name = name.trim();
        String partOfName = name.replace(String.valueOf(name.charAt(0)), "");
        String newName = Character.toUpperCase(name.charAt(0)) + partOfName.toLowerCase();
        return newName;
        //or better
        //return Character.toUpperCase(name.charAt(0)) + name.substring(1).toLowerCase()
    }

    public double normalizeNum(double num){
        num *= 100;
        int number = (int) Math.round(num);
        return number/100.0;
    }

    public String generateToken(){
        byte[] randomBytes = new byte[32];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public boolean authorizeToken(String token){
        Pattern pattern = Pattern.compile("^[a-zA-Z0-9_-]{43}(=)$");
        Matcher matcher = pattern.matcher(token);
        return matcher.find();
    }
}
