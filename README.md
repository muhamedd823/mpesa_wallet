# M-PESA Wallet Service

Simple wallet service built with Spring Boot and PostgreSQL.

## What it does

**Move money between wallets**
```
POST /mpesa/v1/p2p/transfers
POST /mpesa/v1/p2p/transfers/{id}/reversal
```

**Pay out to a bank account**
```
POST /mpesa/v1/payouts
GET  /mpesa/v1/payouts/{id}
```

**Handle bank callbacks**
```
POST /mpesa/v1/callbacks/bank
```

**Keep balances correct under concurrent requests**
```
GET /mpesa/v1/wallets/{id}
PUT /mpesa/v1/wallets/{id}/status
```

## Stack
- Java 17
- Spring Boot 3.3.5
- PostgreSQL

## Run it
Start PostgreSQL then:
```
mvn spring-boot:run
```
Second instance:
```
mvn spring-boot:run -Dserver.port=8082
```

## Note
I used AI to assist with pushing the initial code to GitHub and fixing some errors. All application logic was written by me.
