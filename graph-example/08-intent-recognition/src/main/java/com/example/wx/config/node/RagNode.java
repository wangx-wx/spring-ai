package com.example.wx.config.node;

import com.alibaba.cloud.ai.dashscope.rag.DashScopeDocumentRetriever;
import com.alibaba.cloud.ai.graph.OverAllState;
import com.alibaba.cloud.ai.graph.action.NodeAction;
import com.example.wx.domain.RagDoc;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.rag.Query;

/**
 * @author wangxiang
 * @description
 * @create 2025/12/10 10:19
 */
@AllArgsConstructor
public class RagNode implements NodeAction {

    private final String outputKey;
    private final String query;
    private final DashScopeDocumentRetriever dashScopeDocumentRetriever;

    @Override
    public Map<String, Object> apply(OverAllState state) throws Exception {
        String recall = state.value(query, "");
        List<Document> retrieve = dashScopeDocumentRetriever.retrieve(new Query(recall));
        ArrayList<RagDoc> docs = new ArrayList<>(retrieve.size());
        retrieve.stream()
                .filter(d -> {
                    Double score = (Double) d.getMetadata().get("_score");
                    return score != null && score > 0.5;
                })
                .map(d -> new RagDoc(
                        d.getText(),
                        (String) d.getMetadata().get("doc_name"),
                        formatScore((String)d.getMetadata().get("_score"))
                ))
                .forEach(docs::add);
        return Map.of(outputKey, docs);
    }

    public static String formatScore(String score) {
        if (score == null) {
            return null;
        }
        return new BigDecimal(score)
                .setScale(4, RoundingMode.HALF_UP)
                .toPlainString();
    }

}
