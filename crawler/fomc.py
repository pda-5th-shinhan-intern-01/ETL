from datetime import datetime
import redis
import json
import os
from dotenv import load_dotenv

load_dotenv()
r = redis.Redis(
    host=os.getenv("REDIS_HOST", "localhost"),
    port=int(os.getenv("REDIS_PORT", 6379)),
    password=os.getenv("REDIS_PASSWORD", None),
    decode_responses=True
)

fomc_schedule_2025 = [
    ("2025-01-28", "2025-01-29"),
    ("2025-03-18", "2025-03-19"),
    ("2025-05-06", "2025-05-07"),
    ("2025-06-17", "2025-06-18"),
    ("2025-07-29", "2025-07-30"),
    ("2025-09-16", "2025-09-17"),
    ("2025-10-28", "2025-10-29"),
    ("2025-12-09", "2025-12-10")
]

for start, end in fomc_schedule_2025:
    dt = datetime.strptime(start, "%Y-%m-%d")
    key_time = dt.strftime("%H:%M")
    redis_key = f"event:{start}:{key_time}:FOMC_MEETING"

    value = {
        "event": "FOMC Meeting",
        "event_kor": "FOMC 회의 일정",
        "date": dt.strftime("%Y-%m-%d 02:00 PM"),
        "start": start,
        "end": end
    }

    r.set(redis_key, json.dumps(value))
    print(f"✅ 저장됨: {redis_key}")
