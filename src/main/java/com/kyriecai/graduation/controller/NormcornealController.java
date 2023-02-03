package com.kyriecai.graduation.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyriecai.graduation.config.AuthAccess;
import com.kyriecai.graduation.entity.User;
import com.kyriecai.graduation.mapper.NormcornealMapper;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import com.kyriecai.graduation.common.Result;

import com.kyriecai.graduation.service.INormcornealService;
import com.kyriecai.graduation.entity.Normcorneal;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author kyriecai
 * @since 2022-05-05
 */
@RestController
@RequestMapping("/normcorneal")
public class NormcornealController {

    @Resource
    private INormcornealService normcornealService;

    @Resource
    private NormcornealMapper normcornealMapper;

    @PostMapping
    public Result save(@RequestBody Normcorneal normcorneal) {
        return Result.success(normcornealService.saveOrUpdate(normcorneal));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(normcornealService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {    //从前台接收选中的纯id数组转化为list
        return Result.success(normcornealService.removeBatchByIds(ids));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(normcornealService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(normcornealService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String patientNum,
                           @RequestParam(defaultValue = "") String name,
                           @RequestParam(defaultValue = "") String eye) {
        QueryWrapper<Normcorneal> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (!"".equals(patientNum)) {
            queryWrapper.like("patient_num",patientNum);
        }
        if (!"".equals(name)) {
            queryWrapper.like("name",name);
        }
        if (!"".equals(eye)) {
            queryWrapper.like("eye",eye);
        }
        return Result.success(normcornealService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 通过Excel导入正常角膜数据
     * @param file
     * @throws Exception
     */
    @PostMapping("/import")
    public Result imp(MultipartFile file,@RequestParam Integer uploaderId) throws Exception{
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        //通过JavaBean的方式读取excel内的对象，但要求表头必须是英文，跟JavaBean的属性要对应起来，可以通过在User类注释@Alias实现中文别名
        List<Normcorneal> list = reader.readAll(Normcorneal.class);
        for (Normcorneal normcorneal : list) {
            normcorneal.setUploaderId(uploaderId);
        }
//        List<User> list = reader.read(0, 1, User.class);
        System.out.println(list);
        return Result.success(normcornealService.saveBatch(list));
    }

    /**
     * 当前已有正常角膜数据导出为Excel
     * @param response
     * @throws Exception
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        //从数据库查出所有用户数据
        List<Normcorneal> list = normcornealService.list();

        //通过工具类创建writer写出到磁盘路径
        ExcelWriter writer = ExcelUtil.getWriter(true);
        //自定义标题别名


        //一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list,true);
        writer.addHeaderAlias("patientNum","病例号");
        writer.addHeaderAlias("name","姓名");
        writer.addHeaderAlias("gender","性别");
        writer.addHeaderAlias("eye","眼别");
        writer.addHeaderAlias("esm","等效球镜");
        writer.addHeaderAlias("sphericalMirror","球镜");
        writer.addHeaderAlias("cylinder","柱镜");
        writer.addHeaderAlias("axial","轴向");
        writer.addHeaderAlias("cornealThickness","角膜厚度");
        writer.addHeaderAlias("iop","IOP");
        writer.addHeaderAlias("biop","bIOP");

        //设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("正常角膜数据", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out,true); //将writer中的内容刷新到输出流中
        out.close();
        writer.close();
    }

    /**
     * 前台获得/查询所有正常病例信息接口
     * @param patientNum
     * @param name
     * @return
     */
    @GetMapping("/allpatients")
    public Result getAllPatients(@RequestParam(defaultValue = "") String patientNum,
                                 @RequestParam(defaultValue = "") String name,
                                 @RequestParam(defaultValue = "") String gender) {
        QueryWrapper<Normcorneal> queryWrapper = new QueryWrapper<>();
        queryWrapper = queryWrapper.select("DISTINCT patient_num,`name`,gender");
        if (!"".equals(patientNum)) {
            queryWrapper = queryWrapper.like("patient_num",patientNum);
        }
        if (!"".equals(name)) {
            queryWrapper = queryWrapper.like("name",name);
        }
        if (!"".equals(gender)) {
            queryWrapper = queryWrapper.like("gender",gender);
        }

        return Result.success(normcornealMapper.selectList(queryWrapper));
    }

//    @AuthAccess
//    @GetMapping("/findpatient/{name}")
//    public Result findByPatientName(@PathVariable String name) {
//        QueryWrapper<Normcorneal> queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("name",name);
//        return Result.success(normcornealMapper.selectList(queryWrapper));
//    }
}

