package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.Entity.Category;
import com.itheima.reggie.Entity.Dish;
import com.itheima.reggie.Entity.Setmeal;
import com.itheima.reggie.Entity.SetmealDish;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetMealDishService;
import com.itheima.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/setmeal")
@Slf4j
public class SetMealController {
    @Autowired
    private SetMealService setMealService;

    @Autowired
    private SetMealDishService setMealDishService;

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @PostMapping
    public R<String> save(@RequestBody SetmealDto setmealDto){
        log.info("新增套餐！");
        setMealService.saveWithDishes(setmealDto);
        return R.success("添加套餐成功！");

    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        Page<Setmeal> page1=new Page(page,pageSize);
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.like(name!=null, Setmeal::getName,name);
        queryWrapper.orderByDesc(Setmeal::getUpdateTime);
        setMealService.page(page1,queryWrapper);
        //拷贝基本信息
        Page<SetmealDto> page2=new Page(page,pageSize);
        BeanUtils.copyProperties(page1,page2,"records");
        //
        List<Setmeal> setmealList=page1.getRecords();
        List<SetmealDto> setmealDtoList=setmealList.stream().map((item)->{
            SetmealDto setmealDto=new SetmealDto();
            BeanUtils.copyProperties(item,setmealDto);
            Category category = categoryService.getById(item.getCategoryId());
            setmealDto.setCategoryName(category.getName());
            return setmealDto;
        }).collect(Collectors.toList());
        page2.setRecords(setmealDtoList);
        return R.success(page2);
    }

    //基本数据类型不需要加@RequestParam注解，但其他的需要
    @DeleteMapping
    public R<String> delete(@RequestParam List<Long> ids){
        setMealService.deleteWithDishes(ids);
        return R.success("删除成功！");
    }

    /*停售*/
    @PostMapping("/status/0")
    public R<String> statusStop(@RequestParam List<Long> ids){
        //查询对应的套餐
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        List<Setmeal> setmealList = setMealService.list(queryWrapper);
        //使用流的方法修改每一个套餐的status
        List<Setmeal> list=setmealList.stream().map((item)->{
            item.setStatus(0);
            setMealService.updateById(item);
            return item;
        }).collect(Collectors.toList());
        return R.success("修改成功！");
    }

    /*启售*/
    @PostMapping("/status/1")
    public R<String> statusBegin(@RequestParam List<Long> ids){
        //查询对应的套餐
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId, ids);
        List<Setmeal> setmealList = setMealService.list(queryWrapper);
        //使用流的方法修改每一个套餐的status
        List<Setmeal> list=setmealList.stream().map((item)->{
            item.setStatus(1);
            setMealService.updateById(item);
            return item;
        }).collect(Collectors.toList());
        return R.success("修改成功！");
    }

    @GetMapping("/list")
    public R<List<Setmeal>> list(Setmeal setmeal){
        Long categoryId=setmeal.getCategoryId();
        Integer status = setmeal.getStatus();
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Setmeal::getCategoryId,categoryId);
        queryWrapper.eq(Setmeal::getStatus,status);
        List<Setmeal> setmealList = setMealService.list(queryWrapper);
        return R.success(setmealList);
    }

}
