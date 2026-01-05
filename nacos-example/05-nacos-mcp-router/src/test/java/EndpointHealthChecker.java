import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * MCP 端点健康检查器接口
 */
public interface EndpointHealthChecker {
    boolean isReachable(String endpointUrl);
    String getType();
}

/**
 * Streamable HTTP 传输健康检查器（MCP 2025-03-26 新规范）
 * 单一端点同时支持 POST 和 GET
 */
class StreamableHttpChecker implements EndpointHealthChecker {
    private static final Logger logger = LoggerFactory.getLogger(StreamableHttpChecker.class);
    private static final String JSON_RPC_VERSION = "2.0";
    private static final String PROTOCOL_VERSION = "2025-03-26";
    
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Duration timeout;
    
    public StreamableHttpChecker(Duration timeout) {
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(timeout)
            .build();
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public boolean isReachable(String endpointUrl) {
        try {
            logger.info("Checking Streamable HTTP endpoint: {}", endpointUrl);
            
            // 构造 initialize 请求
            Map<String, Object> request = buildInitializeRequest();
            String requestBody = objectMapper.writeValueAsString(request);
            
            // 关键：必须同时接受 JSON 和 SSE 响应
            HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(endpointUrl))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json, text/event-stream")
                .timeout(timeout)
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();
            
            HttpResponse<String> response = httpClient.send(httpRequest,
                HttpResponse.BodyHandlers.ofString());
            
            int statusCode = response.statusCode();
            String contentType = response.headers()
                .firstValue("Content-Type")
                .orElse("");
            
            logger.info("Streamable HTTP response - Status: {}, Content-Type: {}", 
                statusCode, contentType);
            
            if (statusCode == 200) {
                // 服务器可以返回 JSON 或 SSE 流
                if (contentType.contains("application/json")) {
                    return validateJsonResponse(response.body(), endpointUrl);
                } else if (contentType.contains("text/event-stream")) {
                    logger.info("Server responded with SSE stream (async mode)");
                    return true; // SSE 流表示服务器正常
                }
            }
            
            logger.warn("Streamable HTTP check failed - Status: {}", statusCode);
            return false;
            
        } catch (Exception e) {
            logger.warn("Streamable HTTP check failed: {}", e.getMessage());
            return false;
        }
    }
    
    private Map<String, Object> buildInitializeRequest() {
        Map<String, Object> request = new HashMap<>();
        request.put("jsonrpc", JSON_RPC_VERSION);
        request.put("id", UUID.randomUUID().toString());
        request.put("method", "initialize");
        
        Map<String, Object> params = new HashMap<>();
        params.put("protocolVersion", PROTOCOL_VERSION);
        params.put("capabilities", new HashMap<>());
        params.put("clientInfo", Map.of(
            "name", "JavaHealthChecker",
            "version", "1.0.0"
        ));
        
        request.put("params", params);
        return request;
    }
    
    private boolean validateJsonResponse(String responseBody, String endpointUrl) {
        try {
            @SuppressWarnings("unchecked")
            Map<String, Object> response = objectMapper.readValue(responseBody, Map.class);
            
            if (response.containsKey("error")) {
                logger.warn("Server returned error: {}", response.get("error"));
                return false;
            }
            
            if (response.containsKey("result")) {
                logger.info("Streamable HTTP endpoint {} is healthy", endpointUrl);
                return true;
            }
            
            return false;
        } catch (Exception e) {
            logger.warn("Failed to parse response: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getType() {
        return "StreamableHTTP";
    }
}

/**
 * 旧版 SSE 传输健康检查器（已废弃，但仍需支持）
 * 两个端点：/sse (GET) 和 /messages (POST)
 */
class LegacySseChecker implements EndpointHealthChecker {
    private static final Logger logger = LoggerFactory.getLogger(LegacySseChecker.class);
    
    private final HttpClient httpClient;
    private final Duration timeout;
    
    public LegacySseChecker(Duration timeout) {
        this.timeout = timeout;
        this.httpClient = HttpClient.newBuilder()
            .connectTimeout(timeout)
            .build();
    }
    
    @Override
    public boolean isReachable(String endpointUrl) {
        try {
            logger.info("Checking legacy SSE endpoint: {}", endpointUrl);
            
            // 旧版 SSE：客户端发送 GET 请求打开 SSE 流
            HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpointUrl))
                .header("Accept", "text/event-stream")
                .header("Cache-Control", "no-cache")
                .timeout(timeout)
                .GET()
                .build();
            
            HttpResponse<String> response = httpClient.send(request,
                HttpResponse.BodyHandlers.ofString());
            
            int statusCode = response.statusCode();
            String contentType = response.headers()
                .firstValue("Content-Type")
                .orElse("");
            
            logger.info("Legacy SSE response - Status: {}, Content-Type: {}", 
                statusCode, contentType);
            
            // SSE 端点应该返回 200 且 Content-Type 为 text/event-stream
            boolean reachable = statusCode == 200 && 
                contentType.contains("text/event-stream");
            
            if (reachable) {
                logger.info("Legacy SSE endpoint {} is healthy", endpointUrl);
            } else {
                logger.warn("Legacy SSE check failed");
            }
            
            return reachable;
            
        } catch (Exception e) {
            logger.warn("Legacy SSE check failed: {}", e.getMessage());
            return false;
        }
    }
    
