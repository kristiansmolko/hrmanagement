package company.user;

import company.utill.Util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Login {
    private static Map<String, Date> blocked;
    private static Map<String, Integer> attempt;
    private final String PASSWORD = "Kosice2021!";
    private final DateFormat minutes = new SimpleDateFormat("mm");
    private final DateFormat hours = new SimpleDateFormat("HH");
    private final DateFormat date = new SimpleDateFormat("yyyy-MM-dd");
    Util util = new Util();

    public Login(){
        blocked = new HashMap<>();
        attempt = new HashMap<>();
    }

    public String loginUser(String username, String password){
        for (Map.Entry<String, Date> entry : blocked.entrySet())
            if (entry.getKey().equals(username)){
                Date today = new Date();
                Date databaseDate = entry.getValue();
                if (date.format(today).equals(date.format(databaseDate)))
                    //checks date
                    if (Integer.parseInt(hours.format(today)) <= Integer.parseInt(hours.format(databaseDate)))
                        if (Integer.parseInt(minutes.format(databaseDate)) < Integer.parseInt(minutes.format(today))) {
                            //if 1 and more minutes passed, remove from blocked
                            blocked.remove(username);
                            break;
                        }
                else
                    blocked.remove(username);
                return "401";
            }
        if (password.equals(PASSWORD)){
            for (Map.Entry<String, Integer> entry : attempt.entrySet())
                if (entry.getKey().equals(username))
                    attempt.remove(username);
            return util.generateToken();
        } else {
            for (Map.Entry<String, Integer> entry : attempt.entrySet()) {
                //checks attempt map
                if (entry.getKey().equals(username)) {
                    //if username is found
                    if (entry.getValue() < 2) { //and value is < 3
                        attempt.put(username, entry.getValue() + 1); //add 1
                    }
                    else { // if value is >= 3
                        attempt.remove(username);
                        Date today = new Date();
                        blocked.put(username, today); //put him to blocked
                    }
                    return null;
                }
            }
            attempt.put(username, 1);
        }
        return null;
    }

    
}
