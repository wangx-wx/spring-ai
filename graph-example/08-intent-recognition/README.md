难点：
1. 意图澄清怎么返回
2. 参数澄清呢

```mermaid
flowchart TD
    __START__((start))
    __END__((stop))
    _rewrite_node_("_rewrite_node_")
    _intent_rag_node_("_intent_rag_node_")
    _intent_node_("_intent_node_")
    _qa_rag_node_("_qa_rag_node_")
    _qa_node_("_qa_node_")
    _set_param_node_("_set_param_node_")
    _assess_intent_node_("_assess_intent_node_")
    _assess_wait_node_("_assess_wait_node_")
    _agent_tool_node_("_agent_tool_node_")
    _agent_tool_wait_node_("_agent_tool_wait_node_")
    condition1{"check state"}
    condition2{"check state"}
    condition3{"check state"}
    condition4{"check state"}
    __START__:::__START__ --> _rewrite_node_:::_rewrite_node_
    _rewrite_node_:::_rewrite_node_ --> _intent_rag_node_:::_intent_rag_node_
    _intent_rag_node_:::_intent_rag_node_ --> _intent_node_:::_intent_node_
    _intent_node_:::_intent_node_ -.-> condition1:::condition1
    condition1:::condition1 -.->|qa| _qa_rag_node_:::_qa_rag_node_
%%	_intent_node_:::_intent_node_ -.->|qa| _qa_rag_node_:::_qa_rag_node_
    condition1:::condition1 -.->|analysis| _set_param_node_:::_set_param_node_
%%	_intent_node_:::_intent_node_ -.->|analysis| _set_param_node_:::_set_param_node_
    _qa_rag_node_:::_qa_rag_node_ --> _qa_node_:::_qa_node_
    _qa_node_:::_qa_node_ --> __END__:::__END__
    _set_param_node_:::_set_param_node_ -.-> condition2:::condition2
    condition2:::condition2 -.->|assess| _assess_intent_node_:::_assess_intent_node_
%%	_set_param_node_:::_set_param_node_ -.->|assess| _assess_intent_node_:::_assess_intent_node_
    condition2:::condition2 -.->|skip_assess| _agent_tool_node_:::_agent_tool_node_
%%	_set_param_node_:::_set_param_node_ -.->|skip_assess| _agent_tool_node_:::_agent_tool_node_
    _assess_intent_node_:::_assess_intent_node_ --> _assess_wait_node_:::_assess_wait_node_
    _assess_wait_node_:::_assess_wait_node_ -.-> condition3:::condition3
    condition3:::condition3 -.->|start| _rewrite_node_:::_rewrite_node_
%%	_assess_wait_node_:::_assess_wait_node_ -.->|start| _rewrite_node_:::_rewrite_node_
    condition3:::condition3 -.->|tool| _agent_tool_node_:::_agent_tool_node_
%%	_assess_wait_node_:::_assess_wait_node_ -.->|tool| _agent_tool_node_:::_agent_tool_node_
    _agent_tool_node_:::_agent_tool_node_ --> _agent_tool_wait_node_:::_agent_tool_wait_node_
    _agent_tool_wait_node_:::_agent_tool_wait_node_ -.-> condition4:::condition4
    condition4:::condition4 -.->|next| __END__:::__END__
%%	_agent_tool_wait_node_:::_agent_tool_wait_node_ -.->|next| __END__:::__END__
    condition4:::condition4 -.->|back| _agent_tool_node_:::_agent_tool_node_
%%	_agent_tool_wait_node_:::_agent_tool_wait_node_ -.->|back| _agent_tool_node_:::_agent_tool_node_

    classDef __START__ fill:black,stroke-width:1px,font-size:xx-small;
    classDef __END__ fill:black,stroke-width:1px,font-size:xx-small;
```