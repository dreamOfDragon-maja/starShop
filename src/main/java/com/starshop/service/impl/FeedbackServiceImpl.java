package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.constant.MessageConstant;
import com.starshop.context.BaseContext;
import com.starshop.mapper.FeedbackMapper;
import com.starshop.pojo.dto.FeedbackDTO;
import com.starshop.pojo.entity.Feedback;
import com.starshop.result.Result;
import com.starshop.service.FeedbackService;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class FeedbackServiceImpl extends ServiceImpl<FeedbackMapper, Feedback> implements FeedbackService {

    /**
     * 用户提交反馈
     * @param feedbackDTO
     * @return
     */
    @Override
    public Result<Object> addFeedback(FeedbackDTO feedbackDTO) {
        String userId = BaseContext.getUserId();
        Feedback feedback = Feedback.builder()
                .userId(Long.valueOf(userId))
                .content(feedbackDTO.getContent())
                .imageUrls(feedbackDTO.getImages())
                .contact(feedbackDTO.getContact())
                .createTime(LocalDateTime.now())
                .build();
        boolean isSuccess = save(feedback);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);

        }
        return Result.success();
    }
}
