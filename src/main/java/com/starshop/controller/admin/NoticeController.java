package com.starshop.controller.admin;

import com.starshop.pojo.dto.NoticeDTO;
import com.starshop.result.Result;
import com.starshop.service.NoticeService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

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

    /**
     * 获取最新 notice
     * @param limit
     * @return
     */
    @GetMapping("/notice/latest")
    public Result getLatestNotice(@RequestParam(value = "limit", defaultValue = "5") Integer limit) {
        return noticeService.getLatestNotice(limit);
    }

    /**
     * 更新通知
     * @param noticeDTO
     * @return
     */
    @PutMapping("/admin/notice/update")
    public Result updateNotice(@RequestBody NoticeDTO noticeDTO) {
        return noticeService.updateNotice(noticeDTO);
    }
}
