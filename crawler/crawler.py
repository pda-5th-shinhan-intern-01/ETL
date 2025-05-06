from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from bs4 import BeautifulSoup
import time

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

    TARGETS = [
        "CPI", "PPI", "Core", "GDP", "ISM",
        "Payroll", "Unemployment", "Retail",
        "Industrial", "PMI", "Trade", "Mortgage"
    ]

    result = []
    current_date = None

    for row in rows:
        cells = row.find_all("td")
        if len(cells) < 9:
            continue

        # 날짜 추출 (td의 class명에 포함됨)
        date_td = cells[0]
        date_classes = date_td.get("class", [])
        for cls in date_classes:
            if cls.count("-") == 2:
                current_date = cls
                break

        # 지표명
        event_tag = cells[4].select_one("a")
        if not event_tag:
            continue
        event = event_tag.text.strip()

        # 실제치
        actual_tag = cells[5].select_one("span")
        actual = actual_tag.text.strip() if actual_tag else ""

        # 전월치
        previous_tag = cells[6].select_one("span")
        previous = previous_tag.text.strip() if previous_tag else ""

        # 예측치
        forecast_tag = cells[8].select_one("a, span")
        forecast = forecast_tag.text.strip() if forecast_tag else ""

        if not forecast:
            continue

        for keyword in TARGETS:
            if keyword.lower() in event.lower():
                result.append({
                    "event": event,
                    "date": current_date,
                    "actual": actual,
                    "forecast": forecast,
                    "previous": previous
                })
                break

    driver.quit()
    return result

# 실행
if __name__ == "__main__":
    for item in crawl_tradingeconomics():
        print(f"{item['event']} - 📅 {item['date']} | 실제: {item['actual']} | 예측: {item['forecast']} | 전월: {item['previous']}")
