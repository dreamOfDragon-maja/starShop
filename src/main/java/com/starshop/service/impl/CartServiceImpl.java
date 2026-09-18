package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.mapper.CartMapper;
import com.starshop.pojo.entity.Cart;
import com.starshop.service.CartService;
import org.springframework.stereotype.Service;

@Service
public class CartServiceImpl extends ServiceImpl<CartMapper, Cart> implements CartService {
}
