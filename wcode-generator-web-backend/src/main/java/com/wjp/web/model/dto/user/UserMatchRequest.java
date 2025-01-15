package com.wjp.web.model.dto.user;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户创建请求
 *
 * @author <a href="https://github.com/liwjp">程序员鱼皮</a>
 * @from <a href="https://wjp.icu">编程导航知识星球</a>
 */
@Data
public class UserMatchRequest implements Serializable {
    /**
     * 允许返回心动数据的个数
     */
    private long num;

}