package com.jesper.mapper;

import com.jesper.hftc.entity.Customer;
import com.jesper.hftc.entity.SalesOrder;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author 廖凡
 * @Date 2020/2/18 20:53
 */
@Mapper
public interface CustomerMapper {
    Customer getById(String id);

    int insert(Customer customer);

    int update(Customer customer);

    int delete(@Param("id") String id);

    List<Customer> getCustomerList(Customer customer);

    List<Customer> getAllCustomerList();

    Integer count(Customer customer);

    void addCost(Customer customer);

    void updatePayMoney(Customer customer);
}
