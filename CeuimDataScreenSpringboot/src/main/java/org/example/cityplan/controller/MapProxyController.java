package org.example.cityplan.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestTemplate;

/**
 * 地图搜索代理 (解决天地图 CORS 跨域限制)
 */
@Slf4j
@RestController
@RequestMapping("/api/map")
public class MapProxyController {

    @Value("${tianditu.token:abf1d23efb709a79bb93553d8410ba24}")
    private String tiandituToken;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * 天地图地名搜索代理
     */
    @GetMapping("/search")
    public ResponseEntity<String> searchPlace(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "12") int level,
            @RequestParam(defaultValue = "113.5,37.5,115.5,38.6") String bound,
            @RequestParam(defaultValue = "8") int count) {
        log.info("[地图代理] 地名搜索 - keyword: {}", keyword);
        try {
            String postStr = String.format(
                    "{\"keyWord\":\"%s\",\"level\":%d,\"mapBound\":\"%s\",\"queryType\":1,\"count\":%d,\"start\":0}",
                    keyword, level, bound, count);
            String url = String.format(
                    "http://api.tianditu.gov.cn/v2/search?postStr=%s&type=query&tk=%s",
                    java.net.URLEncoder.encode(postStr, "UTF-8"),
                    tiandituToken);
            String result = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("[地图代理] 搜索失败", e);
            return ResponseEntity.badRequest().body("{\"error\": \"搜索失败\"}");
        }
    }

    /**
     * 天地图行政区划搜索代理 (获取边界)
     */
    @GetMapping("/administrative")
    public ResponseEntity<String> searchAdministrative(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "156130100") String searchWord, // 默认石家庄市辖区
            @RequestParam(defaultValue = "1") String searchType) {
        log.info("[地图代理] 行政区划搜索 - keyword: {}, searchWord: {}", keyword, searchWord);
        try {
            // queryType=1 (1为普通，2为含边界线)
            // searchType=1 (1为按拼音/名称，2为按行政区划代码)
            String postStr = String.format(
                    "{\"searchWord\":\"%s\",\"searchType\":\"%s\",\"needSubInfo\":\"false\",\"needAll\":\"false\",\"needPolygon\":\"true\",\"needPre\":\"true\"}",
                    keyword, searchType);
            String url = String.format(
                    "http://api.tianditu.gov.cn/administrative?postStr=%s&tk=%s",
                    java.net.URLEncoder.encode(postStr, "UTF-8"),
                    tiandituToken);
            String result = restTemplate.getForObject(url, String.class);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("[地图代理] 行政区划搜索失败", e);
            return ResponseEntity.badRequest().body("{\"error\": \"行政区划搜索失败\"}");
        }
    }
}
