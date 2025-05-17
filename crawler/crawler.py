from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from bs4 import BeautifulSoup
import time
import redis
from pathlib import Path
import json
import os
from dotenv import load_dotenv
from datetime import datetime
import pymysql

# .env 불러오기
base_dir = Path(__file__).resolve().parent.parent
load_dotenv(dotenv_path=base_dir / ".env")

REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
REDIS_PORT = int(os.getenv("REDIS_PORT", 6379))
REDIS_PASSWORD = os.getenv("REDIS_PASSWORD", None)

r = redis.Redis(
    host=REDIS_HOST,
    port=REDIS_PORT,
    password=REDIS_PASSWORD if REDIS_PASSWORD else None,
    decode_responses=True
)

# 지표 매핑
INDICATOR_MAP = {
    "Core Inflation Rate YoY": "Core CPI",
    "Core PPI YoY": "Core PPI",
    "Core PCE YoY": "Core PCE",
    "GDP Growth Rate QoQ 2nd Est": "GDP",
    "Core Retail Sales": "Retail Sales",
    "Industrial Production": "Industrial Production",
    "Unemployment Rate": "Unemployment Rate",
    "Nonfarm Payrolls": "Nonfarm Payrolls",
    "ISM Manufacturing PMI": "ISM Manufacturing PMI",
    "ISM Non-Manufacuring PMI": "ISM Services PMI",
    "Michigan Consumer Sentiment Final": "UM Consumer Sentiment",
    "CB Consumer Confidence": "CB Consumer Confidence",
    "Initial Jobless Claims": "Initial Jobless Claims",
    "Durable Goods Orders": "Durable Goods Orders",
    "New Home Sales": "New Home Sales",
    "Existing Home Sales": "Existing Home Sales"
}

INDICATOR_KR_MAP = {
    "Core CPI": "근원 소비자물가지수",
    "Core PPI": "근원 생산자물가지수",
    "Core PCE": "근원 개인소비지출물가지수",
    "GDP": "국내총생산 분기 성장률",
    "Retail Sales": "근원 소매판매",
    "Industrial Production": "산업생산",
    "Unemployment Rate": "실업률",
    "Nonfarm Payrolls": "비농업부문 신규 고용자 수",
    "ISM Manufacturing PMI": "ISM 제조업 PMI",
    "ISM Services PMI": "ISM 서비스업 PMI",
    "UM Consumer Sentiment": "미시간대 소비자심리지수",
    "CB Consumer Confidence": "컨퍼런스보드 소비자신뢰지수",
    "Initial Jobless Claims": "신규 실업수당 청구건수",
    "Durable Goods Orders": "내구재 주문",
    "New Home Sales": "신규 주택 판매건수",
    "Existing Home Sales": "기존 주택 판매건수"
}


def crawl_tradingeconomics():
    options = Options()
    options.add_argument("--disable-gpu")
    options.add_argument("user-agent=Mozilla/5.0")

    driver = webdriver.Chrome(options=options)
    driver.get("https://tradingeconomics.com/united-states/calendar")
    time.sleep(5)

    soup = BeautifulSoup(driver.page_source, "html.parser")
    rows = soup.select("table#calendar > tbody > tr")
    print(f"총 row 수: {len(rows)}")

    result = []

    for row in rows:
        cells = row.find_all("td")
        if len(cells) < 9:
            continue

        date_td = cells[0]
        date_classes = date_td.get("class", [])
        current_date = next((cls for cls in date_classes if cls.count("-") == 2), None)
        if not current_date:
            continue

        time_text = cells[0].text.strip()
        full_date = f"{current_date} {time_text}"

        event_tag = cells[4].select_one("a")
        if not event_tag:
            continue
        raw_event = event_tag.text.strip()

        if raw_event not in INDICATOR_MAP:
            continue

        event = INDICATOR_MAP[raw_event]
        event_kor = INDICATOR_KR_MAP.get(event, event)

        actual_tag = cells[5].select_one("span")
        actual = actual_tag.text.strip() if actual_tag else ""

        previous_tag = cells[6].select_one("span")
        previous = previous_tag.text.strip() if previous_tag else ""

        forecast_tag = cells[8].select_one("a, span")
        forecast = forecast_tag.text.strip() if forecast_tag else ""

        result.append({
            "event": event,
            "event_kor": event_kor,
            "date": full_date,
            "actual": actual,
            "forecast": forecast,
            "previous": previous
        })

    driver.quit()
    return result


# def save_to_redis(data_list):
#     for item in data_list:
#         try:
#             dt = datetime.strptime(item['date'], "%Y-%m-%d %I:%M %p")
#         except Exception as e:
#             print(f"❌ 날짜 파싱 실패: {item['date']} ({e})")
#             continue
#
#         date_part = dt.strftime("%Y-%m-%d")
#         time_part = dt.strftime("%H:%M")
#
#         event_code = item["event"].upper().replace(" ", "_")  # Redis-safe key
#         redis_key = f"event:{date_part}:{time_part}:{event_code}"
#
#         r.set(redis_key, json.dumps(item))
#         print(f"✅ 저장됨: {redis_key}")
#

DB_PORT = int(os.getenv("DB_PORT", 3306))
DB_NAME = os.getenv("DB_NAME")

# env = os.getenv("ENV", "dev")
env_file = base_dir / f".env-dev"
if os.path.exists(env_file):
    load_dotenv(env_file, override=True)
DB_USER = os.getenv("DB_USER")
DB_HOST = os.getenv("DB_HOST","127.0.0.1")
DB_PASSWORD = os.getenv("DB_PASSWORD")
print(DB_USER, DB_HOST, DB_PASSWORD)

def save_to_db(data_list):
    conn = pymysql.connect(
        host=DB_HOST,
        port=DB_PORT,
        user=DB_USER,
        password=DB_PASSWORD,
        db=DB_NAME,
        charset='utf8mb4'
    )
    cursor = conn.cursor()

    for item in data_list:
        try:
            dt = datetime.strptime(item['date'], "%Y-%m-%d %I:%M %p")
        except Exception as e:
            print(f"❌ 날짜 파싱 실패: {item['date']} ({e})")
            continue

        name = item['event']
        event = item['event_kor']
        date = dt.date()
        previous = item['previous']
        forecast = item['forecast']
        actual = item['actual']

        sql = """
                INSERT INTO economicevent (name, event,date, previous, forecast, actual)
                VALUES (%s, %s, %s, %s, %s)
            """
        values = (name, event,date, previous, forecast, actual)

        cursor.execute(sql, values)
        print(f"✅ 저장 완료: {name} ({date})")

    conn.commit()
    cursor.close()
    conn.close()

if __name__ == "__main__":

    data = crawl_tradingeconomics()
    for item in data:
        print(f"{item['event']} - 📅 {item['date']} | 실제: {item['actual']} | 예측: {item['forecast']} | 전월: {item['previous']}")
    # save_to_redis(data)
    save_to_db(data)