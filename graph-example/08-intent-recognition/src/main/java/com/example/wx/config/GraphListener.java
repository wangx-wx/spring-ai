package com.example.wx.config;

import cn.hutool.core.date.DateUtil;
import com.alibaba.cloud.ai.graph.GraphLifecycleListener;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import static com.example.wx.constants.IntentGraphParams.NOW_DATE;
import static com.example.wx.constants.IntentGraphParams.REPLY;
import static com.example.wx.constants.IntentGraphParams.REWRITE_QUERY;
import static com.example.wx.constants.IntentGraphParams.WEEK_DAY;
import static com.example.wx.constants.IntentGraphParams.WEEK_OF_YEAR;

/**
 * @author wangxiang
 * @description
 * @create 2025/12/23 22:08
 */
@Component
@Slf4j
public class GraphListener implements GraphLifecycleListener {
    @Override
    public void onStart(String nodeId, Map<String, Object> state, RunnableConfig config) {
        log.info("工作流开始，初始化参数...[{}]", state);
        GraphLifecycleListener.super.onStart(nodeId, state, config);
    }

    @Override
    public void onComplete(String nodeId, Map<String, Object> state, RunnableConfig config) {
        log.info("工作流完成，end end...[{}]", state);
        GraphLifecycleListener.super.onComplete(nodeId, state, config);
    }
}
