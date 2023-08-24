package bogen.studio.cas_client.Controller;

import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;

@RestController
public class LoginController {


    @GetMapping("/user")
    @ResponseBody
    public String user(HttpServletRequest request) {

        String token = request.getHeader("Authorization");
        token = token.replace("Bearer ", "");

        return token;
    }


    @GetMapping("/login")
    public ModelAndView get() {

        ModelAndView modelAndView = new ModelAndView();

        modelAndView.setViewName("login");
        return modelAndView;
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password) {

        try {
            HttpResponse<JsonNode> response = Unirest.post("http://localhost:9000/oauth/token")
                    .queryString("grant_type", "password")
                    .queryString("username", username)
                    .queryString("password", password)
                    .basicAuth("clientId", "secret").asJson();

            if(response.getStatus() != 200)
                return "wrong credential";

            String token = response.getBody().getObject().getString("access_token");
            return token;

        } catch (UnirestException e) {
            e.printStackTrace();
        }

        return "ok";
    }

}
