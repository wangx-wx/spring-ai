package com.example.wx.dotenv.parser;

import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;

/**
 * 解析 .env 文件的接口
 *
 * @author wangx
 */
public interface DotenvParser {

    /**
     * 解析 .env 文件，返回键值对映射
     *
     * @param path .env 文件路径
     * @return 解析后的键值对，key 为变量名，value 为原始值（未解析变量引用）
     * @throws IOException 读取文件失败时抛出
     */
    Map<String, String> parse(Path path) throws IOException;

}
