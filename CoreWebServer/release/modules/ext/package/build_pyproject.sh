#!/bin/bash

## If you need to install python and have internet access, you can uncomment the
## relevant lines below to install the specific version using the URL.
## Define the download URL (check the official website for the latest version)
## Assuming that there is internet access
#PYTHON_URL="https://www.python.org/ftp/python/3.14.3/python-3.14.3-amd64.exe" #
#DOWNLOAD_DIR="$HOME/Downloads"
#INSTALLER_PATH="$DOWNLOAD_DIR/$(basename $PYTHON_URL)"

## Create download directory if it doesn't exist
#mkdir -p "$DOWNLOAD_DIR"

## Download the installer
#echo "Downloading Python installer ${PYTHON_URL} into ${DOWNLOAD_DIR}..."
#curl -o "$INSTALLER_PATH" "$PYTHON_URL"

## Run the installer silently with options to add to PATH and install for all users (requires admin rights)
#echo "Running installer... (requires UAC prompt)"
## Note: Bash on Windows might need winpty or explicit invocation of the .exe
## The following command runs the Windows executable with specific silent install arguments
#echo "Start installing from ${INSTALLER_PATH}..."
#""$INSTALLER_PATH"" /verbose InstallAllUsers=1 PrependPath=1 Include_test=0
#echo "Finished installation!"


# ensure the latest pip is installed
py -m pip install --upgrade pip

# ensure the latest build version is installed
py -m pip install --upgrade build

# build and put the packages in package_dist
py -m build --outdir ./package_dist

pip list --user

read -p "Press [Enter] key to continue..."