# 송금 서비스 (Remittance Service)

## 목적
이 프로젝트는 송금 서비스의 기본 기능을 구현한 과제입니다.  
주요 기능으로는 계좌 생성, 계좌 삭제, 입금, 출금, 이체, 거래 내역 조회가 포함되어 있습니다.
---

## 기술 스택
- **Java 17**
- **Spring Boot 3.4.2**
- **H2 In-Memory Database**
- **Redisson** (분산 락 구현용)
- **Docker** (컨테이너화 및 배포용)

---

## 실행 방법
```bash
./gradlew build

docker-compose up --build
```

---

## 주요 기능
1. 계좌 생성/삭제
2. 입금/출금
3. 계좌 간 이체
4. 거래 내역 조회

---

## API 목록
| 기능 | HTTP Method | Endpoint |
|------|-------------|----------|
| 계좌 생성 | `POST` | `/api/account` |
| 계좌 삭제 | `DELETE` | `/api/account/{id}` |
| 입금 | `PATCH` | `/api/account/{id}/deposit/{amount}` |
| 출금 | `PATCH` | `/api/account/{id}/withdrawal/{amount}` |
| 이체 | `PATCH` | `/api/account/{transferId}/transfer/{depositId}/{amount}` |
| 거래 내역 조회 | `GET` | `/api/account/{id}/transaction-history` |

---
## API 명세

## 1. 계좌 생성

- **Endpoint:** `POST /api/account`
- **설명:** 새 계좌를 생성합니다.
- **요청 본문:** 없음
- **응답 본문:**
```json
{
  "statusCode": 200,
  "statusMessage": "OK",
  "message": "account creation is success",
  "data": null
}
```
## 2. 계좌 삭제
- **Endpoint**: `DELETE /api/account/{id}`
- **설명**: 지정된 ID의 계좌를 삭제합니다.
- **URL 파라미터**:
    - `id`: 삭제할 계좌의 ID (Long)
- **응답 본문**:
```json
{
  "statusCode": 200,
  "statusMessage": "OK",
  "message": "account deletion is success",
  "data": null
}
```

## 3. 입금
- **Endpoint**: `PATCH /api/account/{id}/deposit/{amount}`
- **설명**: 지정된 계좌에 금액을 입금합니다.
- **URL 파라미터**:
    - `id`: 입금을 진행할 계좌의 ID (Long)
    - `amount`: 입금할 금액 (Long)
- **응답 본문**:
```json
{
  "statusCode": 200,
  "statusMessage": "OK",
  "message": "deposit is success",
  "data": {
      "id": 1,
      "balanceAmount": 1000,
      "createdAt": "2025-02-17T10:00:00.000000",
      "updatedAt": "2025-02-17T10:05:00.000000"
  }
}
```
## 4. 출금
- **Endpoint**: `PATCH /api/account/{id}/withdrawal/{amount}`
- **설명**: 지정된 계좌에서 금액을 출금합니다.
- **URL 파라미터**:
    - `id`: 출금을 진행할 계좌의 ID (Long)
    - `amount`: 출금할 금액 (Long)
- **응답 본문**:
```json
{
  "statusCode": 200,
  "statusMessage": "OK",
  "message": "Withdrawal is success",
  "data": {
      "id": 1,
      "balanceAmount": 500,
      "createdAt": "2025-02-17T10:00:00.000000",
      "updatedAt": "2025-02-17T10:05:00.000000"
  }
}
```
## 5. 이체
- **Endpoint**: `PATCH /api/account/{transferId}/transfer/{depositId}/{amount}`
- **설명**: 지정된 계좌에서 다른 계좌로 금액을 이체합니다.
- **URL 파라미터**:
    - `transferId`: 이체를 진행할 계좌의 ID (Long)
    - `depositId`: 이체받을 계좌의 ID (Long)
    - `amount`: 이체할 금액 (Long)
- **응답 본문**:
```json
{
  "statusCode": 200,
  "statusMessage": "OK",
  "message": "transfer is success",
  "data": [
      {
          "id": 1,
          "balanceAmount": 500,
          "createdAt": "2025-02-17T10:00:00.000000",
          "updatedAt": "2025-02-17T10:05:00.000000"
      },
      {
          "id": 2,
          "balanceAmount": 1500,
          "createdAt": "2025-02-17T10:00:00.000000",
          "updatedAt": "2025-02-17T10:05:00.000000"
      }
  ]
}
```
## 6. 거래 내역 조회
- **Endpoint**: `GET /api/account/{id}/transaction-history`
- **설명**: 지정된 계좌의 거래 내역을 조회합니다.
- **URL 파라미터**:
    - `id`: 거래 내역을 조회할 계좌의 ID (Long)
- **응답 본문**:
```json
{
  "statusCode": 200,
  "statusMessage": "OK",
  "message": "transaction history is success",
  "data": [
    {
      "id": 2,
      "transactionType": "WITHDRAWAL",
      "depositId": null,
      "withdrawalId": 1,
      "amount": 500,
      "fee": 0,
      "createdAt": "2025-02-17T10:10:00.000000",
      "updatedAt": "2025-02-17T10:10:00.000000"
    },
    {
        "id": 1,
        "transactionType": "DEPOSIT",
        "depositId": 1,
        "withdrawalId": null,
        "amount": 1000,
        "fee": 0,
        "createdAt": "2025-02-17T10:00:00.000000",
        "updatedAt": "2025-02-17T10:05:00.000000"
    }
  ]
}
```
