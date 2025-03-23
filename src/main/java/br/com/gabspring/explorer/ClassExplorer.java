package br.com.gabspring.explorer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static java.lang.String.format;

public class ClassExplorer {

    public static List<String> retrieveAllClasses(Class<?> sourceClass) {
        return packageExplorer(sourceClass.getPackageName());
    }

    public static List<String> packageExplorer(String packageName) {
        final var classNames = new ArrayList<String>();
        try {
            final var inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(packageName.replaceAll("\\.", "/"));
            final var reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.endsWith(".class")) {
                    classNames.add(format("%s.%s", packageName, line.substring(0, line.indexOf(".class"))));
                } else {
                    classNames.addAll(packageExplorer(format("%s.%s", packageName, line)));
                }
            }
            return classNames;
        } catch (Exception e) {
            e.fillInStackTrace();
            return null;
        }
    }
}
