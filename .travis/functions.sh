#!/usr/bin/env bash

#
# A collection of utility functions that can be used by other  scripts
#

title()
{
    #
    # Display a title with a black text and white background
    #

    local fg_black=$(tput setaf 0)
    local bg_white=$(tput setab 7)
    local clr=$(tput sgr0)
    local dots="------------------------------------------------------------------------------"

    echo -e "\n${fg_black}${bg_white}\n${dots}\n${1}\n${dots}${clr}\n"
}
