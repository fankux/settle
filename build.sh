#!/usr/bin/env bash

# fetch ffmpeg binaries
if [ ! -d ./ffmpeg ]; then
    mkdir ./ffmpeg
fi

rm -rf ./ffmpeg/*
cd ffmpeg && wget https://ffmpeg.org/releases/ffmpeg-3.4.2.tar.gz -O ffmpeg.tar.gz && tar -zxf ffmpeg.tar.gz && mv ffmpeg-3.4.2/* . && rm -r ffmpeg-3.4.2/
if [ $? -ne 0 ]; then
    rm -f ffmpeg.tar.gz
    print 'get ffmpeg failed'
    exit -1
fi

rm -f ffmpeg.tar.gz

# compile project
#mvn clean package -Dmaven.skip.test=true
