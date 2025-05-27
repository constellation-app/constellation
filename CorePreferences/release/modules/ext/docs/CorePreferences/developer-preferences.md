# Developer Preferences

Developer preferences can be accessed via Setup -> Options ->
CONSTELLATION -> Developer.

<div style="text-align: center">
<img src="../ext/docs/CorePreferences/resources/developerPanel.png" alt="Developer Options Panel" />
</div>

## Memory Usage

Constellation is a java application which means that the memory
management is handled by the Garbage Collector (GC) process inside the
Java Virtual Machine (JVM). It is best left to the GC to decide when to
release unused memory but we can give a hint to the GC to clear unused
memory when a graph is open or closed.

The following options have been enabled by default and does not
guarantee that any garbage collection will be performed because it is
ultimately up to the GC process to decide. A side effect of having this
enabled means that opening and closing graphs could take longer to
complete if the GC decides to clean up unused memory on a open or close.

-   Garbage Collect GC on graph open
-   Garbage Collect GC on graph close

## Open GL

The Constellation graph is rendered via OpenGL so having extra debug
information can be useful for troubleshooting or to see what is
happening internally. We've provided a few options from this preferences
section to include debugging options:

-   *Debug GL*: Prints out various debug parameters and settings
    detected by Constellation to the log file at start up.
-   *Print GL Capabilities*: Outputs the available GL capabilities
    detected by the graphics card to the log file.
-   *Display FPS Counter*: Have a frames per second counter visible on
    the bottom right corner showing you the frame rate for the
    Constellation graph. If you run the Experimental -> Animations ->
    Fly through then you can get a sense of the maximum frame rate
    possible with your graphics card.

## Master Reset Constellation

Constellation is built on top of Apache Netbeans with a number of module
suites and modules within those module suites. Running this option is a
distruptive change that will uninstall all Constellation modules,
stripping the application down to just the NetBeans runtime libraries.

NOTE: This option does not work for the standalone version of
Constellation and it rather applicable to the version of Constellation
that is continuously updating via the update site mechanism. This
version is called the "Constellation Shell".


