package com.example.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.lang.Dict;
import cn.hutool.core.util.StrUtil;
import com.example.common.Result;
import com.example.service.MinioFileService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/files")
public class FileController {

    private static final Logger log = LoggerFactory.getLogger(FileController.class);

    private static final String filePath = System.getProperty("user.dir") + "/files/";

    @Resource
    private MinioFileService minioFileService;

    /**
     * 文件上传
     */
    @PostMapping("/upload")
    public Result upload(MultipartFile file) {
        return uploadToMinio(file, "common");
    }

    @PostMapping("/upload/advertise")
    public Result uploadAdvertise(MultipartFile file) {
        return uploadToMinio(file, "advertise");
    }

    private Result uploadToMinio(MultipartFile file, String directory) {
        try {
            String url = minioFileService.upload(file, directory);
            return Result.success(url);
        } catch (Exception e) {
            String fileName = file == null ? "unknown" : file.getOriginalFilename();
            log.error(fileName + "--文件上传失败", e);
            return Result.error("文件上传失败");
        }
    }

    /**
     * 获取文件
     */
    @GetMapping("/download/{fileName}")
    public void download(@PathVariable String fileName, HttpServletResponse response) {
        OutputStream os;
        try {
            if (StrUtil.isNotEmpty(fileName)) {
                response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileName, StandardCharsets.UTF_8));
                response.setContentType("application/octet-stream");
                byte[] bytes = FileUtil.readBytes(filePath + fileName);
                os = response.getOutputStream();
                os.write(bytes);
                os.flush();
                os.close();
            }
        } catch (Exception e) {
            log.warn("文件下载失败：" + fileName);
        }
    }

    /**
     * wang-editor编辑器文件上传接口
     */
    @PostMapping("/wang/upload")
    public Map<String, Object> wangEditorUpload(MultipartFile file) {
        String url = "";
        try {
            url = minioFileService.upload(file, "editor");
        } catch (Exception e) {
            String fileName = file == null ? "unknown" : file.getOriginalFilename();
            log.error(fileName + "--文件上传失败", e);
        }
        Map<String, Object> resMap = new HashMap<>();
        resMap.put("errno", 0);
        resMap.put("data", CollUtil.newArrayList(Dict.create().set("url", url)));
        return resMap;
    }
}
