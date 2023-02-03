package com.kyriecai.graduation.controller.dto;

import com.kyriecai.graduation.entity.Menu;
import lombok.Data;

import java.util.List;

/**
 * 此dto接收前端登录请求的参数
 */
@Data
public class UserDTO {
    private Integer id;
    private String username;
    private String password;
    public String nickname;
    public String avatarUrl;
    private String token;
    private String role;
    private List<Menu> menus;
}
