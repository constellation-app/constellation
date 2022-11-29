/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2008 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
 */
package au.gov.asd.tac.constellation.graph.file.open;

import java.awt.Container;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import static java.util.logging.Level.FINER;
import static java.util.logging.Level.FINEST;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JEditorPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Element;
import javax.swing.text.StyledDocument;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.actions.FileSystemAction;
import org.openide.actions.ToolsAction;
import org.openide.awt.StatusDisplayer;
import org.openide.cookies.EditCookie;
import org.openide.cookies.EditorCookie;
import static org.openide.cookies.EditorCookie.Observable.PROP_OPENED_PANES;
import org.openide.cookies.OpenCookie;
import org.openide.cookies.ViewCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.nodes.NodeOperation;
import org.openide.text.NbDocument;
import org.openide.util.ContextAwareAction;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 * Opens files when requested. Main functionality.
 *
 * @author Jaroslav Tulach, Jesse Glick, Marian Petras, David Konecny
 */
@org.openide.util.lookup.ServiceProvider(service = au.gov.asd.tac.constellation.graph.file.open.OpenFileImpl.class, position = 100)
public class DefaultOpenFileImpl implements OpenFileImpl, Runnable {

    private static final Logger LOGGER = Logger.getLogger(DefaultOpenFileImpl.class.getName());
    /**
     * parameter of this <code>Runnable</code> - file to open
     */
    private final FileObject fileObject;
    /**
     * parameter of this <code>Runnable</code> - line number to open the
     * {@link #fileObject file} at, or <code>-1</code> to ignore
     */
    private final int line;

    /**
     * Creates an instance of this class. It is used only as an instance of
     * <code>Runnable</code> used for rescheduling to the AWT thread. The
     * arguments are stored to local variables and when the <code>run()</code>
     * method gets executed (in the AWT thread), they are passed to the
     * <code>open(...)</code> method.
     *
     * @param file file to open (must exist)
     * @param line line number to try to open to (starting at zero), or
     * <code>-1</code> to ignore
     * @param waiter double-callback or <code>null</code>
     */
    private DefaultOpenFileImpl(final FileObject fileObject,
            final int line) {
        this.fileObject = fileObject;
        this.line = line;
    }

    /**
     * Creates a new instance of OpenFileImpl
     */
    public DefaultOpenFileImpl() {

        /* These fields are not used in the default instance. */
        this.fileObject = null;
        this.line = -1;
    }

    /**
     * Sets the specified text into the status line.
     *
     * @param text text to be displayed
     */
    protected final void setStatusLine(final String text) {
        StatusDisplayer.getDefault().setStatusText(text);
    }

