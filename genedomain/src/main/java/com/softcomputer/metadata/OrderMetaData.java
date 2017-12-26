package com.softcomputer.metadata;

import com.softcomputer.annotations.Column;

public class OrderMetaData {

    @Column(name = "GP_ORD_RECID", isPrimaryKey = true)
    private Long Id;
}
