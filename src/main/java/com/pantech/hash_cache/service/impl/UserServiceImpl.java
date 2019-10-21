package com.pantech.hash_cache.service.impl;

import com.pantech.hash_cache.annotation.CacheEvictForHash;
import com.pantech.hash_cache.annotation.CacheForHash;
import com.pantech.hash_cache.annotation.CachePutForHash;
import com.pantech.hash_cache.entity.User;
import com.pantech.hash_cache.repository.UserRepository;
import com.pantech.hash_cache.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Description:
 * -------------------------
 * Created by ywq on 2019-10-20
 */
@Service
public class UserServiceImpl implements UserService {
    private UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * 新增用户
     *
     * @param user
     * @return
     */
    @Override
    @CachePutForHash(field = "#user.id", targetClass = User.class)
    public User add(User user) {
        return userRepository.save(user);
    }

    /**
     * 更新用户
     *
     * @param user
     * @return
     */
    @Override
    @CachePutForHash(field = "#user.id", targetClass = User.class)
    public User update(User user) {
        return userRepository.save(user);
    }

    /**
     * 查询所有用户
     */
    @Override
    @CacheForHash(targetClass = User.class)
    public List<User> queryAll() {
        return userRepository.findAll();
    }

    /**
     * 根据id删除用户
     *
     * @return
     */
    @Override
    @CacheEvictForHash(field = "#id", targetClass = User.class)
    public void delete(String id) {
        userRepository.deleteById(id);
    }

    /**
     * 根据id查询用户
     *
     * @return
     */
    @Override
    @CacheForHash(field = "#id", targetClass = User.class)
    public User queryById(String id) {
        Optional<User> userOptional = userRepository.findById(id);
        return userOptional.orElse(null);
    }

}
