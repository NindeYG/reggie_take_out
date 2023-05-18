package com.itheima.reggie.controller;

import com.itheima.reggie.common.R;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

/*进行文件上传下载处理*/
@RestController
@RequestMapping("/common")
@Slf4j
public class CommonController {

    @Value("${reggie.path}")
    private String basePath;
    /*处理文件上传，multipartfile 的形参名需要与前端一致*/
    @PostMapping("/upload")
    public R<String> upload(MultipartFile file) throws IOException {
        //此时file为临时文件，需要转存到指定位置，否则本次请求结束后会被自动删除
        log.info("文件上传。。。"+file.toString());
        //使用原始文件名，可能会出现重名的问题
        //String filename = file.getOriginalFilename();
        //使用随机生成的文件名，避免重复
        String filename = UUID.randomUUID().toString();
        //获取原始文件的后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = originalFilename.substring(originalFilename.lastIndexOf("."));
        filename=filename+suffix;
        //创建目录对象
        File dirs=new File(basePath);
        if (!dirs.exists()){
            //如果当前目录不存在，则创建目录
            dirs.mkdirs();
        }

        file.transferTo(new File(basePath + filename));
        return R.success(filename);
    }

    @GetMapping("/download")
    public void download(String name, HttpServletResponse response) throws Exception {
        //创建输入流，通过输入流读取文件内容
        FileInputStream fileInputStream=new FileInputStream(new File(basePath+name));
        //创建输出流，通过输出流将文件写回浏览器，并在浏览器展示
        ServletOutputStream outputStream=response.getOutputStream();
        response.setContentType("image/png");

        //读写的具体过程
        int len=0;
        byte[] bytes=new byte[1024];
        while ((len=fileInputStream.read(bytes)) != -1){
            outputStream.write(bytes,0,len);
            outputStream.flush();
        }
        outputStream.close();
        fileInputStream.close();
    }
}
