/*
 * Copyright 2002-2023 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.context.annotation;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionDefaults;
import org.springframework.beans.factory.support.BeanDefinitionReaderUtils;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.core.env.Environment;
import org.springframework.core.env.EnvironmentCapable;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.io.ResourceLoader;
import org.springframework.lang.Nullable;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.PatternMatchUtils;

/**
 * A bean definition scanner that detects bean candidates on the classpath,
 * registering corresponding bean definitions with a given registry ({@code BeanFactory}
 * or {@code ApplicationContext}).
 *
 * <p>Candidate classes are detected through configurable type filters. The
 * default filters include classes that are annotated with Spring's
 * {@link org.springframework.stereotype.Component @Component},
 * {@link org.springframework.stereotype.Repository @Repository},
 * {@link org.springframework.stereotype.Service @Service}, or
 * {@link org.springframework.stereotype.Controller @Controller} stereotype.
 *
 * <p>Also supports Jakarta EE's {@link jakarta.annotation.ManagedBean} and
 * JSR-330's {@link jakarta.inject.Named} annotations, if available.
 *
 * @author Mark Fisher
 * @author Juergen Hoeller
 * @author Chris Beams
 * @since 2.5
 * @see AnnotationConfigApplicationContext#scan
 * @see org.springframework.stereotype.Component
 * @see org.springframework.stereotype.Repository
 * @see org.springframework.stereotype.Service
 * @see org.springframework.stereotype.Controller
 */
public class ClassPathBeanDefinitionScanner extends ClassPathScanningCandidateComponentProvider {

	private final BeanDefinitionRegistry registry;

	private BeanDefinitionDefaults beanDefinitionDefaults = new BeanDefinitionDefaults();

	@Nullable
	private String[] autowireCandidatePatterns;

	private BeanNameGenerator beanNameGenerator = AnnotationBeanNameGenerator.INSTANCE;

	private ScopeMetadataResolver scopeMetadataResolver = new AnnotationScopeMetadataResolver();

	private boolean includeAnnotationConfig = true;


