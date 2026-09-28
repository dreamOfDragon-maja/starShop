package com.starshop.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.mapper.ChatMessageMapper;
import com.starshop.mapper.ChatSessionMapper;
import com.starshop.pojo.entity.ChatMessage;
import com.starshop.pojo.entity.ChatSession;
import com.starshop.pojo.vo.ChatSessionVO;
import com.starshop.service.ChatService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatService {

    private final ChatSessionMapper chatSessionMapper;

    /**
     * 获取当前用户的会话列表
     * @return
     */
    @Override
    public List<ChatSessionVO> getSessionList(Long userId) {
        return chatSessionMapper.selectSessionList(userId);
    }

    /**
     * 分页获取历史消息
     */
    @Override
    public Page<ChatMessage> getChatHistory(Long userId, Long contactId, Integer page, Integer size) {
        Page<ChatMessage> chatPage = new Page<>(page, size);
        return this.page(chatPage,new LambdaQueryWrapper<ChatMessage>()
                .and(wrapper -> wrapper
                        .eq(ChatMessage::getFromUserId,userId).eq(ChatMessage::getToUserId,contactId)
                        .or()
                        .eq(ChatMessage::getFromUserId,contactId).eq(ChatMessage::getToUserId,userId)
                        .orderByDesc(ChatMessage::getCreateTime)));
    }

    /**
     * 清除未读数
     */
    @Override
    public void clearUnread(Long userId, Long contactId) {
        Long uid = Math.min(userId, contactId);
        Long cid = Math.max(userId, contactId);

        LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<ChatSession>()
                .eq(ChatSession::getUserId, uid)
                .eq(ChatSession::getContactId, cid);

        if (userId.equals(uid)) {
            updateWrapper.set(ChatSession::getUnreadCountA, 0);
        } else {
            updateWrapper.set(ChatSession::getUnreadCountB, 0);
        }
        chatSessionMapper.update(null, updateWrapper);
    }
}
