package com.starshop.controller.admin;

import com.starshop.service.FeedbackService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class FeedbackController {

    @Resource
    private FeedbackService feedbackService;

}
