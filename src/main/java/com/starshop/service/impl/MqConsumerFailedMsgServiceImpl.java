package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.mapper.MqConsumerFailedMsgMapper;
import com.starshop.pojo.entity.MqConsumerFailedMsg;
import com.starshop.service.MqConsumerFailedMsgService;
import org.springframework.stereotype.Service;

@Service
public class MqConsumerFailedMsgServiceImpl extends ServiceImpl<MqConsumerFailedMsgMapper, MqConsumerFailedMsg> implements MqConsumerFailedMsgService {
}
