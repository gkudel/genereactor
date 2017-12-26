package com.softcomputer.model;


import com.softcomputer.annotations.Column;
import com.softcomputer.annotations.MetaData;
import com.softcomputer.annotations.Table;
import com.softcomputer.metadata.OrderMetaData;

@Table(name = "GP_ORDER")
@MetaData(type = OrderMetaData.class)
public class Order extends Entity<Long> {

    @Column(name = "GP_ORD_ORDNUM")
    private String orderNumber;

    public String getOrderNumber() {
        return orderNumber;
    }

    public void setOrderNumber(String orderNumber) {
        this.orderNumber = orderNumber;
    }
}
