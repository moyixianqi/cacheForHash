package com.pantech.hash_cache.controller;


import com.pantech.hash_cache.entity.User;
import com.pantech.hash_cache.service.UserService;
import com.pantech.hash_cache.util.Message;
import com.pantech.hash_cache.util.MessageBox;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * Description:
 * -------------------------
 * Created by ywq on 2019-10-20
 */

@RestController
@Api(tags = "用户")
@RequestMapping("/user")
public class UserController {

	private UserService userService;

	@Autowired
	public UserController(UserService userService) {
		this.userService = userService;
	}

	@ApiOperation("新增用户")
	@RequestMapping(value = "", method = RequestMethod.POST)
	public Message add(@RequestBody User user) {
		if (user == null) {
			return Message.fail("参数为空");
		}
		return MessageBox.send(userService.add(user));
	}

	@ApiOperation("更新用户")
	@RequestMapping(value = "", method = RequestMethod.PUT)
	public Message update(@RequestBody User user) {
		if (user == null) {
			return Message.fail("参数为空");
	}
		return MessageBox.send(userService.update(user));
	}

	@ApiOperation("删除用户")
	@RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
	public Message delete(@PathVariable String id) {
		if (StringUtils.isEmpty(id)) {
			return Message.fail("id为空");
		}
		userService.delete(id);
		return Message.ok();
	}

	@ApiOperation("查询所有用户")
	@RequestMapping(value = "/all", method = RequestMethod.GET)
	public Message queryAll() {
		return MessageBox.send(userService.queryAll());
	}

	@ApiOperation("根据id查询用户")
	@RequestMapping(value = "/id/{id}", method = RequestMethod.GET)
	public Message query(@PathVariable("id") String id) {
		if (StringUtils.isEmpty(id)) {
			return Message.fail("id 为空");
		}
		return MessageBox.send(userService.queryById(id));
	}
}
