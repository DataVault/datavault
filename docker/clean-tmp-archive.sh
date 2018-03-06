#! /bin/bash
if [ "$EUID" -ne 0 ]
	then echo "Please run as root"
	exit
fi

echo "Clean archived files"
rm -rf ./tmp/datavault/archive/*
echo "Clean meta files"
rm -rf ./tmp/datavault/meta/*
echo "Clean temp files"
rm -rf ./tmp/datavault/temp/*
