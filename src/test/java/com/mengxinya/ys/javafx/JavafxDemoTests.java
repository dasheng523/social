package com.mengxinya.ys.javafx;

import com.sun.webkit.network.CookieManager;
import javafx.application.Application;
import javafx.concurrent.Worker;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Stage;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.CookieHandler;
import java.net.URI;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
            CookieManager cookieManager = new CookieManager();
            CookieHandler.setDefault(cookieManager);

            webEngine.getLoadWorker().stateProperty().addListener((ov, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    CookieHandler handler = CookieHandler.getDefault();
                    ov.addListener((ov2, old, state) -> {
                        try {
                            if (Worker.State.SUCCEEDED.equals(state)) {
                                Map<String, List<String>> cookies = handler.get(URI.create("https://www.zhihu.com"), new HashMap<>());
                                System.out.println(cookies);
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    });
                }
            });

            webEngine.load("https://www.zhihu.com");
            VBox vBox = new VBox(webView);
            Scene scene = new Scene(vBox, 960, 600);
            primaryStage.setScene(scene);
            primaryStage.show();
        }
    }
}
