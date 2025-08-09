package com.poeticjustice.deeppoemsinc.infrastructure.config;

import com.poeticjustice.deeppoemsinc.domain.Repository.mongodb.AcceptedIpsRepository;
import com.poeticjustice.deeppoemsinc.domain.Repository.mysql.UserRespository;
import com.poeticjustice.deeppoemsinc.application.events.NewIPDetectedEvent;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.poeticjustice.deeppoemsinc.domain.models.mongo.AcceptedIps;
import com.poeticjustice.deeppoemsinc.domain.models.mysql.User;
import com.poeticjustice.deeppoemsinc.infrastructure.utils.JwtTokenUtil;

import java.io.IOException;
import java.util.List;

@Component
public class IpCheckFilter extends OncePerRequestFilter {

    private final AcceptedIpsRepository acceptedIpsRepository;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;

    private User loggedInUser;

    @Autowired
    private UserRespository userRepository;
    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(IpCheckFilter.class);

    @Autowired
    private ApplicationEventPublisher eventPublisher;

    public IpCheckFilter(AcceptedIpsRepository acceptedIpsRepository) {
        this.acceptedIpsRepository = acceptedIpsRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        try {
            String authorization = request.getHeader("Authorization");
            if (authorization != null && !authorization.trim().isEmpty()) {

                String token = authorization.startsWith("Bearer ")
                        ? authorization.substring(7).trim()
                        : authorization.trim();

                int userId = jwtTokenUtil.getUserIdFromToken(token);

                String clientIp = getClientIp(request);
                // String userId from int
                String userIdStr = String.valueOf(userId);
                List<AcceptedIps> allowedIps = acceptedIpsRepository.findByUserId(userIdStr);

                boolean ipAllowed = allowedIps.stream()
                        .anyMatch(ip -> ip.getIpAddress().equals(clientIp));

                if (!ipAllowed) {
                    eventPublisher.publishEvent(new NewIPDetectedEvent(userIdStr, clientIp));
                }
            }
            else{
                logger.warn("Authorization header is missing or empty");
            }
        } catch (Exception e) {
            // Swallow exceptions so it never blocks the request
            logger.warn("IP check skipped due to error: {}", e.getMessage());
        }

        filterChain.doFilter(request, response);
    }

    private String getClientIp(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
