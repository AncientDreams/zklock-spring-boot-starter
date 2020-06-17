package com.xiaoyu.zklock.config;

import com.xiaoyu.zklock.annotation.Lock;
import com.xiaoyu.zklock.core.LogFactory;
import com.xiaoyu.zklock.core.ZookeeperLock;
import org.apache.commons.logging.Log;
import org.apache.zookeeper.ZooKeeper;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import java.security.InvalidParameterException;

/**
 * <p>
 * Aop管理配置类
 * </p>
 *
 * @author ZhangXianYu   Email: 1600501744@qq.com
 * @since 2020-04-06 13:11
 */
@Aspect
public class AopConfig implements LogFactory {

    @Autowired
    private ZookeeperLock zookeeperLockService;

    @Autowired(required = false)
    private ZooKeeper zooKeeper;

    @Autowired
    private LockConfig lockConfig;

    private Log log = org.apache.commons.logging.LogFactory.getLog(AopConfig.class);

    /**
     * 环绕通知 @annotation(locks) 在指定注解标注的方法上加入切点，
     * 加上注解的类必须是通过Spring 来管理注入的
     */
    @Around("@annotation(lock)")
    public Object around(ProceedingJoinPoint proceedingJoinPoint, Lock lock) throws Exception {
        if (zooKeeper == null) {
            throw new Exception("ZooKeeper 连接异常！请检查配置文件和Zookeeper启动状态！");
        } else if (zooKeeper.getState() != ZooKeeper.States.CONNECTED) {
            zooKeeper = new ZooKeeper(lockConfig.getZkAddress(), Integer.parseInt(lockConfig.getTimeOut()), null);
        }
        if (StringUtils.isEmpty(lock.lockName())) {
            throw new InvalidParameterException("锁名称不能为空或者'' !");
        }
        Object obj = null;
        try {
            //超时时间
            int outTime = lock.outTime();
            //子节点位置
            String path = zookeeperLockService.getLock(outTime, zooKeeper, "/" + lock.lockName());
            if (!StringUtils.isEmpty(path)) {
                //获取锁，开始执行业务
                obj = proceedingJoinPoint.proceed();
                if (!zookeeperLockService.removeLock(path, zooKeeper)) {
                    zookeeperLockService.removeLock(path, zooKeeper);
                }
            }
        } catch (Throwable e) {
            error(e.getMessage(), e);
            e.printStackTrace();
        }
        return obj;
    }


    @Override
    public Log setLog() {
        return lockConfig.isEnableLog() ? log : null;
    }
}
