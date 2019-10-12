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