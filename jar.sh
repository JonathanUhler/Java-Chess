#!/bin/bash


mkdir -p bin
rm -rf bin/*

jar cmf 'manifest.mf' bin/Chess.jar -C obj/ . -C src/ assets/

