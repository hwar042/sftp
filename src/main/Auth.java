package main;

public class Auth {
    String acct;
    String pass;
    boolean t_user = false;
    boolean t_acct = false;
    boolean t_pass = false;
    boolean auth = false;

    public void setAcct(String acct) {
        this.acct = acct;
    }

    public void setPass(String pass) {
        this.pass = pass;
    }

    public void setT_user() {
        t_user = true;
    }

    public void setT_acct() {
        t_acct = true;
    }

    public void setT_pass() {
        t_pass = true;
    }

    public void setAuth() {
        t_user = true;
        t_acct = true;
        auth = true;
    }
}
