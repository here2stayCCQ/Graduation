package com.kyriecai.graduation.controller;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.kyriecai.graduation.entity.Normcorneal;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.List;
import com.kyriecai.graduation.common.Result;

import com.kyriecai.graduation.service.IBioparamService;
import com.kyriecai.graduation.entity.Bioparam;

import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 *  前端控制器
 * </p>
 *
 * @author kyriecai
 * @since 2022-05-06
 */
@RestController
@RequestMapping("/bioparam")
public class BioparamController {

    @Resource
    private IBioparamService bioparamService;

    @PostMapping
    public Result save(@RequestBody Bioparam bioparam) {
        return Result.success(bioparamService.saveOrUpdate(bioparam));
    }

    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Integer id) {
        return Result.success(bioparamService.removeById(id));
    }

    @PostMapping("/del/batch")
    public Result deleteBatch(@RequestBody List<Integer> ids) {    //从前台接收选中的纯id数组转化为list
        return Result.success(bioparamService.removeBatchByIds(ids));
    }

    @GetMapping
    public Result findAll() {
        return Result.success(bioparamService.list());
    }

    @GetMapping("/{id}")
    public Result findOne(@PathVariable Integer id) {
        return Result.success(bioparamService.getById(id));
    }

    @GetMapping("/page")
    public Result findPage(@RequestParam Integer pageNum,
                           @RequestParam Integer pageSize,
                           @RequestParam(defaultValue = "") String variable) {
        QueryWrapper<Bioparam> queryWrapper = new QueryWrapper<>();
        queryWrapper.orderByDesc("id");
        if (!"".equals(variable)) {
            queryWrapper.like("variable",variable);
        }
        return Result.success(bioparamService.page(new Page<>(pageNum, pageSize), queryWrapper));
    }

    /**
     * 通过Excel导入生物力学参数数据
     * @param file
     * @throws Exception
     */
    @PostMapping("/import")
    public Result imp(MultipartFile file,@RequestParam Integer uploaderId) throws Exception{
        InputStream inputStream = file.getInputStream();
        ExcelReader reader = ExcelUtil.getReader(inputStream);
        //通过JavaBean的方式读取excel内的对象，但要求表头必须是英文，跟JavaBean的属性要对应起来，可以通过在User类注释@Alias实现中文别名
        List<Bioparam> list = reader.readAll(Bioparam.class);
        for (Bioparam bioparam : list) {
            bioparam.setUploaderId(uploaderId);
        }
//        List<User> list = reader.read(0, 1, User.class);
        System.out.println(list);
        return Result.success(bioparamService.saveBatch(list));
    }


    /**
     * 当前已有正常角膜数据导出为Excel
     * @param response
     * @throws Exception
     */
    @GetMapping("/export")
    public void export(HttpServletResponse response) throws Exception {
        //从数据库查出所有用户数据
        List<Bioparam> list = bioparamService.list();

        //通过工具类创建writer写出到磁盘路径
        ExcelWriter writer = ExcelUtil.getWriter(true);
        //自定义标题别名


        //一次性写出list内的对象到excel，使用默认样式，强制输出标题
        writer.write(list,true);
        writer.addHeaderAlias("variable","检验结果变量");
        writer.addHeaderAlias("indexMeaning","指标含义");
        writer.addHeaderAlias("auc","AUC");
        writer.addHeaderAlias("status","状态");
        writer.addHeaderAlias("standardError","标准错误a");
        writer.addHeaderAlias("asymptoticSignificance","渐进显著性b");
        writer.addHeaderAlias("lowerLimit","渐进95%置信区间下限");
        writer.addHeaderAlias("higherLimit","渐进95%置信区间上限");


        //设置浏览器响应的格式
        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet;charset=utf-8");
        String fileName = URLEncoder.encode("生物力学参数数据", "UTF-8");
        response.setHeader("Content-Disposition", "attachment;filename=" + fileName + ".xlsx");

        ServletOutputStream out = response.getOutputStream();
        writer.flush(out,true); //将writer中的内容刷新到输出流中
        out.close();
        writer.close();
    }

}

