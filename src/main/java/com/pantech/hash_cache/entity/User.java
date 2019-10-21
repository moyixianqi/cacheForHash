package com.pantech.hash_cache.entity;

import com.pantech.hash_cache.annotation.CacheEntity;
import com.pantech.hash_cache.annotation.CacheField;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * Description:
 * -------------------------
 * Created by ywq on 2019-10-20
 */
@Getter
@Setter
@Entity
@CacheEntity(key = "USER")
public class User {
    @Id
    @GenericGenerator(name = "genericGenerator", strategy = "uuid")
    @GeneratedValue(generator = "genericGenerator")
    @CacheField
    private String id;

    private String username;

    private String password;

    private String name;

    public User() {
        super();
    }

    public User(String username, String password, String name) {
        this.username = username;
        this.password = password;
        this.name = name;
    }
}
