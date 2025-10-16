package cn.bugstack.knowledge.test;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * @Auther: yangjian
 * @Date: 2025-10-16 - 10 - 16 - 15:16
 * @Description: cn.bugstack.knowledge.test
 * @version: 1.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TextChunk {
    private String id;
    private String text;
    private String parentId;
    private Map<String, Object> metadata;

    public TextChunk(String text) {
        this.id = UUID.randomUUID().toString();
        this.text = text;
        this.metadata = new HashMap<>();
    }
}
