package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.common.result.CursorCommonEntity;
import com.starshop.common.result.CursorCommonResult;
import com.starshop.common.utils.BloomFilterUtils;
import com.starshop.common.utils.JacksonUtils;
import com.starshop.constant.DataConstant;
import com.starshop.constant.MessageConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.infrastructure.es.document.ProductDocument;
import com.starshop.infrastructure.es.mapstruct.EsCopyMapper;
import com.starshop.infrastructure.es.service.ProductDocumentService;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.infrastructure.redis.connect.StringRedisConnector;
import com.starshop.mapper.ProductMapper;
import com.starshop.pojo.enums.CommonStatus;
import com.starshop.pojo.entity.Product;
import com.starshop.pojo.entity.ProductCollection;
import com.starshop.pojo.enums.ProductSortTypeEnum;
import com.starshop.pojo.vo.SimpleProductVO;
import com.starshop.properties.RedisCacheCountProperties;
import com.starshop.properties.RedisCacheTtlProperties;
import com.starshop.result.Result;
import com.starshop.service.CollectionService;
import com.starshop.service.ProductRedisCacheService;
import com.starshop.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl extends ServiceImpl<ProductMapper, Product> implements ProductService {

    private final ProductRedisCacheService productRedisCacheService;

    private final RedisCacheCountProperties redisCacheCountProperties;

    private final ProductMapper productMapper;

    private final CopyMapper copyMapper;

    private final BloomFilterUtils bloomFilterUtils;

    private final RedisCacheTtlProperties redisCacheTtlProperties;

    private final CollectionService collectionService;

    private final ProductDocumentService productDocumentService;

    private final EsCopyMapper esCopyMapper;

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
     * 游标查询指定分类下的简单商品列表, 通过 es 进行查询
     * @param cursorCommonEntity 游标参数实体
     * @return 游标返回实体
     */
    @Override
    public CursorCommonResult getCategorySimpleProduct(CursorCommonEntity cursorCommonEntity, Long categoryId, boolean isFirstCategoryId) {
        //获取数据
        String sortType = cursorCommonEntity.getSortType();
        String sortValue = cursorCommonEntity.getSortValue();
        Long sortId = cursorCommonEntity.getSortId();
        Integer querySize = cursorCommonEntity.getQuerySize();

        //将sortType转成枚举
        ProductSortTypeEnum productSortTypeEnum = ProductSortTypeEnum.getByValue(sortType);

        //对sortValue进行格式化
        ProductSortTypeEnum.filterFormatSortValue(productSortTypeEnum,sortValue);

        //通过es查询
        List<ProductDocument> productDocuments = productDocumentService.searchByCursorByCategoryId(
                querySize, productSortTypeEnum, sortValue, sortId, categoryId, isFirstCategoryId);

        return getCursorCommonResult(productDocuments, querySize, productSortTypeEnum, sortType);

    }

    /**
     * 游标结果封装方法
     * @param productDocuments 在商品文档服务 查询出来的 原始商品文档
     * @param querySize 查询数量
     * @param productSortTypeEnum 商品排序种类枚举
     * @param sortType 排序种类字符串
     * @return 游标结果
     */
    private CursorCommonResult getCursorCommonResult(List<ProductDocument> productDocuments, Integer querySize, ProductSortTypeEnum productSortTypeEnum, String sortType) {
        boolean isEnd = false;
        //es中无数据停止查询
        if (productDocuments.isEmpty()) {
            return CursorCommonResult.builder()
                    .isEnd(true)
                    .list(Collections.emptyList())
                    .build();
        }

        if (querySize > productDocuments.size()) {
            isEnd = true;
        }

        //获取最后一个商品
        ProductDocument productDocument = productDocuments.get(productDocuments.size() - 1);
        String sortValueByProductDocument = ProductSortTypeEnum.getSortValueByProductDocument(productSortTypeEnum, productDocument);
        //返回给前端以再次进行es查询
        CursorCommonEntity cursorCommonEntity = CursorCommonEntity.builder()
                .sortType(sortType)
                .sortValue(sortValueByProductDocument)
                .sortId(productDocument.getId())
                .querySize(querySize)
                .build();

        List<SimpleProductVO> simpleProductVOS = productDocuments.stream().map(esCopyMapper::ProductDocumentToSimpleProductVO).toList();
        return CursorCommonResult.builder()
                .isEnd(isEnd)
                .list(simpleProductVOS)
                .cursorCommonEntity(cursorCommonEntity)
                .build();
    }

    /**
     * 获取商品详细信息
     * @param productId
     * @param userId
     * @return
     */
    @Override
    @SuppressWarnings("unchecked")
    public Result<?> getProductDetail(String productId, String userId) {
        if (StringUtils.isBlank(productId)) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        //通过布隆过滤器判断是否存在
        if (!bloomFilterUtils.contains(Long.valueOf(productId))){
            return Result.error(MessageConstant.DATA_ERROR);
        }
        if (StringUtils.isBlank(userId)) {
            userId = DataConstant.NEGATIVE_ONE_STRING;
        }
        String productDetailKey = RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.DETAIL + productId;
        String productCollectionKey = RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.COLLECTION + productId;
        //从redis中查询信息
        Map<String, Object> productDetailMap = RedisConnector.opsForHash().entries(productDetailKey);
        Set<Object> userIdSet =(Set<Object>)RedisConnector.opsForValue().get(productCollectionKey);
        //如果redis无信息，回到数据库中查询
        if (productDetailMap.isEmpty()) {
            Product product = productMapper.selectByProductId(productId, userId);
            //数据不存在
            if (Objects.isNull(product)) {
                //在redis中写入空对象防缓存穿透
                StringRedisConnector.opsForHash().putAll(productDetailKey,Map.of(Product.Fields.id,productId));
                return Result.error(MessageConstant.DATA_ERROR);
            }
            //数据存在,处理收藏问题
            if (!StringUtils.equals(product.getIsCollection().toString(), CommonStatus.INACTIVE.getNumber().toString())) {
                product.setIsCollection(CommonStatus.ACTIVE.getNumber());
            }
            //转成map存入redis
            Map<String, Object> productDetailResultMap = JacksonUtils.toMap(product);
            productDetailResultMap.put(Product.Fields.isCollection,CommonStatus.INACTIVE.getNumber());
            RedisConnector.opsForHash().putAll(productDetailKey,productDetailResultMap);
            //设置过期时间
            StringRedisConnector.expire(productDetailKey,redisCacheTtlProperties.getProductDetailTtl(), TimeUnit.SECONDS);

            return Result.success(product);
        }
        //如果redis一开始就有数据，有可能是空对象
        if (productDetailMap.size() == DataConstant.ONE_INT) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        //解决默认的不收藏问题
        if (CollectionUtils.isEmpty(userIdSet)) {
            //从数据库查
            List<ProductCollection> productCollectionList = collectionService.lambdaQuery().eq(ProductCollection::getProductId, productId).list();
            userIdSet = productCollectionList.stream().map(ProductCollection::getUserId).collect(Collectors.toSet());
            //写入redis并为商品设置过期时间
            RedisConnector.opsForValue().set(productCollectionKey,userIdSet);
            RedisConnector.expire(productDetailKey,redisCacheTtlProperties.getProductCollectionTtl(),TimeUnit.SECONDS);

        }
        //为每一位用户设置收藏
        Product resultProduct = JacksonUtils.fromMap(productDetailMap, Product.class);
        if (userIdSet.contains(Long.valueOf(userId))) {
            resultProduct.setIsCollection(CommonStatus.ACTIVE.getNumber());
        } else {
            resultProduct.setIsCollection(CommonStatus.INACTIVE.getNumber());
        }
        return Result.success(resultProduct);
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
