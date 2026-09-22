package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.annotation.business.RemoveBannerRedisCacheAnnotation;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.constant.MessageConstant;
import com.starshop.mapper.BannerMapper;
import com.starshop.pojo.dto.BannerDTO;
import com.starshop.pojo.entity.Banner;
import com.starshop.result.Result;
import com.starshop.service.BannerService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.HashMap;

@Service
public class BannerServiceImpl extends ServiceImpl<BannerMapper, Banner> implements BannerService {

    @Resource
    private CopyMapper copyMapper;

    private static final String DELETE_ID = "deleteId";

    /**
     * admin 添加 banner
     * @return
     */
    @Override
    @RemoveBannerRedisCacheAnnotation
    public Result addBanner(BannerDTO bannerDTO) {
        Banner banner = copyMapper.bannerDTOToBanner(bannerDTO);
        boolean isSuccess = save(banner);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        return Result.success(banner);
    }

    /**
     * admin 修改 banner
     * @param bannerDTO
     * @return
     */
    @Override
    @RemoveBannerRedisCacheAnnotation
    public Result updateBanner(BannerDTO bannerDTO) {
        Banner banner = copyMapper.bannerDTOToBanner(bannerDTO);
        boolean isSuccess = updateById(banner);
        if (!isSuccess) {
            return Result.error(MessageConstant.TOM_CAT_ERROR);
        }
        return Result.success(banner);
    }

    /**
     * admin 删除 banner
     * @param id
     * @return
     */
    @Override
    @RemoveBannerRedisCacheAnnotation
    public Result deleteBanner(Long id) {
        boolean isSuccess = removeById(id);
        if (!isSuccess) {
            return Result.error(MessageConstant.DELETE_ERROR);
        }
        HashMap<String, Object> resultMap = new HashMap<>(1);
        resultMap.put(DELETE_ID, id);
        return Result.success(resultMap);
    }
}