#!/usr/bin/env bash

chunk_to_be_repaired=$1
tmp_chunk_location=$2
chunk_description=$3
location_of_good_chunk=$4
location_of_bad_chunk=$5

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

[lacdv@rds-dv-worker-test1 ~]$ cat fixAuditChunkList.sh
#!/usr/bin/env bash

input_file=$1

if [ -z "$input_file" ]; then
  echo "input_file is required"
  echo "Usage: $0 <input_file>"
  exit 1
fi

if [ ! -e "$input_file" ]; then
  echo "input_file does not exist [$input_file]"
  exit 1
fi

if [ ! -f "$input_file" ]; then
  echo "input_file is not a regular file [$input_file]"
  exit 1
fi

if [ ! -r "$input_file" ]; then
  echo "input_file is not readable [$input_file]"
  exit 1
fi

trim() {
  local value=$1
  value="${value#"${value%%[![:space:]]*}"}"
  value="${value%"${value##*[![:space:]]}"}"
  printf '%s' "$value"
}

line_number=0
while IFS= read -r line || [ -n "$line" ]; do
  line_number=$((line_number + 1))

  if [ -z "$line" ]; then
    echo "Invalid line [$line_number]: line is empty"
    exit 1
  fi

  IFS=',' read -r chunk_to_be_repaired tmp_chunk_location chunk_description location_of_good_chunk location_of_bad_chunk extra_field <<EOF
$line
EOF

  if [ -n "$extra_field" ]; then
    echo "Invalid line [$line_number]: expected exactly 5 comma-separated params"
    exit 1
  fi

  chunk_to_be_repaired=$(trim "$chunk_to_be_repaired")
  tmp_chunk_location=$(trim "$tmp_chunk_location")
  chunk_description=$(trim "$chunk_description")
  location_of_good_chunk=$(trim "$location_of_good_chunk")
  location_of_bad_chunk=$(trim "$location_of_bad_chunk")

  if [ -z "$chunk_to_be_repaired" ] || [ -z "$tmp_chunk_location" ] || [ -z "$chunk_description" ] || [ -z "$location_of_good_chunk" ] || [ -z "$location_of_bad_chunk" ]; then
    echo "Invalid line [$line_number]: all 5 params must be non-empty"
    exit 1
  fi

  /usr/bin/dsmc retrieve "$chunk_to_be_repaired" "$tmp_chunk_location" -description="$chunk_description" -optfile="$location_of_good_chunk" -replace=true
  retrieve_rc=$?
  if [ $retrieve_rc -ne 0 ]; then
    echo "Line [$line_number] failed: retrieve for chunk [$chunk_to_be_repaired] returned [$retrieve_rc]"
    exit $retrieve_rc
  fi

  /usr/bin/dsmc delete archive "$chunk_to_be_repaired" -description="$chunk_description" -optfile="$location_of_bad_chunk" -noprompt
  delete_rc=$?
  if [ $delete_rc -ne 0 ]; then
    echo "Line [$line_number] warning: delete archive for chunk [$chunk_to_be_repaired] returned [$delete_rc]. Continuing."
  fi

  /usr/bin/dsmc archive "$chunk_to_be_repaired" "$tmp_chunk_location" -description="$chunk_description" -optfile="$location_of_bad_chunk"
  archive_rc=$?
  if [ $archive_rc -ne 0 ]; then
    echo "Line [$line_number] failed: archive for chunk [$chunk_to_be_repaired] returned [$archive_rc]"
    exit $archive_rc
  fi
done < "$input_file"