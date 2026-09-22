package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.dto.BannerDTO;
import com.starshop.pojo.entity.Banner;
import com.starshop.result.Result;

import java.util.List;

public interface BannerService extends IService<Banner> {

    /**
     * admin 添加 banner
     * @return
     */
    Result addBanner(BannerDTO bannerDTO);

    /**
     * admin 修改 banner
     * @param bannerDTO
     * @return
     */
    Result updateBanner(BannerDTO bannerDTO);

    /**
     * admin 删除 banner
     * @param id
     * @return
     */
    Result deleteBanner(Long id);

    /**
     * 用于 admin 获取联播图列表
     */
    Result<List<Banner>> getBannerListAdmin();
}
