package com.baizhi.controller;

import com.baizhi.entity.User;
import com.baizhi.entity.UserFile;
import com.baizhi.service.UserFileService;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ResourceUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.Mapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.*;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/file")
public class FileController {


    @Autowired
    private UserFileService userFileService;


    @GetMapping("download")
    public void download(String openStyle,String id, HttpServletResponse response) throws IOException {
        //获取打开方式
        openStyle=openStyle==null?"attachment":openStyle;
        //通过文件id查找对应的文件,获取文件信息
        UserFile userFile=userFileService.findById(id);
        //点击下载链接更新下载次数
        if ("attachment".equals(openStyle)){
            userFile.setDowncounts(userFile.getDowncounts()+1);
            userFileService.update(userFile);
        }
        //根据文件信息中文件名字和文件存储路径获取文件输入流
        String realpath=ResourceUtils.getURL("classpath:").getPath()+"/static"+userFile.getPath();
        //获取文件输入流
        FileInputStream is=new FileInputStream(new File(realpath,userFile.getNewFileName()));
        //附件下载attachment
        //attachment改成inline就是在线打开！
        response.setHeader("content-disposition",openStyle+";fileName="+ URLEncoder.encode(userFile.getOldFileName(),"UTF-8"));
        //获取响应输出流
        ServletOutputStream os = response.getOutputStream();
        //文件拷贝
        IOUtils.copy(is,os);
        IOUtils.closeQuietly(is);
        IOUtils.closeQuietly(os);
    }


    @GetMapping("delete")
    public String delete(String id) throws FileNotFoundException {
        //根据文件id查询找到文件信息
        UserFile userFile = userFileService.findById(id);
        //删除文件
        String realPath=ResourceUtils.getURL("classpath").getPath()+"/static"+userFile.getPath();
        //文件的存在要有路径和文件名
        File file = new File(realPath, userFile.getNewFileName());
        if (file.exists()) {
            file.delete();
        }
        userFileService.delete(id);
        return "redirect:/file/showAll";
    }














    @PostMapping("/upload")
    public String upload(MultipartFile aaa,HttpSession session) throws IOException {
        User user = (User) session.getAttribute("user");
        //获取文件原始名称
        String oldFileName = aaa.getOriginalFilename();
        //获取文件后缀名
        String extension="."+FilenameUtils.getExtension(aaa.getOriginalFilename());
        //生成新的文件名称
       String newFileName=new SimpleDateFormat("yyyyMMddHHmmss").format(new Date())+ UUID.randomUUID().toString().replace("-","")+extension;
        //文件大小
        Long size = aaa.getSize();
        //文件类型
        String type = aaa.getContentType();
        //处理文件上传
        //ResourceUtils.getURL("classpath:")拿到是resources这个路径
        //ResourceUtils.getURL("classpath:").getPath()通过相对获取绝对
        String realPath=ResourceUtils.getURL("classpath:").getPath()+ "/static/files";
        //构建日期的文件夹
        String dateFormat=new SimpleDateFormat("yyyy-MM-dd").format(new Date());
        String dateDirPath=realPath+"/"+dateFormat;
        File dateDir= new File(dateDirPath);
        if(!dateDir.exists()) {
            dateDir.mkdirs();//如果不存在就去创建多级目录。
        }
        //处理文件上传
        aaa.transferTo(new File(dateDir,newFileName));
        //将文件信息放入数据库中
        UserFile userFile=new UserFile();
        userFile.setOldFileName(oldFileName).setNewFileName(newFileName).setExt(extension).setSize(String.valueOf(size))
                .setType(type).setPath("/files/" + dateFormat).setUserId(user.getId());
        userFileService.save(userFile);
        //跳转到展示页面
        return "redirect:/file/showAll";
    }




    @GetMapping("/showAll")
    public String findAll(HttpSession session, Model model){
        User user = (User) session.getAttribute("user");
        List<UserFile> userFiles = userFileService.findByUserId(user.getId());
        model.addAttribute("files", userFiles);
        return "showAll";
    }




}
