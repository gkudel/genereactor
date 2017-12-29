package com.softcomputer.ngs.model;

import com.softcomputer.annotationprocessor.annotations.Column;
import com.softcomputer.model.Test;

public class TestNgs extends Test {

    @Column(name = "GP_OTST_DESC")
    private String description;

    @Column(name = "GP_OTST_TASKLISTID", foreignEntity = Tasklist.class, whereClause = " = (SELECT GP_TKL_RECID FROM GP_TASKLIST WHERE GP_TKL_NUM = ?)")
    private Long tasklistId;

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getTasklistId() {
        return tasklistId;
    }

    public void setTasklistId(Long tasklistId) {
        this.tasklistId = tasklistId;
    }
}

