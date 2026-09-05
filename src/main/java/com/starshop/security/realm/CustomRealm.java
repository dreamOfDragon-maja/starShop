package com.starshop.security.realm;

import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.common.result.UserInfo;
import com.starshop.constant.JwtClaimsConstant;
import com.starshop.constant.MessageConstant;
import com.starshop.constant.RedisKeyConstant;
import com.starshop.exception.InvalidCredentialsException;
import com.starshop.infrastructure.redis.connect.RedisConnector;
import com.starshop.pojo.enums.CommonStatus;
import com.starshop.pojo.entity.SysPermission;
import com.starshop.pojo.entity.SysRole;
import com.starshop.pojo.entity.SysUser;
import com.starshop.security.token.JwtToken;
import com.starshop.service.impl.LoginServiceImpl;
import io.jsonwebtoken.Claims;
import jakarta.annotation.Resource;
import lombok.Setter;
import org.apache.commons.collections.CollectionUtils;
import org.apache.shiro.authc.*;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

@Setter
public class CustomRealm extends AuthorizingRealm {
    private static final Logger log = LoggerFactory.getLogger(CustomRealm.class);

    @Resource
    private LoginServiceImpl userserviceimpl;

    @Resource
    private CopyMapper copyMapper;

    @Override
    public boolean supports(AuthenticationToken token) {
        return token instanceof JwtToken;
    }

    @Override
    @SuppressWarnings("unchecked")
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) throws AuthenticationException {
        JwtToken jwtToken = (JwtToken) authenticationToken;
        String token = (String) jwtToken.getCredentials();
        String userId = (String) jwtToken.getPrincipal();
        Claims claims = jwtToken.getClaims(); // 直接获取 Filter 解析好的 Claims
        // 验证 userId 是否一致（双重校验）
        String jwtUserId =claims.get(JwtClaimsConstant.SYS_USER_ID).toString();
        if (!userId.equals(jwtUserId)) {
            log.error("Token篡改：传入userId={}，JWT解析userId={}", userId, jwtUserId);
            throw new InvalidCredentialsException(MessageConstant.TOKEN_INVALID);
        }

        // 查询用户（带角色和权限）
        String key = RedisKeyConstant.PREFIX_LOGIN+RedisKeyConstant.USER+userId;
        Map<String, Object> userMap = RedisConnector.opsForHash().entries(key);

        // 如果 redis 存储用户信息过期,从数据库更新 redis 缓存
        if (userMap.isEmpty()) {
            SysUser sysUser = userserviceimpl.getSysUserByUserIdWithRolesAndPermissions(Long.valueOf(userId));
            if (Objects.isNull(sysUser)){
                throw new UnknownAccountException(MessageConstant.USER_NOT_LOGIN);

            }
            UserInfo userInfo = copyMapper.usertoUserInfo(sysUser);
            userserviceimpl.setUserInfoToRedis(sysUser, userInfo);
            sysUser.setUserInfo(userInfo);
            return new SimpleAuthenticationInfo(sysUser, token, this.getName());
        }

        if (!userMap.get(SysUser.Fields.isEnable).equals(CommonStatus.ACTIVE.getNumber())) {
            throw new DisabledAccountException(MessageConstant.ACCOUNT_LOCKED);

        }

        List<SysRole> sysRoleList = (List<SysRole>) userMap.get(SysUser.Fields.sysRoleList);
        List<SysPermission> sysPermissionList = (List<SysPermission>) userMap.get(SysUser.Fields.sysPermissionList);
        UserInfo userInfo = (UserInfo) userMap.get(SysUser.Fields.userInfo);

        //TODO :角色权限判断没问题，这里是加数据能通过，后续把空集合删除
        if (CollectionUtils.isEmpty(sysPermissionList)) {
            sysPermissionList =new ArrayList<>();
        }
        if (CollectionUtils.isEmpty(sysRoleList)) {
            sysRoleList =new ArrayList<>();
        }

        if (Objects.isNull(sysRoleList) || Objects.isNull(sysPermissionList) || Objects.isNull(userInfo)) {
            throw new UnknownAccountException(MessageConstant.USER_NOT_LOGIN);

        }
        SysUser user = SysUser.builder()
                .id(Long.valueOf(userId))
                .sysRoleList(sysRoleList)
                .sysPermissionList(sysPermissionList)
                .userInfo(userInfo)
                .build();

        return new SimpleAuthenticationInfo(user, token, this.getName());
    }

    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principalCollection) {
        SysUser sysUser = (SysUser) principalCollection.getPrimaryPrincipal();
        if (Objects.isNull(sysUser)) {
            log.error("授权失败：用户不存在");
            throw new UnknownAccountException(MessageConstant.ACCOUNT_NOT_FOUND);
        }

        // 提取角色和权限
        Set<String> roleNames = sysUser.getSysRoleList().stream()
                .map(SysRole::getRoleName)
                .collect(Collectors.toSet());
        Set<String> permNames = sysUser.getSysPermissionList().stream()
                .map(SysPermission::getPermName)
                .collect(Collectors.toSet());

        SimpleAuthorizationInfo authorizationInfo = new SimpleAuthorizationInfo();
        authorizationInfo.setRoles(roleNames);
        authorizationInfo.setStringPermissions(permNames);

        // 注意：doGetAuthorizationInfo 只有在鉴权（@RequiresRoles）时才会被调用
        // 对于普通接口，认证通过后并不会自动调用这里，所以 BaseContext 必须在认证阶段（doGetAuthenticationInfo）或 Filter 中设置
        return authorizationInfo;
    }
}