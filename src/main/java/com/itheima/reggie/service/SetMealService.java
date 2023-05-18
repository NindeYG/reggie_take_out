package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.Entity.Setmeal;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.dto.SetmealDto;

import java.util.List;

public interface SetMealService extends IService<Setmeal> {

    public void saveWithDishes(SetmealDto setmealDto);

    public void deleteWithDishes(List<Long> ids);
}
