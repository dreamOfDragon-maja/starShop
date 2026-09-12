package com.starshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.constant.DataConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.context.BaseContext;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.mapper.ProductCommentLikeMapper;
import com.starshop.mapper.ProductCommentMapper;
import com.starshop.pojo.entity.ProductComment;
import com.starshop.pojo.entity.ProductCommentLike;
import com.starshop.properties.RedisCacheTtlProperties;
import com.starshop.service.ProductCommentService;
import lombok.RequiredArgsConstructor;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductCommentServiceImpl extends ServiceImpl<ProductCommentMapper, ProductComment> implements ProductCommentService {

    private final ProductCommentLikeMapper productCommentLikeMapper;

    private final RedisCacheTtlProperties redisCacheTtlProperties;

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
