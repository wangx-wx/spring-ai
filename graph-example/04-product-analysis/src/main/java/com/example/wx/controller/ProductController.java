package com.example.wx.controller;

import com.alibaba.cloud.ai.graph.CompileConfig;
import com.alibaba.cloud.ai.graph.CompiledGraph;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.RunnableConfig;
import com.alibaba.cloud.ai.graph.StateGraph;
import com.alibaba.cloud.ai.graph.checkpoint.config.SaverConfig;
import com.alibaba.cloud.ai.graph.checkpoint.constant.SaverEnum;
import com.alibaba.cloud.ai.graph.checkpoint.savers.MemorySaver;
import com.alibaba.cloud.ai.graph.exception.GraphRunnerException;
import com.alibaba.cloud.ai.graph.exception.GraphStateException;
import com.example.wx.model.Product;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;
import java.util.Optional;

/**
 * @author wangxiang
 * @description
 * @create 2025/10/8 18:18
 */
@RestController
public class ProductController {

    private final CompiledGraph compiledGraph;

    public ProductController(@Qualifier("productAnalysisGraph") StateGraph productAnalysisGraph) throws GraphStateException {
        SaverConfig saverConfig = SaverConfig.builder().register(SaverEnum.MEMORY.getValue(), new MemorySaver()).build();
        this.compiledGraph = productAnalysisGraph.compile(CompileConfig.builder().saverConfig(saverConfig).build());
    }

    @GetMapping("/product/enrich")
    public Object enrichProduct() throws GraphRunnerException {
        String productDesc = """
                Introducing our new **EcoBreeze Smart Jacket** — a perfect blend of technology and comfort.
                Crafted with lightweight, water-resistant fabric, this jacket features built-in temperature control and adjustable ventilation zones.
                Designed for both men and women, it’s available in navy blue, charcoal gray, and forest green.
                Ideal for spring and autumn adventures, commuting, or casual outings.
                """;
        Map<String, Object> initialState = Map.of("productDesc", productDesc);
        RunnableConfig runnableConfig = RunnableConfig.builder().build();
        Optional<OverAllState> invoke = compiledGraph.call(initialState, runnableConfig);
        Product finalProduct = (Product) invoke.get().value("finalProduct").orElseThrow();
        System.out.println("finalProduct = " + finalProduct);
        return finalProduct;
    }
}
