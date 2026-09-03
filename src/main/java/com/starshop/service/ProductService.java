package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.entity.Product;
import com.starshop.pojo.entity.ProductDocument;

import java.util.List;

public interface ProductService extends IService<Product> {

    /**
     * 获取热门商品
     * @return
     */
    List<ProductDocument> getHotProduct(Integer limit);
}
