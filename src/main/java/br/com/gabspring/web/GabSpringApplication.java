package br.com.gabspring.web;

import br.com.gabspring.annotations.GabController;
import br.com.gabspring.annotations.GabGetMethod;
import br.com.gabspring.annotations.GabPostMethod;
import br.com.gabspring.annotations.GabService;
import br.com.gabspring.datastructures.ControllersMap;
import br.com.gabspring.datastructures.RequestControllerData;
import br.com.gabspring.datastructures.ServiceImplementationMap;
import br.com.gabspring.explorer.ClassExplorer;
import br.com.gabspring.util.GabLogger;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.logging.Logger;

import static java.lang.String.format;

public class GabSpringApplication {
    public static void run(Class<?> sourceClass) {

        disableLogOrgApache();

        long start, end;

        GabLogger.showBanner();

        try {
            start = System.currentTimeMillis();
            GabLogger.log("Embeded Web Container", "Starting GabSpringApplication...");
            final var tomcat = new Tomcat();
            final var connector = new Connector();
            connector.setPort(8080);

            extractMetadata(sourceClass);

            GabLogger.log("Embeded Web Container", format("GabSpringApplication started on port %d", connector.getPort()));

            tomcat.setConnector(connector);

            final var context = tomcat.addContext("", new File(".").getAbsolutePath());
            Tomcat.addServlet(context, GabSpringDispatchServlet.class.getName(), new GabSpringDispatchServlet());
            context.addServletMappingDecoded("/*", GabSpringDispatchServlet.class.getName());

            tomcat.start();
            end = System.currentTimeMillis();
            GabLogger.log("Embeded Web Container", format("GabSpringApplication started in %.2f seconds", (double) (end - start) / 1000));
            tomcat.getServer().await();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }

    private static void disableLogOrgApache() {
        Logger.getLogger("org.apache").setLevel(java.util.logging.Level.OFF);
    }

    private static void extractMetadata(Class<?> sourceClass) throws Exception {

        final var allClasses = ClassExplorer.retrieveAllClasses(sourceClass);

        for (String gabClass : allClasses) {
            //GabLogger.log("ClassExplorer", "class found: " + gabClass);
            Annotation[] annotations = Class.forName(gabClass).getAnnotations();
            for (Annotation classAnnotation : annotations) {
                switch (classAnnotation.annotationType().getSimpleName()) {
                    case "GabController":
                        GabLogger.log("Metadata Explorer", format("Found a Controller %s", gabClass));
                        extractMethod(gabClass);
                        break;
                    case "GabService":
                        GabLogger.log("Metadata Explorer", format("Found a Service %s", gabClass));
                        Arrays.stream(Class.forName(gabClass).getInterfaces())
                                .peek(interface_ -> GabLogger.log("Metadata Explorer", format("Class implements %s", interface_.getName())))
                                .forEach(interface_ -> ServiceImplementationMap.implementations.put(interface_.getName(), gabClass));
                        break;
                    default:
                        GabLogger.log("Metadata Explorer", format("Class Not supported %s %s", gabClass, classAnnotation.annotationType().getName()));
                        break;
                }
            }
        }
    }

    private static void extractMethod(String className) throws Exception {

        Arrays.stream(Class.forName(className).getDeclaredMethods())
                .forEach(method -> Arrays.stream(method.getAnnotations())
                        .map(annotation -> annotation.annotationType().getSimpleName())
                        .forEach(annotationType -> {
                            switch (annotationType) {
                                case "GabGetMethod":
                                    final var getPath = method.getAnnotation(GabGetMethod.class).path();
                                    definedStructures("GET", getPath, className, method);
                                    break;
                                case "GabPostMethod":
                                    final var postPath = method.getAnnotation(GabPostMethod.class).path();
                                    definedStructures("POST", postPath, className, method);
                                    break;
                            }
                        }));

        ControllersMap.values.values()
                .forEach(requestControllerData -> GabLogger.log("", format("%s : %s [%s.%s]",
                        requestControllerData.method(),
                        requestControllerData.path(),
                        requestControllerData.controllerClass(),
                        requestControllerData.controllerMethod())));
    }

    private static void definedStructures(String httpMethod, String path, String className, Method method) {
        final var requestControllerData = new RequestControllerData(httpMethod, path, className, method.getName());
        ControllersMap.values.put(format("%s%s", httpMethod, path), requestControllerData);
    }
}
