package com.jesper.service;

import com.alibaba.fastjson.JSONObject;
import com.jesper.hftc.config.Result;
import com.jesper.hftc.entity.SalesOrder;
import com.jesper.hftc.entity.SalesOrderChild;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author 廖凡
 * @Date 2020/2/19 19:47
 */
public interface SalesOrderService {
    List<SalesOrder> getOrdersByCustomerId(SalesOrder salesOrder);

    int count(SalesOrder salesOrder);

    List<SalesOrder> getList(SalesOrder salesOrder);

    Result saleProduct(String ids);

    Result saleForm();

    Result saleFormCreate(JSONObject jsonObject);

    SalesOrder getSaleFormById(String id);

    void clean();

//    void ckProduct(String id);

    List<SalesOrderChild> getProduct(String id);

    Result saleOrderChildCk(Integer id, Integer warehouseId, Integer saleNumber);

    void updateCkStatus(String id);

    Integer selectCurOrderNum();

    //不知道可不可以long
    Long selectLastPayment();

    Long selectCURPayment();

    Integer selectLastOrderNum();

    Integer selectCurRefundOrder();

    Integer selectLastRefundOrder();

    Integer selectDayOrderNum();

    /**
     * 退貨
     * @param id
     * @param warehouseId
     * @param returnNumber
     * @return
     */
    Result saleOrderChildTh(Integer id, Integer returnNumber);

    Result delete(String id);

    SalesOrder getReturnSaleForm(String id);

    Result saleOrderCkwc(Integer id);
}
