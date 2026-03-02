package com.wangzixian.usedcar.common.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.wangzixian.usedcar.common.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

@RestController
@RequestMapping("/api/common")
public class FileController {

    @Value("${file.upload-path}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.getSuffix(originalFilename);
        String newFileName = IdUtil.simpleUUID() + "." + suffix;

        if (!FileUtil.exist(uploadPath)) {
            FileUtil.mkdir(uploadPath);
        }

        file.transferTo(new File(uploadPath + newFileName));

        // 返回相对路径，前端拼接或直接使用
        // 注意：这里我们统一用 /files/ 前缀，需要在 WebConfig 里配置映射
        String url = "http://localhost:8080/files/" + newFileName;
        return Result.success(url);
    }
}