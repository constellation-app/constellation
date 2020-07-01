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
package au.gov.asd.tac.constellation.visual.vulkan;

import au.gov.asd.tac.constellation.utilities.camera.Camera;
import au.gov.asd.tac.constellation.utilities.color.ConstellationColor;
import au.gov.asd.tac.constellation.utilities.graphics.Frustum;
import au.gov.asd.tac.constellation.utilities.graphics.Matrix44f;
import au.gov.asd.tac.constellation.utilities.visual.DrawFlags;
import au.gov.asd.tac.constellation.utilities.visual.VisualAccess;
import au.gov.asd.tac.constellation.utilities.visual.VisualProcessor.VisualChangeProcessor;
import au.gov.asd.tac.constellation.utilities.visual.VisualProperty;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.BACKGROUND_COLOR;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.BLAZE_SIZE;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.BOTTOM_LABELS_REBUILD;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.BOTTOM_LABEL_COLOR;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.CAMERA;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.CONNECTIONS_OPACITY;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.CONNECTIONS_REBUILD;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.CONNECTION_COLOR;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.CONNECTION_LABELS_REBUILD;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.CONNECTION_LABEL_COLOR;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.CONNECTION_SELECTED;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.DRAW_FLAGS;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.EXTERNAL_CHANGE;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.HIGHLIGHT_COLOUR;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.TOP_LABELS_REBUILD;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.TOP_LABEL_COLOR;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.VERTEX_BLAZED;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.VERTEX_COLOR;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.VERTEX_FOREGROUND_ICON;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.VERTEX_SELECTED;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.VERTEX_X;
import static au.gov.asd.tac.constellation.utilities.visual.VisualProperty.VERTICES_REBUILD;
import static au.gov.asd.tac.constellation.visual.vulkan.CVKUtils.checkVKret;
import au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKAxesRenderable;
import au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKFPSRenderable;
import au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable;
import au.gov.asd.tac.constellation.visual.vulkan.renderables.CVKRenderable.CVKRenderableUpdateTask;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import org.lwjgl.system.MemoryStack;


public class CVKScene implements CVKRenderer.CVKRenderEventListener{
    protected static final float FIELD_OF_VIEW = 35;
    private static final float PERSPECTIVE_NEAR = 1;
    private static final float PERSPECTIVE_FAR = 500000;
    
    protected final CVKVisualProcessor parent;
    protected final BlockingQueue<CVKRenderableUpdateTask> taskQueue = new LinkedBlockingQueue<>();
    
    protected final CVKRenderer cvkRenderer;
    protected final Frustum viewFrustum = new Frustum();
    private final Matrix44f projectionMatrix = new Matrix44f();
    private CVKIconTextureAtlas cvkIconTextureAtlas = null;
    public List<CVKRenderable> renderables = new ArrayList<>();
    
   

    public Matrix44f GetProjectionMatrix() { return projectionMatrix; }
    public CVKIconTextureAtlas GetTextureAtlas() { return cvkIconTextureAtlas; }

    public List<CVKRenderable> GetRenderables(){
        return renderables;
    }


    public void Add(CVKRenderable renderable) {
        renderables.add(renderable);
        cvkRenderer.RenderableAdded(this, renderable);
    }
    
    public void Remove(CVKRenderable renderable) {
        renderables.remove(renderable);
        //cvkRenderer.RenderableAdded(this, false);
    }    
    
