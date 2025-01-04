package com.xiaohang.xiaohangapiclientsdk.Client;


import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONUtil;
import com.xiaohang.xiaohangapiclientsdk.model.User;


import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

import static com.xiaohang.xiaohangapiclientsdk.utils.SignUtils.genSign;

/**
 * API 调用
 *
 * @author xiaohang
 */
public class XiaohangApiClient {

    private static String GATEWAY_HOST = "http://localhost:8090";

    private String accessKey;

    private String secretKey;

    public XiaohangApiClient(String accessKey, String secretKey) {
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    public void setGatewayHost(String gatewayHost) {
        GATEWAY_HOST = gatewayHost;
    }

    public String getNameByGet(String name) {
        //可以单独传入http参数，这样参数会自动做URL编码，拼接在URL中
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        String result = HttpUtil.get(GATEWAY_HOST + "/api/name/", paramMap);
        System.out.println(result);
        return result;
    }

    public String getNameByPost(String name) {
        //可以单独传入http参数，这样参数会自动做URL编码，拼接在URL中
        HashMap<String, Object> paramMap = new HashMap<>();
        paramMap.put("name", name);
        String result = HttpUtil.post(GATEWAY_HOST + "/api/name/", paramMap);
        System.out.println(result);
        return result;
    }

    private Map<String, String> getHeaderMap(String body, String method) throws UnsupportedEncodingException {
        HashMap<String, String> map = new HashMap<>();
        map.put("accessKey", accessKey);
        map.put("nonce", RandomUtil.randomNumbers(10));
        map.put("timestamp", String.valueOf(System.currentTimeMillis() / 1000));
        map.put("sign", genSign(body, secretKey));
        body = URLUtil.encode(body, CharsetUtil.CHARSET_UTF_8);
        map.put("body", body);
        map.put("method", method);
        return map;
    }

    public String invokeInterface(String params, String url, String method) throws UnsupportedEncodingException {
        HttpResponse httpResponse = HttpRequest.post(GATEWAY_HOST + url)
                .header("Accept-Charset", CharsetUtil.UTF_8)
                .addHeaders(getHeaderMap(params, method))
                .body(params)
                .execute();
        return JSONUtil.formatJsonStr(httpResponse.body());
    }
}
