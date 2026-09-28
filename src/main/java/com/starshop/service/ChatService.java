package com.starshop.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.entity.ChatMessage;
import com.starshop.pojo.vo.ChatSessionVO;

import java.util.List;

public interface ChatService extends IService<ChatMessage> {

    /**
     * 清除未读数
     */
    void clearUnread(Long userId, Long contactId);

    /**
     * 获取当前用户的会话列表
     * @return
     */
    List<ChatSessionVO> getSessionList(Long userId);

    /**
     * 分页获取历史消息
     */
    Page<ChatMessage> getChatHistory(Long userId, Long contactId, Integer page, Integer size);

    /**
     * 发送并持久化消息
     */
    ChatMessage saveAndGetMessage(Long fromUserId, Long toUserId, String content, Integer msgType, Long productId);
}
