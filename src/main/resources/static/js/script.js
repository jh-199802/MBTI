// 질문 데이터 - 복합 분석형
const questions = [
    {
        category: '복합분석',
        type: 'comprehensive',
        question: '새로운 직장에 첫 출근하는 날, 당신은 어떻게 하루를 보내고 어떤 첫인상을 남기려고 하나요? 구체적으로 묘사해주세요.',
        criteria: 'MBTI: 먼저 다가가는가(E) vs 관찰 후 접근(I), 구체적 계획(S) vs 전체 분위기 파악(N), 업무 중심(T) vs 관계 중심(F), 체계적 준비(J) vs 상황 대응(P) / D&D: 규칙과 절차 준수(질서) vs 자유로운 적응(혼돈), 타인 도움(선) vs 개인 성과(중립/악) / 에니어그램: 완벽한 모습(1), 도움 되기(2), 인정받기(3), 독특함(4), 관찰(5), 안전 확보(6), 즐거움(7), 주도권(8), 조화(9)'
    },
    {
        category: '복합분석',
        type: 'comprehensive',
        question: '팀 프로젝트에서 의견이 완전히 갈린 상황입니다. 절반은 A안을, 절반은 B안을 주장하고 있습니다. 당신은 어떻게 행동하고 이 상황을 해결하려 하나요?',
        criteria: 'MBTI: 적극적 중재(E) vs 신중한 관찰(I), 실용적 타협(S) vs 창의적 대안(N), 논리적 분석(T) vs 감정적 조화(F), 결정 촉구(J) vs 더 많은 논의(P) / D&D: 규칙/절차대로(질서) vs 유연한 해결(혼돈), 팀 전체 이익(선) vs 효율성 우선(중립) / 에니어그램: 올바른 방향 제시(1), 중재자 역할(2), 성과 중심 해결(3), 창의적 접근(4), 분석 후 제안(5), 신중한 검토(6), 긍정적 분위기(7), 강력한 결정(8), 갈등 회피(9)'
    },
    {
        category: '복합분석',
        type: 'comprehensive',
        question: '당신이 가장 행복하고 만족스러웠던 경험을 떠올려보세요. 그때 무엇을 하고 있었고, 왜 그렇게 만족스러웠는지 자세히 설명해주세요.',
        criteria: 'MBTI: 사람들과 함께(E) vs 혼자만의 시간(I), 구체적 성취(S) vs 의미와 가능성(N), 성과와 효율(T) vs 관계와 조화(F), 계획된 목표 달성(J) vs 즉흥적 경험(P) / D&D: 규칙 안에서의 성취(질서) vs 자유로운 탐험(혼돈), 타인에게 도움(선) vs 개인적 만족(중립) / 에니어그램: 완벽한 완성(1), 누군가 도움(2), 인정과 성공(3), 특별한 경험(4), 깊은 이해(5), 안전한 환경(6), 새로운 즐거움(7), 통제와 성취(8), 평화로운 조화(9)'
    },
    {
        category: '복합분석',
        type: 'comprehensive',
        question: '중요한 결정을 내려야 하는 상황에서 시간이 촉박합니다. 충분한 정보가 없고 주변에서는 서로 다른 조언을 해줍니다. 이럴 때 당신은 어떻게 결정을 내리나요?',
        criteria: 'MBTI: 타인과 상의(E) vs 혼자 고민(I), 기존 경험과 사실(S) vs 직감과 가능성(N), 논리적 분석(T) vs 가치와 감정(F), 빠른 결정(J) vs 더 많은 정보 수집(P) / D&D: 원칙과 규칙 준수(질서) vs 상황에 맞는 유연성(혼돈), 타인 영향 고려(선) vs 개인 이익 우선(중립/악) / 에니어그램: 올바른 선택 추구(1), 타인 의견 수용(2), 성공 가능성 중시(3), 진정성 있는 선택(4), 신중한 분석(5), 안전한 선택(6), 긍정적 결과 기대(7), 주도적 결정(8), 갈등 최소화(9)'
    },
    {
        category: '복합분석',
        type: 'comprehensive',
        question: '당신의 친한 친구가 도덕적으로 문제가 있는 일을 하려고 합니다. 말려도 듣지 않고 오히려 당신에게 도움을 요청합니다. 어떻게 반응하시겠습니까?',
        criteria: 'MBTI: 적극적 개입(E) vs 거리 두기(I), 구체적 결과 경고(S) vs 원칙적 설득(N), 논리적 반박(T) vs 감정적 호소(F), 명확한 거절(J) vs 상황 지켜보기(P) / D&D: 도덕적 원칙 고수(질서선) vs 친구 관계 우선(혼돈중립) vs 자신의 이익 고려(악) / 에니어그램: 올바름 추구(1), 친구 구하기(2), 관계 유지 vs 평판(3), 진정성 있는 관계(4), 거리 두고 관찰(5), 위험 회피(6), 문제 회피(7), 단호한 거절(8), 갈등 피하기(9)'
    },
    {
        category: '복합분석',
        type: 'comprehensive',
        question: '큰 실패를 경험한 후 당신은 어떻게 반응하고 회복하는 편인가요? 그 과정에서 가장 중요하게 생각하는 것은 무엇인가요?',
        criteria: 'MBTI: 타인과 이야기(E) vs 혼자 정리(I), 구체적 원인 분석(S) vs 의미 찾기(N), 객관적 평가(T) vs 감정적 치유(F), 체계적 회복 계획(J) vs 자연스러운 회복(P) / D&D: 원칙적 성찰(질서) vs 유연한 적응(혼돈), 타인에게 미안함(선) vs 개인적 성장 중심(중립) / 에니어그램: 완벽을 위한 개선(1), 타인의 위로(2), 재기를 통한 성공(3), 실패의 깊은 의미(4), 원인의 철저한 분석(5), 안전망 구축(6), 새로운 기회 찾기(7), 더 강한 의지(8), 평온한 수용(9)'
    },
    {
        category: '복합분석',
        type: 'comprehensive',
        question: '리더 역할을 맡게 되었을 때, 당신만의 리더십 스타일은 어떤 모습인가요? 팀원들과는 어떻게 소통하고 어떤 분위기를 만들려고 하나요?',
        criteria: 'MBTI: 적극적 소통(E) vs 필요시 소통(I), 실무 중심(S) vs 비전 중심(N), 성과 중심(T) vs 관계 중심(F), 체계적 관리(J) vs 유연한 관리(P) / D&D: 공정한 규칙(질서) vs 상황별 대응(혼돈), 팀 전체 이익(선) vs 효율성 우선(중립) / 에니어그램: 완벽한 시스템(1), 팀원 돌봄(2), 성과 달성(3), 창의적 환경(4), 전문성 기반(5), 안정적 운영(6), 즐거운 분위기(7), 강력한 추진력(8), 조화로운 팀(9)'
    },
    {
        category: '복합분석',
        type: 'comprehensive',
        question: '당신이 가장 화가 나거나 스트레스를 받는 상황은 언제이며, 그럴 때 어떻게 대처하나요? 주변 사람들은 당신의 이런 모습을 어떻게 볼 것 같나요?',
        criteria: 'MBTI: 외부 표출(E) vs 내재화(I), 구체적 문제 해결(S) vs 의미 부여(N), 논리적 해결(T) vs 감정적 해소(F), 체계적 대응(J) vs 즉흥적 대응(P) / D&D: 원칙적 분노(질서) vs 자유로운 해소(혼돈), 타인 배려(선) vs 자기중심적 해소(악) / 에니어그램: 불완전함에 화남(1), 무시받을 때(2), 실패 두려움(3), 이해받지 못함(4), 침범당함(5), 불안정함(6), 제약받음(7), 통제당함(8), 갈등 상황(9)'
    },
    {
        category: '복합분석',
        type: 'comprehensive',
        question: '10년 후 당신이 되고 싶은 모습은 어떤가요? 그 목표를 위해 지금 가장 중요하게 생각하는 것과 실제로 하고 있는 노력은 무엇인가요?',
        criteria: 'MBTI: 사회적 역할(E) vs 개인적 성취(I), 구체적 목표(S) vs 추상적 비전(N), 성과 지향(T) vs 가치 실현(F), 체계적 계획(J) vs 유연한 적응(P) / D&D: 사회적 기여(질서선) vs 개인적 자유(혼돈중립) / 에니어그램: 완벽한 삶(1), 사랑받는 사람(2), 성공한 사람(3), 특별한 사람(4), 지혜로운 사람(5), 안전한 삶(6), 풍요로운 경험(7), 강력한 영향력(8), 평화로운 삶(9)'
    },
    {
        category: '복합분석',
        type: 'comprehensive',
        question: '새로운 환경이나 변화가 있을 때 당신의 적응 과정은 어떤가요? 변화를 대하는 당신만의 방식이 있다면 설명해주세요.',
        criteria: 'MBTI: 적극적 탐색(E) vs 신중한 적응(I), 단계적 적응(S) vs 전체적 파악(N), 효율적 적응(T) vs 감정적 적응(F), 계획적 준비(J) vs 즉흥적 대응(P) / D&D: 기존 규칙 활용(질서) vs 새로운 방식 시도(혼돈), 타인과 함께 적응(선) vs 개인적 적응(중립) / 에니어그램: 완벽한 적응(1), 도움 받으며 적응(2), 빠른 성과(3), 독특한 방식(4), 충분한 이해 후 적응(5), 안전 확보 후 적응(6), 즐거운 탐험(7), 주도적 적응(8), 점진적 적응(9)'
    },
    {
        category: '복합분석',
        type: 'comprehensive',
        question: '당신에게 정의란 무엇이며, 불의를 목격했을 때 어떻게 행동하나요? 구체적인 상황을 가정해서 설명해주세요.',
        criteria: 'MBTI: 적극적 개입(E) vs 신중한 접근(I), 현실적 해결(S) vs 원칙적 접근(N), 합리적 판단(T) vs 감정적 판단(F), 즉시 행동(J) vs 상황 관찰(P) / D&D: 법적 해결(질서선) vs 직접 해결(혼돈선) vs 개입 안함(중립) vs 이용(악) / 에니어그램: 옳은 일 추구(1), 약자 보호(2), 공정한 성과(3), 진정한 정의(4), 객관적 분석(5), 안전한 방법(6), 밝은 해결(7), 강력한 대응(8), 평화로운 해결(9)'
    },
    {
        category: '복합분석',
        type: 'comprehensive',
        question: '인생에서 가장 소중하게 여기는 가치나 신념이 있다면 무엇인가요? 그것이 당신의 일상적인 선택과 행동에 어떻게 영향을 미치나요?',
        criteria: 'MBTI: 사회적 가치(E) vs 개인적 신념(I), 실용적 가치(S) vs 이상적 가치(N), 원칙과 일관성(T) vs 관계와 조화(F), 확고한 실행(J) vs 유연한 적용(P) / D&D: 사회적 규범(질서) vs 개인적 자유(혼돈), 타인 배려(선) vs 개인 추구(중립/악) / 에니어그램: 완벽함(1), 사랑과 헌신(2), 성취와 성공(3), 진정성과 독특함(4), 지식과 이해(5), 안전과 충성(6), 즐거움과 자유(7), 힘과 자율성(8), 평화와 조화(9)'
    },
    {
        category: '복합분석',
        type: 'comprehensive',
        question: '깊은 고민이나 갈등이 있을 때, 당신은 누구에게 어떤 방식으로 도움을 요청하나요? 혹은 혼자 해결하는 편인가요? 그 이유는 무엇인가요?',
        criteria: 'MBTI: 적극적 상의(E) vs 혼자 해결(I), 경험자 조언(S) vs 새로운 관점(N), 논리적 조언(T) vs 감정적 지지(F), 결론 지향(J) vs 과정 중심(P) / D&D: 공식적 도움(질서) vs 비공식적 도움(혼돈), 타인 부담 고려(선) vs 자신 필요 우선(중립) / 에니어그램: 완벽한 해답 추구(1), 정서적 지지(2), 성공한 사람 조언(3), 이해받고 싶음(4), 전문가 조언(5), 신뢰할 수 있는 사람(6), 긍정적 관점(7), 독립적 해결(8), 갈등 회피(9)'
    },
    {
        category: '복합분석',
        type: 'comprehensive',
        question: '당신만의 행복이나 만족감을 찾는 방법은 무엇인가요? 어떤 순간에 "아, 정말 살아있다는 느낌이다"라고 생각하시나요?',
        criteria: 'MBTI: 사회적 연결(E) vs 개인적 성취(I), 감각적 경험(S) vs 의미 있는 경험(N), 성취와 인정(T) vs 사랑과 조화(F), 목표 달성(J) vs 자유로운 탐험(P) / D&D: 질서 안에서의 만족(질서) vs 자유로운 즐거움(혼돈), 타인과 함께(선) vs 개인적 만족(중립) / 에니어그램: 완벽한 순간(1), 사랑받는 순간(2), 성공의 순간(3), 특별한 순간(4), 이해의 순간(5), 안전한 순간(6), 즐거운 순간(7), 통제하는 순간(8), 평화로운 순간(9)'
    },
    {
        category: '복합분석',
        type: 'comprehensive',
        question: '당신이 다른 사람들에게 어떻게 기억되고 싶은가요? 그리고 실제로 주변 사람들이 당신을 어떤 사람이라고 묘사할 것 같나요? 차이가 있다면 그 이유는 무엇일까요?',
        criteria: 'MBTI: 사회적 영향력(E) vs 개인적 진정성(I), 실용적 기여(S) vs 의미 있는 유산(N), 능력과 성과(T) vs 인간성과 따뜻함(F), 일관된 모습(J) vs 다면적 모습(P) / D&D: 모범적 인물(질서선) vs 자유로운 영혼(혼돈중립) / 에니어그램: 완벽한 사람(1), 헌신적인 사람(2), 성공한 사람(3), 특별한 사람(4), 지혜로운 사람(5), 신뢰할 수 있는 사람(6), 즐거운 사람(7), 강한 사람(8), 평화로운 사람(9)'
    }
];

