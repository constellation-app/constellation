/*
 * Copyright 2010-2020 Australian Signals Directorate
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
package au.gov.asd.tac.constellation.plugins.arrangements.uncollide.d3;

//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.Random;
//import javafx.application.Application;
//import javafx.application.Platform;
//import javafx.geometry.Point3D;
//import javafx.scene.AmbientLight;
//import javafx.scene.DepthTest;
//import javafx.scene.Group;
//import javafx.scene.PerspectiveCamera;
//import javafx.scene.PointLight;
//import javafx.scene.Scene;
//import javafx.scene.input.MouseButton;
//import javafx.scene.input.MouseEvent;
//import javafx.scene.input.ScrollEvent;
//import javafx.scene.paint.Color;
//import javafx.scene.paint.PhongMaterial;
//import javafx.scene.shape.Sphere;
//import javafx.scene.transform.Rotate;
//import javafx.stage.Stage;
/**
 *
 */
public class LightsDemo {

//    private static final int N = 5000;
//    private static final double SCENE_WIDTH = 800;
//    private static final double SCENE_HEIGHT = 800;
//    private static final double RADIUS = 25;
//    private final Random rand = new Random();
//    private final PerspectiveCamera camera;
//    private final Rotate xrotate;
//    private final Rotate yrotate;
//    private final Rotate zrotate;
//    private double dragStartX, dragStartY, dragStartRotateX, dragStartRotateY;
//    public LightsDemo()
//    {
//        camera = new PerspectiveCamera(false);
////        camera.setTranslateX(SCENE_WIDTH/2);
////        camera.setTranslateY(SCENE_WIDTH/2);
//        camera.setTranslateZ(-SCENE_WIDTH);
//        xrotate = new Rotate(0, SCENE_WIDTH/2, SCENE_HEIGHT/2, SCENE_HEIGHT/2, new Point3D(1, 0, 0));
//        yrotate = new Rotate(0, SCENE_WIDTH/2, SCENE_HEIGHT/2, SCENE_HEIGHT/2, new Point3D(0, 1, 0));
//        zrotate = new Rotate(0, SCENE_WIDTH/2, SCENE_HEIGHT/2, SCENE_HEIGHT/2, new Point3D(0, 0, 1));
////        camera.getTransforms().addAll(xrotate, yrotate, zrotate);
//    }
//    private Color makeColor()
//    {
//        final double r = 0.25 + 0.75*rand.nextDouble();
//        final double g = 0.25 + 0.75*rand.nextDouble();
//        final double b = 0.25 + 0.75*rand.nextDouble();
//        return new Color(r, g, b, 1);
//    }
//    /**
//     * Making 3D Object
//     *
//     * @param size
//     * @param radius
//     * @return
//     */
//    public Sphere make3DObject(final double size, final double radius)
//    {
//        final Sphere sphere = new Sphere(radius/2*(rand.nextDouble() + 1));
//        PhongMaterial material = new PhongMaterial();
//        material.setDiffuseColor(makeColor());
//        material.setSpecularColor(makeColor());
//        sphere.setMaterial(material);
//        sphere.setTranslateX(size*rand.nextDouble());
//        sphere.setTranslateY(size*rand.nextDouble());
//        sphere.setTranslateZ(size*rand.nextDouble());
////        sphere.setLayoutX(size * rand.nextDouble());
////        sphere.setLayoutY(size * rand.nextDouble());
////        c.setLayoutY(size * rand.nextDouble());
//        return sphere;
//    }
//    /**
//     * Function for starting the stage and scene components
//     *
//     * @param primaryStage
//     * @throws IOException
//     */
//    @Override
//    public void start(Stage primaryStage) throws IOException
//    {
//        final List<Sphere> shapes = new ArrayList<Sphere>();
//        //Container
//        Group root = new Group();
//        root.setDepthTest(DepthTest.ENABLE);
////        // translate and rotate group so that origin is center and +Y is up
////        root.setTranslateX(SCENE_WIDTH);
////        root.setTranslateY(SCENE_HEIGHT);
////        root.getTransforms().add(new Rotate(180,Rotate.X_AXIS));
//        final AmbientLight ambient = new AmbientLight(Color.gray(0.5, 0.5));
//        final PointLight point = new PointLight(Color.WHITE);
//        point.setLayoutX(400);
//        point.setLayoutY(100);
//        point.setTranslateZ(-1100);
//        root.getChildren().addAll(point, ambient);
//        final Group root2 = new Group();
//        root2.getTransforms().addAll(xrotate, yrotate, zrotate);
//        root.getChildren().add(root2);
//        for(int i=0; i<N; i++)
//        {
//            final double r = i%100==0 ? 5*RADIUS + rand.nextFloat()*75 : RADIUS + rand.nextFloat()*RADIUS;
//            final Sphere shape = make3DObject(SCENE_WIDTH, r);
//            root2.getChildren().add(shape);
//            shapes.add(shape);
//            if(i%100==0)
//            {
//                System.out.printf(" %d\n", i);
//            }
//        }
//        System.out.printf("Shapes: %d\n", shapes.size());
//        Scene scene = new Scene(root, SCENE_WIDTH, SCENE_HEIGHT, true);
//        scene.setCamera(camera);
//        scene.addEventHandler(ScrollEvent.SCROLL, (final ScrollEvent event) ->
//        {
//            camera.setTranslateZ(camera.getTranslateZ()+event.getDeltaY()*2);
//        });
//        scene.addEventHandler(MouseEvent.ANY, (final MouseEvent event) ->
//        {
//            if(event.getEventType() == MouseEvent.MOUSE_PRESSED)
//            {
//                dragStartX = event.getSceneX();
//                dragStartY = event.getSceneY();
//                dragStartRotateX = xrotate.getAngle();
//                dragStartRotateY = yrotate.getAngle();
//            }
//            else if(event.getEventType() == MouseEvent.MOUSE_DRAGGED && event.getButton()==MouseButton.SECONDARY)
//            {
//                double xDelta = event.getSceneX() - dragStartX;
//                double yDelta = event.getSceneY() - dragStartY;
//                xrotate.setAngle(dragStartRotateX - (yDelta * 0.7));
//                yrotate.setAngle(dragStartRotateY + (xDelta * 0.7));
//            }
//            else if(event.getEventType()==MouseEvent.MOUSE_CLICKED && event.getButton()==MouseButton.PRIMARY)
//            {
////                for(Sphere s : shapes)
////                {
////                    s.setTranslateX(s.getTranslateX()+10*(rand.nextDouble()-0.5));
////                    s.setTranslateY(s.getTranslateY()+10*(rand.nextDouble()-0.5));
////                    s.setTranslateZ(s.getTranslateZ()+10*(rand.nextDouble()-0.5));
////                }
//                uncollide(shapes);
//            }
//        });
//        primaryStage.setTitle("Spheres");
//        primaryStage.setScene(scene);
//        primaryStage.show();
//    }
//    private void uncollide(final List<Sphere> shapes)
//    {
//        final Orb3D[] orbs = new Orb3D[shapes.size()];
//        int ix = 0;
//        for(final Sphere s : shapes)
//        {
//            final Orb3D sp = new Orb3D((float)s.getTranslateX(), (float)s.getTranslateY(), (float)s.getTranslateZ(), (float)s.getRadius());
//            orbs[ix++] = sp;
//        }
//        new Thread()
//        {
//            @Override
//            public void run()
//            {
//                while(true)
//                {
//                    final BoundingBox3D.Box3D bb = BoundingBox3D.getBox(orbs);
//                    final Octree qt = new Octree(bb);
//                    for(final Orb3D sp : orbs)
//                    {
//                        qt.insert(sp);
//                    }
//                    float padding = 1;
//                    int totalCollided = 0;
//                    for(final Orb3D sp : orbs)
//                    {
//                        final int collided = qt.uncollide(sp, padding);
//                        totalCollided += collided;
//                    }
//                    System.out.printf("collided=%d\n", totalCollided);
//                    Platform.runLater(() ->
//                    {
//                        int ix1 = 0;
//                        for(final Sphere s : shapes)
//                        {
//                            final Orb3D orb = orbs[ix1];
//                            s.setTranslateX(orb.x);
//                            s.setTranslateY(orb.y);
//                            s.setTranslateZ(orb.z);
//                            ix1++;
//                        }
//                    });
//        //            ix = 0;
//        //            for(final Sphere s : shapes)
//        //            {
//        //                final Spherical sp = sps[ix];
//        //                s.setTranslateX(sp.getX());
//        //                s.setTranslateY(sp.getY());
//        //                s.setTranslateZ(sp.getZ());
//        //                ix++;
//        //            }
//                    if(totalCollided==0)
//                    {
//                        break;
//                    }
//                }
//            }
//        }.start();
//    }
//    /**
//     * The main() method is ignored in correctly deployed JavaFX application.
//     * main() serves only as fallback in case the application can not be
//     * launched through deployment artifacts, e.g., in IDEs with limited FX
//     * support. NetBeans ignores main().
//     *
//     * @param args the command line arguments
//     */
//    public static void main(String[] args)
//    {
//        launch(args);
//    }
}
