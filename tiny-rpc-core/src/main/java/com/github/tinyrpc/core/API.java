package com.github.tinyrpc.core;

public @interface API {
    String host();

    int port();

    String api() default "";
}
