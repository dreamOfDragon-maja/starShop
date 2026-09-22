package com.starshop.controller.user;

import com.starshop.pojo.dto.BannerDTO;
import com.starshop.pojo.entity.Banner;
import com.starshop.result.Result;
import com.starshop.service.BannerService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class BannerController {

    @Resource
    private BannerService bannerService;

    /**
     * 获取首页联播图列表
     * @return
     */
    @GetMapping("/banner/list")
    public Result<List<Banner>> getBannerList(){
        return bannerService.getBannerList();
    }

    /**
     * 用于 admin 获取联播图列表
     */
    @GetMapping("/admin/banner/list")
    public Result<List<Banner>> getBannerListByAdmin()  {
        return bannerService.getBannerListAdmin();
    }

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

    /**
     * admin 删除 banner
     * @param id
     * @return
     */
    @DeleteMapping("/admin/banner/delete")
    public Result deleteBanner(@RequestParam Long id) {
        return bannerService.deleteBanner(id);
    }
}
