package br.com.gabspring.datastructures;

public record RequestControllerData(
        String method,
        String path,
        String controllerClass,
        String controllerMethod
){}