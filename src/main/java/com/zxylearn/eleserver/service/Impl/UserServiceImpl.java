package com.zxylearn.eleserver.service.Impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxylearn.eleserver.mapper.UserMapper;
import com.zxylearn.eleserver.pojo.User;
import com.zxylearn.eleserver.service.UserService;
import org.springframework.stereotype.Service;


@Service
public class UserServiceImpl
        extends ServiceImpl<UserMapper, User>
        implements UserService {


    @Override
    public boolean addUser(User user) {
        if(user == null) {
            return false;
        }
        return baseMapper.insert(user) > 0;
    }

}
