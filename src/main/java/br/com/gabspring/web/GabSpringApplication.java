package br.com.gabspring.web;

import br.com.gabspring.explorer.ClassExplorer;
import br.com.gabspring.util.GabLogger;
import org.apache.catalina.Context;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;

import java.io.File;
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

            final var allClasses = ClassExplorer.retrieveAllClasses(sourceClass);

            for (String s : allClasses) {
                GabLogger.log("ClassExplorer", "class found: " + s);
            }

            GabLogger.log("Embeded Web Container", "GabSpringApplication started on port " + connector.getPort());

            tomcat.setConnector(connector);

            Context context = tomcat.addContext("", new File(".").getAbsolutePath());
            Tomcat.addServlet(context, "GabSpringDispatchServlet", new GabSpringDispatchServlet());
            context.addServletMappingDecoded("/*", "GabSpringDispatchServlet");

            tomcat.start();
            end = System.currentTimeMillis();
            GabLogger.log("Embeded Web Container", "GabSpringApplication started in " + ((double) (end - start)/1000) + " seconds");
            tomcat.getServer().await();
        } catch (Exception e) {
            e.fillInStackTrace();
        }
    }
}
