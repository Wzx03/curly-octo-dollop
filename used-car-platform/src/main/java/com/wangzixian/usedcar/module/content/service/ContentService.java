package com.wangzixian.usedcar.module.content.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangzixian.usedcar.module.content.entity.Article;
import com.wangzixian.usedcar.module.content.entity.Comment;

import java.util.List;

public interface ContentService {
    Page<Article> getArticleList(int page, int size);
    Article getArticleDetail(Long id);
    void publishArticle(Article article);
    void updateArticle(Article article);
    void deleteArticle(Long id);
    
    List<Comment> getComments(Long articleId);
    void publishComment(Comment comment);
    void deleteComment(Long id);
}