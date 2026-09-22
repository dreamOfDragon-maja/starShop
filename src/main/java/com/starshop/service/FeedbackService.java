package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.dto.FeedbackDTO;
import com.starshop.pojo.entity.Feedback;
import com.starshop.result.Result;
import jakarta.validation.constraints.NotNull;

public interface FeedbackService extends IService<Feedback> {
    /**
     * 用户提交反馈
     * @param feedbackDTO
     * @return
     */
    Result<Object> addFeedback(@NotNull FeedbackDTO feedbackDTO);
}
