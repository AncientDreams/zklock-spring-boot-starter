package com.xiaoyu.zklock.core;


import com.xiaoyu.zklock.config.LockConfig;
import org.apache.commons.logging.Log;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.ZooKeeper;
import org.apache.zookeeper.data.Stat;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * <p>
 * Zookeeper 分布式锁服务
 * </p>
 *
 * @author ZhangXianYu   Email: 1600501744@qq.com
 * @since 2020-04-03 14:09
 */
public class ZookeeperLock implements com.xiaoyu.zklock.core.LogFactory {

    @Autowired
    private LockConfig lockConfig;

    /**
     * 获取锁
     *
     * @param outTime   锁超时时间
     * @param zooKeeper zk连接
     * @param path      父节点位置
     * @return 子节点位置
     * @throws Exception 异常
     */
    public String getLock(int outTime, ZooKeeper zooKeeper, String path) throws Exception {
        String logStr = "锁名称：" + path.substring(1) + "，";

        if (outTime == 0) {
            throw new Exception("超时时间不能为0!");
        }

        String createPath = createZookeeperPath(path, zooKeeper);
        if (createPath == null) {
            throw new Exception("节点创建失败！");
        }
        info(logStr + "子节点创建成功:" + createPath);

        try {
            if (!checkState(path, createPath, zooKeeper)) {
                //未拿到锁，监控节点事件
                if (!getLockOrWait(path, createPath, zooKeeper, outTime)) {
                    //删除节点，避免死锁
                    if (!removeLock(createPath, zooKeeper)) {
                        //失败重试
                        removeLock(createPath, zooKeeper);
                    }
                    //超时
                    return null;
                }
            }
            info(logStr + createPath + " : 获取锁成功！");
            return createPath;
        } catch (Exception e) {
            error(logStr + e.getMessage(), e);
            e.printStackTrace();
            //删除节点
            removeLock(createPath, zooKeeper);
            return null;
        }
    }

    public boolean removeLock(String createPath, ZooKeeper zooKeeper) {
        try {
            if (!ZooKeeper.States.CLOSED.equals(zooKeeper.getState())) {
                if (zooKeeper.exists(createPath, true) != null) {
                    zooKeeper.delete(createPath, -1);
                } else {
                    error(createPath + ":节点不存在，无法删除！");
                }
            }
            info(createPath + ":节点删除成功，锁释放！");
            return true;
        } catch (Exception e) {
            error(e.getMessage(), e);
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 创建 Zookeeper 节点
     *
     * @param path      父节点位置
     * @param zooKeeper zk 连接
     * @return 如果创建成功，返回节点位置
     * @throws Exception 异常
     */
    private String createZookeeperPath(String path, ZooKeeper zooKeeper)
            throws Exception {
        //如果不存在父节点 先创建一个父节点
        synchronized (this) {
            try {
                if (null == zooKeeper.exists(path, false)) {
                    //EPHEMERAL 临时节点，PERSISTENT 永久节点，临时节点随着zk会话消失而消失
                    zooKeeper.create(path, null, ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                }
            } catch (Exception ignored) {
            }
        }
        //建立一个顺序临时节点
        return zooKeeper.create(path.concat("/1"), null, ZooDefs.Ids.OPEN_ACL_UNSAFE,
                CreateMode.EPHEMERAL_SEQUENTIAL);
    }


    /**
     * 检查是否拿到锁
     *
     * @param path      父节点位置
     * @param lockName  请求子节点位置
     * @param zooKeeper zk 连接
     * @return 是否拿到
     */
    private boolean checkState(String path, String lockName, ZooKeeper zooKeeper) {
        List<String> childList;
        try {
            childList = zooKeeper.getChildren(path, false);
            Collections.sort(childList);
            String[] myStr = lockName.split("/");
            return childList.get(0).equals(myStr[2]);
        } catch (Exception e) {
            return false;
        }
    }


    /**
     * 获取锁，没有则等待，获取失败或者超时抛出异常，成功接着执行代码
     *
     * @param path      父节点位置
     * @param lockName  请求子节点位置
     * @param zooKeeper zk 连接
     * @param outTime   超时时间，单位毫秒
     * @throws Exception 异常
     */
    private boolean getLockOrWait(String path, String lockName, ZooKeeper zooKeeper, long outTime) throws
            Exception {
        AtomicBoolean resBoolean = new AtomicBoolean(false);
        long startTime = System.currentTimeMillis();

        List<String> childList = zooKeeper.getChildren(path, false);
        String[] myStr = lockName.split("/");
        Collections.sort(childList);

        int i = childList.indexOf(myStr[2]);
        if (i == 0) {
            if (System.currentTimeMillis() - startTime >= outTime) {
                //超时
                throw new Exception("获取锁超时 !" + lockName);
            }
            return true;
        }

        //得到前面的一个节点
        String headId = childList.get(i - 1);

        String headPath = path + "/" + headId;

        info("锁名称：" + path.substring(1) + "，" + myStr[2] + " 向添加监听：" + headPath);

        Stat stat = zooKeeper.exists(headPath, event -> {
            try {
                //获取到锁。
                do {
                    long time = System.currentTimeMillis() - startTime;
                    if (time >= outTime) {
                        //超时
                        throw new Exception("获取锁超时 !" + lockName);
                    }
                } while (!checkState(path, lockName, zooKeeper));

                resBoolean.set(true);
            } catch (Exception e) {
                resBoolean.set(false);
            }
        });
        if (stat == null) {
            //节点不存在，锁已经释放
            return true;
        }
        //线程堵塞，如果超时时间内未能进入节点监控，返回失败。
        while (!resBoolean.get()) {
            //判断是否超时
            long time = System.currentTimeMillis() - startTime;
            if (time >= outTime) {
                //超时
                throw new Exception("获取锁超时 !" + lockName);
            }
            Thread.sleep(10);
        }

        return resBoolean.get();
    }

    @Override
    public Log setLog() {
        return lockConfig.isEnableLog() ? org.apache.commons.logging.LogFactory.getLog(ZookeeperLock.class) : null;
    }
}