    public CVKScene(CVKRenderer inRenderer, CVKVisualProcessor inCVKVisualProcessor) {
        cvkRenderer = inRenderer;
        parent = inCVKVisualProcessor;
    }
    
    
    /*
    TO ANSWER:
    
    When do we load resources
    When do we prepare Vulkan resources
    How do we batch Vulkan resources
    
    
    WHAT ARE OUR VULKAN UNITS?
    
    What's in a command buffer
    What's in a renderpass
    What's in a pipeline
    
    ANSWERS
    One pipeline per vertex input state
    
    IDEAS
    Have each renderable detail what vertex format it needs, what texture (and
    whatever else requires a seperate pipeline) then when adding or updating it
    to the renderer, it's resources are created there.
    
    */
    
    
    @Override
    public void Display(MemoryStack stack, CVKFrame frame, CVKRenderer cvkRenderer, CVKDevice cvkDevice, CVKSwapChain cvkSwapChain, int frameIndex) {
        renderables.forEach(renderable -> {renderable.Display(stack, frame, cvkRenderer, cvkDevice, cvkSwapChain, frameIndex);});
    }
    
    
    @Override
    public void SwapChainRecreated(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain) {
        
        //  Windows-DPI-Scaling
        //
        // If JOGL is ever fixed or another solution is found, either change
        // needsManualDPIScaling to return false (so there is effectively no
        // DPI scaling here) or remove the scaled height and width below.         
        float dpiScaleX = 1.0f;
        float dpiScaleY = 1.0f;
//        if (GLTools.needsManualDPIScaling()){
//            dpiScaleX = (float)((Graphics2D)(parent.canvas).getGraphics()).getTransform().getScaleX();
//            dpiScaleY = (float)((Graphics2D)(parent.canvas).getGraphics()).getTransform().getScaleY();
//        }
        
        // These need to be final as they are used in the lambda function below
        final int dpiScaledWidth = (int)(cvkSwapChain.GetWidth() * dpiScaleX);
        final int dpiScaledHeight = (int)(cvkSwapChain.GetHeight() * dpiScaleY);
        
        // Create the projection matrix, and load it on the projection matrix stack.
        viewFrustum.setPerspective(FIELD_OF_VIEW, (float) dpiScaledWidth / (float) dpiScaledHeight, PERSPECTIVE_NEAR, PERSPECTIVE_FAR);        
        projectionMatrix.set(viewFrustum.getProjectionMatrix());
       
        // Give the renderables to recreate any swapchain dependent resources
        renderables.forEach(renderable -> {
            renderable.SwapChainRezied(cvkDevice, cvkSwapChain);});
    }
    
