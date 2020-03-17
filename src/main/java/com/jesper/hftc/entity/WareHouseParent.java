package com.jesper.hftc.entity;

import com.jesper.model.BaseObject;
import lombok.Data;

/**
 * ClassName:WareHouseParent
 * Package:com.jesper.hftc.entity
 * Description:
 *
 * @date:2020/3/12 18:04
 * @author:廖凡
 */
@Data
public class WareHouseParent extends BaseObject {
    private Integer id;

    private String name;

    private String address;

    private String remark;

    private Integer isDelete;
}
