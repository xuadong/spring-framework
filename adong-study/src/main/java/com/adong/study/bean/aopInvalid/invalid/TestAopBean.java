package com.adong.study.bean.aopInvalid.invalid;

import org.springframework.stereotype.Component;

@Component
public class TestAopBean {

	public void invokeHello() {
		hello();
	}

	public void hello() {
		System.out.println("hello hello hey");
	}
}
