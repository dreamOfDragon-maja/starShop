package com.starshop.controller.user;


import com.starshop.pojo.dto.UserLoginDTO;
import com.starshop.result.Result;
import com.starshop.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequestMapping("/api/user")
public class LoginController {

     @Resource
     private UserService userService;;

    /**
     * 新用户注册账号
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/register")
    public Result register(@RequestBody UserLoginDTO userLoginDTO){
        log.info("新用户注册账号{}",userLoginDTO);
        return userService.register(userLoginDTO);
    }
}
