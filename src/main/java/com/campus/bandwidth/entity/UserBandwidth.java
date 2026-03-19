package com.campus.bandwidth.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 用户带宽状态实体类
 * 对应数据库表 user_bandwidth
 */
@Data
@TableName("user_bandwidth")
public class UserBandwidth {

    /** 用户ID（自增主键） */
    @TableId(type = IdType.AUTO)
    private Long id;

    /** 用户名称（如：同学A） */
    private String username;

    /** 用户类型：SHARER-分享者 / FREERIDER-搭便车者 / SWING-摇摆用户 */
    private String userType;

    /** 当前积分余额（初始100，分享可赚取，消耗流量扣除） */
    private Integer credits;

    /** 声誉分 0-100（合作行为提升，搭便车降低） */
    private Integer reputation;

    /** 是否开启带宽分享（true=已开启） */
    private Boolean isSharing;

    /** 今日合规消耗流量（GB，即开启共享时的消耗） */
    private Integer consumedGb;

    /** 违规流量案底（GB，未开启共享时的消耗，结算无视突击切开关，有案底重罚） */
    private Integer freerideGb;

    /** 是否被限速（true=被限至2Mbps，积分耗尽或结算惩罚触发） */
    private Boolean isThrottled;

    /** 当前网速（Mbps）：正常100，限速后2 */
    private Integer speedMbps;
}
