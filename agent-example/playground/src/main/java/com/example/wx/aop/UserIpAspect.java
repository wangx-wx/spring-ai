package com.example.wx.aop;

import com.example.wx.entiy.User;
import com.example.wx.repository.UserRepository;
import com.example.wx.utils.TimeUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * @author wangx
 * @description
 * @create 2025/7/20 16:11
 */
@Aspect
@Component
public class UserIpAspect {
    private final Logger logger = LoggerFactory.getLogger(UserIpAspect.class);

    private final HttpServletRequest request;

    private final UserRepository userRepository;

    public UserIpAspect(
            HttpServletRequest request,
            UserRepository userRepository
    ) {
        this.request = request;
        this.userRepository = userRepository;
    }

    @Pointcut("@annotation(com.example.wx.annotation.UserIp)")
    public void logUserIp() {}

    @After("logUserIp()")
    public void after() {

        String userIp = request.getRemoteAddr();
        String requestUri = request.getRequestURI();
        String requestTime = TimeUtils.getCurrentTime();

        logger.info("User IP: {}, Time: {}, Uri: {}", userIp, requestTime, requestUri);

        userRepository.findByRequestIp(userIp)
                .ifPresentOrElse(
                        user -> {
                            user.setRequestCount(user.getRequestCount() + 1);
                            user.setRequestUri(user.getRequestUri() + ", " + requestUri);
                            userRepository.save(user);
                        },
                        () -> {
                            User newUser = new User.Builder()
                                    .setRequestUri(requestUri)
                                    .setRequestTime(requestTime)
                                    .setRequestIp(userIp)
                                    .setRequestCount(1)
                                    .build();
                            userRepository.save(newUser);
                        }
                );
    }
}
