import requests
from bs4 import BeautifulSoup
from urllib.parse import urljoin
import re


"""
1. FOMC 회의록 리스트 크롤링
2. url로 FOMC 회의록 상세 크롤링
"""

BASE_URL = "https://www.federalreserve.gov"

# FOMC 회의록 리스트 크롤링
def crawl_fomc_statement_list() -> list:
    url = "https://www.federalreserve.gov/monetarypolicy/fomccalendars.htm"
    response = requests.get(url)
    response.raise_for_status()

    soup = BeautifulSoup(response.text, "html.parser")
    
    # 🔍 monetary 성명서 관련 링크만 추출
    links = soup.select('a[href*="/newsevents/pressreleases/monetary"]')

    statements = []
    for link in links:
        href = link.get('href')

        # a1.htm은 제외 → 성명서만 추출
        if not href.endswith("a.htm"):
            continue

        # 날짜 추출
        date = extract_date_from_url(href)
        if not date:
            continue
        
        # 영상 링크 추출
        video_url = get_press_conference_video_url(date)

        full_url = urljoin(BASE_URL, href)
        statements.append({
            "event": "fomc",
            "date": date,
            "url": full_url,
            "video_url": video_url
        })

    return statements


# fomc 개별 회의록 상세 내용 크롤링
def crawl_fomc_statement_text(url: str) -> str:
    try:
        response = requests.get(url)
        response.raise_for_status()

        soup = BeautifulSoup(response.text, 'html.parser')

        # FOMC 본문이 들어 있는 div 선택
        content_div = soup.find('div', class_='col-xs-12 col-sm-8 col-md-8')

        if not content_div:
            return "본문을 찾을 수 없습니다."

        # p 태그 기준으로 텍스트 모음
        paragraphs = content_div.find_all('p')
        full_text = "\n\n".join(p.get_text(strip=True) for p in paragraphs)

        return full_text

    except requests.exceptions.RequestException as e:
        return f"요청 실패: {e}"


# 날짜 추출
def extract_date_from_url(href: str) -> str:
    # URL에서 날짜 추출 (예: monetary20250129a.htm → 2025-01-29)
    import re
    match = re.search(r"monetary(\d{4})(\d{2})(\d{2})", href)
    if match:
        year, month, day = match.groups()
        return f"{year}-{month}-{day}"
    return None


# FOMC 회의록 video url 추출
def get_press_conference_video_url(meeting_date: str) -> str:
    calendar_url = f"{BASE_URL}/monetarypolicy/fomccalendars.htm"
    response = requests.get(calendar_url)
    response.raise_for_status()
    soup = BeautifulSoup(response.text, "html.parser")

    # 날짜 포맷: 2025-01-29 → 20250129
    date_str = meeting_date.replace("-", "")
    
    # 'Press Conference' 링크 중 날짜 포함된 href 찾기
    link_tag = soup.find("a", href=re.compile(f"fomcpresconf{date_str}"), string=re.compile("Press Conference", re.I))
    
    if link_tag:
        return urljoin(BASE_URL, link_tag['href'])

    return ""

def main():
    # fomc_text = crawl_fomc_statement_text('https://www.federalreserve.gov/newsevents/pressreleases/monetary20250129a.htm')
    # print(fomc_text)
    statement_list = crawl_fomc_statement_list()
    for item in statement_list:
        print(item)
    
if __name__ == "__main__":
    main()