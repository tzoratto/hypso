package com.tzoratto.hypso;

import com.tzoratto.fayaclient.FayaInstance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Response;


import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

import static spark.Spark.*;

public class Hypso {
    private static final Logger LOGGER = LoggerFactory.getLogger(Hypso.class);

    private static final String SCRIPT = System.getenv("SCRIPT");
    private static final String PORT = System.getenv("PORT");
    private static final String FAYA_URL = System.getenv("FAYA_URL");
    private static final String FAYA_API_KEY = System.getenv("FAYA_API_KEY");
    private static final String FAYA_NAMESPACE = System.getenv("FAYA_NAMESPACE");
    private static final FayaInstance FAYA_INSTANCE = new FayaInstance(FAYA_URL, FAYA_API_KEY);


    public static void main(String[] args) {
        checkConfig();

        if (PORT != null) {
            port(Integer.parseInt(PORT));
        }

        post("/trigger", (req, res) -> {
            try {
                LOGGER.info("Script execution triggered. Checking token...");

                String token = req.queryParams("token");
                if (token != null && FAYA_INSTANCE.check(token, FAYA_NAMESPACE)) {
                    LOGGER.info("Valid token. Executing script " + SCRIPT);
                    return executeScript(res);
                } else {
                    return forbidden(res, "Invalid token \"" + token +"\". " + req.ip() + " " + req.userAgent());
                }
            } catch (Exception e) {
                LOGGER.error("Something went wrong.", e);
                return error(res, null);
            }
        });
    }

    private static String executeScript(Response res) {
        Runtime rt = Runtime.getRuntime();
        Process pr;
        int exitCode;
        try {
            pr = rt.exec(SCRIPT);
            logScriptOutput(pr);
            exitCode = pr.waitFor();
        } catch (Exception e) {
            return error(res, "Error during script execution. Details : " + e.getMessage());
        }
        if (exitCode != 0) {
            return error(res, "Error during script execution. Exit code : " + exitCode);
        } else {
            return success(res, "Script execution is complete");
        }
    }

    private static String forbidden(Response res, String message) {
        if (message != null) {
            LOGGER.warn(message);
        }
        res.status(403);
        return "fail";
    }

    private static String error(Response res, String message) {
        if (message != null) {
            LOGGER.error(message);
        }
        res.status(500);
        return "fail";
    }

    private static String success(Response res, String message) {
        if (message != null) {
            LOGGER.info(message);
        }
        res.status(200);
        return "success";
    }

    private static void logScriptOutput(Process pr) {
        Stream.of(pr.getErrorStream(), pr.getInputStream()).parallel().forEach((InputStream is) -> {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                br.lines().forEach(LOGGER::info);
            } catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        });
    }

    private static void checkConfig() {
        LOGGER.info("Script path : " + SCRIPT);
        Path path = Paths.get(SCRIPT);
        if (!Files.exists(path)) {
            LOGGER.error("Script file not found");
        } else if (!Files.isExecutable(path)) {
            LOGGER.error("Script file not executable");
        }
        if (FAYA_NAMESPACE == null) {
            LOGGER.error("Faya namespace not set");
        }
    }
}
