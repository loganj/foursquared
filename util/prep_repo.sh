#!/bin/bash
# Update a newly cloned foursquare repo with required files from an existing
# repo

existing=$1
new=$2
echo 'From: ' $existing ' to: ' $new

path='res/values/credentials.xml'
existing_path=$existing/$path
new_path=$new/$path
echo "cp $existing_path $new_path"
cp $existing_path $new_path

path='tests/com/joelapenna/foursquare/TestCredentials.java'
existing_path=$existing/$path
new_path=$new/$path
echo "cp $existing_path $new_path"
cp $existing_path $new_path

path='.project'
existing_path=$existing/$path
new_path=$new/$path
echo "cp $existing_path $new_path"
cp $existing_path $new_path
sed -i "s#<name>foursquared</name>#<name>$new</name>#g" $new_path
