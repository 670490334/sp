package com.jesper.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jesper.hftc.config.MyConfig;
import com.jesper.hftc.config.Result;
import com.jesper.hftc.entity.*;
import com.jesper.mapper.*;
import com.jesper.redis.RedisService;
import com.jesper.redis.SaleProductIds;
import com.jesper.service.SalesOrderService;
import com.jesper.service.WarehousemangeService;
import com.jesper.util.DateUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.interceptor.TransactionAspectSupport;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.*;

/**
 * @Author 廖凡
 * @Date 2020/2/19 20:01
 */
@Service
@SuppressWarnings("all")
public class SalesOrderServiceImpl implements SalesOrderService {


    @Autowired
    private SalesOrderReturnChildMapper salesOrderReturnChildMapper;
    @Autowired
    private CustomerMapper customerMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private SalesOrderChildMapper salesOrderChildMapper;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ApiLogMapper apiLogMapper;
    @Autowired
    private SalesOrderMapper salesOrderMapper;
    @Autowired
    private WarehousemangeService warehousemangeService;
    @Autowired
    private WarehousemangeMapper warehousemangeMapper;
    @Autowired
    private ProductInstorgeMapper productInstorgeMapper;

    @Override
    public List<SalesOrder> getOrdersByCustomerId(SalesOrder salesOrder) {

        List<SalesOrder> list = new ArrayList<>();
        list = salesOrderMapper.getOrdersByCustomerId(salesOrder);
        for (SalesOrder order : list) {
            order.setSellTimeStr(DateUtil.getDateStr(order.getSellTime()));
        }

        return list;

    }

    @Override
    public int count(SalesOrder salesOrder) {
        int count = 0;
        salesOrder.setMinOrderTime(DateUtil.strToDate(salesOrder.getMinOrderTimeStr()));
        if (salesOrder.getMinOrderTimeStr() != null) {
            if (salesOrder.getMaxOrderTimeStr() == null) {
                salesOrder.setMaxOrderTime(new Date());
            } else {
                salesOrder.setMaxOrderTime(DateUtil.strToDate(salesOrder.getMaxOrderTimeStr()));
            }
        }
        try {
            count = salesOrderMapper.count(salesOrder);
        } catch (Exception e) {
            System.out.println("SalesOrderService.count()：报错" + e.getMessage());
        }
        return count;
    }

    @Override
    public List<SalesOrder> getList(SalesOrder salesOrder) {
        ApiLog apiLog = new ApiLog();
        apiLog.setCreateTime(new Date());
        apiLog.setMethodName("SalesOrderService.getList()");
        List<SalesOrder> list = new ArrayList<>();
        salesOrder.setMinOrderTime(DateUtil.strToDate(salesOrder.getMinOrderTimeStr()));
        if (salesOrder.getMinOrderTime() != null) {
            if (salesOrder.getMaxOrderTimeStr() == null) {
                salesOrder.setMaxOrderTime(new Date());
            } else {
                salesOrder.setMaxOrderTime(DateUtil.strToDate(salesOrder.getMaxOrderTimeStr()));
            }
            if (salesOrder.getMaxOrderTime().before(salesOrder.getMinOrderTime())) {
                salesOrder.setMaxOrderTime(null);
            }
        }

        try {
            list = salesOrderMapper.getList(salesOrder);
            for (SalesOrder order : list) {
                order.setSellTimeStr(DateUtil.getDateStr(order.getSellTime()));
            }
            apiLog.setSuccess(MyConfig.SUCCESS);
        } catch (Exception e) {
            apiLog.setSuccess(MyConfig.FAILD);
            apiLog.setMsg(e.getMessage().toString().length() > 100 ? e.getMessage().toString().substring(0, 99) : e.getMessage().toString());
        } finally {
            apiLogMapper.save(apiLog);
        }
        return list;
    }

