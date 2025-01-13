package com.wjp.web.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wjp.web.annotation.AuthCheck;
import com.wjp.web.common.BaseResponse;
import com.wjp.web.common.DeleteRequest;
import com.wjp.web.common.ErrorCode;
import com.wjp.web.common.ResultUtils;
import com.wjp.web.constant.UserConstant;
import com.wjp.web.exception.BusinessException;
import com.wjp.web.exception.ThrowUtils;
import com.wjp.web.model.dto.user.*;
import com.wjp.web.model.entity.User;
import com.wjp.web.model.vo.LoginUserVO;
import com.wjp.web.model.vo.UserVO;
import com.wjp.web.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.awt.*;
import java.util.List;

import static com.wjp.web.service.impl.UserServiceImpl.SALT;

/**
 * 健康检查接口
 *
 * @author <a href="https://github.com/liwjp">程序员鱼皮</a>
 * @from <a href="https://wjp.icu">编程导航知识星球</a>
 */
@RestController
@RequestMapping("/health")
@Slf4j
public class HealthController {

    /**
     * 测试接口
     *
     * @return
     */
    @GetMapping
    public String userRegister() {
        return "ok";
    }


}