    @Override
    public void DeviceInitialised(CVKDevice cvkDevice) {
     // Idea: add these with events they care about, eg axes don't care about
        // VERTICES_REBUILD
        
        //HACKITY HACKITY HACK
        
        
        // Scene knows about all renderable types so build the static descriptor layout
        // for each class.
        assert(cvkDevice != null && cvkDevice.GetDevice() != null);
        
        // Initialise the shared atlas texture
        cvkIconTextureAtlas = new CVKIconTextureAtlas(cvkDevice);        
        
        // Static as the descriptor layout doesn't change per instance of renderable or over the course of the program
        checkVKret(CVKAxesRenderable.CreateDescriptorLayout(cvkDevice));
        checkVKret(CVKFPSRenderable.CreateDescriptorLayout(cvkDevice));
               
        CVKAxesRenderable a = new CVKAxesRenderable(this);
        Add(a);
                
        CVKFPSRenderable f = new CVKFPSRenderable(this);
        f.Init();
        Add(f);              
        
        // Load shaders for known renderable types
        // Static as the descriptor layout doesn't change per instance of renderable or over the course of the program
        checkVKret(CVKAxesRenderable.LoadShaders(cvkDevice));
        checkVKret(CVKFPSRenderable.LoadShaders(cvkDevice));          
        
        //renderables.forEach(renderable -> {renderable.LoadShaders(cvkDevice);});
        
        // Testing
        cvkIconTextureAtlas.AddIcon("Internet.Ebay");
        cvkIconTextureAtlas.AddIcon("Internet.Gmail");
        cvkIconTextureAtlas.AddIcon("Internet.Bankin");
        cvkIconTextureAtlas.AddIcon("Internet.Behance");
        cvkIconTextureAtlas.AddIcon("Miscellaneous.Dalek");
        cvkIconTextureAtlas.AddIcon("Miscellaneous.HAL-9000");
        cvkIconTextureAtlas.AddIcon("Character.Exclaimation Mark");
        cvkIconTextureAtlas.AddIcon("User Interface.Connections");
        cvkIconTextureAtlas.AddIcon("User Interface.Drag Word");
        cvkIconTextureAtlas.AddIcon("Internet.Shopify");
        cvkIconTextureAtlas.AddIcon("Miscellaneous.Mr Squiggle");
        cvkIconTextureAtlas.AddIcon("Miscellaneous.Lock");
        cvkIconTextureAtlas.AddIcon("Flag.Bahamas");
        cvkIconTextureAtlas.AddIcon("Flag.Netherlands");
        cvkIconTextureAtlas.AddIcon("User Interface.Remove");
        cvkIconTextureAtlas.AddIcon("Flag.Aland Islands");
        cvkIconTextureAtlas.AddIcon("Internet.Hangouts");
        cvkIconTextureAtlas.AddIcon("Background.Flat Square");
        cvkIconTextureAtlas.AddIcon("Communications.SIP Call");
        cvkIconTextureAtlas.AddIcon("Flag.Marshall Islands");
        cvkIconTextureAtlas.AddIcon("Flag.Chad");
        cvkIconTextureAtlas.AddIcon("Flag.Palestine");
        cvkIconTextureAtlas.AddIcon("Flag.Canada");
        cvkIconTextureAtlas.AddIcon("Internet.Zello");
        cvkIconTextureAtlas.AddIcon("Network.Cookie");
        cvkIconTextureAtlas.AddIcon("Internet.Kakao Talk");
        cvkIconTextureAtlas.AddIcon("Flag.Antigua and Barbuda");
        cvkIconTextureAtlas.AddIcon("Flag.Kenya");
        cvkIconTextureAtlas.AddIcon("Flag.Bhutan");
        cvkIconTextureAtlas.AddIcon("Transport.Plane");
        cvkIconTextureAtlas.AddIcon("Transport.Train");
        cvkIconTextureAtlas.AddIcon("User Interface.Drag Drop");
        cvkIconTextureAtlas.AddIcon("Internet.Pastebin");
        cvkIconTextureAtlas.AddIcon("Pie Chart.11/16 Pie");
        cvkIconTextureAtlas.AddIcon("Flag.Solomon Islands");
        cvkIconTextureAtlas.AddIcon("Flag.Moldova");
        cvkIconTextureAtlas.AddIcon("Internet.QQ");
        cvkIconTextureAtlas.AddIcon("Flag.Chile");
        cvkIconTextureAtlas.AddIcon("Miscellaneous.Music");
        cvkIconTextureAtlas.AddIcon("Flag.Lithuania");
        cvkIconTextureAtlas.AddIcon("Internet.Codepen");
        cvkIconTextureAtlas.AddIcon("Flag.Cook Islands");
        cvkIconTextureAtlas.AddIcon("Communications.Email");
        cvkIconTextureAtlas.AddIcon("Flag.Jordan");
        cvkIconTextureAtlas.AddIcon("Flag.Isle of Man");
        cvkIconTextureAtlas.AddIcon("User Interface.Columns");
        cvkIconTextureAtlas.AddIcon("Flag.Kyrgyzstan");
        cvkIconTextureAtlas.AddIcon("Network.Windows");
        cvkIconTextureAtlas.AddIcon("Network.Router");
        cvkIconTextureAtlas.AddIcon("Flag.Malaysia");
        cvkIconTextureAtlas.AddIcon("Internet.Picasa");
        cvkIconTextureAtlas.AddIcon("Miscellaneous.Graph");
        cvkIconTextureAtlas.AddIcon("Flag.Botswana");
        cvkIconTextureAtlas.AddIcon("User Interface.Half Hop");
        cvkIconTextureAtlas.AddIcon("Flag.Burkina Faso");
        cvkIconTextureAtlas.AddIcon("Network.SD Card");
        cvkIconTextureAtlas.AddIcon("Flag.Liechtenstein");
        cvkIconTextureAtlas.AddIcon("User Interface.Information");
        cvkIconTextureAtlas.AddIcon("Internet.Snapchat");
        cvkIconTextureAtlas.AddIcon("Miscellaneous.Bomb");
        cvkIconTextureAtlas.AddIcon("Internet.Medium");
        cvkIconTextureAtlas.AddIcon("User Interface.Expand");
        cvkIconTextureAtlas.AddIcon("Flag.Sao Tome and Principe");
        cvkIconTextureAtlas.AddIcon("Flag.Haiti");
        cvkIconTextureAtlas.AddIcon("User Interface.Axis (y)");
        cvkIconTextureAtlas.AddIcon("Character.Semi-Colon");
        cvkIconTextureAtlas.AddIcon("Flag.Djibouti");
        cvkIconTextureAtlas.AddIcon("Flag.Kosovo");
        cvkIconTextureAtlas.AddIcon("Communications.Call");
        cvkIconTextureAtlas.AddIcon("Internet.Magento");
        cvkIconTextureAtlas.AddIcon("Flag.Aruba");
        cvkIconTextureAtlas.AddIcon("User Interface.Axis (-z)");
        cvkIconTextureAtlas.AddIcon("Flag.Norway");
        cvkIconTextureAtlas.AddIcon("Network.Network Interface Card");
        cvkIconTextureAtlas.AddIcon("User Interface.Node Labels");
        cvkIconTextureAtlas.AddIcon("Flag.Tunisia");
        cvkIconTextureAtlas.AddIcon("Flag.Azerbaijan");
        cvkIconTextureAtlas.AddIcon("Internet.Naver");
        cvkIconTextureAtlas.AddIcon("Flag.Belarus");
        cvkIconTextureAtlas.AddIcon("User Interface.Zoom In");
        cvkIconTextureAtlas.AddIcon("Internet.Chrome");
        cvkIconTextureAtlas.AddIcon("Pie Chart.7/16 Pie");
        cvkIconTextureAtlas.AddIcon("Internet.Dailymotion");
        cvkIconTextureAtlas.AddIcon("Internet.Feedly");
        cvkIconTextureAtlas.AddIcon("Flag.India");
        cvkIconTextureAtlas.AddIcon("User Interface.Connection Labels");
        cvkIconTextureAtlas.AddIcon("User Interface.Axis (x)");
        cvkIconTextureAtlas.AddIcon("User Interface.Chevron Right Double");
        cvkIconTextureAtlas.AddIcon("Flag.Oman");
        cvkIconTextureAtlas.AddIcon("Flag.Turkmenistan");
        cvkIconTextureAtlas.AddIcon("Flag.Saint Lucia");
        cvkIconTextureAtlas.AddIcon("Flag.Argentina");
        cvkIconTextureAtlas.AddIcon("User Interface.Axis (-y)");
        cvkIconTextureAtlas.AddIcon("Flag.Czech Republic");
        cvkIconTextureAtlas.AddIcon("Character.Smiley Face");
        cvkIconTextureAtlas.AddIcon("Flag.South Africa");
        cvkIconTextureAtlas.AddIcon("Flag.Costa Rica");
        cvkIconTextureAtlas.AddIcon("Internet.Sina Weibo");
        cvkIconTextureAtlas.AddIcon("Network.OSX");
        cvkIconTextureAtlas.AddIcon("User Interface.Tag");
        cvkIconTextureAtlas.AddIcon("Flag.Colombia");
        cvkIconTextureAtlas.AddIcon("Flag.Equatorial Guinea");
        cvkIconTextureAtlas.AddIcon("Flag.Germany");
        cvkIconTextureAtlas.AddIcon("User Interface.Nodes");
        cvkIconTextureAtlas.AddIcon("User Interface.Search");
        cvkIconTextureAtlas.AddIcon("Character.Quotation Mark");
        cvkIconTextureAtlas.AddIcon("User Interface.Labels");
        cvkIconTextureAtlas.AddIcon("Flag.Guinea Bissau");
        cvkIconTextureAtlas.AddIcon("Internet.Internet Explorer");
        cvkIconTextureAtlas.AddIcon("Character.Full Stop");
        cvkIconTextureAtlas.AddIcon("Internet.Vine");
        cvkIconTextureAtlas.AddIcon("Network.Microprocessor");
        cvkIconTextureAtlas.AddIcon("Internet.Periscope");
        cvkIconTextureAtlas.AddIcon("Miscellaneous.Cloud");
        cvkIconTextureAtlas.AddIcon("Flag.Guernsey");
        cvkIconTextureAtlas.AddIcon("User Interface.Axis (-x)");
        cvkIconTextureAtlas.AddIcon("User Interface.Add Alternate");
        cvkIconTextureAtlas.AddIcon("Flag.Monaco");
        cvkIconTextureAtlas.AddIcon("Flag.Uruguay");
        cvkIconTextureAtlas.AddIcon("Flag.Mexico");
        cvkIconTextureAtlas.AddIcon("Flag.Algeria");
        cvkIconTextureAtlas.AddIcon("Internet.Bankin");
        cvkIconTextureAtlas.AddIcon("Flag.Swaziland");
        cvkIconTextureAtlas.AddIcon("Network.Webcam");
        cvkIconTextureAtlas.AddIcon("Flag.Cambodia");
        cvkIconTextureAtlas.AddIcon("User Interface.Axis (z)");
        cvkIconTextureAtlas.AddIcon("Flag.Venezuela");
        cvkIconTextureAtlas.AddIcon("Flag.Uganda");
        cvkIconTextureAtlas.AddIcon("Internet.Dribbble");
        cvkIconTextureAtlas.AddIcon("Internet.Imgur");
        cvkIconTextureAtlas.AddIcon("Miscellaneous.Globe");
        cvkIconTextureAtlas.AddIcon("Flag.Lebanon");
        cvkIconTextureAtlas.AddIcon("Flag.Estonia");
        cvkIconTextureAtlas.AddIcon("Internet.Viber");
        cvkIconTextureAtlas.AddIcon("Person.Person");
        cvkIconTextureAtlas.AddIcon("User Interface.Zoom Out");
        cvkIconTextureAtlas.AddIcon("Internet.Envato");
        cvkIconTextureAtlas.AddIcon("Character.Opening Square Bracket");
        cvkIconTextureAtlas.AddIcon("Flag.Eritrea");
        cvkIconTextureAtlas.AddIcon("Miscellaneous.Dalek");
        cvkIconTextureAtlas.AddIcon("Flag.Montenegro");
        cvkIconTextureAtlas.AddIcon("Flag.Seychelles");
        cvkIconTextureAtlas.AddIcon("Communications.Group Chat");
        cvkIconTextureAtlas.AddIcon("User Interface.Settings");
        cvkIconTextureAtlas.AddIcon("Internet.Google+");
        cvkIconTextureAtlas.AddIcon("Flag.Gambia");
        cvkIconTextureAtlas.AddIcon("Flag.Ireland");
        cvkIconTextureAtlas.AddIcon("Flag.Turkey");
        cvkIconTextureAtlas.AddIcon("Flag.Mauritania");
        cvkIconTextureAtlas.AddIcon("Internet.Jabber");
        cvkIconTextureAtlas.AddIcon("Internet.Google");
        cvkIconTextureAtlas.AddIcon("Internet.Instagram");
        cvkIconTextureAtlas.AddIcon("Internet.Aim");
        cvkIconTextureAtlas.AddIcon("Internet.Skype");
        cvkIconTextureAtlas.AddIcon("Network.Linux");
        cvkIconTextureAtlas.AddIcon("Flag.Greece");
        cvkIconTextureAtlas.AddIcon("Flag.Bahrain");
        cvkIconTextureAtlas.AddIcon("Internet.Whatsapp");
        cvkIconTextureAtlas.AddIcon("Flag.Vanuatu");
        cvkIconTextureAtlas.AddIcon("Transport.Tardis");
        cvkIconTextureAtlas.AddIcon("Flag.Namibia");
        cvkIconTextureAtlas.AddIcon("Flag.Paraguay");
        cvkIconTextureAtlas.AddIcon("Flag.Burundi");
        cvkIconTextureAtlas.AddIcon("Flag.Nauru");
        cvkIconTextureAtlas.AddIcon("Internet.Product Hunt");
        cvkIconTextureAtlas.AddIcon("Transport.Boat");
        cvkIconTextureAtlas.AddIcon("Network.Speaker");
        cvkIconTextureAtlas.AddIcon("Flag.Northern Mariana Islands");
        cvkIconTextureAtlas.AddIcon("Internet.Deviantart");
        cvkIconTextureAtlas.AddIcon("Network.Mouse");
        cvkIconTextureAtlas.AddIcon("Flag.Myanmar");
        cvkIconTextureAtlas.AddIcon("Internet.Telegram");
        cvkIconTextureAtlas.AddIcon("Miscellaneous.Map");
        cvkIconTextureAtlas.AddIcon("Background.Edge Square");
        cvkIconTextureAtlas.AddIcon("Flag.Guyana");
        cvkIconTextureAtlas.AddIcon("Internet.Airbnb");
        cvkIconTextureAtlas.AddIcon("Flag.Tonga");
        cvkIconTextureAtlas.AddIcon("Miscellaneous.Galaxy");
        cvkIconTextureAtlas.AddIcon("Internet.Viadeo");
        cvkIconTextureAtlas.AddIcon("Network.Internet");
        cvkIconTextureAtlas.AddIcon("Flag.Romania");
        cvkIconTextureAtlas.AddIcon("User Interface.Chevron Down");
        cvkIconTextureAtlas.AddIcon("Flag.Suriname");
        cvkIconTextureAtlas.AddIcon("Flag.Dominica");
        cvkIconTextureAtlas.AddIcon("Internet.Bittorrent");
        cvkIconTextureAtlas.AddIcon("Communications.Cell Tower");
        cvkIconTextureAtlas.AddIcon("Miscellaneous.Heart");
        cvkIconTextureAtlas.AddIcon("Internet.Outlook");
        cvkIconTextureAtlas.AddIcon("Internet.Paypal");
        cvkIconTextureAtlas.AddIcon("Pie Chart.0/16 Pie");
        cvkIconTextureAtlas.AddIcon("Flag.Uzbekistan");
        cvkIconTextureAtlas.AddIcon("Internet.Scoopit");
        cvkIconTextureAtlas.AddIcon("Miscellaneous.Shield");
        cvkIconTextureAtlas.AddIcon("Internet.Lastfm");
        cvkIconTextureAtlas.AddIcon("Flag.Latvia");
        cvkIconTextureAtlas.AddIcon("User Interface.Key");        
        
        
        // The renderables above will have requested the icons they need for their initial state, we
        // now need to generate the atlas texture and sampler before the renderables that rely on them
        // create their descriptors
        cvkIconTextureAtlas.Init();
    }
    
