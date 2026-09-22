package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.dto.BannerDTO;
import com.starshop.pojo.entity.Banner;
import com.starshop.result.Result;

public interface BannerService extends IService<Banner> {

    /**
     * admin 添加 banner
     * @return
     */
    Result addBanner(BannerDTO bannerDTO);
}
