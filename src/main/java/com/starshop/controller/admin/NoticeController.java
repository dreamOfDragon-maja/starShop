package com.starshop.controller.admin;

import com.starshop.pojo.dto.NoticeDTO;
import com.starshop.result.Result;
import com.starshop.service.NoticeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class NoticeController {

    @Resource
    private NoticeService noticeService;

    /**
     * 添加通知
     * @return
     */
    @PostMapping("/admin/notice/add")
    public Result addNotice(@RequestBody NoticeDTO noticeDTO) {
        return noticeService.addNotice(noticeDTO);
    }
}
