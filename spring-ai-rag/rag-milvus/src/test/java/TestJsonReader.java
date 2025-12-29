import org.springframework.ai.document.Document;
import org.springframework.ai.reader.JsonReader;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.util.List;

/**
 * @author wangx
 * @description
 * @create 2025/7/1 10:12
 */
public class TestJsonReader {

    public static final String json = """
            [
              {
                "id": 1,
                "brand": "Trek",
                "description": "A high-performance mountain bike for trail riding."
              },
              {
                "id": 2,
                "brand": "Cannondale",
                "description": "An aerodynamic road bike for racing enthusiasts."
              }
            ]
            """;

    public static void main(String[] args) throws IOException {
        Resource resource = new ClassPathResource("test.json");
        System.out.println(resource.contentLength());
        System.out.println(json.length());
        JsonReader jsonReader = new JsonReader(resource);
        System.out.println("documents = " + jsonReader.get());
    }

}
