package bogen.studio.cas_client.Controller;

import bogen.studio.cas_client.DTO.ActivateRequest;
import bogen.studio.cas_client.DTO.LoginRequest;
import bogen.studio.cas_client.DTO.SetUsernameRequest;
import bogen.studio.cas_client.DTO.SignUpRequest;
import bogen.studio.cas_client.Service.UserService;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.nio.charset.StandardCharsets;
import java.util.*;

import org.springframework.http.HttpStatus;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLEncoder;

import static my.common.commonkoochita.Utility.Statics.JSON_NOT_VALID;
import static my.common.commonkoochita.Utility.Statics.JSON_NOT_VALID_PARAMS;
import static my.common.commonkoochita.Utility.Utility.*;

@RestController
@RequestMapping(path = "/cas")
@Validated
public class LoginController {

    @Autowired
    UserService userService;

    public static final String[] DONT_ALLOW_SIGN_UP = new String[]{
            "boom.bogenstudio.com",
            "panel.koochita.com",
    };

    public static class UUID {

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

    public static ArrayList<UUID> uuids = new ArrayList<>();

    @GetMapping("/login")
    public ModelAndView get(HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestParam(value = "callback") String callback,
                            @RequestParam(value = "redirectUrl") String redirectUrl
    ) {

        Object token = request.getSession().getAttribute("token");

        if (token != null) {

            UUID uuid = uuids.stream().filter(e -> e.token.equals(token)).findFirst().orElse(null);

            if (uuid != null && System.currentTimeMillis() - uuid.lastGet > 10000) {

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

                        response.setHeader("Location", URLEncoder.encode(redirectUrl + "?uuid=" + newUuid, StandardCharsets.UTF_8));
                        response.setStatus(302);
                        return null;
                    }

                } catch (UnirestException ignore) {
                }

            }

            response.setHeader("Location", URLEncoder.encode(redirectUrl, StandardCharsets.UTF_8));
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


    @PostMapping(value = "/signUp")
    @ResponseBody
    public String signUp(final @RequestBody @Valid SignUpRequest signUpRequest) {

        try {

            return userService.signUp(
                    signUpRequest.getVia(),
                    signUpRequest.getValue(),
                    signUpRequest.getCallback()
            );

        } catch (Exception x) {
            printException(x);
        }

        return JSON_NOT_VALID;
    }

    //    @PostMapping(value = "/resendCode")
//    @ResponseBody
//    public String resendCode(@RequestBody @StrongJSONConstraint(
//            params = {"token", "value"},
//            paramsType = {String.class, String.class}
//    ) @NotBlank String json) {
//        return UserController.resend(convertPersian(new JSONObject(json)));
//    }
//
    @PostMapping(value = "/checkCode")
    @ResponseBody
    public String checkCode(@RequestBody @Valid ActivateRequest activateRequest) {

        if (activateRequest.getCode() < 1000 || activateRequest.getCode() > 9999)
            return JSON_NOT_VALID_PARAMS;

        return userService.checkCode(activateRequest);
    }

    @PostMapping(value = "/setUsername")
    @ResponseBody
    public String setUsername(@RequestBody @Valid SetUsernameRequest usernameRequest) {
        return userService.setUsername(usernameRequest);
    }

    @GetMapping("/logout")
    public ResponseEntity<Object> logout(HttpServletRequest request,
                                         HttpServletResponse response,
                                         @RequestParam String redirectUrl) {
        try {
            request.getSession().removeAttribute("token");

            URI externalUri = new URI(redirectUrl);
            HttpHeaders httpHeaders = new HttpHeaders();
            httpHeaders.setLocation(externalUri);

            return new ResponseEntity<>(httpHeaders, HttpStatus.SEE_OTHER);
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(HttpServletRequest request,
                                        @RequestParam String token,
                                        @RequestParam String redirectUrl,
                                        @RequestParam String callback,
                                        @RequestParam Integer code,
                                        @RequestParam String value
    ) {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setCallback(callback);
        loginRequest.setCode(code);
        loginRequest.setToken(token);
        loginRequest.setRedirectUrl(redirectUrl);
        loginRequest.setValue(value);
        return userService.signIn(request, loginRequest);
    }

}
