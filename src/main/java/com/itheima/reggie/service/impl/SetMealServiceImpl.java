package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.Entity.Setmeal;
import com.itheima.reggie.Entity.SetmealDish;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.dto.SetmealDto;
import com.itheima.reggie.mapper.SetMealMapper;
import com.itheima.reggie.service.SetMealDishService;
import com.itheima.reggie.service.SetMealService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SetMealServiceImpl extends ServiceImpl<SetMealMapper, Setmeal> implements SetMealService {
    @Autowired
    private SetMealDishService setMealDishService;

    @Override
    public void saveWithDishes(SetmealDto setmealDto) {
        //保存套餐基本信息
        super.save(setmealDto);
        //通过流的方法，为每一个菜品的seatmealid赋值
        Long setMealId=setmealDto.getId();
        List<SetmealDish> list=setmealDto.getSetmealDishes();
        list=list.stream().map((item)->{
            item.setSetmealId(setMealId);
            return item;
        }).collect(Collectors.toList());
        setMealDishService.saveBatch(list);

    }

    @Transactional
    @Override
    public void deleteWithDishes(List<Long> ids) {
        //检查套餐的售卖状态，只有停售的才能删除,如果不能删除，抛出异常
        LambdaQueryWrapper<Setmeal> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.in(Setmeal::getId,ids);
        queryWrapper.eq(Setmeal::getStatus,1);
        int count = this.count(queryWrapper);
        if(count>0){
            throw new CustomException("套餐正在售卖中，无法删除！");
        }
        //删除套餐表中的数据
        this.removeByIds(ids);
        //删除关联表中的数据，当前的表是通过获得的id相关联的
        LambdaQueryWrapper<SetmealDish> queryWrapper1=new LambdaQueryWrapper();
        queryWrapper1.in(SetmealDish::getSetmealId,ids);
        setMealDishService.remove(queryWrapper1);
    }
}
