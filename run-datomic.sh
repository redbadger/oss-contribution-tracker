#!/usr/bin/env sh

datomic_path=(../datomic/datomic-*/bin/transactor)
props_path="$(pwd)/dev-transactor.properties"

echo $datomic_path $props_path
$datomic_path $props_path 
