package com.pantech.hash_cache.entity;

import com.pantech.hash_cache.annotation.CacheEntity;
import com.pantech.hash_cache.annotation.CacheField;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotEmpty;
import java.util.Date;

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

    @NotEmpty(message = "账号不能为空")
    private String username;

    @NotEmpty(message = "密码不能为空")
    private String password;

    @NotEmpty(message = "姓名不能为空")
    private String name;


    private Date time;

    public User() {
        super();
    }

    public User(String username, String password, String name) {
        this.username = username;
        this.password = password;
        this.name = name;
    }
}