	/**
	 * Create a new {@code ClassPathBeanDefinitionScanner} for the given bean factory.
	 * @param registry the {@code BeanFactory} to load bean definitions into, in the form
	 * of a {@code BeanDefinitionRegistry}
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry) {
		this(registry, true);
	}

	/**
	 * Create a new {@code ClassPathBeanDefinitionScanner} for the given bean factory.
	 * <p>If the passed-in bean factory does not only implement the
	 * {@code BeanDefinitionRegistry} interface but also the {@code ResourceLoader}
	 * interface, it will be used as default {@code ResourceLoader} as well. This will
	 * usually be the case for {@link org.springframework.context.ApplicationContext}
	 * implementations.
	 * <p>If given a plain {@code BeanDefinitionRegistry}, the default {@code ResourceLoader}
	 * will be a {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}.
	 * <p>If the passed-in bean factory also implements {@link EnvironmentCapable} its
	 * environment will be used by this reader.  Otherwise, the reader will initialize and
	 * use a {@link org.springframework.core.env.StandardEnvironment}. All
	 * {@code ApplicationContext} implementations are {@code EnvironmentCapable}, while
	 * normal {@code BeanFactory} implementations are not.
	 * @param registry the {@code BeanFactory} to load bean definitions into, in the form
	 * of a {@code BeanDefinitionRegistry}
	 * @param useDefaultFilters whether to include the default filters for the
	 * {@link org.springframework.stereotype.Component @Component},
	 * {@link org.springframework.stereotype.Repository @Repository},
	 * {@link org.springframework.stereotype.Service @Service}, and
	 * {@link org.springframework.stereotype.Controller @Controller} stereotype annotations
	 * @see #setResourceLoader
	 * @see #setEnvironment
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters) {
		this(registry, useDefaultFilters, getOrCreateEnvironment(registry));
	}

	/**
	 * Create a new {@code ClassPathBeanDefinitionScanner} for the given bean factory and
	 * using the given {@link Environment} when evaluating bean definition profile metadata.
	 * <p>If the passed-in bean factory does not only implement the {@code
	 * BeanDefinitionRegistry} interface but also the {@link ResourceLoader} interface, it
	 * will be used as default {@code ResourceLoader} as well. This will usually be the
	 * case for {@link org.springframework.context.ApplicationContext} implementations.
	 * <p>If given a plain {@code BeanDefinitionRegistry}, the default {@code ResourceLoader}
	 * will be a {@link org.springframework.core.io.support.PathMatchingResourcePatternResolver}.
	 * @param registry the {@code BeanFactory} to load bean definitions into, in the form
	 * of a {@code BeanDefinitionRegistry}
	 * @param useDefaultFilters whether to include the default filters for the
	 * {@link org.springframework.stereotype.Component @Component},
	 * {@link org.springframework.stereotype.Repository @Repository},
	 * {@link org.springframework.stereotype.Service @Service}, and
	 * {@link org.springframework.stereotype.Controller @Controller} stereotype annotations
	 * @param environment the Spring {@link Environment} to use when evaluating bean
	 * definition profile metadata
	 * @since 3.1
	 * @see #setResourceLoader
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters,
			Environment environment) {

		this(registry, useDefaultFilters, environment,
				(registry instanceof ResourceLoader resourceLoader ? resourceLoader : null));
	}

	/**
	 * Create a new {@code ClassPathBeanDefinitionScanner} for the given bean factory and
	 * using the given {@link Environment} when evaluating bean definition profile metadata.
	 * @param registry the {@code BeanFactory} to load bean definitions into, in the form
	 * of a {@code BeanDefinitionRegistry}
	 * @param useDefaultFilters whether to include the default filters for the
	 * {@link org.springframework.stereotype.Component @Component},
	 * {@link org.springframework.stereotype.Repository @Repository},
	 * {@link org.springframework.stereotype.Service @Service}, and
	 * {@link org.springframework.stereotype.Controller @Controller} stereotype annotations
	 * @param environment the Spring {@link Environment} to use when evaluating bean
	 * definition profile metadata
	 * @param resourceLoader the {@link ResourceLoader} to use
	 * @since 4.3.6
	 */
	public ClassPathBeanDefinitionScanner(BeanDefinitionRegistry registry, boolean useDefaultFilters,
			Environment environment, @Nullable ResourceLoader resourceLoader) {

		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		this.registry = registry;

		if (useDefaultFilters) {
			registerDefaultFilters();
		}
		setEnvironment(environment);
		setResourceLoader(resourceLoader);
	}


	/**
	 * Return the BeanDefinitionRegistry that this scanner operates on.
	 */
	@Override
	public final BeanDefinitionRegistry getRegistry() {
		return this.registry;
	}

	/**
	 * Set the defaults to use for detected beans.
	 * @see BeanDefinitionDefaults
	 */
	public void setBeanDefinitionDefaults(@Nullable BeanDefinitionDefaults beanDefinitionDefaults) {
		this.beanDefinitionDefaults =
				(beanDefinitionDefaults != null ? beanDefinitionDefaults : new BeanDefinitionDefaults());
	}

	/**
	 * Return the defaults to use for detected beans (never {@code null}).
	 * @since 4.1
	 */
	public BeanDefinitionDefaults getBeanDefinitionDefaults() {
		return this.beanDefinitionDefaults;
	}

	/**
	 * Set the name-matching patterns for determining autowire candidates.
	 * @param autowireCandidatePatterns the patterns to match against
	 */
	public void setAutowireCandidatePatterns(@Nullable String... autowireCandidatePatterns) {
		this.autowireCandidatePatterns = autowireCandidatePatterns;
	}

	/**
	 * Set the BeanNameGenerator to use for detected bean classes.
	 * <p>Default is a {@link AnnotationBeanNameGenerator}.
	 */
	public void setBeanNameGenerator(@Nullable BeanNameGenerator beanNameGenerator) {
		this.beanNameGenerator =
				(beanNameGenerator != null ? beanNameGenerator : AnnotationBeanNameGenerator.INSTANCE);
	}