    /**
     * 存入redis
     *
     * @param ids
     * @return
     */
    @Override
    public Result saleProduct(String ids) {
        String redisIds = "";//redis中的id
        String key = "";//reids中的key
        SaleProductIds keys = SaleProductIds.keys;
        SaleProductIds saleProductIds = SaleProductIds.board;
        try {
            key = redisService.get(keys, "", String.class);
            if (!StringUtils.isEmpty(key)) {
                redisIds = redisService.get(saleProductIds, key, String.class);
            }
        } catch (Exception e) {
            System.out.println("从redis 获取key--redisIds失败" + e.getMessage());
        }
        if (StringUtils.isEmpty(key)) {
            key = System.currentTimeMillis() + "";
        }
        //如果缓存中有
        if (!StringUtils.isEmpty(redisIds)) {
            String[] idsArray = ids.split(",");
            String[] redisIdsArray = redisIds.split(",");
            //合并数组
            String[] array = new String[idsArray.length + redisIdsArray.length];
            System.arraycopy(redisIdsArray, 0, array, 0, redisIdsArray.length);
            System.arraycopy(idsArray, 0, array, redisIdsArray.length, idsArray.length);
            //数组去重
            ids = quchong(array);
            ids = ids.substring(1, ids.length() - 1);
        }
        // key为时间戳  当作当前出库单的key
        Boolean keySet = redisService.set(keys, "", key);
        Boolean idsSet = redisService.set(saleProductIds, key, ids);
        if (idsSet && keySet) {
            return Result.ok(ids);
        } else {
            return Result.ofMessage(400, "存放ids失败");
        }

    }

