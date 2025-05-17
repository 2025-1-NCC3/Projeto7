package br.fecap.pi.ubersaferroutes;

public class UserActivity {

    private String email;
    private String password;

    public UserActivity(String email, String password) {
        this.email = email;
        this.password = password;
    }

    public String getEmail() {
        return email;
    }

    public String getPassword() {
        return password;
    }
}
