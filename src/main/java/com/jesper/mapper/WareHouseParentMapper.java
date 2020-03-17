package com.jesper.mapper;

import com.jesper.hftc.entity.WareHouseParent;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ClassName:WareHouseParentMapper
 * Package:com.jesper.mapper
 * Description:
 *
 * @date:2020/3/12 18:08
 * @author:廖凡
 */
@Mapper
public interface WareHouseParentMapper {
    List<WareHouseParent> getList();
    List<WareHouseParent> getLists(WareHouseParent wareHouseParent);
    void add(WareHouseParent wareHouseParent);

    void delete(Integer id);

    void update(WareHouseParent wareHouseParent);

    WareHouseParent getById(Integer id);

    int count();
}
