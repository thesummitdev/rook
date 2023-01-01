#!/bin/bash
# This script creates a volume of traffic (~ 2000 requests)
# as a split between a list/filter endpoint and create new bookmarks
# across two seperate users.
# The intent is to both check the throughput of the server / database
# as well as ensure there isn't any database deadlocking issues.

# These API keys are from the --seedtestdata=true flag when starting the server.
USER_1_APIKEY=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJkZXYudGhlc3VtbWl0LnJvb2siLCJpYXQiOjE2NzI1Njc4MTcsInVzZXJuYW1lIjoicm9vayJ9.oX86c8EuxNHKtVe12bOCK2sLg_2lasBtaM-JozLIYQyC26pXxGt7131ULFmIHb0fv4aHcC-usXHqcFli_QtLZw
USER_2_APIKEY=eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJkZXYudGhlc3VtbWl0LnJvb2siLCJpYXQiOjE2NzI1Njc4MjksInVzZXJuYW1lIjoidHlsZXIifQ.Kt8Iw9NsBsw7qy_CfOQfxiLo1alnT8IHheZ-Cb2DEFAV0wMzU95JDLn0UazqzUjFytCkuNH6rmFB6YhX5k1V8A

echo "Starting stress test..."

for i in $(seq 500)
do

    response=$( curl --location --request PUT 'localhost:8000/links' \
            --silent \
            --write-out "%{http_code}" \
            --header "Authorization: Bearer $USER_1_APIKEY" \
            --header 'Content-Type: application/json' \
            --output /dev/null \
            --data-raw "{
            'title': 'bookmark $i',
            'url': 'https://www.testloop.com',
            'tags': 'unique $RANDOM'
        }"
    )

    if [[ $response != 200 ]]; then
        echo "Create bookmark failure detected. Response code: $response"
    fi

done &

for i in $(seq 500)
do
    response=$( curl --location --request PUT 'localhost:8000/links' \
            --silent \
            --write-out "%{http_code}" \
            --header "Authorization: Bearer $USER_2_APIKEY" \
            --header 'Content-Type: application/json' \
            --output /dev/null \
            --data-raw "{
            'title': 'bookmark $i',
            'url': 'https://www.testloop.com',
            'tags': 'unique $RANDOM'
        }"
    )

    if [[ $response != 200 ]]; then
        echo "Create bookmark failure detected. Response code: $response"
    fi

done &

for i in $(seq 500)
do
    response=$(curl --location --request POST 'localhost:8000/links' \
            --silent \
            --write-out "%{http_code}" \
            --header "Authorization: Bearer $USER_1_APIKEY" \
            --header 'Content-Type: application/json' \
            --output /dev/null \
            --data-raw '{ "limit": 75 }'
    )

    if [[ $response != 200 ]]; then
        echo "List bookmarks failure detected. Response code: $response"
    fi
done &

for i in $(seq 500)
do
    response=$(curl --location --request POST 'localhost:8000/links' \
            --silent \
            --write-out "%{http_code}" \
            --header "Authorization: Bearer $USER_2_APIKEY" \
            --header 'Content-Type: application/json' \
            --output /dev/null \
            --data-raw '{ "limit": 75 }'
    )

    if [[ $response != 200 ]]; then
        echo "List bookmarks failure detected. Response code: $response"
    fi

done &

wait

echo "Stress test complete"
