# Constellation
Constellation is a graph-focused data visualisation and interactive analysis application enabling data access, federation and manipulation capabilities across large and complex data sets.

## Vision Statement

Constellation is a first class, domain agnostic data visualisation and analysis application 
enabling the user to solve large and complex data problems in a simple and intuitive way.

* ***Users***: data analysts, data scientists, and all people interested in graph data analysis.
* ***Data analysis domains***: graph datasets with rich feature data e.g. social networks, network infrastructure, chemical composition, etc.

![Constellation Application](docs/screenshot.png)

## Prerequisites

* Constellation requires at least Open JDK 8 with JFX 8 support build to be installed and is known to work on Windows 64-bit and Linux 64-bit.
* The OpenGL graph display works with NVIDIA and ATI graphics cards that support 
OpenGL 3.3 or later. It is known to not work with Intel on-board graphics cards.

## Download Constellation

Download Constellation by going to the [release page](https://github.com/constellation-app/constellation/releases).

## Run Constellation

Unzip the constellation.zip bundle and double click the `bin/constellation64.exe` for Windows or
run the `bin\constellation` shell script for Linux.

There is currently no support for Mac and the feature request is tracked by [Issue #21](https://github.com/constellation-app/constellation/issues/21).

## Build Constellation

To build Constellation from source code do the following:

* Download NetBeans 8.2
* Download the Open JDK 8 with JFX 8 from Azul website for [Windows 64 bit build](https://cdn.azul.com/zulu/bin/zulu8.38.0.13-ca-fx-jdk8.0.212-win_x64.zip) or [Linux 64 bit build](https://cdn.azul.com/zulu/bin/zulu8.38.0.13-ca-fx-jdk8.0.212-linux_x64.tar.gz)
* Clone this repository
* Update the netbeans.conf file's netbeans_jdkhome entry (The file can be found under <C:\Program Files\NetBeans 8.2\etc> folder)
* Open the Constellation_Core module suite from NetBeans
* Important Files -> build.xml -> Right click -> Update dependencies and clean build
* Right click -> Run

## Package Constellation

To package Constellation in a zip bundle do the following:

* In NetBeans, expand Constellation_Core -> Important Files
* Right click on Build Script and run the build-zip target

This will create a dist/constellation.zip file. It does not contain the JRE as 
that depends on the platform you wish to run it on.

The JRE location Constellation looks for is defined in `etc/constellation.conf`.
For example, if you wanted to package a JRE in the zip bundle, copy the jre into 
the same folder level as bin and update the `jdkhome` variable to the name of 
the JRE folder.

## Common Troubleshooting Checks

* You can check whether your graphics card is supported by following these steps:

1. Click on `File` > `New Graph` to create a graph
1. Click on `Experimental` > `Build Graph` > `Sphere Graph` to create a random graph.
1. If you can see a graph try to interact with it using the mouse.
1. If the graph view remains blank, you may not have a supported graphics card.

* Click on `Help` > `JOGL Version` to see the graphics card capabilities of your machine.
* Click on `Help` > `Show Logs` to view Constellation log information.


## Contributing to Constellation

For more information please see the [contributing guide](CONTRIBUTING.md).

## Acknowledgments

Third party libraries and assets were used in development of Constellation, please view [attribution list](ATTRIBUTION.md) for details. 
