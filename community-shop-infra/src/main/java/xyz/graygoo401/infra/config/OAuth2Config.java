package xyz.graygoo401.infra.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestTemplate;

/**
 * OAuth2.0第三方登录配置类，适配微信和QQ登录
 * 用于实现第三方登录集成方案
 */
public class OAuth2Config {
    // 微信开放平台APP ID（从配置文件读取）
    @Value("${oauth.wechat.app-id}")
    private String wechatAppId;

    // 微信开放平台APP 密钥
    @Value("${oauth.wechat.app-secret}")
    private String wechatAppSecret;

    // 微信授权回调地址
    @Value("${oauth.wechat.redirect-uri}")
    private String wechatRedirectUri;

    // QQ互联APP ID
    @Value("${oauth.qq.app-id}")
    private String qqAppId;

    // QQ互联APP 密钥
    @Value("${oauth.qq.app-secret}")
    private String qqAppSecret;

    // QQ授权回调地址
    @Value("${oauth.qq.redirect-uri}")
    private String qqRedirectUri;

    // 微信获取access_token的API地址
    private static final String WECHAT_ACCESS_TOKEN_URL =
            "https://api.weixin.qq.com/sns/oauth2/access_token?appid=%s&secret=%s&code=%s&grant_type=authorization_code";

    // 微信获取用户信息的API地址
    private static final String WECHAT_USER_INFO_URL =
            "https://api.weixin.qq.com/sns/userinfo?access_token=%s&openid=%s";

    // QQ获取access_token的API地址
    private static final String QQ_ACCESS_TOKEN_URL =
            "https://graph.qq.com/oauth2.0/token?grant_type=authorization_code&client_id=%s&client_secret=%s&code=%s&redirect_uri=%s";

    // QQ获取openid的API地址
    private static final String QQ_OPENID_URL =
            "https://graph.qq.com/oauth2.0/me?access_token=%s";

    // QQ获取用户信息的API地址
    private static final String QQ_USER_INFO_URL =
            "https://graph.qq.com/user/get_user_info?access_token=%s&oauth_consumer_key=%s&openid=%s";

    /**
     * 创建RestTemplate实例，用于调用第三方API
     * @return RestTemplate对象
     */
    @Bean
    public RestTemplate restTemplate() {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        // 设置连接超时时间为5秒
        requestFactory.setConnectTimeout(5000);
        // 设置读取超时时间为10秒
        requestFactory.setReadTimeout(10000);
        return new RestTemplate(requestFactory);
    }

    // Getter方法，提供第三方平台配置信息给Service层使用
    public String getWechatAppId() {
        return wechatAppId;
    }

    public String getWechatAppSecret() {
        return wechatAppSecret;
    }

    public String getWechatRedirectUri() {
        return wechatRedirectUri;
    }

    public String getQqAppId() {
        return qqAppId;
    }

    public String getQqAppSecret() {
        return qqAppSecret;
    }

    public String getQqRedirectUri() {
        return qqRedirectUri;
    }

    /**
     * 构建微信获取access_token的完整URL
     * @param code 前端获取的授权码
     * @return 完整的API请求URL
     */
    public String buildWechatAccessTokenUrl(String code) {
        return String.format(WECHAT_ACCESS_TOKEN_URL, wechatAppId, wechatAppSecret, code);
    }

    /**
     * 构建微信获取用户信息的完整URL
     * @param accessToken 访问令牌
     * @param openId 微信用户唯一标识
     * @return 完整的API请求URL
     */
    public String buildWechatUserInfoUrl(String accessToken, String openId) {
        return String.format(WECHAT_USER_INFO_URL, accessToken, openId);
    }

    /**
     * 构建QQ获取access_token的完整URL
     * @param code 前端获取的授权码
     * @return 完整的API请求URL
     */
    public String buildQqAccessTokenUrl(String code) {
        return String.format(QQ_ACCESS_TOKEN_URL, qqAppId, qqAppSecret, code, qqRedirectUri);
    }

    /**
     * 构建QQ获取openid的完整URL
     * @param accessToken 访问令牌
     * @return 完整的API请求URL
     */
    public String buildQqOpenidUrl(String accessToken) {
        return String.format(QQ_OPENID_URL, accessToken);
    }

    /**
     * 构建QQ获取用户信息的完整URL
     * @param accessToken 访问令牌
     * @param openId QQ用户唯一标识
     * @return 完整的API请求URL
     */
    public String buildQqUserInfoUrl(String accessToken, String openId) {
        return String.format(QQ_USER_INFO_URL, accessToken, qqAppId, openId);
    }
}
