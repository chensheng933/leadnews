package com.heima.gateway.app.filter;

import com.heima.model.user.pojos.ApUser;
import com.heima.utils.common.JwtUtils;
import com.heima.utils.common.Payload;
import com.heima.utils.common.RsaUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.security.PublicKey;

/**
 * 鉴权过滤器
 */
@Component
public class AuthorizeFilter implements GlobalFilter,Ordered{
    @Value("${leadnews.jwt.publicKeyPath}")
    private String publicKeyPath;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        //获取请求和响应
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        //判断该请求是否为登录请求
        String uri = request.getURI().getPath();  // /user/api/v1/login/login_auth
        if(uri.contains("/login")){
            //放行
            return chain.filter(exchange);
        }

        //判断是否存在token请求头
        String token = request.getHeaders().getFirst("token");
        if(StringUtils.isEmpty(token)){
            //拒绝访问
            response.setStatusCode(HttpStatus.UNAUTHORIZED);//401
            //停止请求
            return response.setComplete();
        }

        //校验token是否合法
        try {
            //获取公钥
            PublicKey publicKey = RsaUtils.getPublicKey(publicKeyPath);

            Payload<ApUser> payload = JwtUtils.getInfoFromToken(token, publicKey, ApUser.class);

            //取出登录用户信息
            ApUser user = payload.getInfo();

            //把用户信息存入请求头
            request.mutate().header("userId",user.getId().toString());

            //放行
            return chain.filter(exchange);
        } catch (Exception e) {
            //拒绝访问
            response.setStatusCode(HttpStatus.UNAUTHORIZED);//401
            //停止请求
            return response.setComplete();
        }
    }

    /**
     * 数字越大，优先级越低
     * @return
     */
    @Override
    public int getOrder() {
        return 0;
    }
}
