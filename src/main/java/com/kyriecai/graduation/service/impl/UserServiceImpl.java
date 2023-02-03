package com.kyriecai.graduation.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.log.Log;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.kyriecai.graduation.common.Constants;
import com.kyriecai.graduation.controller.dto.UserDTO;
import com.kyriecai.graduation.controller.dto.UserPasswordDTO;
import com.kyriecai.graduation.entity.Menu;
import com.kyriecai.graduation.entity.User;
import com.kyriecai.graduation.exception.ServiceException;
import com.kyriecai.graduation.mapper.RoleMapper;
import com.kyriecai.graduation.mapper.RoleMenuMapper;
import com.kyriecai.graduation.mapper.UserMapper;
import com.kyriecai.graduation.service.IMenuService;
import com.kyriecai.graduation.service.IUserService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.kyriecai.graduation.utils.TokenUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author kyriecai
 * @since 2022-04-17
 */
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements IUserService {

    private static final Log LOG = Log.get();

    @Resource
    private RoleMapper roleMapper;

    @Resource
    private RoleMenuMapper roleMenuMapper;

    @Resource
    private IMenuService menuService;

    @Resource
    private UserMapper userMapper;

    /**
     * 登录业务方法
     * @param userDto
     * @return
     */
    @Override
    public UserDTO login(UserDTO userDto) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",userDto.getUsername());
        queryWrapper.eq("password",userDto.getPassword());
        User one;
        try {
            one = getOne(queryWrapper);    //从数据库查询用户信息

        } catch (Exception e) {
            LOG.error(e);
            throw new ServiceException(Constants.CODE_500,"系统错误");
        }
        if (one != null){
            BeanUtils.copyProperties(one,userDto);  //把查询结果user对象中的对应属性拷贝到userDto对象
            //设置token
            String token = TokenUtils.generateToken(one.getId().toString(), one.getPassword());
            userDto.setToken(token);

            String role = one.getRole();    //ROLE_ADMIN
            //设置用户的菜单列表
            List<Menu> roleMenus = getRoleMenus(role);

            userDto.setMenus(roleMenus);
            return userDto;
        } else {
            throw new ServiceException(Constants.CODE_600,"用户名或密码错误");
        }
    }

    @Override
    public User register(UserDTO userDto) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("username",userDto.getUsername());
        User one =getOne(queryWrapper); //从数据库中查询此用户名是否已经存
        if (one == null){
            one = new User();
            BeanUtils.copyProperties(userDto,one);
            save(one);  //把copy完的用户对象存储到数据库
        } else {
            throw new ServiceException(Constants.CODE_600,"用户名已存在");
        }
        return one;
    }

    @Override
    public void updatePassword(UserPasswordDTO userPasswordDTO) {
        int update = userMapper.updatePassword(userPasswordDTO);
        if (update < 1) {
            throw new ServiceException(Constants.CODE_600, "密码错误");
        }
    }

    /**
     * 获取当前角色的菜单列表
     * @param roleFlag
     * @return
     */
    private List<Menu> getRoleMenus(String roleFlag) {
        Integer roleId = roleMapper.selectByFlag(roleFlag);

        //当前用户角色的所有菜单id集合
        List<Integer> menuIds = roleMenuMapper.selectByRoleId(roleId);

        //查出系统所有的菜单
        List<Menu> menus = menuService.findMenus("");
        //new一个最后筛选完成之后的登录用户角色对用的菜单list
        List<Menu> roleMenus = new ArrayList<>();
        //筛选当前用户角色的菜单
        for (Menu menu : menus) {
            if (menuIds.contains(menu.getId())) {
                if (!menu.getChildren().isEmpty()){
                    List<Menu> children = menu.getChildren();
                    //removeIf() 移除children子菜单里面不在menuIds集合中（即用户角色未授权）的menu元素
                    children.removeIf(child -> !menuIds.contains(child.getId()));

                    menu.setChildren(children);
                }
                roleMenus.add(menu);
            }
        }
        return roleMenus;
    }
}
