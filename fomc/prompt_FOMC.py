# 요약 프롬프트


prompt_FOMC = """
당신은 통화정책 분석 전문가입니다. 아래는 FOMC 회의 발표문 전문입니다. 이 내용을 바탕으로 다음 조건에 따라 JSON 형식으로 요약해 주세요.

### 출력 형식(JSON):
```json
{
  "title:"title": "기준금리 동결과 QT 속도 조절을 통한 점진적 완화 기조 유지",  // 회의의 핵심 정책 방향과 메시지를 담은 한 줄 요약
  "announcement": {
    "datetime": "2025년 1월 29일, 오후 2시 (ET)"
  },
  "economic_conditions": {
    "labor_market": "...",
    "consumption_investment_trade": "...",
    "inflation_trend": "...",
    "other_macro": "...",
    "keywords": ["고용 안정", "소비 증가", "물가 상승", "..."]
  },
  "policy_decision": {
    "rate_policy": {
      "direction": "freeze | raise | lower",
      "range": "5.25% - 5.50%",
      "change": "동결"  // 또는 "0.25%p 인상" 등
    },
    "ioer": "5.40%",  // 발표문에 명시된 경우만 포함
    "qt_policy": {
      "treasury": "...",
      "agency_debt_mbs": "..."
    },
    "keywords": ["기준금리 동결", "QT 속도 완화", "IOER", "..."]
  },
  "future_guidance": {
    "rationale": "...",
    "factors_to_watch": "...",
    "change_conditions": "...",
    "keywords": ["정책 유연성", "인플레이션 기대", "노동시장", "..."]
  },
  "votes": {
    "approve": {
      "count": 8,
      "members": ["Powell", "Williams", "..."],
      "reason": "..."  // 특별한 사유가 언급된 경우에만 작성
    },
    "oppose": {
      "count": 1,
      "members": ["Kashkari"],
      "reason": "..."
    },
    "abstain": {
      "count": 0,
      "members": []
    }
  },
  "additional_insight": "이 항목은 발표문에 명시되지 않은 내용이나, 시장의 해석 또는 정책의 의미를 유추할 수 있는 경우에 작성해 주세요. 독자가 이해하기 쉽게 존댓말로 작성해 주시면 좋습니다. 예: '이러한 결정은 시장에서 완화적 신호로 받아들여질 수 있습니다.'"
}
```
작성 지침:
주의: JSON 외에 아무런 텍스트도 추가하지 마세요. 응답은 반드시 JSON 객체({})만 포함해야 합니다
모든 항목은 한국어로 작성해주세요.
title은 이번 회의의 핵심 메시지를 한 줄로 요약한 제목을 작성해 주세요.(예: "금리 동결과 리스크 균형 인식 확대", "긴축 종료 후 첫 동결, 향후 유연한 정책 시사" 등)
수치(예: 금리 범위, IOER, QT 수치 등)는 원문 그대로 보존해주세요.
economic_conditions, policy_decision, future_guidance에는 각각 핵심 키워드를 keywords 배열로 정리해주세요 (3~6개).
additional_insight만 존댓말로, 나머지 항목은 간결한 요약체로 작성해 주세요.
"""