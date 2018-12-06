package leaderboard;//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;
import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.Maps;
import com.google.common.io.Resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class App implements RequestHandler<Object, String> {

    public String handleRequest(Object input, Context context) {
        String json = "";

        try {
            final AmazonS3 s3 = AmazonS3ClientBuilder.defaultClient();
            json = s3.getObjectAsString("advent-of-code-bucket", "leaderboard.json");
//            json = Resources.toString(Resources.getResource("leaderboard.json"), StandardCharsets.UTF_8).trim();
            return this.generateTable((Map)this.handleJson(json).get("members"));
        } catch (Exception e) {
            System.err.println("Exception: " + e);
            e.printStackTrace();
            return "error";
        }
    }

    String generateTable(Map<String, Object> map) {
        StringBuilder init = new StringBuilder("<html><head><style><style>body {    background-color: #0f0f23;}table {    border-collapse: collapse;    background: #0f0f23;    border-color: #cccccc;    width: 100%;}th, td {    padding: 8px;    text-align: left;    border-bottom: 1px solid #ddd;    background: #0f0f23;    color: #cccccc;}tr:hover {background-color:#ffff66;}</style></style></head><body><table>  <tr><th>name</th>    <th>score</th>    <th>stars</th>");

        for (int i = 1; i <= 25; ++i) {
            init.append(String.format("<th>%s*</th><th>%s**</th>", i, i));
        }

        init.append("</tr>");
        Iterator var5 = ((List) map.values()
                                   .stream()
                                   .sorted(Comparator.comparing(this::getScore)
                                                     .reversed())
                                   .collect(Collectors.toList())).iterator();

        while (var5.hasNext()) {
            Object value = var5.next();
            init.append(this.getRow(value, map));
        }

        init.append("</table></body></html>");
        return init.toString();
    }

    private Integer getScore(Object x2) {
        return (Integer) ((Map) x2).get("local_score");
    }

    private String getRow(Object value, Map<String, Object> all) {
        Map<String, Object> map = (Map) value;
        StringBuilder res = new StringBuilder(String.format("<tr><td><b>%s</b></td><td>%s</td><td>%s</td>", map.get("name") == null ? map.get("id") : map.get("name"), map.get("local_score"), map.get("stars")));

        for (int i = 1; i <= 25; ++i) {
            Map<String, Map<String, Map<String, String>>> completion_day_level = (Map) ((Map) map.get("completion_day_level"));

            for (int j = 1; j <= 2; ++j) {
                String date1 = (String) ((Map) ((Map) completion_day_level.getOrDefault(Integer.toString(i), Maps.newHashMap())).getOrDefault(Integer.toString(j), Maps.newHashMap())).getOrDefault("get_star_ts", "-");
                String pos = this.getPoints(i, j, date1, all);
                res.append(this.getRes(pos));
            }
        }

        return res + "</tr>";
    }

    private String getRes(String pos) {
        return "<td>" + this.getColor(pos) + "</td>";
    }

    private String getColor(String pos) {
        byte var3 = -1;
        switch (pos.hashCode()) {
            case 49:
                if (pos.equals("1")) {
                    var3 = 0;
                }
                break;
            case 50:
                if (pos.equals("2")) {
                    var3 = 1;
                }
                break;
            case 51:
                if (pos.equals("3")) {
                    var3 = 2;
                }
        }

        switch (var3) {
            case 0:
                return "<font color=\"#ffff66\"><b>" + pos + "</b></font>";
            case 1:
                return "<font color=\"aaaa66\"><b>" + pos + "</b></font>";
            case 2:
                return "<font color=\"#965A38\"><b>" + pos + "</b></font>";
            default:
                return pos;
        }
    }

    private String getPoints(int i, int j, String date1, Map<String, Object> all) {
        if (date1.equals("-")) {
            return date1;
        } else {
            int result = ((List) all.values().stream().map((x) -> {
                return (Map) ((Map) ((Map) x).get("completion_day_level"));
            }).map((x) -> {
                return (String) ((Map) ((Map) x.getOrDefault(Integer.toString(i), Maps.newHashMap())).getOrDefault(Integer.toString(j), Maps.newHashMap())).getOrDefault("get_star_ts", "-");
            }).filter((x) -> {
                return !"-".equals(x);
            }).map(this::fromString).filter((x) -> {
                return x.compareTo(this.fromString(date1)) <= 0;
            }).collect(Collectors.toList())).size();
            return Integer.toString(result);
        }
    }

    LocalDateTime fromString(String s) {
        LocalDateTime date =
                LocalDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(s)), ZoneId.systemDefault());
        return date;
    }

    Map<String, Object> handleJson(String json) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> map = (Map) mapper.readValue(json, new TypeReference<Map<String, Object>>() {
            });
            return map;
        } catch (JsonGenerationException var4) {
            var4.printStackTrace();
        } catch (JsonMappingException var5) {
            var5.printStackTrace();
        } catch (IOException var6) {
            var6.printStackTrace();
        }

        return null;
    }
}
