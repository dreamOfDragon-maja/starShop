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


    /**
     * 获取最新 notice
     * @param limit
     * @return
     */
    Result getLatestNotice(Integer limit);

    /**
     * 更新通知
     * @param noticeDTO
     * @return
     */
    Result updateNotice(NoticeDTO noticeDTO);

    /**
     * 根据指定 id删除通知
     * @param id
     * @return
     */
    Result deleteNotice(String id);
}
