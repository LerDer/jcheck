# AOP + Aviator 实现参数校验

在开发过程中，始终避免不了的是校验参数，参数的校验和业务代码耦合在一起，代码变得越来越臃肿，影响后期的维护，代码也不够优美。

> Aviator 是谷歌的`表达式求值引擎`。使用`Aviator`主要是来校验参数。它支持大部分运算操作符，包括算术操作符、关系运算符、逻辑操作符、正则匹配操作符(=~)、三元表达式?:，并且支持操作符的优先级和括号强制优先级。

由于在之前的项目中有用过Aviator，并且我习惯用`Assert断言`来进行参数校验。因为`Assert断言`抛出的异常是`IllegalArgumentException`，可能会抛出对用户不友好的异常。所以才想开发一个参数校验的东西。


# 依赖

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
	xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
	<modelVersion>4.0.0</modelVersion>
	<parent>
		<groupId>org.springframework.boot</groupId>
		<artifactId>spring-boot-starter-parent</artifactId>
		<version>2.1.9.RELEASE</version>
		<relativePath/> <!-- lookup parent from repository -->
	</parent>
	<groupId>com.ler</groupId>
	<artifactId>jcheck</artifactId>
	<version>1.0.0-SNAPSHOT</version>
	<name>jcheck</name>
	<description>Demo project for Spring Boot</description>

	<properties>
		<java.version>1.8</java.version>
	</properties>

	<dependencies>
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-web</artifactId>
		</dependency>

		<dependency>
			<groupId>org.projectlombok</groupId>
			<artifactId>lombok</artifactId>
			<optional>true</optional>
		</dependency>

		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-test</artifactId>
			<scope>test</scope>
		</dependency>

		<!--AOP依赖-->
		<dependency>
			<groupId>org.springframework.boot</groupId>
			<artifactId>spring-boot-starter-aop</artifactId>
		</dependency>

		<!--Aviator依赖-->
		<dependency>
			<groupId>com.googlecode.aviator</groupId>
			<artifactId>aviator</artifactId>
			<version>3.3.0</version>
		</dependency>

		<dependency>
			<groupId>com.alibaba</groupId>
			<artifactId>fastjson</artifactId>
			<version>1.2.56</version>
		</dependency>

		<dependency>
			<groupId>org.apache.commons</groupId>
			<artifactId>commons-lang3</artifactId>
			<version>3.8.1</version>
		</dependency>

		<!--swagger-->
		<dependency>
			<groupId>io.springfox</groupId>
			<artifactId>springfox-swagger-ui</artifactId>
			<version>2.9.2</version>
		</dependency>

		<dependency>
			<groupId>io.springfox</groupId>
			<artifactId>springfox-swagger2</artifactId>
			<version>2.9.2</version>
		</dependency>
	</dependencies>

	<build>
		<plugins>
			<plugin>
				<groupId>org.springframework.boot</groupId>
				<artifactId>spring-boot-maven-plugin</artifactId>
			</plugin>
		</plugins>
	</build>

</project>
```

首先想到的是注解，可是因为一般会有多个参数校验，所以需要在同一个方法上使用多个注解。
**但是在Java8之前，同一个注解是不能在同一个位置上重复使用的。**

虽然可以重复使用注解，其实这也是一个语法糖，多个注解在编译后其实还是要用一个容器包裹起来。

下面是注解：
```java
package com.ler.jcheck.annation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lww
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
//这个注解就是可以让一个注解同一个方法上标注多次
@Repeatable(CheckContainer.class)
public @interface Check {

	String ex() default "";

	String msg() default "";

}
```

- `ex`是需要校验的表达式，可以使用正则表达式。key是形参的名字，JOSN对象的话，key是形参名字.属性，具体可以看下面例子。
- `msg`是提示的错误信息，需要配合**全局异常拦截器**使用。
- 参数校验的顺序，是注解的顺序。

```java
package com.ler.jcheck.annation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author lww
 */