    @Override
    public void DisplayUpdate(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain, int frameIndex) {
        // Give the shared icon loader a chance to recreate itself if needed
        cvkIconTextureAtlas.DisplayUpdate();
        
        renderables.forEach(renderable -> {renderable.DisplayUpdate(cvkDevice, cvkSwapChain, frameIndex);});
    }
    
/**
     * Gets the relevant {@link VisualChangeProcessor} for the specified
     * {@link VisualProperty}.
     * <p>
     * This change processor will perform the necessary buffer updating and
     * resending for this renderable, taking care of the fact that retrieval
     * from the supplied {@link VisualAccess} needs to be synchronous, whilst
     * the sending of buffered data needs to occur asynchronously as part of the
     * next display phase of the GL life-cycle.
     *
     * @param property
     * @return
     */
    VisualChangeProcessor getChangeProcessor(VisualProperty property) {
        switch (property) {
            case VERTICES_REBUILD:
                return (change, access) -> {
//                    addTask(nodeLabelBatcher.setTopLabelColors(access));
//                    addTask(nodeLabelBatcher.setTopLabelSizes(access));
//                    addTask(nodeLabelBatcher.setBottomLabelColors(access));
//                    addTask(nodeLabelBatcher.setBottomLabelSizes(access));
//                    addTask(xyzTexturiser.dispose());
//                    addTask(xyzTexturiser.createTexture(access));
//                    addTask(vertexFlagsTexturiser.dispose());
//                    addTask(vertexFlagsTexturiser.createTexture(access));
//                    addTask(iconBatcher.disposeBatch());
//                    addTask(iconBatcher.createBatch(access));
//                    addTask(nodeLabelBatcher.disposeBatch());
//                    addTask(nodeLabelBatcher.createBatch(access));
//                    addTask(blazeBatcher.disposeBatch());
//                    addTask(blazeBatcher.createBatch(access));
//                    addTask(gl -> {
//                        iconTextureArray = iconBatcher.updateIconTexture(gl);
//                    });
//                    final DrawFlags updatedDrawFlags = access.getDrawFlags();
//                    addTask(gl -> {
//                        drawFlags = updatedDrawFlags;
//                    });
                };
            case CONNECTIONS_REBUILD:
                return (change, access) -> {
//                    addTask(connectionLabelBatcher.setLabelColors(access));
//                    addTask(connectionLabelBatcher.setLabelSizes(access));
//                    addTask(lineBatcher.disposeBatch());
//                    addTask(lineBatcher.createBatch(access));
//                    addTask(loopBatcher.disposeBatch());
//                    addTask(loopBatcher.createBatch(access));
//                    addTask(connectionLabelBatcher.disposeBatch());
//                    addTask(connectionLabelBatcher.createBatch(access));
                };
            case BACKGROUND_COLOR:
                return (change, access) -> {
                    final ConstellationColor backgroundColor = access.getBackgroundColor();
//                    addTask(gl -> {
//                        graphBackgroundColor = new float[]{backgroundColor.getRed(), backgroundColor.getGreen(), backgroundColor.getBlue(), 1};
//                    });
//                    addTask(connectionLabelBatcher.setBackgroundColor(access));
//                    addTask(nodeLabelBatcher.setBackgroundColor(access));
                };
            case HIGHLIGHT_COLOUR:
                return (change, access) -> {
//                    addTask(nodeLabelBatcher.setHighlightColor(access));
//                    addTask(connectionLabelBatcher.setHighlightColor(access));
//                    addTask(lineBatcher.setHighlightColor(access));
//                    addTask(iconBatcher.setHighlightColor(access));
                };
            case DRAW_FLAGS:
                return (change, access) -> {
                    final DrawFlags updatedDrawFlags = access.getDrawFlags();
//                    addTask(gl -> {
//                        drawFlags = updatedDrawFlags;
//                    });
                };
            case BLAZE_SIZE:
                return (change, access) -> {
//                    addTask(blazeBatcher.updateSizeAndOpacity(access));
                };
            case CONNECTIONS_OPACITY:
                return (change, access) -> {
//                    addTask(lineBatcher.updateOpacity(access));
                };
            case BOTTOM_LABEL_COLOR:
                return (change, access) -> {
//                    addTask(nodeLabelBatcher.setBottomLabelColors(access));
                };
            case BOTTOM_LABELS_REBUILD:
                return (change, access) -> {
//                    addTask(nodeLabelBatcher.setBottomLabelColors(access));
//                    addTask(nodeLabelBatcher.setBottomLabelSizes(access));
//                    // Note that updating bottom labels always rebuilds from scratch, so it is not an issue if the batch was not 'ready'.
//                    addTask(nodeLabelBatcher.updateBottomLabels(access));
                };
            case CAMERA:
                return (change, access) -> {
                    final Camera updatedCamera = access.getCamera();
//                    addTask(gl -> {
//                        camera = updatedCamera;
//                        parent.setDisplayCamera(camera);
//                        Graphics3DUtilities.getModelViewMatrix(camera.lookAtEye, camera.lookAtCentre, camera.lookAtUp, parent.getDisplayModelViewMatrix());
//                    });
                };
            case CONNECTION_LABEL_COLOR:
                return (change, access) -> {
//                    addTask(connectionLabelBatcher.setLabelColors(access));
                };
            case CONNECTION_LABELS_REBUILD:
                return (change, access) -> {
//                    addTask(connectionLabelBatcher.setLabelColors(access));
//                    addTask(connectionLabelBatcher.setLabelSizes(access));
//                    // Note that updating connection labels always rebuilds from scratch, so it is not an issue if the batch was not 'ready'.
//                    addTask(connectionLabelBatcher.updateLabels(access));
                };
            case TOP_LABEL_COLOR:
                return (change, access) -> {
//                    addTask(nodeLabelBatcher.setTopLabelColors(access));
                };
            case TOP_LABELS_REBUILD:
                return (change, access) -> {
//                    addTask(nodeLabelBatcher.setTopLabelColors(access));
//                    addTask(nodeLabelBatcher.setTopLabelSizes(access));
//                    // Note that updating top labels always rebuilds from scratch, so it is not an issue if the batch was not 'ready'.
//                    addTask(nodeLabelBatcher.updateTopLabels(access));
                };
            case CONNECTION_COLOR:
                return (change, access) -> {
//                    addTaskIfReady(loopBatcher.updateColors(access, change), loopBatcher);
//                    addTaskIfReady(lineBatcher.updateColors(access, change), lineBatcher);
                };
            case CONNECTION_SELECTED:
                return (change, access) -> {
//                    addTaskIfReady(loopBatcher.updateInfo(access, change), loopBatcher);
//                    addTaskIfReady(lineBatcher.updateInfo(access, change), lineBatcher);
                };
            case VERTEX_BLAZED:
                return (change, access) -> {
                    // Note that updating blazes always rebuilds from scratch, so it is not an issue if the batch was not 'ready'.
//                    addTask(blazeBatcher.updateBlazes(access, change));
                };
            case VERTEX_COLOR:
                return (change, access) -> {
//                    addTaskIfReady(iconBatcher.updateColors(access, change), iconBatcher);
                };
            case VERTEX_FOREGROUND_ICON:
                return (change, access) -> {
//                    addTaskIfReady(iconBatcher.updateIcons(access, change), iconBatcher);
//                    addTask(gl -> {
//                        iconTextureArray = iconBatcher.updateIconTexture(gl);
//                    });
                };
            case VERTEX_SELECTED:
                return (change, access) -> {
//                    if (vertexFlagsTexturiser.isReady()) {
//                        addTask(vertexFlagsTexturiser.updateFlags(access, change));
//                    } else {
//                        addTask(vertexFlagsTexturiser.dispose());
//                        addTask(vertexFlagsTexturiser.createTexture(access));
//                    }
                };
            case VERTEX_X:
                return (change, access) -> {
//                    if (vertexFlagsTexturiser.isReady()) {
//                        addTask(xyzTexturiser.updateXyzs(access, change));
//                    } else {
//                        addTask(xyzTexturiser.dispose());
//                        addTask(xyzTexturiser.createTexture(access));
//                    }
                };
            case EXTERNAL_CHANGE:
            default:
                return (change, access) -> {
                };
        }
    } 
    
    /**
     * Tasks are added to the taskQueue by the VisualProcessor thread in 
     * response to user input.  The taskQueue is drained and actioned by
     * the AWT Event thread when the canvas is painted (ie our render thread)
     * 
     * @param task
     */
    protected void addTask(final CVKRenderableUpdateTask task) {
        taskQueue.add(task);
    }    
    
    public void GetDescriptorTypeRequirements(int descriptorTypeCounts[]) {
        renderables.forEach(renderable -> {renderable.IncrementDescriptorTypeRequirements(descriptorTypeCounts);});
    }
}
