package org.example.cityplan.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.example.cityplan.entity.ProjectFile;
import org.example.cityplan.mapper.ProjectFileMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class ProjectFileService extends ServiceImpl<ProjectFileMapper, ProjectFile> {

    @Value("${file.upload-dir:uploads/projects}")
    private String uploadDir;

    /**
     * 上传文件
     */
    public ProjectFile uploadFile(Long projectId, MultipartFile file, Long userId) throws IOException {
        log.info("[文件服务] 上传文件 - projectId: {}, fileName: {}, size: {}", projectId, file.getOriginalFilename(),
                file.getSize());

        // 创建上传目录
        Path dirPath = Paths.get(uploadDir, String.valueOf(projectId));
        Files.createDirectories(dirPath);

        // 生成唯一文件名
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String storedName = UUID.randomUUID().toString().replace("-", "") + ext;
        Path filePath = dirPath.resolve(storedName);

        // 获取绝对路径保存文件
        File dest = filePath.toAbsolutePath().toFile();
        // 如果使用了绝对路径，也要确保上级目录存在
        if (!dest.getParentFile().exists()) {
            dest.getParentFile().mkdirs();
        }
        file.transferTo(dest);

        // 保存记录
        ProjectFile pf = new ProjectFile();
        pf.setProjectId(projectId);
        pf.setFileName(originalName);
        pf.setFilePath(filePath.toString());
        pf.setFileSize(file.getSize());
        pf.setFileType(file.getContentType());
        pf.setUploadedBy(userId);
        this.save(pf);

        log.info("[文件服务] 文件上传成功 - id: {}, path: {}", pf.getId(), filePath);
        return pf;
    }

    /**
     * 获取项目的文件列表
     */
    public List<ProjectFile> getFilesByProjectId(Long projectId) {
        return baseMapper.selectByProjectId(projectId);
    }

    /**
     * 删除文件
     */
    public void deleteFile(Long fileId) {
        ProjectFile pf = this.getById(fileId);
        if (pf == null) {
            throw new RuntimeException("文件不存在");
        }

        // 删除物理文件
        try {
            Path path = Paths.get(pf.getFilePath());
            Files.deleteIfExists(path);
        } catch (IOException e) {
            log.warn("[文件服务] 物理文件删除失败: {}", pf.getFilePath(), e);
        }

        this.removeById(fileId);
        log.info("[文件服务] 文件删除成功 - id: {}", fileId);
    }
}