	/**
	 * Set the ScopeMetadataResolver to use for detected bean classes.
	 * Note that this will override any custom "scopedProxyMode" setting.
	 * <p>The default is an {@link AnnotationScopeMetadataResolver}.
	 * @see #setScopedProxyMode
	 */
	public void setScopeMetadataResolver(@Nullable ScopeMetadataResolver scopeMetadataResolver) {
		this.scopeMetadataResolver =
				(scopeMetadataResolver != null ? scopeMetadataResolver : new AnnotationScopeMetadataResolver());
	}

	/**
	 * Specify the proxy behavior for non-singleton scoped beans.
	 * Note that this will override any custom "scopeMetadataResolver" setting.
	 * <p>The default is {@link ScopedProxyMode#NO}.
	 * @see #setScopeMetadataResolver
	 */
	public void setScopedProxyMode(ScopedProxyMode scopedProxyMode) {
		this.scopeMetadataResolver = new AnnotationScopeMetadataResolver(scopedProxyMode);
	}

	/**
	 * Specify whether to register annotation config post-processors.
	 * <p>The default is to register the post-processors. Turn this off
	 * to be able to ignore the annotations or to process them differently.
	 */
	public void setIncludeAnnotationConfig(boolean includeAnnotationConfig) {
		this.includeAnnotationConfig = includeAnnotationConfig;
	}


	/**
	 * Perform a scan within the specified base packages.
	 * @param basePackages the packages to check for annotated classes
	 * @return number of beans registered
	 */
	public int scan(String... basePackages) {
		/**
		 * 1. 记录了一下扫描刚开始时的 Bean的数量（实际上是 BeanDefinition的数量）
		 *    这里的 registry.getBeanDefinitionCount()实际上调用的是 DefaultListableBeanFactory.getBeanDefinitionCount()
		 *    进到 DefaultListableBeanFactory里面看一下就知道了，这个方法返回的是一个 map.size()
		 *    这个 map也就是用来存储 BeanDefinition的 map（很明显我们现在还没开始扫描、size肯定=0）
		 */
		int beanCountAtScanStart = this.registry.getBeanDefinitionCount();

		/**
		 * 2. 这里是主要干活的地儿，进去看看吧
		 */
		doScan(basePackages);

		// Register annotation config processors, if necessary.
		/**
		 * 3. 下面是注册 annotation config processors
		 *    里面主要是处理了一些必要类型的 bean：
		 *      · 这个方法会判断当前所有的 beanDefinition中有没有一些必要的 bean、没有的话就必须手动加上
		 *      · 比如说自动注入处理器、事件监听器等等
		 *      · 这些 bean是 ioc容器启动所必须的 bean、如果连这些 bean对应的 beanDefinition都没有、那么 ioc容器的功能会不完整
		 */
		if (this.includeAnnotationConfig) {
			AnnotationConfigUtils.registerAnnotationConfigProcessors(this.registry);
		}

		/**
		 * 4. 最后返回了一下注册成功的 BeanDefinition的数量
		 */
		return (this.registry.getBeanDefinitionCount() - beanCountAtScanStart);
	}

	/**
	 * Perform a scan within the specified base packages,
	 * returning the registered bean definitions.
	 * <p>This method does <i>not</i> register an annotation config processor
	 * but rather leaves this up to the caller.
	 * @param basePackages the packages to check for annotated classes
	 * @return set of beans registered if any for tooling registration purposes (never {@code null})
	 */
	protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
		/**
		 * 跳过
		 */
		Assert.notEmpty(basePackages, "At least one base package must be specified");

