package com.starshop.controller.admin;

import com.starshop.result.Result;
import com.starshop.service.FactoryInfoService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/about/us")
public class FactoryInfoController {

    @Resource
    private FactoryInfoService factoryInfoService;

    /**
     * 查询单条工厂信息
     * @return
     */
    @GetMapping("/introduce")
    public Result<Object> getFactoryInfo(){
        return factoryInfoService.getFactoryInfo();
    }
}
