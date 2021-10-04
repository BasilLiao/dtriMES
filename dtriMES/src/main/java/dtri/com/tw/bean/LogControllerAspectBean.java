package dtri.com.tw.bean;

import java.util.Arrays;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * AOP Log 設定 Pointcut：要被AOP切入的位置，使用pointcut expression來表示， 而Pointcut位置的Join
 * point即為Advice施行的目標。 上面的@Pointcut("execution(*
 * idv.matt.controller..*(..))")即表示切入位置為idv.matt.controller下的任意method。
 * 
 * https://matthung0807.blogspot.com/2019/02/springbootspring-aoplog.html
 **/
@Aspect
@Component
public class LogControllerAspectBean {

	@Pointcut("execution(* dtri.com.tw.controller..*(..))")
	public void pointcut() {

	}

	@Before("pointcut()")
	public void before(JoinPoint joinPoint) {
		Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass().getName());
		String args = Arrays.toString(joinPoint.getArgs());
		logger.info("<===== Start =====> " + logger.getName());
		logger.info("<====v Args v====>");
		logger.info(args);
		logger.info("<====^ Args ^====>");
	}

	@After("pointcut()")
	public void after(JoinPoint joinPoint) {
		Logger logger = LoggerFactory.getLogger(joinPoint.getTarget().getClass().getName());
		logger.info("<===== End =====> " + logger.getName());
	}

}
