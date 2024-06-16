package com.adong.study.bean.aop;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MyAopAspect {

	@Pointcut("execution(* com.adong.study.bean.aop.*.*(..))")
	public void pointCut(){}

	@Before("pointCut()")
	public void before() {
		System.out.println("before method aspect");
	}

}
