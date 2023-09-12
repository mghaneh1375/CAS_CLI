package bogen.studio.cas_client.Controller;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

@RestController
@RequestMapping(path = "/cas")
public class LoginController {

    @Value("${cas.address}")
    String CAS_SERVER;

    @Value("${cas.clientId}")
    String CLIENT_ID;

    @Value("${cas.password}")
    String PASSWORD;

    @Value("${cas.public-key}")
    String PUB_KEY;

    private static final String[] DONT_ALLOW_SIGN_UP = new String[] {
            "boom.bogenstudio.com"
    };

    class UUID {

        String uuid;
        String token;
        long expireAt;
        long lastGet;

        public UUID(String uuid, String token, long expireAt) {
            this.uuid = uuid;
            this.token = token;
            this.expireAt = expireAt;
            lastGet = System.currentTimeMillis();
        }
    }

    private static ArrayList<UUID> uuids = new ArrayList<>();

    @GetMapping("/login")
    public ModelAndView get(HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestParam(value = "callback") String callback,
                            @RequestParam(value = "redirectUrl") String redirectUrl
    ) {

        Object token = request.getSession().getAttribute("token");

        if (token != null) {

            UUID uuid = uuids.stream().filter(e -> e.token.equals(token)).findFirst().orElse(null);

            if(uuid != null && System.currentTimeMillis() - uuid.lastGet > 10000) {

                HttpResponse<JsonNode> res;
                try {
                    res = Unirest.post(callback)
                            .header("Content-Type", "application/json")
                            .body(new JSONObject()
                                    .put("token", token)
                            ).asJson();

                    if (res.getStatus() == 200) {
                        String newUuid = res.getBody().getObject().getString("data");
                        uuid.lastGet = System.currentTimeMillis();
                        uuid.uuid = newUuid;

                        response.setHeader("Location", redirectUrl + "?uuid=" + newUuid);
                        response.setStatus(302);
                        return null;
                    }

                } catch (UnirestException ignore) {}

            }

            response.setHeader("Location", redirectUrl);
            response.setStatus(302);
            return null;
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("callback", callback);
        modelAndView.addObject("redirectUrl", redirectUrl);
        modelAndView.addObject("allowSignUp", Arrays.stream(DONT_ALLOW_SIGN_UP).noneMatch(callback::contains));
        modelAndView.setViewName("login");
        return modelAndView;
    }

    @GetMapping("/logout")
    public void logout(HttpServletRequest request) {
        request.getSession().removeAttribute("token");
    }

    @PostMapping("/login")
    public void login(HttpServletRequest request,
                      HttpServletResponse response,
                      @RequestParam String username,
                      @RequestParam String password,
                      @RequestParam String callback,
                      @RequestParam String redirectUrl
    ) {

        try {

            HttpResponse<JsonNode> res = Unirest.post(CAS_SERVER)
                    .queryString("grant_type", "password")
                    .queryString("username", username)
                    .queryString("password", password)
                    .basicAuth(CLIENT_ID, PASSWORD).asJson();

            System.out.println(res.getStatus());

            if (res.getStatus() != 200) {
                ModelAndView modelAndView = new ModelAndView();
                modelAndView.addObject("callback", callback);
                modelAndView.addObject("redirectUrl", redirectUrl);
                modelAndView.addObject("error", "نام کاربری و یا رمزعبور وارد شده استباه است.");
                modelAndView.setViewName("login");

                response.setHeader("Location", "http://" + request.getHeader("host") + "/login?redirectUrl=" + redirectUrl + "&callback=" + callback + "&error");
                response.setStatus(302);
                return;
            }

            String token = res.getBody().getObject().getString("access_token");
            request.getSession().setAttribute("token", token);

            res = Unirest.post(callback)
                    .header("Content-Type", "application/json")
                    .body(new JSONObject()
                            .put("token", token)
                    ).asJson();

            System.out.println(res.getStatus());
            System.out.println(res.getStatusText());

            if (res.getStatus() != 200) {
                return;
            }

            byte[] pubkeyder = Base64.getDecoder().decode(PUB_KEY);
            PublicKey pubkey;
            try {
                pubkey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubkeyder));
                Claims claims = Jwts.parser().setSigningKey(pubkey).parseClaimsJws(token).getBody();

                String uuid = res.getBody().getObject().getString("data");
                uuids.add(new UUID(uuid, token, claims.getExpiration().getTime()));

                response.setHeader("Location", redirectUrl + "?uuid=" + uuid);
                response.setStatus(302);

            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }


        } catch (UnirestException e) {
            e.printStackTrace();
        }

    }

}
