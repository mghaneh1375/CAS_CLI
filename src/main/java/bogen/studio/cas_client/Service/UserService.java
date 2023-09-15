package bogen.studio.cas_client.Service;

import bogen.studio.cas_client.Controller.LoginController;
import bogen.studio.cas_client.DTO.ActivateRequest;
import bogen.studio.cas_client.DTO.LoginRequest;
import bogen.studio.cas_client.DTO.SetUsernameRequest;
import bogen.studio.cas_client.Enum.AuthVia;
import bogen.studio.cas_client.Model.Activation;
import bogen.studio.cas_client.Model.Avatar;
import bogen.studio.cas_client.Model.CommonUser;
import bogen.studio.cas_client.Repository.ActivationRepository;
import bogen.studio.cas_client.Repository.AvatarRepository;
import bogen.studio.cas_client.Repository.UserRepository;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import my.common.commonkoochita.Utility.PairValue;
import my.common.commonkoochita.Utility.Utility;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;

import static bogen.studio.cas_client.Controller.LoginController.uuids;
import static bogen.studio.cas_client.Utility.StaticValues.*;
import static bogen.studio.cas_client.Utility.Utility.randInt;
import static bogen.studio.cas_client.Utility.Utility.randInvitationCode;
import static my.common.commonkoochita.Utility.Statics.*;
import static my.common.commonkoochita.Utility.Utility.generateErr;
import static my.common.commonkoochita.Utility.Utility.generateSuccessMsg;

@Service
public class UserService {


    @Value("${cas.address}")
    String CAS_SERVER;

    @Value("${cas.clientId}")
    String CLIENT_ID;

    @Value("${cas.password}")
    String PASSWORD;

    @Value("${cas.public-key}")
    String PUB_KEY;

    @Autowired
    UserRepository userRepository;

    @Autowired
    ActivationRepository activationRepository;

    @Autowired
    AvatarRepository avatarRepository;

    @Autowired
    PasswordEncoder passwordEncoder;


    public PairValue existSMS(String value) {

        Activation activation =
                activationRepository.findByValueAndCreatedAt(value, System.currentTimeMillis() - SMS_RESEND_MSEC);

        if (activation != null)
            return new PairValue(activation.getToken(), SMS_RESEND_SEC - (System.currentTimeMillis() - activation.getCreatedAt()) / 1000);

        return null;
    }

    public String sendNewSMS(String value, String password, AuthVia via) {

        int code = randInt();
        String token = Utility.randomString(20);
        long now = System.currentTimeMillis();

        activationRepository.deleteByValueAndCreatedAt(value, now);

        Activation activation = new Activation();
        activation.setCode(code);
        activation.setToken(token);
        activation.setCreatedAt(now);
        activation.setValue(value);
        activation.setAuthVia(via);

        if (password != null)
            activation.setPassword(password);

        activationRepository.insert(activation);

//        if (via.equalsIgnoreCase(AuthVia.SMS.getName()))
//            Utility.sendSMS(code, value);
//        if (via.equals("email") || via.equals("both"))
//            Utility.sendMail(code + "", "verification code", "signUp", null);

        return token;
    }

    public String signUp(AuthVia via, String value) {

        boolean isNewPerson = true;

        if (
                (
                        via.equals(AuthVia.MAIL) &&
                                userRepository.findByMailCount(value) > 0
                ) ||
                        (
                                via.equals(AuthVia.SMS) &&
                                        userRepository.findByPhoneCount(value) > 0
                        )
        )
            isNewPerson = false;

        String password = isNewPerson ? "123456" : null;

        PairValue existTokenP = existSMS(value);

        if (existTokenP != null)
            return generateSuccessMsg(
                    "token", existTokenP.getKey(),
                    new PairValue("reminder", existTokenP.getValue())
            );

        String existToken = sendNewSMS(
                value, password, via
        );

        return generateSuccessMsg("token", existToken,
                new PairValue("reminder", SMS_RESEND_SEC)
        );

    }

    public String checkCode(ActivateRequest activateRequest) {

        if (activateRequest.getCode() < 1000 || activateRequest.getCode() > 9999)
            return JSON_NOT_VALID_PARAMS;

        Activation activation = activationRepository.findByValueAndTokenAndCode(
                activateRequest.getValue(),
                activateRequest.getToken(),
                activateRequest.getCode()
        );

        if (activation == null)
            return generateErr("کد وارد شده اشتباه است");

        if (activation.getCreatedAt() < System.currentTimeMillis() - SMS_VALIDATION_EXPIRATION_MSEC) {
            activationRepository.delete(activation);
            return generateErr("کد احراز هویت شما منقضی شده است");
        }

        if(activation.isUsed()) {
            activationRepository.delete(activation);
            return generateErr("این کد قبلا استفاده شده است");
        }

        activation.setUsed(true);
        activationRepository.save(activation);

        return generateSuccessMsg("needUsername", activation.getPassword() != null);
    }

