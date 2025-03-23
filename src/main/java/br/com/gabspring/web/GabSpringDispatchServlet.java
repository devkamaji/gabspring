package br.com.gabspring.web;

import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

public class GabSpringDispatchServlet extends HttpServlet {

    @Override
    protected void service(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        // ignore favicon.ico requests
        if (req.getRequestURL().toString().endsWith("/favicon.ico")) {
            return;
        }

        System.out.println("Received request: " + req.getMethod() + " " + req.getRequestURI());
        resp.getWriter().write("Hello from GabSpringDispatchServlet!");
    }
}
