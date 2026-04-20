package org.example.cityplan.controller;

import org.example.cityplan.dto.ApiResponse;
import org.example.cityplan.entity.FormTemplate;
import org.example.cityplan.service.FormTemplateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/form-templates")
public class FormTemplateController {

    @Autowired
    private FormTemplateService service;

    @GetMapping
    public ResponseEntity<ApiResponse<List<FormTemplate>>> listAll() {
        return ResponseEntity.ok(ApiResponse.success(service.listAll()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<FormTemplate>> getById(@PathVariable Long id) {
        FormTemplate t = service.getById(id);
        if (t != null)
            return ResponseEntity.ok(ApiResponse.success(t));
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/match")
    public ResponseEntity<ApiResponse<FormTemplate>> getByMatch(
            @RequestParam String category, @RequestParam String subType) {
        FormTemplate t = service.getByCategoryAndSubType(category, subType);
        return ResponseEntity.ok(ApiResponse.success(t));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<FormTemplate>> create(@RequestBody FormTemplate template) {
        return ResponseEntity.ok(ApiResponse.success("创建成功", service.create(template)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<FormTemplate>> update(@PathVariable Long id, @RequestBody FormTemplate template) {
        return ResponseEntity.ok(ApiResponse.success("更新成功", service.update(id, template)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable Long id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功"));
    }
}
