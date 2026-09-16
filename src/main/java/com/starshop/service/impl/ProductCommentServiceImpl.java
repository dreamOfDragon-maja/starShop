package com.starshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.support.SFunction;
import com.baomidou.mybatisplus.extension.conditions.query.LambdaQueryChainWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.common.result.CursorCommonEntity;
import com.starshop.common.result.CursorCommonResult;
import com.starshop.common.utils.BloomFilterUtils;
import com.starshop.common.utils.MyBatisBatchExecutor;
import com.starshop.constant.DataConstant;
import com.starshop.constant.DatePatternConstants;
import com.starshop.constant.MessageConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.context.BaseContext;
import com.starshop.exception.EmptyObjectException;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.infrastructure.redis.connect.StringRedisConnector;
import com.starshop.mapper.ProductCommentAppendMapper;
import com.starshop.mapper.ProductCommentLikeMapper;
import com.starshop.mapper.ProductCommentMapper;
import com.starshop.pojo.dto.AppendProductFirstCommentDTO;
import com.starshop.pojo.dto.FirstProductCommentDTO;
import com.starshop.pojo.dto.SecondProductCommentDTO;
import com.starshop.pojo.entity.ProductComment;
import com.starshop.pojo.entity.ProductCommentAppend;
import com.starshop.pojo.entity.ProductCommentLike;
import com.starshop.pojo.enums.ProductCommentQuerySortTypeEnum;
import com.starshop.pojo.vo.ProductAppendCommentVO;
import com.starshop.properties.RedisCacheTtlProperties;
import com.starshop.result.Result;
import com.starshop.service.ProductCommentService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.shiro.authz.UnauthenticatedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    private final ProductCommentAppendMapper productCommentAppendMapper;
    
    private final MyBatisBatchExecutor myBatisBatchExecutor;

    private final static String emptyProductCommentCount = "-1";

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
     * 查询指定一级评论下的用户追评
     * @return
     */
    @Override
    public Result<?> getAppendComment(String firstCommentId) {
        Long aboutFirstCommentId = Long.valueOf(firstCommentId);
        String appendCommentKey = RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.APPEND_COMMENT + RedisKeyConstant.FIRST_COMMENT + firstCommentId;
        //查询缓存
        ProductCommentAppend productCommentAppend = RedisConnector.getHashObject(appendCommentKey, ProductCommentAppend.class);
        //查询不到查询数据库
        if (Objects.isNull(productCommentAppend)) {
            ProductCommentAppend productCommentAppendSelectOne = productCommentAppendMapper.selectOne(new LambdaQueryWrapper<>(ProductCommentAppend.class)
                    .eq(ProductCommentAppend::getCommentId, aboutFirstCommentId));
            //如果数据库中也没有数据，缓存空对象防止缓存穿透
            if (Objects.isNull(productCommentAppendSelectOne)) {
                //构建空对象
                ProductCommentAppend emptyProductCommentAppend = ProductCommentAppend.builder().id(DataConstant.ZERO_LONG).build();
                RedisConnector.setHashObject(appendCommentKey, emptyProductCommentAppend);
                RedisConnector.expire(appendCommentKey, redisCacheTtlProperties.getProductAppendCommentTtl(), TimeUnit.SECONDS);
                return Result.error(MessageConstant.DATA_ERROR);
            }
            //将查到的数据写入缓存并返回
            RedisConnector.setHashObject(appendCommentKey, productCommentAppendSelectOne);
            RedisConnector.expire(appendCommentKey, redisCacheTtlProperties.getProductAppendCommentTtl(), TimeUnit.SECONDS);
            ProductAppendCommentVO productAppendCommentVO = copyMapper.productCommentAppendToProductCommentAppendVO(productCommentAppendSelectOne);
            return Result.success(productAppendCommentVO);
        }
        //空对象过滤
        if (productCommentAppend.getId().equals(DataConstant.ZERO_LONG)) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        ProductAppendCommentVO productAppendCommentVO = copyMapper.productCommentAppendToProductCommentAppendVO(productCommentAppend);
        return Result.success(productAppendCommentVO);

    }

    /**
     * 用户发表一级商品评论
     * @param firstProductCommentDTO
     * @return
     */
    @Override
    public Result<?> saveProductFirstComment(FirstProductCommentDTO firstProductCommentDTO) {
        if (Objects.isNull(firstProductCommentDTO)) {
            return Result.error(MessageConstant.NETWORK_ERROR);
        }
        //获取当前登录用户id
        String userId = BaseContext.getUserId();
        ProductComment productComment = copyMapper.firstProductCommentDTOToProductComment(firstProductCommentDTO);
        testIsAnonymous(productComment)
                .setParentId(DataConstant.ZERO_LONG)
                .setUserId(Long.valueOf(userId))
                .setIsBuyer(DataConstant.ONE_INT)
                .setCreateTime(LocalDateTime.now())
                .setUpdatedTime(LocalDateTime.now());
        //存入数据库
        boolean isSuccess = save(productComment);
        if (!isSuccess) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        //TODO后续使用rocketmq修改订单状态为已评价

        //删除redis中缓存的评论点赞数
        String productId = firstProductCommentDTO.getProductId();
        String key = RedisKeyConstant.PREFIX_PRODUCT + productId + ":" + RedisKeyConstant.COMMENT_COUNT;
        StringRedisConnector.delete(key);
        return Result.success();
    }

    /**
     * 用户发表二级以上商品评论
     * @param secondProductCommentDTO
     * @return
     */
    @Override
    public Result<?> saveProductSecondComment(SecondProductCommentDTO secondProductCommentDTO) {
        if (Objects.isNull(secondProductCommentDTO)) {
            return Result.error(MessageConstant.NETWORK_ERROR);
        }
        //获取当前登录用户id
        String userId = BaseContext.getUserId();
        ProductComment productComment = copyMapper.secondProductCommentDTOToProductComment(secondProductCommentDTO);
        Long parentCommentId = productComment.getParentId();
        if (Objects.isNull(parentCommentId)) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        //在redis查询一级评论数据
        String firstCommentKey = RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.FIRST_COMMENT + parentCommentId;
        ProductComment firstProductComment = RedisConnector.getHashObject(firstCommentKey, ProductComment.class);
        //防止缓存穿透
        firstProductComment = getProductCommentIfRedisCacheNull(firstProductComment, parentCommentId, firstCommentKey);
        //过滤空对象
        if (firstProductComment.getId().equals(DataConstant.ZERO_LONG)) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        Long firstProductCommentUserId = firstProductComment.getUserId();
        //确定是买家,进行标记
        if (firstProductCommentUserId.equals(Long.valueOf(userId))) {
            productComment.setIsBuyer(DataConstant.ONE_INT);
        }
        //判断是否匿名发送
        testIsAnonymous(productComment)
                .setUserId(Long.valueOf(userId))
                .setCreateTime(LocalDateTime.now()).
                setUpdatedTime(LocalDateTime.now());
        boolean isSuccess = save(productComment);
        if (!isSuccess) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        //删除redis中缓存的评论点赞数
        String productId = secondProductCommentDTO.getProductId();
        String key = RedisKeyConstant.PREFIX_PRODUCT + productId + ":" + RedisKeyConstant.COMMENT_COUNT;
        StringRedisConnector.delete(key);
        return Result.success();

    }

    /**
     * 用户对一级评论进行追加
     * @param appendProductFirstCommentDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result<?> appendProductFirstComment(AppendProductFirstCommentDTO appendProductFirstCommentDTO) {
        //获取当前登录用户id
        String userId = BaseContext.getUserId();
        ProductCommentAppend productCommentAppend = copyMapper.appendProductFirstCommentDTOToProductCommentAppend(appendProductFirstCommentDTO);
        //根据订单号查询商品评论
        ProductComment productComment = lambdaQuery().eq(ProductComment::getOrderNo, appendProductFirstCommentDTO.getOrderNo()).one();
        Long commentId = productComment.getId();
        //在redis查询一级评论数据
        String firstCommentKey = RedisKeyConstant.PREFIX_PRODUCT + RedisKeyConstant.FIRST_COMMENT + commentId;
        ProductComment firstProductComment = RedisConnector.getHashObject(firstCommentKey, ProductComment.class);
        //防止缓存穿透
        firstProductComment = getProductCommentIfRedisCacheNull(firstProductComment, commentId, firstCommentKey);
        //过滤空对象
        if (firstProductComment.getId().equals(DataConstant.ZERO_LONG)) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        //评论伪造过滤
        if (!firstProductComment.getProductId().equals(Long.valueOf(appendProductFirstCommentDTO.getProductId()))
                && firstProductComment.getUserId().equals(Long.valueOf(userId))) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        //已经追评过滤
        if (firstProductComment.getIsAppendComment() == DataConstant.ONE_INT) {
            return Result.error(MessageConstant.HAVE_APPEND);
        }
        productCommentAppend.setProductId(firstProductComment.getProductId())
                .setProductSpecId(firstProductComment.getProductSpecId())
                .setOrderNo(firstProductComment.getOrderNo())
                .setUserId(Long.valueOf(userId))
                .setCommentId(commentId);
        //批量操作数据库
        myBatisBatchExecutor.executeBatch(sqlSession -> {
            ProductCommentAppendMapper batchAppendMapper = sqlSession.getMapper(ProductCommentAppendMapper.class);
            ProductCommentMapper batchCommentMapper = sqlSession.getMapper(ProductCommentMapper.class);
            //新增追加商品评论
            batchAppendMapper.insert(productCommentAppend);
            //更新商品评论表状态
            LambdaUpdateWrapper<ProductComment> updateWrapper = new LambdaUpdateWrapper<ProductComment>()
                    .eq(ProductComment::getId, commentId)
                    .set(ProductComment::getIsAppendComment, DataConstant.ONE_INT);
            batchCommentMapper.update(updateWrapper);
            return null;
        });
        RedisConnector.delete(firstCommentKey);
        //TODO后续使用rocketmq修改订单状态为已追评
        return Result.success();
    }


    /**
     * 统计商品下评论数
     * @param productId
     * @return
     */
    @Override
    public Result<?> getProductCommentCount(String productId) {
        Long productIdLong = Long.valueOf(productId);
        //布隆过滤
        if (!bloomFilterUtils.contains(productIdLong)) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        //查询redis
        String key = RedisKeyConstant.PREFIX_PRODUCT + productId + ":" + RedisKeyConstant.COMMENT_COUNT;
        String productCommentCount = StringRedisConnector.opsForValue().get(key);
        if (StringUtils.isBlank(productCommentCount)) {
            //查询数据库
            Long productCommentCountLong = lambdaQuery().eq(ProductComment::getProductId, productIdLong).count();
            if (Objects.isNull(productCommentCountLong)) {
                //缓存空对象
                StringRedisConnector.opsForValue().set(key, emptyProductCommentCount);
                StringRedisConnector.expire(key, redisCacheTtlProperties.getProductCommentCountTtl(), TimeUnit.SECONDS);
                return Result.error(MessageConstant.DATA_ERROR);
            }
            //查询到的结果写入缓存并返回
            productCommentCount = productCommentCountLong.toString();
            StringRedisConnector.opsForValue().set(key, productCommentCount);
            StringRedisConnector.expire(key, redisCacheTtlProperties.getProductCommentCountTtl(), TimeUnit.SECONDS);
            return Result.success(productCommentCount);
        }
        //过滤空对象
        if (StringUtils.equals(productCommentCount, emptyProductCommentCount)) {
            return Result.error(MessageConstant.DATA_ERROR);
        }
        return Result.success(productCommentCount);

    }

    /**
     * 传入 redis 查询后的一级评论结果
     * 进行判断是否为 null ,是会进行数据库查询,如果为空会缓存空对象
     * 不是 null ,放行,不做处理
     * @param firstProductComment
     * @param parentCommentId
     * @param firstCommentKey
     * @return
     */
    private ProductComment getProductCommentIfRedisCacheNull(ProductComment firstProductComment, Long parentCommentId, String firstCommentKey) {
        if (Objects.isNull(firstProductComment)) {
            //查询数据库
            firstProductComment = lambdaQuery().eq(ProductComment::getId, parentCommentId).one();
            //缓存空对象
            if (Objects.isNull(firstProductComment)) {
                ProductComment emptyProductComment = ProductComment
                        .builder()
                        .id(DataConstant.ZERO_LONG)
                        .build();
                RedisConnector.setHashObject(firstCommentKey,emptyProductComment);
                throw new EmptyObjectException(MessageConstant.DATA_ERROR);
            }
            //将查询到的数据写入redis
            RedisConnector.setHashObject(firstCommentKey, firstProductComment);
            RedisConnector.expire(firstCommentKey, redisCacheTtlProperties.getProductFirstCommentTtl(), TimeUnit.SECONDS);
        }

        return firstProductComment;
    }

    /**
     * 判断是否为匿名发布
     * @param productComment
     */
    private ProductComment testIsAnonymous(ProductComment productComment) {
        int isAnonymous = productComment.getIsAnonymous();
        if (isAnonymous == DataConstant.ONE_INT) {
            productComment.setUserNickname(DataConstant.ANONYMOUS_NICKNAME)
                    .setImageUrls(DataConstant.DEFAULT_AVATAR);
        }
        return productComment;
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
