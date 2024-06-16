package com.adong.study.bean.lazyImpl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

@Component
public class Cat {
	@Autowired
	@Lazy
	public Person master;
}
