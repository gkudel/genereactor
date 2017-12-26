package com.softcomputer.model;


import com.softcomputer.annotations.Column;
import com.softcomputer.annotations.MetaData;
import com.softcomputer.annotations.Table;
import com.softcomputer.metadata.TestMetaData;

@Table(name = "GP_OTEST")
@MetaData(type = TestMetaData.class)
public class Test extends Entity<Long> {

    @Column(name = "GP_OTST_ORDID", foreignEntity = Order.class)
    private Long orderRecid;

    @Column(name = "GP_OTST_TYPE")
    private String type;

    public Long getOrderRecid() {
        return orderRecid;
    }

    public void setOrderRecid(Long orderRecid) {
        this.orderRecid = orderRecid;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
