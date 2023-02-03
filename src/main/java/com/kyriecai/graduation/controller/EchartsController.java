package com.kyriecai.graduation.controller;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.date.Quarter;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kyriecai.graduation.common.Result;
import com.kyriecai.graduation.config.AuthAccess;
import com.kyriecai.graduation.entity.Bioparam;
import com.kyriecai.graduation.entity.Normcorneal;
import com.kyriecai.graduation.entity.User;
import com.kyriecai.graduation.service.IBioparamService;
import com.kyriecai.graduation.service.INormcornealService;
import com.kyriecai.graduation.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

@RestController
@RequestMapping("/echarts")
public class EchartsController {

    @Autowired
    private IUserService userService;

    @Resource
    private INormcornealService normcornealService;

    @Resource
    private IBioparamService bioparamService;

    @GetMapping("/example")
    public Result get() {
        HashMap<String, Object> map = new HashMap<>();
        map.put("x", CollUtil.newArrayList("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"));
        map.put("y", CollUtil.newArrayList("150", "230", "224", "218", "135", "147", "260"));
        return Result.success(map);
    }


    /**
     * 每季度用户注册量Echarts图示接口
     * @return
     */
    @GetMapping("/members")
    public Result members() {
        List<User> list = userService.list();
        int q1 = 0;
        int q2 = 0;
        int q3 = 0;
        int q4 = 0;
        for (User user : list) {
            Date createTime = user.getCreateTime();
            Quarter quarter = DateUtil.quarterEnum(createTime);
            switch (quarter) {
                case Q1: q1 += 1; break;
                case Q2: q2 += 1; break;
                case Q3: q3 += 1; break;
                case Q4: q4 += 1; break;
                default: break;
            }
        }
        return Result.success(CollUtil.newArrayList(q1,q2,q3,q4));
    }

    /**
     * 获取相应用户左右眼正常角膜数据用以展示
     * @param name
     * @return
     */
    @AuthAccess
    @GetMapping("/normcorneal/{name}")
    public Result getNormcornealByName(@PathVariable String name) {
        QueryWrapper<Normcorneal> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name",name);
        List<Normcorneal> list = normcornealService.list(queryWrapper);
        ArrayList<Double> sMirrors = CollUtil.newArrayList(list.get(1).getSphericalMirror(), list.get(0).getSphericalMirror());
        ArrayList<Double> cylinders = CollUtil.newArrayList(list.get(1).getCylinder(), list.get(0).getCylinder());
        ArrayList<Integer> axials = CollUtil.newArrayList(list.get(1).getAxial(), list.get(0).getAxial());
        ArrayList<Integer> cornealThicknesses = CollUtil.newArrayList(list.get(1).getCornealThickness(), list.get(0).getCornealThickness());
        ArrayList<Double> iops = CollUtil.newArrayList(list.get(1).getIop(), list.get(0).getIop());
        ArrayList<Double> biops = CollUtil.newArrayList(list.get(1).getBiop(), list.get(0).getBiop());

        return Result.success(CollUtil.newArrayList(sMirrors,cylinders,axials,cornealThicknesses,iops,biops));

    }

    /**
     * 前台获取生物力学参数数据用于可视化展示
     * @return
     */
    @AuthAccess
    @GetMapping("/bioparam")
    public Result getBioparam() {
        List<Bioparam> list = bioparamService.list();
        ArrayList<String> variables = new ArrayList<>();
        ArrayList<Double> aucs = new ArrayList<>();
        ArrayList<Double> standardErrors = new ArrayList<>();
        ArrayList<Double> asymptoticSignificances = new ArrayList<>();
        ArrayList<Double> lowerLimits = new ArrayList<>();
        ArrayList<Double> higherLimits = new ArrayList<>();



        for (Bioparam bioparam : list) {
            variables.add(bioparam.getVariable());
            aucs.add(bioparam.getAuc());
            standardErrors.add(bioparam.getStandardError());
            asymptoticSignificances.add(bioparam.getAsymptoticSignificance());
            lowerLimits.add(bioparam.getLowerLimit());
            higherLimits.add(bioparam.getHigherLimit());
        }

        return Result.success(CollUtil.newArrayList(variables,aucs,standardErrors,asymptoticSignificances,lowerLimits,higherLimits));
    }
}
