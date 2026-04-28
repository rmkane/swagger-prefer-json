#!/usr/bin/env bash
# Hit API (and related) endpoints with several Accept variants and assert status + Content-Type.
#
# Usage:
#   ./scripts/smoke-accept.sh
#   BASE_URL=http://localhost:8080 ./scripts/smoke-accept.sh
#
# "none" means no Accept header is sent (curl: -H 'Accept:').
set -uo pipefail

BASE_URL=${BASE_URL:-http://localhost:8080}
BASE_URL=${BASE_URL%/}

HDR_FILE=$(mktemp)
trap 'rm -f "$HDR_FILE"' EXIT

pass=0
fail=0

# $1 description  $2 path  $3 accept: none|json|xml|browser  $4 expected HTTP status  $5 expected Content-Type prefix (lowercase), or "-" to skip
assert_request() {
	local desc=$1
	local path=$2
	local accept_kind=$3
	local want_status=$4
	local want_ct=$5

	local -a curl_opts=(-sS -D "$HDR_FILE" -o /dev/null -w '%{http_code}')
	case "$accept_kind" in
		none) curl_opts+=(-H 'Accept:') ;;
		json) curl_opts+=(-H 'Accept: application/json') ;;
		xml) curl_opts+=(-H 'Accept: application/xml') ;;
		browser) curl_opts+=(-H 'Accept: text/html,application/xhtml+xml,application/xml;q=0.9,*/*;q=0.8') ;;
		*)
			echo "internal error: bad accept_kind=$accept_kind" >&2
			exit 2
			;;
	esac

	local url="${BASE_URL}${path}"
	local got_status
	got_status=$(curl "${curl_opts[@]}" "$url")

	local got_ct=""
	if grep -qi '^content-type:' "$HDR_FILE"; then
		got_ct=$(grep -i '^content-type:' "$HDR_FILE" | head -1 | cut -d: -f2- | tr -d '\r' | awk '{$1=$1};1' | tr '[:upper:]' '[:lower:]')
	fi

	local ok=1
	if [[ "$got_status" != "$want_status" ]]; then
		ok=0
	fi
	if [[ "$want_ct" != "-" ]]; then
		if [[ "$got_ct" != "${want_ct}"* ]]; then
			ok=0
		fi
	fi

	if ((ok)); then
		printf 'OK  %s [%s] HTTP %s Content-Type: %s\n' "$desc" "$accept_kind" "$got_status" "${got_ct:-<none>}"
		((++pass)) || true
	else
		printf 'FAIL %s [%s] want HTTP %s and Content-Type prefix %q; got HTTP %s and %q\n' \
			"$desc" "$accept_kind" "$want_status" "$want_ct" "$got_status" "$got_ct" >&2
		((++fail)) || true
	fi
}

echo "BASE_URL=$BASE_URL"
echo

# --- People API (dual JSON/XML) ---
for path in /api/v1/people /api/v1/people/corrected; do
	assert_request "GET $path" "$path" none 200 application/json
	assert_request "GET $path" "$path" json 200 application/json
	assert_request "GET $path" "$path" xml 200 application/xml
	assert_request "GET $path" "$path" browser 200 application/json
done

# --- XML-only people ---
path=/api/v1/people/xml
assert_request "GET $path" "$path" none 406 application/json
assert_request "GET $path" "$path" json 406 application/json
assert_request "GET $path" "$path" xml 200 application/xml
assert_request "GET $path" "$path" browser 200 application/xml

# --- Plain-text only (filter does not force JSON; */* in browser Accept allows text/plain) ---
path=/api/v1/people/summary
assert_request "GET $path" "$path" none 406 application/json
assert_request "GET $path" "$path" json 406 application/json
assert_request "GET $path" "$path" xml 406 application/xml
assert_request "GET $path" "$path" browser 200 text/plain

# --- OpenAPI (JSON only) ---
path=/v3/api-docs
assert_request "GET $path" "$path" none 200 application/json
assert_request "GET $path" "$path" json 200 application/json
# 406 body follows negotiated error format (XML when client asked for XML).
assert_request "GET $path" "$path" xml 406 application/xml
# Browser Accept includes */*; Springdoc still serves JSON.
assert_request "GET $path" "$path" browser 200 application/json

# --- Home (HTML only): default negotiation favors JSON app-wide, so non-HTML Accept yields 406 ---
path=/
assert_request "GET $path" "$path" none 406 application/json
assert_request "GET $path" "$path" json 406 application/json
assert_request "GET $path" "$path" xml 406 application/xml
assert_request "GET $path" "$path" browser 200 text/html

# --- Swagger UI entry (redirect; Content-Type often empty) ---
path=/swagger-ui.html
for kind in none json xml browser; do
	assert_request "GET $path" "$path" "$kind" 302 -
done

echo
if ((fail > 0)); then
	echo "Summary: $pass passed, $fail failed" >&2
	exit 1
fi
echo "Summary: $pass passed, $fail failed"
exit 0
