package com.codniv.cookbook1;

public class User {
    private String mName;
    private String mJobTitle;

    public User(String mName, String mJobTitle) {
        this.mName = mName;
        this.mJobTitle = mJobTitle;
    }

    public String getmName() {
        return mName;
    }

    public void setmName(String mName) {
        this.mName = mName;
    }

    public String getmJobTitle() {
        return mJobTitle;
    }

    public void setmJobTitle(String mJobTitle) {
        this.mJobTitle = mJobTitle;
    }
}
