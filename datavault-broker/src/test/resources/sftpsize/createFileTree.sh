#!/bin/bash

# This is used to create files in a SFTP Container.
# These files are used to test SFTPFileSystemDriver.getSize
# See CombinedSizeSFTPJSchIT
WD=$1
DEPTH=$2

echo "WorkingDir is [$WD]"
echo "Depth is [$DEPTH]"
FILES_CREATED=0
function createFiles() {
  local base=$1
  local depth=$2
  #echo  "in createFiles ${base} ${depth}"

  echo "aaa" > $base/a.txt
  #ls -l $base/a.txt
  echo "bbb" > $base/b.txt
  #ls -l $base/b.txt
  FILES_CREATED=$((FILES_CREATED+2))
  if (( $depth > 0 )); then
    local left="${base}/L"
    local right="${base}/R"
    local nextDepth=$((depth-1))
    mkdir -p $left
    createFiles $left $nextDepth
    mkdir -p $right
    createFiles $right $nextDepth
  fi
}
createFiles $WD $DEPTH
echo "FILES_CREATED ${FILES_CREATED}"