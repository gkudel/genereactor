package com.softcomputer.metadata;


import com.softcomputer.annotationprocessor.annotations.Column;

public class TestMetaData {

    @Column(name = "GP_OTST_RECID", isPrimaryKey = true)
    private Long Id;
}
