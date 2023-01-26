package easyJava.advice;

import com.alibaba.fastjson.JSON;
import easyJava.entity.ResponseEntity;
import easyJava.utils.AESUtils;
import easyJava.utils.DateUtils;
import easyJava.utils.TokenProccessor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@ControllerAdvice
public class MyResponseBodyAdvice implements ResponseBodyAdvice {
    private static final Logger logger = LoggerFactory.getLogger(MyResponseBodyAdvice.class);

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body, MethodParameter returnType, MediaType selectedContentType, Class selectedConverterType, ServerHttpRequest request, ServerHttpResponse response) {
        String requestPath = request.getURI().getPath();
        HttpServletRequest httpServletRequest = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        HttpSession httpSession = httpServletRequest.getSession(true);
        var headers = request.getHeaders();
        var adminToken = headers.get("adminToken");
        if (adminToken != null) {
            logger.info("requestPath:" + requestPath + ",adminToken:" + adminToken);
            String nowToken = "chr" + DateUtils.getDateStr();
            String nowTokenMd5 = TokenProccessor.EncodeByMD5(nowToken);
            logger.info("requestPath:" + requestPath + ",nowToken:" + nowToken + ",md5:" + nowTokenMd5);
            if (!adminToken.get(0).equals(nowTokenMd5)) {
                logger.error("requestPath:" + requestPath + ",adminToken:" + adminToken + ",md5:" + nowTokenMd5);
                return new ResponseEntity(401, "adminToken", "adminToken error");
            }
        }
        var encrypt = headers.get("encrypt");
        if (encrypt != null) {
            logger.info("requestPath:" + requestPath + ",encrypt:" + encrypt);
            String bodyStr = JSON.toJSONString(body);
            try {
                body = AESUtils.encryptIntoHexString(bodyStr, encrypt.get(0));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else {
            logger.info("requestPath:" + requestPath + ",encrypt:" + encrypt);
        }
        return body;
    }
}