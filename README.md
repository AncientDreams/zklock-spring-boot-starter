# zklock-spring-boot-starter
基于Zookeeper的高性能分布式锁，简单接入项目，便可以使项目拥有分布式锁的能力，对原代码的低侵入，目前暂时只支持Spring boot项目。
锁的类型是：不可重入且公平锁！
# 快速开始
1.指定仓库

    <repositories>
        <repository>
            <id>jitpack.io</id>
            <url>https://jitpack.io</url>
        </repository>
    </repositories>

2.引入依赖

    <dependencies>
         <dependency>
             <groupId>com.github.AncientDreams</groupId>
             <artifactId>zklock-spring-boot-starter</artifactId>
             <version>1.0.1</version>
         </dependency>
     </dependencies>
     
# 参数说明
> 1.配置参数说明

```properties
xiaoyu.lock.zk-address : zookeeper链接地址，多个用“,”分开。
xiaoyu.lock.enable-log : 是否开启日志打印，默认false。开启后会输出日志，方便调试！
xiaoyu.lock.time-out : zookeeper链接超时时间，默认 60000 毫秒。
```

> 2.注解参数说明
```properties
@Lock注解包含两个参数

outTime：锁的超时时间，默认6000毫秒，超时抛出异常。
lockName：锁的名称！
```

# 配置方法
1.在配置文件yml或者properties中指定好连接属性。<br/>
列如：xiaoyu.lock.zk-address=zookeeper地址<br/>
2.在需要使用的方法头上加上 @Lock 注解，指定锁名称和锁的超时时间。<br/>


