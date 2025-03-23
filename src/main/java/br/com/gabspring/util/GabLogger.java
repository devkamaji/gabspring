package br.com.gabspring.util;

import java.time.format.DateTimeFormatter;

import static java.time.LocalDateTime.now;

public class GabLogger {
    public static final String GREEN = "\u001B[32m";
    public static final String YELLOW = "\u001B[33m";
    public static final String WHITE = "\u001B[37m";
    public static final String RESET = "\u001B[0m";

    public static DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

    public static void log(String module, String message) {
        System.out.printf(GREEN + "%15S " + YELLOW + "%-30s:" + WHITE + " %s\n" + RESET, now().format(formatter), module, message);
    }

    public static void showBanner() {
        System.out.println(YELLOW);
        System.out.println("____       _________    ____ _____ ____  ____  _____   ________   ____  ");
        System.out.println("\\ \\ \\     / ____/   |  / __ ) ___// __ \\/ __ \\/  _/ | / / ____/   \\ \\ \\ ");
        System.out.println(" \\ \\ \\   / / __/ /| | / __  \\__ \\/ /_/ / /_/ // //  |/ / / __      \\ \\ \\");
        System.out.println(" / / /  / /_/ / ___ |/ /_/ /__/ / ____/ _, _// // /|  / /_/ /      / / /");
        System.out.println("/_/_/   \\____/_/  |_/_____/____/_/   /_/ |_/___/_/ |_/\\____/      /_/_/ ");
        System.out.println("=========================================================================");
        System.out.println(RESET);
    }
}
