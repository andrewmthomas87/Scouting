#!/bin/bash

git config --global branch.autosetuprebase never
git config --unset branch.master.rebase
git config remote.origin.push HEAD
git config --global core.autocrlf input
git config --global push.default tracking