from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from bs4 import BeautifulSoup
import time
import redis
import json
import os
from dotenv import load_dotenv

# .env 불러오기
load_dotenv()
REDIS_HOST = os.getenv("REDIS_HOST", "localhost")
REDIS_PORT = int(os.getenv("REDIS_PORT", 6379))
REDIS_PASSWORD = os.getenv("REDIS_PASSWORD", None)

# Redis 연결
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
    "Core PCE Prices QoQ 2nd Est": "Core PCE",
    "GDP Growth Rate QoQ 2nd Est": "GDP",
    "Retail Sales": "Retail Sales",
    "Industrial Production": "Industrial Production",
    "Unemployment Rate": "Unemployment Rate",
    "Nonfarm Payrolls": "Nonfarm Payrolls",
    "ISM Manufacturing PMI": "ISM Manufacturing PMI"
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

        # 날짜 추출 from class="2025-05-15"
        date_td = cells[0]
        date_classes = date_td.get("class", [])
        current_date = None
        for cls in date_classes:
            if cls.count("-") == 2:
                current_date = cls
                break

        if not current_date:
            continue

        # 시간 + 날짜 조합
        time_text = cells[0].text.strip()
        full_date = f"{current_date} {time_text}"

        event_tag = cells[4].select_one("a")
        if not event_tag:
            continue
        raw_event = event_tag.text.strip()

        if raw_event not in INDICATOR_MAP:
            continue

        event = INDICATOR_MAP[raw_event]

        actual_tag = cells[5].select_one("span")
        actual = actual_tag.text.strip() if actual_tag else ""

        previous_tag = cells[6].select_one("span")
        previous = previous_tag.text.strip() if previous_tag else ""

        forecast_tag = cells[8].select_one("a, span")
        forecast = forecast_tag.text.strip() if forecast_tag else ""

        result.append({
            "event": event,
            "date": full_date,
            "actual": actual,
            "forecast": forecast,
            "previous": previous
        })

    driver.quit()
    return result

def save_to_redis(data_list):
    for item in data_list:
        key = f"econ:{item['date']}:{item['event']}"
        value = json.dumps(item)
        r.set(key, value)
        print(f"✅ 저장됨: {key}")

# 실행
if __name__ == "__main__":
    data = crawl_tradingeconomics()
    for item in data:
        print(f"{item['event']} - 📅 {item['date']} | 실제: {item['actual']} | 예측: {item['forecast']} | 전월: {item['previous']}")
    save_to_redis(data)
