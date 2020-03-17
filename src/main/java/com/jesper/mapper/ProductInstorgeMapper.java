package com.jesper.mapper;

import com.jesper.hftc.entity.ProductInstorge;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

/**
 * ClassName:ProductInstorgeMapper
 * Package:com.jesper.mapper
 * Description:
 *
 * @date:2020/3/12 14:27
 * @author:廖凡
 */
@Mapper
public interface ProductInstorgeMapper {
    void save(ProductInstorge productInstorge);

    Integer count(ProductInstorge productInstorge);

    List<ProductInstorge> getList(ProductInstorge productInstorge);
}
