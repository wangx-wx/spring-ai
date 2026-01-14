package com.example.wx.controller;

import com.alibaba.cloud.ai.graph.agent.ReactAgent;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.example.wx.agent.SkillsAgent;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ChatController {

    private final ReactAgent agent;

    public ChatController(SkillsAgent skillsAgent, ChatModel chatModel) {
        this.agent = skillsAgent.buildAgent(chatModel);
    }

    @GetMapping("/chat")
    public String chat(@RequestParam("message") String message) throws GraphRunnerException {
        return String.valueOf(agent.call(message));
    }
}
