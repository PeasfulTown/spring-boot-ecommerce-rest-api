package xyz.peasfultown.ecommerce.order_service.controller.aspect;

import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.server.ResponseStatusException;
import xyz.peasfultown.ecommerce.order_service.exception.AccessDeniedException;

@Aspect
@Component
public class AdminOnlyAspect {
    @Before("@annotation(AdminOnly)")
    public void checkAdminRole(JoinPoint joinPoint) {
        // extract userRole from request headers via RequestContextHolder
        HttpServletRequest request = ((ServletRequestAttributes)
                RequestContextHolder.getRequestAttributes()).getRequest();
        String role = request.getHeader("X-User-Role");

        if (!"ADMIN".equals(role))
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
    }
}
