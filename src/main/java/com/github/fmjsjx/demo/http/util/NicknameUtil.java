package com.github.fmjsjx.demo.http.util;

import java.util.LinkedHashSet;
import java.util.Scanner;

import com.github.fmjsjx.libcommon.util.RandomUtil;

public class NicknameUtil {

    private static final String[] nicknames = loadNicknames();

    private static final String[] loadNicknames() {
        try (var in = new Scanner(NicknameUtil.class.getResourceAsStream("/nicknames.txt"))) {
            var set = new LinkedHashSet<String>(1 << 17);
            for (; in.hasNext();) {
                var line = in.nextLine().trim();
                if (!line.isBlank()) {
                    set.add(line.intern());
                }
            }
            return set.stream().toArray(String[]::new);
        }
    }

    public static final String randomNickname() {
        return RandomUtil.randomOne(nicknames);
    }

    public static final String randomNickname(int guestRate) {
        return randomNickname(guestRate, "用户");
    }

    public static final String randomNickname(int guestRate, String guestPrefix) {
        if (RandomUtil.randomInt(100) < guestRate) {
            var number = RandomUtil.randomInRange(1_000_000_000, 1_999_999_999);
            return guestPrefix + (number / 7) + "****" + (number % 1_000);
        }
        var nickname = RandomUtil.randomOne(nicknames);
        return mask(nickname);
    }

    public static final String mask(String nickname) {
        switch (nickname.length()) {
        case 0:
        case 1:
            return nickname;
        case 2:
            return nickname.substring(0, 1) + "*";
        case 3:
            return nickname.substring(0, 1) + "*" + nickname.substring(2);
        case 4:
            return nickname.substring(0, 1) + "**" + nickname.substring(3);
        case 5:
            return nickname.substring(0, 2) + "**" + nickname.substring(3);
        case 6:
            return nickname.substring(0, 2) + "**" + nickname.substring(4);
        case 7:
            return nickname.substring(0, 2) + "***" + nickname.substring(5);
        case 8:
            return nickname.substring(0, 3) + "***" + nickname.substring(6);
        case 9:
            return nickname.substring(0, 3) + "***" + nickname.substring(6);
        }
        var maskSize = (nickname.length() + 2) / 3;
        return nickname.substring(0, maskSize) + "*".repeat(maskSize) + nickname.substring(maskSize << 1);
    }

    private NicknameUtil() {
    }

}
