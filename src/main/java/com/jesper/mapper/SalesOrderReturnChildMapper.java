package com.jesper.mapper;

import com.jesper.hftc.entity.SalesOrderReturnChild;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * ClassName:SalesOrderReturnChildMapper
 * Package:com.jesper.mapper
 * Description:
 *
 * @date:2020/3/16 20:18
 * @author:廖凡
 */
@Mapper
public interface SalesOrderReturnChildMapper {
    void save(SalesOrderReturnChild salesOrderReturnChild);

    List<SalesOrderReturnChild> getByParentId(String parentId);

    SalesOrderReturnChild getById(Integer id);

    void rk(SalesOrderReturnChild salesOrderChild);

    void delete(String id);

    SalesOrderReturnChild getByProductIdAndParentId(@Param("productId") Integer productId, @Param("parentId")String parentId);

    void updatenumber(SalesOrderReturnChild salesOrderReturnChild);
}
