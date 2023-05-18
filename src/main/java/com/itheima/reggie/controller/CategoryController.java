package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.Entity.Category;
import com.itheima.reggie.common.R;
import com.itheima.reggie.service.CategoryService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/*现在有一个问题，在断点调试的过程中，如果关闭服务器，则当前的操作仍然会执行*/
@RestController
@RequestMapping("/category")
@Slf4j
public class CategoryController {
    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody Category category){
        log.info("添加分类。。。");
        categoryService.save(category);
        return R.success("新增分类成功！");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize){
        //创建分页构造器
        Page<Category> page1=new Page(page,pageSize);
        //创建条件构造器
        LambdaQueryWrapper<Category> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加排序条件
        lambdaQueryWrapper.orderByDesc(Category::getSort);
        //执行查询
        categoryService.page(page1,lambdaQueryWrapper);
        return R.success(page1);
    }

    @DeleteMapping
    public R<String> delete(Long ids){
        categoryService.remove(ids);
        return R.success("删除成功！");
    }

    @PutMapping
    public R<String> update(@RequestBody Category category){
        log.info("修改分类信息");
        categoryService.updateById(category);
        return R.success("修改分类信息成功！");
    }

    @GetMapping("/list")
    public R<List<Category>> list(Category category){
        //查询构造器
        LambdaQueryWrapper<Category> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(category.getType()!=null,Category::getType,category.getType());
        lambdaQueryWrapper.orderByAsc(Category::getSort).orderByDesc(Category::getUpdateTime);
        List<Category> categoryList = categoryService.list(lambdaQueryWrapper);
        return R.success(categoryList);
    }
}
