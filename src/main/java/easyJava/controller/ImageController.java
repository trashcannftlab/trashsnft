package easyJava.controller;


import easyJava.utils.ImageVerificationCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@RestController
public class ImageController {
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @RequestMapping("/user/getPicCode")
    @ResponseBody
    public void getPictureValidationCode(@RequestParam Map<String, Object> map, HttpServletRequest request, HttpServletResponse response) throws IOException {

        if (map.get("validationKey") == null || map.get("validationKey").toString().length() == 0) {
            return;
        }
        ImageVerificationCode imageVerificationCode = new ImageVerificationCode();
        BufferedImage image = imageVerificationCode.getImage();
        String key = "getPictureValidationCode_" + map.get("validationKey").toString();
        redisTemplate.opsForValue().set(key, imageVerificationCode.getText(), 3, TimeUnit.MINUTES);
        ImageVerificationCode.output(image, response.getOutputStream());
    }

    public Boolean checkPicCodeInternal(String validationKey, String validationCode) {
        Object rightValidationCode =
                redisTemplate.opsForValue().get("getPictureValidationCode_" + validationKey);
        if (StringUtils.isEmpty(rightValidationCode) || !rightValidationCode.toString().equalsIgnoreCase(validationCode.toLowerCase())) {
            return false;
        }
        return true;
    }
}
