package com.starshop.controller.user;

import com.starshop.pojo.dto.AddressDTO;
import com.starshop.result.Result;
import com.starshop.service.AddressService;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/address")
public class AddressController {

    @Resource
    private AddressService addressService;

    /**
     * 新增地址
     * @param addressDTO
     * @return
     */
    @PostMapping("/add")
    public Result insertAddress(@RequestBody @Valid AddressDTO addressDTO) {
        return addressService.insertAddress(addressDTO);

    }

    /**
     * 查询地址列表
     * @return
     */
    @GetMapping("/list")
    public Result getAddressList() {
        return addressService.getAddressList();
    }

    /**
     * 修改地址
     * @param addressDTO
     * @return
     */
    @PutMapping("/update")
    public Result updateAddress(@RequestBody @Valid AddressDTO addressDTO) {
        return addressService.updateAddress(addressDTO);

    }

}
