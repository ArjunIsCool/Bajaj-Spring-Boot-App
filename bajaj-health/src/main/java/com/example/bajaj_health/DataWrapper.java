package com.example.bajaj_health;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DataWrapper {
    private List<User> users;
    private Integer n;      // For Q2
    private Integer findId; // For Q2

    public List<User> getUsers() { return users; }
    public void setUsers(List<User> users) { this.users = users; }

    public Integer getN() { return n; }
    public void setN(Integer n) { this.n = n; }

    public Integer getFindId() { return findId; }
    public void setFindId(Integer findId) { this.findId = findId; }
}
