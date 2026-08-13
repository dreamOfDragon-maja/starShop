package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.JsonNode;
import com.starshop.common.annotation.Validate;
import com.starshop.common.creation.NicknameCreation;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.common.result.LoginInfo;
import com.starshop.common.result.UserInfo;
import com.starshop.common.utils.JwtUtils;
import com.starshop.common.utils.WechatLoginUtils;
import com.starshop.constant.JwtClaimsConstant;
import com.starshop.constant.MessageConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.context.BaseContext;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.mapper.UserMapper;
import com.starshop.pojo.dto.UserLoginDTO;
import com.starshop.pojo.dto.UserUpdateDTO;
import com.starshop.pojo.dto.UserWechatDTO;
import com.starshop.pojo.emums.CommonStatus;
import com.starshop.pojo.emums.UserRoleEnum;
import com.starshop.pojo.entity.SysUser;
import com.starshop.properties.JwtProperties;
import com.starshop.result.Result;
import com.starshop.service.UserService;
import jakarta.annotation.Resource;
import org.apache.commons.lang3.StringUtils;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, SysUser> implements UserService {

    @Resource
    private JwtProperties jwtProperties;
    @Resource
    private CopyMapper copyMapper;
    @Resource
    private WechatLoginUtils wechatLoginUtils;
    @Resource
    private UserMapper userMapper;


    /**
     * 获取当前用户信息
     * @return
     */
    public Result<Object> getUser() {
        return Result.success(BaseContext.getUserInfo());
    }

    /**
     * 用户使用微信快速登录
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result loginByWechat(UserWechatDTO userWechatDTO) {
        //1.获取前端的临时code
        String code = userWechatDTO.getCode();
        if(StringUtils.isBlank(code)){
            return Result.error(MessageConstant.WECHAT_CODE_EMPTY);
        }
        //2.根据code解析json
        JsonNode jsonNode = wechatLoginUtils.getWechatUserInfo(code);
        if(Objects.isNull(jsonNode) || !jsonNode.has(SysUser.Fields.openid)){
            return Result.error(MessageConstant.GET_OPENID_ERROR);
        }
        //3.获取openid
        String openid = jsonNode.get(SysUser.Fields.openid).asText();
        if (StringUtils.isBlank(openid)) {
            return Result.error(MessageConstant.GET_OPENID_ERROR);
        }
        //4，根据openid获取用户
        SysUser user = getSysUserByOpenidWithRolesAndPermissions(openid);
        //5.新用户
        if(Objects.isNull(user)){
            //5.1.创建新用户并赋值
            SysUser userNew = SysUser.builder()
                    .openid(openid)
                    .nickname(userWechatDTO.getNickName())
                    .avatar(userWechatDTO.getAvatarUrl())
                    .firstLoginTime(LocalDateTime.now())
                    .lastLoginTime(LocalDateTime.now())
                    .build();
            //5.2.将新用户保存到数据库当中
            save(userNew);
            //5.3.给用户添加角色
            userMapper.insertSysUserConnectSysRole(userNew.getId(), UserRoleEnum.ROLE_BUYER.getId());
            //5.4.保存到redis
            UserInfo userInfo = copyMapper.usertoUserInfo(userNew);
            setUserInfoToRedis(userNew,userInfo);
            //5.5.构造LoginInfo返回结果
            String accessToken = getAccessToken(userInfo);
            String refreshToken = getRefreshToken(userInfo);
            return Result.success(new LoginInfo(accessToken,refreshToken,userInfo));
        }

        //6.老用户
        //6.1.更新最后登录时间
        user.setLastLoginTime(LocalDateTime.now());
        //6.2.更新数据库信息
        updateById(user);
        //6.3.更新redis
        UserInfo userInfo = copyMapper.usertoUserInfo(user);
        setUserInfoToRedis(user,userInfo);
        //6.4.组装返回
        String accessToken = getAccessToken(userInfo);
        String refreshToken = getRefreshToken(userInfo);
        return Result.success(new LoginInfo(accessToken,refreshToken,userInfo));

    }

    /**
     * 忘记密码
     * @param username
     * @param phone
     * @param passwordNew
     * @return
     */
    @Override
    @Validate(requiredPhone = true)
    public Result forgetPassword(String username, String phone, String passwordNew) {
        //1.根据username和phone查询一个用户
        SysUser sysUser = lambdaQuery().eq(SysUser::getUsername, username).eq(SysUser::getPhone, phone).one();
        //2.判断是否存在
        if(Objects.isNull(sysUser)){
            return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        //3.设置新密码
        String passwordOld = sysUser.getPassword();
        if(BCrypt.checkpw(passwordNew,passwordOld)){
            return Result.error(MessageConstant.ERROR_NEW_PASSWORD_SAME_AS_OLD);
        }
        String hashpw = BCrypt.hashpw(passwordNew, BCrypt.gensalt());
        sysUser.setPassword(hashpw);

        //4.更新到数据库
        boolean isSuccess = updateById(sysUser);
        if(!isSuccess){
            return Result.error(MessageConstant.PASSWORD_MODIFY_ERROR);
        }
        return Result.success(sysUser.getId());
    }

    /**
     * 修改密码
     * @param username
     * @param passwordOld
     * @param passwordNew
     * @return
     */
    @Override
    @Validate
    public Result changePassword(String username, String passwordOld, String passwordNew) {
        //1.查询用户
        SysUser sysUser = lambdaQuery().eq(SysUser::getUsername, username).one();
        //2.业务判断
        if(Objects.isNull(sysUser)){
            return Result.error(MessageConstant.ACCOUNT_NOT_FOUND);
        }
        if(!BCrypt.checkpw(passwordOld, sysUser.getPassword())){
            return Result.error(MessageConstant.LOGIN_ERROR);
        }
        //3.设置新密码
        String hashpw = BCrypt.hashpw(passwordNew, BCrypt.gensalt());
        sysUser.setPassword(hashpw);

        //4.更新到数据库
        boolean isSuccess = updateById(sysUser);
        if(!isSuccess){
            return Result.error(MessageConstant.PASSWORD_MODIFY_ERROR);
        }
        return Result.success(sysUser.getId());
    }

    /**
     * 更新用户信息
     * @param userUpdateDTO
     * @return
     */
    @Override
    @Transactional
    @Validate
    public Result<UserInfo> updateUserInfo(UserUpdateDTO userUpdateDTO) {
        //1.查询当前用户
        Long userId = Long.valueOf(BaseContext.getUserId());
        //2.判断用户在数据库中是否存在
        SysUser sysUser = lambdaQuery().eq(SysUser::getId, userId).one();
        if(Objects.isNull(sysUser)){
            return Result.error(MessageConstant.USER_NOT_EXISTS);
        }
        //3.利用mapstruct更新数据
        copyMapper.updateUserFromDTO(userUpdateDTO, sysUser);
        //4.数据同步到数据库
        boolean isSuccess = updateById(sysUser);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_UPDATE_ERROR);
        }
        //5.封装VO
        UserInfo userInfo = copyMapper.usertoUserInfo(sysUser);
        //6.将数据同步到redis
        String key = RedisKeyConstant.PREFIX_LOGIN + RedisKeyConstant.USER + userId;
        if (RedisConnector.hasKey(key)) {
            RedisConnector.opsForHash().put(key, SysUser.Fields.userInfo, userInfo);
        }

        return Result.success(userInfo);
    }

    /**
     * 刷新token
     * @param refreshToken
     * @return
     */
    @Override
    public Result refreshToken(String refreshToken) {
        //1.从redis中获取refreshToken的信息
        String key = RedisKeyConstant.PREFIX_LOGIN + RedisKeyConstant.REFRESH + RedisKeyConstant.TOKEN +refreshToken;
        Map<String, Object> refreshTokenMap = RedisConnector.opsForHash().entries(key);
        //2.判断是否为空，如果为空，返回错误信息
        if(refreshTokenMap.isEmpty()){
            return Result.error(MessageConstant.REFRESH_TOKEN_EXPIRED_ERROR);
        }
        //3.生成新的accessToken
        String accessTokenNew = JwtUtils.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), refreshTokenMap);
        //4.从refreshTokenMap里面获取userId
        Long userId = Long.valueOf(refreshTokenMap.get(JwtClaimsConstant.SYS_USER_ID).toString());
        //5，删除旧的refreshToken
        RedisConnector.delete(key);
        //6.生成新的的refreshToken
        UserInfo userInfo = UserInfo.builder().id(String.valueOf(userId)).build();
        String refreshTokenNew = getRefreshToken(userInfo);
        //7.封装信息组装
        HashMap<String, Object> resultMap = new HashMap<>(2);
        resultMap.put(RedisKeyConstant.FIELD_ACCESS_TOKEN,accessTokenNew);
        resultMap.put(RedisKeyConstant.FIELD_REFRESH_TOKEN,refreshTokenNew);
        return Result.success(resultMap);
    }



    /**
     * 用户登录
     * @param userLoginDTO
     * @return
     */
    @Override
    @Validate
    public Result<Object> login(UserLoginDTO userLoginDTO) throws Exception {
        //1.查询用户
        SysUser sysUser = lambdaQuery().eq(SysUser::getUsername, userLoginDTO.getUsername()).one();
        //2.如果不存在，抛出异常
        if (sysUser == null) {
            throw new Exception(MessageConstant.USER_NOT_EXISTS);
        }
        //3.存在，查询数据库校验是否正确
        if (!BCrypt.checkpw(userLoginDTO.getPassword(), sysUser.getPassword())) {
            throw new Exception(MessageConstant.PASSWORD_ERROR);
        }
        //4.信息正确则生成accessToken
        UserInfo userInfo = copyMapper.usertoUserInfo(sysUser);
        String accessToken = getAccessToken(userInfo);

        //5.生成refreshToken
        String refreshToken = getRefreshToken(userInfo);

        //6.将生成的token存入redis
        setUserInfoToRedis(sysUser,userInfo);

        //7.组装VO返回结果
        return Result.success(new LoginInfo(accessToken,refreshToken,userInfo));
    }

    /**
     * 新用户注册账号
     * @param userLoginDTO
     * @return
     */
    @Override
    @Transactional
    @Validate(requiredPhone = true)
    public Result register(UserLoginDTO userLoginDTO) {
        //1.根据username查询数据库
        SysUser sysUser = lambdaQuery().eq(SysUser::getUsername, userLoginDTO.getUsername()).one();
        //2.判断是否存在
        if(Objects.nonNull(sysUser)){
            //2.1存在，返回错误信息
            return Result.error(MessageConstant.USER_NAME_EXISTS);
        }
        //2.2不存在，构造User
        //2.3加密密码
        String hashpw = BCrypt.hashpw(userLoginDTO.getPassword(), BCrypt.gensalt());
        //2.4生成随机昵称（UUID）
        String nickname = NicknameCreation.createDefaultNickname();
        SysUser build = SysUser.builder()
                .nickname(nickname)
                .password(hashpw)
                .username(userLoginDTO.getUsername())
                .phone(userLoginDTO.getPhone())
                .build();
        //3.写入数据库
        boolean isSuccess = save(build);
        if(!isSuccess){
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        //4.返回结果
        return Result.success();
    }

    /**
     * 刷新 token 格式: UUID
     * @param userInfo
     * @return
     */
    private String getRefreshToken(UserInfo userInfo) {
        String refreshToken = UUID.randomUUID().toString();
        String key = RedisKeyConstant.PREFIX_LOGIN + RedisKeyConstant.REFRESH + RedisKeyConstant.TOKEN +refreshToken;
        HashMap<String, Object> map = new HashMap<>(1);
        map.put(JwtClaimsConstant.SYS_USER_ID, userInfo.getId());
        RedisConnector.opsForHash().putAll(key, map);
        RedisConnector.expire(key, jwtProperties.getLoginRefreshTokenTtl(), TimeUnit.DAYS);
        return refreshToken;
    }

    /**
     * 获取accessToken
     * @param userInfo
     * @return
     */
    private String getAccessToken(UserInfo userInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put(JwtClaimsConstant.SYS_USER_ID, userInfo.getId());
        return JwtUtils.createJWT(jwtProperties.getUserSecretKey(), jwtProperties.getUserTtl(), map);
    }

    /**
     * 设置用户信息存入redis
     * @param user
     * @param userInfo
     */
    public void setUserInfoToRedis(SysUser user, UserInfo userInfo) {
        if (Objects.isNull(user)) {
            return;
        }
        String key = RedisKeyConstant.PREFIX_LOGIN+RedisKeyConstant.USER+ user.getId();
        HashMap<String, Object> loginUserMap = new HashMap<>(4);
        loginUserMap.put(SysUser.Fields.userInfo, userInfo);
        loginUserMap.put(SysUser.Fields.isEnable, CommonStatus.ACTIVE.getNumber());
        loginUserMap.put(SysUser.Fields.sysRoleList, user.getSysRoleList());
        loginUserMap.put(SysUser.Fields.sysPermissionList, user.getSysPermissionList());
        RedisConnector.opsForHash().putAll(key, loginUserMap);
        RedisConnector.expire(key, jwtProperties.getLoginUserInfoInRedisTtl(), TimeUnit.DAYS);
    }


    /**
     * 根据 username 查询 user 带角色和权限
     */
    @Override
    public SysUser getSysUserByNameWithRolesAndPermissions(String username) {
        if (StringUtils.isBlank(username)) {
            return null;

        }
        return userMapper.getSysUserByNameWithRolesAndPermissions(username);
    }

    /**
     * 根据 userId 查询 user 带角色和权限
     */
    @Override
    public SysUser getSysUserByUserIdWithRolesAndPermissions(Long userId) {
        if (Objects.isNull(userId)) {
            return null;

        }
        return userMapper.getSysUserByUserIdWithRolesAndPermissions(userId);
    }

    /**
     * 根据 openid 查询 user 带角色和权限
     */
    @Override
    public SysUser getSysUserByOpenidWithRolesAndPermissions(String openid) {
        if (Objects.isNull(openid)) {
            return null;

        }
        return userMapper.getSysUserByOpenidWithRolesAndPermissions(openid);
    }

}
