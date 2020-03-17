package com.jesper.hftc.entity;

import com.jesper.model.BaseObject;
import lombok.Data;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.Date;

/**
 * ClassName:ProductInstorge
 * Package:com.jesper.hftc.entity
 * Description:
 *
 * @date:2020/3/12 14:06
 * @author:廖凡
 */
@Data
public class ProductInstorge extends BaseObject {
    private Integer id;

    private Integer productId;

    private Integer warehouseId;

    private String  address;

    private String chmc;

    private Integer number;

    private String jianshu;

    private BigDecimal price;

    private BigDecimal totalMoney;

    private Integer state ;

    private String remark;

    private Date createTime;

    private String createTimeStr;


    private String productName;

    private String ck;
}
