package com.starshop.controller.user;

import com.starshop.result.Result;
import com.starshop.service.CollectionService;
import com.starshop.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private CollectionService collectionService;

    /**
     * 获取用户详情
     * @return
     */
    @GetMapping("/detail/get")
    public Result getUserDetail() {
        return userService.getUserDetail();
    }
}
