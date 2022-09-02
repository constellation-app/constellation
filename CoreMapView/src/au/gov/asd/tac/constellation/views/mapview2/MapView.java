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
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.ScrollEvent;
import javafx.scene.layout.Background;
import javafx.scene.layout.Region;
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

    private Canvas mapCanvas;
    private Group countryGroup;
    private GraphicsContext gc;
    //private WebEngine webEngine;
    //private WebView webView;
    private static final Logger LOGGER = Logger.getLogger("Test");

    private final DoubleProperty zoomProperty = new SimpleDoubleProperty(1.0);

    private final double mapScaleFactor = 1.1;

    private double scale = 1.0;
    //private final SVGPath p = new SVGPath();
    private final List<SVGPath> countrySVGPaths = new ArrayList<>();

    private double mouseAnchorX;
    private double mouseAnchorY;
    private double transalateX;
    private double transalateY;

    public MapView() {
        LOGGER.log(Level.SEVERE, "In MapView constructor");

        mapImage = new Image("C:\\Projects\\constellation\\CoreMapView\\src\\au\\gov\\asd\\tac\\constellation\\views\\mapview\\resources\\world-map.jpg");
        countryGroup = new Group();
        SVGPath worldMapSVG = new SVGPath();

        worldMapSVG.setContent("m 479.68275,331.6274 -0.077,0.025 -0.258,0.155 -0.147,0.054 -0.134,0.027 -0.105,-0.011 -0.058,-0.091 0.006,-0.139 -0.024,-0.124 -0.02,-0.067 0.038,-0.181 0.086,-0.097 0.119,-0.08 0.188,0.029 0.398,0.116 0.083,0.109 10e-4,0.072 -0.073,0.119 z");
        worldMapSVG.setFill(Color.web("#81c483"));

        worldMapSVG.setScaleX(1009.6727);
        worldMapSVG.setScaleY(665.96301);
        //super.setWidth(10000);
        //super.setWidth(10000);
        mapCanvas = new Canvas(3000, 750);
        //mapCanvas.maxWidth(10000);
        //mapCanvas.maxHeight(10000);
        //mapCanvas.prefWidth(1000);
        //mapCanvas.prefHeight(750);
        //mapCanvas.setScaleX(1009);
        //mapCanvas.setScaleY(665);
        gc = mapCanvas.getGraphicsContext2D();
        //setUp(gc);


        //draw(gc, "m 479.68275,331.6274 -0.077,0.025 -0.258,0.155 -0.147,0.054 -0.134,0.027 -0.105,-0.011 -0.058,-0.091 0.006,-0.139 -0.024,-0.124 -0.02,-0.067 0.038,-0.181 0.086,-0.097 0.119,-0.08 0.188,0.029 0.398,0.116 0.083,0.109 10e-4,0.072 -0.073,0.119 z");
        //Platform.runLater(() -> {
            //gc.beginPath();
            //gc.setFill(Color.RED);
            //gc.setStroke(Color.BLUE);
            //gc.scale(1.85, 1.85);
            //countryDrawings.forEach(path -> {
                //path = String.toUpperCase(path);
       // mapCanvas.setScaleX(5);
        //mapCanvas.setScaleY(5);
        //gc.clearRect(0, 0, mapCanvas.getScaleX(), mapCanvas.getScaleY());
        //gc.scale(mapCanvas.getScaleX(), mapCanvas.getScaleY());
        countrySVGPaths.clear();
        setUp(gc);
        LOGGER.log(Level.SEVERE, "Size of country array: " + countrySVGPaths.size());
        //draw(gc);

        //});

            //gc.fill();
            //gc.stroke();

        //});

        mapStackPane = new StackPane();
        mapStackPane.setBackground(Background.fill(Color.BLUE));
        mapStackPane.getChildren().add(countryGroup);

        mapStackPane.setOnScroll(new EventHandler<ScrollEvent>() {
            @Override
            public void handle(ScrollEvent e) {
                e.consume();

                if (e.getDeltaY() == 0) {
                    return;
                }

                double scaleFactor = (e.getDeltaY() > 0) ? mapScaleFactor : 1 / mapScaleFactor;

                double oldXScale = countryGroup.getScaleY();
                double oldYScale = countryGroup.getScaleX();

                double newXScale = oldXScale * scaleFactor;
                double newYScale = oldYScale * scaleFactor;

                double xAdjust = (newXScale / oldXScale) - 1;
                double yAdjust = (newYScale / oldYScale) - 1;

                double moveX = e.getSceneX() - (countryGroup.getBoundsInParent().getWidth() / 2 + countryGroup.getBoundsInParent().getMinX());
                double moveY = e.getSceneY() - (countryGroup.getBoundsInParent().getHeight() / 2 + countryGroup.getBoundsInParent().getMinY());

                countryGroup.setTranslateX(countryGroup.getTranslateX() - xAdjust * moveX);
                countryGroup.setTranslateY(countryGroup.getTranslateY() - yAdjust * moveY);


                countryGroup.setScaleX(newXScale);
                countryGroup.setScaleY(newYScale);
                //mapStackPane.setScaleX(newXScale);
                //mapStackPane.setScaleY(newYScale);
                //mapStackPane.setScaleX(newXScale);

            }
        });

        mapStackPane.setOnMousePressed(event -> {
            mouseAnchorX = event.getSceneX();
            mouseAnchorY = event.getSceneY();

            Node node = (Node) event.getSource();

            transalateX = node.getTranslateX();
            transalateY = node.getTranslateY();
        });

        mapStackPane.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                return;
            }

            double scaleX = mapStackPane.getScaleX();
            double scaleY = mapStackPane.getScaleY();

            Node node = (Node) event.getSource();

            node.setTranslateX(transalateX + ((event.getSceneX() - mouseAnchorX) / scaleX));
            node.setTranslateY(transalateY + ((event.getSceneY() - mouseAnchorY) / scaleY));

            event.consume();

        });

        mapDisplay = new ImageView(mapImage);
        //mapStackPane.getChildren().add(mapCanvas);
        countryGroup.getChildren().clear();
        for (int i = 0; i < countrySVGPaths.size(); ++i) {
            countryGroup.getChildren().add(countrySVGPaths.get(i));
        }

        this.setPannable(true);
        this.setPrefSize(1009.6727, 665.96301);
        this.setHbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        this.setVbarPolicy(ScrollPane.ScrollBarPolicy.NEVER);
        //countryGroup.getChildren().add(p);

        setContent(mapStackPane);

        mapCanvas.scaleXProperty().bind(zoomProperty);
        mapCanvas.scaleYProperty().bind(zoomProperty);



        this.setHvalue(this.getHmin() + (this.getHmax() - this.getHmin()) / 2);
        this.setVvalue(this.getVmin() + (this.getVmax() - this.getVmin()) / 2);

    }

    private void draw(GraphicsContext gc) {
        //svgPath = svgPath.toUpperCase();
        //gc.scale(20, 20);
        //gc.beginPath();
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.WHITE);
        //gc.appendSVGPath(svgPath);
        gc.fill();
        gc.stroke();
    }

    private void draw(GraphicsContext gc, String svgPath) {
        //gc.beginPath();
        gc.setFill(Color.BLACK);
        gc.setStroke(Color.WHITE);
        gc.appendSVGPath(svgPath);
        gc.fill();
        gc.stroke();
    }



    private void setUp(GraphicsContext gc) {
        //gc.appendSVGPath("M 0 0 L 1 1 L 2 2 z");
        countryGroup.getChildren().clear();
        LOGGER.log(Level.SEVERE, "Inside setup");
        try {
            try (BufferedReader bFileReader = new BufferedReader(new FileReader("C:\\Projects\\constellation\\CoreMapView\\src\\au\\gov\\asd\\tac\\constellation\\views\\mapView\\resources\\mapSVGPaths.txt"))) {
                String line = "";
                String path = "";
                String lineEnds = " z m ";

                String worldMap = "";

                while ((line = bFileReader.readLine()) != null) {
                    //LOGGER.log(Level.SEVERE, "test1\ntest2");
                    line = line.strip();
                    if (line.startsWith("d=")) {
                        SVGPath p = new SVGPath();
                        path = line.substring(4, line.length() - 1);
                        path = "M" + path;
                        int index = path.indexOf(' ', 5);
                        path = path.substring(0, index) + lineEnds + path.substring(index + 1);

                        path = path.strip();
                        p.setContent(path);
                        //p.scaleXProperty().set(60);
                        //p.scaleYProperty().set(60);
                        gc.appendSVGPath(p.getContent());
                        p.setStroke(Color.WHITE);
                        p.setFill(Color.BLACK);
                        countrySVGPaths.add(p);
                        //countrySVGPaths.get(countrySVGPaths.size() - 1).setContent(path);
                        //Region countryRegion = new Region();
                        //countryRegion.setShape(p);
                        //countryRegion.setPrefSize(50, 30);
                        //countryGroup.getChildren().add(countryRegion);

                        //path = path.replaceFirst("^", "M");
                        //pathWithLines = pathWithLines.trim();
                        LOGGER.log(Level.SEVERE, "path : " + p.getContent());
                        //LOGGER.log(Level.SEVERE, "AUSTRALIA!!");

                        //countryDrawings.add(p.getContent());
                        path = "";

                    }

                    /*if (line.startsWith("d=")) {
                        path = line.substring(4, line.length() - 1);
                        path = "M" + path;

                        int index = path.indexOf(' ', 5);
                        path = path.substring(0, index) + lineEnds + path.substring(index + 1);

                        path = path.strip();

                        worldMap += " " + path;

                    }

                    worldMap = worldMap.strip();
                    p.setContent(worldMap);
                    p.setFill(Color.BLACK);
                    p.setStroke(Color.WHITE);
                    LOGGER.log(Level.SEVERE, "map : " + p.getContent());*/
                    //gc.appendSVGPath(p.getContent());
                    //p.setScaleX(5);
                    //p.setScaleY(5);

                    //countryGroup.setScaleX(countryGroup.getScaleX() * 5);
                    //countryGroup.setScaleY(countryGroup.getScaleY() * 5);
                }
                bFileReader.close();
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
