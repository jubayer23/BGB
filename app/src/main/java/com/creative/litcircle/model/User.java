package com.creative.litcircle.model;

/**
 * Created by comsol on 01-Dec-16.
 */
public class User {

    String id;
    String user_id;
    String name;
    String email;

    public User(String id, String user_id) {
        this.id = id;
        this.user_id = user_id;
    }

    public User(String id, String user_id, String name, String email) {
        this.id = id;
        this.user_id = user_id;
        this.name = name;
        this.email = email;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }
}
