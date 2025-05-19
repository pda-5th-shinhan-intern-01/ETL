from datetime import datetime

def inject_fomc_datetime(summary: dict, date_str: str) -> dict:
    """
    요약 JSON에 'announcement.datetime' 필드를 주어진 날짜로 설정된 형식으로 삽입/갱신합니다.

    Parameters:
        summary (dict): GPT 요약 결과 (JSON 파싱된 dict)
        date_str (str): 'YYYY-MM-DD' 형식의 날짜 문자열 (예: '2025-01-29')

    Returns:
        dict: datetime이 삽입된 summary
    """
    try:
        dt = datetime.strptime(date_str, "%Y-%m-%d")
        formatted_date = f"{dt.year}년 {dt.month}월 {dt.day}일"
    except ValueError:
        formatted_date = date_str  # 혹시 날짜 포맷이 다르면 그대로 사용

    fixed_time = "오후 2시 (ET)"
    summary.setdefault("announcement", {})
    summary["announcement"]["datetime"] = f"{formatted_date}, {fixed_time}"
    return summary

def clean_placeholder_fields(obj):
    """
    JSON 객체에서 값이 '...'인 필드를 재귀적으로 제거함
    """
    if isinstance(obj, dict):
        return {
            k: clean_placeholder_fields(v)
            for k, v in obj.items()
            if v != "..." and clean_placeholder_fields(v) not in [None, {}, [], "..."]
        }
    elif isinstance(obj, list):
        return [clean_placeholder_fields(i) for i in obj if i != "..."]
    else:
        return obj