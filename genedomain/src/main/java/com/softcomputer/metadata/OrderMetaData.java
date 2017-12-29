package com.softcomputer.metadata;

import com.softcomputer.annotationprocessor.annotations.Column;

public class OrderMetaData {

    @Column(name = "GP_ORD_RECID", isPrimaryKey = true)
    private Long Id;
}
