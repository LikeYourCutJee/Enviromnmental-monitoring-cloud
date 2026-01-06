package com.env.events.events_service.config.properties;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Configuration
@ConfigurationProperties("emqx")
public class EmqxProperties {

    private boolean enable = false;
    private String host = "https://127.0.0.1:18084";

    private Api api = new Api();
    private Mqtt mqtt = new Mqtt();

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Api {
        private String key = "";
        private String secret = "";
    }

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Mqtt {
        private String host = "tcp://127.0.0.1:1883";
        private Client client = new Client();
        private String username = "";
        private String password = "";
        private Request request = new Request();
        private Reply reply = new Reply();
        private int qos = 1;
        private Timeout timeout = new Timeout();

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Client {
            private String id = "app-default";
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Request {
            private Topic topic = new Topic();

            @Getter
            @Setter
            @NoArgsConstructor
            @AllArgsConstructor
            public static class Topic {
                private String tpl = "devices/%s/rpc/request";
            }
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Reply {
            private String prefix = "reply";
        }

        @Getter
        @Setter
        @NoArgsConstructor
        @AllArgsConstructor
        public static class Timeout {
            private long seconds = 30;
        }
    }
}