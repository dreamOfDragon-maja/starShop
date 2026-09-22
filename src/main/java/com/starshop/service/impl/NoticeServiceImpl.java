package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.mapper.NoticeMapper;
import com.starshop.pojo.entity.Notice;
import com.starshop.service.NoticeService;
import org.springframework.stereotype.Service;

@Service
public class NoticeServiceImpl extends  ServiceImpl<NoticeMapper, Notice> implements NoticeService {
}
