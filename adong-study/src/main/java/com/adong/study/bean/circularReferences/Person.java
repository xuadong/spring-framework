package com.adong.study.bean.circularReferences;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class Person {
	public String name;
	@Autowired
	public Cat cat;
}
