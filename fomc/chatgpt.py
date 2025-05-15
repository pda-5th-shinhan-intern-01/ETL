import os
import json
import logging
import requests
from typing import List
import openai
import tiktoken
from prompt_FOMC import prompt_FOMC
from dotenv import load_dotenv

# 환경 변수에서 API Key 가져오기
load_dotenv()
OPENAI_API_KEY = os.getenv("OPENAI_API_KEY")

# 로깅 설정
logging.basicConfig(level=logging.INFO, format="%(asctime)s - %(levelname)s - %(message)s")

# OpenAI 클라이언트
client = openai.OpenAI(api_key=OPENAI_API_KEY)

# 토큰 제한 설정
MAX_INPUT_TOKENS = 5000
MAX_OUTPUT_TOKENS = 800


# 토큰 분할 함수
def divide_text_by_tokens(text: str, max_tokens: int = MAX_INPUT_TOKENS) -> List[str]:
    encoding = tiktoken.get_encoding("cl100k_base")
    tokens = encoding.encode(text)
    return [encoding.decode(tokens[i:i + max_tokens]) for i in range(0, len(tokens), max_tokens)]


# 개별 청크 요약
def summarize_chunk(chunk: str) -> str:
    prompt_text = (
        f"다음은 FOMC 회의록의 일부입니다:\n\n{chunk}\n\n"
        "이 내용을 간결하게 요약해 주세요. 특히, 원문에 등장하는 수치(예: %, 숫자 등)는 반드시 그대로 유지해 주세요."
    )
    try:
        response = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": "You are a concise financial summarizer."},
                {"role": "user", "content": prompt_text}
            ],
            max_tokens=MAX_OUTPUT_TOKENS,
            temperature=0.5
        )
        return response.choices[0].message.content.strip()
    except openai.OpenAIError as e:
        logging.error(f"GPT 요약 실패: {e}")
        return "요약 실패"


# 전체 요약 분석 (JSON 포맷 기대)
def analyze_combined_summary(summary_texts: List[str]) -> str:
    combined_summary = "\n".join(summary_texts)
    prompt = (
        f"{prompt_FOMC}\n\n"
        "다음은 위 단계에서 생성된 FOMC 회의록 요약입니다. 이를 바탕으로 회의 핵심 내용을 통합 정리하고, "
        "투자자에게 도움이 되는 인사이트를 JSON 형식으로 반환해주세요.\n\n"
        f"{combined_summary}"
    )
    try:
        response = client.chat.completions.create(
            model="gpt-4o-mini",
            messages=[
                {"role": "system", "content": "You are an expert financial analyst."},
                {"role": "user", "content": prompt}
            ],
            max_tokens=MAX_OUTPUT_TOKENS,
            temperature=0.7
        )
        return response.choices[0].message.content.strip()
    except openai.OpenAIError as e:
        logging.error(f"GPT 분석 실패: {e}")
        return "분석 실패"


# 전체 파이프라인
def process_summarize_fil(content: str) -> str:
    if not content.strip():
        logging.warning("내용 없음")
        return ""

    chunks = divide_text_by_tokens(content)
    logging.info(f"{len(chunks)}개 청크로 분할됨")

    summaries: List[str] = []
    for i, chunk in enumerate(chunks, 1):
        logging.info(f"청크 {i}/{len(chunks)} 요약 중...")
        summaries.append(summarize_chunk(chunk))

    logging.info("전체 요약 완료 → 통합 분석 시작")
    return analyze_combined_summary(summaries)


# 외부 요청 처리 및 최종 JSON 변환
def get_summary_as_json(fomc_url: str, headers: dict) -> str:
    try:
        response = requests.get(fomc_url, headers=headers)
        response.raise_for_status()
        original_text = response.text

        summary_content = process_summarize_fil(original_text)
        summary_content = summary_content.strip("```").replace("json\n", "", 1).strip()

        if summary_content.startswith("{"):
            parsed_json = json.loads(summary_content)
            return json.dumps(parsed_json, ensure_ascii=False, indent=4)
        else:
            logging.error("응답이 JSON 형식이 아님")
            return json.dumps({"error": "Response is not in JSON format"}, ensure_ascii=False, indent=4)

    except requests.exceptions.RequestException as e:
        logging.error(f"요청 오류: {e}")
        return json.dumps({"error": "Request failed"}, ensure_ascii=False, indent=4)

    except json.JSONDecodeError as e:
        logging.error(f"JSON 파싱 오류: {e}")
        return json.dumps({"error": "JSON parsing failed"}, ensure_ascii=False, indent=4)
