package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.itheima.reggie.Entity.Employee;
import com.itheima.reggie.common.R;
import com.itheima.reggie.service.EmployeeService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.websocket.server.PathParam;
import java.time.LocalDateTime;

@RestController
@Slf4j
@RequestMapping("/employee")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;


    @PostMapping("/login")
    public R<Employee> login(HttpServletRequest request, @RequestBody Employee employee){
        //将密码md5加密处理
        String password=employee.getPassword();
        password=DigestUtils.md5DigestAsHex(password.getBytes());
        //根据用户名在数据库中查询
        LambdaQueryWrapper<Employee> queryWrapper=new LambdaQueryWrapper<>();
        queryWrapper.eq(Employee::getUsername,employee.getUsername());
        Employee emp = employeeService.getOne(queryWrapper);
        //判断是否查到，如果没有则返回登陆失败
        if (emp==null){
            return R.error("登陆失败");
        }
        //判断密码是否正确
        if (!emp.getPassword().equals(password)){
            return R.error("密码错误，登陆失败！");
        }
        if(emp.getStatus()==0){
            return R.error("账号禁用");
        }
        //登陆成功,将员工id存入Sesssion并返回
        request.getSession().setAttribute("employee",emp.getId());
        return R.success(emp);
    }

    @PostMapping("/logout")
    public R<String> logout(HttpServletRequest request){
        //清理session中当前员工的id
        request.getSession().removeAttribute("employee");
        return R.success("退出成功！");

    }

    @PostMapping
    public R<String> save(HttpServletRequest request,@RequestBody Employee employee){
        log.info("添加员工 {}",employee.toString());
        //默认密码123456
        employee.setPassword(DigestUtils.md5DigestAsHex("123456".getBytes()));
//        employee.setCreateTime(LocalDateTime.now());
//        employee.setUpdateTime(LocalDateTime.now());
        //设置创建人、更新人
//        Long userId =(Long) request.getSession().getAttribute("employee");
//        employee.setCreateUser(userId);
//        employee.setUpdateUser(userId);
        //添加新用户
        employeeService.save(employee);
        return R.success("添加成功！");
    }

    @GetMapping("/page")
    public R<Page> page(int page, int pageSize, String name){
        //分页构造器
        Page page1=new Page(page,pageSize);
        //条件构造器
        LambdaQueryWrapper<Employee> lambdaQueryWrapper=new LambdaQueryWrapper();
        //添加查询条件
        lambdaQueryWrapper.like(!StringUtils.isEmpty(name),Employee::getName,name);
        //添加排序条件
        lambdaQueryWrapper.orderByDesc(Employee::getUpdateTime);
        //执行查询,无需返回值，查询结果会被自动封装到page1内
        employeeService.page(page1,lambdaQueryWrapper);
        return R.success(page1);
    }

    @PutMapping()
    public R<String> update(@RequestBody Employee employee,HttpServletRequest request){
        log.info(employee.toString());
        //设置更新时间和更新用户
//        Long empID = (Long) request.getSession().getAttribute("employee");
//        employee.setUpdateTime(LocalDateTime.now());
//        employee.setUpdateUser(empID);
        employeeService.updateById(employee);
        return R.success("员工信息更新成功！");

    }

    @GetMapping("/{id}")
    public R<Employee> getById(@PathVariable Long id){
        log.info("根据员工id查询");
        Employee emp = employeeService.getById(id);
        if(emp != null) {
            return R.success(emp);
        }
        return R.error("没有查询到当前员工信息！");

    }
}
