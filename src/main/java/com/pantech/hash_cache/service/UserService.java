package com.pantech.hash_cache.service;

import com.pantech.hash_cache.entity.User;

import java.util.List;

/**
 * Description:
 * -------------------------
 * Created by ywq on 2019-10-20
 */
public interface UserService {
    User add(User user);

    User update(User user);

    void delete(String id);

    User queryById(String id);

    List<User> queryAll();

}
