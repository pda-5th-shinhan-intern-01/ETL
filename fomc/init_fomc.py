from crawling_fomc import crawl_fomc_statement_list, crawl_fomc_statement_text
from chatgpt import process_summarize_fil
import time
import json
import re

# json 추출 함수
def extract_json_string(gpt_response: str) -> str:
    """
    GPT 응답에서 JSON 객체 블록만 추출합니다.
    """
    match = re.search(r"\{.*\}", gpt_response, re.DOTALL)
    if match:
        return match.group(0).strip()
    return gpt_response.strip()


# 초기 FOMC 회의록 데이터 수집
def init_fomc():
    print("📘 FOMC 회의록 크롤링 및 요약 시작")

    statement_list = crawl_fomc_statement_list()

    for item in statement_list:
        url = item["url"]
        date = item["date"]
        video_url = item.get("video_url", "")

        print(f"\n📅 [{date}] 회의록 처리 중...")
        print(f"🔗 원문 링크: {url}")
        if video_url:
            print(f"🎥 영상 링크: {video_url}")

        # 회의록 본문 크롤링
        content = crawl_fomc_statement_text(url)
        if "요청 실패" in content or "본문을 찾을 수 없습니다" in content:
            print("❌ 회의록 본문 크롤링 실패")
            continue

        # GPT 요약 수행
        summary_raw = process_summarize_fil(content)
        summary_json_str = extract_json_string(summary_raw)

        try:
            summary = json.loads(summary_json_str)
        except json.JSONDecodeError:
            print("❌ JSON 파싱 실패")
            print("🧾 GPT 응답 원문:\n", summary_raw)  # 디버깅용 출력
            continue

        # 출력 필드 추출
        title = summary.get("title", "제목 없음")
        policy = summary.get("policy_decision", {}).get("rate_policy", {})
        policy_bias = policy.get("direction", "정보 없음")
        change = policy.get("change", "정보 없음")

        # 결과 출력
        print("✅ 요약 결과:")
        print(f"📌 제목: {title}")
        print(f"📅 날짜: {date}")
        print(f"🏷️ 기준금리 방향: {policy_bias}")
        print(f"🔁 금리 변동 내용: {change}")
        print(f"📰 요약 JSON:\n{json.dumps(summary, ensure_ascii=False, indent=2)}")
        print(f"📎 원문 링크: {url}")
        if video_url:
            print(f"🎬 영상 링크: {video_url}")

        # Optional: 잠깐 대기 (rate limit 방지)
        time.sleep(2)

if __name__ == "__main__":
    init_fomc()