    /**
     * Displays a dialog that the file cannot be open. This method is to be used
     * in cases that the file was open via the Open File Server. The message
     * also informs that the launcher will be notified as if the file was closed
     * immediately.
     *
     * @param fileName name of file that could not be opened
     */
    protected void notifyCannotOpen(final String fileName) {
        assert EventQueue.isDispatchThread();

        DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                NbBundle.getMessage(DefaultOpenFileImpl.class,
                        "MSG_cannotOpenWillClose", //NOI18N
                        fileName)));
    }

    /**
     * Opens an editor using <code>EditorCookie</code>. If non-negative line
     * number is passed, it also places cursor at the given line.
     *
     * @param cookie cookie to use for opening an editor
     * @param observable whether the cookie is
     * <code>EditorCookie.Observable</code>
     * @param line line number to place cursor to (starting at <code>0</code>)
     * @return  <code>true</code> if the cookie was successfully activated,
     * <code>false</code> if some error occurred
     */
    private boolean openEditor(final EditorCookie editorCookie, final int line) {
        assert EventQueue.isDispatchThread();
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.log(FINER, "openEditor(EditorCookie, line={0})", line); //NOI18N
        }

        /* if the editor is already open, just set the cursor and activate it */
        final JEditorPane[] openPanes = editorCookie.getOpenedPanes();
        if (openPanes != null) {
            LOGGER.finest("open pane(s) found");                           //NOI18N
            if (line >= 0) {
                openPanes[0].setCaretPosition(getCursorOffset(editorCookie.getDocument(),line));
            }

            final Container c;
            c = SwingUtilities.getAncestorOfClass(TopComponent.class, openPanes[0]);
            if (c != null) {
                WindowManager.getDefault().invokeWhenUIReady(((TopComponent) c)::requestActive);
            } else {
                assert false;
            }
            return true;
        }

        /* get the document: */
        final StyledDocument doc;
        try {
            doc = editorCookie.openDocument();
        } catch (final IOException ex) {
            final String msg = NbBundle.getMessage(
                    DefaultOpenFileImpl.class,
                    "MSG_cannotOpenWillClose");                     //NOI18N
            ErrorManager.getDefault().notify(
                    ErrorManager.EXCEPTION,
                    ErrorManager.getDefault().annotate(ex, msg));
            return false;
        }

        WindowManager.getDefault().invokeWhenUIReady(() -> {
            /*
            * Note: editorCookie.open() may return before the editor is
            * actually open. But since the document was successfully open,
            * the editor should be opened quite quickly and no problem
            * should occur.
             */
            editorCookie.open();

            if (line >= 0) {
                openDocAtLine(editorCookie, doc, line);
            }
        });
        return true;
    }

    /**
     * Opens a document in the editor at a given line. This method is used in
     * the case that the editor is not opened yet (
     * <code>EditorCookie.getOpenedPanes()</code> returned <code>null</code>)
     * and is to be opened at a specific line.
     *
     * @param editorCookie editor cookie to use for opening the document
     * @param doc document already loaded using the editor cookie
     * @param line line to open the document at (first line = <code>0</code>);
     * must be non-negative
     */
    private void openDocAtLine(final EditorCookie editorCookie, final StyledDocument doc, final int line) {
        assert EventQueue.isDispatchThread();
        assert line >= 0;
        assert editorCookie.getDocument() == doc;

        if (LOGGER.isLoggable(FINER)) {
            LOGGER.log(FINER, "openDocAtLine(EditorCookie, Document, line={0})", line);
        }

        final int offset = getCursorOffset(doc, line);
        new SetCursorTask(editorCookie, offset).perform();
    }

    final class SetCursorTask implements Runnable, PropertyChangeListener {

        /**
         * if opening file using non-observable {@code EditorCookie}, how long
         * should we wait (in milliseconds) between tries?
         */
        private static final int OPEN_EDITOR_WAIT_PERIOD_MS = 200;
        
        /**
         * if opening file using non-observable {@code EditorCookie}, how long
         * should we wait (in milliseconds) in total before giving up?
         */
        private static final int OPEN_EDITOR_TOTAL_TIMEOUT_MS = 10000;
        private static final int MAX_TRIES = OPEN_EDITOR_TOTAL_TIMEOUT_MS / OPEN_EDITOR_WAIT_PERIOD_MS;
        private final EditorCookie editorCookie;
        private final EditorCookie.Observable observable;
        private final int offset;
        private volatile boolean success = false;

        private SetCursorTask(final EditorCookie editorCookie, final int offset) {
            this.editorCookie = editorCookie;
            this.observable = (editorCookie instanceof EditorCookie.Observable)
                    ? (EditorCookie.Observable) editorCookie
                    : null;
            this.offset = offset;

            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.finest("SetCursorTask.<init>");                     //NOI18N
                LOGGER.log(FINEST, " - observable: {0}", (observable != null));//NOI18N
            }
        }

        private void perform() {
            LOGGER.finer("SetCursorTask: perform()");                      //NOI18N

            LOGGER.finest("SetCursorTask: Calling tryNow() for the first time...");//NOI18N
            if (tryNow()) {
                LOGGER.finest("SetCursorTask:    SUCCESS!");               //NOI18N
                return;
            }

            if (observable != null) {
                LOGGER.finest("SetCursorTask: addPropertyChangeListener...");//NOI18N
                observable.addPropertyChangeListener(this);

                /*
                 * We must try after we started listening, otherwise
                 * we might miss the moment the pane was opened.
                 */
                LOGGER.finest("SetCursorTask: tryNow() after adding the listener...");//NOI18N
                if (tryNow()) {
                    LOGGER.finest("SetCursorTask:    SUCCESS!");           //NOI18N
                }
            } else {
                trySeveralTimes();
            }
        }

        private boolean tryNow() {
            assert !success;

            final JEditorPane[] panes = editorCookie.getOpenedPanes();
            if (panes != null) {
                this.success = true;
                panes[0].setCaretPosition(offset);
                return true;
            } else {
                return false;
            }
        }

        @Override
        public void propertyChange(final PropertyChangeEvent e) {
            LOGGER.finer("SetCursorTask: propertyChange()");               //NOI18N

            if (!PROP_OPENED_PANES.equals(e.getPropertyName())) {
                throw new IllegalArgumentException();
            } else {
                observable.removePropertyChangeListener(this);
                Mutex.EVENT.writeAccess(this);
            }
        }

        private void trySeveralTimes() {
            LOGGER.finest("SetCursorTask: trySeveralTimes()");             //NOI18N
            RequestProcessor.getDefault().post(new ScheduledOpenTask(),
                    OPEN_EDITOR_WAIT_PERIOD_MS);
        }

        class ScheduledOpenTask implements Runnable {

            private final AtomicInteger remainingTries = new AtomicInteger(MAX_TRIES);

            @Override
            public void run() {
                try {
                    EventQueue.invokeAndWait(SetCursorTask.this);
                } catch (final InterruptedException ex) {
                    LOGGER.log(Level.SEVERE, "Thread was interrupted", ex);
                    Thread.currentThread().interrupt();
                } catch (final InvocationTargetException ex) {
                    LOGGER.log(Level.SEVERE, ex.getLocalizedMessage(), ex);
                }
                if (!SetCursorTask.this.success) {
                    if (remainingTries.decrementAndGet() != 0) {
                        RequestProcessor.getDefault()
                                .post(this, OPEN_EDITOR_WAIT_PERIOD_MS);
                    } else {
                        notifyCouldNotOpen();
                    }
                }
            }
        }

        @Override
        public void run() {
            assert EventQueue.isDispatchThread();
            LOGGER.finer("SetCursorTask: run()");                          //NOI18N

            if (success) {
                /*
                 * This may happen e.g. if the editor pane is opened during
                 * the period between call of addPropertyChangeListener(...)
                 * and the second call of tryNow() in method perform().
                 */
                LOGGER.finest("SetCursorTask:   - already done");          //NOI18N
                return;
            }

            LOGGER.finest("SetCursorTask:   tryNow() from run()");         //NOI18N
            tryNow();
            if ((observable != null) && !success) {
                assert false;
                notifyCouldNotOpen();
            }
        }

        private void notifyCouldNotOpen() {
            DialogDisplayer.getDefault().notifyLater(
                    new NotifyDescriptor.Message(
                            NbBundle.getMessage(
                                    DefaultOpenFileImpl.class,
                                    "MSG_couldNotOpenAt"), //NOI18N
                            NotifyDescriptor.INFORMATION_MESSAGE));
        }
    }

    /**
     * Computes cursor offset of a given line of a document. The line number
     * must be non-negative. If the line number is greater than number of the
     * last line, the returned offset corresponds to the last line of the
     * document.
     *
     * @param doc document to computer offset for
     * @param line line number (first line = <code>0</code>)
     * @return cursor offset of the beginning of the given line
     */
    private static int getCursorOffset(final StyledDocument doc, final int line) {
        assert EventQueue.isDispatchThread();
        assert line >= 0;

        try {
            return NbDocument.findLineOffset(doc, line);
        } catch (final IndexOutOfBoundsException ex) {
            /* probably line number out of bounds */

            final Element lineRootElement = NbDocument.findLineRootElement(doc);
            final int lineCount = lineRootElement.getElementCount();
            if (line >= lineCount) {
                return NbDocument.findLineOffset(doc, lineCount - 1);
            } else {
                throw ex;
            }
        }
    }

    /**
     * Activates the specified cookie, thus opening a file. The file is
     * specified by the cookie, because the cookie was obtained from it. The
     * cookie must be one of <code>EditorCookie</code> <code>OpenCookie</code>,
     * <code>EditCookie</code>, <code>ViewCookie</code>.
     *
     * @param cookie cookie to activate
     * @param cookieClass type of the cookie - specifies action to activate
     * @param line used only by <code>EditorCookie</code>s&nbsp;- specifies
     * initial line to open the file at
     * @return  <code>true</code> if the cookie was successfully activated,
     * <code>false</code> if some error occurred
     * @exception java.lang.IllegalArgumentException if <code>cookieClass</code>
     * is not any of <code>EditorCookie</code>, <code>OpenCookie</code>,
     * <code>ViewCookie</code>
     * @exception java.lang.ClassCastException if the <code>cookie</code> is not
     * an instance of the specified cookie class
     */
    protected boolean openByCookie(final Node.Cookie cookie,
            final Class<?> cookieClass,
            final int line) {
        assert EventQueue.isDispatchThread();

        if ((cookieClass == EditorCookie.Observable.class)
                || (cookieClass == EditorCookie.class)) {
            return openEditor((EditorCookie) cookie, line);
        } else if (cookieClass == OpenCookie.class) {
            ((OpenCookie) cookie).open();
        } else if (cookieClass == EditCookie.class) {
            ((EditCookie) cookie).edit();
        } else if (cookieClass == ViewCookie.class) {
            ((ViewCookie) cookie).view();
        } else {
            throw new IllegalArgumentException();
        }
        return true;
    }

    /**
     * Tries to open the specified file, using one of <code>EditorCookie</code>,
     * <code>OpenCookie</code>, <code>EditCookie</code>, <code>ViewCookie</code>
     * (in the same order). If the client of the open file server wants, waits
     * until the file is closed and notifies the client.
     *
     * @param dataObject  <code>DataObject</code> representing the file
     * @param line if <code>EditorCookie</code> is used, specifies initial line
     * to open the file at
     * @return  <code>true</code> if the file was successfully open,
     * <code>false</code> otherwise
     */
    private boolean openDataObjectByCookie(final DataObject dataObject,
            final int line) {
        if (dataObject.getCookie(OpenCookie.class) != null) {
            return openByCookie(dataObject.getCookie(OpenCookie.class), OpenCookie.class, line);
        } else if (dataObject.getCookie(EditCookie.class) != null) {
            return openByCookie(dataObject.getCookie(EditCookie.class), EditCookie.class, line);
        } else if (dataObject.getCookie(ViewCookie.class) != null) {
            return openByCookie(dataObject.getCookie(ViewCookie.class), ViewCookie.class, line);
        } else {
            return false;
        }
    }

    /**
     * This method is called when it is rescheduled to the AWT thread. (from a
     * different thread). It is always run in the AWT thread.
     */
    @Override
    public void run() {
        assert EventQueue.isDispatchThread();

        open(fileObject, line);
    }

    /**
     * Opens the <code>FileObject</code> either by calling {@link EditorCookie}
     * (or {@link OpenCookie} or {@link ViewCookie}), or by showing it in the
     * Explorer.
     */
    @Override
    public boolean open(final FileObject fileObject, final int line) {
        if (LOGGER.isLoggable(FINER)) {
            LOGGER.log(FINER, "open({0}, line={1}) called from thread {2}",//NOI18N
                    new Object[]{fileObject.getNameExt(),
                        line, Thread.currentThread().getName()});
        }

        /* Ensure file opening happens on the EDT */
        if (!EventQueue.isDispatchThread()) {
            LOGGER.finest(" - rescheduling to EDT using invokeLater(...)");//NOI18N
            EventQueue.invokeLater(
                    new DefaultOpenFileImpl(fileObject, line));
            return true;
        }

        assert EventQueue.isDispatchThread();
        LOGGER.finest(" - yes, it is the EDT");                            //NOI18N

        /* Find a DataObject for the FileObject */
        final DataObject dataObject;
        try {
            dataObject = DataObject.find(fileObject);
        } catch (final DataObjectNotFoundException ex) {
            ErrorManager.getDefault().notify(ex);
            return false;
        }

        /* Set a status to notify of file opening */
        final String fileName = fileObject.getNameExt();
        StatusDisplayer.getDefault().setStatusText(
                NbBundle.getMessage(DefaultOpenFileImpl.class,
                        "MSG_opening", //NOI18N
                        fileName));

        /* Look for an EditorCookie indicating a text file */
        if ((line != -1) && dataObject.getCookie(EditorCookie.Observable.class) != null) {
            return openByCookie(dataObject.getCookie(EditorCookie.Observable.class), EditorCookie.Observable.class, line);
        }

        if (dataObject.getCookie(EditorCookie.class) != null) {
            return openByCookie(dataObject.getCookie(EditorCookie.class), EditorCookie.class, line);
        }

        /* Attempt to open the DataObject using its default action */
        final Node dataNode = dataObject.getNodeDelegate();
        Action action = dataNode.getPreferredAction();
        if ((action != null)
                && !(action instanceof FileSystemAction)
                && !(action instanceof ToolsAction)) {
            if (LOGGER.isLoggable(FINEST)) {
                LOGGER.log(FINEST, " - using preferred action " //NOI18N
                        + "(\"{0}\" - {1}) for opening the file", //NOI18N
                        new Object[]{action.getValue(Action.NAME),
                            action.getClass().getName()});
            }

            if (action instanceof ContextAwareAction) {
                action = ((ContextAwareAction) action)
                        .createContextAwareInstance(dataNode.getLookup());
                if (LOGGER.isLoggable(FINEST)) {
                    LOGGER.finest("    - it is a ContextAwareAction");     //NOI18N
                    LOGGER.log(FINEST, "    - using a context-aware " //NOI18N
                            + "instance instead (\"{0}\" - {1})", //NOI18N
                            new Object[]{action.getValue(Action.NAME),
                                action.getClass().getName()});
                }
            }

            LOGGER.finest("   - will call action.actionPerformed(...)");   //NOI18N
            final Action a = action;
            final Node n = dataNode;
            WindowManager.getDefault().invokeWhenUIReady(() -> a.actionPerformed(new ActionEvent(n, 0, "")));

            return true;
        }

        /* Look for an OpenCookie, EditCookie or ViewCookie indicating an openable file */
        final boolean success = openDataObjectByCookie(dataObject, line);
        if (success) {
            return true;
        }

        /* If the file is a folder or archive, open it in an explorer window */
        if (fileObject.isFolder() || FileUtil.isArchiveFile(fileObject)) {
            final Node node = dataObject.getNodeDelegate();
            if (node != null) {
                WindowManager.getDefault().invokeWhenUIReady(() -> NodeOperation.getDefault().explore(node));
                return true;
            }
        }

        return false;
    }
}
