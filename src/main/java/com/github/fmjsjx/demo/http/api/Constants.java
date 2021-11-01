package com.github.fmjsjx.demo.http.api;

public final class Constants {

    public static final class Partners {

        /**
         * 无平台（游客） {@code 0}
         */
        public static final int NONE = 0;

        /**
         * 微信 {@code 1}
         */
        public static final int WECHAT = 1;

        /**
         * 苹果 {@code 2}
         */
        public static final int APPLE = 2;

        private Partners() {
        }
    }

    public static final class SourceIds {

        private SourceIds() {
        }
    }

    public static final class ClientFeatures {

        public static final String CLIENT_ARCODE = "client_arcode";

        private ClientFeatures() {
        }
    }

    public static final class QuestStatuses {

        public static final int INACTIVE = -1;

        public static final int IN_PROGRESS = 0;

        public static final int COMPLETED = 1;

        public static final int FINISHED = 2;

    }

    public static final class Events {

        public static final String CROSS_DAY = "cross_day";

    }

    public static final class ItemIds {

        // 100001 ~ 109999 为货币或资源

        /**
         * {@code 100001} 金币
         */
        public static final int COIN = 100001;
        /**
         * {@code 100002} 钻石
         */
        public static final int DIAMOND = 100002;
        /**
         * {@code 100011} 随机金币
         */
        public static final int RANDOM_COIN = 100011;

        private ItemIds() {
        }

    }

    private Constants() {
    }

}
