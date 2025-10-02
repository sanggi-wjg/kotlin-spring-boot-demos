# Commerce MSA Demo

## Architecture

### Shared module

### Services module

- User Service
- Product Service
- Order Service
- Payment Service

각 Service 모듈에서 멀티 모듈 구성은 생략함.

## 이벤트 시나리오

### 1. 정상적인 주문 및 결제 완료 시나리오

```
[Order Service] → OrderCreated 
    ↓
[Product Service] → InventoryReserved
    ↓
[Payment Service] → PaymentCompleted
    ↓
[Order Service] → OrderConfirmed
    ↓
[Product Service] → InventoryCommitted
```

### 2. 재고 부족으로 인한 주문 실패 시나리오

```
[Order Service] → OrderCreated 
    ↓
[Product Service] → InventoryReservationFailed
    ↓
[Order Service] → OrderCancelled
```

### 3. 결제 실패로 인한 Saga 보상 트랜잭션 시나리오

```
[Order Service] → OrderCreated 
    ↓
[Product Service] → InventoryReserved
    ↓
[Payment Service] → PaymentFailed
    ↓
[Product Service] → InventoryReleased (보상)
    ↓
[Order Service] → OrderCancelled (보상)
```
