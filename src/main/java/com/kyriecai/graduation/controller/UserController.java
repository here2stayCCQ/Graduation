package com.kyriecai.graduation.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyriecai.graduation.common.Constants;
import com.kyriecai.graduation.common.Result;
import com.kyriecai.graduation.controller.dto.UserDTO;
import com.kyriecai.graduation.controller.dto.UserPasswordDTO;
import com.kyriecai.graduation.utils.TokenUtils;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;

import com.kyriecai.graduation.service.IUserService;
import com.kyriecai.graduation.entity.User;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  用户前端控制器
 * </p>
 *
 * @author kyriecai
 * @since 2022-04-17
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private IUserService userService;

    //新增或者更新用户数据
    @PostMapping
    public Result save(@RequestBody User user) {
        return Result.success(userService.saveOrUpdate(user));
    }

    //删除用户数据
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(userService.removeById(id));
    }

    //批量删除对应id用户
    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {    //从前台接收选中的纯id数组转化为list
        return Result.success(userService.removeBatchByIds(ids));
    }

    //查找所有用户
    @GetMapping
    public Result findAll() {
        return Result.success(userService.list());
    }

    //根据用户名查找用户
    @GetMapping("/username/{username}")
    public Result findOneByUsername(@PathVariable String username) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",username);
        return Result.success(userService.getOne(queryWrapper));
    }

    //根据id查找用户
    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(userService.getById(id));
    }

    //分页查询
    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                                @RequestParam Integer pageSize,
                               @RequestParam(defaultValue = "") String username,
                               @RequestParam(defaultValue = "") String email,
                               @RequestParam(defaultValue = "") String address) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (!"".equals(username)) {
            queryWrapper.like("username",username);
        }
        if (!"".equals(email)) {
            queryWrapper.like("email",email);
        }
        if (!"".equals(address)) {
            queryWrapper.like("address",address);
        }

        //获取当前按用户信息
        User currentUser = TokenUtils.getCurrentUser();
        System.out.println("获取当前用户信息=========" + currentUser.getNickname());

        return Result.success(userService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 用户导出为Excel
     * @param response
     * @throws Exception
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        //从数据库查出所有用户数据
        List<User> list = userService.list();

        //通过工具类创建writer写出到磁盘路径
        ExcelWriter writer = ExcelUtil.getWriter(true);
        //自定义标题别名


        //一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list,true);
        writer.addHeaderAlias("username","用户名");
        writer.addHeaderAlias("password","密码");
        writer.addHeaderAlias("nickname","昵称");
        writer.addHeaderAlias("email","邮箱");
        writer.addHeaderAlias("phone","电话");
        writer.addHeaderAlias("address","地址");
        writer.addHeaderAlias("createTime","创建时间");
        writer.addHeaderAlias("avatarUrl","头像");
        //设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("用户信息", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out,true); //将writer中的内容刷新到输出流中
        out.close();
        writer.close();
    }

    /**
     * 通过Excel导入用户
     * @param file
     * @throws Exception
     */
    @PostMapping("/import")
    public Result imp(MultipartFile file) throws Exception{
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        //通过JavaBean的方式读取excel内的对象，但要求表头必须是英文，跟JavaBean的属性要对应起来，可以通过在User类注释@Alias实现中文别名
        List<User> list = reader.readAll(User.class);
//        List<User> list = reader.read(0, 1, User.class);
        System.out.println(list);
        return Result.success(userService.saveBatch(list));
    }

    /**
     * 登录接口
     * @param userDto
     * @return
     */
    @PostMapping("/login")
    public Result login(@RequestBody UserDTO userDto){
        String username = userDto.getUsername();
        String password = userDto.getPassword();
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password))
            return Result.error(Constants.CODE_400,"参数错误");
        UserDTO dto = userService.login(userDto);
        return Result.success(dto);
    }

    /**
     * 注册接口
     * @param userDto
     * @return
     */
    @PostMapping("/register")
    public Result register(@RequestBody UserDTO userDto) {
        String username = userDto.getUsername();
        String password = userDto.getPassword();
        if (StrUtil.isBlank(username) || StrUtil.isBlank(password))
            return Result.error(Constants.CODE_400,"参数错误");
        return Result.success(userService.register(userDto));
    }

    /**
     * 修改密码
     * @param userPasswordDTO
     * @return
     */
    @PostMapping("/password")
    public Result changePassword(@RequestBody UserPasswordDTO userPasswordDTO) {
        userService.updatePassword(userPasswordDTO);
        return Result.success();
    }

}

