package com.starshop.controller.admin;

import com.starshop.service.FactoryInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/about/us")
public class FactoryInfoController {

    @Resource
    private FactoryInfoService factoryInfoService;
}