    @Override
    public String getType() {
        return "LegacySSE";
    }
}

/**
 * 自动检测传输类型的健康检查器
 * 按照官方规范的检测逻辑
 */
class AutoDetectingChecker implements EndpointHealthChecker {
    private static final Logger logger = LoggerFactory.getLogger(AutoDetectingChecker.class);
    
    private final StreamableHttpChecker streamableChecker;
    private final LegacySseChecker legacySseChecker;
    
    public AutoDetectingChecker(Duration timeout) {
        this.streamableChecker = new StreamableHttpChecker(timeout);
        this.legacySseChecker = new LegacySseChecker(timeout);
    }
    
    @Override
    public boolean isReachable(String endpointUrl) {
        logger.info("Auto-detecting transport type for: {}", endpointUrl);
        
        // 步骤1: 先尝试 Streamable HTTP (POST)
        logger.debug("Trying Streamable HTTP transport...");
        try {
            if (streamableChecker.isReachable(endpointUrl)) {
                logger.info("Detected Streamable HTTP transport");
                return true;
            }
        } catch (Exception e) {
            logger.debug("Streamable HTTP failed: {}", e.getMessage());
        }
        
        // 步骤2: 如果失败，尝试旧版 SSE (GET)
        logger.debug("Trying legacy SSE transport...");
        try {
            if (legacySseChecker.isReachable(endpointUrl)) {
                logger.info("Detected legacy SSE transport");
                return true;
            }
        } catch (Exception e) {
            logger.debug("Legacy SSE failed: {}", e.getMessage());
        }
        
        logger.warn("Both transport checks failed for: {}", endpointUrl);
        return false;
    }
    
    @Override
    public String getType() {
        return "AutoDetecting";
    }
}

/**
 * 智能端点健康检查器
 * 根据 URL 路径自动选择检查策略
 */
class SmartEndpointHealthChecker {
    private static final Logger logger = LoggerFactory.getLogger(SmartEndpointHealthChecker.class);
    
    private final StreamableHttpChecker streamableChecker;
    private final LegacySseChecker legacySseChecker;
    private final AutoDetectingChecker autoDetectingChecker;
    
    public SmartEndpointHealthChecker(Duration timeout) {
        this.streamableChecker = new StreamableHttpChecker(timeout);
        this.legacySseChecker = new LegacySseChecker(timeout);
        this.autoDetectingChecker = new AutoDetectingChecker(timeout);
    }
    
    /**
     * 智能检查端点可达性
     * 根据 URL 模式选择合适的检查器
     */
    public boolean isEndpointReachable(String endpointUrl) {
        EndpointHealthChecker checker = selectChecker(endpointUrl);
        logger.debug("Selected {} checker for: {}", checker.getType(), endpointUrl);
        return checker.isReachable(endpointUrl);
    }
    
    /**
     * 根据 URL 选择检查器
     */
    private EndpointHealthChecker selectChecker(String endpointUrl) {
        String url = endpointUrl.toLowerCase();
        
        // 明确的 /mcp 端点使用 Streamable HTTP
        if (url.contains("/mcp")) {
            logger.debug("URL contains /mcp, using Streamable HTTP checker");
            return streamableChecker;
        }
        
        // 明确的 /sse 端点使用旧版 SSE
        if (url.contains("/sse") || url.contains("/events")) {
            logger.debug("URL contains /sse or /events, using legacy SSE checker");
            return legacySseChecker;
        }
        
        // 不确定的情况使用自动检测
        logger.debug("URL pattern unclear, using auto-detecting checker");
        return autoDetectingChecker;
    }
}

/**
 * 使用示例
 */
class EndpointHealthCheckExample {
    public static void main(String[] args) {
        SmartEndpointHealthChecker checker = new SmartEndpointHealthChecker(
            Duration.ofSeconds(10)
        );
        
        System.out.println("=== 检查 MCP 端点 ===");
        String mcpEndpoint = "http://192.168.1.8:10032/mcp";
        boolean mcpReachable = checker.isEndpointReachable(mcpEndpoint);
        System.out.println("MCP endpoint (/mcp) reachable: " + mcpReachable);
        
        System.out.println("\n=== 检查 SSE 端点 ===");
        String sseEndpoint = "http://192.168.1.8:10018/sse";
        boolean sseReachable = checker.isEndpointReachable(sseEndpoint);
        System.out.println("SSE endpoint (/sse) reachable: " + sseReachable);
        
        System.out.println("\n=== 使用自动检测 ===");
        String unknownEndpoint = "http://192.168.1.8:8080/unknown";
        boolean unknownReachable = checker.isEndpointReachable(unknownEndpoint);
        System.out.println("Unknown endpoint reachable: " + unknownReachable);
    }
}