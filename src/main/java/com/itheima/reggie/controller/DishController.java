package com.itheima.reggie.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.google.gson.JsonObject;
import com.itheima.reggie.Entity.Category;
import com.itheima.reggie.Entity.Dish;
import com.itheima.reggie.Entity.DishFlavor;
import com.itheima.reggie.common.R;
import com.itheima.reggie.dto.DishDto;
import com.itheima.reggie.service.CategoryService;
import com.itheima.reggie.service.DishFlavorService;
import com.itheima.reggie.service.DishService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.asm.TypeReference;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/dish")
@Slf4j
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private DishFlavorService dishFlavorService;

    @PostMapping
    public R<String> save(@RequestBody DishDto dishDto){
        log.info(dishDto.toString());
        dishService.saveWithFlavor(dishDto);
        //新增成功后需要清理当前类别的缓存
        String key = "dish_"+dishDto.getCategoryId()+" "+dishDto.getStatus();
        redisTemplate.delete(key);
        return R.success("新增菜品成功！");
    }

    @GetMapping("/page")
    public R<Page> page(int page,int pageSize, String name){
        //创建分页构造器
        Page<Dish> page1=new Page(page,pageSize);
        Page<DishDto> page2=new Page<>(page,pageSize);
        //创建条件构造器
        LambdaQueryWrapper<Dish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        //添加搜索条件
        lambdaQueryWrapper.like(name!=null,Dish::getName,name);
        //添加排序条件
        lambdaQueryWrapper.orderByDesc(Dish::getUpdateTime);
        //执行查询操作
        dishService.page(page1,lambdaQueryWrapper);
        //执行拷贝操作
        BeanUtils.copyProperties(page1,page2,"records");

        List<Dish> records=page1.getRecords();
        List<DishDto> list=records.stream().map((item)->{
            //创建dishdto对象，并拷贝item
            DishDto dishDto=new DishDto();
            BeanUtils.copyProperties(item,dishDto);
            //根据category_id查询category_name
            Long categoryId=item.getCategoryId();
            Category category = categoryService.getById(categoryId);
            dishDto.setCategoryName(category.getName());
            return dishDto;
        }).collect(Collectors.toList());
        page2.setRecords(list);
        return R.success(page2);
    }



    @GetMapping("/{id}")
    public R<DishDto> get(@PathVariable Long id){
        DishDto dishDto = dishService.getByIdWithFlavor(id);
        return R.success(dishDto);
    }

    @PutMapping
    public R<String> update(@RequestBody DishDto dishDto){
        dishService.updateByIdWithFlavor(dishDto);
        //更新成功后需要清除缓存
        String key = "dish_"+dishDto.getCategoryId()+" "+dishDto.getStatus();
        redisTemplate.delete(key);
        return R.success("更新成功！");
    }

    /*根据获取的category_id查询对应的菜品*/
//    @GetMapping("/list")
//    public R<List<Dish>> list(Dish dish){
//        Long categoryId=dish.getCategoryId();
//        LambdaQueryWrapper<Dish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
//        lambdaQueryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,categoryId);
//        lambdaQueryWrapper.orderByDesc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
//        List<Dish> dishList = dishService.list(lambdaQueryWrapper);
//        return R.success(dishList);
//    }

    @GetMapping("/list")
    public R<List<DishDto>> listWithFlavor(Dish dish){
        //构造key
        String key = "dish_" + dish.getCategoryId()+" " +dish.getStatus();
        //从redis中获取缓存数据
        String s = redisTemplate.opsForValue().get(key);//从redis中获取的就是Json格式的字符串
        List<DishDto> dishDtoList = JSONObject.parseArray(s, DishDto.class);//将Json字符串反序列化为List
        //如果存在，直接返回，无需查询
        if(dishDtoList != null){

            return R.success(dishDtoList);
        }
        //如果不存在，执行查询操作，从sql数据库中获取数据
        Long categoryId=dish.getCategoryId();
        LambdaQueryWrapper<Dish> lambdaQueryWrapper=new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(dish.getCategoryId()!=null,Dish::getCategoryId,categoryId);
        lambdaQueryWrapper.orderByDesc(Dish::getSort).orderByDesc(Dish::getUpdateTime);
        List<Dish> dishList = dishService.list(lambdaQueryWrapper);
        dishDtoList = dishList.stream().map((item)->{
            DishDto dishDto = new DishDto();
            //拷贝基本信息
            BeanUtils.copyProperties(item,dishDto);
            //查询口味信息
            Long dishId=item.getId();
            LambdaQueryWrapper<DishFlavor> queryWrapper= new LambdaQueryWrapper<>();
            queryWrapper.eq(DishFlavor::getDishId,dishId);
            List<DishFlavor> dishFlavors = dishFlavorService.list(queryWrapper);
            //把口味赋给dishdto
            dishDto.setFlavors(dishFlavors);
            return dishDto;
        }).collect(Collectors.toList());

        //将获取到的数据存入redis中
        redisTemplate.opsForValue().set(key,JSON.toJSON(dishDtoList).toString(),60, TimeUnit.MINUTES);
        return R.success(dishDtoList);
    }
}
