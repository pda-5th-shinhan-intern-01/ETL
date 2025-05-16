package com.example.collector.util;


import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Component
public class TickerNameMapper {

    private final Map<String, String> tickerToName;

    public TickerNameMapper() {
        tickerToName = new HashMap<>();

        // 헬스케어
        tickerToName.put("LLY", "일라이 릴리");
        tickerToName.put("JNJ", "존슨앤드존슨");
        tickerToName.put("UNH", "유나이티드헬스");
        tickerToName.put("ABBV", "애브비");
        tickerToName.put("ABT", "애보트");
        tickerToName.put("TMO", "써모피셔");
        tickerToName.put("MRK", "머크");
        tickerToName.put("ISRG", "인튜이티브 서지컬");
        tickerToName.put("BSX", "보스턴 사이언티픽");
        tickerToName.put("VRTX", "버텍스");

        // IT/기술
        tickerToName.put("MSFT", "마이크로소프트");
        tickerToName.put("AAPL", "애플");
        tickerToName.put("NVDA", "엔비디아");
        tickerToName.put("AVGO", "브로드컴");
        tickerToName.put("ORCL", "오라클");
        tickerToName.put("IBM", "IBM");
        tickerToName.put("CSCO", "시스코");
        tickerToName.put("ACN", "액센츄어 A");
        tickerToName.put("ADBE", "어도비");
        tickerToName.put("AMD", "AMD");

        // 자유소비재
        tickerToName.put("AMZN", "아마존");
        tickerToName.put("TSLA", "테슬라");
        tickerToName.put("HD", "홈디포");
        tickerToName.put("MCD", "맥도날드");
        tickerToName.put("BKNG", "부킹홀딩스");
        tickerToName.put("TJX", "TJX");
        tickerToName.put("LOW", "로우스");
        tickerToName.put("SBUX", "스타벅스");
        tickerToName.put("CMG", "치폴레");
        tickerToName.put("NKE", "나이키 B");

        // 금융
        tickerToName.put("BRK.B", "버크셔 해서웨이 B");
        tickerToName.put("JPM", "JP모간 체이스");
        tickerToName.put("V", "비자 A");
        tickerToName.put("MA", "마스터카드 A");
        tickerToName.put("BAC", "뱅크오브아메리카");
        tickerToName.put("WFC", "웰스파고");
        tickerToName.put("SCHW", "찰스슈왑");
        tickerToName.put("AXP", "아메리칸 익스프레스");
        tickerToName.put("PGR", "프로그레시브");
        tickerToName.put("SPGI", "S&P 글로벌");

        // 유틸리티
        tickerToName.put("NEE", "넥스트에라에너지");
        tickerToName.put("SO", "서던컴퍼니");
        tickerToName.put("DUK", "듀크에너지");
        tickerToName.put("CEG", "컨스텔레이션에너지");
        tickerToName.put("AEP", "아메리칸일렉트릭파워");
        tickerToName.put("SRE", "셈프라");
        tickerToName.put("XEL", "엑셀에너지");
        tickerToName.put("PCG", "PG&E");
        tickerToName.put("EXC", "엑셀론");
        tickerToName.put("PEG", "퍼블릭서비스엔터프라이즈");

        // 에너지
        tickerToName.put("XOM", "엑슨모빌");
        tickerToName.put("CVX", "셰브론");
        tickerToName.put("COP", "코노코필립스");
        tickerToName.put("WMB", "윌리엄스컴퍼니스");
        tickerToName.put("OKE", "원오크");
        tickerToName.put("SLB", "슐럼버거");
        tickerToName.put("KMI", "킨더모건");
        tickerToName.put("EOG", "EOG리소시스");
        tickerToName.put("FANG", "다이아몬드백에너지");
        tickerToName.put("OXY", "옥시덴탈페트롤리엄");

        // 필수소비재
        tickerToName.put("WMT", "월마트");
        tickerToName.put("COST", "코스트코");
        tickerToName.put("PG", "프록터앤갬블");
        tickerToName.put("KO", "코카콜라");
        tickerToName.put("PM", "필립모리스");
        tickerToName.put("PEP", "펩시코");
        tickerToName.put("MO", "알트리아그룹");
        tickerToName.put("CL", "콜게이트");
        tickerToName.put("MDLZ", "몬델리즈");
        tickerToName.put("TGT", "타겟");

        // 커뮤니케이션 서비스
        tickerToName.put("META", "메타");
        tickerToName.put("GOOGL", "알파벳 A");
        tickerToName.put("GOOG", "알파벳 C");
        tickerToName.put("NFLX", "넷플릭스");
        tickerToName.put("TMUS", "T모바일");
        tickerToName.put("T", "AT&T");
        tickerToName.put("VZ", "버라이즌");
        tickerToName.put("DIS", "월트디즈니");
        tickerToName.put("CMCSA", "컴캐스트");
        tickerToName.put("CHTR", "차터커뮤니케이션즈");

        // 산업재
        tickerToName.put("GE", "GE 에어로스페이스");
        tickerToName.put("UBER", "우버 테크놀로지스");
        tickerToName.put("RTX", "RTX 코퍼레이션");
        tickerToName.put("DE", "디어 & 컴퍼니");
        tickerToName.put("BA", "보잉");
        tickerToName.put("HON", "허니웰");
        tickerToName.put("CAT", "캐터필러");
        tickerToName.put("UNP", "유니언퍼시픽");
        tickerToName.put("CTAS", "신타스");
        tickerToName.put("ETN", "이튼");

        // 소재
        tickerToName.put("LIN", "린데");
        tickerToName.put("SHW", "셔윈-윌리엄스");
        tickerToName.put("ECL", "에콜랩");
        tickerToName.put("NEM", "뉴몬트");
        tickerToName.put("APD", "에어프로덕츠 앤 케미컬즈");
        tickerToName.put("FCX", "프리포트 맥모란");
        tickerToName.put("CTVA", "코르테바");
        tickerToName.put("MLM", "마틴 머리에타 머티리얼즈");
        tickerToName.put("VMC", "벌칸 머티리얼즈");
        tickerToName.put("SMR", "스머핏 웨스트록");

        // 리츠
        tickerToName.put("AMT", "아메리칸타워");
        tickerToName.put("PLD", "프로로지스");
        tickerToName.put("WELL", "웰타워");
        tickerToName.put("EQIX", "이퀴닉스");
        tickerToName.put("DLR", "디지털리얼티");
        tickerToName.put("SPG", "사이먼프라퍼티그룹");
        tickerToName.put("CCI", "크라운캐슬");
        tickerToName.put("PSA", "퍼블릭스토리지");
        tickerToName.put("O", "리얼티인컴");
        tickerToName.put("VICI", "비치프라퍼티스");
    }

    public String getKoreanName(String ticker) {
        return tickerToName.getOrDefault(ticker, "알 수 없음");
    }

    public Set<String> getAllTickers() {
        return tickerToName.keySet();
    }

    public Map<String, String> getTickerNameMap() {
        return tickerToName;
    }

    public static String toYahooSymbol(String ticker) {
        return ticker.replace(".", "-");
    }
}
