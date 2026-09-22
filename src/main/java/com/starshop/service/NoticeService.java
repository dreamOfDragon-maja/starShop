package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.dto.NoticeDTO;
import com.starshop.pojo.entity.Notice;
import com.starshop.result.Result;

public interface NoticeService extends IService<Notice> {

    /**
     * 添加通知
     * @return
     */
    Result addNotice(NoticeDTO noticeDTO);
}