@Target({ElementType.FIELD, ElementType.METHOD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
public @interface CheckContainer {

	Check[] value();
}
```

这个是容器注解，当使用多个注解时，在编译后会使用这个注解把多个相同的注解包裹起来。
所以AOP切面，应该要监视 `Check` 和 `CheckContainer`。


# 核心类AopConfig

```java
package com.ler.jcheck.config;

import com.googlecode.aviator.AviatorEvaluator;
import com.ler.jcheck.annation.Check;
import com.ler.jcheck.annation.CheckContainer;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.LocalVariableTableParameterNameDiscoverer;
import org.springframework.util.StringUtils;

/**
 * @author lww
 * @date 2019-09-03 20:35
 */
@Aspect
@Configuration
public class AopConfig {

	/**
	 * 切面，监视多个注解，因为一个注解的时候是Check 多个注解编译后是CheckContainer
	 */
	@Pointcut("@annotation(com.ler.jcheck.annation.CheckContainer) || @annotation(com.ler.jcheck.annation.Check)")
	public void pointcut() {
	}

	@Before("pointcut()")
	public Object before(JoinPoint point) {
		//获取参数
		Object[] args = point.getArgs();
		//用于获取参数名字
		Method method = ((MethodSignature) point.getSignature()).getMethod();
		LocalVariableTableParameterNameDiscoverer u = new LocalVariableTableParameterNameDiscoverer();
		String[] paramNames = u.getParameterNames(method);

		CheckContainer checkContainer = method.getDeclaredAnnotation(CheckContainer.class);
		List<Check> value = new ArrayList<>();

		if (checkContainer != null) {
			value.addAll(Arrays.asList(checkContainer.value()));
		} else {
			Check check = method.getDeclaredAnnotation(Check.class);
			value.add(check);
		}
		for (int i = 0; i < value.size(); i++) {
			Check check = value.get(i);
			String ex = check.ex();
			//规则引擎中null用nil表示
			ex = ex.replaceAll("null", "nil");
			String msg = check.msg();
			if (StringUtils.isEmpty(msg)) {
				msg = "服务器异常...";
			}

			Map<String, Object> map = new HashMap<>(16);
			for (int j = 0; j < paramNames.length; j++) {
				//防止索引越界
				if (j > args.length) {
					continue;
				}
				map.put(paramNames[j], args[j]);
			}
			Boolean result = (Boolean) AviatorEvaluator.execute(ex, map);
			if (!result) {
				throw new UserFriendlyException(msg);
			}
		}
		return null;
	}
}
```


注释说的很清楚了。下面来看一下具体使用。


# 在Controller中的使用

## 普通参数

```java
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
```


如果要校验参数，应该要先进行非空判断，如果不校验，普通参数不会报错，如 `age > 18`。但是如果是正则表达式，则会抛出`ExpressionRuntimeException`。

在校验日期时，如`date > '2019-12-20 00:00:00:00`，应该首先校验格式，因为如果参数格式不能与日期比较时，Aviator是不会比较的。因此不会进行校验。

如果校验的是没有的参数，结果是false，会直接抛出注解中的 `msg` 的。



## @RequestBody参数

```java
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
```

参数是以JSON的形式传过来的，ex表达式中的key为形参.属性名。

什么都不传是参数错误，如果要传空，是传一个`{}`，校验顺序是注解的顺序。基本和上面的普通参数相同，有一点不一样的是，如果ex里是没有的属性，会抛出`java.lang.NoSuchMethodException`

## 在Service中使用

参数校验是使用AOP切面，监视 `Check` 和 `CheckContainer`这两个注解，所以只要是Spring代理的类都可以使用该注解来完成参数校验。

代码如下：
### Controller
```java
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
```

### Service
```java
package com.ler.jcheck.service;

import com.ler.jcheck.domain.User;

/**
 * @author lww
 */
public interface UserService {

	void addUser(User user);

	void deleteUser(Long id);
}
```

### ServiceImpl
```java
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
```

在Service中使用，其实在和Controller使用是一样的。

项目代码


还可以再进一步，把这个项目作为一个Starter，在开发时直接引入依赖，就可以使用了。
可以看一下我的博客
<a href="https://www.yunask.cn/post/84fbeef6.html" target="_blank">JCheck参数校验框架之创建自己的SpringBoot-Starter</a>
这里把该项目封装成了一个 `SpringBoot-Starter`，又集成了Swagger配置，运行环境配置，全局异常拦截器，跨域配置等。博客最后有项目的Git地址。
