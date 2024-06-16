package com.adong.test;

import com.adong.study.bean.aop.TestAopBeanByClass;
import com.adong.study.bean.aop.TestAopBeanByInterface;
import com.adong.study.bean.aop.TestAopInterface;
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
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.adong.study.bean.circularReferences");
		System.out.println("wait wait wait");
	}

	/**
	 * 2. 简单测试一下抽象类的注入
	 */
	@Test
	public void testAbstractBean() {
		/**
		 * 2.1 这里的 "com.adong.study.bean.abstra"下只有一个抽象类，并且添加了@Component注解
		 *     现在让我们在 sout的那一行加一个断点、开启 debug模式、然后看一下 context.getBeanFactory()的 singletonObjects属性中有没有 AbstractBean类型的 bean
		 */
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.adong.study.bean.abstra");

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
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.adong.study.bean.constructor");

		System.out.println("wait wait wait");
	}

	/**
	 * 4. 接下来我们看一下 aop生成代理相关的内容，看 "com.adong.study.bean.aop"这个路径下的结构
	 *    首先需要引入 aop相关的依赖、并且创建一个配置类来开启 aop的能力（也就是我们的 TestAopConfiguration以及上面的 @EnableAspectJAutoProxy）
	 *      · 实际上这个 @EnableAspectJAutoProxy加在任意一个 bean上都是可以的（在 spring项目中通常会加在启动类上）
	 *      · 这种 @EnableXXX这一类的注解其实就是会向 ioc容器中提前注入一个后置处理器、然后通过这个后置处理器来实现一些逻辑
	 *    有一个 MyAopAspect的切面类、会对当前包路径下的所有 bean进行代理
	 *      · 代理的结果是当前包下的所有 bean的任意方法被调用之前会先打印一个 "before method aspect"
	 *    有一个 TestAopInterface的接口、里面有一个 say方法
	 *      · 有一个实现类叫 TestAopBeanByInterface，他重写了 say方法：会打印一句 "i am TestAopBeanByInterface"
	 *    还有一个 TestAopBeanByClass类、里面有一个 say方法：会打印一句 "i am TestAopBeanByClass"
	 *    接下来我们在 sout "wait wait wait"那一行打一个断点、看看 context.getBeanFactory()里面有什么
	 */
	@Test
	public void testAop() {
		/**
		 * 4.1 首先这里面有一个 bean名字叫 testAopBeanByClass、类型是 TestAopBeanByClass$$SpringCGLIB$$0
		 *       · 很明显这个 bean是经过 cglib代理的 TestAopBeanByClass类对应的实例
		 *     然后还有一个 bean名字叫 testAopBeanByInterface、类型是 $Proxy25
		 *       · 很明显这个 bean是经过 jdk代理的 TestAopBeanByInterface接口对应的实例
		 *     至此我们发现如果要生成的 bean实现了某个接口、那么就会用 jdk代理(不管是实现了一个接口还是多个接口)
		 *     其他情况下就会用 cglib进行代理
		 */
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.adong.study.bean.aop");

		System.out.println("wait wait wait");

		/**
		 * 4.2 然后我们尝试一下获取 TestAopBeanByClass类型的 bean、你会发现没问题，可以获取到
		 *     这个很好理解、因为 cglib是基于继承去实现的，
		 *       · 就是 cglib会生成一个需要被代理的类的子类、然后将这个子类注入 ioc容器
		 *       · 这个子类会重写父类中需要被代理的所有方法、也就是在重写的这个过程中完成需要代理的逻辑
		 *       · 这个重写的过程相当于将我们的代码复制一遍、然后在我们的代码前后去加上代理逻辑、之后再将新的子类编译成字节码文件
		 */
		TestAopBeanByClass beanByClass = context.getBean(TestAopBeanByClass.class);

		/**
		 * 4.3 然后我们尝试获取一下 TestAopInterface类型的 bean、也可以找到
		 *     这里找到的这个 bean就是经过 jdk代理的 TestAopBeanByInterface接口对应的实例
		 *     这个也好理解，因为 jdk是基于接口去实现的：
		 *       · jdk会实现需要被代理对象相同的接口、然后实现接口的所有方法
		 *       · 在实现接口的方法的过程中加上需要代理的逻辑、并且通过反射来调用原对象的方法
		 *       · 需要注意的是，jdk的代理是基于接口+反射实现的，而 cglib的代理是基于继承+字节码增强来实现的
		 */
		TestAopInterface interfaceBean = context.getBean(TestAopInterface.class);

		try {
			/**
			 * 4.4 然后我们尝试获取一下 TestAopBeanByInterface类型的 bean、你会发现找不到
			 *       · 很明显我这里都用 try catch包起来了、就是为了避免代码运行不起来
			 *     这就很奇怪了，因为我们明明在 TestAopBeanByInterface这个类上加了 @Component注解、怎么没有这个类型的 bean呢？
			 *       · 原因就出现在代理上，因为 TestAopBeanByInterface实现了一个接口、同时这个类又需要被代理
			 *       · 此时就会通过 jdk代理，经过 jdk代理之后、ioc容器内部是不会有原本的 bean的、只会有 jdk代理后的 bean
			 *       · 好巧不巧 jdk是基于接口进行代理的，所以 ioc容器里面只会有 TestAopInterface类型的 bean而不会有原本类型的 bean
			 */
			TestAopBeanByInterface beanByInterface = context.getBean(TestAopBeanByInterface.class);
		} catch (Exception e) {
			System.out.println("木有木有木有");
		}

		/**
		 * 4.5 最后，我们可以看一下在源码的什么地方会生成代理，有以下三个位置会判断是否需要生成代理：
		 *       · AbstractAutowireCapableBeanFactory#534
		 *       · AbstractAutowireCapableBeanFactory#686
		 *       · AbstractAutowireCapableBeanFactory#727
		 */
	}

	/**
	 * 5. 接下来我们来试一下懒加载的 bean是什么时候实例化的
	 */
	@Test
	public void testLazy() {
		/**
		 * 5.1 这里的 "com.adong.study.bean.lazy"下只有一个类：
		 *     也就是一个 Person类，并且加了@Component和@Lazy，代表这是一个需要懒加载的 bean
		 */
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.adong.study.bean.lazy");

		/**
		 * 5.2 我们在这一行打一个断点，看一下此时 context.getBeanFactory()的 singletonObjects属性中有没有 person的
		 *     其实背过八股的朋友应该都知道，懒加载的 bean，在第一次调用 getBean()的时候才会实例化
		 *     所以我们在 "wait wait wait2"的 sout的位置也加一个断点，再看看 context.getBeanFactory()的 singletonObjects属性中有没有 person
		 */
		System.out.println("wait wait wait1");

		Object person = context.getBean("person");

		/**
		 * 5.3 结果还是比较好猜的，上一个断点的位置中、ioc容器已经启动了但是没有 person
		 *     在经过调用 getBean()之后、ioc容器中就多个一个 person了
		 *     这也是为什么懒加载能够解决循环依赖问题的关键
		 *     其实不只是调用 getBean()方法、只要我们调用 getBean()或者说调用 person的任意方法时、都会实例化这个 bean
		 */
		System.out.println("wait wait wait2");
	}

	/**
	 * 6. 接下来我们来研究一下懒加载是怎么实现的
	 *    大家可以看一下这个博客，写的很好：https://juejin.cn/post/7288963211071684666?share_token=0bd09734-ae57-4564-ac45-f2bbd53417a9
	 */
	@Test
	public void testLazyImpl() {
		/**
		 * 6.1 "com.adong.study.bean.lazy"下有两个类：
		 *       · Cat类，这个类上加了@Component、里面有一个master属性，并且加了@Lazy+@Autowired
		 *       · Person类，这个类上加了@Component、里面啥也没有
		 *     然后我们在 sout "wait wait wait1"的地方加一个断点、debug一下
		 */
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.adong.study.bean.lazyImpl");

		/**
		 * 6.2 此时我们看 context.getBeanFactory()的 singletonObjects属性中是没有 person但是有 cat
		 *     我们打开 cat看一下、会发现 cat的 master属性是一个 Person的代理对象，然后你还会惊奇的发现、这个代理对象居然不在 ioc容器里面
		 *     其实到这里大概就能想明白懒加载的原理了：
		 *       · 首先，懒加载的 bean会有一个代理对象、并且这个代理对象还不会被注入到 ioc容器中
		 *       · 当我们调用被代理对象的任意方法时、会先执行实例化的过程、然后再调用被代理对象的方法
		 */
		System.out.println("wait wait wait1");

		/**
		 * 6.3 总结一下懒加载的实现过程，第一个需要实现的点：ioc容器启动的时候为什么懒加载的 bean没有被实例化？
		 *       · 这个问题其实很简单，在实例化的过程中有不少地方都会判断这个 bean是不是懒加载的、如果是那就会跳过
		 *       · 大家自己看代码的时候留意一下即可
		 *     第二个需要实现的点：如何在调用这个这个 bean的时候去执行实例化的逻辑？
		 *       · 这实际上就是一个代理的逻辑
		 *     最后还要说一下懒加载的一个失效场景：
		 *       · 就是如果 Person类上加了@Lazy、然后 Cat的 master属性上没加@Lazy
		 *       · 此时这个 Person类对应的 bean并不会被懒加载，因为依赖注入的逻辑是在 AbstractAutowireCapableBeanFactory#populateBean()中实现的
		 *       · 这个方法并不会管 Person类上有没有加@Lazy
		 */
	}
}
