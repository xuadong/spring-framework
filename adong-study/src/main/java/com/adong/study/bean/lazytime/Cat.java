package com.adong.study.bean.lazytime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class Cat {
	@Lazy
	@Autowired
	public Person master;
}
