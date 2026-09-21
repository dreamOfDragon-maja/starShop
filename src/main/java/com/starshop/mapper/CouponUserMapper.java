package com.starshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starshop.pojo.entity.CouponUser;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface CouponUserMapper extends BaseMapper<CouponUser> {

    // 批量更新同一个字段
    void batchUpdateSameField(@Param("idList") List<Long> idList, @Param("valueList") List<Integer> valueList);
}
