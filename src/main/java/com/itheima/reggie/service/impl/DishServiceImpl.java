package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.Entity.Dish;
import com.itheima.reggie.Entity.DishFlavor;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.mapper.DishMapper;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Slf4j
public class DishServiceImpl extends ServiceImpl<DishMapper, Dish> implements DishService {

    @Autowired
    private DishFlavorService dishFlavorService;
    @Autowired
    private DishService dishService;
    /*添加菜品及其口味*/
    @Transactional
    @Override
    public void saveWithFlavor(DishDto dishDto) {
        //先保存菜品的基本信息
        super.save(dishDto);
        //获取菜品的id
        Long dishId = dishDto.getId();
        //将Flavor_list里的每一个口味都赋上id，使用流的方法
        List<DishFlavor> flavors=dishDto.getFlavors();
        flavors = flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        //保存flavors
        dishFlavorService.saveBatch(flavors);
    }


    public DishDto getByIdWithFlavor(Long id){
        //根据id查询菜品的基本信息
        Dish dish = super.getById(id);
        //将dish拷贝至dishdto中
        DishDto dishDto=new DishDto();
        BeanUtils.copyProperties(dish,dishDto);
        LambdaQueryWrapper<DishFlavor> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(DishFlavor::getDishId,id);
        List<DishFlavor> flavorList=dishFlavorService.list(lambdaQueryWrapper);
        dishDto.setFlavors(flavorList);
        return dishDto;
    }

    @Transactional
    @Override
    public void updateByIdWithFlavor(DishDto dishDto) {
        //更新dish表中的内容
        dishService.updateById(dishDto);
        //清除原本的dishFlavor
        LambdaQueryWrapper<DishFlavor> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(DishFlavor::getDishId,dishDto.getId());
        dishFlavorService.remove(queryWrapper);
        //插入新的dishFlavor
        List<DishFlavor> flavors=dishDto.getFlavors();
        Long dishId=dishDto.getId();
        //使用流的方法，为每个flavor赋上dishId
        flavors=flavors.stream().map((item)->{
            item.setDishId(dishId);
            return item;
        }).collect(Collectors.toList());
        dishFlavorService.saveBatch(flavors);
    }
}
