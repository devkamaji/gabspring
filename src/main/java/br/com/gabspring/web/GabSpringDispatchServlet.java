package br.com.gabspring.web;

import br.com.gabspring.annotations.GabBody;
import br.com.gabspring.annotations.GabInjected;
import br.com.gabspring.datastructures.ControllerInstances;
import br.com.gabspring.datastructures.ControllersMap;
import br.com.gabspring.datastructures.DependencyInjectionMap;
import br.com.gabspring.datastructures.ServiceImplementationMap;
import br.com.gabspring.util.GabLogger;
import com.google.gson.Gson;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;

public class GabSpringDispatchServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // ignore favicon.ico requests
        if (req.getRequestURL().toString().endsWith("/favicon.ico")) {
            return;
        }

        PrintWriter out = new PrintWriter(resp.getWriter());
        Gson gson = new Gson();

        final var url = req.getRequestURI();
        final var httpMethod = req.getMethod().toUpperCase();
        final var key = String.format("%s%s", httpMethod, url);
        final var requestControllerData = ControllersMap.values.get(key);

        GabLogger.log("GabDispatchServlet", String.format("URL: %s (%s) - Handler %s.%s", url, httpMethod, requestControllerData.controllerClass(), requestControllerData.controllerMethod()));

        Object controller;
        try {
            GabLogger.log("GabDispatchServlet", "Searching for controller instances");
            controller = ControllerInstances.instances.get(requestControllerData.controllerClass());
            if (controller == null) {
                GabLogger.log("GabDispatchServlet", "Creating new controller instance");
                controller = Class.forName(requestControllerData.controllerClass()).getDeclaredConstructor().newInstance();

                ControllerInstances.instances.put(requestControllerData.controllerClass(), controller);
                injectDependencies(controller);
            }

            Method controllerMethod = null;
            for (Method method : controller.getClass().getMethods()) {
                if (method.getName().equals(requestControllerData.controllerMethod())) {
                    controllerMethod = method;
                    break;
                }
            }

            if (controllerMethod.getParameterCount() > 0) {
                GabLogger.log("GabDispatchServlet", "Method " + controllerMethod.getName() + " has parameters");
                Object arg;
                final var parameter = controllerMethod.getParameters()[0];
                if (parameter.getAnnotations()[0].annotationType().getName().contains(GabBody.class.getName())) {
                    GabLogger.log("GabDispatchServlet", "Found parameter from request of type " + parameter.getType().getName());
                    final var body = readBytesFromRequest(req);
                    GabLogger.log("GabDispatchServlet", "Parameter content: " + body);
                    arg = gson.fromJson(body, parameter.getType());

                    out.println(gson.toJson(controllerMethod.invoke(controller, arg)));
                }
                out.close();
                return;
            }

            GabLogger.log("GabDispatchServlet", String.format("Invoking controller method %s to handle request", controllerMethod.getName()));

            out.println(gson.toJson(controllerMethod.invoke(controller)));
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void injectDependencies(Object controller) {
        Arrays.stream(controller.getClass().getDeclaredFields()).forEach(attr -> {
            final var attrType = attr.getType().getName();
            GabLogger.log("GabDispatchServlet", String.format("Injecting dependencies for field %s", attrType));
            Object serviceImpl;

            if (DependencyInjectionMap.values.get(attrType) == null) {
                GabLogger.log("GabDispatchServlet", String.format("Couldn't find instance for %s", attrType));
                final var implType = ServiceImplementationMap.implementations.get(attrType);
                if (implType != null) {
                    GabLogger.log("GabDispatchServlet", String.format("Found instance for %s", implType));
                    serviceImpl = DependencyInjectionMap.values.get(implType);
                    if (serviceImpl == null) {
                        GabLogger.log("GabDispatchServlet", "Injecting new Object");
                        try {
                            serviceImpl = Class.forName(implType).getDeclaredConstructor().newInstance();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        DependencyInjectionMap.values.put(implType, serviceImpl);
                    }

                    attr.setAccessible(true);
                    try {
                        attr.set(controller, serviceImpl);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    GabLogger.log("GabDispatchServlet", "Injected Object successfully");
                }
            }
        });
    }

    private String readBytesFromRequest(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(req.getInputStream()));
        while ((line = reader.readLine()) != null) {
            sb.append(line);
        }
        return sb.toString();
    }
}
