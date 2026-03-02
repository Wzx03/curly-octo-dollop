package com.wangzixian.usedcar.module.content.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangzixian.usedcar.module.content.entity.Article;
import com.wangzixian.usedcar.module.content.entity.Comment;
import com.wangzixian.usedcar.module.content.mapper.ArticleMapper;
import com.wangzixian.usedcar.module.content.mapper.CommentMapper;
import com.wangzixian.usedcar.module.content.service.ContentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ContentServiceImpl implements ContentService {

    @Autowired
    private ArticleMapper articleMapper;

    @Autowired
    private CommentMapper commentMapper;

    @Override
    public Page<Article> getArticleList(int page, int size) {
        Page<Article> pageParam = new Page<>(page, size);
        return articleMapper.selectPage(pageParam, new LambdaQueryWrapper<Article>().orderByDesc(Article::getCreateTime));
    }

    @Override
    public Article getArticleDetail(Long id) {
        Article article = articleMapper.selectById(id);
        if (article != null) {
            article.setViews(article.getViews() + 1);
            articleMapper.updateById(article);
        }
        return article;
    }

    @Override
    public void publishArticle(Article article) {
        article.setCreateTime(LocalDateTime.now());
        article.setViews(0);
        articleMapper.insert(article);
    }

    @Override
    public void updateArticle(Article article) {
        articleMapper.updateById(article);
    }

    @Override
    public void deleteArticle(Long id) {
        articleMapper.deleteById(id);
        // 级联删除评论
        commentMapper.delete(new LambdaQueryWrapper<Comment>().eq(Comment::getArticleId, id));
    }

    @Override
    public List<Comment> getComments(Long articleId) {
        return commentMapper.selectList(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getArticleId, articleId)
                .orderByDesc(Comment::getCreateTime));
    }

    @Override
    public void publishComment(Comment comment) {
        comment.setCreateTime(LocalDateTime.now());
        commentMapper.insert(comment);
    }

    @Override
    public void deleteComment(Long id) {
        commentMapper.deleteById(id);
    }
}