package com.starshop.controller.user;

import com.starshop.service.BannerService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class BannerController {

    @Resource
    private BannerService bannerService;
}
