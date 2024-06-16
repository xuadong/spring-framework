package com.adong.study.bean.aop;

import org.springframework.stereotype.Component;

@Component
public class TestAopBeanByInterface implements TestAopInterface {
	public void say() {
		System.out.println("i am TestAopBeanByInterface");
	}
}
