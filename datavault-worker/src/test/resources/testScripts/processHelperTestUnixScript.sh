#!/usr/bin/env bash

# Author David Hay
# Used to test the startup/shutdown of operating system processes via ProcessHelper
# Please see 'org.datavaultplatform.worker.cleanup.ProcessHelperWithProcessInfoTest'
# this script is run as part of ProcessHelperWithProcessInfoTest - which runs on Jenkins CI/CD via Docker 
# the Docker image we run the tests on is created via Dockerfile.jenkins
# this script assumes that both 'perl' and 'bash' are available in the operating system 'environment' running 'ProcessHelperWithProcessInfoTest'
if [[ $# -ne 4 ]]; then
    echo "Error: You must provide exactly 4 arguments."
    echo "Usage: $0 <label> <delayMillis> <exitcode> <ignoresigerm>"
    exit 1
fi
LABEL="$1" # a label used in the script output
DELAYMS="$2" # the number of ms to delaty before exiting
EXITCODE="$3" # the code to exit with
IGNORESIGERM="$4" # 'yes/no' - whether this script will ignore SIGTERM and have to be stopped with SIGKILL instead

if [[ $IGNORESIGERM == "yes" ]]; then
  trap 'echo "Received SIGTERM: Staying Alive!" >&2' SIGTERM
  echo "Custom SIGTERM handling enabled";
else
  echo "Standard signal handling enabled";
fi

if ! [[ $EXITCODE =~ ^[0-9]+$ ]]; then 
  echo "Error: '$EXITCODE' is not numeric" 
  exit 1 
fi

round_up() {
    local input="$1"
    
    # 1. Extract the integer part (everything before the dot)
    #    If input starts with a dot (e.g. ".5"), default to 0.
    local int_part="${input%%.*}"
    int_part="${int_part:-0}"
    
    # 2. Extract the decimal part (everything after the dot)
    local dec_part="${input#*.}"
    
    # 3. Check if we actually had a decimal point
    if [[ "$input" == "$dec_part" ]]; then
        # No dot found (e.g. input was "5"), just return it
        echo "$input"
        return
    fi
    
    # 4. Check if the decimal part is effectively zero (e.g. "1.0" or "1.00")
    #    We remove all '0' characters; if nothing is left, it was exactly 0.
    if [[ -z "${dec_part//0/}" ]]; then
        echo "$int_part"
    else
        # It has a non-zero decimal, so round up
        echo $((int_part + 1))
    fi
}
echo "$LABEL,$DELAYMS,$EXITCODE"
echo "start: ${LABEL}"
echo "stdout: ${LABEL}" # sends text to stdout
echo "stderr: ${LABEL}" >&2 # sends text to stderr
# for macos at least - the arg to sleep has to be in parts of seconds - so 100ms would be '0.1 seconds'
SECONDS_ARG=$(perl -e "print $DELAYMS / 1000")
echo "SLEEPING $SECONDS_ARG SECONDS..."
if [[ $IGNORESIGERM == "yes" ]]; then
  # we need to sleep but without a long running child process and the best way is to use 'read -t' but we require whole seconds
  WHOLE_SECONDS_ARG=$(round_up $SECONDS_ARG)
  echo "WHOLE_SECONDS_ARG $WHOLE_SECONDS_ARG"
  tmp=$(mktemp -u); mkfifo "$tmp"; read -t $WHOLE_SECONDS_ARG <> "$tmp"; rm "$tmp"
else
  sleep $SECONDS_ARG
fi
echo "end: ${LABEL}"
echo "exiting with: $((EXITCODE))"
exit $((EXITCODE));