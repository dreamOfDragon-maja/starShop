package com.starshop.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.starshop.pojo.dto.AddressDTO;
import com.starshop.pojo.entity.Address;
import com.starshop.result.Result;
import jakarta.validation.Valid;

public interface AddressService extends IService<Address> {

    /**
     * 新增地址
     * @param addressDTO
     * @return
     */
    Result insertAddress(@Valid AddressDTO addressDTO);

}
