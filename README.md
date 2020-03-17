<p align="center">
  <img src="./docs/constellation-logo.png"/>
  </p>
<p align="center">
  <a href="https://travis-ci.com/constellation-app/constellation" alt="travis-ci">
    <img src="https://travis-ci.com/constellation-app/constellation.svg?branch=master"/>
  </a>
  <!--  <a href="https://sonarcloud.io/dashboard?id=constellation-app_constellation" alt="Quality Gate Status">
    <img src="https://sonarcloud.io/api/project_badges/measure?project=constellation-app_constellation&metric=alert_status"/>
  </a>    -->
  <a href="https://github.com/constellation-app/constellation/releases" alt="Release downloads">
    <img src="https://img.shields.io/github/downloads/constellation-app/constellation/total.svg"/>
  <a href="https://github.com/constellation-app/constellation/blob/master/CONTRIBUTING.md" alt="contributions welcome">
    <img src="https://img.shields.io/badge/contributions-welcome-brightgreen.svg"/>
  </a>
  <a href="https://github.com/constellation-app/constellation/blob/master/LICENSE" alt="license">
    <img src="https://img.shields.io/github/license/constellation-app/constellation.svg"/>
  </a>
</p>

Constellation is a graph-focused data visualisation and interactive analysis 
application enabling data access, federation and manipulation capabilities 
across large and complex data sets.

# Table of Contents

- [Vision Statement](#vision-statement)
- [Prerequisites](#prerequisites)
- [Download Constellation](#download-constellation)
- [Run Constellation](#run-constellation)
- [Build Constellation](#build-constellation)
- [Package Constellation](#package-constellation)
- [Contributing to Constellation](#contributing-to-constellation)
- [Documentation](#documentation)
- [Training](#training)
- [Common Troubleshooting Checks](#common-troubleshooting-checks)
- [Acknowledgments](#acknowledgments)

## Vision Statement

Constellation is a first class, domain agnostic data visualisation and analysis 
application enabling the user to solve large and complex data problems in a 
simple and intuitive way.

* ***Users***: data analysts, data scientists, and all people interested in 
graph data analysis.
* ***Data analysis domains***: graph datasets with rich feature data e.g. social 
networks, network infrastructure, chemical composition, etc.

![Constellation Application](docs/screenshot.png)

## Prerequisites

* Constellation is known to work on Windows 64-bit and Linux 64-bit. There is 
currently no support for Mac and the feature request is tracked by 
[Issue #21](https://github.com/constellation-app/constellation/issues/21).
* The OpenGL graph display works with NVIDIA and ATI graphics cards that support
OpenGL 3.3 or later. It is known to not work with Intel on-board graphics cards.

## Download Constellation

Download Constellation by going to the [release page](https://github.com/constellation-app/constellation/releases).

## Run Constellation

Unzip the constellation.zip bundle and double click the `bin/constellation64.exe` 
for Windows or run the `bin\constellation` shell script for Linux.

There is currently no support for Mac and the feature request is tracked by 
[Issue #21](https://github.com/constellation-app/constellation/issues/21).

## Build Constellation

* Constellation requires at least Open JDK 11 with JFX 11 support build to be 
installed and is known to work on Windows 64-bit and Linux 64-bit.

To build Constellation from source code do the following:

* Download [NetBeans 11.3](https://netbeans.apache.org/download/nb113/nb113.html)
* Download Azul's Zulu distribution of Open JDK 11 with JFX 11, either the 
[Windows 64 bit build](https://cdn.azul.com/zulu/bin/zulu11.37.19-ca-fx-jdk11.0.6-win_x64.zip) 
or [Linux 64 bit build](https://cdn.azul.com/zulu/bin/zulu11.37.19-ca-fx-jdk11.0.6-linux_x64.tar.gz)
* Update the netbeans.conf file's netbeans_jdkhome entry (the file can be found 
 under the `NetBeans-11.3-bin\netbeans\etc` directory)
* Clone this repository
* Open the Constellation_Core module suite from NetBeans
* In the Projects view, expand `Important Files` > `Build Script` > Right click > 
`Update dependencies and clean build`
* Right click > `Run`

## Package Constellation

To package Constellation in a zip bundle do the following:

* In NetBeans, expand `Constellation_Core` > `Important Files`
* Right click on `Build Script` and run the `build-zip-with-windows-jre` or 
`build-zip-with-linux-jre` target

The packaged file should be available from the `dist` directory.

## Contributing to Constellation

For more information please see the [contributing guide](CONTRIBUTING.md).

## Documentation

* Constellation 101 Slides _(coming soon)_
* Overview Video _(coming later)_
* [Quick Start Guide](docs/Constellation_Quick_Start_Guide.pdf)
* User Guide _(coming later)_
* Built in documentation to Constellation exists

## Training

* Basics (Buttonology) _(coming later)_
* Social Network Analysis _(coming later)_
* [Developer Guide](https://github.com/constellation-app/constellation-training/blob/master/CONSTELLATION%20Developer%20Guide.pdf)
* [Example Module Template](https://github.com/constellation-app/constellation-module-example)

## Common Troubleshooting Checks

* You can check whether your graphics card is supported by following these steps:

1. Click on `File` > `New Graph` to create a graph
1. Click on `Experimental` > `Build Graph` > `Sphere Graph` to create a random graph.
1. If you can see a graph try to interact with it using the mouse.
1. If the graph view remains blank, you may not have a supported graphics card.

* Click on `Help` > `JOGL Version` to see the graphics card capabilities of your machine.
* Click on `Help` > `Show Logs` to view Constellation log information.

## Acknowledgments

Third party libraries and assets were used in development of Constellation, 
please view [attribution list](ATTRIBUTION.md) for details.
