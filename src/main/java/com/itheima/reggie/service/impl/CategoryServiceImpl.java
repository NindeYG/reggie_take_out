package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.Entity.Category;
import com.itheima.reggie.Entity.Dish;
import com.itheima.reggie.Entity.Setmeal;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.mapper.CategoryMapper;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishService;
import com.itheima.reggie.service.SetMealService;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CategoryServiceImpl extends ServiceImpl<CategoryMapper, Category> implements CategoryService {


    @Autowired
    private DishService dishService;

    @Autowired
    private SetMealService setMealService;
    /*根据id进行删除，删除前进行判断*/
    @Override
    public void remove(Long id) {
        LambdaQueryWrapper<Dish> lambdaQueryWrapper1=new LambdaQueryWrapper<>();
        //设置查询条件，根据categoryId在dish表中进行查询
        lambdaQueryWrapper1.eq(Dish::getCategoryId,id);
        int count1 = dishService.count(lambdaQueryWrapper1);
        //判断当前分类是否与菜品相关联，如果关联抛出一个业务异常
        if(count1>0){
            //当前分类与菜品相关联
            throw new CustomException("当前分类与菜品相关联，删除失败！");
        }
        LambdaQueryWrapper<Setmeal> lambdaQueryWrapper2=new LambdaQueryWrapper<>();
        lambdaQueryWrapper2.eq(Setmeal::getCategoryId,id);
        int count2=setMealService.count(lambdaQueryWrapper2);
        //判断当前分类是否与套餐相关联，如果关联抛出一个业务异常
        if(count2>0){
            //当前分类与套餐相关联
             throw new CustomException("当前分类与套餐相关联，删除失败！");
        }
        //进行删除操作
        super.removeById(id);
    }
}
