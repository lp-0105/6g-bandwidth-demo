package com.campus.bandwidth.controller;

import com.campus.bandwidth.service.UserBandwidthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * 带宽分享激励系统 REST API Controller
 * 所有接口供前端 JS 调用，驱动演示 Dashboard 的实时状态更新
 */
@Controller
@RequiredArgsConstructor
public class UserBandwidthController {

    private final UserBandwidthService userService;

    /**
     * 渲染主页（Thymeleaf 模板 index.html）
     */
    @GetMapping("/")
    public String index() {
        return "index";
    }

    /**
     * 获取所有用户当前状态
     * GET /api/users
     */
    @GetMapping("/api/users")
    @ResponseBody
    public ResponseEntity<?> getAllUsers() {
        return ResponseEntity.ok(userService.getAllUsers());
    }

    /**
     * 模拟用户消耗 1GB 流量
     * POST /api/action/consume?userId=1
     */
    @PostMapping("/api/action/consume")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> consume(@RequestParam Long userId) {
        return ResponseEntity.ok(userService.consumeTraffic(userId));
    }

    /**
     * 每日结算：检查分享配额，未达标者降速
     * POST /api/action/settle
     */
    @PostMapping("/api/action/settle")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> settle() {
        return ResponseEntity.ok(userService.settleDaily());
    }

    /**
     * 切换用户分享开关（主要用于用户C的摇摆演示）
     * POST /api/action/toggle-share?userId=3
     */
    @PostMapping("/api/action/toggle-share")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleShare(@RequestParam Long userId) {
        return ResponseEntity.ok(userService.toggleShare(userId));
    }

    /**
     * 重置所有用户到初始状态（演示用）
     * POST /api/action/reset
     */
    @PostMapping("/api/action/reset")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> reset() {
        return ResponseEntity.ok(userService.resetAll());
    }
}
