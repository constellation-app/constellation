#!/usr/bin/env bash

#
# A collection of utility functions that can be used by other scripts
#

title()
{
    #
    # Display a title with dash separators
    #

    local dots="------------------------------------------------------------------------------"
    echo -e "\n${dots}\n${1}\n${dots}\n"
}
