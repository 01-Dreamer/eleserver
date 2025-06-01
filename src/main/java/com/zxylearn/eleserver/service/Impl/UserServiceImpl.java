package com.zxylearn.eleserver.service.Impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zxylearn.eleserver.mapper.UserMapper;
import com.zxylearn.eleserver.pojo.entity.User;
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

    @Override
    public boolean updateAvatar(Integer userId, String avatar) {
        if(userId == null) {
            return false;
        }
        UpdateWrapper<User> updateWrapper = new UpdateWrapper<>();
        updateWrapper.eq("userId", userId);
        updateWrapper.set("avatar", avatar);

        return baseMapper.update(null, updateWrapper) > 0;
    }

    @Override
    public String getAvatarByUserId(Integer userId) {
        if (userId == null) {
            return null;
        }
        User user = baseMapper.selectById(userId);
        return (user != null) ? user.getAvatar() : null;
    }
}
