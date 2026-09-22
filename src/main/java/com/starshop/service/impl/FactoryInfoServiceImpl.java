package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.mapper.FactoryInfoMapper;
import com.starshop.pojo.entity.FactoryInfo;
import com.starshop.service.FactoryInfoService;
import org.springframework.stereotype.Service;

@Service
public class FactoryInfoServiceImpl extends ServiceImpl<FactoryInfoMapper, FactoryInfo> implements FactoryInfoService {
}
