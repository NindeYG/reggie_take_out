package com.itheima.reggie.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.itheima.reggie.Entity.Orders;
import org.springframework.core.annotation.Order;

public interface OrdersService extends IService<Orders> {

    public void submit(Orders orders);
}
