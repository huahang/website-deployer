#!/usr/bin/env bash
mvn -U clean package source:jar "$@"
