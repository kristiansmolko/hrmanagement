package company.user;

import company.utill.Util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class Login {
    private static Map<String, Date> blocked;
    private static Map<String, Integer> attempt;
    private final String PASSWORD = "Kosice2021!";
    Util util = new Util();

    public Login(){
        blocked = new HashMap<>();
        attempt = new HashMap<>();
    }

    public String loginUser(String username, String password){
        for (Map.Entry<String, Date> entry : blocked.entrySet())
            if (entry.getKey().equals(username)){
                //if date.today == date.entry

                //else block remove
            }

        if (password.equals(PASSWORD)){
            for (Map.Entry<String, Integer> entry : attempt.entrySet())
                if (entry.getKey().equals(username))
                    attempt.remove(username);
            return util.generateToken();
        } else {
            for (Map.Entry<String, Integer> entry : attempt.entrySet())
                //checks attempt map
                if (entry.getKey().equals(username)){
                    //if username is found
                    if (entry.getValue() < 3) //and value is < 3
                        attempt.put(username, entry.getValue() + 1); //add 1
                    else { // if value is >= 3
                        attempt.remove(username);
                        Date today = new Date();
                        blocked.put(username, today); //put him to blocked
                    }
                }
            //is username is not in attempts, put him in
            attempt.put(username, 1);
        }
        return null;
    }

    
}
