package com.jesper.service.impl;

import com.jesper.hftc.config.MyConfig;
import com.jesper.hftc.config.Result;
import com.jesper.hftc.entity.ApiLog;
import com.jesper.hftc.entity.Product;
import com.jesper.hftc.entity.PurchaseOrder;
import com.jesper.hftc.entity.Warehousemanage;
import com.jesper.mapper.ApiLogMapper;
import com.jesper.mapper.ProductMapper;
import com.jesper.mapper.PurchaseOrderMapper;
import com.jesper.mapper.WarehousemangeMapper;
import com.jesper.service.PurchaseOrderService;
import com.jesper.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Author 廖凡
 * @Date 2020/3/1 16:46
 */
@SuppressWarnings("all")
@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    @Autowired
    private ApiLogMapper apiLogMapper;
    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;

    @Autowired
    private WarehousemangeMapper warehousemangeMapper;

    @Autowired
    private ProductMapper productMapper;

    @Override
    public int count(PurchaseOrder purchaseOrder) {
        int count = 0;
        try {
            if (!StringUtils.isEmpty(purchaseOrder.getBeginTimeStr())){
                purchaseOrder.setBeginTime(DateUtil.strToDate(purchaseOrder.getBeginTimeStr()));
            }
            if (!StringUtils.isEmpty(purchaseOrder.getEndTimeStr())){
                purchaseOrder.setEndTime(DateUtil.strToDate(purchaseOrder.getEndTimeStr()));
            }
            count = purchaseOrderMapper.count(purchaseOrder);
        } catch (Exception e) {
            System.out.println("错误信息：" + e.getMessage());
        }
        return count;
    }

    @Override
    public List<PurchaseOrder> getList(PurchaseOrder purchaseOrder, int start, int end) {
        List<PurchaseOrder> list = new ArrayList<>();
        try {
            if (!StringUtils.isEmpty(purchaseOrder.getBeginTimeStr())){
                purchaseOrder.setBeginTime(DateUtil.strToDate(purchaseOrder.getBeginTimeStr()));
            }
            if (!StringUtils.isEmpty(purchaseOrder.getEndTimeStr())){
                purchaseOrder.setEndTime(DateUtil.strToDate(purchaseOrder.getEndTimeStr()));
            }
            list = purchaseOrderMapper.getList(purchaseOrder, start, end);
            for (PurchaseOrder p : list) {
                p.setCreateTimeStr(DateUtil.getDateStr(p.getCreateTime()));
            }
        } catch (Exception e) {
            System.out.println("错误信息：" + e.getMessage());
        }
        return list;
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public boolean update(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getId() == null) {
            return false;
        }

        purchaseOrder.setUpdateTime(new Date());
        try {
            Integer returnNumber = purchaseOrder.getReturnNumber();
            if (returnNumber == null) {
//                purchaseOrderMapper.update(purchaseOrder);
                return false;
            } else if (returnNumber >= 0) {
                ;
                PurchaseOrder obj = purchaseOrderMapper.getById(purchaseOrder.getId());
                obj.setStyle(1);
                Integer baseReturnNumber = obj.getReturnNumber();
                if (baseReturnNumber == null) baseReturnNumber = 0;
                obj.setReturnNumber(baseReturnNumber + returnNumber);
                obj.setUpdateTime(new Date());
                obj.setNumber(obj.getNumber()-returnNumber);
                obj.setPurchaseNumber(purchaseOrder.getPurchaseNumber());
                purchaseOrderMapper.update(obj);

//                Warehousemanage warehousemanage = warehousemangeMapper.getById(obj.getInwarehouseId());
//                Integer warehouseBaseNumber = warehousemanage.getNumber();
//                if (warehouseBaseNumber >= returnNumber) {
//                    warehousemanage.setNumber(warehouseBaseNumber - returnNumber);
//                    warehousemanage.setUpdateTime(new Date());
//                    warehousemangeMapper.inStorage(warehousemanage);
//                }
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//进行手动回滚
            System.out.println(e.getMessage());
            return false;
        }
    }

    @Override
    public Result updateFinisnPurchaseService(Integer id, Integer state) {
        try {
            purchaseOrderMapper.updateFinisnPurchaseService(id, state);
            return Result.ok();
        } catch (Exception e) {
            return Result.ofMessage(400, e.getMessage());
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result add(PurchaseOrder purchaseOrder) {
        if (purchaseOrder.getProductId() == 0 || purchaseOrder.getProductId() == null) {
            return Result.ofMessage(400, "请选择商品");
        }
//        if (purchaseOrder.getInwarehouseId() == 0 || purchaseOrder.getInwarehouseId() == null) {
//            return Result.ofMessage(400, "请选择仓库");
//        }
        if (StringUtils.isEmpty(purchaseOrder.getSupplierId())) {
            return Result.ofMessage(400, "请选择供应商");
        }
        ApiLog apiLog = new ApiLog();
        apiLog.setCreateTime(new Date());
        apiLog.setMethodName("PurchaseOrderService.add()");
        apiLog.setParams(purchaseOrder.toString());
        try {
            Integer number = purchaseOrder.getNumber();
            if (number == null) return Result.ofMessage(400,"数量为空");
            //入库数量不为0
//            if (number != 0) {
//                Warehousemanage warehousemanage = warehousemangeMapper.getById(purchaseOrder.getInwarehouseId());
//                Integer baseNumber = warehousemanage.getNumber();
//                if (baseNumber == null) baseNumber = 0;
//                if (warehousemanage.getProductId() == null || warehousemanage.getProductId() == 0) {
//                    warehousemangeMapper.addProduct(purchaseOrder.getProductId(), purchaseOrder.getInwarehouseId());
//                }
//                warehousemanage.setNumber(baseNumber + number);
//                warehousemanage.setUpdateTime(new Date());
//                //仓库入库
//                warehousemangeMapper.inStorage(warehousemanage);
//
//                Product product = productMapper.getProductById(purchaseOrder.getProductId());
//                Integer baseProductNumber = product.getInventoryNumber();
//                if (baseProductNumber == null) baseProductNumber = 0;
//                product.setInventoryNumber(baseProductNumber + number);
//                //商品入库
//                productMapper.inStorage(product);
//            }
            Date date = new Date();
            purchaseOrder.setCreateTime(date);

            purchaseOrder.setPurchaseTime(date);
            if (purchaseOrder.getPurchaseNumber()==null || purchaseOrder.getPurchaseNumber().equals("")) {
                purchaseOrder.setPurchaseNumber("CGDD-" + date.getYear() + date.getMonth() + date.getDate() + "-" + purchaseOrder.getProductId());
            }
            if (purchaseOrder.getTotalMoney()==null){
                purchaseOrder.setTotalMoney(purchaseOrder.getAmount().multiply(new BigDecimal(purchaseOrder.getNumber())));
            }
            purchaseOrderMapper.insert(purchaseOrder);
            apiLog.setSuccess(MyConfig.SUCCESS);
            return Result.ok();
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//进行手动回滚
            apiLog.setSuccess(MyConfig.FAILD);
            apiLog.setMsg(e.getMessage().length() > 100 ? e.getMessage().substring(0, 99) : e.getMessage());
            System.out.println("错误：" + e.getMessage());
            return Result.ofMessage(400, e.getMessage());
        } finally {
            apiLogMapper.save(apiLog);
        }
    }

    @Override
    public PurchaseOrder getById(Integer id) {
        PurchaseOrder purchaseOrder = null;
        try {
            purchaseOrder = purchaseOrderMapper.getById(id);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return purchaseOrder;
    }
}
