package com.example.bajaj_health;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataWrapper {
    private List<User> users;

    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }
}
