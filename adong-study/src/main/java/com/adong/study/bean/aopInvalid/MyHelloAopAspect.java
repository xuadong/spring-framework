package com.adong.study.bean.aopInvalid;

import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

@Aspect
@Component
public class MyHelloAopAspect {

	@Pointcut("execution(* com.adong.study.bean.aopInvalid.*.hello(..))")
	public void pointCut(){}

	@Before("pointCut()")
	public void before() {
		System.out.println("before hello");
	}

}
