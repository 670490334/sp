package com.jesper.mapper;

import com.jesper.hftc.entity.SalesOrder;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * @Author 廖凡
 * @Date 2020/2/19 20:02
 */
@Mapper
public interface SalesOrderMapper {
    List<SalesOrder> getOrdersByCustomerId(SalesOrder salesOrder);

    int count(SalesOrder salesOrder);

    List<SalesOrder> getList(SalesOrder salesOrder);

    void save(SalesOrder salesOrder);

    SalesOrder getById(String id);

    void updateCkStatus(String id);

    int selectCurOrderNum();

    Long selectLastPayment();

    Long selectCURPayment();

    Integer selectLastOrderNum();

    Integer selectCurRefundOrder();

    Integer selectLastRefundOrder();

    void th(SalesOrder salesOrder);

    Integer selectDayOrderNum();

    void delete(String id);
}
