package com.adong.test;

import org.springframework.cglib.core.DebuggingClassWriter;
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
	 *      · 实际上这个@EnableAspectJAutoProxy加在任意一个 bean上都是可以的（在 spring项目中通常会加在启动类上）
	 *      · 这种 @EnableXXX这一类的注解其实就是会向 ioc容器中提前注入一个后置处理器、然后通过这个后置处理器来实现一些逻辑
	 *    有一个 MyAopAspect的切面类、会对当前包路径下的所有 bean进行代理
	 *      · 代理的结果是当前包下的所有 bean的任意方法被调用之前会先打印一个 "before method aspect"
	 *    有一个 TestAopInterface的接口、里面有一个 say方法
	 *      · 有一个实现类叫 TestAopBeanByInterface，他重写了 say方法：会打印一句 "i am TestAopBeanByInterface"
	 *    还有一个 TestAopBeanByClass类、里面有一个 say方法：会打印一句 "i am TestAopBeanByClass"
	 *    接下来我们在 sout "wait wait wait"那一行打一个断点、看看 context.getBeanFactory()里面有什么
	 *    特别说一下，当前我们运行完了测试用例以后会看到控制台会打印很多东西出来、这些其实都是 spring自己帮我们打印的
	 *      · 大家可以忽略掉 >Task开头的这种日志、找到我们自己 sout的内容就可以了
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
		com.adong.study.bean.aop.TestAopBeanByClass beanByClass = context.getBean(com.adong.study.bean.aop.TestAopBeanByClass.class);

		/**
		 * 4.3 然后我们尝试获取一下 TestAopInterface类型的 bean、也可以找到
		 *     这里找到的这个 bean就是经过 jdk代理的 TestAopBeanByInterface接口对应的实例
		 *     这个也好理解，因为 jdk是基于接口去实现的：
		 *       · jdk会实现需要被代理对象相同的接口、然后实现接口的所有方法
		 *       · 在实现接口的方法的过程中加上需要代理的逻辑、并且通过反射来调用原对象的方法
		 *       · 需要注意的是，jdk的代理是基于接口+反射实现的，而 cglib的代理是基于继承+字节码增强来实现的
		 */
		com.adong.study.bean.aop.TestAopInterface interfaceBean = context.getBean(com.adong.study.bean.aop.TestAopInterface.class);

		try {
			/**
			 * 4.4 然后我们尝试获取一下 TestAopBeanByInterface类型的 bean、你会发现找不到
			 *       · 很明显我这里都用 try catch包起来了、就是为了避免代码运行不起来
			 *     这就很奇怪了，因为我们明明在 TestAopBeanByInterface这个类上加了 @Component注解、怎么没有这个类型的 bean呢？
			 *       · 原因就出现在代理上，因为 TestAopBeanByInterface实现了一个接口、同时这个类又需要被代理
			 *       · 此时就会通过 jdk代理，经过 jdk代理之后、ioc容器内部是不会有原本的 bean的、只会有 jdk代理后的 bean
			 *       · 好巧不巧 jdk是基于接口进行代理的，所以 ioc容器里面只会有 TestAopInterface类型的 bean而不会有原本类型的 bean
			 */
			com.adong.study.bean.aop.TestAopBeanByInterface beanByInterface = context.getBean(com.adong.study.bean.aop.TestAopBeanByInterface.class);
		} catch (Exception e) {
			System.out.println("木有木有木有");
		}

		/**
		 * 4.5 最后，我们可以看一下在源码的什么地方会生成代理，有以下三个位置会判断是否需要生成代理：
		 *       · AbstractAutowireCapableBeanFactory#534
		 *       · AbstractAutowireCapableBeanFactory#686
		 *       · AbstractAutowireCapableBeanFactory#727
		 *     当需要回答 aop是怎么实现的时候可以考虑这么说：
		 *       · 简单来说，aop是通过代理的方式来实现的、即通过对原对象进行加强后执行代理的增强逻辑，接下来具体说说如何实现
		 *       · 首先要使用 aop需要引入一个@EnableAspectJAutoProxy注解
		 *       · 引入这个注解以后、在 ioc容器的启动过程中 spring会向 ioc容器里面注入一个特殊类型的后置处理器
		 *       · 这个后置处理器主要有两个功能：
		 *         · 第一个是在 ioc容器启动过程中、在所有 bean实例化之前先将所有的切面添加到一个集合中
		 *         · 第二个作用就是在每个 bean的实例化过程中去判断当前 bean有没有与之对应的切面、如果有的话就为其创建代理
		 *       · 生成代理有两种方式：
		 *         · 分别是 jdk代理和 cglib代理，我们可以在@EnableAspectJAutoProxy的 proxyTargetClass属性来指定使用哪种代理
		 *           · 如果没有指定代理的话、spring也有一个默认的代理策略
		 *         · 第一种是 jdk代理，在 spring默认的代理策略中、当前正在实例化的 bean如果实现了某个接口、就会使用这种代理
		 *           · jdk代理会实现接口中需要被代理的方法，进行逻辑加强、之后通过反射来调用原有方法来实现 aop
		 *         · 第二种是 cglib代理，在 spring默认的代理策略中、除了刚才说的实现了接口的情况、其他时候都会使用这种代理
		 *           · cglib代理会生成当前 bean的一个子类并且重写这个 bean中需要被代理的方法、进行逻辑加强后生成一个新的字节码文件
		 *           · 之后调用原方法时直接调用代理中的增强方法来实现 aop
		 */
	}

	/**
	 * 5 接下来我们测试一下调用内部方法时注解失效的问题（如果不知道注解失效是什么东西，看完这个用例大概就知道了）
	 *   "com.adong.study.bean.aopInvalid.invalid"包下面有三个东西：
	 *     · MyHelloAopAspect是一个切面，他主要是给当前 com.adong.study.bean.aopInvalid.invalid.*.hello(..))做切面
	 *     	 · 也就是 com.adong.study.bean.aopInvalid.invalid路径下的任意类的 hello方法都会被切到
	 *     	 · 切面的逻辑也就是在 hello()执行前、先 System.out.println("before hello")
	 *     · TestAopBean是一个普通的 bean，里面有两个方法：
	 *   	 · 一个是 hello()，这个方法打印一句 "hello hello hey"，同时这个方法会被上面的 MyHelloAopAspect切面增强
	 *     	 · 一个是 invokeHello()，这个方法的作用就是调用 hello()
	 *     · TestAopConfiguration就是用来引入 aop切面的
	 *   接下来我们在 sout "wait wait wait"的地方打一个断点、看此时控制台会打印什么东西
	 */
	@Test
	public void testAopInvalid() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.adong.study.bean.aopInvalid.invalid");

		com.adong.study.bean.aopInvalid.invalid.TestAopBean bean = context.getBean(com.adong.study.bean.aopInvalid.invalid.TestAopBean.class);

		/**
		 * 5.1 这里打印了 "hello hello hey"
		 *     但是有点奇怪啊？我们不是有个切面逻辑吗？为什么没能打印 "before hello"呢？
		 *       · 这就是所谓的内部方法注解失效问题
		 *       · 也就是 A方法内部调用了被切面增强的 B方法、此时调用 A方法时、B方法的切面逻辑是不生效的
		 *     你可能会觉得是我的切面写的有问题，所以我在下面又调用了 bean.hello()，一起看看结果
		 */
		bean.invokeHello();

		System.out.println("wait wait wait");

		/**
		 * 5.2 这里的切面逻辑正常执行了，先打印了 "before hello"然后打印了 "hello hello hey"
		 */
		bean.hello();

		/**
		 * 5.3 那么我们此时来想一下为什么会发生注解失效
		 *     首先我们知道 aop是基于代理去实现的，那么我们的 hello()既然被切面增强了、实际上就会生成代理类并且重写这个 hello()方法
		 *       · 这里使用的是 cglib代理、会生成一个子类（假设叫 SonOfTestAopBean）、然后 SonOfTestAopBean会重写这个 hello()方法
		 *       · 但是注意，SonOfTestAopBean里面并不会重写 invokerHello方法、因为他没有被切面切到、所以不会被代理
		 *       · 此时 ioc容器中存储的是 SonOfTestAopBean对应的实例 bean
		 *       · 此时我们调用 bean.invokerHello()方法、但是 SonOfTestAopBean里面没有这个方法、他就会去执行父类的 invokerHello()方法
		 *       · 那么在父类的 invokerHello()方法会调用 hello()方法、既然都在父类里面了、肯定调用的就是父类的 hello()方法
		 *       · 父类的 hello()是没有被增强的、所以自然我们的切面注解就失效了
		 *     现在大概明白了，注解失效的原因是 hello()被代理了但是 invokeHello()没被代理
		 *     那么新的问题来了，如果 invokeHello()也被代理、但是 invokeHello()和 hello()的是不同的 aop切面呢？
		 *       · 比如 invokeHello()是被切面1增强、但是 hello()被切面2增强
		 *       · 此时这两个方法都会被代理、而代理肯定是生成在同一个代理子类里面、这种时候这个子类的内部的增强方法之间互相调用还会不会注解失效呢？
		 */
	}

	/**
	 * 6. 接下来我们来测试一下，如果 invokeHello和 hello都被 aop增强了、还会不会注解失效
	 *    "com.adong.study.bean.aopInvalid.multiAop"下的东西也很简单，大概说一下：
	 *      · MyHelloAopAspect用来增强 hello方法，在 hello()执行前打印 "before hello"
	 *      · MyInvokeAopAspect用来增强 invokeHello方法、在 invokeHello()执行前打印 "before invoke"
	 *      · TestAopBean就是被 aop的类、TestAopConfiguration就是用来引入 aop切面的
	 *    现在我们在 sout "wait wait wait"的地方打个断点，看一下
	 */
	@Test
	public void testAopInvalidMultiAop() {
		/**
		 * 6.1 我这里设置了一下文件路径，这个就是 cglib生成的代理类的字节码文件，一会我们会用到这个东西
		 *     指定 CGLIB 将动态生成的代理类保存至指定的磁盘路径下：
		 *       · 等这个测试方法执行完、在 adong-study路径下就会有一个 cglib的文件夹
		 *       · 在这个文件夹里面有两个代理类 TestAopBean$$SpringCGLIB$$0和 TestAopConfiguration$$SpringCGLIB$$0
		 *       · 我们只关心 TestAopBean$$SpringCGLIB$$0，当然、这个代码直接看是看不懂的，我们现在先运行测试用例看一下运行结果
		 */
		System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "cglib");

		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.adong.study.bean.aopInvalid.multiAop");

		com.adong.study.bean.aopInvalid.multiAop.TestAopBean bean = context.getBean(com.adong.study.bean.aopInvalid.multiAop.TestAopBean.class);

		/**
		 * 6.2 代码运行到这里的时候打印了 "before invoke"和 "hello hello hey"
		 *     很明显，这里的 invokeHello()方法中调用 hello()时调用的还是没被增强的 hello()
		 *     因为被增强的 hello()方法会在运行前打印一个 "before hello"出来
		 */
		bean.invokeHello();
		System.out.println("wait wait wait");

		/**
		 * 6.3 以免是我 aop写的有问题、这里我手动调一下 hello()、可以看一下控制台
		 *     会打印 before hello"和 "hello hello hey"
		 */
		bean.hello();

		/**
		 * 6.4 具体这个问题产生的原因、大家可以去 cglib生成的那个 TestAopBean$$SpringCGLIB$$0文件看一下
		 *     找到里面的 invokeHello()方法、大概在 102行
		 *       · 注意是 invokeHello()方法、不是 CGLIB$invokeHello$1()方法
		 *     这里我们可以看到 110～114行、有一个 if逻辑
		 *       · 其中 112行的会调用一个 var10000.intercept()方法
		 *       · 这个方法的第四个参数是 CGLIB$invokeHello$1$Proxy、这个东西你可能不认识、但是我们可以看一下这个东西的类型
		 *       · 这是一个 MethodProxy，然后我们往上看、第 40行还有一个 MethodProxy CGLIB$hello$0$Proxy
		 *     这里我们大概知道结果了：
		 *       · 代理子类中调用对应方法的时候、不仅会判断当前方法是否被增强、还要检查增强当前方法的 MethodProxy
		 *       · 被 aop生成的方法都会生成一个 MethodProxy、并且每个不一样
		 *     当我们调用 invokeHello()时、会执行 invoke的切面逻辑、并且把 invokeMethodProxy传进去
		 *       · 然后再执行 hello()时、又会判断当前的 MethodProxy是不是 helloMethodProxy
		 *       · 如果是 helloMethodProxy、那么就执行增强的 hello()方法
		 *       · 不然就执行父类的 hello()方法
		 *     （同样、不信的话大家可以自己写个测试用例试一下哈，我理解有问题的话欢迎找我和谐讨论，联系方式可以看 Readme.md）
		 */
	}

	/**
	 * 7. 接下来我们看一看解决注解失效的一种方法、也就是自己注入自己
	 *      · 注解失效最简单的办法就是直接调用被增强的类、或者直接增强被调用的方法
	 *    注意，自己注入自己并不是一个好的解决办法、最好不要用
	 *      · 因为这是很明显的循环依赖问题，循环依赖是设计有问题、spring虽然会帮我们解决、但并不代表我们应该设计出这种代码
	 *      · 但是我们出于学习的目的是可以研究一下的哈
	 *    照例说一下 "com.adong.study.bean.aopInvalid.autowiredSelf"的结构：
	 *      · MyHelloAopAspect增强 hello()方法、TestAopConfiguration引入 aop
	 *      · TestAopBean中自己注入了自己、并且在 invokeHello()方法中并不直接调用 hello()、而是通过注入的自己来调用 hello()
	 *    这里其实不用加断点了、直接运行看看结果就行
	 */
	@Test
	public void testAopInvalidAutowiredSelf() {
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.adong.study.bean.aopInvalid.autowiredSelf");

		com.adong.study.bean.aopInvalid.autowiredSelf.TestAopBean bean = context.getBean(com.adong.study.bean.aopInvalid.autowiredSelf.TestAopBean.class);

		bean.invokeHello();

		System.out.println("wait wait wait");

		bean.hello();
		/**
		 * 7.1 我们可以发现，这里打印了两次 "before hello"和 "hello hello hey"
		 *     说明 invokeHello()调用的 self.hello()也被增强了
		 *     其实如果前面几个测试用例你都理解了的话、这个也很好理解
		 *     我们在 TestAopBean注入的 TestAopBean肯定是经过代理以后的 bean、这个 bean里面的 hello()方法很明显是被增强过的
		 *     所以自己注入自己可以解决注解失效的问题、但再次强调这并不值得提倡
		 */
	}

	/**
	 * 8. 接下来我们来试一下懒加载的 bean是什么时候实例化的
	 */
	@Test
	public void testLazy() {
		/**
		 * 8.1 这里的 "com.adong.study.bean.lazy"下只有一个类：
		 *     也就是一个 Person类，并且加了@Component和@Lazy，代表这是一个需要懒加载的 bean
		 */
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.adong.study.bean.lazy");

		/**
		 * 8.2 我们在这一行打一个断点，看一下此时 context.getBeanFactory()的 singletonObjects属性中有没有 person的
		 *     其实背过八股的朋友应该都知道，懒加载的 bean，在第一次调用 getBean()的时候才会实例化
		 *     所以我们在 "wait wait wait2"的 sout的位置也加一个断点，再看看 context.getBeanFactory()的 singletonObjects属性中有没有 person
		 */
		System.out.println("wait wait wait1");

		Object person = context.getBean("person");

		/**
		 * 8.3 结果还是比较好猜的，上一个断点的位置中、ioc容器已经启动了但是没有 person
		 *     在经过调用 getBean()之后、ioc容器中就多个一个 person了
		 *     这也是为什么懒加载能够解决循环依赖问题的关键
		 *     其实不只是调用 getBean()方法、只要我们调用 getBean()或者说调用 person的任意方法时、都会实例化这个 bean
		 */
		System.out.println("wait wait wait2");
	}

	/**
	 * 9. 接下来我们来研究一下懒加载是怎么实现的
	 *    大家可以看一下这个博客，写的很好：https://juejin.cn/post/7288963211071684666?share_token=0bd09734-ae57-4564-ac45-f2bbd53417a9
	 */
	@Test
	public void testLazyImpl() {
		/**
		 * 9.1 "com.adong.study.bean.lazy"下有两个类：
		 *       · Cat类，这个类上加了@Component、里面有一个master属性，并且加了@Lazy+@Autowired
		 *       · Person类，这个类上加了@Component、里面啥也没有
		 *     然后我们在 sout "wait wait wait1"的地方加一个断点、debug一下
		 */
		AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext("com.adong.study.bean.lazyImpl");

		/**
		 * 9.2 此时我们看 context.getBeanFactory()的 singletonObjects属性中是没有 person但是有 cat
		 *     我们打开 cat看一下、会发现 cat的 master属性是一个 Person的代理对象，然后你还会惊奇的发现、这个代理对象居然不在 ioc容器里面
		 *     其实到这里大概就能想明白懒加载的原理了：
		 *       · 首先，懒加载的 bean会有一个代理对象、并且这个代理对象还不会被注入到 ioc容器中
		 *       · 当我们调用被代理对象的任意方法时、会先执行实例化的过程、然后再调用被代理对象的方法
		 */
		System.out.println("wait wait wait1");

		/**
		 * 9.3 总结一下懒加载的实现过程，第一个需要实现的点：ioc容器启动的时候为什么懒加载的 bean没有被实例化？
		 *       · 这个问题其实很简单，在实例化的过程中有不少地方都会判断这个 bean是不是懒加载的、如果是那就会跳过
		 *       · 大家自己看代码的时候留意一下即可
		 *     第二个需要实现的点：如何在调用这个这个 bean的时候去执行实例化的逻辑？
		 *       · 这实际上就是一个代理的逻辑
		 *     最后还要说一下懒加载的一个失效场景：
		 *       · 就是如果 Person类上加了@Lazy、然后 Cat的 master属性上没加@Lazy
		 *       · 此时这个 Person类对应的 bean并不会被懒加载，因为依赖注入的逻辑是在 AbstractAutowireCapableBeanFactory#populateBean()中实现的
		 *       · 这个方法并不会管 Person类上有没有加@Lazy
		 *       · 只要当前正在创建的 cat需要依赖注入 person、那么就会去实例化 person
		 */
	}
}
