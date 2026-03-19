package com.campus.bandwidth.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.campus.bandwidth.entity.UserBandwidth;
import com.campus.bandwidth.mapper.UserBandwidthMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 带宽分享激励系统 - 核心业务服务
 * 实现积分计算、声誉更新、结算惩罚等博弈论机制
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserBandwidthService {

    private final UserBandwidthMapper userMapper;

    // ===== 常量定义 (资金精度全部放大 100 倍以支持整数折损结算) =====
    /** 每 GB 流量消耗的积分（消费者支付）: 600 分 (即 6.00) */
    private static final int COST_PER_GB = 600;
    /** 每 GB 流量全网可瓜分的基础总奖金池: 500 分 (即 5.00) */
    private static final int REWARD_POOL_PER_GB = 500;
    /** 搭便车惩罚：每 GB 额外扣除积分: 800 分 (即 8.00) */
    private static final int PENALTY_PER_GB = 800;
    /** 正常网速（Mbps） */
    private static final int SPEED_NORMAL = 100;
    /** 限速后的网速（Mbps）—— 惩罚降速 */
    private static final int SPEED_THROTTLED = 2;
    /** 信誉分单次奖励 */
    private static final int REPUTATION_REWARD = 5;
    /** 信誉分单次惩罚 */
    private static final int REPUTATION_PENALTY = 20;

    /**
     * 查询所有用户当前状态
     */
    public List<UserBandwidth> getAllUsers() {
        return userMapper.selectList(null);
    }

    /**
     * 模拟用户消耗 1GB 流量
     * 逻辑：
     * 1. 检查积分是否充足
     * 2. 扣除消费者的积分
     * 3. 若有积极分享者在线（例如A），则为其增加对应积分（模拟流量从A处获取）
     *
     * @param userId 消费者ID
     * @return 操作结果说明
     */
    @Transactional
    public Map<String, Object> consumeTraffic(Long userId) {
        UserBandwidth user = userMapper.selectById(userId);
        if (user == null) {
            return result(false, "用户不存在");
        }

        // 拦截逻辑：如果积分不足抵扣 1GB 的消耗
        if (user.getCredits() < COST_PER_GB) {
            // 如果没红灯限速，此时触发红灯并保存
            if (!Boolean.TRUE.equals(user.getIsThrottled())) {
                user.setIsThrottled(true);
                user.setSpeedMbps(SPEED_THROTTLED);
                userMapper.updateById(user);
            }
            return result(false, "⚠️ <span class='text-danger'>操作拒绝：余额不足！(剩余 " + user.getCredits()
                    + " 分)。已强制断网/降速，请立即开启分享赚取积分！</span>");
        }

        // 1. 扣除消费者积分
        int newCredits = user.getCredits() - COST_PER_GB;
        user.setCredits(newCredits);
        
        // 【补丁防御：日切瞬时漏洞】行为快照审计
        if (Boolean.TRUE.equals(user.getIsSharing())) {
            user.setConsumedGb(user.getConsumedGb() + 1); // 开启共享的属于合法合规消耗
        } else {
            user.setFreerideGb(user.getFreerideGb() + 1); // 关闭共享的属于违规搭便车，被记入案底
        }

        // 积分不足则立即限速惩罚
        if (newCredits < COST_PER_GB) {
            user.setIsThrottled(true);
            user.setSpeedMbps(SPEED_THROTTLED);
            log.info("⚠️ 用户[{}]积分濒危，立即降速至{}Mbps", user.getUsername(), SPEED_THROTTLED);
        }
        userMapper.updateById(user);

        StringBuilder msg = new StringBuilder();
        msg.append(String.format("消耗1GB流量，积分 -%.2f。<br>", COST_PER_GB / 100.0));

        // 2. 核心演示逻辑：寻找一个正在分享的奉献者（例如A）给予奖励，模拟 P2P 流量是由 A 贡献的
        // 2. 核心演示逻辑：均摊共享 + 信誉折扣惩戒模型 (Reputation-based Discount Allocation)
        LambdaQueryWrapper<UserBandwidth> sharerQuery = new LambdaQueryWrapper<>();
        sharerQuery.eq(UserBandwidth::getIsSharing, true)
                .ne(UserBandwidth::getId, userId); // 不能是自己给自己供血

        List<UserBandwidth> activeSharers = userMapper.selectList(sharerQuery);
        
        if (!activeSharers.isEmpty()) {
            // 第一步：计算全网均摊的基础底薪
            int baseReward = REWARD_POOL_PER_GB / activeSharers.size();
            
            msg.append("  (底层 P2P 路由已建立：该笔流量由全网 " + activeSharers.size() + " 名分享者共同承载)<br>");

            // 第二步：遍历所有分享者，打折发放工资
            for (UserBandwidth sharer : activeSharers) {
                // 信誉折扣公式：实际奖金 = 底薪 * (个人信誉分 / 100.0)
                int actualReward = (int) (baseReward * (sharer.getReputation() / 100.0));
                int forfeitTax = baseReward - actualReward; // 因为信誉不足被系统没收的损耗税

                sharer.setCredits(sharer.getCredits() + actualReward);
                // 每次成功提供带宽，声誉分缓慢上涨
                sharer.setReputation(Math.min(100, sharer.getReputation() + 1));
                userMapper.updateById(sharer);
                
                if (forfeitTax > 0) {
                    msg.append(String.format("  &nbsp;&nbsp;↳ 节点 [%s] 承载并获得 +%.2f 积分 (因信誉仅 %d 分，被惩戒没收 %.2f 分销毁)<br>",
                            sharer.getUsername(), actualReward / 100.0, sharer.getReputation(), forfeitTax / 100.0));
                } else {
                    msg.append(String.format("  &nbsp;&nbsp;↳ 节点 [%s] 承载并获得满额 +%.2f 积分 (满信誉无损耗)<br>",
                            sharer.getUsername(), actualReward / 100.0));
                }
            }
        } else {
            msg.append("  <span class='text-danger'>(系统告警：当前全网竟然没有任何一个分享者在线！该笔收益池已全部回滚销毁)</span><br>");
        }

        return result(true, msg.toString());
    }

    /**
     * 每日结算逻辑（博弈论核心机制）
     * 规则：
     * - 分享者（isSharing=true）：按消耗量发放积分奖励，声誉提升，解除限速
     * - 搭便车者（isSharing=false 且 consumedGb>0）：赤字惩罚，声誉下降，强制限速
     * 结算后重置当日消耗量
     */
    @Transactional
    public Map<String, Object> settleDaily() {
        List<UserBandwidth> users = userMapper.selectList(null);
        StringBuilder log_msg = new StringBuilder("📊 今日结算结果：<br>");

        for (UserBandwidth user : users) {
            int consumed = user.getConsumedGb();
            int freeride = user.getFreerideGb();

            // === 只要有搭便车案底，日切前伪造分享状态全部无效，必须受到铁拳惩罚 ===
            if (freeride > 0) {
                // === 搭便车者：惩罚机制（博弈论：背叛受惩罚）===
                int penalty = freeride * PENALTY_PER_GB;
                user.setCredits(Math.max(0, user.getCredits() - penalty));
                user.setReputation(Math.max(0, user.getReputation() - REPUTATION_PENALTY));
                user.setIsThrottled(true);
                user.setSpeedMbps(SPEED_THROTTLED);
                log.info("❌ 用户[{}]今日存在{}GB搭便车记录，结算严惩积分-{}", user.getUsername(), freeride, penalty);
                log_msg.append(String.format("❌ %s：核实含有 %dGB 未共享下的非法流量消耗，无视突击切开关！立刻剥夺今天分红资格并重罚！积分-%.2f，声誉跌至%d，无期降速 %dMbps！<br>",
                        user.getUsername(), freeride, penalty / 100.0, user.getReputation(), SPEED_THROTTLED));

            } else if (Boolean.TRUE.equals(user.getIsSharing())) {
                // === 合作分享者：奖励机制，只要开启分享（挂机），拿到固定基建维护工资 ===
                int reward = 1000; // 固定红利 10.00 分
                user.setCredits(user.getCredits() + reward);
                user.setReputation(Math.min(100, user.getReputation() + REPUTATION_REWARD));
                user.setIsThrottled(false);
                user.setSpeedMbps(SPEED_NORMAL);
                log.info("✅ 用户[{}]分享者，获得全天挂机基建奖励+{}", user.getUsername(), reward);
                log_msg.append(String.format("✅ %s：保持分享状态，系统发放连通性红利+%.2f，声誉+%d，维持高速<br>",
                        user.getUsername(), reward / 100.0, REPUTATION_REWARD));

            } else {
                // === 摇摆潜水者：不出力，每天收基建闲置税 (防止死号沉淀资源) ===
                int tax = Math.max(100, (int)(user.getCredits() * 0.01)); // 扣 1% 闲置税，至少扣 1.00 分
                user.setCredits(Math.max(0, user.getCredits() - tax)); // 跌破 0 截至
                log_msg.append(String.format("📉 %s：闲置不作为，扣除基建维护税 %.2f 积分<br>", user.getUsername(), tax / 100.0));
            }

            // 重置当日消耗计数和违法案底
            user.setConsumedGb(0);
            user.setFreerideGb(0);
            userMapper.updateById(user);
        }

        return result(true, log_msg.toString());
    }

    /**
     * 切换用户分享状态（用于演示者控制用户C的摇摆行为）
     *
     * @param userId 用户ID
     * @return 操作结果
     */
    @Transactional
    public Map<String, Object> toggleShare(Long userId) {
        UserBandwidth user = userMapper.selectById(userId);
        if (user == null) {
            return result(false, "用户不存在");
        }

        boolean newState = !Boolean.TRUE.equals(user.getIsSharing());
        user.setIsSharing(newState);

        // 切换分享时，若开启分享则恢复网速
        if (newState) {
            user.setIsThrottled(false);
            user.setSpeedMbps(SPEED_NORMAL);
        }

        userMapper.updateById(user);
        log.info("🔄 用户[{}]分享状态切换为:{}", user.getUsername(), newState ? "开启" : "关闭");
        return result(true, String.format("分享已%s", newState ? "开启，恢复积极合作者身份及极速网络。" : "关闭，不再为社区贡献带宽。"));
    }

    /**
     * 重置所有用户到初始状态（根据演示剧本：初始全是 100 积分，100Mbps 绿网速）
     */
    @Transactional
    public Map<String, Object> resetAll() {
        List<UserBandwidth> users = userMapper.selectList(null);
        for (UserBandwidth user : users) {
            switch (user.getUserType()) {
                case "SHARER" -> {
                    user.setCredits(12000);
                    user.setReputation(95);
                    user.setIsSharing(true);
                    user.setConsumedGb(0);
                    user.setFreerideGb(0);
                    user.setIsThrottled(false);
                    user.setSpeedMbps(SPEED_NORMAL);
                }
                case "FREERIDER" -> {
                    user.setCredits(8000);
                    user.setReputation(70);
                    user.setIsSharing(false);
                    user.setConsumedGb(0);
                    user.setFreerideGb(0);
                    user.setIsThrottled(false);
                    user.setSpeedMbps(SPEED_NORMAL);
                }
                case "SWING" -> {
                    user.setCredits(10000);
                    user.setReputation(85);
                    user.setIsSharing(false);
                    user.setConsumedGb(0);
                    user.setFreerideGb(0);
                    user.setIsThrottled(false);
                    user.setSpeedMbps(SPEED_NORMAL);
                }
            }
            userMapper.updateById(user);
        }
        return result(true, "【重置成功】演示环境已就绪：精度已升维 100 倍！");
    }

    /** 统一返回结果封装 */
    private Map<String, Object> result(boolean success, String message) {
        Map<String, Object> map = new HashMap<>();
        map.put("success", success);
        map.put("message", message);
        return map;
    }
}
