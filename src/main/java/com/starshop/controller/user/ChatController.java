package com.starshop.controller.user;

import com.starshop.context.BaseContext;
import com.starshop.pojo.vo.ChatSessionVO;
import com.starshop.result.Result;
import com.starshop.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/chat")
@RequiredArgsConstructor
@Slf4j
public class ChatController {

    private final ChatService chatService;

    /**
     * 获取当前用户的会话列表
     * @return
     */
    @GetMapping("/sessions")
    public Result<List<ChatSessionVO>> getSessionList() {
        Long userId = Long.valueOf(BaseContext.getUserId());
        List<ChatSessionVO> list = chatService.getSessionList(userId);
        return Result.success(list);
    }


    /**
     * 手动清除某会话的未读数
     * @param contactId
     * @return
     */
    @PostMapping("/clearUnread/{contactId}")
    public Result<Void> clearUnread(@PathVariable Long contactId) {
        Long userId = Long.valueOf(BaseContext.getUserId());
        chatService.clearUnread(userId, contactId);
        return Result.success();
    }
}
