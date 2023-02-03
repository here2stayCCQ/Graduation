package com.kyriecai.graduation.service;

import com.kyriecai.graduation.entity.Menu;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author kyriecai
 * @since 2022-04-30
 */
public interface IMenuService extends IService<Menu> {

    List<Menu> findMenus(String name);
}
