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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl extends ServiceImpl<ChatMessageMapper, ChatMessage> implements ChatService {

    private final ChatSessionMapper chatSessionMapper;

    /**
     * 发送并持久化消息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public ChatMessage saveAndGetMessage(Long fromUserId, Long toUserId, String content, Integer msgType, Long productId) {
        //保存消息
        ChatMessage message = ChatMessage.builder()
                .fromUserId(fromUserId)
                .toUserId(toUserId)
                .content(content)
                .msgType(msgType)
                .productId(productId)
                .isRead(0)
                .createTime(LocalDateTime.now())
                .build();
        this.save(message);

        //更新或创建会话
        Long userId = Math.min(fromUserId, toUserId);
        Long contactId = Math.max(fromUserId, toUserId);

        //根据对话双方id查询会话记录
        ChatSession session = chatSessionMapper.selectOne(new LambdaQueryWrapper<ChatSession>()
                .eq(ChatSession::getContactId, contactId)
                .eq(ChatSession::getUserId, userId));

        if (Objects.isNull(session)) {
            //新增
            session = ChatSession.builder()
                    .userId(userId)
                    .contactId(contactId)
                    .lastMsgContent(content)
                    .lastMsgTime(LocalDateTime.now())
                    .unreadCountA(fromUserId.equals(contactId) ? 1 : 0) // 如果发送者是较大ID，则较小ID(A)未读+1
                    .unreadCountB(fromUserId.equals(userId) ? 1 : 0)    // 如果发送者是较小ID，则较大ID(B)未读+1
                    .build();
            chatSessionMapper.insert(session);
        }else {
            //更新
            LambdaUpdateWrapper<ChatSession> updateWrapper = new LambdaUpdateWrapper<ChatSession>()
                    .eq(ChatSession::getId, session.getId())
                    .set(ChatSession::getLastMsgContent, content)
                    .set(ChatSession::getLastMsgTime, LocalDateTime.now());

            if (fromUserId.equals(userId)) {
                // 发送者是 A，则 B 未读数 + 1
                updateWrapper.setSql("unread_count_b = unread_count_b + 1");
            } else {
                // 发送者是 B，则 A 未读数 + 1
                updateWrapper.setSql("unread_count_a = unread_count_a + 1");
            }
            chatSessionMapper.update(null,updateWrapper);
        }

        return message;
    }

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
