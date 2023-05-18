package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.Entity.ShoppingCart;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.service.ShoppingCartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/shoppingCart")
public class ShoppingCartController {

    @Autowired
    private ShoppingCartService shoppingCartService;


    @GetMapping("/list")
    public R<List<ShoppingCart>> list(){
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId, BaseContext.getCurrentId());
        queryWrapper.orderByDesc(ShoppingCart::getCreateTime);
        List<ShoppingCart> list = shoppingCartService.list(queryWrapper);
        return R.success(list);
    }

    @PostMapping("/add")
    public R<ShoppingCart> add(@RequestBody ShoppingCart shoppingCart){
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        //查询结果要与userId匹配
        queryWrapper.eq(ShoppingCart::getUserId,BaseContext.getCurrentId());
        if(dishId!=null){
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
            ShoppingCart shoppingCart1 = shoppingCartService.getOne(queryWrapper);
            if(shoppingCart1!=null){
                int count=shoppingCart1.getNumber();
                if(count>0){
                    shoppingCart1.setNumber(count+1);
                    shoppingCartService.updateById(shoppingCart1);
                }
            }else {
                shoppingCart.setUserId(BaseContext.getCurrentId());
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCartService.save(shoppingCart);
            }
        }
        if(setmealId!=null){
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
            ShoppingCart shoppingCart1 = shoppingCartService.getOne(queryWrapper);
            if(shoppingCart1!=null){
                int count=shoppingCart1.getNumber();
                if(count>0){
                    shoppingCart1.setNumber(count+1);
                    shoppingCartService.updateById(shoppingCart1);
                }
            }else {
                shoppingCart.setUserId(BaseContext.getCurrentId());
                shoppingCart.setCreateTime(LocalDateTime.now());
                shoppingCartService.save(shoppingCart);
            }
        }

        return R.success(shoppingCart);
    }

    @PostMapping("/sub")
    public R<String> sub(@RequestBody ShoppingCart shoppingCart){
        Long dishId = shoppingCart.getDishId();
        Long setmealId = shoppingCart.getSetmealId();
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        if(dishId!=null){
            //操作的是菜品
            queryWrapper.eq(ShoppingCart::getDishId,dishId);
            ShoppingCart dish = shoppingCartService.getOne(queryWrapper);
            if(dish!=null){
                Integer number = dish.getNumber();
                if(number>1){
                    //直接减一
                    dish.setNumber(number-1);
                    shoppingCartService.updateById(dish);
                }else{
                    //从购物车中删除
                    shoppingCartService.removeById(dish);
                }
            }else{
                return R.error("查询不到当前菜品！");
            }

        }
        if(setmealId!=null){
            //操作的是套餐
            queryWrapper.eq(ShoppingCart::getSetmealId,setmealId);
            ShoppingCart setmeal = shoppingCartService.getOne(queryWrapper);
            if(setmeal!=null){
                Integer number = setmeal.getNumber();
                if(number>1){
                    //直接减一
                    setmeal.setNumber(number-1);
                    shoppingCartService.updateById(setmeal);
                }else{
                    //从购物车中删除
                    shoppingCartService.removeById(setmeal);
                }
            }else {
                return R.error("查询不到当前套餐！");
            }

        }
        return R.success("修改成功！");
    }

    @DeleteMapping("/clean")
    public R<String> clean(){
        Long userId = BaseContext.getCurrentId();
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        shoppingCartService.remove(queryWrapper);
        return R.success("已清空购物车！");
    }
}
