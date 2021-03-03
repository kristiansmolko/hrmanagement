package company.log;

public class Log {
    public void error(String msg){
        System.out.println("[ERROR] : " + msg);
    }

    public void print(String msg){
        System.out.println("[OK] : " + msg);
    }

    public void info(String msg){
        System.out.println("[INFO] : " + msg);
    }


}
