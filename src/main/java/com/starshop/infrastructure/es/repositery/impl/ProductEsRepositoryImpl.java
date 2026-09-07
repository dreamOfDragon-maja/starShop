package com.starshop.infrastructure.es.repositery.impl;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.Hit;
import com.starshop.common.annotation.common.ParamCheckAnnotation;
import com.starshop.infrastructure.es.document.ProductDocument;
import com.starshop.infrastructure.es.index.EsIndexEnum;
import com.starshop.infrastructure.es.repositery.ProductEsRepository;
import com.starshop.pojo.enums.CommonSortTypeEnum;
import com.starshop.pojo.enums.CommonStatus;
import com.starshop.pojo.enums.ProductSortTypeEnum;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ProductEsRepositoryImpl implements ProductEsRepository {

    private final ElasticsearchClient esClient;


    /**
     * 根据查询种类和商品名 进行游标查询(需要开始游标)
     * @param productSortTypeEnum 商品排序格式
     * @param keyword 商品关键词
     * @param limit 查询数
     * @param sortValue 开始游标值
     * @param productId 开始商品 id
     * @return 商品文档列表
     */
    @Override
    public List<ProductDocument> searchCursorByProductSortTypeAndProductName(ProductSortTypeEnum productSortTypeEnum, String keyword, Integer limit, String sortValue, Long productId) {
        //校验参数
        if (StringUtils.isBlank(keyword)) {
            return Collections.emptyList();
        }
        String sortField = productSortTypeEnum.getSortField();
        CommonSortTypeEnum commonSortTypeEnum = productSortTypeEnum.getCommonSortTypeEnum();
        SortOrder sortOrder = commonSortTypeEnum.isAsc() ? SortOrder.Asc :SortOrder.Desc;

        //构造请求参数
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(EsIndexEnum.PRODUCT.getIndexName())
                .sort(s -> s.field(f -> f.field(sortField).order(sortOrder)))
                .sort(s -> s.field(f -> f.field(ProductDocument.Fields.id).order(SortOrder.Asc)))
                .query(q -> q.bool(b -> b
                        .must(m -> m.match(ma -> ma
                                .field(ProductDocument.Fields.name)
                                .query(keyword)
                                .fuzziness("AUTO")
                        ))
                        .must(m -> m.term(t -> t.field(ProductDocument.Fields.status).value(CommonStatus.ACTIVE.getNumber())))
                ))
                .searchAfter(Arrays.asList(
                        FieldValue.of(sortValue),
                        FieldValue.of(productId)
                ))
                .size(limit)
                .build();

        try {
            SearchResponse<ProductDocument> searchResponse = esClient.search(searchRequest, ProductDocument.class);
            return searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("es 游标 limit 查询失败");
        }

    }

    /**
     * 根据查询种类和商品名 进行首次游标查询(无需开始游标)
     * @param productSortTypeEnum 商品排序格式
     * @param keyword 商品关键词
     * @param limit 查询数
     * @return 商品文档列表
     */
    @Override
    public List<ProductDocument> searchLimitByProductSortTypeAndProductName(ProductSortTypeEnum productSortTypeEnum, String keyword, Integer limit) {
        //校验参数
        if (StringUtils.isBlank(keyword)) {
            return Collections.emptyList();
        }
        String sortField = productSortTypeEnum.getSortField();
        CommonSortTypeEnum commonSortTypeEnum = productSortTypeEnum.getCommonSortTypeEnum();
        SortOrder sortOrder = commonSortTypeEnum.isAsc() ? SortOrder.Asc :SortOrder.Desc;

        //构造请求参数
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(EsIndexEnum.PRODUCT.getIndexName())
                .sort(s -> s.field(f -> f.field(sortField).order(sortOrder)))
                .sort(s -> s.field(f -> f.field(ProductDocument.Fields.id).order(SortOrder.Asc)))
                .query(q -> q.bool(b -> b
                        .must(m -> m.match(ma -> ma
                                .field(ProductDocument.Fields.name)
                                .query(keyword)
                                .fuzziness("AUTO")
                        ))
                        .must(m -> m.term(t -> t.field(ProductDocument.Fields.status).value(CommonStatus.ACTIVE.getNumber())))
                ))
                .size(limit)
                .build();

        try {
            SearchResponse<ProductDocument> searchResponse = esClient.search(searchRequest, ProductDocument.class);
            return searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("es 游标 limit 查询失败");
        }

    }

    /**
     * 根据查询种类和分类 id 进行游标查询 (需要开始游标)
     * @param productSortTypeEnum 商品排序格式
     * @param categoryId 查询商品分类 id
     * @param limit 查询数
     * @param sortValue 开始游标值
     * @param productId 开始商品 id
     * @return 商品文档列表
     */
    @Override
    @ParamCheckAnnotation
    public List<ProductDocument> searchCursorByProductSortTypeAndCategoryId(ProductSortTypeEnum productSortTypeEnum, Long categoryId, Integer limit, String sortValue, Long productId) {
        String sortField = productSortTypeEnum.getSortField();
        CommonSortTypeEnum commonSortTypeEnum = productSortTypeEnum.getCommonSortTypeEnum();
        SortOrder sortOrder = commonSortTypeEnum.isAsc() ? SortOrder.Asc : SortOrder.Desc;

        SearchRequest.Builder request = new SearchRequest.Builder();
        SearchRequest searchRequest = request.index(EsIndexEnum.PRODUCT.getIndexName())
                .sort(s -> s.field(f -> f.field(sortField).order(sortOrder)))
                .sort(s -> s.field(f -> f.field(ProductDocument.Fields.id).order(SortOrder.Asc)))
                .query(q -> q.bool(b -> b
                        .filter(f -> f.term(t -> t.field(ProductDocument.Fields.categoryId).value(categoryId)))
                        .filter(f -> f.term(t -> t.field(ProductDocument.Fields.status).value(CommonStatus.ACTIVE.getNumber())))
                ))
                .searchAfter(Arrays.asList(
                        FieldValue.of(sortValue),
                        FieldValue.of(productId)
                ))
                .size(limit)
                .build();
        try {
            SearchResponse<ProductDocument> searchResponse = esClient.search(searchRequest, ProductDocument.class);
            return searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("es 游标 limit 查询失败");
        }

    }

    /**
     * 根据查询种类和分类 id 首次进行游标查询 (无需开始游标)
     * @param productSortTypeEnum 商品排序格式
     * @param categoryId 查询商品分类 id
     * @param limit 查询数
     * @return 商品文档列表
     */
    @Override
    @ParamCheckAnnotation
    public List<ProductDocument> searchLimitByProductSortTypeAndCategoryId(ProductSortTypeEnum productSortTypeEnum, Long categoryId, Integer limit) {
        //准备参数
        String sortField = productSortTypeEnum.getSortField();
        CommonSortTypeEnum commonSortTypeEnum = productSortTypeEnum.getCommonSortTypeEnum();
        SortOrder sortOrder = commonSortTypeEnum.isAsc() ? SortOrder.Asc : SortOrder.Desc;
        //构造请求对象
        SearchRequest.Builder request = new SearchRequest.Builder();
        SearchRequest searchRequest = request.index(EsIndexEnum.PRODUCT.getIndexName())
                .sort(s -> s.field(f -> f.field(sortField).order(sortOrder)))
                .sort(s -> s.field(f -> f.field(ProductDocument.Fields.id).order(SortOrder.Asc)))
                .query(q -> q.bool(b -> b
                        .filter(f -> f.term(t -> t.field(ProductDocument.Fields.categoryId).value(categoryId)))
                        .filter(f -> f.term(t -> t.field(ProductDocument.Fields.status).value(CommonStatus.ACTIVE.getNumber())))
                ))
                .size(limit)
                .build();
        try {
            SearchResponse<ProductDocument> searchResponse = esClient.search(searchRequest, ProductDocument.class);
            return searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("es 首次游标 limit 查询失败");
        }

    }

    /**
     * 根据查询种类和分类 id 进行游标查询 (需要开始游标)
     * @param productSortTypeEnum 商品排序格式
     * @param categoryIdList 分类 id 集合
     * @param limit 查询数
     * @param sortValue 开始游标值
     * @param productId 开始商品 id
     * @return 商品文档列表
     */
    @Override
    @ParamCheckAnnotation
    public List<ProductDocument> searchCursorByProductSortTypeAndCategoryIdList(ProductSortTypeEnum productSortTypeEnum,
                                                                                List<Long> categoryIdList,
                                                                                Integer limit,
                                                                                String sortValue,
                                                                                Long productId) {
        //准备参数
        String sortField = productSortTypeEnum.getSortField();  //排序字段
        CommonSortTypeEnum commonSortTypeEnum = productSortTypeEnum.getCommonSortTypeEnum();
        SortOrder sortOrder = commonSortTypeEnum.isAsc()? SortOrder.Asc :SortOrder.Desc;  //排序方式

        //构造请求对象
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(EsIndexEnum.PRODUCT.getIndexName())
                //根据传入排序字段和方式sort
                .sort(s -> s.field(f -> f.field(sortField).order(sortOrder)))
                //再根据id升序排
                .sort(s -> s.field(f -> f.field(ProductDocument.Fields.id).order(SortOrder.Asc)))
                //过滤条件，必须在分类当中
                .query(q -> q.bool(b -> b
                        .must(m -> m
                                        .terms(t -> t.field(ProductDocument.Fields.categoryId)
                                                .terms(f -> f.value(categoryIdList.stream().map(FieldValue::of).toList())
                                                ))
                                //商品必须是启用状态
                        ).must(m -> m.term(t -> t
                                .field(ProductDocument.Fields.status)
                                .value(CommonStatus.ACTIVE.getNumber())
                        ))
                ))
                //深度分页（传入上一次分页的开始游标值和开始商品id）
                .searchAfter(Arrays.asList(FieldValue.of(sortValue),
                        FieldValue.of(productId)
                ))
                .size(limit)
                .build();

        //es查询
        try {
            SearchResponse<ProductDocument> searchResponse = esClient.search(searchRequest, ProductDocument.class);
            return searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("es 游标 limit 查询失败");
        }

    }

    /**
     * 根据查询种类和分类 id 首次进行游标查询 (无需开始游标)
     * @param productSortTypeEnum 商品排序格式
     * @param categoryIdList 分类 id 集合
     * @param limit 查询数
     * @return 商品文档列表
     */
    @Override
    @ParamCheckAnnotation
    public List<ProductDocument> searchLimitByProductSortTypeAndCategoryIdList(ProductSortTypeEnum productSortTypeEnum,
                                                                               List<Long> categoryIdList,
                                                                               Integer limit) {
        //准备参数
        String sortField = productSortTypeEnum.getSortField();  //排序字段
        CommonSortTypeEnum commonSortTypeEnum = productSortTypeEnum.getCommonSortTypeEnum();
        SortOrder sortOrder = commonSortTypeEnum.isAsc()? SortOrder.Asc :SortOrder.Desc;  //排序方式

        //构造请求对象
        SearchRequest searchRequest = new SearchRequest.Builder()
                .index(EsIndexEnum.PRODUCT.getIndexName())
                //根据传入排序字段和方式sort
                .sort(s -> s.field(f -> f.field(sortField).order(sortOrder)))
                //再根据id升序排
                .sort(s -> s.field(f -> f.field(ProductDocument.Fields.id).order(SortOrder.Asc)))
                //过滤条件，必须在分类当中
                .query(q -> q.bool(b -> b
                        .must(m -> m
                                        .terms(t -> t.field(ProductDocument.Fields.categoryId)
                                                .terms(f -> f.value(categoryIdList.stream().map(FieldValue::of).toList())
                                                ))
                                //商品必须是启用状态
                        ).must(m -> m.term(t -> t
                                .field(ProductDocument.Fields.status)
                                .value(CommonStatus.ACTIVE.getNumber())
                        ))
                ))
                .size(limit)
                .build();

        //es查询
        try {
            SearchResponse<ProductDocument> searchResponse = esClient.search(searchRequest, ProductDocument.class);
            return searchResponse.hits().hits().stream()
                    .map(Hit::source)
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new RuntimeException("es 首次游标 limit 查询失败");
        }


    }
}
