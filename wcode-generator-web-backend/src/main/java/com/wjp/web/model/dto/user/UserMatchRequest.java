package com.wjp.web.model.dto.user;

import lombok.Data;

@Data
public class UserMatchRequest {
    /**
     * 允许返回心动数据的个数
     */
    private long num;
}
