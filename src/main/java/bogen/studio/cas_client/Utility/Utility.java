package bogen.studio.cas_client.Utility;

import org.springframework.beans.factory.annotation.Value;

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
}
