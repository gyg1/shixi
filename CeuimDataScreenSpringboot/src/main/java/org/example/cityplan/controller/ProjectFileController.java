package org.example.cityplan.controller;

import lombok.extern.slf4j.Slf4j;
import org.example.cityplan.dto.ApiResponse;
import org.example.cityplan.entity.ProjectFile;
import org.example.cityplan.security.CustomUserDetails;
import org.example.cityplan.service.ProjectFileService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/projects")
public class ProjectFileController {

    @Autowired
    private ProjectFileService projectFileService;

    /**
     * 上传文件 (管理员)
     */
    @PostMapping("/{projectId}/files")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<ProjectFile>> uploadFile(
            @PathVariable Long projectId,
            @RequestParam("file") MultipartFile file,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[文件控制器] 上传文件 - projectId: {}, fileName: {}", projectId, file.getOriginalFilename());
        try {
            ProjectFile pf = projectFileService.uploadFile(projectId, file, userDetails.getUserId());
            return ResponseEntity.ok(ApiResponse.success("上传成功", pf));
        } catch (Exception e) {
            log.error("[文件控制器] 上传失败", e);
            return ResponseEntity.badRequest().body(ApiResponse.badRequest("上传失败: " + e.getMessage()));
        }
    }

    /**
     * 获取项目文件列表 (所有认证用户)
     */
    @GetMapping("/{projectId}/files")
    public ResponseEntity<ApiResponse<List<ProjectFile>>> listFiles(@PathVariable Long projectId) {
        log.info("[文件控制器] 获取文件列表 - projectId: {}", projectId);
        List<ProjectFile> files = projectFileService.getFilesByProjectId(projectId);
        return ResponseEntity.ok(ApiResponse.success(files));
    }

    /**
     * 下载文件 (所有认证用户)
     */
    @GetMapping("/files/{fileId}/download")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        log.info("[文件控制器] 下载文件 - fileId: {}", fileId);
        ProjectFile pf = projectFileService.getById(fileId);
        if (pf == null) {
            return ResponseEntity.notFound().build();
        }

        File file = new File(pf.getFilePath());
        if (!file.exists()) {
            return ResponseEntity.notFound().build();
        }

        Resource resource = new FileSystemResource(file);
        String encodedName = URLEncoder.encode(pf.getFileName(), StandardCharsets.UTF_8).replace("+", "%20");

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + encodedName)
                .body(resource);
    }

    /**
     * 删除文件 (管理员)
     */
    @DeleteMapping("/files/{fileId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<Void>> deleteFile(
            @PathVariable Long fileId,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        log.info("[文件控制器] 删除文件 - fileId: {}, 操作人: {}", fileId, userDetails.getUsername());
        try {
            projectFileService.deleteFile(fileId);
            return ResponseEntity.ok(ApiResponse.success("删除成功"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.badRequest(e.getMessage()));
        }
    }
}
