package com.adong.study.bean.aopInvalid.autowiredSelf;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TestAopBean {
	@Autowired
	public TestAopBean self;

	public void invokeHello() {
		self.hello();
	}

	public void hello() {
		System.out.println("hello hello hey");
	}
}