class PersonalityTest {
    constructor() {
        this.currentQuestionIndex = 0;
        this.answers = [];
        this.lastResults = null;
        this.lastTap = 0; // 모바일 더블탭 방지용
        // API 키는 더 이상 클라이언트에서 관리하지 않음 (서버에서만 사용)
        this.init();
    }

    async init() {
        // 저장된 답변이 있으면 복구
        this.loadFromLocalStorage();
        
        // test.html 페이지에서는 showWelcomeScreen을 호출하지 않음
        if (!window.location.pathname.includes('/test')) {
            this.showWelcomeScreen();
        }
    }

    // 로컬 스토리지에 저장
    saveToLocalStorage() {
        const saveData = {
            currentQuestionIndex: this.currentQuestionIndex,
            answers: this.answers,
            timestamp: new Date().getTime()
        };
        localStorage.setItem('personalityTest_progress', JSON.stringify(saveData));
        console.log('답변 자동 저장됨:', this.currentQuestionIndex + 1, '번째 질문');
    }

    // 로컬 스토리지에서 복구
    loadFromLocalStorage() {
        try {
            const saved = localStorage.getItem('personalityTest_progress');
            if (saved) {
                const saveData = JSON.parse(saved);
                // 24시간 이내의 데이터만 복구
                if (new Date().getTime() - saveData.timestamp < 24 * 60 * 60 * 1000) {
                    this.currentQuestionIndex = saveData.currentQuestionIndex || 0;
                    this.answers = saveData.answers || [];
                    console.log('이전 답변 복구됨:', this.answers.length, '개');
                }
            }
        } catch (error) {
            console.log('저장된 데이터 복구 실패:', error);
        }
    }

