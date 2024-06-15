package com.adong.study;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class Main {
	public static void main(String[] args) {
		/**
		 * ioc容器的启动过程实际上就是 new()一个 ioc容器的过程，所以我们只需要找一个 new()为入口即可看到整个 ioc容器的启动过程
		 * 这里选择注解式的ioc容器，目前广泛应用的 spring-boot中就是使用这个容器
		 * 现在，直接进到构造方法里面去
		 */
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext("com.adong.study.bean");
		Object person = annotationConfigApplicationContext.getBean("person");
		Object cat = annotationConfigApplicationContext.getBean("cat");
		System.out.println("wait wait wait");
	}
}