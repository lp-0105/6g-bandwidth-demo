-- ==========================================
-- 校园带宽分享系统 - 初始化数据
-- 三类典型用户：老实分享者A、搭便车者B、摇摆用户C
-- (资金精度已全局放大 100 倍，如 12000 代表 120.00 分)
-- ==========================================

-- 用户A：老实分享者，默认开启分享，高积分高声誉 (12000积分, 95信誉)
INSERT INTO user_bandwidth (username, user_type, credits, reputation, is_sharing, consumed_gb, is_throttled, speed_mbps)
VALUES ('同学A（分享者）', 'SHARER', 12000, 95, TRUE, 0, FALSE, 100);

-- 用户B：精致利己搭便车者，默认关闭分享，积分较少 (8000积分, 70信誉)
INSERT INTO user_bandwidth (username, user_type, credits, reputation, is_sharing, consumed_gb, is_throttled, speed_mbps)
VALUES ('同学B（搭便车者）', 'FREERIDER', 8000, 70, FALSE, 0, FALSE, 100);

-- 用户C：普通摇摆用户，默认关闭分享，演示者可手动切换 (10000积分, 85信誉)
INSERT INTO user_bandwidth (username, user_type, credits, reputation, is_sharing, consumed_gb, is_throttled, speed_mbps)
VALUES ('同学C（摇摆用户）', 'SWING', 10000, 85, FALSE, 0, FALSE, 100);
