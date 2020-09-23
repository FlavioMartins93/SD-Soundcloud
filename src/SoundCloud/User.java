package SoundCloud;

public class User {
    private String username;
    private String password;

    public User(String un, String pass){
        this.username=un;
        this.password=pass;
    }

    public String getUserName() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

}
