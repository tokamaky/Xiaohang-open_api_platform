package com.xiaohang.xiaohangapiinterface.controller;

import cn.hutool.core.util.CharsetUtil;
import cn.hutool.core.util.URLUtil;
import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import  jakarta.servlet.http.HttpServletRequest;

/**
 * 网易云音乐接口
 *
 * @author csw
 */
@RestController
public class NetEaseController {

    @PostMapping("/api/comments.163")
    public String hotComments(HttpServletRequest request) {
        String url = "https://api.uomg.com/api/comments.163";
        String body = URLUtil.decode(request.getHeader("body"), CharsetUtil.CHARSET_UTF_8);
        System.out.println(body);
        HttpResponse httpResponse = HttpRequest.post(url)
                .body(body)
                .execute();
        return httpResponse.body();
    }

    @PostMapping("/api/")
    public String randUser(HttpServletRequest request) {
        String url = "https://randomuser.me/api/";
        String body = URLUtil.decode(request.getHeader("body"), CharsetUtil.CHARSET_UTF_8);
        HttpResponse httpResponse = HttpRequest.get(url + "?" + body)
                .execute();
        return httpResponse.body();
    }

    @PostMapping("/api/randomDog")
    public ResponseEntity<String> randPic() {
        String url = "https://dog.ceo/api/breeds/image/random";

        // Execute the HTTP GET request to fetch the dog image URL
        HttpResponse response = HttpRequest.get(url).execute();

        // Check if the request was successful (status code 200)
        if (response.getStatus() != 200) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error fetching image data");
        }

        // Parse the JSON response to extract the image URL
        String body = response.body();
        String imageUrl = parseImageUrl(body);

        // Return the image URL as a string in the response
        if (imageUrl != null) {
            return ResponseEntity.ok(imageUrl);
        } else {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Error parsing image URL");
        }
    }

    private String parseImageUrl(String responseBody) {
        try {
            // Using Jackson to parse the JSON response
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode rootNode = objectMapper.readTree(responseBody);

            // Extract the "message" field which contains the image URL
            JsonNode messageNode = rootNode.path("message");
            return messageNode.asText();  // Return the image URL
        } catch (Exception e) {
            e.printStackTrace();
            return null;  // Return null if there's an error parsing the JSON
        }
    }
}
