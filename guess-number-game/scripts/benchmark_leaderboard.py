#!/usr/bin/env python3
"""Concurrent read benchmark for GET /api/game/leaderboard."""

import argparse
import http.client
import statistics
import threading
import time
from urllib.parse import urlsplit


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--url", default="http://localhost:8080/api/game/leaderboard")
    parser.add_argument("--requests", type=int, default=2000)
    parser.add_argument("--concurrency", type=int, default=50)
    args = parser.parse_args()

    parsed = urlsplit(args.url)
    if parsed.scheme not in ("http", "https"):
        parser.error("URL must use http or https")
    path = parsed.path or "/"
    if parsed.query:
        path += "?" + parsed.query

    def request_once(connection):
        connection.request("GET", path, headers={"Accept": "application/json"})
        response = connection.getresponse()
        response.read()
        if response.status != 200:
            raise RuntimeError(f"HTTP {response.status}")

    warmup = http.client.HTTPSConnection if parsed.scheme == "https" else http.client.HTTPConnection
    conn = warmup(parsed.hostname, parsed.port, timeout=20)
    request_once(conn)
    conn.close()

    lock = threading.Lock()
    next_request = 0
    latencies = []
    errors = []
    start = time.perf_counter()

    def worker():
        nonlocal next_request
        connection_type = http.client.HTTPSConnection if parsed.scheme == "https" else http.client.HTTPConnection
        connection = connection_type(parsed.hostname, parsed.port, timeout=30)
        try:
            while True:
                with lock:
                    if next_request >= args.requests:
                        break
                    next_request += 1
                request_start = time.perf_counter()
                try:
                    request_once(connection)
                    latencies.append((time.perf_counter() - request_start) * 1000)
                except Exception as exc:  # Report failed requests instead of hiding them.
                    errors.append(str(exc))
                    connection.close()
                    connection = connection_type(parsed.hostname, parsed.port, timeout=30)
        finally:
            connection.close()

    threads = [threading.Thread(target=worker) for _ in range(args.concurrency)]
    for thread in threads:
        thread.start()
    for thread in threads:
        thread.join()
    elapsed = time.perf_counter() - start

    ordered = sorted(latencies)

    def percentile(value):
        if not ordered:
            return float("nan")
        index = min(len(ordered) - 1, int((value / 100) * len(ordered)))
        return ordered[index]

    print(f"requests={args.requests} concurrency={args.concurrency} successful={len(latencies)} errors={len(errors)}")
    print(f"elapsed_seconds={elapsed:.3f} throughput_rps={len(latencies) / elapsed:.1f}")
    if ordered:
        print(f"latency_ms_avg={statistics.mean(ordered):.2f} p50={percentile(50):.2f} p95={percentile(95):.2f} p99={percentile(99):.2f}")
    if errors:
        print(f"first_error={errors[0]}")
        raise SystemExit(1)


if __name__ == "__main__":
    main()
