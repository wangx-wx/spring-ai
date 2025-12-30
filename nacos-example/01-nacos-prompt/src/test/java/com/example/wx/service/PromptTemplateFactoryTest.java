package com.example.wx.service;

import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplate;
import com.alibaba.cloud.ai.prompt.ConfigurablePromptTemplateFactory;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * ConfigurablePromptTemplateFactory 功能测试
 * 测试模板工厂的各种使用场景
 *
 * @author wangx
 */
@ExtendWith(MockitoExtension.class)
class PromptTemplateFactoryTest {

    @Mock
    private ConfigurablePromptTemplateFactory promptTemplateFactory;

    @Mock
    private ConfigurablePromptTemplate promptTemplate;

    @Test
    @DisplayName("测试从 Nacos 获取模板 - getTemplate 方法")
    void testGetTemplate() {
        // Given
        String templateName = "chat-greeting";
        when(promptTemplateFactory.getTemplate(templateName)).thenReturn(promptTemplate);

        // When
        ConfigurablePromptTemplate result = promptTemplateFactory.getTemplate(templateName);

        // Then
        assertNotNull(result);
        verify(promptTemplateFactory).getTemplate(templateName);
    }

    @Test
    @DisplayName("测试代码创建模板 - create 方法")
    void testCreateTemplate() {
        // Given
        String templateName = "custom-template";
        String templateContent = "Hello, {name}!";
        when(promptTemplateFactory.create(templateName, templateContent)).thenReturn(promptTemplate);

        // When
        ConfigurablePromptTemplate result = promptTemplateFactory.create(templateName, templateContent);

        // Then
        assertNotNull(result);
        verify(promptTemplateFactory).create(templateName, templateContent);
    }

    @Test
    @DisplayName("测试模板渲染 - 单变量")
    void testTemplateRender_SingleVariable() {
        // Given
        String expected = "Hello, World!";
        when(promptTemplate.render(any(Map.class))).thenReturn(expected);

        // When
        String result = promptTemplate.render(Map.of("name", "World"));

        // Then
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("测试模板渲染 - 多变量")
    void testTemplateRender_MultipleVariables() {
        // Given
        String expected = "User John from Beijing logged in.";
        Map<String, Object> variables = Map.of(
                "user", "John",
                "city", "Beijing",
                "action", "logged in"
        );
        when(promptTemplate.render(variables)).thenReturn(expected);

        // When
        String result = promptTemplate.render(variables);

        // Then
        assertEquals(expected, result);
        verify(promptTemplate).render(variables);
    }

    @Test
    @DisplayName("测试创建 Prompt 对象")
    void testCreatePrompt() {
        // Given
        Map<String, Object> variables = Map.of("name", "Test");
        Prompt mockPrompt = mock(Prompt.class);
        when(mockPrompt.getContents()).thenReturn("Hello, Test!");
        when(promptTemplate.create(variables)).thenReturn(mockPrompt);

        // When
        Prompt result = promptTemplate.create(variables);

        // Then
        assertNotNull(result);
        assertEquals("Hello, Test!", result.getContents());
    }

    @Test
    @DisplayName("测试模板缓存 - 多次获取同一模板")
    void testTemplateCaching() {
        // Given
        String templateName = "cached-template";
        when(promptTemplateFactory.getTemplate(templateName)).thenReturn(promptTemplate);

        // When - 多次获取同一模板
        ConfigurablePromptTemplate result1 = promptTemplateFactory.getTemplate(templateName);
        ConfigurablePromptTemplate result2 = promptTemplateFactory.getTemplate(templateName);
        ConfigurablePromptTemplate result3 = promptTemplateFactory.getTemplate(templateName);

        // Then - 验证调用次数
        verify(promptTemplateFactory, times(3)).getTemplate(templateName);
        assertSame(result1, result2);
        assertSame(result2, result3);
    }

    @Test
    @DisplayName("测试中文模板渲染")
    void testChineseTemplateRender() {
        // Given
        String expected = "你好，张三！欢迎来到 Spring AI 系统。";
        Map<String, Object> variables = Map.of(
                "name", "张三",
                "system", "Spring AI 系统"
        );
        when(promptTemplate.render(variables)).thenReturn(expected);

        // When
        String result = promptTemplate.render(variables);

        // Then
        assertEquals(expected, result);
        assertTrue(result.contains("张三"));
        assertTrue(result.contains("Spring AI 系统"));
    }

    @Test
    @DisplayName("测试代码评审模板场景")
    void testCodeReviewTemplate() {
        // Given
        String templateName = "code-review";
        String codeSnippet = """
                public class HelloWorld {
                    public static void main(String[] args) {
                        System.out.println("Hello, World!");
                    }
                }
                """;

        Map<String, Object> variables = Map.of(
                "language", "Java",
                "code", codeSnippet
        );

        String expectedPrompt = "请评审以下 Java 代码：\n" + codeSnippet;
        when(promptTemplateFactory.getTemplate(templateName)).thenReturn(promptTemplate);
        when(promptTemplate.render(variables)).thenReturn(expectedPrompt);

        // When
        ConfigurablePromptTemplate template = promptTemplateFactory.getTemplate(templateName);
        String result = template.render(variables);

        // Then
        assertTrue(result.contains("Java"));
        assertTrue(result.contains("HelloWorld"));
        assertTrue(result.contains("System.out.println"));
    }

    @Test
    @DisplayName("测试问候模板场景")
    void testGreetingTemplate() {
        // Given
        String templateName = "chat-greeting";
        Map<String, Object> variables = Map.of("name", "wangx");

        String expectedPrompt = "你是一个友好的AI助手。用户 wangx 刚刚登录系统，请用热情、专业的方式向他/她打招呼。";

        when(promptTemplateFactory.getTemplate(templateName)).thenReturn(promptTemplate);
        when(promptTemplate.render(variables)).thenReturn(expectedPrompt);

        // When
        ConfigurablePromptTemplate template = promptTemplateFactory.getTemplate(templateName);
        String result = template.render(variables);

        // Then
        assertTrue(result.contains("wangx"));
        assertTrue(result.contains("AI助手"));
    }

    @Test
    @DisplayName("测试模板不存在异常处理")
    void testTemplateNotFound() {
        // Given
        String nonExistentTemplate = "non-existent";
        when(promptTemplateFactory.getTemplate(nonExistentTemplate))
                .thenThrow(new IllegalArgumentException("Template not found: " + nonExistentTemplate));

        // When & Then
        Exception exception = assertThrows(IllegalArgumentException.class, () -> {
            promptTemplateFactory.getTemplate(nonExistentTemplate);
        });

        assertTrue(exception.getMessage().contains("Template not found"));
    }

    @Test
    @DisplayName("测试空变量 Map 渲染")
    void testRenderWithEmptyMap() {
        // Given
        String expected = "这是一个没有变量的模板。";
        when(promptTemplate.render(Map.of())).thenReturn(expected);

        // When
        String result = promptTemplate.render(Map.of());

        // Then
        assertEquals(expected, result);
    }

    @Test
    @DisplayName("测试无参数渲染")
    void testRenderWithoutParameters() {
        // Given
        String expected = "静态模板内容";
        when(promptTemplate.render()).thenReturn(expected);

        // When
        String result = promptTemplate.render();

        // Then
        assertEquals(expected, result);
    }
}
