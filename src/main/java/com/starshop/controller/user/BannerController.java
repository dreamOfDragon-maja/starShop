package com.starshop.controller.user;

import com.starshop.pojo.dto.BannerDTO;
import com.starshop.result.Result;
import com.starshop.service.BannerService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

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

    /**
     * admin 修改 banner
     * @param bannerDTO
     * @return
     */
    @PutMapping("/admin/banner/update")
    public Result updateBanner(@RequestBody BannerDTO bannerDTO) {
        return bannerService.updateBanner(bannerDTO);
    }
}
