package com.starshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.common.result.CursorCommonEntity;
import com.starshop.common.result.CursorCommonResult;
import com.starshop.common.utils.BloomFilterUtils;
import com.starshop.constant.DataConstant;
import com.starshop.constant.DatePatternConstants;
import com.starshop.constant.MessageConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.context.BaseContext;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.mapper.ProductCommentLikeMapper;
import com.starshop.mapper.ProductCommentMapper;
import com.starshop.pojo.entity.Product;
import com.starshop.pojo.entity.ProductComment;
import com.starshop.pojo.entity.ProductCommentLike;
import com.starshop.pojo.enums.ProductCommentQuerySortTypeEnum;
import com.starshop.properties.RedisCacheTtlProperties;
import com.starshop.result.Result;
import com.starshop.service.ProductCommentService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCommentServiceImpl extends ServiceImpl<ProductCommentMapper, ProductComment> implements ProductCommentService {

    private final ProductCommentLikeMapper productCommentLikeMapper;

    private final RedisCacheTtlProperties redisCacheTtlProperties;

    private final BloomFilterUtils bloomFilterUtils;

    private final CopyMapper copyMapper;
    /**
     * 用户做分类查询商品一级评论
     * @param cursorCommonEntity
     * @param productId
     * @return
     */
    @Override
    public Result<?> getProductCommentBySortType(CursorCommonEntity cursorCommonEntity, String productId) {
        //布隆过滤
        if (!bloomFilterUtils.contains(Long.valueOf(productId))) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        //获取传递数据
        String sortType = cursorCommonEntity.getSortType();
        String endCommentCreateTimeText = cursorCommonEntity.getSortValue();
        Long sortId = cursorCommonEntity.getSortId();
        Integer querySize = cursorCommonEntity.getQuerySize();
        LocalDateTime endCommentCreateTime;
        //解析sortValue(时间戳)
        if (StringUtils.isNotBlank(endCommentCreateTimeText)) {
            try {
                endCommentCreateTime = LocalDateTime.parse(endCommentCreateTimeText, DatePatternConstants.NORMAL_DATETIME_FORMATTER);
            } catch (DateTimeParseException e) {
                log.error(MessageConstant.DATE_TIME_PARSE_ERROR);
                return Result.error(MessageConstant.DATE_TIME_PARSE_ERROR);
            }
        }else {
            endCommentCreateTime = LocalDateTime.now();
        }
        //将sortType转成枚举
        ProductCommentQuerySortTypeEnum productCommentQuerySortTypeEnum = ProductCommentQuerySortTypeEnum.getByValue(sortType);
        SFunction<ProductComment, Object> function = productCommentQuerySortTypeEnum.getFunction();
        Object parameter = productCommentQuerySortTypeEnum.getParameter();

        //构造查询条件
        LambdaQueryChainWrapper<ProductComment> productCommentLambdaQueryChainWrapper = lambdaQuery();
        if (!Objects.isNull(function)) {
            //排序类型不是默认
            productCommentLambdaQueryChainWrapper = productCommentLambdaQueryChainWrapper.eq(function,parameter);
        }
        LocalDateTime finalEndCommentCreateTime = endCommentCreateTime;

        //开始分页(先按创建时间倒序，同时间按主键 ID 倒序)
        Page<ProductComment> pageResult = productCommentLambdaQueryChainWrapper
                .eq(ProductComment::getProductId, productId)
                .eq(ProductComment::getParentId, DataConstant.ZERO_INT)
                .and(wrapper -> wrapper
                        .lt(ProductComment::getCreateTime, finalEndCommentCreateTime)
                        .or(wrapper2 -> wrapper2
                                .eq(ProductComment::getCreateTime, finalEndCommentCreateTime)
                                .lt(ProductComment::getId, sortId)))
                .orderByDesc(ProductComment::getCreateTime)
                .orderByDesc(ProductComment::getId)
                .page(new Page<>(DataConstant.ONE_INT, querySize));
        List<ProductComment> productCommentList = pageResult.getRecords();
        //为评论设置点赞
        setProductCommentIsLike(productCommentList).forEach(productComment -> {
            //在redis中查询总点赞数
            Long firstCommentId = productComment.getId();
            String key =RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.FIRST_COMMENT + firstCommentId;
            if (RedisConnector.hasKey(key)) {
                Integer likeCount = RedisConnector.getHashField(key, ProductComment.Fields.likeCount, Integer.class);
                productComment.setLikeCount(likeCount);
            }
        });
        return getCursorCommonResult(cursorCommonEntity, productCommentList, copyMapper::productCommentToProductFirstCommentVO);
    }

    /**
     * 查询指定一级评论下二级评论
     * @param firstCommentId
     * @param cursorCommonEntity
     * @return
     */
    @Override
    public Result<?> getSecondComment(String firstCommentId, CursorCommonEntity cursorCommonEntity) {
        //获取传递数据
        String endCommentCreateTimeText = cursorCommonEntity.getSortValue();
        Long endCommentId = cursorCommonEntity.getSortId();
        Integer querySize = cursorCommonEntity.getQuerySize();
        LocalDateTime endCommentCreateTime;
        //解析sortValue(时间戳)
        if (StringUtils.isNotBlank(endCommentCreateTimeText)) {
            try {
                endCommentCreateTime = LocalDateTime.parse(endCommentCreateTimeText, DatePatternConstants.NORMAL_DATETIME_FORMATTER);
            } catch (DateTimeParseException e) {
                log.error(MessageConstant.DATE_TIME_PARSE_ERROR);
                return Result.error(MessageConstant.DATE_TIME_PARSE_ERROR);
            }
        }else {
            endCommentCreateTime = LocalDateTime.now();
        }
        //开始分页(先按创建时间倒序，同时间按主键 ID 倒序)
        Page<ProductComment> pageResult = lambdaQuery().eq(ProductComment::getParentId, firstCommentId)
                .and(wrapper ->
                        wrapper.lt(ProductComment::getCreateTime, endCommentCreateTime)
                                .or(wrapper2 -> {
                                    wrapper2.eq(ProductComment::getCreateTime, endCommentCreateTime)
                                            .lt(ProductComment::getId, endCommentId);
                                })
                )
                .orderByDesc(ProductComment::getCreateTime)
                .orderByDesc(ProductComment::getId)
                .page(new Page<>(DataConstant.ONE_INT, querySize));
        List<ProductComment> productCommentList = pageResult.getRecords();
        //为评论设置点赞
        setProductCommentIsLike(productCommentList).forEach(productComment -> {
            //在redis中查询总点赞数
            Long secondCommentId = productComment.getId();
            String key = RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.SECOND_COMMENT + secondCommentId;
                    if (RedisConnector.hasKey(key)) {
                        Integer likeCount = RedisConnector.getHashField(key, ProductComment.Fields.likeCount, Integer.class);
                        productComment.setLikeCount(likeCount);
                    }
                }
        );
        return getCursorCommonResult(cursorCommonEntity, productCommentList, copyMapper::productCommentToProductSecondCommentVO);

    }

    /**
     * 统一业务封装方法
     * @param cursorCommonEntity
     * @param productCommentList
     * @param copyMapperFunction
     * @return
     * @param <T>
     */
    private <T> Result<CursorCommonResult> getCursorCommonResult(CursorCommonEntity cursorCommonEntity, List<ProductComment> productCommentList, Function<ProductComment,T> copyMapperFunction) {
        if (productCommentList.isEmpty()) {
            return Result.success(CursorCommonResult.builder().isEnd(true).build());
        }
        List<T> resultList = productCommentList.stream().map(copyMapperFunction).toList();
        ProductComment productComment = productCommentList.get(productCommentList.size() - 1);
        //设置下一次滚动查询的参数
        cursorCommonEntity.setSortId(productComment.getId()).setSortValue(productComment.getCreateTime().format(DatePatternConstants.NORMAL_DATETIME_FORMATTER));
        CursorCommonResult cursorCommonResult = CursorCommonResult.builder().cursorCommonEntity(cursorCommonEntity).list(resultList).build();
        return Result.success(cursorCommonResult);
    }

    /**
     * 为评论设置 like , 更新评论下的用户set缓存
     * @param productCommentList 未赋值的评论列表
     * @return 赋值后的评论列表
     */
    private List<ProductComment> setProductCommentIsLike(List<ProductComment> productCommentList){
        //初始化userId
        String userId;
        //获取当前登录用户
        try {
            userId = BaseContext.getUserId();
        } catch (UnauthenticatedException e) {
            //用户未登录,设置like为false
            productCommentList.forEach(productComment -> productComment.setLike(false));
            return productCommentList;
        }
        if (productCommentList.isEmpty()) {
            return productCommentList;
        }
        //创建一个map用来存储需要查询数据库的商品评论(id,index)
        HashMap<Long,Integer> needQueryEntityMap = new HashMap<>();
        for (int i = 0; i < productCommentList.size(); i++) {
            ProductComment productComment = productCommentList.get(i);
            Long commentId = productComment.getId();
            String key = RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.COMMENT + commentId + ":" + RedisKeyConstant.USER + RedisKeyConstant.ID_LIST;
            //如果key不存在
            if (!RedisConnector.hasKey(key)) {
                needQueryEntityMap.put(commentId,i);
            }else {
                //查询redis并设置like
                Boolean member = RedisConnector.opsForSet().isMember(key, Long.valueOf(userId));
                boolean isLike = Boolean.TRUE.equals(member);
                productComment.setLike(isLike);
            }
        }

        if (needQueryEntityMap.isEmpty()) {
            return productCommentList;
        }

        //由map查询数据库(map->set<CommentId>)
        Set<Long> needQueryEntityIdSet = needQueryEntityMap.keySet();
        LambdaQueryWrapper<ProductCommentLike> lambdaQueryWrapper = new LambdaQueryWrapper<ProductCommentLike>()
                .in(ProductCommentLike::getCommentId, needQueryEntityIdSet)
                .eq(ProductCommentLike::getStatus, DataConstant.ONE_INT);
        List<ProductCommentLike> allLike = productCommentLikeMapper.selectList(lambdaQueryWrapper);
        //将结果利用流收集成map<commentId,Set<userId>>
        Map<Long, Set<Long>> commentUserMap = allLike.stream().filter(Objects::nonNull)
                .collect(Collectors.groupingBy(ProductCommentLike::getCommentId,
                        Collectors.mapping(ProductCommentLike::getUserId,Collectors.toSet())));

        //消费有点赞的评论
        commentUserMap.forEach((commentId,userIdSet)->{
            //判断userid在set中是否存在
            boolean isLike = userIdSet.contains(Long.valueOf(userId));
            Integer index = needQueryEntityMap.get(commentId);
            productCommentList.get(index).setLike(isLike);
            //写入缓存
            String key = RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.COMMENT + commentId + ":" + RedisKeyConstant.USER + RedisKeyConstant.ID_LIST;
            RedisConnector.opsForSet().add(key,userIdSet.toArray(new Long[0]));
            RedisConnector.expire(key,redisCacheTtlProperties.getProductCommentUserIdSetTtl(), TimeUnit.SECONDS);
            //去除元素
            needQueryEntityIdSet.remove(commentId);
        });

        //消费无点赞的评论
        if (!needQueryEntityMap.isEmpty()) {
            needQueryEntityMap.forEach((commentId,index)->{
                ProductComment productComment = productCommentList.get(index);
                productComment.setLike(false);
                String key = RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.COMMENT + commentId + ":" + RedisKeyConstant.USER + RedisKeyConstant.ID_LIST;
                //写入缓存，用0L代替防止缓存穿透
                RedisConnector.opsForSet().add(key,DataConstant.ZERO_LONG);
                RedisConnector.expire(key,redisCacheTtlProperties.getProductCommentUserIdSetTtl(), TimeUnit.SECONDS);

            });
        }
        return productCommentList;
    }
}
