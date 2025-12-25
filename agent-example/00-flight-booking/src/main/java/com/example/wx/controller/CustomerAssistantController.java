package com.example.wx.controller;

import com.example.wx.service.CustomerAssistantService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RequestMapping("/api/assistant")
@RestController
public class CustomerAssistantController {

    private final CustomerAssistantService agent;

    public CustomerAssistantController(CustomerAssistantService agent) {
        this.agent = agent;
    }

    @RequestMapping(path = "/stream/chat", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<String> streamChat(@RequestParam(name = "chatId") String chatId,
                                   @RequestParam(name = "userMessage") String userMessage,
                                   HttpServletResponse response
    ) {
        response.setContentType("text/event-stream;charset=UTF-8");
        return agent.streamChat(chatId, userMessage);
    }

    @GetMapping(path = "/chat")
    @ResponseBody
    public String chat(@RequestParam(name = "chatId") String chatId,
                       @RequestParam(name = "userMessage") String userMessage) {
        return agent.chat(chatId, userMessage);
    }

}

