package com.starshop.controller.user;

import com.starshop.pojo.dto.UserDetailDTO;
import com.starshop.result.Result;
import com.starshop.service.CollectionService;
import com.starshop.service.UserService;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private CollectionService collectionService;

    /**
     * 获取用户详情
     * @return
     */
    @GetMapping("/detail/get")
    public Result getUserDetail() {
        return userService.getUserDetail();
    }

    /**
     * 更改用户详情
     * @param userDetailDTO
     * @return
     */
    @PutMapping("/detail/update")
    public Result updateUserDetail(@RequestBody @NotNull UserDetailDTO userDetailDTO) {
        return userService.updateUserDetail(userDetailDTO);
    }

    /**
     * 新增收藏
     * @param productId
     * @return
     */
    @PostMapping("/collect/add")
    public Result addCollection(@RequestParam @NotBlank String productId) {
        return collectionService.addCollection(productId);
    }

    /**
     * 删除收藏
     * 支持单个或批量删除收藏的商品（商品ID以逗号分隔）
     * @param productIds
     * @return
     */
    @DeleteMapping("/collect/delete")
    public Result deleteCollection(@RequestParam String productIds) {
        return collectionService.deleteCollection(productIds);
    }
}
