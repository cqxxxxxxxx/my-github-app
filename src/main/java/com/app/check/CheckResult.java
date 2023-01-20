package com.app.check;

public class CheckResult {
    private boolean pass = true;
    private String title;
    private StringBuilder message = new StringBuilder();

    public boolean isPass() {
        return pass;
    }

    public void setMessage(StringBuilder message) {
        this.message = message;
    }

    public CheckResult resetMessage() {
        this.message = new StringBuilder();
        return this;
    }

    public void setPass(boolean pass) {
        this.pass = pass;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public CheckResult append(String s) {
        message.append(s);
        return this;
    }

    public String getMessage() {
        return message.toString();
    }
}
