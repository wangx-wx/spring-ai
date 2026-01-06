import com.alibaba.cloud.ai.mcp.router.service.McpProxyService;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.McpSyncClient;
import io.modelcontextprotocol.client.transport.WebFluxSseClientTransport;
import org.springframework.web.reactive.function.client.WebClient;

public class McpSdkVerifier {

    public static void main(String[] args) {
        System.out.println("v = " + verifyWithSdk("http://localhost:10018"));
    }

    /**
     * 使用 SDK 进行深度校验 (Connect + Ping)
     */
    public static boolean verifyWithSdk(String endpointUrl) {
        // 1. 创建 Transport
        var transport = new WebFluxSseClientTransport(WebClient.builder().baseUrl(endpointUrl));
        // var transport = new HttpClientSseClientTransport(endpointUrl);

        // 2. 创建临时 Client (注意资源关闭)
        // 在实际应用中，通常应该复用 Client，而不是每次校验都创建
        try (McpSyncClient mcpClient = McpClient.sync(transport).build()) {

            // 3. 初始化连接 (发送 initialize 消息)
            mcpClient.initialize();

            // 4. 发送 Ping (这是最核心的存活检测)
            // 如果对方响应了 pong，说明业务逻辑完全正常
            mcpClient.ping();

            return true;
        } catch (Exception e) {
            // 捕获超时、协议错误、连接拒绝等
            System.err.println("MCP SDK Verify failed: " + e.getMessage());
            return false;
        }
    }
}