#!/bin/bash
set -eo pipefail

BASE_URL="${1:-http://localhost:8080}"
echo "========================================================"
echo "Running Sage Backend API Smoke Tests against: $BASE_URL"
echo "========================================================"

FAILED=0

check_response() {
  local name="$1"
  local expected_status="$2"
  local method="$3"
  local path="$4"
  local data="$5"

  echo -n "[TEST] $name ($method $path)... "

  local curl_cmd=(curl -s -w "\n%{http_code}\n%{content_type}" -X "$method" "$BASE_URL$path")
  if [ -n "$data" ]; then
    curl_cmd+=(-H "Content-Type: application/json" -d "$data")
  fi

  local output
  output=$("${curl_cmd[@]}")
  local content_type
  content_type=$(echo "$output" | tail -n 1)
  local status_code
  status_code=$(echo "$output" | tail -n 2 | head -n 1)
  local body
  body=$(echo "$output" | sed '$d' | sed '$d')

  # Check status code
  if [ "$status_code" != "$expected_status" ]; then
    echo "FAIL: Expected HTTP $expected_status but got $status_code"
    echo "Response body: $body"
    FAILED=$((FAILED + 1))
    return
  fi

  # Check Content-Type is JSON
  if [[ "$content_type" != *"application/json"* ]]; then
    echo "FAIL: Content-Type is not JSON! Got: $content_type"
    echo "Response body: $body"
    FAILED=$((FAILED + 1))
    return
  fi

  # Check for forbidden HTML strings
  if [[ "$body" == *"<html"* || "$body" == *"<!doctype"* || "$body" == *"<!DOCTYPE"* || "$body" == *"<head"* ]]; then
    echo "FAIL: Response contains forbidden HTML markup!"
    echo "Response body: $body"
    FAILED=$((FAILED + 1))
    return
  fi

  echo "PASS (HTTP $status_code, JSON)"
}

# 1. Test basic health endpoint
check_response "Health Check" "200" "GET" "/api/health" ""

# 2. Test Gemini upstream health check
check_response "Gemini Upstream Health Check" "200" "GET" "/api/health?checkGemini=true" ""

# 3. Test Chat completion
CHAT_PAYLOAD='{"history":[{"role":"user","text":"What is 2+2? Answer in one word."}],"mode":"normal"}'
check_response "Chat Completion" "200" "POST" "/api/chat" "$CHAT_PAYLOAD"

# 4. Test 404 JSON contract on unknown API route
check_response "Unknown API Route (GET)" "404" "GET" "/api/nonexistent-test-route" ""
check_response "Unknown API Route (POST)" "404" "POST" "/api/nonexistent-endpoint" '{"test":true}'

# 5. Test root path JSON response
check_response "Root API Info" "200" "GET" "/" ""

# 6. Test Study Tools - Notes
NOTES_PAYLOAD='{"operation":"notes","topic":"Evolution of operating systems","subject":"Operating Systems"}'
check_response "Study Tools (Notes)" "200" "POST" "/api/study-tools" "$NOTES_PAYLOAD"

# 7. Test Study Tools - Flashcards
FLASH_PAYLOAD='{"operation":"flashcards","topic":"CPU Scheduling","subject":"Operating Systems"}'
check_response "Study Tools (Flashcards)" "200" "POST" "/api/study-tools" "$FLASH_PAYLOAD"

# 8. Test Study Tools - Invalid Operation Validation
INVALID_OP_PAYLOAD='{"operation":"unknown_operation","topic":"Testing"}'
check_response "Study Tools Invalid Operation (400)" "400" "POST" "/api/study-tools" "$INVALID_OP_PAYLOAD"

echo "========================================================"
if [ "$FAILED" -eq 0 ]; then
  echo "ALL SMOKE TESTS PASSED! Backend strictly adheres to JSON-only API contract."
  echo "========================================================"
  exit 0
else
  echo "SMOKE TESTS FAILED! $FAILED failure(s) detected."
  echo "========================================================"
  exit 1
fi
