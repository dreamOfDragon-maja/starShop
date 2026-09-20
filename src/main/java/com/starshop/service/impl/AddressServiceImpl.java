package com.starshop.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.starshop.common.mapstruct.CopyMapper;
import com.starshop.constant.MessageConstant;
import com.starshop.context.BaseContext;
import com.starshop.mapper.AddressMapper;
import com.starshop.pojo.dto.AddressDTO;
import com.starshop.pojo.entity.Address;
import com.starshop.pojo.enums.CommonDefault;
import com.starshop.result.Result;
import com.starshop.service.AddressService;
import jakarta.annotation.Resource;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AddressServiceImpl extends ServiceImpl<AddressMapper, Address> implements AddressService {

    @Resource
    private CopyMapper copyMapper;


    /**
     * 查询地址列表
     * @return
     */
    @Override
    public Result getAddressList() {
        String userId = BaseContext.getUserId();
        List<Address> list = lambdaQuery().eq(Address::getUserId, userId).list();
        return Result.success(list);
    }

    /**
     * 新增地址
     * @param addressDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result insertAddress(AddressDTO addressDTO) {
        String userId = BaseContext.getUserId();
        Address address = copyMapper.addressDTOToAddress(addressDTO);
        //设置用户id
        address.setUserId(Long.valueOf(userId));
        //获取当前对象的aop代理对象
        AddressServiceImpl addressService = (AddressServiceImpl) AopContext.currentProxy();
        addressService.makeOnlyHaveOneDefault(addressDTO, userId);

        boolean isSuccess = save(address);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        return Result.success(address);

    }

    /**
     * 修改地址
     * @param addressDTO
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public Result updateAddress(AddressDTO addressDTO) {
        String userId = BaseContext.getUserId();
        Address address = copyMapper.addressDTOToAddress(addressDTO);
        AddressServiceImpl addressService = (AddressServiceImpl) AopContext.currentProxy();
        addressService.makeOnlyHaveOneDefault(addressDTO, userId);
        address.setUserId(Long.valueOf(userId));
        boolean isSuccess = updateById(address);
        if (!isSuccess) {
            return Result.error(MessageConstant.SQL_MESSAGE_SAVE_ERROR);
        }
        return Result.success(address);

    }

    /**
     * 保证只有一个默认地址
     * @param addressDTO
     * @param userId
     */
    @Transactional(rollbackFor = Exception.class)
    void makeOnlyHaveOneDefault(AddressDTO addressDTO, String userId) {
        //标记，是否存在默认地址，默认地址id
        boolean isHaveDefaultInList = false;
        long haveDefaultId = -1;
        //如果为默认地址
        if (addressDTO.getIsDefault().getIsDefault()) {
            //查询该用户下的所有地址
            List<Address> addressList = lambdaQuery().eq(Address::getUserId, userId).list();
            if (CollectionUtils.isNotEmpty(addressList)) {
                for (Address addressInTheList : addressList) {
                    //如果原来地址中有默认地址，记录地址id
                    if (addressInTheList.getIsDefault().getIsDefault()) {
                        isHaveDefaultInList = true;
                        haveDefaultId = addressInTheList.getId();
                    }
                }

            }
        }
        //将这条地址修改为非默认
        if (isHaveDefaultInList) {
            lambdaUpdate().eq(Address::getUserId, userId).eq(Address::getId, haveDefaultId)
                    .set(Address::getIsDefault, CommonDefault.NO_DEFAULT.getNumber()).update();
        }
    }
}
