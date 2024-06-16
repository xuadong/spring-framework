package com.adong.study.bean.aop;

import org.springframework.stereotype.Component;

@Component
public class TestAopBeanByClass {
	public void say() {
		System.out.println("i am TestAopBeanByClass");
	}
}
