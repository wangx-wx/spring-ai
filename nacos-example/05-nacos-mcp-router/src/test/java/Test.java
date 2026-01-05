import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * @author wangx
 * @description
 * @create 2026/1/5 21:38
 */

public class Test {

    private static boolean isEndpointReachable(String endpointUrl) {
        try {
            // logger.info("Checking endpoint reachability: {}", endpointUrl);

            java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
            java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder()
                    .uri(java.net.URI.create(endpointUrl))
                    // .method("GET", java.net.http.HttpRequest.BodyPublishers.noBody())
                    .GET()
                    .timeout(java.time.Duration.ofSeconds(10))
                    .build();

            java.net.http.HttpResponse<Void> response = client.send(request,
                    java.net.http.HttpResponse.BodyHandlers.discarding());

            int statusCode = response.statusCode();
            // logger.info("Endpoint {} returned status code: {}", endpointUrl, statusCode);

            // 接受 2xx 和 3xx 状态码，拒绝 4xx 和 5xx
            boolean reachable = statusCode >= 200 && statusCode < 400;
            if (!reachable) {
                // logger.warn("Endpoint {} is not reachable, status code: {}", endpointUrl, statusCode);
            }

            return reachable;
        } catch (Exception e) {
            // logger.warn("Endpoint {} is not reachable: {}", endpointUrl, e.getMessage());
            return false;
        }
    }

    public static void main(String[] args) {
        System.out.println("isEndpointReachable(\"http://192.168.1.8:10032/mcp\") = " + isEndpointReachable("http://192.168.1.8:10032/mcp"));
        System.out.println("isEndpointReachable(\"http://192.168.1.8:10032/mcp\") = " + isEndpointReachable("http://192.168.1.8:10018/sse"));
    }


    private static boolean isMcpServerAlive(String endpointUrl) {
        try {
            // logger.info("Checking MCP server readiness: {}", endpointUrl);

            HttpClient client = HttpClient.newHttpClient();

            String body = """
                    {
                      "jsonrpc": "2.0",
                      "id": "health-check",
                      "method": "initialize",
                      "params": {
                        "protocolVersion": "2024-11-05",
                        "capabilities": {},
                        "clientInfo": {
                          "name": "health-check",
                          "version": "1.0"
                        }
                      }
                    }
                    """;

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpointUrl))
                    .timeout(Duration.ofSeconds(10))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(body))
                    .build();

            HttpResponse<String> response =
                    client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() != 200) {
                // logger.warn("MCP server returned non-200 status: {}", response.statusCode());
                return false;
            }

            // 最小校验：JSON-RPC response 必须有 result
            String responseBody = response.body();
            boolean ok = responseBody.contains("\"result\"");

            if (!ok) {
                // logger.warn("MCP initialize response invalid: {}", responseBody);
            }

            return ok;
        } catch (Exception e) {
            // logger.warn("MCP server not reachable: {}", e.getMessage(), e);
            return false;
        }
    }

}
