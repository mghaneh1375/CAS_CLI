package bogen.studio.cas_client.Utility;

import bogen.studio.cas_client.Kavenegar.KavenegarApi;
import bogen.studio.cas_client.Kavenegar.excepctions.ApiException;
import bogen.studio.cas_client.Kavenegar.excepctions.HttpException;
import bogen.studio.cas_client.Kavenegar.models.SendResult;
import my.common.commonkoochita.Validator.PhoneValidator;
import org.springframework.beans.factory.annotation.Value;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Random;
import java.util.regex.Pattern;

import static my.common.commonkoochita.Utility.Utility.convertPersianDigits;

public class Utility {
    static Boolean DEV_MODE = true;

    private static Random random = new Random();

    private static final Pattern mailPattern = Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);

    public static boolean isValidMail(String in) {
        return mailPattern.matcher(convertPersianDigits(in)).matches();
    }

    public static String getDomainName(String url) throws URISyntaxException {
        URI uri = new URI(url);
        String domain = uri.getHost();
        if(domain == null)
            return "";
        return domain.startsWith("www.") ? domain.substring(4) : domain;
    }

    public static int randInt() {

        if (DEV_MODE)
            return 1111;

        int r = 0;
        for (int i = 0; i < 4; i++) {
            int x = random.nextInt(10);

            while (x == 0)
                x = random.nextInt(10);

            r += x * Math.pow(10, i);
        }

        return r;
    }

    public static int randInvitationCode() {

        int r = 0;
        for (int i = 0; i < 8; i++) {

            int x = random.nextInt(10);

            while (x == 0)
                x = random.nextInt(10);

            r += x * Math.pow(10, i);
        }

        return r;
    }

    public static boolean sendSMS(String receptor, String token,
                                  String token2, String token3,
                                  String template
    ) {

        if(DEV_MODE)
            return true;

        receptor = convertPersianDigits(receptor);

        if(!PhoneValidator.isValid(receptor)) {
            System.out.println("not valid phone num");
            return false;
        }

        try {
            KavenegarApi api = new KavenegarApi("4836666C696247676762504666386A336846366163773D3D");
            SendResult Result = api.verifyLookup(receptor, token, token2, token3, template);

            if(Result.getStatus() == 6 ||
                    Result.getStatus() == 11 ||
                    Result.getStatus() == 13 ||
                    Result.getStatus() == 14 ||
                    Result.getStatus() == 100
            )
                return false;

            return true;
        } catch (HttpException ex) {
            System.out.print("HttpException  : " + ex.getMessage());
        } catch (ApiException ex) {
            System.out.print("ApiException : " + ex.getMessage());
        }

        return false;
    }
}
