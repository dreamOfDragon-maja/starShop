package com.starshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.annotation.business.RemoveProductCollectionRedisCacheAnnotation;
import com.starshop.common.result.SimpleCursorCommonEntity;
import com.starshop.common.result.SimpleCursorCommonResult;
import com.starshop.common.utils.DateUtils;
import com.starshop.constant.MessageConstant;
import com.starshop.context.BaseContext;
import com.starshop.mapper.CollectionMapper;
import com.starshop.pojo.entity.ProductCollection;
import com.starshop.pojo.vo.SimpleProductVO;
import com.starshop.result.Result;
import com.starshop.service.CollectionService;
import com.starshop.service.ProductService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@Service
public class CollectionServiceImpl extends ServiceImpl<CollectionMapper, ProductCollection> implements CollectionService {

    //懒加载：防止依赖循环
    @Lazy
    @Resource
    private ProductService productService;

    /**
     * 新增收藏
     * @param productId
     * @return
     */
    @Override
    @RemoveProductCollectionRedisCacheAnnotation
    public Result addCollection(String productId) {
        if (StringUtils.isBlank(productId)) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        String userId = BaseContext.getUserId();
        ProductCollection productCollection = new ProductCollection();
        productCollection.setUserId(Long.valueOf(userId)).setProductId(Long.valueOf(productId));
        boolean isSuccess = save(productCollection);
        if (!isSuccess) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        return Result.success();
    }

    /**
     * 删除收藏
     * 支持单个或批量删除收藏的商品（商品ID以逗号分隔）
     * @param productIds
     * @return
     */
    @Override
    @RemoveProductCollectionRedisCacheAnnotation
    public Result deleteCollection(String productIds) {
        if (StringUtils.isBlank(productIds)) {
            return Result.error(MessageConstant.CONTENT_NOT_EXIST_ERROR);
        }
        String userId = BaseContext.getUserId();
        List<String> productIdList = Arrays.stream(StringUtils.split(productIds, ",")).toList();
        LambdaQueryWrapper<ProductCollection> lambdaQueryWrapper = new LambdaQueryWrapper<ProductCollection>()
                .eq(ProductCollection::getUserId, userId)
                .in(ProductCollection::getProductId, productIdList);
        boolean isSuccess = remove(lambdaQueryWrapper);
        if (!isSuccess) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        return Result.success(productIdList.size());
    }

    /**
     * 获取用户收藏商品列表
     * @param simpleCursorCommonEntity 简单查询请求参数
     * @return 简单商品封装列表
     */
    @Override
    public Result<SimpleCursorCommonResult> getCollectionList(SimpleCursorCommonEntity simpleCursorCommonEntity) {
        String userId = BaseContext.getUserId();
        Integer querySize = simpleCursorCommonEntity.getQuerySize();
        String sortValue = simpleCursorCommonEntity.getSortValue();
        Long sortId = simpleCursorCommonEntity.getSortId();
        boolean isEnd = false;

        //查询当前用户的商品收藏列表，按照收藏时间以及id倒序排
        LambdaQueryChainWrapper<ProductCollection> wrapper = lambdaQuery().eq(ProductCollection::getUserId, userId)
                .orderByDesc(ProductCollection::getCreateTime, ProductCollection::getId)
                .last("limit " + querySize);

        if (StringUtils.isNotBlank(sortValue) && Objects.nonNull(sortId)) {
            LocalDateTime beginTime = DateUtils.parseToLocalDateTime(sortValue);
            //先比较收藏时间，相等则根据sortId排序
            wrapper.apply("(create_time, id) < ({0}, {1})", beginTime, sortId);
        }

        List<ProductCollection> productCollectionList = wrapper.list();
        //没有数据返回结果，isEnd为true
        if (productCollectionList.isEmpty()) {
            SimpleCursorCommonResult simpleCursorCommonResult = SimpleCursorCommonResult.builder()
                    .isEnd(true)
                    .list(Collections.emptyList())
                    .build();
            return Result.success(simpleCursorCommonResult);
        }

        //判断是否查询结束
        if (productCollectionList.size() < querySize) {
            isEnd = true;
        }

        //获取商品id
        StringBuilder stringBuilder = new StringBuilder();
        for (int i = 0; i < productCollectionList.size(); i++) {
            //最后一个不用拼接逗号
            if (i == productCollectionList.size() - 1) {
                stringBuilder.append(productCollectionList.get(i).getProductId());
                break;
            }
            stringBuilder.append(productCollectionList.get(i).getProductId()).append(",");
        }
        //查询商品信息
        List<SimpleProductVO> simpleProductVOList = productService.getBriefProduct(stringBuilder.toString()).getData();
        if (simpleProductVOList.isEmpty()) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        //获取查询到最后一个收藏商品
        ProductCollection productCollectionLast = productCollectionList.get(productCollectionList.size() - 1);
        SimpleCursorCommonEntity simpleCursorCommonEntityResult = SimpleCursorCommonEntity.builder()
                .querySize(querySize)
                .sortId(productCollectionLast.getId())
                .sortValue(DateUtils.formatLocalDateTime(productCollectionLast.getCreateTime()))
                .build();
        //构造结果
        SimpleCursorCommonResult result = SimpleCursorCommonResult.builder()
                .isEnd(isEnd)
                .simpleCursorCommonEntity(simpleCursorCommonEntityResult)
                .list(simpleProductVOList)
                .build();
        return Result.success(result);

    }
}
