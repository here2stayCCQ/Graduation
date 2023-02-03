package com.kyriecai.graduation.controller;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.file.FileReader;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.crypto.SecureUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyriecai.graduation.common.Result;
import com.kyriecai.graduation.config.AuthAccess;
import com.kyriecai.graduation.entity.Avatar;
import com.kyriecai.graduation.entity.MyFile;
import com.kyriecai.graduation.entity.Normcorneal;
import com.kyriecai.graduation.mapper.AvatarMapper;
import com.kyriecai.graduation.mapper.FileMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URLEncoder;
import java.util.List;

/**
 * 文件操作相关接口
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Value("${files.upload.path}")
    private String fileUploadPath;

    @Value("${files.upload.avatarpath}")
    private String avatarPath;

    @Value("${server.ip}")
    private String serverIp;

    @Resource
    private FileMapper fileMapper;

    @Resource
    private AvatarMapper avatarMapper;

    /**
     * 文件上传接口
     * @param file 前端传递过来的文件
     * @return
     * @throws IOException
     */
    @PostMapping("/upload")
    public Result upload(@RequestParam MultipartFile file,@RequestParam Integer uploaderId) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String type = FileUtil.extName(originalFilename);
        long size = file.getSize();


        //定义一个文件唯一的标识码
        String uuid = IdUtil.fastSimpleUUID();
        String fileUUID = uuid + StrUtil.DOT + type;

        File uploadFile = new File(fileUploadPath + fileUUID);
        //判断配置的文件目录是否存在，若不存在则创建一个新的文件目录
        if (!uploadFile.getParentFile().exists()){
                uploadFile.getParentFile().mkdirs();
        }

        //获取文件的md5，并再数据库中查找是否已经存在此md5的文件（目的是通过对比md5避免重复上传相同内容的文件）
        String md5 = SecureUtil.md5(file.getInputStream()); //注意从输入流中获取
        MyFile myFile = getFileByMd5(md5);


        //获取文件的url
        String url;
        if (myFile != null) //若已有此md5的文件存在，则不重复存储文件，url即已存在文件的url
            url = myFile.getUrl();
        else {
            //否则，把获取到的文件存储到磁盘目录，并记录新存储的文件的url
            file.transferTo(uploadFile);
            url = "http://"+ serverIp +":9090/file/"+fileUUID;
        }

        //从文件名中获取病例信息
        int pos = originalFilename.indexOf("(");
        String patientName = originalFilename.substring(0, pos - 1);
        String eye = "";
        if (originalFilename.contains("Left")) {
            eye = "left";
        } else if(originalFilename.contains("Right")){
            eye = "right";
        }
        int pos2 = originalFilename.indexOf("2");
        String createTime = originalFilename.substring(pos2, pos2 + 8);
        int pos3 = originalFilename.lastIndexOf("_");
        String code = originalFilename.substring(pos3+1, originalFilename.indexOf("."));

        //存储文件信息到数据库
        MyFile saveFile = new MyFile();
        saveFile.setName(originalFilename);
        saveFile.setType(type);
        saveFile.setSize(size/1024);
        saveFile.setUrl(url);
        saveFile.setMd5(md5);
        saveFile.setUploaderId(uploaderId);
        saveFile.setPatientName(patientName);
        saveFile.setEye(eye);
        saveFile.setCreateTime(createTime);
        saveFile.setCode(code);

        fileMapper.insert(saveFile);
        return Result.success(uploadFile.getAbsolutePath());
    }

    /**
     * 头像上传接口
     * @param file 前端传递过来的文件
     * @return
     * @throws IOException
     */
    @PostMapping("/uploadavatar")
    public String uploadAvatar(@RequestParam MultipartFile file) throws IOException {
        String originalFilename = file.getOriginalFilename();
        String type = FileUtil.extName(originalFilename);
        long size = file.getSize();


        //定义一个文件唯一的标识码
        String uuid = IdUtil.fastSimpleUUID();
        String fileUUID = uuid + StrUtil.DOT + type;

        File uploadFile = new File(avatarPath + fileUUID);
        //判断配置的文件目录是否存在，若不存在则创建一个新的文件目录
        if (!uploadFile.getParentFile().exists()){
            uploadFile.getParentFile().mkdirs();
        }

        //获取文件的md5，并再数据库中查找是否已经存在此md5的文件（目的是通过对比md5避免重复上传相同内容的文件）
        String md5 = SecureUtil.md5(file.getInputStream()); //注意从输入流中获取
        Avatar myAvatar = getAvatarByMd5(md5);


        //获取文件的url
        String url;
        if (myAvatar != null) //若已有此md5的文件存在，则不重复存储文件，url即已存在文件的url
            url = myAvatar.getUrl();
        else {
            //否则，把获取到的文件存储到磁盘目录，并记录新存储的文件的url
            file.transferTo(uploadFile);
            url = "http://"+ serverIp +":9090/file/avatar/"+fileUUID;
        }


        //存储文件信息到数据库
        Avatar saveAvatar = new Avatar();
        saveAvatar.setName(originalFilename);
        saveAvatar.setType(type);
        saveAvatar.setSize(size/1024);
        saveAvatar.setUrl(url);
        saveAvatar.setMd5(md5);
        avatarMapper.insert(saveAvatar);
        return url;
    }

    /**
     * 文件下载接口 http://localhost:9090/file/"{fileUUID}
     * @param fileUUID
     * @param response
     * @throws IOException
     */
    @GetMapping("/{fileUUID}")
    public void download(@PathVariable String fileUUID, HttpServletResponse response) throws IOException {
        //根据文件的唯一标识码获取文件
        File uploadFile = new File(fileUploadPath + fileUUID);
        // 设置输出流的格式
        ServletOutputStream os = response.getOutputStream();
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileUUID, "UTF-8"));
        response.setContentType("application/octet-stream");

        // 读取文件的字节流
        os.write(FileUtil.readBytes(uploadFile));
        os.flush();
        os.close();
    }

    /**
     * 头像下载接口
     * @param fileUUID
     * @param response
     * @throws IOException
     */
    @GetMapping("/avatar/{fileUUID}")
    public void downloadAvatar(@PathVariable String fileUUID, HttpServletResponse response) throws IOException {
        //根据文件的唯一标识码获取文件
        File uploadFile = new File(avatarPath + fileUUID);
        // 设置输出流的格式
        ServletOutputStream os = response.getOutputStream();
        response.addHeader("Content-Disposition", "attachment;filename=" + URLEncoder.encode(fileUUID, "UTF-8"));
        response.setContentType("application/octet-stream");

        // 读取文件的字节流
        os.write(FileUtil.readBytes(uploadFile));
        os.flush();
        os.close();
    }

    /**
     * （辅助方法）通过文件的md5查询文件
     * @param md5
     * @return
     */
    private MyFile getFileByMd5(String md5) {
        //查询数据库中是否md5是否已存在
        QueryWrapper<MyFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("md5",md5);
        List<MyFile> myFiles = fileMapper.selectList(queryWrapper);
        return myFiles.size() == 0 ? null : myFiles.get(0);
    }

    private Avatar getAvatarByMd5(String md5) {
        //查询数据库中是否md5是否已存在
        QueryWrapper<Avatar> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("md5",md5);
        List<Avatar> myAvatars = avatarMapper.selectList(queryWrapper);
        return myAvatars.size() == 0 ? null : myAvatars.get(0);
    }

    @PostMapping("/update")
    public Result update(@RequestBody MyFile myFile){
        return Result.success(fileMapper.updateById(myFile));
    }

    //删除文件
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        MyFile myFile = fileMapper.selectById(id);
        myFile.setIsDelete(true);
        fileMapper.updateById(myFile);
        return Result.success();
    }

    //批量删除对应id文件
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {    //从前台接收选中的纯id数组转化为list

        //select * from sys_file where id in (id,id,id)...
        QueryWrapper<MyFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.in("id",ids);
        List<MyFile> myFiles = fileMapper.selectList(queryWrapper);
        for (MyFile myFile : myFiles) {
            myFile.setIsDelete(true);
            fileMapper.updateById(myFile);
        }

        return Result.success();
    }

    /**
     * 后台分页查询接口
     * @param pageNum
     * @param pageSize
     * @param name
     * @return
     */
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String name) {
        QueryWrapper<MyFile> queryWrapper = new QueryWrapper<>();
        //查询未被假删除的记录
        queryWrapper.eq("is_delete",false);
        queryWrapper.orderByDesc("id");

        if (!"".equals(name)) {
            queryWrapper.like("name",name);
        }

        return Result.success(fileMapper.selectPage(new Page<>(pageNum,pageSize),queryWrapper));
    }


    /**
     * 前台获得/查询各个病例
     * @param code
     * @param patientName
     * @return
     */
    @GetMapping("/allpatients")
    public Result getAllPatients(@RequestParam(defaultValue = "") String code,
                                 @RequestParam(defaultValue = "") String patientName) {
        QueryWrapper<MyFile> queryWrapper = new QueryWrapper<>();
        queryWrapper = queryWrapper.select("DISTINCT patient_name,`eye`,create_time,code");
        if (!"".equals(code)) {
            queryWrapper = queryWrapper.like("code",code);
        }
        if (!"".equals(patientName)) {
            queryWrapper = queryWrapper.like("patient_name",patientName);
        }

        return Result.success(fileMapper.selectList(queryWrapper));
    }

    /**
     * 前台下载病例的圆锥角膜轮廓文本
     * @param patientName
     * @return
     */
    @GetMapping("/downloadtxt/{patientName}")
    public Result getText(@PathVariable String patientName) {
        QueryWrapper<MyFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_name",patientName);
        queryWrapper.eq("type","txt");
        MyFile txt = fileMapper.selectOne(queryWrapper);
        return Result.success(txt.getUrl());
    }

    @GetMapping("/getvideo/{patientName}/{eye}")
    public Result getVideo(@PathVariable String patientName,@PathVariable String eye) {
        QueryWrapper<MyFile> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("patient_name",patientName);
        queryWrapper.eq("eye",eye);
        queryWrapper.eq("type","mp4");
        MyFile video = fileMapper.selectOne(queryWrapper);
        return Result.success(video.getUrl());
    }
}
