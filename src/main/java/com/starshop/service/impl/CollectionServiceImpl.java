package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.mapper.CollectionMapper;
import com.starshop.pojo.entity.ProductCollection;
import com.starshop.service.CollectionService;
import org.springframework.stereotype.Service;

@Service
public class CollectionServiceImpl extends ServiceImpl<CollectionMapper, ProductCollection> implements CollectionService {
}
