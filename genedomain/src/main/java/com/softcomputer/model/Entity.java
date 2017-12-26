package com.softcomputer.model;

public class Entity<TKey> {

    private TKey Id;

    public TKey getId() {
        return Id;
    }

    public void setId(TKey id) {
        Id = id;
    }
}
