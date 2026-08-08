package com.starshop.controller.common;

import com.starshop.common.utils.AliyunOSSUtils;
import com.starshop.result.Result;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

/**
 *  文件上传
 */
@RestController
@RequestMapping("/api")
public class CommonController {

    @Resource
    private AliyunOSSUtils aliyunOSSUtils;

    @PostMapping("/upload/image")
    public Result<String> upload(MultipartFile file) throws Exception {
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        String objectName = UUID.randomUUID().toString() + extension;
        String url = aliyunOSSUtils.upload(file.getBytes(), objectName);
        return Result.success(url);
    }
}
