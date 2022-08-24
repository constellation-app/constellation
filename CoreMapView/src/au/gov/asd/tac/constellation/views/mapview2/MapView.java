/*
 * Copyright 2010-2022 Australian Signals Directorate
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package au.gov.asd.tac.constellation.views.mapview2;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.application.Platform;
import javafx.beans.InvalidationListener;
import javafx.beans.Observable;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.geometry.Rectangle2D;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

/**
 *
 * @author altair1673
 */
public class MapView extends ScrollPane {

    private final StackPane mapStackPane;
    private final Image mapImage;
    private final ImageView mapDisplay;
    private final List<String> countryDrawings = new ArrayList<String>();

    private final Canvas mapCanvas;
    //private WebEngine webEngine;
    //private WebView webView;
    private static final Logger LOGGER = Logger.getLogger("Test");

    private final DoubleProperty zoomProperty = new SimpleDoubleProperty(200);

    public MapView() {
        LOGGER.log(Level.SEVERE, "In MapView constructor");

        mapImage = new Image("C:\\Projects\\constellation\\CoreMapView\\src\\au\\gov\\asd\\tac\\constellation\\views\\mapview\\resources\\world-map.jpg");
        LOGGER.log(Level.SEVERE, "Size of country array: " + countryDrawings.size());
        SVGPath worldMapSVG = new SVGPath();

        worldMapSVG.setContent("m 479.68275,331.6274 -0.077,0.025 -0.258,0.155 -0.147,0.054 -0.134,0.027 -0.105,-0.011 -0.058,-0.091 0.006,-0.139 -0.024,-0.124 -0.02,-0.067 0.038,-0.181 0.086,-0.097 0.119,-0.08 0.188,0.029 0.398,0.116 0.083,0.109 10e-4,0.072 -0.073,0.119 z");
        worldMapSVG.setFill(Color.web("#81c483"));

        worldMapSVG.setScaleX(1009.6727);
        worldMapSVG.setScaleY(665.96301);

        mapCanvas = new Canvas(1009, 665);
        //mapCanvas.setScaleX(1009);
        //mapCanvas.setScaleY(665);
        GraphicsContext gc = mapCanvas.getGraphicsContext2D();
        setUp(gc);

        //draw(gc, "m 479.68275,331.6274 -0.077,0.025 -0.258,0.155 -0.147,0.054 -0.134,0.027 -0.105,-0.011 -0.058,-0.091 0.006,-0.139 -0.024,-0.124 -0.02,-0.067 0.038,-0.181 0.086,-0.097 0.119,-0.08 0.188,0.029 0.398,0.116 0.083,0.109 10e-4,0.072 -0.073,0.119 z");
        //Platform.runLater(() -> {
            //gc.beginPath();
            //gc.setFill(Color.RED);
            //gc.setStroke(Color.BLUE);
            //gc.scale(1.85, 1.85);
            //countryDrawings.forEach(path -> {
                //path = String.toUpperCase(path);
            draw(gc, "");

        //});

            //gc.fill();
            //gc.stroke();

        //});

        mapStackPane = new StackPane();
        mapDisplay = new ImageView(mapImage);
        mapStackPane.getChildren().add(mapCanvas);

        this.setPannable(true);
        this.setPrefSize(1009.6727, 665.96301);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        setContent(mapStackPane);

        /*zoomProperty.addListener(new InvalidationListener() {
            @Override
            public void invalidated(Observable observable) {
                mapDisplay.setFitWidth(zoomProperty.get() * 4);
                mapDisplay.setFitHeight(zoomProperty.get() * 3);
            }

        });

        addEventFilter(ScrollEvent.ANY, new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent event) {
                if (event.getDeltaY() > 0) {
                    zoomProperty.set(zoomProperty.get() * 1.1);
                } else if (event.getDeltaY() < 0) {
                    zoomProperty.set(zoomProperty.get() / 1.1);
                }

            }

        });*/


        //this.setHvalue(this.getHmin() + (this.getHmax() - this.getHmin()) / 2);
        //this.setVvalue(this.getVmin() + (this.getVmax() - this.getVmin()) / 2);

    }

    private void draw(GraphicsContext gc, String svgPath) {
        //svgPath = svgPath.toUpperCase();
        //gc.scale(1.5, 1.5);
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.WHITE);
        //gc.appendSVGPath(svgPath);
        gc.fill();
        gc.stroke();
    }


    private void setUp(GraphicsContext gc) {
        //gc.appendSVGPath("M 0 0 L 1 1 L 2 2 z");

        try {
            try (BufferedReader bFileReader = new BufferedReader(new FileReader("C:\\Projects\\constellation\\CoreMapView\\src\\au\\gov\\asd\\tac\\constellation\\views\\mapView\\resources\\mapSVGPaths.txt"))) {
                String line = "";
                String path = "";
                String lineEnds = " z m";
                SVGPath p = new SVGPath();
                while ((line = bFileReader.readLine()) != null) {
                    //LOGGER.log(Level.SEVERE, "test1\ntest2");
                    line = line.strip();
                    if (line.startsWith("d=")) {


                        path = line.substring(4, line.length() - 1);
                        path = "M" + path;
                        int index = path.indexOf(' ', 5);
                        path = path.substring(0, index) + lineEnds + path.substring(index + 1);

                        String pathWithLines = "";
                        boolean firstN = true;


                        /*for (int i = 0; i < path.length(); ++i) {

                            if (path.charAt(i) == 'm') {
                                if (firstN) {
                                    //pathWithLines += "M";

                                    firstN = false;
                                    //continue;
                                } else {
                                    pathWithLines = pathWithLines.trim();

                                    p.setContent(pathWithLines);
                                    gc.appendSVGPath(p.getContent());
                                    LOGGER.log(Level.SEVERE, "Path : " + p.getContent());

                                    pathWithLines = "";
                                }

                            }

                            pathWithLines += path.charAt(i);
                        }*/
                        path = path.strip();
                        p.setContent(path);
                        gc.appendSVGPath(p.getContent());
                        //path = path.replaceFirst("^", "M");
                        //pathWithLines = pathWithLines.trim();
                        LOGGER.log(Level.SEVERE, "path : " + p.getContent());
                        //LOGGER.log(Level.SEVERE, "AUSTRALIA!!");
                        //countryDrawings.add(p.getContent());
                        path = "";

                    }

                }
            }
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, e.getMessage());
        }
    }

    /*private String getImgElement() {
        String img = "";

        File imgFiles = new File("C:\\Projects\\constellation\\CoreMapView\\src\\au\\gov\\asd\\tac\\constellation\\views\\mapview\\resources\\world.svg");

        img += "<img src=\"" + imgFiles.toURI() + "\" width=950 height=740/>";

        LOGGER.log(Level.SEVERE, imgFiles.toURI() + "");
        return img;

    }*/

}
