package com.example.wx.domain;

import com.fasterxml.jackson.annotation.JsonPropertyOrder;
import org.springframework.ai.tool.annotation.ToolParam;

/**
 * @author wangx
 * @description
 * @create 2025/5/25 17:26
 */
@JsonPropertyOrder({"title", "date", "author", "content"})
public class ObjectEntity {
    /**
     * 标题
     */
    @ToolParam(description = "标题")
    private String title;
    private String author;
    private String date;
    private String content;


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Override
    public String toString() {
        return "StreamToBeanEntity{" +
                "title='" + title + '\'' +
                ", author='" + author + '\'' +
                ", date='" + date + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}
