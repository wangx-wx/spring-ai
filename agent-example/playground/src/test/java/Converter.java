import com.example.wx.tools.BaiduTranslateTools;
import org.springframework.ai.converter.BeanOutputConverter;
import org.springframework.core.ParameterizedTypeReference;

/**
 *@description 
 *@author wangx
 *@create 2025/8/24 18:07
 */
public class Converter {
    public static void main(String[] args) {
        BeanOutputConverter<BaiduTranslateTools.Request> converter = new BeanOutputConverter<>(
                new ParameterizedTypeReference<BaiduTranslateTools.Request>() {
                });
        System.out.println("converter.getFormat() = " + converter.getFormat());
        System.out.println("converter.getJsonSchema() = " + converter.getJsonSchema());
    }
}