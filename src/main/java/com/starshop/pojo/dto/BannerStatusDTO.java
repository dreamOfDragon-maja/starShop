package com.starshop.pojo.dto;

import com.starshop.pojo.enums.BannerStatus;
import lombok.Data;

@Data
public class BannerStatusDTO {

    String id;

    BannerStatus status;
}
