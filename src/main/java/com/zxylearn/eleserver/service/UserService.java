package com.zxylearn.eleserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxylearn.eleserver.pojo.entity.User;

public interface UserService extends IService<User> {
    boolean addUser(User user);
    boolean updateAvatar(Integer userId, String avatar);
    String getAvatarByUserId(Integer userId);
}
