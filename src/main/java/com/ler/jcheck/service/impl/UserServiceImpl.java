package com.ler.jcheck.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.ler.jcheck.annation.Check;
import com.ler.jcheck.domain.User;
import com.ler.jcheck.service.UserService;
import org.springframework.stereotype.Service;

/**
 * @author lww
 * @date 2019-10-10 15:33
 */
@Service
public class UserServiceImpl implements UserService {

	@Override
	@Check(ex = "user.name != null", msg = "姓名不能为空")
	public void addUser(User user) {
		System.out.println(JSONObject.toJSONString(user));
	}

	@Override
	@Check(ex = "id != null", msg = "id不能为空！")
	public void deleteUser(Long id) {
		System.out.println("id = " + id);
	}
}
