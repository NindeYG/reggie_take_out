package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.itheima.reggie.Entity.AddressBook;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.R;
import com.itheima.reggie.service.AddressService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/addressBook")
@Slf4j
public class AddressController {

    @Autowired
    private AddressService addressService;

    @GetMapping("/list")
    public R<List<AddressBook>> list(){
        //查询当前userId对应的数据
        LambdaQueryWrapper<AddressBook> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        queryWrapper.orderByDesc(AddressBook::getUpdateTime);
        List<AddressBook> list=addressService.list(queryWrapper);
        return R.success(list);
    }

    @PostMapping
    public R<String> save(@RequestBody AddressBook addressBook){
        //获取UserId
        addressBook.setUserId(BaseContext.getCurrentId());
        addressService.save(addressBook);
        return R.success("新增地址成功！");
    }

    /*根据id查询,在页面回显*/
    @GetMapping("/{id}")
    public R<AddressBook> getById(@PathVariable Long id){
        LambdaQueryWrapper<AddressBook> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getId,id);
        AddressBook address = addressService.getOne(queryWrapper);
        if(address==null){
            return R.error("没有找到该对象！");
        }
        return R.success(address);
    }

    @PutMapping
    public R<String> update(@RequestBody AddressBook addressBook){
        addressService.updateById(addressBook);
        return R.success("修改成功！");
    }

    @Transactional
    @PutMapping("/default")
    public R<String> setDefault(@RequestBody AddressBook addressBook){
        //把该userId对应的所有地址的状态都赋0
        LambdaUpdateWrapper<AddressBook> updateWrapper1=new LambdaUpdateWrapper<>();
        updateWrapper1.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        updateWrapper1.set(AddressBook::getIsDefault,0);
        addressService.update(updateWrapper1);

        //根据id获取新的默认地址,并更新其状态
        LambdaUpdateWrapper<AddressBook> updateWrapper2=new LambdaUpdateWrapper<>();
        updateWrapper2.eq(AddressBook::getId,addressBook.getId());
        updateWrapper2.set(AddressBook::getIsDefault,1);
        addressService.update(updateWrapper2);
        return R.success("修改成功");
    }

    /*查询默认地址*/
    @GetMapping("/default")
    public R<AddressBook> getDefault(){
        LambdaQueryWrapper<AddressBook> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.eq(AddressBook::getUserId,BaseContext.getCurrentId());
        queryWrapper.eq(AddressBook::getIsDefault,1);
        AddressBook default_add = addressService.getOne(queryWrapper);
        if(default_add!=null){
            return R.success(default_add);
        }
        return R.error("没有查询到默认地址！");
    }
}
