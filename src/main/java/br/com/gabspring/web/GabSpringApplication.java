package br.com.gabspring.web;

import br.com.gabspring.annotations.GabController;
import br.com.gabspring.annotations.GabGetMethod;
import br.com.gabspring.annotations.GabPostMethod;
import br.com.gabspring.datastructures.ControllersMap;
import br.com.gabspring.datastructures.RequestControllerData;
import br.com.gabspring.explorer.ClassExplorer;
import br.com.gabspring.util.GabLogger;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.logging.Logger;

public class GabSpringApplication {
    public static void run(Class<?> sourceClass) {

        Logger.getLogger("org.apache").setLevel(java.util.logging.Level.OFF);

        long start, end;

        GabLogger.showBanner();
        try {
            start = System.currentTimeMillis();
            GabLogger.log("Embeded Web Container", "Starting GabSpringApplication...");
            Tomcat tomcat = new Tomcat();
            Connector connector = new Connector();
            connector.setPort(8080);

            extractMetadata(sourceClass);

            GabLogger.log("Embeded Web Container", "GabSpringApplication started on port " + connector.getPort());

            tomcat.setConnector(connector);

            Context context = tomcat.addContext("", new File(".").getAbsolutePath());
            Tomcat.addServlet(context, "GabSpringDispatchServlet", new GabSpringDispatchServlet());
            context.addServletMappingDecoded("/*", "GabSpringDispatchServlet");

            tomcat.start();
            end = System.currentTimeMillis();
            GabLogger.log("Embeded Web Container", "GabSpringApplication started in " + ((double) (end - start) / 1000) + " seconds");
            tomcat.getServer().await();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private static void extractMetadata(Class<?> sourceClass) throws Exception {

        final var allClasses = ClassExplorer.retrieveAllClasses(sourceClass);

        for (String gabClass : allClasses) {
            //GabLogger.log("ClassExplorer", "class found: " + gabClass);
            Annotation[] annotations = Class.forName(gabClass).getAnnotations();
            for (Annotation classAnnotation : annotations) {
                if (classAnnotation.annotationType().getName().contains("GabController")) {
                    GabLogger.log("Metadata Explorer", "Found a Controller " + gabClass);
                    extractMethod(gabClass);
                }
            }
        }
    }

    private static void extractMethod(String className) throws Exception {

        for (Method method : Class.forName(className).getDeclaredMethods()) {
            for (Annotation annotation: method.getAnnotations()){
                if (annotation.annotationType().getName().contains("GabGetMethod")) {
                    final var path = ((GabGetMethod)annotation).path();
                    //GabLogger.log("Metadata Explorer", "Found a Get Method " + method.getName() + " with path: " + path);

                    definedStructures("GET", path, className, method);
                }
                else if (annotation.annotationType().getName().contains("GabPostMethod")) {
                    final var path = ((GabPostMethod)annotation).path();
                    //GabLogger.log("Metadata Explorer", "Found a Post Method " + method.getName() + " with path: " + path);

                    definedStructures("POST", path, className, method);
                }
            }
        }

        for (RequestControllerData requestControllerData : ControllersMap.values.values()) {
            GabLogger.log("", String.format("%s : %s [%s.%s]", requestControllerData.method(), requestControllerData.path(), requestControllerData.controllerClass(), requestControllerData.controllerMethod()));
        }
    }

    private static void definedStructures(String httpMethod, String path, String className, Method method) {
        final var requestControllerData = new RequestControllerData(httpMethod, path, className, method.getName());
        ControllersMap.values.put(String.format("%s%s", httpMethod, path), requestControllerData);
    }
}