    // 저장된 데이터 삭제
    clearSavedData() {
        localStorage.removeItem('personalityTest_progress');
        console.log('저장된 답변 데이터 삭제됨');
    }

    showWelcomeScreen() {
        const content = document.getElementById('content');

        // 저장된 답변이 있는지 확인
        const hasSevedData = this.answers.length > 0;
        const continueButton = hasSevedData ? `
            <div style="margin-bottom: 20px;">
                <button class="btn-continue" onclick="window.personalityTest.continueTest()">
                    🔄 계속하기 (${this.answers.length}/${questions.length}개 답변 완료)
                </button>
            </div>
        ` : '';

        content.innerHTML = `
            <div style="text-align: center; padding: 40px;">
                <h2 style="color: #2c3e50; margin-bottom: 20px;">🧠 AI 성격 분석 테스트</h2>
                <p style="color: #666; margin-bottom: 30px; line-height: 1.6;">
                    MBTI, D&D 성향, 에니어그램을 종합한 심층 성격 분석<br>
                    15개의 질문으로 당신만의 특별한 성격을 발견해보세요
                </p>
                <div style="margin-bottom: 30px;">
                    <!-- 메인 테스트 시작 버튼 - 크고 눈에 띄게 (위쪽) -->
                    <button class="btn-test-main" onclick="window.personalityTest.startTest()">>
                        <span class="btn-icon">🚀</span>
                        <span class="btn-text">${hasSevedData ? '새로 시작하기' : '테스트 시작하기'}</span>
                        <span class="btn-subtitle">나의 성격을 분석해보세요</span>
                    </button>
                    
                    <!-- 계속하기 버튼 (있을 경우) -->
                    ${continueButton}
                    
                    <!-- API 연결 테스트 - 작고 보조적으로 (아래쪽) -->
                    <div style="margin-top: 20px;">
                        <button class="btn-test-secondary" onclick="window.personalityTest.testAPI()">
                            🔧 API 연결 상태 확인
                        </button>
                        <button class="btn-test-secondary" onclick="window.personalityTest.testDBSave()" style="margin-left: 10px;">
                            🗃️ DB 저장 테스트
                        </button>
                    </div>
                </div>
                ${hasSevedData ? `
                <div style="background: #fff3cd; color: #856404; padding: 15px; border-radius: 10px; margin-bottom: 20px;">
                    💾 이전에 작성한 답변이 발견되었습니다! 계속하기를 누르면 ${this.currentQuestionIndex + 1}번째 질문부터 이어서 진행됩니다.
                </div>
                ` : ''}
                <div style="background: #f8f9fa; padding: 20px; border-radius: 10px; margin-top: 20px;">
                    <h4 style="color: #4ECDC4; margin-bottom: 15px;">📊 이런 것들을 알 수 있어요</h4>
                    <div style="display: grid; grid-template-columns: repeat(auto-fit, minmax(200px, 1fr)); gap: 15px; text-align: left;">
                        <div>
                            <strong style="color: #2c3e50;">MBTI 성격 유형</strong><br>
                            <small style="color: #666;">16가지 성격 유형 중 당신의 유형</small>
                        </div>
                        <div>
                            <strong style="color: #2c3e50;">D&D 도덕 성향</strong><br>
                            <small style="color: #666;">질서-혼돈, 선-악 축의 성향</small>
                        </div>
                        <div>
                            <strong style="color: #2c3e50;">에니어그램</strong><br>
                            <small style="color: #666;">9가지 성격 유형과 핵심 동기</small>
                        </div>
                        <div>
                            <strong style="color: #2c3e50;">비슷한 캐릭터</strong><br>
                            <small style="color: #666;">당신과 닮은 애니/만화 캐릭터</small>
                        </div>
                    </div>
                </div>
            </div>
        `;
    }

    continueTest() {
        this.showQuestion();
    }

