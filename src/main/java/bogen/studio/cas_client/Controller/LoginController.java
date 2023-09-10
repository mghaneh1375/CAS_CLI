package bogen.studio.cas_client.Controller;

import bogen.studio.cas_client.DTO.LoginRequest;
import bogen.studio.cas_client.Security.SecurityProperties;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.Principal;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.HashMap;

import static java.nio.charset.StandardCharsets.UTF_8;

@RestController
public class LoginController {

    @Autowired
    SecurityProperties securityProperties;


    private String getPublicKeyAsString() {
        try {
            return IOUtils.toString(securityProperties.getJwt().getPublicKey().getInputStream(), UTF_8);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping("/user")
    @ResponseBody
    public String user(HttpServletRequest request, Authentication authentication, Principal principal) {

        String token = request.getHeader("Authorization");
        token = token.replace("Bearer ", "");

        String pubkeyb64 = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAtx5XIQ7QRnKZRRDexf7X" +
                "zZxMhf+hE807qwi0Ul1WWcLt5be7zsHGdOsn3BGGB8BAmeA54qespU7MJFNIW21l" +
                "Qb/XqexShrsiOvVxs8Z75RZfA2UjYwV1tHW58MTIgRdER67aJj0hIofgOFztB0CN" +
                "RHaehltR3up3tEPnz0HxsuSESmPccU86YJUKyu2QUW7hcrj0yUBeFiFrDhRKel5O" +
                "9+X862FOE+aSWAaX69hTUTf8CDSXpAlH93xX27Uz5h/bTbSIB2fXbsINe0d4HdX2" +
                "TQceyBQe+LoNmIfrnTPjyvf67ICGYFkCH8G7zF9851o63sbquWKA6NQ90ydkV/hO" +
                "twIDAQAB";

        byte[] pubkeyder = Base64.getDecoder().decode(pubkeyb64);
        PublicKey pubkey = null;
        try {
            pubkey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubkeyder));

            Claims claims = Jwts.parser().setSigningKey(pubkey).parseClaimsJws(token).getBody();
            System.out.println(claims.getExpiration());
            System.out.println(claims.getId());

            System.out.println(principal.getName());
            System.out.println(principal);

            authentication.getAuthorities().forEach(grantedAuthority -> {
                System.out.println(grantedAuthority.getAuthority());
            });

        } catch (InvalidKeySpecException e) {
            throw new RuntimeException(e);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }

        return token;
    }


    @GetMapping("/login")
    public ModelAndView get(HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestParam(value = "callback") String callback,
                            @RequestParam(value = "redirectUrl") String redirectUrl
    ) {

        Object token = request.getSession().getAttribute("token");

        if (token != null) {
            response.setHeader("Location", redirectUrl);
            response.setStatus(302);
            return null;
        }

        ModelAndView modelAndView = new ModelAndView();
        modelAndView.addObject("callback", callback);
        modelAndView.addObject("redirectUrl", redirectUrl);
        modelAndView.setViewName("login");
        return modelAndView;
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

            HttpResponse<JsonNode> res = Unirest.post("http://193.151.137.75:9000/oauth/token")
                    .queryString("grant_type", "password")
                    .queryString("username", username)
                    .queryString("password", password)
                    .basicAuth("clientId", "Admin123").asJson();

            System.out.println(res.getStatus());

            if (res.getStatus() != 200) {
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

            response.setHeader("Location", redirectUrl + "?uuid=" + res.getBody().getObject().getString("data"));
            response.setStatus(302);

        } catch (UnirestException e) {
            e.printStackTrace();
        }
    }

}
