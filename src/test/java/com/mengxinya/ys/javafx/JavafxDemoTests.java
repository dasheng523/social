package com.mengxinya.ys.javafx;

import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import netscape.javascript.JSObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

@Slf4j
public class JavafxDemoTests {

    @Test
    public void testRun() {
        Application.launch(JavaFxDemo.class);
    }

    public static class JavaFxDemo extends Application {

        public JavaFxDemo() {}

        @Override
        public void start(Stage primaryStage) throws Exception {
            WebView webView = new WebView();
            WebEngine webEngine = webView.getEngine();
            webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:99.0) Gecko/20100101 Firefox/99.0");

            webEngine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    System.out.println(webEngine.getLocation());
                    waitForElementExist(webEngine, "div.SignFlowHomepage", jsObject -> {
                        CookieHandler handler = CookieHandler.getDefault();
                        try {
                            List<String> cookies = handler.get(URI.create("https://www.zhihu.com/signin?next=%2F"), new HashMap<>()).get("Cookie");
                            if (cookies != null) {
                                System.out.println(cookies.get(0));
                            }
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    });
                }
            });

            webEngine.setOnAlert(System.out::println);

            webEngine.load("https://www.zhihu.com");

            StackPane pane = new StackPane();
            pane.getChildren().add(webView);

            Scene scene = new Scene(pane, 960, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        }

        protected void waitForElementExist(WebEngine webEngine, String cssSelector, Consumer<JSObject> consumer) {
            waitForElementExist(webEngine, cssSelector, consumer, 10 * 1000, (jsObject) -> {});
        }
        protected void waitForElementExist(WebEngine webEngine, String cssSelector, Consumer<JSObject> consumer, long timeoutMs, Consumer<JSObject> timeoutCb) {
            JSObject window = (JSObject) webEngine.executeScript("window");
            String contextKey = "javafx" + uuidStr();
            window.setMember(contextKey, new JsContext(consumer));
            String timeoutKey = "timeoutKey" + uuidStr();
            window.setMember(timeoutKey, new JsContext(timeoutCb));
            String timerKey = "timer" + uuidStr();
            String js = """
                    var $(timer) = setInterval(
                        function() {
                            const elem = document.querySelector('$(cssSelector)');
                            if (elem) {
                                alert($(contextKey))
                                try {
                                    $(contextKey).callback(elem);
                                } catch (e) {
                                    alert(e)
                                }
                                
                                clearInterval($(timer));
                            }
                        },
                        300
                    );
                    setTimeout(
                        function() {
                            $(timerKey).callback(window.document);
                            clearInterval($(timer));
                        },
                        $(timeoutMs)
                    );
                    """;

            String script = format(js, new HashMap<>() {{
                put("timer", timerKey);
                put("cssSelector", cssSelector);
                put("contextKey", contextKey);
                put("timerKey", timeoutKey);
                put("timeoutMs", timeoutMs + "");
            }});
            webEngine.executeScript(script);
        }

        private String uuidStr() {
            return UUID.randomUUID().toString().replaceAll("-", "");
        }


        public record JsContext(Consumer<JSObject> consumer) {
            public void callback(JSObject jsObject) {
                consumer.accept(jsObject);
            }
        }
    }


    public static String format(String str, Map<String, String> params) {
        return params.keySet().stream().reduce(str, (init, key) -> init.replace("$(" + key + ")", params.get(key)));
    }

    @Test
    public void testFormat() {
        String js = "sdfsdf$(b) fsadf$(b)";
        Assertions.assertEquals("sdfsdf666 fsadf666", format(js, new HashMap<>() {{ put("b", "666"); }}));

        String js2 = "sdfsdf$(b) fsadf$(b)$(a)";
        Assertions.assertEquals("sdfsdf666 fsadf666888", format(js2, new HashMap<>() {{ put("b", "666"); put("a", "888"); }}));
    }
}
