#!/usr/bin/env bash

chunk_to_be_repaired=$1
tmp_chunk_location=$2
chunk_description=$3
location_of_good_chunk=$4
location_of_bad_chunk=$5


# ./fixAuditChunk.sh /datavault/temp/018218c1-0830-4eca-a868-9408ae8d5175.1/407d6229-37df-4587-bd6b-7f0109f172a3.tar.1 /datavault/temp/dv_20260309005150/407d6229-37df-4587-bd6b-7f0109f172a3.tar.1 018218c1-0830-4eca-a868-9408ae8d5175.1 /home/lacdv/.TSM/opt/dsm1.opt /home/lacdv/.TSM/opt/dsm2.opt

if [ -z "$chunk_to_be_repaired" ]; then
  echo "chunk_to_be_repaired is required"
  echo "Usage: $0 <chunk_to_be_repaired> <tmp_chunk_location> <chunk_description> <location_of_good_chunk> <location_of_bad_chunk>"
  exit 1
fi

if [ -z "$tmp_chunk_location" ]; then
  echo "tmp_chunk_location is required"
  echo "Usage: $0 <chunk_to_be_repaired> <tmp_chunk_location> <chunk_description> <location_of_good_chunk> <location_of_bad_chunk>"
  exit 1
fi

if [ -z "$chunk_description" ]; then
  echo "chunk_description is required"
  echo "Usage: $0 <chunk_to_be_repaired> <tmp_chunk_location> <chunk_description> <location_of_good_chunk> <location_of_bad_chunk>"
  exit 1
fi

if [ -z "$location_of_good_chunk" ]; then
  echo "location_of_good_chunk is required"
  echo "Usage: $0 <chunk_to_be_repaired> <tmp_chunk_location> <chunk_description> <location_of_good_chunk> <location_of_bad_chunk>"
  exit 1
fi

if [ -z "$location_of_bad_chunk" ]; then
  echo "location_of_bad_chunk is required"
  echo "Usage: $0 <chunk_to_be_repaired> <tmp_chunk_location> <chunk_description> <location_of_good_chunk> <location_of_bad_chunk>"
  exit 1
fi

/usr/bin/dsmc retrieve "$chunk_to_be_repaired" "$tmp_chunk_location" -description="$chunk_description" -optfile="$location_of_good_chunk" -replace=true
retrieve_rc=$?
if [ $retrieve_rc -ne 0 ]; then
  echo "Failed to retrieve chunk [$chunk_to_be_repaired] to [$tmp_chunk_location] using optfile [$location_of_good_chunk]. dsmc returned [$retrieve_rc]"
  exit $retrieve_rc
fi

/usr/bin/dsmc delete archive "$chunk_to_be_repaired" -description="$chunk_description" -optfile="$location_of_bad_chunk" -noprompt
delete_rc=$?
if [ $delete_rc -ne 0 ]; then
  echo "Failed to delete archived chunk [$chunk_to_be_repaired] using optfile [$location_of_bad_chunk]. dsmc returned [$delete_rc]. Continuing."
fi

/usr/bin/dsmc archive "$chunk_to_be_repaired" "$tmp_chunk_location" -description="$chunk_description" -optfile="$location_of_bad_chunk"
archive_rc=$?
if [ $archive_rc -ne 0 ]; then
  echo "Failed to archive chunk [$chunk_to_be_repaired] from [$tmp_chunk_location] using optfile [$location_of_bad_chunk]. dsmc returned [$archive_rc]"
  exit $archive_rc
fi