		/**
		 * 1. 这里创建了一个 Set，用来装所有的 BeanDefinition，这个实际上就是结果集
		 *    先思考第一个问题：为什么用 Set不用 List？
		 *      · 这个问题其实还比较简单，因为 Set可以快速查找并去重，这样可以在注册过程中快速判断当前 BeanDefinition是否已经存在、同时避免重复注册
		 *    第二个问题：但是为什么要使用 LinkedHashSet呢？
		 *    	· 可以看看 LinkedHashSet和 HashSet有什么区别：https://cloud.tencent.com/developer/article/2348753
		 *      · 其实最大的原因就是 LinkedHashSet是有序的
		 *      · 思考一个问题 Todo：有序有什么用？后面会用到这个特性吗？
		 */
		Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();

		/**
		 * 2. 遍历传入的所有包路径
		 */
		for (String basePackage : basePackages) {
			/**
			 * 2.1 首先，找到所有的候选者
			 *     什么叫候选者？可以看到 candidates是一个 Set<BeanDefinition>，所以这里的 candidates其实也是一些 BeanDefinition
			 *     思考一个问题 Todo：那为什么叫候选者？为啥不直接注入这些 BeanDefinition？意思有一些不会被注入进来吗？
			 */
			Set<BeanDefinition> candidates = findCandidateComponents(basePackage);

			/**
			 * 2.2 遍历每个候选人，判断是不是要注册
			 */
			for (BeanDefinition candidate : candidates) {
				/**
				 * 2.3 获取作用域的元数据，实际上就是解析@Scope注解中的值、默认是单例的
				 * 	   然后把作用域加到候选人上
				 */
				ScopeMetadata scopeMetadata = this.scopeMetadataResolver.resolveScopeMetadata(candidate);
				candidate.setScope(scopeMetadata.getScopeName());

				/**
				 * 2.4 生成一下 bean的名字，调用的是 AnnotationBeanNameGenerator.generateBeanName()
				 *     可以进去看一下，大概逻辑就是取@Component的 value、没有的话默认是类名首字母小写
				 */
				String beanName = this.beanNameGenerator.generateBeanName(candidate, this.registry);

				/**
				 * 2.5 判断当前的候选人是否是某几个特殊类型、进行特殊的逻辑处理
				 * 	   主要是进行设置一些参数：比如懒加载、优先级之类的，不用深究
				 */
				if (candidate instanceof AbstractBeanDefinition abstractBeanDefinition) {
					postProcessBeanDefinition(abstractBeanDefinition, beanName);
				}
				if (candidate instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
					AnnotationConfigUtils.processCommonDefinitionAnnotations(annotatedBeanDefinition);
				}

				/**
				 * 2.6 筛选并注册候选人
				 *     上面说了、之所以叫“候选人”，是因为这些 BeanDefinition不一定会被注册进来，这里的 checkCandidate()就是筛选的方法
				 */
				if (checkCandidate(beanName, candidate)) {
					/**
					 * 2.7 这里创建了一个 BeanDefinitionHolder，进去看看这个类的几个属性就知道了
					 *     其实主要是用来存 BeanDefinition + BeanName + Alias的
					 */
					BeanDefinitionHolder definitionHolder = new BeanDefinitionHolder(candidate, beanName);
					definitionHolder =
							AnnotationConfigUtils.applyScopedProxyMode(scopeMetadata, definitionHolder, this.registry);

					/**
					 * 2.8 将当前的候选人的 BeanDefinitionHolder加到结果集中，代表这个候选人是一个需要被注册的 bean
					 */
					beanDefinitions.add(definitionHolder);
					registerBeanDefinition(definitionHolder, this.registry);
				}
			}
		}
		return beanDefinitions;
	}

	/**
	 * Apply further settings to the given bean definition,
	 * beyond the contents retrieved from scanning the component class.
	 * @param beanDefinition the scanned bean definition
	 * @param beanName the generated bean name for the given bean
	 */
	protected void postProcessBeanDefinition(AbstractBeanDefinition beanDefinition, String beanName) {
		beanDefinition.applyDefaults(this.beanDefinitionDefaults);
		if (this.autowireCandidatePatterns != null) {
			beanDefinition.setAutowireCandidate(PatternMatchUtils.simpleMatch(this.autowireCandidatePatterns, beanName));
		}
	}

	/**
	 * Register the specified bean with the given registry.
	 * <p>Can be overridden in subclasses, e.g. to adapt the registration
	 * process or to register further bean definitions for each scanned bean.
	 * @param definitionHolder the bean definition plus bean name for the bean
	 * @param registry the BeanDefinitionRegistry to register the bean with
	 */
	protected void registerBeanDefinition(BeanDefinitionHolder definitionHolder, BeanDefinitionRegistry registry) {
		BeanDefinitionReaderUtils.registerBeanDefinition(definitionHolder, registry);
	}


	/**
	 * Check the given candidate's bean name, determining whether the corresponding
	 * bean definition needs to be registered or conflicts with an existing definition.
	 * @param beanName the suggested name for the bean
	 * @param beanDefinition the corresponding bean definition
	 * @return {@code true} if the bean can be registered as-is;
	 * {@code false} if it should be skipped because there is an
	 * existing, compatible bean definition for the specified name
	 * @throws IllegalStateException if an existing, incompatible bean definition
	 * has been found for the specified name
	 */
	protected boolean checkCandidate(String beanName, BeanDefinition beanDefinition) throws IllegalStateException {
		/**
		 * 1. 如果 registry中不存在这个 BeanDefinition，那么直接返回 true，表示可以注册
		 */
		if (!this.registry.containsBeanDefinition(beanName)) {
			return true;
		}
		/**
		 * 1.1 上面的判断逻辑走到这儿之后，其实说明这个候选人已经被注册进来了
		 *     下面的逻辑主要是为了判断当前候选人和 registry中已经存在的 BeanDefinition 是否冲突、不深究
		 */

		BeanDefinition existingDef = this.registry.getBeanDefinition(beanName);
		BeanDefinition originatingDef = existingDef.getOriginatingBeanDefinition();
		if (originatingDef != null) {
			existingDef = originatingDef;
		}

		// Explicitly registered overriding bean?
		if (!(existingDef instanceof ScannedGenericBeanDefinition) &&
				(this.registry.isBeanDefinitionOverridable(beanName) || ObjectUtils.nullSafeEquals(
						beanDefinition.getBeanClassName(), existingDef.getBeanClassName()))) {
			return false;
		}

		// Scanned same file or equivalent class twice?
		if (isCompatible(beanDefinition, existingDef)) {
			return false;
		}

		throw new ConflictingBeanDefinitionException("Annotation-specified bean name '" + beanName +
				"' for bean class [" + beanDefinition.getBeanClassName() + "] conflicts with existing, " +
				"non-compatible bean definition of same name and class [" + existingDef.getBeanClassName() + "]");
	}

	/**
	 * Determine whether the given new bean definition is compatible with
	 * the given existing bean definition.
	 * <p>The default implementation considers them as compatible when the existing
	 * bean definition comes from the same source or from a non-scanning source.
	 * @param newDef the new bean definition, originated from scanning
	 * @param existingDef the existing bean definition, potentially an
	 * explicitly defined one or a previously generated one from scanning
	 * @return whether the definitions are considered as compatible, with the
	 * new definition to be skipped in favor of the existing definition
	 */
	protected boolean isCompatible(BeanDefinition newDef, BeanDefinition existingDef) {
		return ((newDef.getSource() != null && newDef.getSource().equals(existingDef.getSource())) ||
				newDef.equals(existingDef));
	}


	/**
	 * Get the Environment from the given registry if possible, otherwise return a new
	 * StandardEnvironment.
	 */
	private static Environment getOrCreateEnvironment(BeanDefinitionRegistry registry) {
		Assert.notNull(registry, "BeanDefinitionRegistry must not be null");
		if (registry instanceof EnvironmentCapable environmentCapable) {
			return environmentCapable.getEnvironment();
		}
		return new StandardEnvironment();
	}

}
