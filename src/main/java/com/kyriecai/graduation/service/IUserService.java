package com.kyriecai.graduation.service;

import com.kyriecai.graduation.controller.dto.UserDTO;
import com.kyriecai.graduation.controller.dto.UserPasswordDTO;
import com.kyriecai.graduation.entity.User;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author kyriecai
 * @since 2022-04-17
 */
public interface IUserService extends IService<User> {

    UserDTO login(UserDTO userDto);

    User register(UserDTO userDto);

    void updatePassword(UserPasswordDTO userPasswordDTO);
}
