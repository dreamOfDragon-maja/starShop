package com.starshop.controller.user;

import com.starshop.pojo.dto.BannerDTO;
import com.starshop.result.Result;
import com.starshop.service.BannerService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BannerController {

    @Resource
    private BannerService bannerService;

    /**
     * admin 添加 banner
     * @return
     */
    @PostMapping("/admin/banner/add")
    public Result addBanner(@RequestBody BannerDTO bannerDTO) {
        return bannerService.addBanner(bannerDTO);
    }
}
