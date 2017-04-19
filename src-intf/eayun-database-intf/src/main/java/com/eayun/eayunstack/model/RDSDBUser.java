package com.eayun.eayunstack.model;

public class RDSDBUser {
    private String name;
    private String password;
    private Databases[] databases;
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Databases[] getDatabases() {
        return databases;
    }
    public void setDatabases(Databases[] databases) {
        this.databases = databases;
    }
}
