# Constellation Design Decisions

<!--

A list of common design decisions will be listed below. If there is anything 
specific you would like answered then create a GitHub issue with what you want 
answered and we can start to populate this file.

Constellation is a flexible, pluggable application so you are largely able to
develop as you wish. However there are some specific things to do the 
"Constellation Way" which will be documented here.

-->

1. Wrap functionality around a plugin whenever possible

Constellation has a build-in plugin framework which is multi-threaded and allows 
you to run plugins by making a `PluginExecution` or `PluginExecutor` call. 
Wrapping functionality into plugins like ***selecting all nodes*** 
(`SelectAllPlugin`) or performing a ***grid arrangement*** 
(`ArrangeInGridGeneralPlugin`) means that they can not only be re-used within
your own plugin, but can be called via the built-in Scripting View or RESTful
webservice (for example via a Jupyter Notebook).

