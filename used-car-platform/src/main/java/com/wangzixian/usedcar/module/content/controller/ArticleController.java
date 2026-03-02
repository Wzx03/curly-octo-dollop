package com.wangzixian.usedcar.module.content.controller;

import cn.hutool.jwt.JWTUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.wangzixian.usedcar.common.Result;
import com.wangzixian.usedcar.module.content.entity.Article;
import com.wangzixian.usedcar.module.content.entity.Comment;
import com.wangzixian.usedcar.module.content.service.ContentService;
import com.wangzixian.usedcar.module.user.entity.User;
import com.wangzixian.usedcar.module.user.mapper.UserMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/content")
public class ArticleController {

    @Autowired
    private ContentService contentService;

    @Autowired
    private UserMapper userMapper;

    private Long getUserIdFromToken(String token) {
        try {
            Object idObj = JWTUtil.parseToken(token).getPayload("id");
            return Long.valueOf(idObj.toString());
        } catch (Exception e) {
            return null;
        }
    }

    // --- 文章相关 ---

    @GetMapping("/article/list")
    public Result<Page<Article>> list(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size
    ) {
        return Result.success(contentService.getArticleList(page, size));
    }

    @GetMapping("/article/{id}")
    public Result<Article> detail(@PathVariable Long id) {
        return Result.success(contentService.getArticleDetail(id));
    }

    @PostMapping("/article/publish")
    public Result<String> publish(@RequestBody Article article, @RequestHeader("token") String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error("请先登录");
        
        User user = userMapper.selectById(userId);
        article.setAuthor(user.getNickname()); // 简单处理，直接存昵称
        
        contentService.publishArticle(article);
        return Result.success("发布成功");
    }

    @PostMapping("/article/update")
    public Result<String> update(@RequestBody Article article, @RequestHeader("token") String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error("请先登录");
        
        // 实际应校验作者是否为当前用户，这里简化
        contentService.updateArticle(article);
        return Result.success("修改成功");
    }

    @PostMapping("/article/delete/{id}")
    public Result<String> delete(@PathVariable Long id, @RequestHeader("token") String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error("请先登录");
        
        // 实际应校验作者
        contentService.deleteArticle(id);
        return Result.success("删除成功");
    }

    // --- 评论相关 ---

    @GetMapping("/comment/list")
    public Result<List<Comment>> getComments(@RequestParam Long articleId) {
        return Result.success(contentService.getComments(articleId));
    }

    @PostMapping("/comment/publish")
    public Result<String> publishComment(@RequestBody Comment comment, @RequestHeader("token") String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error("请先登录");

        User user = userMapper.selectById(userId);
        comment.setUserId(userId);
        comment.setNickname(user.getNickname());
        comment.setAvatar(user.getAvatar());

        contentService.publishComment(comment);
        return Result.success("评论成功");
    }

    @PostMapping("/comment/delete/{id}")
    public Result<String> deleteComment(@PathVariable Long id, @RequestHeader("token") String token) {
        Long userId = getUserIdFromToken(token);
        if (userId == null) return Result.error("请先登录");
        
        contentService.deleteComment(id);
        return Result.success("删除成功");
    }
}