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
package au.gov.asd.tac.constellation.visual.vulkan.utils;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.FileHandler;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class CVKGraphLogger {
    // Static logger used by static code like shader loading or by code that doesn't know
    // what graph it is working for.    
    private final static Level DEFAULT_LOG_LEVEL = Level.ALL;    
    public final static Logger CVKLOGGER = CreateNamedFileLogger("CVK", DEFAULT_LOG_LEVEL);    
    
    // Variables for the per graph loggers
    private static final boolean INDIVIDUAL_LOGS = true; 
    private static int NEXT_LOGGER_ID = 0;
    private final int loggerId;
    private int indentation = 0;
    private final Logger graphLogger;
    
        
    // ========================> Classes <======================== \\
    
    public static class CVKGraphLogRecord extends LogRecord {
        public final int loggerId;
        public final int indentation;
        public final boolean formatted;
        public boolean graphAnnotation;
        
        public CVKGraphLogRecord(Level level, String msg, final int loggerId) {
            this(level, msg, loggerId, 0, true, true);
        }        
        public CVKGraphLogRecord(Level level, String msg, final int loggerId, final int indentation) {
            this(level, msg, loggerId, indentation, true, true);
        }
        public CVKGraphLogRecord(Level level, String msg, final int loggerId, final int indentation, boolean formatted, boolean graphAnnotation) {
            super(level, msg);
            this.loggerId = loggerId;
            this.indentation = indentation;
            this.formatted = formatted;
            this.graphAnnotation = graphAnnotation;
        }        
    }    
    
    public static class CVKGraphLogFormatter extends Formatter {
        public static int indent = 0;
        public final static int PADLEN = 40;
        
        @Override
        public String format(LogRecord record) {
            // This does all the VA_ARGS formatting
            String msg = formatMessage(record);
            
            // Don't fuss about with line and file for empty lines
            if (msg.isBlank()) {
                return msg;
            }
            
            final StackTraceElement[] stackTrace = Thread.currentThread().getStackTrace();
            final StringBuilder lineBuilder = new StringBuilder();
            final int indentation;
            int stackLevel = 8; // A guesstimation based on a few observations
            final boolean formatting;
            final boolean graphAnnotation;           
            
            if (record instanceof CVKGraphLogRecord) {
                CVKGraphLogRecord graphRecord = (CVKGraphLogRecord)record;
                indentation = graphRecord.indentation;
                formatting = graphRecord.formatted;
                graphAnnotation = graphRecord.graphAnnotation;
                if (graphAnnotation) {
                    lineBuilder.append(String.format("[graph %d] ", graphRecord.loggerId));
                }
                
                // Find the first call before CVKGraphLogger
                int level = 0;
                boolean encounteredCVKGraphLogger = false;
                for (var el : stackTrace) {
                    if (el.getClassName().endsWith("CVKGraphLogger")) {
                        encounteredCVKGraphLogger = true;
                    } else if (encounteredCVKGraphLogger) {
                        stackLevel = level;
                        break;
                    }
                    level++;
                }
            } else {
                indentation = indent;
                formatting = true;
                graphAnnotation = true;
                
                // Find the first constellation call (after this log format method)
                int level = 0;
                boolean encounteredConstellation = false;
                for (var el : stackTrace) {
                    final String className = el.getClassName();
                    if (className.contains(".constellation.") && !className.endsWith("CVKGraphLogFormatter")) {
                        stackLevel = level;
                        break;
                    }
                    ++level;
                }                
            }
                                    
            if (formatting) {
                // With a grand total of 1 observation, the call we are interested
                // is at element 8                
                if (stackTrace.length >= stackLevel) {
                    StackTraceElement ste = stackTrace[stackLevel];
                    String fileAndLine = String.format("%s:%d>", ste.getFileName(), ste.getLineNumber());
                    lineBuilder.append(fileAndLine);                   

                    // Right pad (additional to indent padding) up to PADLEN so we have 
                    // table like output for records at the same indent level                
                    int padding = PADLEN - fileAndLine.length();
                    for (int i = 0; i < padding; ++i) {
                        lineBuilder.append(" ");
                    }                
                }   
                
                // StartLogSection increments indent                 
                for (int i = 0; i < indentation; ++i) {
                    lineBuilder.append("    ");
                }

                if (record.getLevel() == Level.WARNING) {
                    lineBuilder.append("!WARNING! ");
                }
            }
            
            StringBuilder stringBuilder = new StringBuilder();
            
            // Really make SEVERE stand out
            if (formatting && record.getLevel() == Level.SEVERE) {
                stringBuilder.append("\r\n======================================================= SEVERE =======================================================\n");
            }            
            
            String msgLines[] = msg.split("\\r?\\n");
            for (String msgLine : msgLines) {
                if (!msgLine.isBlank()) {
                    stringBuilder.append(lineBuilder.toString());
                    stringBuilder.append(msgLine);
                }
                stringBuilder.append(System.getProperty("line.separator"));
            }             
            
            // Really make SEVERE stand out
            if (formatting && record.getLevel() == Level.SEVERE) {
                stringBuilder.append("======================================================================================================================\r\n\r\n");
            } 
            
            return stringBuilder.toString();
        }         
    }     
    
    
    // ========================> Static Logging <======================== \\
       
    public static Logger CreateNamedFileLogger(String name, Level level) {        
        Logger logger = Logger.getLogger(name);
        logger.setUseParentHandlers(false);
        logger.setLevel(level);

        // Delete old log
        final String logName = String.format("%s.log", name);
        try {         
            File oldLog = new File(logName);
            oldLog.delete();                
        }  
        catch(Exception e) {   
            // old log doesn't exist or is locked, oh well keep going
        }  

        try {
            FileHandler fileHandler = new FileHandler(logName);
            fileHandler.setFormatter(new CVKGraphLogFormatter());
            logger.addHandler(fileHandler);              
        } catch (IOException e) {
            logger.log(Level.WARNING, "Logger failed to create {0}, exception: {1}",
                    new Object[]{logName, e.toString()});
        }

        StreamHandler streamHanlder = new StreamHandler(System.out, new CVKGraphLogFormatter());
        logger.addHandler(streamHanlder);   
        
        return logger;
    }
        
//    public static void StaticStartLogSection(String msg) {
//        CVKLOGGER.log(Level.INFO, "{0}---- START {1} ----", new Object[]{System.getProperty("line.separator"), msg});        
//        ++CVKGraphLogFormatter.indent;
//    }
//    
//    public static void StaticEndLogSection(String msg) {
//        --CVKGraphLogFormatter.indent;
//        CVKLOGGER.log(Level.INFO, "---- END {1} ----{0}{0}", new Object[]{System.getProperty("line.separator"), msg});        
//    }    
    
    
    // ========================> Per Graph Logging <======================== \\
    
    public CVKGraphLogger(String graphId) {
        this.loggerId = NEXT_LOGGER_ID++;
        if (INDIVIDUAL_LOGS) {
            graphLogger = CreateNamedFileLogger(String.format("CVK_graph_%d", loggerId), DEFAULT_LOG_LEVEL);
        } else {
            graphLogger = null;
        }        
        DoLog(Level.SEVERE, String.format("Graph %s using logger %d", graphId, loggerId), loggerId, 0, false);
    }
    
    private void DoLog(Level level, String msg) {
        DoLog(level, msg, loggerId, indentation, true);
    }
    
    private void DoLog(Level level, String msg, final int loggerId, final int indentation, boolean formatted) {
        CVKGraphLogRecord record = new CVKGraphLogRecord(level, msg, loggerId, indentation, formatted, true);
        CVKLOGGER.log(record);
        if (graphLogger != null) {
            record.graphAnnotation = false;
            graphLogger.log(record);
        }
    }    
    
    public void log(Level level, String format, Object... args) {
       if (CVKLOGGER.isLoggable(level))  {
           String msg = String.format(format, args);
           DoLog(level, msg);
       }     
    }
    
    public void log(Level level, String msg) {
        if (CVKLOGGER.isLoggable(level))  {
            DoLog(level, msg);
        }
    }   
             
    public void severe(String msg) {
       log(Level.SEVERE, msg); 
    }
    public void severe(String format, Object... args) {
       log(Level.SEVERE, format, args); 
    }    
    public void warning(String msg) {
       log(Level.WARNING, msg); 
    }
    public void warning(String format, Object... args) {
       log(Level.WARNING, format, args); 
    }      
    public void info(String msg) {
       log(Level.INFO, msg); 
    }
    public void info(String format, Object... args) {
       log(Level.INFO, format, args); 
    }
    public void config(String msg) {
       log(Level.INFO, msg); 
    }
    public void config(String format, Object... args) {
       log(Level.INFO, format, args); 
    }    
    public void fine(String msg) {
       log(Level.FINE, msg); 
    }
    public void fine(String format, Object... args) {
       log(Level.FINE, format, args); 
    }  
    public void finer(String msg) {
       log(Level.FINER, msg); 
    }
    public void finer(String format, Object... args) {
       log(Level.FINER, format, args); 
    }  
    public void finest(String msg) {
       log(Level.FINEST, msg); 
    }
    public void finest(String format, Object... args) {
       log(Level.FINEST, format, args); 
    }       
    
    public void StartLogSection(String msg) {
        StartLogSection(DEFAULT_LOG_LEVEL, msg);
    }
    
    public void StartLogSection(Level level, String msg) {
        if (CVKLOGGER.isLoggable(level))  {              
            log(level, "%s---- START %s ----", System.getProperty("line.separator"), msg);
            ++indentation;
        }
    }
    
    public void EndLogSection(String msg) {
        EndLogSection(DEFAULT_LOG_LEVEL, msg);       
    }    
    
    public void EndLogSection(Level level, String msg) {
        if (CVKLOGGER.isLoggable(level))  {
            indentation = Math.max(0, indentation - 1);
            log(level, "---- END %s ----", msg);
            log(level, System.getProperty("line.separator"));
        }     
    }    
    
    public void LogException(Exception exception, String format, Object... args) {
        String msg = String.format(format, args);
        LogException(exception, msg);
    }
    
    public void LogException(Exception exception, String msg) {
        final StringWriter exceptionTraceWriter = new StringWriter();
        final PrintWriter exceptionPrintWriter = new PrintWriter( exceptionTraceWriter );
        exception.printStackTrace(exceptionPrintWriter);
        exceptionPrintWriter.flush();
        severe("\r\n%s\r\n%s", msg, exceptionTraceWriter.toString());
    }
}
