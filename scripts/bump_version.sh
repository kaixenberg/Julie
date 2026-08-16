#!/bin/bash

# A helper script to safely bump the versionName and versionCode
# Usage: ./scripts/bump_version.sh <new_version_name>
# Example: ./scripts/bump_version.sh 1.1.0

if [ -z "$1" ]; then
  echo "Error: Missing version name."
  echo "Usage: $0 <new_version_name>"
  exit 1
fi

NEW_VERSION=$1
GRADLE_FILE="app/build.gradle.kts"

if [ ! -f "$GRADLE_FILE" ]; then
    echo "Error: Could not find $GRADLE_FILE"
    exit 1
fi

# Extract current versionCode
CURRENT_VERSION_CODE=$(grep -oP 'versionCode\s*=\s*\K\d+' $GRADLE_FILE)
if [ -z "$CURRENT_VERSION_CODE" ]; then
    echo "Error: Could not find versionCode in $GRADLE_FILE"
    exit 1
fi

NEW_VERSION_CODE=$((CURRENT_VERSION_CODE + 1))

# Replace versionCode
sed -i "s/versionCode = $CURRENT_VERSION_CODE/versionCode = $NEW_VERSION_CODE/" $GRADLE_FILE

# Replace versionName
sed -i -E "s/versionName\s*=\s*\"[^\"]+\"/versionName = \"$NEW_VERSION\"/" $GRADLE_FILE

echo "Successfully bumped version to $NEW_VERSION (versionCode $NEW_VERSION_CODE)"
