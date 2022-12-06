#!/bin/bash
#
# Copyright (c) 2022, NVIDIA CORPORATION. All rights reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#
# Argument(s):
#   SPARK_VER: spark version. e.g. 3.3.0
#   CUSTOM_URL: custom download url
#

set -e

if [[ $# -eq 0 ]]; then
    echo "No arguments provided, use default: bin-hadoop3.2"
    exit 1
fi

# Split spark version into base version (e.g. 3.3.0) and suffix (e.g. SNAPSHOT)
PRE_IFS=$IFS
IFS="-" read -r -a SPARK_VER <<< "$1"
IFS=$PRE_IFS
if [[ ${#SPARK_VER[@]} > 1 ]]; then # version with suffix
    BIN_HADOOP_VER="bin-hadoop3.2"
elif [[ -n "$2" ]] && [[ "$2" != "apache default url" ]]; then # without suffix, provided custom-url and url != default url
    BIN_HADOOP_VER="bin-hadoop3.2"
elif [[ `echo -e "${SPARK_VER[0]}\n3.3.0" | sort -V | head -n 1` == "3.3.0" ]]; then # no custom url and spark version >= 3.3.0
    BIN_HADOOP_VER="bin-hadoop3"
else
    BIN_HADOOP_VER="bin-hadoop3.2"
fi
