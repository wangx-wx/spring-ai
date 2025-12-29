package com.example.wx.controller;

import com.example.wx.service.RagService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

/**
 * @author wangx
 * @description
 * @create 2025/7/13 14:25
 */
@RestController
@RequestMapping("/ai")
public class CloudRagController {

    private final RagService ragService;

    public CloudRagController(RagService ragService) {
        this.ragService = ragService;
    }

    @GetMapping("/knowledge/import")
    public void importDocument() {
        ragService.importDocuments();
    }

    @GetMapping("/knowledge/generate")
    public Flux<String> generate(@RequestParam(value = "message",
            defaultValue = "你好，请问你的知识库文档主要是关于什么内容的?") String message) {
        return ragService.retrieve(message).map(x -> x.getResult().getOutput().getText());
    }

    @GetMapping("/delete")
    public String delete(@RequestParam(value = "idList") String[] idList) {
        return ragService.delete(idList);
    }
}
