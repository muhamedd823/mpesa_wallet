# M-PESA Wallet Service

Simple wallet service built with Spring Boot and PostgreSQL.

## What it does

Move money between wallets

POST http://localhost:8081/mpesa/v1/p2p/transfers
POST http://localhost:8081/mpesa/v1/p2p/transfers/{id}/reversal


Pay out to a bank account

POST http://localhost:8081/mpesa/v1/payouts
GET  http://localhost:8081/mpesa/v1/payouts/{id}


Handle bank callbacks

POST http://localhost:8081/mpesa/v1/callbacks/bank


Keep balances correct under concurrent requests

GET http://localhost:8081/mpesa/v1/wallets/{id}
PUT http://localhost:8081/mpesa/v1/wallets/{id}/status


## Note
I used AI to assist with pushing the code to GitHub and fixing some errors. All application logic was written by me.
