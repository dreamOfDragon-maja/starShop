package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.mapper.ProductMapper;
import com.starshop.pojo.entity.Product;
import com.starshop.pojo.entity.ProductDocument;
import com.starshop.pojo.vo.SimpleProductVO;
import com.starshop.properties.RedisCacheCountProperties;
import com.starshop.result.Result;
import com.starshop.service.ProductRedisCacheService;
import com.starshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductRedisCacheService productRedisCacheService;

    private final RedisCacheCountProperties redisCacheCountProperties;

    private final ProductMapper productMapper;

    private final CopyMapper copyMapper;

    //TODO es优化
    /**
     * 获取热门商品
     * 按照销量进行排名
     * @return
     */
    @Override
    public List<ProductDocument> getHotProduct(Integer limit) {
        if (limit > redisCacheCountProperties.getHotProductCacheSize()) {
            throw new RuntimeException("超出热门商品最大缓存数量");
        }
        //从redis里取出热门商品及其id
        List<ProductDocument> hotProductList = productRedisCacheService.getHotProduct();
        List<Long> hotProductIdList = productRedisCacheService.getHotProductIdList();
        //TODO 这里后续优化
        if (hotProductList == null) {
            hotProductList = Collections.emptyList();
        }
        if (hotProductIdList == null) {
            hotProductIdList = Collections.emptyList();
        }
        //热门商品本身无序，利用idList进行排序
        HashMap<Long,ProductDocument> map = new HashMap<>(hotProductList.size());
        for (ProductDocument productDocument : hotProductList) {
            map.put(productDocument.getId(),productDocument);
        }
        //创建一个arraylist来存放最终结果
        List<ProductDocument> productDocumentResultList = new ArrayList<>(hotProductIdList.size());
        for (Long id : hotProductIdList) {
            ProductDocument productDocument = map.get(id);
            productDocumentResultList.add(productDocument);
        }
        return productDocumentResultList.stream().limit(limit).toList();
    }

    /**
     * 获取商品简单信息
     * @param productIds
     * @return
     */
    @Override
    public Result<List<SimpleProductVO>> getBriefProduct(String productIds) {
        if (StringUtils.isBlank(productIds)) {
            return Result.success(new ArrayList<>(0));
        }
        //利用stream流处理数据
        List<Long> productIdList = Arrays.stream(StringUtils.split(productIds, ",")).map(Long::valueOf).toList();

        Map<Long,SimpleProductVO> resultMap = new HashMap<>(productIdList.size());
        List<SimpleProductVO> resultList = new ArrayList<>(productIdList.size());

        //先进行初始化,后续value为null查询数据库
        for (Long id : productIdList) {
            resultMap.put(id,null);
        }
        //TODO 后续用es查询

        //创建一个用于放需要查询数据库的商品id
        List<Long> needQueryBySQLIdList = new ArrayList<>();
        resultMap.forEach((key,value)->{
            if(Objects.isNull(value)){
                needQueryBySQLIdList.add(key);
            }
        });
        //通过idlist去查询数据库
        List<Product> list = productMapper.getBriefProduct(needQueryBySQLIdList);

        //TODO使用 mq 消息通知进行数据同步

        list.stream().map(copyMapper::productToSimpleProductVO)
                .forEach(simpleProductVO -> {
                        resultMap.put(simpleProductVO.getId(),simpleProductVO);
                });
        for (int i = 0; i < productIdList.size(); i++) {
            Long id = productIdList.get(i);
            SimpleProductVO simpleProductVO = resultMap.get(id);
            resultList.add(i,simpleProductVO);
        }

        return Result.success(resultList);
    }
}
