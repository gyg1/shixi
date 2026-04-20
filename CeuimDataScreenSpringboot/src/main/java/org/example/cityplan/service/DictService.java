package org.example.cityplan.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.example.cityplan.entity.SysDict;
import org.example.cityplan.entity.SysDictItem;
import org.example.cityplan.mapper.SysDictMapper;
import org.example.cityplan.mapper.SysDictItemMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class DictService {

    @Autowired
    private SysDictMapper dictMapper;

    @Autowired
    private SysDictItemMapper dictItemMapper;

    // ========== 字典类型 ==========

    public List<SysDict> listDicts() {
        return dictMapper.selectList(
                new LambdaQueryWrapper<SysDict>().orderByAsc(SysDict::getId));
    }

    public SysDict getDictById(Long id) {
        return dictMapper.selectById(id);
    }

    public SysDict getDictByCode(String code) {
        return dictMapper.selectByCode(code);
    }

    @Transactional
    public SysDict createDict(SysDict dict) {
        dictMapper.insert(dict);
        return dict;
    }

    @Transactional
    public SysDict updateDict(Long id, SysDict dict) {
        dict.setId(id);
        dictMapper.updateById(dict);
        return dictMapper.selectById(id);
    }

    @Transactional
    public void deleteDict(Long id) {
        dictMapper.deleteById(id);
        // 同时删除该字典下的所有字典项
        dictItemMapper.delete(
                new LambdaQueryWrapper<SysDictItem>().eq(SysDictItem::getDictId, id));
    }

    // ========== 字典项 ==========

    public List<SysDictItem> listItemsByDictId(Long dictId) {
        return dictItemMapper.selectByDictId(dictId);
    }

    public List<SysDictItem> listItemsByDictCode(String dictCode) {
        SysDict dict = dictMapper.selectByCode(dictCode);
        if (dict == null)
            return List.of();
        return dictItemMapper.selectByDictId(dict.getId());
    }

    public SysDictItem getItemById(Long id) {
        return dictItemMapper.selectById(id);
    }

    @Transactional
    public SysDictItem createItem(SysDictItem item) {
        dictItemMapper.insert(item);
        return item;
    }

    @Transactional
    public SysDictItem updateItem(Long id, SysDictItem item) {
        item.setId(id);
        dictItemMapper.updateById(item);
        return dictItemMapper.selectById(id);
    }

    @Transactional
    public void deleteItem(Long id) {
        dictItemMapper.deleteById(id);
    }
}