    public String setUsername(SetUsernameRequest usernameRequest) {

        if (usernameRequest.getCode() < 1000 || usernameRequest.getCode() > 9999)
            return JSON_NOT_VALID_PARAMS;

        Activation activation = activationRepository.findByValueAndTokenAndCode(
                usernameRequest.getValue(),
                usernameRequest.getToken(),
                usernameRequest.getCode()
        );

        if (activation == null)
            return JSON_NOT_VALID_PARAMS;

        if (activation.getCreatedAt() < System.currentTimeMillis() - SMS_VALIDATION_EXPIRATION_MSEC) {
            activationRepository.delete(activation);
            return generateErr("کد احراز هویت شما منقضی شده است");
        }

        if(!activation.isUsed() || activation.getPassword() == null)
            return JSON_NOT_ACCESS;

        if (userRepository.findByUsername(usernameRequest.getUsername()).isPresent())
            return generateErr("نام کاربری وارد شده در سیستم موجود است");

        CommonUser user = new CommonUser();

        user.setUsername(usernameRequest.getUsername());
        user.setPassword(passwordEncoder.encode(activation.getPassword()));

        Set<String> roles = new HashSet<>();
        roles.add("USER");

        user.setRoles(roles);
        user.setActions(new Document().append("bookmarks", new ArrayList<>())
                .append("likes_or_dis", new ArrayList<>())
                .append("bookmarks", new ArrayList<>())
        );

        user.setInvitationCode(randInvitationCode());

        if (activation.getAuthVia().equals(AuthVia.SMS))
            user.setPhone(activation.getValue());
        else
            user.setEmail(activation.getValue());

        Avatar avatar = avatarRepository.findByDefault();
        if(avatar != null)
            user.setAvatar(avatar.getId());

        user.setId(new ObjectId());

        userRepository.insert(user);

        return JSON_OK;
    }

    public void signIn(
            HttpServletRequest request,
            HttpServletResponse response,
            LoginRequest loginRequest) {

        try {

            Activation activation = activationRepository.findByValueAndTokenAndCode(
                    loginRequest.getValue(),
                    loginRequest.getToken(),
                    loginRequest.getCode()
            );

            if (activation == null)
                return;

            if (activation.getCreatedAt() < System.currentTimeMillis() - SMS_VALIDATION_EXPIRATION_MSEC) {
                activationRepository.delete(activation);
                return;
            }

            if(!activation.isUsed())
                return;

            activationRepository.delete(activation);
            Optional<CommonUser> userOptional;

            if (activation.getAuthVia().equals(AuthVia.SMS))
                userOptional = userRepository.findByPhone(activation.getValue());
            else
                userOptional = userRepository.findByEmail(activation.getValue());

            if(userOptional.isEmpty())
                return;

            HttpResponse<JsonNode> res = Unirest.post(CAS_SERVER)
                    .queryString("grant_type", "password")
                    .queryString("username", userOptional.get().getUsername())
                    .queryString("password", "123456")
                    .basicAuth(CLIENT_ID, PASSWORD).asJson();

            System.out.println(res.getStatus());

            if (res.getStatus() != 200) {
                response.setHeader("Location", "https://" + request.getHeader("host") + "/cas/login?redirectUrl=" + loginRequest.getRedirectUrl() + "&callback=" + loginRequest.getCallback() + "&error");
                response.setStatus(302);
                return;
            }

            String token = res.getBody().getObject().getString("access_token");
            request.getSession().setAttribute("token", token);

            res = Unirest.post(loginRequest.getCallback())
                    .header("Content-Type", "application/json")
                    .body(new JSONObject()
                            .put("token", token)
                    ).asJson();

            System.out.println("callback res is = " +  res.getStatus());
            System.out.println("callback getStatusText is = " + res.getStatusText());

            if (res.getStatus() != 200) {
                return;
            }

            byte[] pubkeyder = Base64.getDecoder().decode(PUB_KEY);
            PublicKey pubkey;
            try {
                pubkey = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(pubkeyder));
                Claims claims = Jwts.parser().setSigningKey(pubkey).parseClaimsJws(token).getBody();

                String uuid = res.getBody().getObject().getString("data");
                uuids.add(new LoginController.UUID(uuid, token, claims.getExpiration().getTime()));

                response.setHeader("Location", loginRequest.getRedirectUrl() + "?uuid=" + uuid);
                response.setStatus(302);

            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }


        } catch (UnirestException e) {
            e.printStackTrace();
        }


    }

}
