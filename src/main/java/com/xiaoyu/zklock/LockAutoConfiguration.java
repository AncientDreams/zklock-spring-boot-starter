package com.xiaoyu.zklock;


import com.xiaoyu.zklock.config.AopConfig;
import com.xiaoyu.zklock.config.LockConfig;
import com.xiaoyu.zklock.core.ZookeeperLock;
import org.apache.zookeeper.ZooKeeper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

import java.io.IOException;

/**
 * <p>
 *  zkLock 自动装配
 * </p>
 *
 * @author ZhangXianYu   Email: 1600501744@qq.com
 * @since 2020-04-03 14:21
 */
@Configuration
//确认开启配置类
@EnableConfigurationProperties(LockConfig.class)
//导入AOP类
@Import({AopConfig.class})
public class LockAutoConfiguration {

    @Autowired
    private LockConfig lockConfig;

    @Bean
    @ConditionalOnMissingBean
    ZookeeperLock lockConfig() {
        return new ZookeeperLock();
    }

    @Bean
    ZooKeeper zooKeeper() throws IOException {
        if(StringUtils.isEmpty(lockConfig.getZkAddress())){
            return null;
        }
        return new ZooKeeper(lockConfig.getZkAddress(), Integer.parseInt(lockConfig.getTimeOut()), event -> {});
    }


}
