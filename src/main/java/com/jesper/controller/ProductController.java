package com.jesper.controller;

import com.jesper.hftc.config.Result;
import com.jesper.hftc.entity.*;
import com.jesper.model.ResObject;
import com.jesper.redis.RedisService;
import com.jesper.redis.SaleProductIds;
import com.jesper.service.CategoryService;
import com.jesper.service.ProductService;
import com.jesper.service.WarehousemangeService;
import com.jesper.util.Constant;
import com.jesper.util.DateUtil;
import com.jesper.util.PageUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品管理
 *
 * @Author 廖凡
 * @Date 2020/2/17 22:04
 */
@Controller
@SuppressWarnings("all")
public class ProductController {


    @Autowired
    private RedisService redisService;

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WarehousemangeService warehousemangeService;

    @GetMapping("/user/productAdd")
    public String productAdd(Model model, Product product) {
        model.addAttribute("categoryList", categoryService.getCategoryList());
        model.addAttribute("product", product);
        return "product/productAdd";
    }
    @GetMapping("/user/productDetail")
    public String productDetail(Model model, Integer id) {
        Product product = productService.getProduct(id);
        model.addAttribute("product", product);
        return "product/productDetail";
    }

    //新增商品
    @PostMapping("/user/addProduct")
    public String addProduct(Model model, Product product) {
        //保存product
        productService.add(product);
        return "redirect:productManage_0_0_0";
    }

    @GetMapping("/user/productManage_{pageCurrent}_{pageSize}_{pageCount}")
    public String getProductList(Model model, @PathVariable Integer pageCurrent,
                                 @PathVariable Integer pageSize,
                                 @PathVariable Integer pageCount, Product product) {
        if (pageSize == 0) pageSize = 20;
        if (pageCurrent == 0) pageCurrent = 1;
        if (product.getProductCategoryName() != null) {
            if (product.getProductCategoryName().equals("0")) {
                product.setProductCategoryName(null);
            }
        }
        int rows = productService.count(product);
        if (pageCount == 0) pageCount = rows % pageSize == 0 ? (rows / pageSize) : (rows / pageSize) + 1;
        product.setStart((pageCurrent - 1) * pageSize);
        product.setEnd(pageSize);
        model.addAttribute("categoryList", categoryService.getCategoryList());
        model.addAttribute("productList", productService.getProductList(product));
        model.addAttribute("product", product);
        String pageHTML = PageUtil.getPageContent("productManage_{pageCurrent}_{pageSize}_{pageCount}", pageCurrent, pageSize, pageCount);
        model.addAttribute("pageHTML", pageHTML);
        return "product/productManage";
    }

    @GetMapping("/user/productSale_{pageCurrent}_{pageSize}_{pageCount}")
    public String productSale(Model model, @PathVariable Integer pageCurrent,
                              @PathVariable Integer pageSize,
                              @PathVariable Integer pageCount, Product product) {
        if (pageSize == 0) pageSize = 20;
        if (pageCurrent == 0) pageCurrent = 1;
        int rows = productService.count(product);
        if (pageCount == 0) pageCount = rows % pageSize == 0 ? (rows / pageSize) : (rows / pageSize) + 1;
        product.setStart((pageCurrent - 1) * pageSize);
        product.setEnd(pageSize);
        model.addAttribute("categoryList", categoryService.getCategoryList());
        model.addAttribute("productList", productService.getProductList(product));
        model.addAttribute("product", product);
        String ids = "";
        if (redisService.exists(SaleProductIds.keys, "")) {
            String key = redisService.get(SaleProductIds.keys, "", String.class);
            if (redisService.exists(SaleProductIds.board, key)) {
                ids = redisService.get(SaleProductIds.board, key, String.class);
            }
        }
        model.addAttribute("ids", ids);
        String pageHTML = PageUtil.getPageContent("productSale_{pageCurrent}_{pageSize}_{pageCount}", pageCurrent, pageSize, pageCount);
        model.addAttribute("pageHTML", pageHTML);
        return "product/productSale";
    }

    @GetMapping("/user/productListBycatoryName_{pageCurrent}_{pageSize}_{pageCount}")
    public String getProductListBycatoryName(Model model, String catoryName, @PathVariable Integer pageCurrent,
                                             @PathVariable Integer pageSize,
                                             @PathVariable Integer pageCount) {
        model.addAttribute("categoryList", categoryService.getCategoryList());
        model.addAttribute("productList", productService.getProductListBy(catoryName, (pageCount - 1) * pageSize, pageSize));
        return "product/productManage";
    }

    @GetMapping("/user/productEdit")
    public String productEdit(Model model, Integer id) {
        Product product = productService.getProduct(id);

        model.addAttribute("inwarehousemanageList", warehousemangeService.getInwarehousemanageList(id));
        model.addAttribute("warehousemanageList", warehousemangeService.getListByProductId(id));
        model.addAttribute("product", product);
        model.addAttribute("categoryList", categoryService.getCategoryList());
        return "product/productEdit";
    }

    @PostMapping("/user/productEdit")
    public String productEdit(Product product) {

        productService.edit(product);
        return "redirect:productManage_0_0_0";
    }

    @ResponseBody
    @PostMapping("/user/productDelete")
    public Result productDelete(Integer id) {
        Result result = productService.delete(id);
        return result;
    }

