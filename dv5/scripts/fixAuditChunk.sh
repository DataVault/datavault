#!/usr/bin/env bash

deposit_id=$1
chunk_num=$2
bag_id=$3
location_of_bad_chunk=$4

usage() {
  echo "Usage: $0 <deposit_id> <chunk_num> <bag_id> <location_of_bad_chunk>"
}

if [ -z "$deposit_id" ]; then
  echo "deposit_id is required"
  usage
  exit 1
fi

if [ -z "$chunk_num" ]; then
  echo "chunk_num is required"
  usage
  exit 1
fi

if [ -z "$bag_id" ]; then
  echo "bag_id is required"
  usage
  exit 1
fi

if [ -z "$location_of_bad_chunk" ]; then
  echo "location_of_bad_chunk is required"
  usage
  exit 1
fi

case "$location_of_bad_chunk" in
  1) location_of_good_chunk=2 ;;
  2) location_of_good_chunk=1 ;;
  *)
    echo "location_of_bad_chunk must be 1 or 2"
    usage
    exit 1
    ;;
esac

/usr/bin/dsmc retrieve /datavault/temp/"$deposit_id"."$chunk_num"/"$bag_id".tar."$chunk_num" /datavault/temp/"$deposit_id"."$chunk_num"/"$bag_id".tar."$chunk_num" -description="$deposit_id"."$chunk_num" -optfile=/home/lacdv/.TSM/opt/dsm"$location_of_good_chunk".opt -replace=true
retrieve_rc=$?
if [ $retrieve_rc -ne 0 ]; then
  echo "Failed to retrieve chunk [/datavault/temp/$deposit_id.$chunk_num/$bag_id.tar.$chunk_num] to [/datavault/temp/$deposit_id.$chunk_num/$bag_id.tar.$chunk_num] using optfile [/home/lacdv/.TSM/opt/dsm$location_of_good_chunk.opt]. dsmc returned [$retrieve_rc]"
  exit $retrieve_rc
fi

#what about the move stuff? Is that needed?  Also sort the timestamp stuff so it is taken from the OS but passed in.
#sort the location params too make one param either true (1 )or false (0) for 1 bad 2 good or some way to ensure if param is 1 or 2 the other is non used value

/usr/bin/dsmc delete archive /datavault/temp/"$deposit_id"."$chunk_num"/"$bag_id".tar."$chunk_num" -description="$deposit_id"."$chunk_num" -optfile=/home/lacdv/.TSM/opt/dsm"$location_of_bad_chunk".opt -noprompt
delete_rc=$?
if [ $delete_rc -ne 0 ]; then
  echo "Failed to delete archived chunk [/datavault/temp/$deposit_id.$chunk_num/$bag_id.tar.$chunk_num] using optfile [/home/lacdv/.TSM/opt/dsm$location_of_bad_chunk.opt]. dsmc returned [$delete_rc]. Continuing."
fi

/usr/bin/dsmc archive /datavault/temp/"$deposit_id"."$chunk_num"/"$bag_id".tar."$chunk_num" /datavault/temp/"$deposit_id"."$chunk_num"/"$bag_id".tar."$chunk_num" -description="$deposit_id"."$chunk_num" -optfile=/home/lacdv/.TSM/opt/dsm"$location_of_bad_chunk".opt
archive_rc=$?
if [ $archive_rc -ne 0 ]; then
  echo "Failed to archive chunk [/datavault/temp/$deposit_id.$chunk_num/$bag_id.tar.$chunk_num] from [/datavault/temp/$deposit_id.$chunk_num/$bag_id.tar.$chunk_num] using optfile [/home/lacdv/.TSM/opt/dsm$location_of_bad_chunk.opt]. dsmc returned [$archive_rc]"
  exit $archive_rc
fi
