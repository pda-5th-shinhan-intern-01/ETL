# 요약 프롬프트


prompt_FOMC = """
당신은 통화정책 분석 전문가입니다. 아래는 FOMC 회의 발표문 전문입니다. 이 내용을 바탕으로 다음 조건에 따라 JSON 형식으로 요약해 주세요.

### 출력 형식(JSON):
```json
{
  "title": "기준금리 동결과 QT 속도 조절을 통한 점진적 완화 기조 유지",
  "announcement": {
    "datetime": "2025년 1월 29일, 오후 2시 (ET)"
  },
  "economic_conditions": {
    "labor_market": "...",
    "consumption_investment_trade": "...",
    "inflation_trend": "...",
    "other_macro": "...",
    "keywords": ["고용 안정", "소비 증가", "물가 상승"]
  },
  "policy_decision": {
    "rate_policy": {
      "direction": "Freeze", 
      "range": "5.25% - 5.50%",
      "change": "동결"
    },
    "ioer": "5.40%",
    "qt_policy": {
      "treasury": "...",
      "agency_debt_mbs": "..."
    },
    "keywords": ["기준금리 동결", "QT 속도 완화", "IOER"]
  },
  "future_guidance": {
    "rationale": "...",
    "factors_to_watch": "...",
    "change_conditions": "...",
    "keywords": ["정책 유연성", "인플레이션 기대", "노동시장"]
  },
  "votes": {
    "approve": {
      "count": 8,
      "members": ["Powell", "Williams"],
      "reason": "별도 언급 없음"  
    },
    "oppose": {
      "count": 1,
      "members": ["Kashkari"],
      "reason": "별도 언급 없음"
    },
    "abstain": {
      "count": 0,
      "members": []
    }
  },
  "additional_insight":"이 항목은 발표문에 명시되지 않은 내용이나, 시장의 해석 또는 정책의 의미를 유추할 수 있는 경우에 작성해 주세요. 독자가 이해하기 쉽게 존댓말로 작성해 주시면 좋습니다. 예: '이러한 결정은 시장에서 완화적 신호로 받아들여질 수 있습니다.'"
}
```
작성 지침:
주의: JSON 외에 아무런 텍스트도 추가하지 마세요. 응답은 반드시 JSON 객체({})만 포함해야 합니다
모든 항목은 한국어로 작성해주세요.
"announcement.datetime"은 회의가 실제로 열린 날짜와 시간을 반드시 "2025년 1월 29일, 오후 2시 (ET)" 와 같은 형식으로 입력하세요.
형식은 반드시 "YYYY년 M월 D일, 오후/오전 h시 (ET)"로 통일해야 합니다.
"오후/오전"은 정확히 구분하고, "ET"를 반드시 포함하세요.
"rate_policy.direction" 필드는 반드시 다음 중 하나여야 합니다: "Freeze", "Raise", "Lower"(앞에 대문자) 다른 표현("cut", "increase", "hold" 등)은 사용하지 마세요.
title은 이번 회의의 핵심 메시지를 한 줄로 요약한 제목을 작성해 주세요.(예: "금리 동결과 리스크 균형 인식 확대", "긴축 종료 후 첫 동결, 향후 유연한 정책 시사" 등)
title은 회의 핵심 메시지를 한 줄 요약으로 작성하세요.
수치(예: 금리 범위, IOER, QT 수치 등)는 원문 그대로 보존해주세요.
economic_conditions, policy_decision, future_guidance에는 각각 핵심 키워드를 keywords 배열로 정리해주세요 (3~6개).
"..."으로 응답이 오는 경우 그 항목은 JSON에서 제거해주세요 (필드 생략).
"reason"도 특별한 사유가 명시되지 않았다면 생략해도 됩니다.
additional_insight만 존댓말로, 나머지는 간결한 요약체로 작성해 주세요. vcvbnghmjtyuuㅕuㅕuㅕuuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuㅕuuuuuuuuuuuuuuuㅕㅕㅕㅕㅕㅕㅕㅕㅕㅕㅕㅕㅕㅕㅕuㅕuuuuuuuuuuuuuuuuu676767676767676767676767676767676767676767677777 7777778   i                    i                i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i i iiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiiii iiiiiiii tgvbyrhfnj676um59IiiiiiiiiiiiIIIIIIIIIIIIIIII8
주의!!!!해야할 점은 "range": "4-1/4% - 4-1/2%" 분수로 주지말고 소수로 주세요 "5.25% - 5.50%" 와 같이 주시면 됩니다. "change": "1/4% 인하"도 0.25% 인하로 주세요. 
- 항목 내용이 없는 경우, 그 항목을 **JSON에서 완전히 생략**하세요.
- 예를 들어 `"inflation_trend": "..."`처럼 `"..."`은 절대 사용하지 마세요.
- `"..."`을 포함한 key는 아예 빠져야 하며, 빈 문자열이나 null도 넣지 마세요.
- 각 keywords 배열은 GPT가 본문 내용을 바탕으로 판단하여 작성해야 합니다.
- 예시로 제공된 단어("고용 안정", "물가 상승" 등)를 그대로 복사하지 말고, 해당 회의 내용에 맞는 단어를 새롭게 추출하세요.
- 모든 keywords 배열은 반드시 3~6개 사이여야 하며, 중복 없이 핵심 개념을 표현하세요.

 """