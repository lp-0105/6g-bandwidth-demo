-- ==========================================
-- 校园带宽分享系统 - 数据库表结构
-- ==========================================
DROP TABLE IF EXISTS user_bandwidth;

CREATE TABLE user_bandwidth (
    id           BIGINT       PRIMARY KEY AUTO_INCREMENT COMMENT '用户ID',
    username     VARCHAR(50)  NOT NULL                   COMMENT '用户名',
    user_type    VARCHAR(20)  NOT NULL                   COMMENT '用户类型: SHARER/FREERIDER/SWING',
    credits      INT          NOT NULL DEFAULT 100       COMMENT '积分余额',
    reputation   INT          NOT NULL DEFAULT 100       COMMENT '声誉分 (0-100)',
    is_sharing   BOOLEAN      NOT NULL DEFAULT FALSE     COMMENT '是否开启带宽分享',
    consumed_gb  INT          NOT NULL DEFAULT 0         COMMENT '今日已消耗流量(GB) - 合法范围',
    freeride_gb  INT          NOT NULL DEFAULT 0         COMMENT '违规搭便车白嫖流量记录(GB)',
    is_throttled BOOLEAN      NOT NULL DEFAULT FALSE     COMMENT '是否被限速',
    speed_mbps   INT          NOT NULL DEFAULT 100       COMMENT '当前网速(Mbps)'
);