    async testAPI() {
        const content = document.getElementById('content');

        // 로딩 표시
        content.innerHTML = `
            <div style="text-align: center; padding: 40px;">
                <div class="spinner" style="margin: 0 auto 20px;"></div>
                <h3>API 연결을 테스트하고 있습니다...</h3>
                <p style="color: #666;">서버에서 안전하게 처리 중입니다.</p>
            </div>
        `;

        try {
            const response = await fetch('/api/test', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({})
            });

            if (!response.ok) {
                throw new Error(`서버 요청 실패: ${response.status}`);
            }

            const data = await response.json();

            if (!data.success) {
                throw new Error(data.error || '서버에서 알 수 없는 오류가 발생했습니다.');
            }

            // 서버에서 받은 Gemini 응답 처리
            const geminiData = data.data;
            let aiResponse = '';

            if (geminiData.candidates && geminiData.candidates[0] &&
                geminiData.candidates[0].content &&
                geminiData.candidates[0].content.parts &&
                geminiData.candidates[0].content.parts[0].text) {
                aiResponse = geminiData.candidates[0].content.parts[0].text;
            } else {
                aiResponse = '응답을 받았지만 텍스트 추출에 실패했습니다.';
            }

            // 성공 화면
            content.innerHTML = `
                <div style="text-align: center; padding: 40px;">
                    <div style="background: #d4edda; color: #155724; padding: 20px; border-radius: 10px; margin-bottom: 20px;">
                        <h3>✅ API 연결 성공!</h3>
                        <p style="margin: 10px 0;">서버에서 안전하게 Gemini AI와 통신했습니다.</p>
                        <div style="background: rgba(0,0,0,0.1); padding: 10px; border-radius: 5px; margin-top: 15px;">
                            <strong>AI 응답:</strong> ${aiResponse}
                        </div>
                    </div>
                    <button class="btn-test-main" onclick="window.personalityTest.startTest()"> style="margin-right: 15px;">
                        <span class="btn-icon">🚀</span>
                        <span class="btn-text">테스트 시작하기</span>
                        <span class="btn-subtitle">나의 성격을 분석해보세요</span>
                    </button>
                    <button class="btn-test-secondary" onclick="window.personalityTest.showWelcomeScreen()" style="margin-top: 15px;">
                        🏠 처음으로
                    </button>
                </div>
            `;

        } catch (error) {
            // 실패 화면
            content.innerHTML = `
                <div style="text-align: center; padding: 40px;">
                    <div style="background: #f8d7da; color: #721c24; padding: 20px; border-radius: 10px; margin-bottom: 20px;">
                        <h3>❌ API 연결 실패</h3>
                        <p style="margin: 10px 0;">다음 오류가 발생했습니다:</p>
                        <div style="background: rgba(0,0,0,0.1); padding: 10px; border-radius: 5px; margin: 15px 0; font-family: monospace; text-align: left;">
                            ${error.message}
                        </div>
                        <div style="text-align: left; background: #fff3cd; color: #856404; padding: 15px; border-radius: 5px; margin-top: 15px;">
                            <strong>💡 해결 방법:</strong><br>
                            • 서버 API 키 설정 확인<br>
                            • 인터넷 연결 확인<br>
                            • 서버 재시작 후 다시 시도<br>
                            • 브라우저 콘솔(F12)에서 자세한 오류 확인
                        </div>
                    </div>
                    <button class="btn-test-secondary" onclick="window.personalityTest.testAPI()" style="margin-right: 15px;">
                        🔄 다시 테스트
                    </button>
                    <button class="btn-test-secondary" onclick="window.personalityTest.showWelcomeScreen()">
                        🏠 처음으로
                    </button>
                </div>
            `;

            console.error('API Test Error:', error);
        }
    }

    async testDBSave() {
        const content = document.getElementById('content');

        // 로딩 표시
        content.innerHTML = `
            <div style="text-align: center; padding: 40px;">
                <div class="spinner" style="margin: 0 auto 20px;"></div>
                <h3>🗃️ DB 저장 테스트 중...</h3>
                <p style="color: #666;">샘플 데이터로 DB 저장을 테스트합니다.</p>
            </div>
        `;

        try {
            const response = await fetch('/api/test-db-save', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                }
            });

            if (!response.ok) {
                throw new Error(`서버 요청 실패: ${response.status}`);
            }

            const data = await response.json();

            if (!data.success) {
                throw new Error(data.error || '서버에서 알 수 없는 오류가 발생했습니다.');
            }

            const hasResultId = data.resultId;

            // 성공 화면
            content.innerHTML = `
                <div style="text-align: center; padding: 40px;">
                    <div style="background: #d4edda; color: #155724; padding: 20px; border-radius: 10px; margin-bottom: 20px;">
                        <h3>✅ DB 저장 테스트 완료!</h3>
                        <p style="margin: 10px 0;">${data.message}</p>
                        
                        ${hasResultId ? `
                        <div style="background: rgba(0,0,0,0.1); padding: 15px; border-radius: 5px; margin: 15px 0;">
                            <strong>🎯 DB 저장 성공!</strong><br>
                            <strong>결과 ID:</strong> ${data.resultId}<br>
                            <strong>결과 URL:</strong> <a href="${data.resultUrl}" target="_blank" style="color: #0066cc;">${data.resultUrl}</a>
                        </div>
                        ` : `
                        <div style="background: rgba(255,193,7,0.2); padding: 15px; border-radius: 5px; margin: 15px 0; color: #856404;">
                            <strong>⚠️ DB 저장 정보 없음</strong><br>
                            DB 저장 결과를 확인할 수 없습니다.
                        </div>
                        `}
                        
                        <div style="background: rgba(0,0,0,0.1); padding: 15px; border-radius: 5px; margin-top: 15px;">
                            <strong>🔍 H2 콘솔에서 확인:</strong><br>
                            <a href="/h2-console" target="_blank" style="color: #0066cc;">http://localhost:10000/h2-console</a><br>
                            <small>JDBC URL: ${data.jdbcUrl}, Username: sa</small><br>
                            <small>쿼리: ${data.query}</small>
                        </div>
                    </div>
                    
                    ${hasResultId ? `
                    <button class="btn-test-main" onclick="window.open('${data.resultUrl}', '_blank')" style="margin-right: 15px;">
                        <span class="btn-icon">🎯</span>
                        <span class="btn-text">저장된 결과 보기</span>
                        <span class="btn-subtitle">테스트 결과 페이지로</span>
                    </button>
                    ` : ''}
                    
                    <button class="btn-test-secondary" onclick="window.open('/h2-console', '_blank')" style="margin-right: 15px;">
                        🗃️ H2 콘솔 열기
                    </button>
                    <button class="btn-test-secondary" onclick="window.personalityTest.showWelcomeScreen()">
                        🏠 처음으로
                    </button>
                </div>
            `;

        } catch (error) {
            // 실패 화면
            content.innerHTML = `
                <div style="text-align: center; padding: 40px;">
                    <div style="background: #f8d7da; color: #721c24; padding: 20px; border-radius: 10px; margin-bottom: 20px;">
                        <h3>❌ DB 저장 테스트 실패</h3>
                        <p style="margin: 10px 0;">다음 오류가 발생했습니다:</p>
                        <div style="background: rgba(0,0,0,0.1); padding: 10px; border-radius: 5px; margin: 15px 0; font-family: monospace; text-align: left;">
                            ${error.message}
                        </div>
                        <div style="text-align: left; background: #fff3cd; color: #856404; padding: 15px; border-radius: 5px; margin-top: 15px;">
                            <strong>💡 확인사항:</strong><br>
                            • 서버가 정상 실행 중인지 확인<br>
                            • 브라우저 콘솔에서 자세한 오류 확인<br>
                            • H2 데이터베이스 연결 상태 확인
                        </div>
                    </div>
                    <button class="btn-test-secondary" onclick="window.personalityTest.testDBSave()" style="margin-right: 15px;">
                        🔄 다시 테스트
                    </button>
                    <button class="btn-test-secondary" onclick="window.personalityTest.showWelcomeScreen()">
                        🏠 처음으로
                    </button>
                </div>
            `;

            console.error('DB Save Test Error:', error);
        }
    }

    startTest() {
        // 테스트 페이지에서 호출된 경우에만 실제 테스트 시작
        this.currentQuestionIndex = 0;
        this.answers = [];
        this.clearSavedData(); // 새로 시작할 때는 기존 데이터 삭제
        this.showQuestion();
    }

    showQuestion() {
        const content = document.getElementById('content');
        const question = questions[this.currentQuestionIndex];
        const progress = ((this.currentQuestionIndex + 1) / questions.length) * 100;

        content.innerHTML = `
<div class="progress-bar">
<div class="progress-fill" style="width: ${progress}%"></div>
</div>
<p style="text-align: center; color: #666; margin-bottom: 20px;">
질문 ${this.currentQuestionIndex + 1} / ${questions.length}
</p>

<div class="question-container">
<div class="question">
<span class="category-badge ${question.category.toLowerCase()}">${question.category}</span>
<h3 style="font-size: 1.8rem; line-height: 1.5; margin-bottom: 30px; color: #1e293b;">${question.question}</h3>
</div>

<textarea
    id="answer"
    placeholder="💭 자유롭게 작성해주세요!

📝 구체적이고 솔직한 답변일수록 더 정확한 분석이 가능합니다.
• 실제 경험 예시가 있다면 더욱 좋아요
• 첫 번째로 떠오르는 생각을 편하게 써주세요
• 길이는 자유롭게, 마음껏 표현하세요

⌨️ Ctrl + Enter로 빠르게 다음 질문으로!"
    style="width: 100%; min-height: 400px; padding: 40px; font-size: 1.3rem; line-height: 1.9; border-radius: 24px; resize: vertical; border: 2px solid rgba(226, 232, 240, 0.8); background: rgba(255, 255, 255, 0.98); font-family: inherit; font-weight: 500; color: #2c3e50; box-shadow: inset 0 4px 8px rgba(0, 0, 0, 0.08), 0 2px 4px rgba(0, 0, 0, 0.05), 0 8px 24px rgba(59, 130, 246, 0.08); backdrop-filter: blur(16px); transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);"
></textarea>

<div class="navigation" style="margin-top: 50px;">
<button
    class="btn-secondary"
    onclick="window.personalityTest.previousQuestion()"
    ${this.currentQuestionIndex === 0 ? 'disabled' : ''}
    style="padding: 18px 36px; font-size: 1.1rem; min-height: 56px;"
>
    ← 이전 질문
</button>
<button
    class="btn-primary"
    onclick="window.personalityTest.nextQuestion()"
    id="nextBtn"
    style="padding: 18px 36px; font-size: 1.1rem; min-height: 56px;"
>
${this.currentQuestionIndex === questions.length - 1 ? '🚀 분석 시작하기' : '다음 질문 →'}
</button>
</div>

<!-- 진행 상황 안내 -->
<div style="text-align: center; margin-top: 30px; padding: 20px; background: rgba(59, 130, 246, 0.05); border-radius: 16px;">
<p style="color: #64748b; font-size: 0.95rem; margin: 0;">
📝 답변이 자동 저장됩니다 | ⏱️ 평균 소요 시간: 6-8분 | 🎯 ${Math.round(progress)}% 완료
</p>
</div>
</div>
`;

        // 이전 답변이 있다면 복원
        if (this.answers[this.currentQuestionIndex]) {
            document.getElementById('answer').value = this.answers[this.currentQuestionIndex];
        }

        // 엔터키로 다음 질문으로 이동
        document.getElementById('answer').addEventListener('keydown', (e) => {
            if (e.ctrlKey && e.key === 'Enter') {
                this.nextQuestion();
            }
        });

        // 텍스트 영역에 포커스
        setTimeout(() => {
            document.getElementById('answer').focus();
        }, 100);

        // 모바일 최적화: 입력 필드 관련 이벤트 처리
        this.addMobileOptimizations();
    }

    nextQuestion() {
        const answerElement = document.getElementById('answer');
        const answer = answerElement.value.trim();

        if (!answer) {
            alert('답변을 입력해주세요.');
            answerElement.focus();
            return;
        }

        // 답변 저장
        this.answers[this.currentQuestionIndex] = answer;

        // 로컬 스토리지에 자동 저장
        this.saveToLocalStorage();

        if (this.currentQuestionIndex === questions.length - 1) {
            // 모든 질문 완료 - 분석 시작
            this.analyzePersonality();
        } else {
            // 다음 질문으로
            this.currentQuestionIndex++;
            this.showQuestion();
        }
    }

    previousQuestion() {
        if (this.currentQuestionIndex > 0) {
            this.currentQuestionIndex--;
            this.showQuestion();
        }
    }

    async analyzePersonality() {
        const content = document.getElementById('content');

        // 로딩 화면 표시
        content.innerHTML = `
<div class="loading">
<div class="spinner"></div>
<h3>AI가 당신의 성격을 분석하고 있습니다...</h3>
<p>잠시만 기다려주세요. 이 과정은 약 30초 정도 소요됩니다.</p>
</div>
`;

        try {
            const analysisResult = await this.callGeminiAPI();
            this.lastResults = analysisResult; // 결과 저장
            this.showResults(analysisResult);
        } catch (error) {
            console.error('Analysis error:', error);
            this.showError(error.message);
        }
    }

    async callGeminiAPI() {
        // 테스트 시작 시간 기록 (추정)
        const testDuration = Math.floor(Math.random() * 300) + 180; // 3-8분 사이 랜덤

        // 답변 데이터 준비
        const answersWithQuestions = questions.map((q, index) => ({
            category: q.category,
            type: q.type,
            question: q.question,
            answer: this.answers[index],
            criteria: q.criteria
        }));

        // AI 프롬프트 작성
        const prompt = this.createAnalysisPrompt(answersWithQuestions);

        // 간단한 MBTI 타입 추정 (AI 응답에서 추출할 예정)
        const estimatedMbtiType = "ENFP"; // 임시값, AI가 정확한 값 반환할 것

        // 카테고리 점수 생성 (AI가 더 정확한 값 반환할 것)
        const categoryScores = {
            E: 60, I: 40,
            S: 35, N: 65,
            T: 45, F: 55,
            J: 30, P: 70
        };

        try {
            const response = await fetch('/api/analyze', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify({
                    prompt: prompt,
                    mbtiType: estimatedMbtiType,
                    testDuration: testDuration,
                    categoryScores: categoryScores,
                    answers: this.answers.map((answer, index) => ({ question: index + 1, answer: answer }))
                })
            });

            if (!response.ok) {
                throw new Error(`서버 요청 실패: ${response.status}`);
            }

            const data = await response.json();

            if (!data.success) {
                throw new Error(data.error || '서버에서 알 수 없는 오류가 발생했습니다.');
            }

            // 서버에서 받은 Gemini 응답 처리
            const geminiData = data.data;

            // 디버깅: 실제 응답 내용 확인
            console.log('=== 서버 API 응답 디버깅 ===');
            console.log('서버 응답:', data);
            console.log('Gemini 데이터:', geminiData);
            console.log('===========================');

            if (!geminiData.candidates || !geminiData.candidates[0] || !geminiData.candidates[0].content) {
                throw new Error('AI 응답 형식이 올바르지 않습니다.');
            }

            // parts 구조 안전하게 접근
            const content = geminiData.candidates[0].content;
            let aiResponse = '';

            console.log('aiResponse 추출 시도...');
            if (content.parts && content.parts.length > 0 && content.parts[0].text) {
                aiResponse = content.parts[0].text;
                console.log('성공: parts[0].text 사용:', aiResponse.substring(0, 200) + '...');
            } else if (content.text) {
                // 혹시 직접 text 필드가 있는 경우
                aiResponse = content.text;
                console.log('성공: content.text 사용:', aiResponse.substring(0, 200) + '...');
            } else {
                aiResponse = '응답을 받았지만 텍스트 추출에 실패했습니다. (finishReason: ' + geminiData.candidates[0].finishReason + ')';
                console.log('실패: 기본 메시지 사용:', aiResponse);
            }

            // DB 저장 성공 여부 확인
            if (data.resultId) {
                console.log('✅ DB 저장 성공! 결과 ID:', data.resultId);
                console.log('🔗 결과 URL:', data.resultUrl);

                // 결과 페이지로 리다이렉트할 수도 있음 (선택사항)
                // window.location.href = data.resultUrl;
            } else {
                console.log('⚠️ DB 저장 실패 또는 정보 없음');
            }

            // JSON 파싱 시도
            try {
                // JSON 코드 블록에서 내용 추출
                const jsonMatch = aiResponse.match(/```json\s*([\s\S]*?)\s*```/);
                if (jsonMatch) {
                    return JSON.parse(jsonMatch[1]);
                }
                // JSON 코드 블록이 없다면 전체 응답에서 JSON 파싱 시도
                return JSON.parse(aiResponse);
            } catch (parseError) {
                console.error('JSON 파싱 오류:', parseError);
                console.log('원본 응답:', aiResponse);
                // JSON이 아닌 경우 텍스트 파싱
                return this.parseTextResponse(aiResponse);
            }

        } catch (error) {
            throw new Error(`분석 중 오류가 발생했습니다: ${error.message}`);
        }
    }

    createAnalysisPrompt(answersWithQuestions) {
        return `
당신은 전문적인 성격 분석 AI입니다. 사용자의 답변을 바탕으로 MBTI, D&D 성향, 에니어그램을 정확하고 자세하게 분석해주세요.

### 분석 대상 답변:
${answersWithQuestions.map((item, index) => `
**질문 ${index + 1}:** ${item.question}
**답변:** ${item.answer}
`).join('\n')}

### 분석 요구사항:
1. **MBTI**: E/I, S/N, T/F, J/P 차원 분석하여 4글자 조합 도출
   - 각 차원별 퍼센트도 함께 제공 (합이 100%가 되도록, 예: E 30% + I 70% = 100%)
2. **D&D 성향**: 질서-혼돈, 선-악 축 분석하여 9가지 중 선택
3. **에니어그램**: 1-9번 유형 중 선택, 가능하면 날개(w) 포함

### 응답 형식 (반드시 JSON으로):
\`\`\`json
{
  "mbti": {
    "type": "MBTI타입",
    "percentages": {
      "E": 30,
      "I": 70,
      "S": 25,
      "N": 75,
      "T": 80,
      "F": 20,
      "J": 35,
      "P": 65
    },
    "description": "상세한 설명 (5-6문장)"
  },
  "dnd": {
    "alignment": "성향명",
    "description": "상세한 설명 (5-6문장)"
  },
  "enneagram": {
    "type": "유형",
    "description": "상세한 설명 (5-6문장)"
  },
  "comprehensive": {
    "summary": "종합 분석 (8-10문장)",
    "strengths": ["강점 5-6개"],
    "weaknesses": ["단점 4-5개"],
    "growth_areas": ["성장영역 5-6개"],
    "one_line_summary": "한 줄 핵심 정체성",
    "similar_characters": {
      "name": "캐릭터명",
      "source": "작품명",
      "reason": "유사성 설명 (3-4문문장)"
    },
    "recommendations": "맞춤 조언 (5-6문장)"
  }
}
\`\`\`

사용자의 답변 패턴을 종합적으로 분석하여 정확하고 개인화된 결과를 제공해주세요.

**중요: similar_characters는 반드시 애니메이션, 만화, 게임 캐릭터만 선택하세요. 실사 드라마, 영화, 소설 캐릭터는 제외합니다.**
`;
    }

    parseTextResponse(response) {
        // JSON이 아닌 텍스트 응답을 파싱하는 백업 메서드
        return {
            mbti: {
                type: "분석중",
                description: "AI 분석 결과를 처리하는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            },
            dnd: {
                alignment: "분석중",
                description: "AI 분석 결과를 처리하는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            },
            enneagram: {
                type: "분석중",
                description: "AI 분석 결과를 처리하는 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
            },
            comprehensive: {
                summary: "분석 과정에서 오류가 발생했습니다. 다시 시도해주세요.",
                strengths: ["분석 진행중"],
                weaknesses: ["분석 진행중"],
                growth_areas: ["분석 진행중"],
                one_line_summary: "분석 진행중입니다",
                similar_characters: {
                    name: "분석 진행중",
                    source: "분석 진행중",
                    reason: "분석이 완료되면 가장 비슷한 캐릭터를 추천해드립니다."
                },
                recommendations: "다시 분석을 시도해주세요."
            }
        };
    }

    showResults(results) {
        const content = document.getElementById('content');

        // 테스트 완료 후 저장된 데이터 삭제
        this.clearSavedData();

        content.innerHTML = `
<div class="results">
<h2 style="text-align: center; margin-bottom: 30px; color: #2c3e50;">
    🎭 당신의 성격 분석 결과
</h2>

<div class="result-card">
<div class="category-badge mbti">MBTI</div>
<div class="result-type">${results.mbti.type}</div>

<!-- MBTI 퍼센트 표시 -->
<div style="margin: 20px 0; padding: 15px; background: rgba(78, 205, 196, 0.1); border-radius: 10px;">
<h5 style="color: #2c3e50; margin-bottom: 15px; text-align: center;">📊 성향별 비율</h5>
<div style="display: grid; grid-template-columns: 1fr 1fr; gap: 10px;">
<div style="display: flex; align-items: center; justify-content: space-between; padding: 8px 12px; background: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
<span style="font-weight: bold; color: #4ECDC4;">E ${results.mbti.percentages?.E || 50}%</span>
<div style="flex: 1; height: 8px; background: #e0e0e0; border-radius: 4px; margin: 0 10px; position: relative;">
<div style="height: 100%; width: ${results.mbti.percentages?.E || 50}%; background: linear-gradient(45deg, #4ECDC4, #44A08D); border-radius: 4px;"></div>
</div>
<span style="font-weight: bold; color: #FF6B6B;">I ${results.mbti.percentages?.I || 50}%</span>
</div>
<div style="display: flex; align-items: center; justify-content: space-between; padding: 8px 12px; background: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
<span style="font-weight: bold; color: #4ECDC4;">S ${results.mbti.percentages?.S || 50}%</span>
<div style="flex: 1; height: 8px; background: #e0e0e0; border-radius: 4px; margin: 0 10px; position: relative;">
<div style="height: 100%; width: ${results.mbti.percentages?.S || 50}%; background: linear-gradient(45deg, #4ECDC4, #44A08D); border-radius: 4px;"></div>
</div>
<span style="font-weight: bold; color: #FF6B6B;">N ${results.mbti.percentages?.N || 50}%</span>
</div>
<div style="display: flex; align-items: center; justify-content: space-between; padding: 8px 12px; background: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
<span style="font-weight: bold; color: #4ECDC4;">T ${results.mbti.percentages?.T || 50}%</span>
<div style="flex: 1; height: 8px; background: #e0e0e0; border-radius: 4px; margin: 0 10px; position: relative;">
<div style="height: 100%; width: ${results.mbti.percentages?.T || 50}%; background: linear-gradient(45deg, #4ECDC4, #44A08D); border-radius: 4px;"></div>
</div>
<span style="font-weight: bold; color: #FF6B6B;">F ${results.mbti.percentages?.F || 50}%</span>
</div>
<div style="display: flex; align-items: center; justify-content: space-between; padding: 8px 12px; background: white; border-radius: 8px; box-shadow: 0 2px 4px rgba(0,0,0,0.1);">
<span style="font-weight: bold; color: #4ECDC4;">J ${results.mbti.percentages?.J || 50}%</span>
<div style="flex: 1; height: 8px; background: #e0e0e0; border-radius: 4px; margin: 0 10px; position: relative;">
<div style="height: 100%; width: ${results.mbti.percentages?.J || 50}%; background: linear-gradient(45deg, #4ECDC4, #44A08D); border-radius: 4px;"></div>
</div>
<span style="font-weight: bold; color: #FF6B6B;">P ${results.mbti.percentages?.P || 50}%</span>
</div>
</div>
</div>

<div class="result-description">${results.mbti.description}</div>
</div>

<div class="result-card">
<div class="category-badge dnd">D&D 성향</div>
<div class="result-type">${results.dnd.alignment}</div>
<div class="result-description">${results.dnd.description}</div>
</div>

<div class="result-card">
<div class="category-badge enneagram">에니어그램</div>
<div class="result-type">${results.enneagram.type}</div>
<div class="result-description">${results.enneagram.description}</div>
</div>

<div class="comprehensive-analysis">
<h3 style="margin-bottom: 20px;">📊 종합 분석</h3>
<p style="margin-bottom: 20px; line-height: 1.6;">${results.comprehensive.summary}</p>

<div style="text-align: center; margin: 25px 0; padding: 20px; background: rgba(255,255,255,0.1); border-radius: 10px;">
<h4 style="color: #FFD93D; margin-bottom: 10px;">✨ 당신을 한 줄로 표현하면</h4>
<p style="font-size: 1.3em; font-weight: bold; color: #ffffff;">"${results.comprehensive.one_line_summary}"</p>
</div>

<div style="margin: 25px 0; padding: 25px; background: linear-gradient(135deg, rgba(78, 205, 196, 0.15), rgba(255, 107, 107, 0.15)); border-radius: 15px; border: 2px solid rgba(255, 215, 0, 0.3);">
<h4 style="color: #FFD93D; margin-bottom: 15px; text-align: center;">🎭 당신과 가장 비슷한 캐릭터</h4>
<div style="text-align: center; background: rgba(255,255,255,0.1); padding: 20px; border-radius: 12px;">
<h3 style="color: #4ECDC4; margin-bottom: 8px; font-size: 1.5em;">${results.comprehensive.similar_characters.name}</h3>
<p style="color: #FFB84D; font-size: 1.1em; margin-bottom: 15px; font-weight: bold;">${results.comprehensive.similar_characters.source}</p>
<p style="font-size: 1.05em; line-height: 1.5; color: #FFB84D;">${results.comprehensive.similar_characters.reason}</p>
</div>
</div>

<div style="display: grid; grid-template-columns: 1fr 1fr 1fr; gap: 20px; margin-bottom: 20px;">
<div>
<h4 style="color: #4ECDC4; margin-bottom: 10px;">💪 주요 강점</h4>
<ul style="margin-left: 20px;">
${results.comprehensive.strengths.map(strength => `<li>${strength}</li>`).join('')}
</ul>
</div>
<div>
<h4 style="color: #FF6B6B; margin-bottom: 10px;">⚠️ 주요 단점</h4>
<ul style="margin-left: 20px;">
${results.comprehensive.weaknesses.map(weakness => `<li>${weakness}</li>`).join('')}
</ul>
</div>
<div>
<h4 style="color: #FFB84D; margin-bottom: 10px;">🌱 성장 영역</h4>
<ul style="margin-left: 20px;">
${results.comprehensive.growth_areas.map(area => `<li>${area}</li>`).join('')}
</ul>
</div>
</div>

<h4 style="color: #FFD93D; margin-bottom: 10px;">✨ 맞춤 추천</h4>
<p style="line-height: 1.6;">${results.comprehensive.recommendations}</p>
</div>

<div style="text-align: center; margin-top: 30px;">
<button class="btn-primary restart-btn" onclick="window.personalityTest.restart()">
    🔄 다시 테스트하기
</button>
<button class="btn-secondary" onclick="window.personalityTest.showMyAnswers()" style="margin: 0 10px;">
    📝 내 답변 보기
</button>
<button class="btn-secondary" onclick="window.personalityTest.shareResults()">
    📤 결과 공유하기
</button>
</div>
</div>
`;
    }

    showMyAnswers() {
        const content = document.getElementById('content');
        content.innerHTML = `
<div class="results">
<h2 style="text-align: center; margin-bottom: 30px; color: #2c3e50;">
    📝 내가 작성한 답변들
</h2>

${questions.map((question, index) => `
<div class="result-card">
<h4 style="color: #4ECDC4; margin-bottom: 10px;">질문 ${index + 1}</h4>
<p style="color: #666; margin-bottom: 15px; font-size: 0.95em;">${question.question}</p>
<div style="background: #f8f9fa; padding: 15px; border-radius: 8px; border-left: 4px solid #4ECDC4;">
<p style="line-height: 1.6; color: #333;">${this.answers[index] || '답변 없음'}</p>
</div>
</div>
`).join('')}

<div style="text-align: center; margin-top: 30px;">
<button class="btn-primary" onclick="window.personalityTest.copyAllAnswers()">
    📋 모든 답변 복사하기
</button>
<button class="btn-secondary" onclick="window.personalityTest.showResults(personalityTest.lastResults)" style="margin-left: 10px;">
    ← 결과로 돌아가기
</button>
</div>
</div>
`;
    }

    copyAllAnswers() {
        const answersText = questions.map((question, index) =>
            `질문 ${index + 1}: ${question.question}\n답변: ${this.answers[index] || '답변 없음'}\n`
        ).join('\n');

        navigator.clipboard.writeText(answersText).then(() => {
            alert('📋 모든 답변이 클립보드에 복사되었습니다!');
        }).catch(() => {
            // 복사 실패시 텍스트 영역으로 표시
            const textarea = document.createElement('textarea');
            textarea.value = answersText;
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            document.body.removeChild(textarea);
            alert('📋 모든 답변이 복사되었습니다!');
        });
    }

    showError(errorMessage) {
        const content = document.getElementById('content');

        content.innerHTML = `
<div class="error">
<h3>😔 분석 중 오류가 발생했습니다</h3>
<p>${errorMessage}</p>
<p>잠시 후 다시 시도해주세요.</p>

<div style="margin-top: 20px;">
<button class="btn-primary" onclick="window.personalityTest.analyzePersonality()">
    🔄 다시 분석하기
</button>
<button class="btn-secondary" onclick="window.personalityTest.restart()" style="margin-left: 10px;">
    🏠 처음으로
</button>
</div>
</div>
`;
    }

    restart() {
        this.currentQuestionIndex = 0;
        this.answers = [];
        this.lastResults = null;
        this.clearSavedData();
        this.init();
    }

    shareResults() {
        if (navigator.share) {
            navigator.share({
                title: 'AI 성격 분석 테스트 결과',
                text: '나의 성격 분석 결과를 확인해보세요!',
                url: window.location.href
            });
        } else {
            const url = window.location.href;
            navigator.clipboard.writeText(url).then(() => {
                alert('링크가 클립보드에 복사되었습니다!');
            }).catch(() => {
                alert('링크 복사에 실패했습니다. 수동으로 복사해주세요: ' + url);
            });
        }
    }

    // 모바일 최적화 메서드들
    addMobileOptimizations() {
        const answerElement = document.getElementById('answer');
        if (!answerElement) return;

        // 모바일 디바이스 체크
        const isMobile = /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent);

        if (isMobile) {
            // 모바일에서 키보드가 올라올 때 스크롤 조정
            answerElement.addEventListener('focus', () => {
                setTimeout(() => {
                    answerElement.scrollIntoView({
                        behavior: 'smooth',
                        block: 'center',
                        inline: 'nearest'
                    });
                }, 300); // 키보드 애니메이션 후 스크롤
            });

            // 터치 스크롤 개선
            answerElement.style.webkitOverflowScrolling = 'touch';
            answerElement.style.overscrollBehavior = 'contain';

            // iOS Safari 줌 방지 (16px 미만일 때 발생)
            if (window.getComputedStyle(answerElement).fontSize.replace('px', '') < 16) {
                answerElement.style.fontSize = '16px';
            }

            // 더블 탭 줌 방지
            answerElement.addEventListener('touchend', (e) => {
                const now = new Date().getTime();
                const timesince = now - (this.lastTap || 0);
                if (timesince < 300 && timesince > 0) {
                    e.preventDefault();
                }
                this.lastTap = now;
            });

            // 화면 회전 시 레이아웃 재조정
            window.addEventListener('orientationchange', () => {
                setTimeout(() => {
                    if (document.activeElement === answerElement) {
                        answerElement.scrollIntoView({
                            behavior: 'smooth',
                            block: 'center'
                        });
                    }
                }, 100);
            });

            // 가상 키보드 대응 (실험적)
            window.addEventListener('resize', () => {
                if (document.activeElement === answerElement) {
                    const windowHeight = window.innerHeight;
                    const documentHeight = document.documentElement.clientHeight;

                    // 키보드가 올라왔을 때 (화면 높이가 줄어들었을 때)
                    if (windowHeight < documentHeight * 0.75) {
                        document.body.style.transform = 'translateY(-50px)';
                    } else {
                        document.body.style.transform = 'translateY(0)';
                    }
                }
            });

            // 블러 시 변형 초기화
            answerElement.addEventListener('blur', () => {
                document.body.style.transform = 'translateY(0)';
            });
        }

        // 자동 높이 조절 (모든 디바이스)
        answerElement.addEventListener('input', () => {
            this.autoResizeTextarea(answerElement);
        });
    }

    // 텍스트영역 자동 높이 조절
    autoResizeTextarea(element) {
        // 현재 내용에 맞게 높이 조절
        element.style.height = 'auto';
        const scrollHeight = element.scrollHeight;
        const minHeight = window.innerWidth <= 480 ? 120 : window.innerWidth <= 768 ? 160 : 400; // 모바일에서 더 작게
        const maxHeight = window.innerHeight * 0.5; // 화면 높이의 50%까지 (60%에서 줄임)

        element.style.height = Math.min(Math.max(scrollHeight, minHeight), maxHeight) + 'px';
    }

    // 모바일 디바이스 체크
    isMobileDevice() {
        return /Android|webOS|iPhone|iPad|iPod|BlackBerry|IEMobile|Opera Mini/i.test(navigator.userAgent) ||
               (window.innerWidth <= 768 && window.innerHeight <= 1024);
    }

    // 화면 크기에 따른 동적 스타일 조정
    adjustForScreenSize() {
        const isMobile = this.isMobileDevice();
        const content = document.getElementById('content');

        if (isMobile && content) {
            // 모바일에서 추가 여백 조정
            content.style.padding = '1rem 0.5rem';

            // 질문 컨테이너 찾기
            const questionContainer = content.querySelector('.question-container');
            if (questionContainer) {
                questionContainer.style.margin = '0 0.25rem';
                questionContainer.style.padding = '16px 12px';
            }
        }
    }
}

// PersonalityTest 인스턴스는 전역에서 하나만 생성되도록 관리
// 전역 변수로 선언하여 어디서든 접근 가능하게 함
window.personalityTest = null;

// DOM 로드 완료 후 초기화
document.addEventListener('DOMContentLoaded', function() {
    try {
        window.personalityTest = new PersonalityTest();
        console.log('✅ PersonalityTest 초기화 성공');
    } catch (error) {
        console.error('❌ PersonalityTest 초기화 실패:', error);
        // 에러가 발생해도 기본 기능은 작동하도록
        window.personalityTest = {
            testAPI: function() { alert('PersonalityTest 로드 실패'); },
            testDBSave: function() { alert('PersonalityTest 로드 실패'); },
            startTest: function() { alert('PersonalityTest 로드 실패'); }
        };
    }
});