    private String quchong(String[] array) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (String str : array) {
            map.put(str, str);
        }
        //返回一个包含所有对象的指定类型的数组
        String[] newArrStr = map.keySet().toArray(new String[map.size()]);
        return Arrays.toString(newArrStr);
    }

    @Override
    public Result saleForm() {
        String redisIds = "";//redis中的id
        String key = "";//reids中的key
        SaleProductIds keys = SaleProductIds.keys;
        SaleProductIds saleProductIds = SaleProductIds.board;
        try {
            key = redisService.get(keys, "", String.class);
            if (!StringUtils.isEmpty(key)) {
                redisIds = redisService.get(saleProductIds, key, String.class);

            } else {
                return Result.ofMessage(400, "添加的商品已经过期，请重新添加");
            }

            if (StringUtils.isEmpty(redisIds)) {
                return Result.ofMessage(400, "添加的商品已经过期，请重新添加");
            }
            String[] split = redisIds.split(",");
            List<Integer> list = new ArrayList<>();
            for (int i = 0; i < split.length; i++) {
                list.add(Integer.valueOf(split[i].trim()));
            }
            List<Product> productList = productMapper.getSaleProductList(list);
            return Result.ok(productList);
        } catch (Exception e) {
            System.out.println("错误：" + e.getMessage());
            return Result.ofMessage(400, e.getMessage());
        }

    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result saleFormCreate(JSONObject jsonObject) {
        String orderName = jsonObject.getString("ckdm");
        if (StringUtils.isEmpty(orderName)) {
            orderName = "恒丰陶瓷出库单";
        }
        JSONArray tabledata = jsonObject.getJSONArray("tabledata");


        SalesOrder salesOrder = new SalesOrder();
        String sellTimeStr = jsonObject.getString("ldrq");
        //如果没有选择时间-使用当天时间作为录单日期
        if (StringUtils.isEmpty(sellTimeStr)) {
            salesOrder.setSellTime(new Date());
        } else {
            salesOrder.setSellTime(DateUtil.strToDate(sellTimeStr));
        }
        salesOrder.setCustomerName(jsonObject.getString("gmdw"));
        salesOrder.setCreateTime(new Date());
        salesOrder.setMadeBy(jsonObject.getString("zdr"));
        salesOrder.setPassBy(jsonObject.getString("jsr"));
        salesOrder.setOrderNumber(jsonObject.getString("bh"));
        salesOrder.setOrderName(orderName);
        salesOrder.setZy(jsonObject.getString("zy"));
        salesOrder.setJgkh(jsonObject.getString("jgkh"));

        //id
        String salesOrderId = UUID.randomUUID().toString();
        salesOrder.setId(salesOrderId);
        //customerID
        String customerId = jsonObject.getString("customerId");
        salesOrder.setCustomerId(customerId);

        BigDecimal totalMoney = new BigDecimal(0);
        List<SalesOrderChild> list = new ArrayList<>();
        for (int i = 0; i < tabledata.size(); i++) {
            JSONArray array = (JSONArray) tabledata.get(i);
            //array  [6]  0:id  1:number 2.price  3: amount 4:jianshu  5 ：remark
            Integer id = Integer.valueOf(array.getString(0).trim());
            Product product = productMapper.getProductById(id);

            Integer number = Integer.valueOf(array.getString(1).trim());
            //金额
            String amountStr = array.getString(3).trim();
            BigDecimal amount = new BigDecimal(0);
            if (StringUtils.isEmpty(amountStr)) {
                amount = array.getBigDecimal(2).multiply(new BigDecimal(number));
            } else {
                amount = new BigDecimal(amountStr);
            }
            totalMoney = totalMoney.add(amount);
            String jianshu = array.getString(4);
            String remark = array.getString(5);
            Integer inventoryNumber = product.getInventoryNumber();
            if (inventoryNumber == null) inventoryNumber = 0;
            //todo 先不校验库存
            SalesOrderChild salesOrderChild = new SalesOrderChild(salesOrderId);
            salesOrderChild.setPrice(array.getBigDecimal(2));
            salesOrderChild.setAmount(amount);
            salesOrderChild.setJianshu(jianshu);
            salesOrderChild.setRemark(remark);
            salesOrderChild.setProductId(id);
            salesOrderChild.setNumber(number);
            list.add(salesOrderChild);
        }
        salesOrder.setChildList(list);
        String totalMoneyStr = jsonObject.getString("totalMoney");
        if (StringUtils.isEmpty(totalMoneyStr)) {
            salesOrder.setAmount(totalMoney);
        } else {
            salesOrder.setAmount(new BigDecimal(totalMoneyStr));
        }

        //todo 客户加 消费
        Customer customer = customerMapper.getById(salesOrder.getCustomerId());
        BigDecimal costMoney = customer.getCostMoney();
        customer.setCostMoney(costMoney.add(salesOrder.getAmount()));
        try {
            customerMapper.addCost(customer);
            salesOrderMapper.save(salesOrder);
            //todo 商品出库  逻辑
            salesOrderChildMapper.save(list);
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//进行手动回滚
            System.out.println("错误：" + e.getMessage());
            return Result.ofMessage(400, e.getMessage());
        }
        return Result.ok(salesOrder);
    }


    @Override
    public SalesOrder getSaleFormById(String id) {
        SalesOrder salesOrder = null;
        if (StringUtils.isEmpty(id)) {
            return salesOrder;
        }
        salesOrder = salesOrderMapper.getById(id);
        salesOrder.setSellTimeStr(DateUtil.getDateStr(salesOrder.getSellTime()));
        List<SalesOrderChild> list = salesOrderChildMapper.getByParentId(id);
        for (SalesOrderChild salesOrderChild : list) {
            salesOrderChild.setProduct(productMapper.getProductById(salesOrderChild.getProductId()));
        }
        int size = list.size();

        if (size<10){
            SalesOrderChild salesOrderChild = new SalesOrderChild();
            Product product = new Product();
            product.setProductName(" ");
            product.setSpecifications(" ");
            product.setColor(" ");
            product.setUnit(" ");
            product.setProductNumber(" ");
            product.setPrice(null);
            salesOrderChild.setProduct(product);
            /*
            *  <td th:text="${child.product.productName == null ? '' : child.product.productName}">Tanmay</td>
                <td th:text="${child.product.productNumber == null ? '' : child.product.productNumber}">Bangalore</td>
                <td th:text="${child.product.specifications == null ? '' : child.product.specifications}">560001</td>
                <td th:text="${child.product.color == null ? '' : child.product.color}">560001</td>
                <td th:text="${child.product.unit == null ? '' : child.product.unit}">560001</td>
            * */
            for (int i =0;i<10-size;i++){
                list.add(salesOrderChild);
            }
        }
        salesOrder.setChildList(list);
        return salesOrder;
    }


    /**
     * 清空redis商品出库
     */
    @Override
    public void clean() {
        SaleProductIds keys = SaleProductIds.keys;
        SaleProductIds saleProductIds = SaleProductIds.board;
        SaleProductIds customer = SaleProductIds.customerBuy;
        if (redisService.exists(customer, "ID")) {
            redisService.delete(customer, "ID");
        }
        if (redisService.exists(keys, "")) {
            String key = redisService.get(keys, "", String.class);
            if (redisService.exists(saleProductIds, key)) {
                redisService.delete(saleProductIds, key);
            }
            redisService.delete(keys, "");
        }
    }


    @Override
    public List<SalesOrderChild> getProduct(String id) {
        List<SalesOrderChild> list = salesOrderChildMapper.getByParentId(id);
//        List<SalesOrderChild> notCkOrder = new ArrayList<>();
        int state = 0;
        for (SalesOrderChild salesOrderChild : list) {
            if (salesOrderChild.getCkStatus() == 0) {
//              salesOrderChild.setProduct();
                salesOrderChild.setWarehousemanageList(warehousemangeService.getListByProductId(salesOrderChild.getProductId()));
//                notCkOrder.add(salesOrderChild);
                state++;
            }
        }
        if (state==0){
            updateCkStatus(id);
        }
        return list;
    }
    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result saleOrderChildCk(Integer id, Integer warehouseId, Integer saleNumber) {
        SalesOrderChild salesOrderChild = salesOrderChildMapper.getById(id);
        Integer productId = salesOrderChild.getProductId();
        Integer number = salesOrderChild.getNumber();
        if (number == null) {
            return Result.ofMessage(400, "数量为空");
        }
        if (number < saleNumber) {
            return Result.ofMessage(400, "出库超标");
        }

        Integer ckNumber = salesOrderChild.getCkNumber();
        Integer ckStatus = salesOrderChild.getCkStatus();
        Warehousemanage warehousemanage = warehousemangeMapper.getById(warehouseId);
        Integer wareNumber = warehousemanage.getNumber();
        if (wareNumber == null) {
            return Result.ofMessage(400, "库存为空");
        }
        if (wareNumber < saleNumber) {
            return Result.ofMessage(400, "库存不足");
        }
        Integer wareSaleNumber = warehousemanage.getSaleNumber();
        if (wareSaleNumber == null) wareSaleNumber = 0;
        warehousemanage.setNumber(wareNumber - saleNumber);
        warehousemanage.setSaleNumber(wareSaleNumber + saleNumber);
        warehousemanage.setUpdateTime(new Date());

        Product product = productMapper.getProductById(productId);

        Integer productNumber = product.getInventoryNumber();
        if (productNumber == null) {
            return Result.ofMessage(400, "库存为空");
        }
        if (productNumber < saleNumber) {
            return Result.ofMessage(400, "库存不足");
        }
        Integer productSaleNumber = product.getSaleNumber();
        if (productSaleNumber == null) productSaleNumber = 0;
        product.setSaleNumber(productSaleNumber + saleNumber);
        product.setInventoryNumber(productNumber - saleNumber);

        salesOrderChild.setCkNumber(ckNumber + saleNumber);
//        if (salesOrderChild.getCkNumber().equals(number+salesOrderChild.getReturnNumber())) {
//            salesOrderChild.setCkStatus(1);
//        }

        ProductInstorge productInstorge = new ProductInstorge();
        productInstorge.setProductId(salesOrderChild.getProductId());
        productInstorge.setState(1);
        productInstorge.setCreateTime(new Date());
        productInstorge.setAddress(warehousemanage.getAddress());
        productInstorge.setChmc(warehousemanage.getChmc());
        productInstorge.setJianshu(salesOrderChild.getJianshu());
        productInstorge.setNumber(saleNumber);
        productInstorge.setWarehouseId(warehousemanage.getParentId());
        productInstorge.setPrice(product.getPrice());
        productInstorge.setTotalMoney(product.getPrice().multiply(new BigDecimal(saleNumber)));


        try {
            salesOrderChildMapper.ck(salesOrderChild);
            productMapper.sale(product);
            warehousemangeMapper.sale(warehousemanage);
            productInstorgeMapper.save(productInstorge);
            return Result.ok();
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//进行手动回滚
            System.out.println("错误信息：" + e.getMessage());
            return Result.ofMessage(401, e.getMessage());
        }
    }

    @Override
    public void updateCkStatus(String id) {
        salesOrderMapper.updateCkStatus(id);
    }

    @Override
    public Integer selectCurOrderNum() {
        return salesOrderMapper.selectCurOrderNum();
    }

    @Override
    public Long selectLastPayment() {

        return salesOrderMapper.selectLastPayment();
    }

    @Override
    public Long selectCURPayment() {
        return salesOrderMapper.selectCURPayment();
    }

    @Override
    public Integer selectLastOrderNum() {
        return salesOrderMapper.selectLastOrderNum();
    }

    @Override
    public Integer selectCurRefundOrder() {
        return salesOrderMapper.selectCurRefundOrder();
    }

    @Override
    public Integer selectLastRefundOrder() {
        return salesOrderMapper.selectLastRefundOrder();
    }

    @Override
    public Integer selectDayOrderNum() {
        return salesOrderMapper.selectDayOrderNum();
    }

    @Override
    public Result saleOrderChildTh(Integer id, Integer returnNumber) {
        SalesOrderChild salesOrderChild = salesOrderChildMapper.getById(id);
        Integer productId = salesOrderChild.getProductId();
        Integer number = salesOrderChild.getNumber();
        if (number == null) {
            return Result.ofMessage(400, "数量为空");
        }
        if (number < returnNumber) {
            return Result.ofMessage(400, "退貨超標");
        }
        Integer baseReturnNumber = salesOrderChild.getReturnNumber();
        if (baseReturnNumber == null) baseReturnNumber =0;
        salesOrderChild.setReturnNumber(baseReturnNumber + returnNumber);
        salesOrderChild.setNumber(number-returnNumber);
        BigDecimal baseAmount = salesOrderChild.getAmount();
        //退多少钱
        Product product = productMapper.getProductById(productId);
        BigDecimal price = salesOrderChild.getPrice();
        BigDecimal thMoney = price.multiply(new BigDecimal(returnNumber));
        salesOrderChild.setAmount(baseAmount.subtract(thMoney));
        //合计金额更改
        SalesOrder salesOrder = salesOrderMapper.getById(salesOrderChild.getParentId());
        BigDecimal baseParentAmount = salesOrder.getAmount();
        salesOrder.setAmount(baseParentAmount.subtract(thMoney));

        String customerId = salesOrder.getCustomerId();
        Customer customer = customerMapper.getById(customerId);
        BigDecimal costMoney = customer.getCostMoney();
        customer.setCostMoney(costMoney.subtract(thMoney));

        //退货单
        SalesOrderReturnChild salesOrderReturnChild = new SalesOrderReturnChild();
        SalesOrderReturnChild baseSalesOrderReturnChild = salesOrderReturnChildMapper.getByProductIdAndParentId(productId,salesOrderChild.getParentId());
        try {
            // todo
            if (baseSalesOrderReturnChild!=null){
                salesOrderReturnChild = baseSalesOrderReturnChild;
                Integer baseNumber = salesOrderReturnChild.getNumber();
                if (baseNumber == null) baseNumber = 0;
                salesOrderReturnChild.setNumber(returnNumber+baseNumber);
                salesOrderReturnChild.setAmount(salesOrderReturnChild.getAmount().add(thMoney));
                salesOrderReturnChildMapper.updatenumber(salesOrderReturnChild);
            }else {
                salesOrderReturnChild.setParentId(salesOrderChild.getParentId());
                salesOrderReturnChild.setNumber(returnNumber);//退货数量
                salesOrderReturnChild.setAmount(thMoney);
                salesOrderReturnChild.setProductId(salesOrderChild.getProductId());
                salesOrderReturnChild.setPrice(price);
                salesOrderReturnChildMapper.save(salesOrderReturnChild);
            }
            salesOrderChildMapper.th(salesOrderChild);
            salesOrderMapper.th(salesOrder);
            customerMapper.addCost(customer);
            return Result.ok();
        } catch (Exception e) {
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//进行手动回滚
            System.out.println("错误信息：" + e.getMessage());
            return Result.ofMessage(401, e.getMessage());
        }
    }

    @Transactional(rollbackFor = Exception.class)
    @Override
    public Result delete(String id) {
        SalesOrder salesOrder = salesOrderMapper.getById(id);
        BigDecimal amount = salesOrder.getAmount();
        String customerId = salesOrder.getCustomerId();
        Customer customer = customerMapper.getById(customerId);
        BigDecimal costMoney = customer.getCostMoney();
        customer.setCostMoney(costMoney.subtract(amount));
        try {
            customerMapper.addCost(customer);
            salesOrderMapper.delete(id);
            salesOrderChildMapper.delete(id);
            return Result.ok();
        }catch (Exception e){
            TransactionAspectSupport.currentTransactionStatus().setRollbackOnly();//进行手动回滚
            System.out.println(e.getMessage());
            return Result.ofMessage(400,e.getMessage());
        }
    }

    @Override
    public SalesOrder getReturnSaleForm(String id) {
        SalesOrder salesOrder = null;
        if (StringUtils.isEmpty(id)) {
            return salesOrder;
        }
        salesOrder = salesOrderMapper.getById(id);
        salesOrder.setSellTimeStr(DateUtil.getDateStr(salesOrder.getSellTime()));
        List<SalesOrderReturnChild> list = salesOrderReturnChildMapper.getByParentId(id);
        for (SalesOrderReturnChild salesOrderChild : list) {
            salesOrderChild.setProduct(productMapper.getProductById(salesOrderChild.getProductId()));
        }
        int size = list.size();

        if (size<10){
            SalesOrderReturnChild salesOrderChild = new SalesOrderReturnChild();
            Product product = new Product();
            product.setProductName(" ");
            product.setSpecifications(" ");
            product.setColor(" ");
            product.setUnit(" ");
            product.setProductNumber(" ");
            product.setPrice(null);
            salesOrderChild.setProduct(product);
            for (int i =0;i<10-size;i++){
                list.add(salesOrderChild);
            }
        }
        salesOrder.setReturnChildList(list);
        return salesOrder;
    }

    @Override
    public Result saleOrderCkwc(Integer id) {
        try {
            int res = salesOrderChildMapper.saleOrderCkwc(id);
            return Result.ok();
        }catch (Exception e){
            return Result.ofMessage(400,e.getMessage());
        }


    }
}
