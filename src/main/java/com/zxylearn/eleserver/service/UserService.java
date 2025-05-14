package com.zxylearn.eleserver.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zxylearn.eleserver.pojo.User;

public interface UserService extends IService<User> {

    public boolean addUser(User user);

}
