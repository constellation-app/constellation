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

* The OpenGL graph display works with NVIDIA and ATI graphics cards that support
OpenGL 3.3 or later. It is known to not work with older Intel on-board graphics cards.

## Download Constellation

Download Constellation by going to the [release page](https://github.com/constellation-app/constellation/releases).

## Run Constellation

Unzip the constellation.zip bundle and double click the `constellation64.bat` 
for Windows or run the `bin\constellation` shell script for Linux and MacOSX.

## Build Constellation

To build Constellation from source code do the following:

* Download Azul's Zulu distribution of JDK 11 with JFX 11, either the 
[Windows 64 bit](https://cdn.azul.com/zulu/bin/zulu11.37.19-ca-fx-jre11.0.6-win_x64.zip),
[Linux 64 bit](https://cdn.azul.com/zulu/bin/zulu11.37.19-ca-fx-jre11.0.6-linux_x64.tar.gz)
or [MacOSX 64 bit](https://cdn.azul.com/zulu/bin/zulu11.37.19-ca-fx-jre11.0.6-macosx_x64.tar.gz)

* Download [NetBeans 12](https://netbeans.apache.org/download/nb120/nb120.html)
* Update `netbeans_jdkhome` in netbeans.conf to point to the Azul Zulu JDK you 
downloaded (e.g. `C:\Program Files\NetBeans-12\netbeans\etc`)
* Apache NetBeans dropped support for the Java Help system but is still required 
by Constellation. Until we find a solution to #15 there are 2 jar files that 
have to be manually copied into the NetBeans installation folder.
  * Download [org-netbeans-modules-javahelp.jar](https://github.com/constellation-app/third-party-dependencies/blob/master/NetBeans%20Help/org-netbeans-modules-javahelp.jar?raw=true) to `C:\Program Files\NetBeans 12\platform\modules` (using Windows as an example). Note that you will need to override this file when prompted.
  * Download [jhall-2.0_05.jar](https://github.com/constellation-app/third-party-dependencies/blob/master/NetBeans%20Help/jhall-2.0_05.jar?raw=true) to `C:\Program Files\NetBeans 12\netbeans\platform\modules\ext` (using Windows as an example)
* Clone this repository
* Open the Constellation module suite from NetBeans
* In the Projects view, expand `Important Files` > `Build Script` > Right click > 
`Update dependencies and clean build`. This can take around 20 minutes to 
download the first time depending on your internet connection so feel free to 
get a :coffee: and come back later.
* Start Constellation by right clicking on `Constellation` > `Run`

## Package Constellation

To package Constellation in a zip bundle do the following:

* In NetBeans, expand `Constellation` > `Important Files`
* Right click on `Build Script` and run the `build-zip`
* Navigate to the `dist` folder to get `constellation.zip` file
* If you want to use a specific JRE (e.g. `zulu11.37.19-ca-fx-jre11.0.6-win_x64`) 
then copy this to the same level as the `bin` folder and call it `jre`. This is 
the default folder name Constellation expects but you can change this from 
`etc\constellation.conf`.

Note that Constellation "Core" (which is this repository) is designed to be 
domain agnostic and work standalone. The version of Constellation available for 
download from [the official website](https://constellation-app.com) is built 
with additional plugins and managed via the [Constellation-Applications](https://github.com/constellation-app/constellation-applications) repository.

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
