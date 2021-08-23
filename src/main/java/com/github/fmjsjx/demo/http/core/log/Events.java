package com.github.fmjsjx.demo.http.core.log;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class Events {

    public static final class Auth {

        public static final String ACCOUNT = "auth.account";

        public static final String LOGIN = "auth.login";

    }

    public static final class Videos {

        private static final ConcurrentMap<String, String> bonusEvents = new ConcurrentHashMap<String, String>();

        public static final String bonus(String name) {
            return bonusEvents.computeIfAbsent(name, k -> ("videos.bonus_" + name).intern());
        }

    }

    private Events() {
    }

}
