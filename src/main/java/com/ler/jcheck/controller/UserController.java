package com.ler.jcheck.controller;

import com.alibaba.fastjson.JSONObject;
import com.ler.jcheck.annation.Check;
import com.ler.jcheck.config.HttpResult;
import com.ler.jcheck.domain.User;
import com.ler.jcheck.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import javax.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author lww
 * @date 2019-10-10 15:31
 */
@Api(value = "/user", description = "测试专用")
@RestController
@RequestMapping("/user")
public class UserController {

	@Resource
	private UserService userService;

	@ApiOperation("测试普通参数")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "name", value = "姓名"),
			@ApiImplicitParam(name = "age", value = "年龄"),
			@ApiImplicitParam(name = "phone", value = "手机号"),
			@ApiImplicitParam(name = "idCard", value = "身份证号"),
	})
	@GetMapping("/simple")
	@Check(ex = "name != null", msg = "姓名不能为空")
	@Check(ex = "age != null", msg = "年龄不能为空")
	@Check(ex = "age > 18", msg = "年龄要大于18岁")
	@Check(ex = "phone != null", msg = "手机号不能为空")
	@Check(ex = "phone =~ /^(1)[0-9]{10}$/", msg = "手机号格式错误")
	@Check(ex = "string.startsWith(phone,\"1\")", msg = "手机号要以1开头")
	@Check(ex = "idCard != null", msg = "身份证号不能为空")
	//不先判空 com.googlecode.aviator.exception.ExpressionRuntimeException
	@Check(ex = "idCard =~ /^[1-9]\\d{5}[1-9]\\d{3}((0[1-9])||(1[0-2]))((0[1-9])||(1\\d)||(2\\d)||(3[0-1]))\\d{3}([0-9]||X)$/", msg = "身份证号格式错误")
	//没有，不会抛出 NoSuchMethodException 或者 NullPointerException 异常
	@Check(ex = "gender == 1", msg = "性别")
	@Check(ex = "date =~ /^[1-9][0-9]{3}-((0)[1-9]|(1)[0-2])-((0)[1-9]|[1,2][0-9]|(3)[0,1])$/", msg = "日期格式错误")
	@Check(ex = "date > '2019-12-20 00:00:00:00'", msg = "日期要大于 2019-12-20")
	public HttpResult simple(String name, Integer age, String phone, String idCard, String date) {
		System.out.println("name = " + name);
		System.out.println("age = " + age);
		System.out.println("phone = " + phone);
		System.out.println("idCard = " + idCard);
		System.out.println("date = " + date);
		return HttpResult.success();
	}

	/*
		{
			"age": 0,
            "bornDate": "string",
            "idCard": "string",
            "name": "string",
            "phone": "string"
		}
	*/
	@ApiOperation("测试 @RequestBody")
	@PostMapping("/body")
	@Check(ex = "user.name != null", msg = "姓名不能为空")
	@Check(ex = "user.age != null", msg = "年龄不能为空")
	@Check(ex = "user.age > 18", msg = "年龄要大于18岁")
	@Check(ex = "user.phone =~ /^(1)[0-9]{10}$/", msg = "手机号格式错误")
	@Check(ex = "user.name != null && user.age != null", msg = "姓名和年龄不能为空")
	//先要检查日期格式，bornDate="string" 这种非正常数据，不会比较大小
	@Check(ex = "user.bornDate =~ /^[1-9][0-9]{3}-((0)[1-9]|(1)[0-2])-((0)[1-9]|[1,2][0-9]|(3)[0,1])$/", msg = "日期格式错误")
	@Check(ex = "user.bornDate > '2019-12-20'", msg = "日期要大于 2019-12-20")
	//@Check(ex = "user.gender == 1", msg = "性别")
	//Caused by: java.lang.NoSuchMethodException: Unknown property 'gender' on class 'class com.ler.jcheck.domain.User'
	public HttpResult body(@RequestBody User user) {
		String jsonString = JSONObject.toJSONString(user);
		System.out.println(jsonString);
		return HttpResult.success();
	}

	@ApiOperation("添加 在 Service 中校验")
	@PostMapping("/addUser")
	public HttpResult addUser(@RequestBody User user) {
		userService.addUser(user);
		return HttpResult.success();
	}

	@ApiOperation("删除 在 Service 中校验")
	@ApiImplicitParams({
			@ApiImplicitParam(name = "id", value = "id"),
	})
	@PostMapping("/delete")
	public HttpResult delete(Long id) {
		userService.deleteUser(id);
		return HttpResult.success();
	}
}

