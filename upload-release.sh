#!/bin/bash

token=${1}
tag=${2}
name=${2}

description=$(echo "${3}" | sed -z 's/\n/\\n/g') # Escape
release=$(curl -XPOST -H "Authorization:token $token" --data "{\"tag_name\": \"$tag\", \"target_commitish\": \"master\", \"name\": \"$name\", \"body\": \"$description\", \"draft\": false, \"prerelease\": false}" https://api.github.com/repos/shayaantx/botdar/releases)
id=$(echo "$release" | sed -n -e 's/"id":\ \([0-9]\+\),/\1/p' | head -n 1 | sed 's/[[:blank:]]//g')

curl -XPOST -H "Authorization:token $token" -H "Content-Type:application/octet-stream" --data-binary "target/botdar-release.jar" https://uploads.github.com/repos/shayaantx/botdar/releases/$id/assets?name=botdar-release.jar
