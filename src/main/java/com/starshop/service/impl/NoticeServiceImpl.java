package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.constant.MessageConstant;
import com.starshop.mapper.NoticeMapper;
import com.starshop.pojo.dto.NoticeDTO;
import com.starshop.pojo.entity.Notice;
import com.starshop.result.Result;
import com.starshop.service.NoticeService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl extends  ServiceImpl<NoticeMapper, Notice> implements NoticeService {

    @Resource
    private CopyMapper copyMapper;


    /**
     * 添加通知
     * @return
     */
    @Override
    public Result addNotice(NoticeDTO noticeDTO) {
        Notice notice = copyMapper.noticeDTOToNotice(noticeDTO);
        boolean isSuccess = save(notice);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        return Result.success(notice);
    }
}
