#!/bin/bash

echo "🧹 불필요한 프로필 파일 정리"

# 1. prod 파일 삭제 (DB 설정이 없어서 사용 불가)
if [ -f "src/main/resources/application-prod.properties" ]; then
    echo "❌ application-prod.properties 삭제 (DB 설정 없음)"
    rm src/main/resources/application-prod.properties
fi

# 2. example 파일 삭제 (실제 사용 안함)
if [ -f "src/main/resources/application.properties.example" ]; then
    echo "❌ application.properties.example 삭제 (예제 파일)"
    rm src/main/resources/application.properties.example
fi

echo "✅ 정리 완료!"
echo ""
echo "📋 남은 프로필:"
echo "- application.properties (기본, Oracle DB)"
echo "- application-local.properties (H2 테스트용)"
echo "- application-cloud.properties (배포용)"
