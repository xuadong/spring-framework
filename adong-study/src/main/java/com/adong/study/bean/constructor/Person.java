package com.adong.study.bean.constructor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Person {
	public Cat cat;

	@Autowired
	public Person(Cat cat)
	{
		this.cat = cat;
	}
}
