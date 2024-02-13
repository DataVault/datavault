#!/usr/bin/env bash

cd $1
echo "cwd is $PWD"
for fname in *.ftl; do
    prefix=$(basename "$fname" '.ftl')
    newfile="${prefix}.html"
    echo "prefix is ${prefix}, fname is ${fname}, newfile is ${newfile}"
    git mv $fname $newfile
done
