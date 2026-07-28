package com.starshop.controller.user;


import com.starshop.pojo.dto.UserLoginDTO;
import com.starshop.pojo.vo.UserLoginVO;
import com.starshop.result.Result;
import com.starshop.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@RequestBody UserLoginDTO userLoginDTO) throws Exception {
        log.info("用户登录{}",userLoginDTO);
        UserLoginVO userLoginVO = userService.login(userLoginDTO);
        return Result.success(userLoginVO);
    }

    /**
     * 刷新token
     * @param refreshToken
     * @return
     */
    @PostMapping("/refresh/token")
    public Result refreshToken(@RequestParam String refreshToken){
        log.info("刷新token{}",refreshToken);
        return userService.refreshToken(refreshToken);
    }
}
