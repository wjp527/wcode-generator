package com.wjp.web.config;

import com.xxl.job.core.executor.impl.XxlJobSpringExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * xxl-job config
 * @author wjp
 */
@Configuration
@Slf4j
public class XxlJobConfig {

    // XXL-JOB 的调度中心地址，用于和执行器通信。
    @Value("${xxl.job.admin.addresses}")
    private String adminAddresses;

    // XXL-JOB 的通信令牌，用于身份认证。
    @Value("${xxl.job.accessToken}")
    private String accessToken;

    // 执行器的应用名称。
    @Value("${xxl.job.executor.appname}")
    private String appname;

    // 执行器的固定地址（可选）。
    @Value("${xxl.job.executor.address}")
    private String address;

    // 执行器的 IP 地址。
    @Value("${xxl.job.executor.ip}")
    private String ip;

    // 执行器的端口号。
    @Value("${xxl.job.executor.port}")
    private int port;

    // 日志保存路径。
    @Value("${xxl.job.executor.logpath}")
    private String logPath;

    // 日志保留天数。
    @Value("${xxl.job.executor.logretentiondays}")
    private int logRetentionDays;


    /**
     * 基于配置文件中的参数，完成 XXL-JOB 执行器的初始化和配置。
     * 它让执行器能够与调度中心通信，并根据调度中心的指令执行任务。
     * @return
     */
    @Bean
    public XxlJobSpringExecutor xxlJobExecutor() {
        log.info(">>>>>>>>>>> xxl-job config init.");
        // 创建一个 XxlJobSpringExecutor 对象实例
        XxlJobSpringExecutor xxlJobSpringExecutor = new XxlJobSpringExecutor();
        // 将配置文件中的值注入到执行器中
        xxlJobSpringExecutor.setAdminAddresses(adminAddresses);
        xxlJobSpringExecutor.setAppname(appname);
        xxlJobSpringExecutor.setAddress(address);
        xxlJobSpringExecutor.setIp(ip);
        xxlJobSpringExecutor.setPort(port);
        xxlJobSpringExecutor.setAccessToken(accessToken);
        xxlJobSpringExecutor.setLogPath(logPath);
        xxlJobSpringExecutor.setLogRetentionDays(logRetentionDays);

        return xxlJobSpringExecutor;
    }

}