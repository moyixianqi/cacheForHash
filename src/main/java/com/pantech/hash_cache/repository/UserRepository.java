package com.pantech.hash_cache.repository;

import com.pantech.hash_cache.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

/**
 * Description:
 * -------------------------
 * Created by ywq on 2019-10-20
 */

@Repository
public interface UserRepository  extends JpaRepository<User, String>, JpaSpecificationExecutor<User> {
}
