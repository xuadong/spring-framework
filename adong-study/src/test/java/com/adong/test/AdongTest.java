package com.adong.test;

import com.adong.study.bean.constructor.Person;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.junit.jupiter.api.Test;

public class AdongTest {

	/**
	 * 1. ioc容器的启动过程实际上就是 new()一个 ioc容器的过程，所以我们只需要找一个 new()为入口即可看到整个 ioc容器的启动过程
	 *    这里选择注解式的ioc容器，目前广泛应用的 spring-boot中就是使用这个容器
	 */
	@Test
	public void testCircularReferences() {
		/**
		 * 1.1 这里的 "com.adong.study.bean.circularReferences"包下有两个类：
		 *       · Cat类，里面需要注入 Person
		 *       · Person类，里面需要注入 Cat
		 *     所以其实现在还是一个循环依赖的场景，我们通过这个测试用例可以看到循环以来场景下、ioc容器是如何启动的、以及三级缓存是如何解决循环依赖的
		 *     现在，直接进到构造方法里面去，一起看一下 ioc容器的启动流程
		 */
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext("com.adong.study.bean.circularReferences");
		Object person = annotationConfigApplicationContext.getBean("person");
		Object cat = annotationConfigApplicationContext.getBean("cat");
		System.out.println("wait wait wait");
	}

	/**
	 * 2. 简单测试一下抽象类的注入
	 */
	@Test
	public void testAbstractBean() {
		/**
		 * 2.1 这里的 "com.adong.study.bean.abstra"下只有一个抽象类，并且添加了@Component注解
		 *     现在让我们在 sout的那一行加一个断点、开启 debug模式、然后看一下 annotationConfigApplicationContext.getBeanFactory()里面有没有 AbstractBean类型的 bean
		 */
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext("com.adong.study.bean.abstra");

		/**
		 * 2.2 很明显是没有的，在我们看源码的过程中也可以发现、在不少地方都会判断当前实例化的是否是抽象类、如果是抽象类那么是不能实例化的
		 *     但是 spring也不会报错、就是单纯跳过这个 bean的实例化过程
		 */
		System.out.println("wait wait wait");
	}

	/**
	 * 3. 我们接下来来测试一下构造器注入
	 *    背过八股的同学都知道、构造器注入是会出现循环依赖问题的，但是为什么这种场景下没能通过三级缓存解决循环依赖呢？
	 *      · 其实猜也能猜到，那就是这种场景下、根本没走到三级缓存那一步
	 *      · 我们通过这个例子看一下构造器注入时是通过哪个逻辑来实例化的
	 */
	@Test
	public void testConstructor() {
		/**
		 * 3.1 这里的 "com.adong.study.bean.constructor"下有两个类：
		 * 	     · 一个 Cat，里面依赖一个 Person，并且是通过构造器注入的 Person
		 * 	     · 一个 Person，里面啥都没有
		 * 	     · 这俩都加了@Component注解
		 * 	   想一下，之前我们第一个试验读源码可以认识到这种需要依赖注入的实例化逻辑大体应该是这样（注意哈，我们现在的场景很简单、没有循环依赖问题）：
		 * 	     · 先实例化 Cat、然后把 Cat放进三级缓存中去
		 * 	     · 在 Cat的实例化过程中发现需要 Person、然后去实例化 Person
		 * 	     · 等 Person实例化好了、再回到 Cat的实例化流程、然后 Cat也实例化好了
		 * 	   那么如果说构造器注入没有走到三级缓存、说明我们在先实例化 Cat的时候就没有放进三级缓存中去、而是走了别的逻辑
		 * 	   具体是不是这样呢？我们进入构造方法里面 debug一下
		 */
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext("com.adong.study.bean.constructor");

		System.out.println("wait wait wait");
	}

	/**
	 * 4. 接下来我们来试一下懒加载的 bean是什么时候实例化的
	 */
	@Test
	public void testLazy() {
		/**
		 * 4.1 这里的 "com.adong.study.bean.lazy"下只有一个类：
		 *     也就是一个 Person类，并且加了@Component和@Lazy，代表这是一个需要懒加载的 bean
		 */
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext("com.adong.study.bean.lazy");

		/**
		 * 4.2 我们在这一行打一个断点，看一下此时 annotationConfigApplicationContext.getBeanFactory()的 singletonObjects属性中有没有 Person类型的 bean
		 *     其实背过八股的朋友应该都知道，懒加载的 bean，在第一次调用 getBean()的时候才会实例化
		 *     所以我们在 "wait wait wait2"的 sout的位置也加一个断点，再看看 annotationConfigApplicationContext.getBeanFactory()的 singletonObjects属性中有没有 Person类型的 bean
		 */
		System.out.println("wait wait wait1");

		Object person = annotationConfigApplicationContext.getBean("person");

		/**
		 * 4.3 结果还是比较好猜的，上一个断点的位置中、ioc容器已经启动了但是没有 person
		 *     在经过调用 getBean()之后、ioc容器中就多个一个 person了
		 *     这也是为什么懒加载能够解决循环依赖问题的关键
		 */
		System.out.println("wait wait wait2");
	}

	/**
	 * 5. 思考一个问题，懒加载的 bean是在调用 getBean()的时候实例化的，那么我们什么时候调用了 getBean()呢？
	 *    这个测试用例我们一起来 debug一下
	 */
	@Test
	public void testLazyTime() {
		/**
		 * 5.1 这里的 "com.adong.study.bean.lazytime"下有两个类：
		 *       · 一个 Person类，并且加了@Component，代表这是一个需要懒加载的 bean
		 *       · 一个 Cat类，并且加了@Component，其内部依赖 Person是通过属性注入的、并且加了@Lazy
		 *     这里需要注意@Lazy的位置，直接在Person上加@Lazy是没法实现懒加载的：
		 *       · 在实例化 cat的时候依旧会实例化 person、这就和懒加载没关系了
		 *       · 这个 @Lazy需要加在属性上才行
		 */
		AnnotationConfigApplicationContext annotationConfigApplicationContext = new AnnotationConfigApplicationContext("com.adong.study.bean.lazytime");

		/**
		 * 5.2 我们在这个 "wait wait wait1"的位置打个断点，看一下当前 cat里面的 person是什么
		 */
		com.adong.study.bean.lazytime.Cat cat = (com.adong.study.bean.lazytime.Cat) annotationConfigApplicationContext.getBean("cat");
		System.out.println("wait wait wait1");

		com.adong.study.bean.lazytime.Person master = cat.master;
		System.out.println(master.name);
		System.out.println("wait wait wait2");
	}
}
