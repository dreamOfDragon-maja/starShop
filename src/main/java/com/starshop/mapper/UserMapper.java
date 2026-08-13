package com.starshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starshop.pojo.entity.SysUser;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<SysUser> {
    SysUser getSysUserByNameWithRolesAndPermissions(String username);

    SysUser getSysUserByUserIdWithRolesAndPermissions(Long userId);

    SysUser getSysUserByOpenidWithRolesAndPermissions(String openid);

    void insertSysUserConnectSysRole(Long id, int id1);
}
