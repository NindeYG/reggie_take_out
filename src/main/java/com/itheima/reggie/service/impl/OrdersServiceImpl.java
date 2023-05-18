package com.itheima.reggie.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.reggie.Entity.*;
import com.itheima.reggie.common.BaseContext;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.mapper.OrdersMapper;
import com.itheima.reggie.service.*;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Service
public class OrdersServiceImpl extends ServiceImpl<OrdersMapper, Orders> implements OrdersService {


    @Autowired
    private ShoppingCartService shoppingCartService;

    @Autowired
    private OrderDetailService orderDetailService;

    @Autowired
    private UserService userService;

    @Autowired
    private AddressService addressService;

    @Transactional
    public void submit(Orders orders) {
        //获取用户id&地址id
        Long userId = BaseContext.getCurrentId();
        Long addressBookId = orders.getAddressBookId();
        //查询当前用户的购物车数据
        LambdaQueryWrapper<ShoppingCart> queryWrapper=new LambdaQueryWrapper();
        queryWrapper.eq(ShoppingCart::getUserId,userId);
        List<ShoppingCart> shoppingCartList = shoppingCartService.list(queryWrapper);
        if(shoppingCartList==null||shoppingCartList.size()==0){
            throw new CustomException("购物车为空，不能下单！");
        }
        AtomicInteger amount=new AtomicInteger(0);
        //订单号
        Long id = IdWorker.getId();
        orders.setNumber(String.valueOf(id));
        orders.setId(id);
        //1.获取订单id
        Long ordersId = id;
        //2.获取orderDetail List
        List<OrderDetail> orderDetailList = shoppingCartList.stream().map((item)->{
            OrderDetail orderDetail = new OrderDetail();
            //拷贝信息
            BeanUtils.copyProperties(item,orderDetail);
            orderDetail.setOrderId(ordersId);
            amount.addAndGet(item.getAmount().multiply(new BigDecimal(item.getNumber())).intValue());
            return orderDetail;
        }).collect(Collectors.toList());
        //查询用户数据
        User user = userService.getById(userId);
        //查询地址信息
        AddressBook address = addressService.getById(addressBookId);
        if(address==null){
            throw new CustomException("地址有误，无法下单！");
        }

        //总金额
        orders.setAmount(new BigDecimal(amount.get()));
        //用户id
        orders.setUserId(userId);
        //用户名
        orders.setUserName(user.getName());
        //电话
        orders.setPhone(user.getPhone());
        //收货人
        orders.setConsignee(address.getConsignee());
        //收货地址
        orders.setAddress((address.getProvinceName()==null? "":address.getProvinceName())
                +(address.getCityName()==null? "":address.getCityName())
                +(address.getDistrictName()==null? "":address.getDistrictName())
                +(address.getDetail()==null? "":address.getDetail()));
        //下单时间
        orders.setOrderTime(LocalDateTime.now());
        //付款时间
        orders.setCheckoutTime(LocalDateTime.now());
        //订单状态
        orders.setStatus(2);
        //金额

        //向订单表中插入数据,一条
        this.save(orders);
        //向订单明细表中插入多条数据
        orderDetailService.saveBatch(orderDetailList);
        //下单成功后，清空购物车数据
        shoppingCartService.remove(queryWrapper);

    }
}
