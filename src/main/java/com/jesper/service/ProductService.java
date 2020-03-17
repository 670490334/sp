package com.jesper.service;

import com.jesper.hftc.config.Result;
import com.jesper.hftc.entity.Product;
import com.jesper.hftc.entity.ProductInstorge;

import java.math.BigDecimal;
import java.util.List;

/**
 * @Author 廖凡
 * @Date 2020/2/17 22:33
 */
public interface ProductService {
    void add(Product product);

    void edit(Product product);

    Result delete(Integer id);

    Product getProduct(Integer id);

    List<Product> getProductListBy(String categoryName, Integer pageCurrent, Integer pageSize);

    List<Product> getProductList(Product product);

    int count(Product product);

    Result loss(Integer id, Integer lossNumber, Integer warehouseId,BigDecimal price,String jianshu,BigDecimal totalMoney);

    Result sale(Integer id, Integer saleNumber, Integer warehouseId,BigDecimal price,String jianshu,BigDecimal totalMoney);

    Result inStorage(Integer id, Integer warehouseId, Integer number, BigDecimal price, String jianshu, BigDecimal totalMoney);

    Result instorgeProduct(ProductInstorge productInstorge,Product product);

    List<ProductInstorge> getProductInstorgeList(ProductInstorge productInstorge);

    Integer count(ProductInstorge productInstorge);
}