    @GetMapping("/user/categotyManage_{pageCurrent}_{pageSize}_{pageCount}")
    public String productCategoryManage(Model model, Category category, @PathVariable Integer pageCurrent,
                                        @PathVariable Integer pageSize,
                                        @PathVariable Integer pageCount) {
        model.addAttribute("categoryList", categoryService.getCategoryList());
        model.addAttribute("category", category);
        String pageHTML = PageUtil.getPageContent("getProductList_{pageCurrent}_{pageSize}_{pageCount}?productName=" + category.getName(), pageCurrent, pageSize, pageCount);
        model.addAttribute("pageHTML", pageHTML);
        return "product/productCategory";
    }

    @GetMapping("/user/categoryEdit")
    public String productCategoryEdit(Model model, Category category) {
        model.addAttribute("category", category);
        return "product/categoryEdit";
    }

    @PostMapping("/user/categoryAdd")
    public String productCategoryAdd(Category category) {
        categoryService.add(category);
        return "redirect:categotyManage_0_0_0";
    }

    @ResponseBody
    @PostMapping("/user/categoryDelete")
    public ResObject categoryDelete(Category category) {
        categoryService.delete(category.getId());
        return new ResObject<Object>(Constant.Code01, Constant.Msg01, null, null);
    }

    @ResponseBody
    @PostMapping("/user/productLoss")
    public Result loss(Integer id, Integer warehouseId, Integer lossNumber,BigDecimal price,String jianshu,BigDecimal totalMoney) {
        return productService.loss(id, lossNumber, warehouseId,price,jianshu,totalMoney);
    }

    @ResponseBody
    @PostMapping("/user/productSale")
    public Result sale(Integer id, Integer warehouseId, Integer saleNumber,BigDecimal price,String jianshu,BigDecimal totalMoney) {
        return productService.sale(id, saleNumber, warehouseId,price,jianshu,totalMoney);
    }

    @ResponseBody
    @PostMapping("/user/productInStorage")
    public Result inStorage(Integer id, Integer warehouseId, Integer number, BigDecimal price,String jianshu,BigDecimal totalMoney) {
        return productService.inStorage(id, number, warehouseId,price,jianshu,totalMoney);
    }

    @GetMapping("user/productInstorge")
    public String productInstorge(Model model, Product product, ProductInstorge productInstorge) {
        List<WareHouseParent> wareHouseParentList = warehousemangeService.getWareHouseParentList();
        product = productService.getProduct(product.getId());
        model.addAttribute("product", product);
        model.addAttribute("wareHouseParentList", wareHouseParentList);
        model.addAttribute("msg", null);
        model.addAttribute("productInstorge", productInstorge);
        return "product/instorge";
    }

    @PostMapping("user/instorgeProduct")
    public String instorgeProduct(Model model, ProductInstorge productInstorge) {
        List<WareHouseParent> wareHouseParentList = warehousemangeService.getWareHouseParentList();
        model.addAttribute("wareHouseParentList", wareHouseParentList);
        Product product = productService.getProduct(productInstorge.getProductId());
        model.addAttribute("product", product);
        model.addAttribute("productInstorge", productInstorge);
        if (productInstorge.getNumber()==null){
            productInstorge.setNumber(0);
        }
        if (productInstorge.getChmc()==null || productInstorge.getChmc()==""){
            model.addAttribute("msg", "请输入存货名称");
            return "product/instorge";
        }
        if (productInstorge.getAddress()==null || productInstorge.getAddress()==""){
            model.addAttribute("msg", "请输入货位");
            return "product/instorge";
        }
        if (productInstorge.getJianshu()==null || productInstorge.getJianshu()==""){
            model.addAttribute("msg", "请输入件数");
            return "product/instorge";
        }
        if (productInstorge.getTotalMoney()==null){
            productInstorge.setTotalMoney(productInstorge.getPrice().multiply(new BigDecimal(productInstorge.getNumber())));
        }
        Result result = productService.instorgeProduct(productInstorge, product);
        if (result.getCode() == 200) {
            return "redirect:instorgeManage_0_0_0";
        } else {
            model.addAttribute("msg", result.getMessage());
            model.addAttribute("product", product);
            return "product/instorge";
        }
    }

    @GetMapping("user/instorgeManage_{pageCurrent}_{pageSize}_{pageCount}")
    public String instorgeManage(Model model, @PathVariable Integer pageCurrent,
                                 @PathVariable Integer pageSize,
                                 @PathVariable Integer pageCount, ProductInstorge productInstorge) {
        if (pageSize == 0) pageSize = 20;
        if (pageCurrent == 0) pageCurrent = 1;
        if (StringUtils.isEmpty(productInstorge.getChmc())){
            productInstorge.setChmc(null);
        }
        productInstorge.setBeginTime(DateUtil.strToDate(productInstorge.getBeginTimeStr()));
        productInstorge.setEndTime(DateUtil.strToDate(productInstorge.getEndTimeStr()));
        int rows = productService.count(productInstorge);
        if (pageCount == 0) pageCount = rows % pageSize == 0 ? (rows / pageSize) : (rows / pageSize) + 1;
        productInstorge.setStart((pageCurrent - 1) * pageSize);
        productInstorge.setEnd(pageSize);
        List<ProductInstorge> productInstorgeList = productService.getProductInstorgeList(productInstorge);

        model.addAttribute("productInstorgeList", productInstorgeList);
        model.addAttribute("productInstorge", productInstorge);
        String pageHTML = PageUtil.getPageContent("instorgeManage_{pageCurrent}_{pageSize}_{pageCount}", pageCurrent, pageSize, pageCount);
        model.addAttribute("pageHTML", pageHTML);
        return "product/instorgeManage";
    }
}
