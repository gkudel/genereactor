package com.softcomputer.model;

import com.softcomputer.annotations.Column;

public class TestNgs extends Test {
    @Column(name = "GP_OTST_DESC")
    private String description;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
