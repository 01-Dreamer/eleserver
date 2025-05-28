package com.zxylearn.eleserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxylearn.eleserver.pojo.User;

public interface UserService extends IService<User> {
    boolean addUser(User user);
    boolean updateAvatar(Integer userId, String avatar);
    boolean existsById(Integer userId);
    String getAvatarByUserId(Integer userId);
}
