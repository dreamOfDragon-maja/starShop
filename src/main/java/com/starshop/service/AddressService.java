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

    /**
     * 查询地址列表
     * @return
     */
    Result getAddressList();

    /**
     * 修改地址
     * @param addressDTO
     * @return
     */
    Result updateAddress(@Valid AddressDTO addressDTO);

    /**
     * 删除地址
     * @param id
     * @return
     */
    Result deleteAddress(String id);
}
