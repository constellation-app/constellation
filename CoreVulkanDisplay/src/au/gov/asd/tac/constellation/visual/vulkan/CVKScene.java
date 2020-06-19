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
import static org.lwjgl.vulkan.VK10.VK_NULL_HANDLE;


public class CVKScene implements CVKRenderer.CVKRenderEventListener{
    private final CVKVisualProcessor parent;
    private final BlockingQueue<CVKRenderableUpdateTask> taskQueue = new LinkedBlockingQueue<>();
    
    protected final CVKRenderer cvkRenderer;
    public List<CVKRenderable> renderables = new ArrayList<>();
    
   
    
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
        renderables.forEach(renderable -> {renderable.SwapChainRezied(cvkDevice, cvkSwapChain);});
    }
    
    @Override
    public void DeviceInitialised(CVKDevice cvkDevice) {
     // Idea: add these with events they care about, eg axes don't care about
        // VERTICES_REBUILD
        
        //HACKITY HACKITY HACK
        
        
        // Scene knows about all renderable types so build the static descriptor layout
        // for each class.
        assert(cvkDevice != null && cvkDevice.GetDevice() != null);
        checkVKret(CVKAxesRenderable.CreateDescriptorLayout(cvkDevice));
        checkVKret(CVKFPSRenderable.CreateDescriptorLayout(cvkDevice));
        
        // Load shaders for known renderable types
        checkVKret(CVKAxesRenderable.LoadShaders(cvkDevice));
        checkVKret(CVKFPSRenderable.LoadShaders(cvkDevice));        
        
        CVKAxesRenderable a = new CVKAxesRenderable(this);
        Add(a);
                
        CVKFPSRenderable f = new CVKFPSRenderable(this);
        Add(f);              
        
        //renderables.forEach(renderable -> {renderable.LoadShaders(cvkDevice);});
    }
    
    @Override
    public void DisplayUpdate(CVKDevice cvkDevice, CVKSwapChain cvkSwapChain, int frameIndex) {
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
