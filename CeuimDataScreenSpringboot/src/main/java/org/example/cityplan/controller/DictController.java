package org.example.cityplan.controller;

import org.example.cityplan.dto.ApiResponse;
import org.example.cityplan.entity.SysDict;
import org.example.cityplan.entity.SysDictItem;
import org.example.cityplan.service.DictService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dicts")
public class DictController {

    @Autowired
    private DictService dictService;

    // ========== 字典类型 ==========

    @GetMapping
    public ResponseEntity<ApiResponse<List<SysDict>>> listDicts() {
        return ResponseEntity.ok(ApiResponse.success(dictService.listDicts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<SysDict>> getDict(@PathVariable Long id) {
        SysDict dict = dictService.getDictById(id);
        if (dict != null) {
            return ResponseEntity.ok(ApiResponse.success(dict));
        }
        return ResponseEntity.notFound().build();
    }

    @GetMapping("/code/{code}")
    public ResponseEntity<ApiResponse<SysDict>> getDictByCode(@PathVariable String code) {
        SysDict dict = dictService.getDictByCode(code);
        if (dict != null) {
            return ResponseEntity.ok(ApiResponse.success(dict));
        }
        return ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<ApiResponse<SysDict>> createDict(@RequestBody SysDict dict) {
        return ResponseEntity.ok(ApiResponse.success("创建成功", dictService.createDict(dict)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<SysDict>> updateDict(@PathVariable Long id, @RequestBody SysDict dict) {
        return ResponseEntity.ok(ApiResponse.success("更新成功", dictService.updateDict(id, dict)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteDict(@PathVariable Long id) {
        dictService.deleteDict(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功"));
    }

    // ========== 字典项 ==========

    @GetMapping("/{dictId}/items")
    public ResponseEntity<ApiResponse<List<SysDictItem>>> listItems(@PathVariable Long dictId) {
        return ResponseEntity.ok(ApiResponse.success(dictService.listItemsByDictId(dictId)));
    }

    @GetMapping("/code/{dictCode}/items")
    public ResponseEntity<ApiResponse<List<SysDictItem>>> listItemsByCode(@PathVariable String dictCode) {
        return ResponseEntity.ok(ApiResponse.success(dictService.listItemsByDictCode(dictCode)));
    }

    @PostMapping("/{dictId}/items")
    public ResponseEntity<ApiResponse<SysDictItem>> createItem(@PathVariable Long dictId,
            @RequestBody SysDictItem item) {
        item.setDictId(dictId);
        return ResponseEntity.ok(ApiResponse.success("创建成功", dictService.createItem(item)));
    }

    @PutMapping("/items/{id}")
    public ResponseEntity<ApiResponse<SysDictItem>> updateItem(@PathVariable Long id, @RequestBody SysDictItem item) {
        return ResponseEntity.ok(ApiResponse.success("更新成功", dictService.updateItem(id, item)));
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        dictService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功"));
    }
}
