package com.itheima.reggie.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.itheima.reggie.Entity.User;
import com.itheima.reggie.common.CustomException;
import com.itheima.reggie.common.R;
import com.itheima.reggie.service.UserService;
import com.itheima.reggie.utils.SMSUtils;
import com.itheima.reggie.utils.ValidateCodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpSession;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
@Slf4j
public class UserController {
    @Autowired
    private UserService userService;

    @Autowired
    private StringRedisTemplate redisTemplate;

    /*发送手机验证码*/
    @PostMapping("/sendMsg")
    public R<String> sendMsg(@RequestBody User user, HttpSession httpSession){
        //获取手机号
        String phone = user.getPhone();
        //判断是否为空
        if (StringUtils.isEmpty(phone)){
            throw new CustomException("短信发送失败，请输入手机号！");
        }
        //生成随机的4位验证码
        String code = ValidateCodeUtils.generateValidateCode4String(4);
        log.info("code is {}",code);
        //发送短信
        //SMSUtils.sendMessage("阿里云短信测试","SMS_154950909",phone,code);
        //将生成的验证码保存到session
        //httpSession.setAttribute(phone,code);
        //将生成的验证码保存在redis中，并设置有效期为5分钟
        redisTemplate.opsForValue().set(phone,code,5, TimeUnit.MINUTES);
        return R.success("短信发送成功！");
    }

    /*移动端用户登录*/
    @PostMapping("/login")
    public R<User> login(@RequestBody Map map,HttpSession httpSession){
        log.info(map.toString());
        //获取手机号
        String phone = map.get("phone").toString();
        //获取验证码
        String code = map.get("code").toString();
        //获取session中保存的验证码
        //String realCode = httpSession.getAttribute(phone).toString();
        //获取redis中保存的验证码
        String realCode = redisTemplate.opsForValue().get(phone);
        //进行验证码的比较
        if(realCode!=null && code.equals(realCode)){
            //如果正确，检查当前手机号是否已注册，如未注册则自动注册
            LambdaQueryWrapper<User> queryWrapper=new LambdaQueryWrapper<>();
            queryWrapper.eq(User::getPhone,phone);
            User user = userService.getOne(queryWrapper);
            if (user==null){
                //未注册
                user=new User();
                user.setPhone(phone);
                userService.save(user);
            }
            //成功登录，把电话号码存入session
            httpSession.setAttribute("user",user.getId());
            //成功登录，删除redis中保存的验证码
            redisTemplate.delete(phone);
            return R.success(user);
        }else {
            //如果错误，返回R.error
            return R.error("验证码错误！");
        }


    }

}
