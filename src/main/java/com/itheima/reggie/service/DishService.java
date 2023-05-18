package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.Entity.Dish;
import com.itheima.reggie.dto.DishDto;

public interface DishService extends IService<Dish> {

    //添加菜品，同时对dish和dish_flavor两张表进行操作
    public void saveWithFlavor(DishDto dishDto);

    //修改菜品，同时对两张表进行查询
    public DishDto getByIdWithFlavor(Long id);

    public void updateByIdWithFlavor(DishDto dishDto);
}
