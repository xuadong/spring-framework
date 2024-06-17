package com.adong.study.bean.aopInvalid;

import org.springframework.stereotype.Component;

@Component
public class TestAopBean {

	public void invokeHello() {
		hello();
	}

	public void hello() {
		System.out.println("hello hello hey");
	}

	public void invokeBye() {
		bye();
	}

	public void bye() {
		System.out.println("bye bye bye");
	}
}
