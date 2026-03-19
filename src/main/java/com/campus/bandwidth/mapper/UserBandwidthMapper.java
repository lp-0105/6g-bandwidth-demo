package com.campus.bandwidth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.campus.bandwidth.entity.UserBandwidth;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户带宽状态 Mapper 接口
 * 继承 BaseMapper，自动获得 CRUD 能力，无需写 XML
 */
@Mapper
public interface UserBandwidthMapper extends BaseMapper<UserBandwidth> {
        // MyBatis-Plus 已提供基础 CRUD，无需额外定义
}
