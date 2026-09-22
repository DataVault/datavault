#!/usr/bin/env bash

# Runs fixAuditChunk.sh once for every row in a CSV file. Each row must be:
# deposit_id,chunk_num,bag_id,location_of_bad_chunk

input_file=$1
script_dir=$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)
fix_audit_chunk="$script_dir/fixAuditChunk.sh"

usage() {
  echo "Usage: $0 <input_file.csv>"
  echo "Each CSV row: <deposit_id>,<chunk_num>,<bag_id>,<location_of_bad_chunk>"
}

if [ -z "$input_file" ]; then
  echo "input_file is required"
  usage
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

if [ ! -f "$fix_audit_chunk" ]; then
  echo "fixAuditChunk.sh does not exist [$fix_audit_chunk]"
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

  # Allow blank lines and comments in the input file.
  trimmed_line=$(trim "$line")
  if [ -z "$trimmed_line" ] || [[ "$trimmed_line" == \#* ]]; then
    continue
  fi

  IFS=',' read -r deposit_id chunk_num bag_id location_of_bad_chunk extra_field <<EOF
$line
EOF

  if [ -n "$extra_field" ]; then
    echo "Invalid line [$line_number]: expected exactly 4 comma-separated values"
    exit 1
  fi

  deposit_id=$(trim "$deposit_id")
  chunk_num=$(trim "$chunk_num")
  bag_id=$(trim "$bag_id")
  location_of_bad_chunk=$(trim "$location_of_bad_chunk")

  if [ -z "$deposit_id" ] || [ -z "$chunk_num" ] || [ -z "$bag_id" ] || [ -z "$location_of_bad_chunk" ]; then
    echo "Invalid line [$line_number]: all 4 values must be non-empty"
    exit 1
  fi

  echo "Processing line [$line_number]: deposit [$deposit_id], chunk [$chunk_num], bag [$bag_id], bad location [$location_of_bad_chunk]"
  bash "$fix_audit_chunk" "$deposit_id" "$chunk_num" "$bag_id" "$location_of_bad_chunk"
  repair_rc=$?
  if [ "$repair_rc" -ne 0 ]; then
    echo "Line [$line_number] failed: fixAuditChunk.sh returned [$repair_rc]"
    exit "$repair_rc"
  fi
done < "$input_file"
