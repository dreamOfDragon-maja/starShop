package com.starshop.controller.user;


import com.starshop.constant.RedisKeyConstant;
import com.starshop.context.BaseContext;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.pojo.dto.UserLoginDTO;
import com.starshop.pojo.dto.UserUpdateDTO;
import com.starshop.pojo.vo.UserVO;
import com.starshop.result.Result;
import com.starshop.service.UserService;
import jakarta.annotation.Resource;
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
    public Result<Object> login(@RequestBody UserLoginDTO userLoginDTO) throws Exception {
        log.info("用户登录{}",userLoginDTO);
        return userService.login(userLoginDTO);
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

    /**
     * 获取当前用户信息
     * @return
     */
    @GetMapping("/info")
    public Result<UserVO> getUser(){
        return userService.getUser();
    }

    /**
     * 更新用户信息
     * @param userUpdateDTO
     * @return
     */
    @PutMapping("/info")

    public Result<UserVO> updateUserInfo(@RequestBody UserUpdateDTO userUpdateDTO){
        log.info("更新用户信息{}",userUpdateDTO);
        return userService.updateUserInfo(userUpdateDTO);
    }

    /**
     * 退出登录
     * @return
     */
    @PostMapping("/logout")
    public Result logout(){
        String userId = BaseContext.getCurrentId().toString();
        String key = RedisKeyConstant.PREFIX_LOGIN+RedisKeyConstant.USER+ userId;
        RedisConnector.delete(key);
        BaseContext.removeCurrentId();
        return Result.success();
    }

    /**
     * 修改密码
     * @param username
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    @PutMapping("/change/password")
    public Result changePassword(@RequestParam String username,
                                 @RequestParam String passwordOld,
                                 @RequestParam String passwordNew){
        log.info("修改密码，{},{}，{}",username,passwordOld,passwordNew);
        return userService.changePassword(username,passwordOld,passwordNew);
    }

    /**
     * 忘记密码
     * @param username
     * @param phone
     * @param passwordNew
     * @return
     */
    @PutMapping("/forget/password")
    public Result forgetPassword(@RequestParam String username,
                                 @RequestParam String phone,
                                 @RequestParam String passwordNew){
        log.info("忘记密码，{},{}，{}",username,phone,passwordNew);
        return userService.forgetPassword(username,phone,passwordNew);
    }



}
