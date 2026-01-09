package com.example.wx.config;

import com.alibaba.cloud.ai.graph.GraphLifecycleListener;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author wangx
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
