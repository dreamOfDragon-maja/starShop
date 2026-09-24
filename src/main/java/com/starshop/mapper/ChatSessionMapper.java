package com.starshop.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.starshop.pojo.entity.ChatSession;
import com.starshop.pojo.vo.ChatSessionVO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface ChatSessionMapper extends BaseMapper<ChatSession> {

    /**
     * 获取当前用户的会话列表 (带联系人昵称、头像、未读数)
     */
    List<ChatSessionVO> selectSessionList(@Param("userId") Long userId);
}
