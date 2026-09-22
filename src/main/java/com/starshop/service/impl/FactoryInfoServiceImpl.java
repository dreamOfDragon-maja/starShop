package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.constant.DataConstant;
import com.starshop.constant.MessageConstant;
import com.starshop.mapper.FactoryInfoMapper;
import com.starshop.pojo.entity.FactoryInfo;
import com.starshop.pojo.vo.FactoryInfoVO;
import com.starshop.result.Result;
import com.starshop.service.FactoryInfoService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class FactoryInfoServiceImpl extends ServiceImpl<FactoryInfoMapper, FactoryInfo> implements FactoryInfoService {

    @Resource
    private CopyMapper copyMapper;

    /**
     * 查询单条工厂信息
     * @return
     */
    @Override
    public Result<Object> getFactoryInfo() {
        FactoryInfo factoryInfo = query().last("LIMIT " + DataConstant.ONE_INT).one();
        if (Objects.isNull(factoryInfo)){
            return Result.error(MessageConstant.TOM_CAT_ERROR);

        }
        FactoryInfoVO factoryInfoVO = copyMapper.factoryInfoToFactoryInfoVO(factoryInfo);
        return Result.success(factoryInfoVO);

    }
}
