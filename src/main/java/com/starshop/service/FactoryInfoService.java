package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.entity.FactoryInfo;
import com.starshop.result.Result;

public interface FactoryInfoService extends IService<FactoryInfo> {

    /**
     * 查询单条工厂信息
     * @return
     */
    Result<Object> getFactoryInfo();
}
