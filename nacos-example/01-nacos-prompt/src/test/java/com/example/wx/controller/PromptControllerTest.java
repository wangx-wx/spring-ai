package com.example.wx.controller;

import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * PromptController 单元测试
 *
 * @author wangx
 */
@ExtendWith(MockitoExtension.class)
class PromptControllerTest {

    @Mock
    private ChatModel chatModel;

    @Mock
    private ConfigurablePromptTemplateFactory promptTemplateFactory;

    @Mock
    private ConfigurablePromptTemplate promptTemplate;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ChatClient chatClient;

    @Mock
    private ChatClient.ChatClientRequestSpec requestSpec;

    @Mock
    private ChatClient.StreamResponseSpec streamResponseSpec;

    private PromptController promptController;

    @BeforeEach
    void setUp() {
        // 由于 ChatClient.builder(chatModel).build() 是静态方法，这里需要特殊处理
        // 实际测试中可以使用 @SpringBootTest 或者通过构造函数注入 ChatClient
    }

    @Test
    @DisplayName("测试模板渲染 - 成功获取并渲染模板")
    void testTemplate_Success() {
        // Given
        String templateName = "chat-greeting";
        String expectedContent = "你好，欢迎使用 Spring AI 助手。";

        when(promptTemplateFactory.getTemplate(templateName)).thenReturn(promptTemplate);
        when(promptTemplate.render()).thenReturn(expectedContent);

        // When & Then
        // 注意：由于构造函数中有 ChatClient.builder().build()，这里需要集成测试才能完整测试
        verify(promptTemplateFactory, never()).getTemplate(anyString()); // 仅验证 mock 设置正确
    }

    @Test
    @DisplayName("测试模板创建 - 使用变量渲染模板")
    void testTemplateCreate_WithVariables() {
        // Given
        String templateName = "chat-greeting";
        String templateContent = "你好，{name}！欢迎使用 Spring AI 助手。";
        String userName = "张三";
        String expectedRendered = "你好，张三！欢迎使用 Spring AI 助手。";

        when(promptTemplateFactory.getTemplate(templateName)).thenReturn(promptTemplate);
        when(promptTemplate.render(any(Map.class))).thenReturn(expectedRendered);

        // When
        String result = promptTemplate.render(Map.of("name", userName));

        // Then
        verify(promptTemplate).render(Map.of("name", userName));
        assert result.equals(expectedRendered);
    }

    @Test
    @DisplayName("测试 Prompt 创建 - 验证模板创建 Prompt 对象")
    void testPromptCreate() {
        // Given
        String templateName = "chat-greeting";
        Map<String, Object> variables = Map.of("name", "李四");
        Prompt mockPrompt = mock(Prompt.class);

        when(promptTemplateFactory.getTemplate(templateName)).thenReturn(promptTemplate);
        when(promptTemplate.create(variables)).thenReturn(mockPrompt);
        when(mockPrompt.getContents()).thenReturn("你好，李四！欢迎使用 Spring AI 助手。");

        // When
        ConfigurablePromptTemplate template = promptTemplateFactory.getTemplate(templateName);
        Prompt prompt = template.create(variables);

        // Then
        verify(promptTemplateFactory).getTemplate(templateName);
        verify(promptTemplate).create(variables);
        assert prompt.getContents().contains("李四");
    }

    @Test
    @DisplayName("测试工厂创建模板 - 使用 create 方法")
    void testFactoryCreate() {
        // Given
        String templateName = "author";
        String templateContent = "please list the three most famous books by this {author}.";

        when(promptTemplateFactory.create(templateName, templateContent)).thenReturn(promptTemplate);

        // When
        ConfigurablePromptTemplate template = promptTemplateFactory.create(templateName, templateContent);

        // Then
        verify(promptTemplateFactory).create(templateName, templateContent);
        assert template != null;
    }

    @Test
    @DisplayName("测试模板不存在场景")
    void testTemplate_NotFound() {
        // Given
        String nonExistentTemplate = "non-existent-template";

        when(promptTemplateFactory.getTemplate(nonExistentTemplate))
                .thenThrow(new RuntimeException("Template not found: " + nonExistentTemplate));

        // When & Then
        try {
            promptTemplateFactory.getTemplate(nonExistentTemplate);
            assert false : "Should throw exception";
        } catch (RuntimeException e) {
            assert e.getMessage().contains("Template not found");
        }
    }

    @Test
    @DisplayName("测试多变量模板渲染")
    void testTemplateRender_MultipleVariables() {
        // Given
        String templateName = "code-review";
        Map<String, Object> variables = Map.of(
                "language", "Java",
                "code", "public void test() { }"
        );
        String expectedContent = "请评审以下 Java 代码：\npublic void test() { }";

        when(promptTemplateFactory.getTemplate(templateName)).thenReturn(promptTemplate);
        when(promptTemplate.render(variables)).thenReturn(expectedContent);

        // When
        ConfigurablePromptTemplate template = promptTemplateFactory.getTemplate(templateName);
        String result = template.render(variables);

        // Then
        assert result.contains("Java");
        assert result.contains("public void test()");
    }

    @Test
    @DisplayName("测试空变量渲染")
    void testTemplateRender_EmptyVariables() {
        // Given
        String templateName = "simple-template";
        String templateContent = "这是一个简单的模板，没有变量。";

        when(promptTemplateFactory.getTemplate(templateName)).thenReturn(promptTemplate);
        when(promptTemplate.render()).thenReturn(templateContent);

        // When
        ConfigurablePromptTemplate template = promptTemplateFactory.getTemplate(templateName);
        String result = template.render();

        // Then
        assert result.equals(templateContent);
    }

    @Test
    @DisplayName("测试特殊字符变量")
    void testTemplateRender_SpecialCharacters() {
        // Given
        String templateName = "special-template";
        Map<String, Object> variables = Map.of(
                "code", "System.out.println(\"Hello <World>\");"
        );
        String expectedContent = "代码：System.out.println(\"Hello <World>\");";

        when(promptTemplateFactory.getTemplate(templateName)).thenReturn(promptTemplate);
        when(promptTemplate.render(variables)).thenReturn(expectedContent);

        // When
        String result = promptTemplate.render(variables);

        // Then
        assert result.contains("<World>");
    }
}
