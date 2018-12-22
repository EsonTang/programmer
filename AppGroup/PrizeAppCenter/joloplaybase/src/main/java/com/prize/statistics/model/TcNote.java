package com.prize.statistics.model;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;
import java.io.Serializable;

/**
 * 存入数据的对象
 */
@Table(name = "TcNote")
public class TcNote implements Serializable {
    @Column(name = "id", autoGen = true, isId = true)
    public int id;
    @Column(name = "firstCloumn")//一些事件
    public String firstCloumn;
    @Column(name = "secondCloumn")
    public String secondCloumn;//存储曝光统计
    @Column(name = "thirdCloumn")
    public String thirdCloumn;//存储下载统计
    @Column(name = "fourthCloumn")
    public String fourthCloumn;//预留一个(3.2add 连接曾慧新后台曝光 )
    @Column(name = "fifthCloumn")
    public String fifthCloumn;//预留一个(3.2add 连接曾慧新后台下载数据 )
    @Column(name = "timeStamp")
    public long timeStamp;


}
