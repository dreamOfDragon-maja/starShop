package com.starshop.controller.admin;

import com.starshop.pojo.dto.FeedbackDTO;
import com.starshop.result.Result;
import com.starshop.service.FeedbackService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class FeedbackController {

    @Resource
    private FeedbackService feedbackService;

    /**
     * 用户提交反馈
     * @param feedbackDTO
     * @return
     */
    @PostMapping("/feedback/add")
    public Result<Object> addFeedback(@RequestBody @NotNull FeedbackDTO feedbackDTO){
        return feedbackService.addFeedback(feedbackDTO);
    }
}
