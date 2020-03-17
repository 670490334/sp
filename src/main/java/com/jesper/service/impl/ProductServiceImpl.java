package com.jesper.service.impl;

import com.jesper.hftc.config.MyConfig;
import com.jesper.hftc.config.Result;
import com.jesper.hftc.entity.ApiLog;
import com.jesper.hftc.entity.Product;
import com.jesper.hftc.entity.ProductInstorge;
import com.jesper.hftc.entity.Warehousemanage;
import com.jesper.mapper.ApiLogMapper;
import com.jesper.mapper.ProductInstorgeMapper;
import com.jesper.mapper.ProductMapper;
import com.jesper.mapper.WarehousemangeMapper;
import com.jesper.service.ProductService;
import com.jesper.util.DateUtil;
import groovy.util.logging.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author 廖凡
 * @Date 2020/2/17 23:04
 */
@SuppressWarnings("all")
@Service
@Slf4j
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ApiLogMapper apiLogMapper;

    @Autowired
    private WarehousemangeMapper warehousemangeMapper;
    @Autowired
    private WarehousemangeServiceImpl warehousemangeService;
    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private ProductInstorgeMapper productInstorgeMapper;
    @Override
    public void add(Product product) {
        ApiLog apiLog = new ApiLog();
        apiLog.setCreateTime(new Date());
        apiLog.setMethodName("ProductService.add()");
        apiLog.setParams(product.toString().length() > 1000 ? product.toString().substring(0, 999) : product.toString());
        try {
            productMapper.insert(product);
            apiLog.setSuccess(MyConfig.SUCCESS);
        } catch (Exception e) {
            apiLog.setSuccess(MyConfig.FAILD);
            apiLog.setMsg(e.getMessage().toString().length() > 1000 ? e.getMessage().toString().substring(0, 999) : e.getMessage().toString());
            System.out.println(product.toString() + ":保存失败----错误信息：" + e.getMessage());
        } finally {
            apiLogMapper.save(apiLog);
        }
    }

    @Override
    public void edit(Product product) {
        ApiLog apiLog = new ApiLog();
        apiLog.setCreateTime(new Date());
        apiLog.setMethodName("ProductService.edit()");
        apiLog.setParams(product.toString().length() > 1000 ? product.toString().substring(0, 999) : product.toString());
        try {
            productMapper.edit(product);
            apiLog.setSuccess(MyConfig.SUCCESS);
        } catch (Exception e) {
            apiLog.setSuccess(MyConfig.FAILD);
            apiLog.setMsg(e.getMessage().toString().length() > 50 ? e.getMessage().toString().substring(0, 49) : e.getMessage().toString());
            System.out.println(product.getProductName() + ":修改失败---错误信息：" + e.getMessage());
        } finally {
            apiLogMapper.save(apiLog);
        }
    }

    @Override
    public Result delete(Integer id) {
        ApiLog apiLog = new ApiLog();
        apiLog.setCreateTime(new Date());
        apiLog.setMethodName("ProductService.delete()");
        apiLog.setParams(id.toString());
        try {
            productMapper.delete(id);
            apiLog.setSuccess(MyConfig.SUCCESS);
            return Result.ok();
        } catch (Exception e) {
            apiLog.setSuccess(MyConfig.FAILD);
            apiLog.setMsg(e.getMessage().toString().length() > 50 ? e.getMessage().toString().substring(0, 49) : e.getMessage().toString());
            System.out.println("删除商品id为+" + id + "----失败;错误信息：" + e.getMessage());
            return Result.ofMessage(400,e.getMessage());
        } finally {
            apiLogMapper.save(apiLog);
        }
    }

    @Override
    public Product getProduct(Integer id) {

        Product product = new Product();
        try {
            product = productMapper.getProductById(id);
        } catch (Exception e) {
            System.out.println("错误信息：" + e.getMessage());
        }
        return product;
    }

    @Override
    public List<Product> getProductListBy(String categoryName, Integer pageCurrent, Integer pageSize) {
        List<Product> productList = new ArrayList<>();
        try {
            productList = productMapper.getProductByCategory(categoryName, pageCurrent, pageCurrent + pageSize);
            return productList;
        } catch (Exception e) {
            System.out.println("错误信息：" + e.getMessage());
        }
        return productList;
    }

    @Override
    public List<Product> getProductList(Product product) {
        List<Product> productList = new ArrayList<>();
        try {
            if (product.getProductCategoryName() == "0") {
                product.setProductCategoryName(null);
            }
            productList = productMapper.getProductList(product);
            return productList;
        } catch (Exception e) {
            System.out.println("错误信息：" + e.getMessage());
        }
        return productList;
    }

    @Override
    public int count(Product product) {
        return productMapper.count(product);
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result loss(Integer id, Integer lossNumber, Integer warehouseId,BigDecimal price,String jianshu,BigDecimal totalMoney) {
        ApiLog apiLog = new ApiLog();
        apiLog.setCreateTime(new Date());
        apiLog.setMethodName("ProductService.loss()");
        apiLog.setParams("id:" + id + ";lossNumber" + lossNumber + ";warehouseId" + warehouseId);
        if (price==null) price =new BigDecimal(0);
        if (totalMoney==null) totalMoney =new BigDecimal(0);
        try {
            //查询原本仓库信息
            Product product = productMapper.getProductById(id);
            //库存
            Integer baseLossNumber = product.getLossNumber();
            Integer baseNumber = product.getInventoryNumber();
            if (baseLossNumber == null) baseLossNumber = 0;
            if (baseNumber == null) baseNumber = 0;
            if (lossNumber > baseNumber) {
                return Result.ofMessage(400, "操作失败：库存数量不足");
            }
            //更新库存
            product.setLossNumber(baseLossNumber + lossNumber);
            product.setInventoryNumber(baseNumber - lossNumber);
            productMapper.loss(product);


            //仓库
            Warehousemanage warehousemanage = warehousemangeMapper.getById(warehouseId);
            //库存
            Integer wbaseLossNumber = warehousemanage.getLossNumber();
            if (wbaseLossNumber == null) wbaseLossNumber = 0;
            Integer wbaseNumber = warehousemanage.getNumber();
            if (wbaseNumber == null) wbaseNumber = 0;
            //更新库存
            warehousemanage.setLossNumber(wbaseLossNumber + lossNumber);
            if (wbaseNumber < lossNumber) return Result.ofMessage(400, "库存不足");
            warehousemanage.setNumber(wbaseNumber - lossNumber);
            warehousemangeMapper.loss(warehousemanage);

            //入库记录
            ProductInstorge productInstorge = new ProductInstorge();
            productInstorge.setProductId(id);
            productInstorge.setState(2);
            productInstorge.setCreateTime(new Date());
            productInstorge.setAddress(warehousemanage.getAddress());
            productInstorge.setChmc(warehousemanage.getChmc());
            productInstorge.setJianshu(jianshu);
            productInstorge.setNumber(lossNumber);
            productInstorge.setWarehouseId(warehousemanage.getParentId());
            productInstorge.setPrice(price);
            productInstorge.setTotalMoney(totalMoney);

            productInstorgeMapper.save(productInstorge);
            apiLog.setSuccess(MyConfig.SUCCESS);
            return Result.ok();
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//进行手动回滚
            apiLog.setSuccess(MyConfig.FAILD);
            apiLog.setMsg(e.getMessage().length() > 1000 ? e.getMessage().substring(0, 999) : e.getMessage());
            System.out.println(":----错误信息：" + e.getMessage());
            return Result.ofMessage(400, e.getMessage());
        } finally {
            apiLogMapper.save(apiLog);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result sale(Integer id, Integer saleNumber, Integer warehouseId,BigDecimal price,String jianshu,BigDecimal totalMoney) {
        ApiLog apiLog = new ApiLog();
        apiLog.setCreateTime(new Date());
        apiLog.setMethodName("WarehousemangeService.sale()");
        apiLog.setParams("id:" + id + ";saleNumber" + saleNumber + ";warehouseId" + warehouseId);
        if (price==null) price =new BigDecimal(0);
        if (totalMoney==null) totalMoney =new BigDecimal(0);
        try {
            //查询原本仓库信息
            Product product = productMapper.getProductById(id);

            if (price == null) {
                price = product.getPrice();
                if (totalMoney == null){
                    totalMoney = price.multiply(new BigDecimal(saleNumber));
                }
            }
            //库存
            Integer baseSaleNumber = product.getSaleNumber();
            Integer baseNumber = product.getInventoryNumber();
            if (baseNumber == null) baseNumber = 0;
            if (baseNumber < saleNumber) {
                return Result.ofMessage(400, "库存数量不足");
            }
            //更新库存
            if (baseSaleNumber == null) baseSaleNumber = 0;
            product.setSaleNumber(baseSaleNumber + saleNumber);
            product.setInventoryNumber(baseNumber - saleNumber);
            productMapper.sale(product);


            Warehousemanage warehousemanage = warehousemangeMapper.getById(warehouseId);
            //库存
            Integer wbaseSaleNumber = warehousemanage.getSaleNumber();
            if (wbaseSaleNumber == null) wbaseSaleNumber = 0;
            Integer wbaseNumber = warehousemanage.getNumber();
            if (wbaseNumber == null) wbaseNumber = 0;
            //更新库存
            warehousemanage.setSaleNumber(wbaseSaleNumber + saleNumber);
            if (wbaseNumber < saleNumber) return Result.ofMessage(400, "库存不足");
            warehousemanage.setNumber(wbaseNumber - saleNumber);
            warehousemangeMapper.sale(warehousemanage);
            //入库记录
            ProductInstorge productInstorge = new ProductInstorge();
            productInstorge.setProductId(id);
            productInstorge.setState(1);
            productInstorge.setCreateTime(new Date());
            productInstorge.setAddress(warehousemanage.getAddress());
            productInstorge.setChmc(warehousemanage.getChmc());
            productInstorge.setJianshu(jianshu);
            productInstorge.setNumber(saleNumber);
            productInstorge.setWarehouseId(warehousemanage.getParentId());
            productInstorge.setPrice(price);
            productInstorge.setTotalMoney(totalMoney);

            productInstorgeMapper.save(productInstorge);
            apiLog.setSuccess(MyConfig.SUCCESS);

            return Result.ok();
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//进行手动回滚
            apiLog.setSuccess(MyConfig.FAILD);
            apiLog.setMsg(e.getMessage().length() > 1000 ? e.getMessage().substring(0, 999) : e.getMessage());
            System.out.println(":----错误信息：" + e.getMessage());
            return Result.ofMessage(400, e.getMessage());
        } finally {
            apiLogMapper.save(apiLog);
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result inStorage(Integer id, Integer inNumber, Integer warehouseId, BigDecimal price, String jianshu, BigDecimal totalMoney) {
        ApiLog apiLog = new ApiLog();
        apiLog.setCreateTime(new Date());
        apiLog.setMethodName("WarehousemangeService.inStorage()");
        apiLog.setParams("id:" + id + ";inNumber" + inNumber + ";warehouseId" + warehouseId);
        try {
            /*商品更新*/
            //查询原本仓库信息
            Product product = productMapper.getProductById(id);
            if (totalMoney==null){
                totalMoney = price.multiply(new BigDecimal(inNumber));
            }
            //库存
            Integer baseNumber = product.getInventoryNumber();
            //更新库存
            if (baseNumber == null) baseNumber = 0;
            product.setInventoryNumber(baseNumber + inNumber);
            productMapper.inStorage(product);


            /*
             * 存入仓库
             * */
            Warehousemanage warehousemanage = warehousemangeMapper.getById(warehouseId);
            if (warehousemanage.getProductId() == null || warehousemanage.getProductId() == 0) {
                warehousemangeMapper.addProduct(id, warehouseId);
            }
            //库存
            Integer wbaseNumber = warehousemanage.getNumber();
            if (wbaseNumber == null) wbaseNumber = 0;
            //更新库存
            warehousemanage.setNumber(wbaseNumber + inNumber);
            warehousemangeMapper.inStorage(warehousemanage);

            //入库记录
            ProductInstorge productInstorge = new ProductInstorge();
            productInstorge.setProductId(id);
            productInstorge.setState(0);
            productInstorge.setCreateTime(new Date());
            productInstorge.setAddress(warehousemanage.getAddress());
            productInstorge.setChmc(warehousemanage.getChmc());
            productInstorge.setJianshu(jianshu);
            productInstorge.setNumber(inNumber);
            productInstorge.setWarehouseId(warehousemanage.getParentId());
            productInstorge.setPrice(price);
            productInstorge.setTotalMoney(totalMoney);

            productInstorgeMapper.save(productInstorge);
            apiLog.setSuccess(MyConfig.SUCCESS);
            return Result.ok();
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//进行手动回滚
            apiLog.setSuccess(MyConfig.FAILD);
            apiLog.setMsg(e.getMessage().length() > 1000 ? e.getMessage().substring(0, 999) : e.getMessage());
            System.out.println(":----错误信息：" + e.getMessage());
            return Result.ofMessage(400, e.getMessage());
        } finally {
            apiLogMapper.save(apiLog);
        }
    }

    /**
     * 商品入库
     * @param productInstorge
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result instorgeProduct(ProductInstorge productInstorge,Product product) {
        try {
            productInstorge.setCreateTime(new Date());
            productInstorge.setState(0);
            productInstorgeMapper.save(productInstorge);

            Warehousemanage warehousemanage = new Warehousemanage();
            warehousemanage.setNumber(productInstorge.getNumber());
            warehousemanage.setUpdateTime(new Date());
            warehousemanage.setCreateTime(new Date());
            warehousemanage.setAddress(productInstorge.getAddress());
            warehousemanage.setProductId(product.getId());
            warehousemanage.setParentId(productInstorge.getWarehouseId());
            warehousemangeMapper.save(warehousemanage);

            Integer inventoryNumber = product.getInventoryNumber();
            if (inventoryNumber==null) inventoryNumber = 0;
            product.setInventoryNumber(inventoryNumber+productInstorge.getNumber());
            product.setUpdateTime(new Date());
            productMapper.inStorage(product);
            return Result.ok();
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//进行手动回滚
            System.out.println(e.getMessage());
            return Result.ofMessage(400,e.getMessage());
        }
    }

    @Override
    public List<ProductInstorge> getProductInstorgeList(ProductInstorge productInstorge) {
        List<ProductInstorge> list =  productInstorgeMapper.getList(productInstorge);
        for (ProductInstorge p: list) {
            p.setCreateTimeStr(DateUtil.getDateStr(p.getCreateTime()));
        }
        return list;
    }

    @Override
    public Integer count(ProductInstorge productInstorge) {
        return productInstorgeMapper.count(productInstorge);
    }
}
