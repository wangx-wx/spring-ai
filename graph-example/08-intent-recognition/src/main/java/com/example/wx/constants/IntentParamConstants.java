package com.example.wx.constants;

import java.util.Map;

/**
 * @author wangxiang
 * @description
 * @create 2025/12/21 23:12
 */
public interface IntentParamConstants {
    Map<String, String> INTENT_MAP = Map.of("商家维度经营分析", "_slot_node_", "下载场景", "_slot_node_", "其他场景", "_qa_rag_node_");
    Map<String, String> INTENT_DESC_MAP = Map.of(
            "商家维度经营分析", "涉及整体收益、订单、收入、GMV、营业额、营收、交易额、流水、成交量、经营建议、同行比较等商家全局经营分析。关键词: 总收益、营业额、整体订单、总交易额、总收入、报表、年报",
            "下载场景", "商户下载或导出经营数据，数据会发送到商家邮箱。关键词: 下载、导出、发送邮箱、数据导出"
    );
}
