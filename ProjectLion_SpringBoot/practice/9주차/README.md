# Chapter9 강의 정리

## RabbitMQ

![image](https://user-images.githubusercontent.com/83503188/160383377-bd339e71-0e10-4ca5-86c3-6ce84ca0c2d7.png)
- 단방향 통신? 한번 요청을 보내고 나면 그에 대한 관계는 끝남, 단발성, 어느 한쪽이 요청을 보내고 다른 한쪽에 요청을 보내달라 X

### 시나리오 1

![image](https://user-images.githubusercontent.com/83503188/160383514-d2d9fdea-de53-421e-a385-2b860553358c.png)

- 1초에 5번만 요청을 받는 시스템에서 1초에 10번을 요청 받으면 문제 발생
- producer <-> consumer
- 1초에 5번 요청 받는 서버 2개를 만들어 처리 => Scale out


### 시나리오 2

![image](https://user-images.githubusercontent.com/83503188/160383524-801266c1-42db-45dd-a4c9-152f53adab75.png)

- 각각의 다른 서비스에서 동일한 사용자 정보를 통해 사용 중
- 사용자 정보가 변동되면 다른 모든 서비스에 알려야함
- publisher <-> subscriber
- MSA: 하나의 큰 서버가 아니라 여러 서버에 걸쳐서 서비스를 제공하는 것


#### HTTP 요청만으로는 힘든 부분

![image](https://user-images.githubusercontent.com/83503188/160384233-55b67e48-b3b0-4be4-998f-0cb176e6a346.png)

- message queue를 두어서 서버에서 메시지를 보내면 message queue에 적재
- 관심이 있는 서버에서 queue를 통해 message를 받음
- Message Broker



### Job Queue

![image](https://user-images.githubusercontent.com/83503188/160384252-9102d9b9-b9c8-45c4-8fdf-65b6ff2de64d.png)

- Job을 Queue에 쌓아둔다,
- 해당 Job을 처리할 수 있는 서버를 따로 구성함으로써 처리하는 양을 늘림

###  Publish Subscribe Message Pattern
- 한 곳에서 일어난 데이터 변동 사항을 많은 서버에서 걸쳐서 돌려주는 패턴

![image](https://user-images.githubusercontent.com/83503188/160384262-9e53f6ac-f2ac-405c-bf5e-8ab54e3551dd.png)

### RabbitMQ
- Job Queue, Publish Subscribe Message Pattern과 같은 서비스를 구성할 수 있는 Message Broker

![image](https://user-images.githubusercontent.com/83503188/160384270-9a90d583-be63-4ba1-bad4-13436d08124e.png)

![image](https://user-images.githubusercontent.com/83503188/160384284-e12b17ee-8963-493d-8a84-6d819f2ba907.png)

- 두 개의 port: rabbitMQ 자체의 port, RabbitMQ management ui를 위한 port
- localhost:15672


![image](https://user-images.githubusercontent.com/83503188/160384300-0f7eb1bc-2e5a-4e66-a92b-4d1fddfd79c6.png)

- Username: guest
- Password: guest



#### Exchanges, Queues
- Queue: 우편함, Exchange: 우체국
- RabbitMQ는 Queue를 구현하는 미들웨어
- 메시지를 받는 서버는 항상 Queue를 선언
- Queue를 직접적으로 사용하는 것이 아니라 Exchange를 통해 메시지를 넣어줌
- Queue는 특정 Exchange에 붙어서(binding) 사용
- RabbitMQ의 Client 프로그램들은 Queue에 직접 메시지를 적재하는 것이 아닌 Exchange에 메시지를 적재하여 해당 Exchange가 어떤 Queue로 메시지를 보낼 지 결정하는 구조

> 기존에 있는 RabbitMQ를 사용하는 경우에는 yml 또는 properties파일을 설정해야함
>
> spring boot starter package에 기본적으로 전체가 다들어가있음 -> 사용자 계정(guest), localhost, port번호, … 기본으로 설정이 되어있음
>
> 설정파일 -> 나중에 다시 공부

### Job-Queue 실행 화면
#### Producer
- java -jar build/libs/producer-0.0.1-SNAPSHOT.jar

![image](https://user-images.githubusercontent.com/83503188/160384314-fe9b7901-33df-45c6-b0ce-e5af5820bcc2.png)

- 5개의 요청이 들어와서 5개를 send

#### Consumer
- java -jar -Dserver.port=8081 build/libs/consumer-0.0.1-SNAPSHOT.jar
- java -jar -Dserver.port=8082 build/libs/consumer-0.0.1-SNAPSHOT.jar

![image](https://user-images.githubusercontent.com/83503188/160384325-e297e357-c115-40f2-9b65-c91e15903876.png)
![image](https://user-images.githubusercontent.com/83503188/160384346-e95b14d3-800d-4ad0-8929-311c35b641ef.png)

- Consumer 1에서 3개, Consumer 2에서 2개를 받은 모습
- 응답을 돌려주는 방법 ? -> 아래에 나옴


### Publish Subscribe Message Pattern 실행 화면


![image](https://user-images.githubusercontent.com/83503188/160384374-84f9667e-53fd-4c42-81a3-613103607465.png)
![image](https://user-images.githubusercontent.com/83503188/160384377-f111ab77-2e04-475e-b3de-9dbc485e959e.png)
- 모든 subscriber에서 같은 내용을 확인할 수 있다.


## Redis
![image](https://user-images.githubusercontent.com/83503188/160384393-5eb5dc1b-2f3d-4a3d-9da3-80b1eddd8558.png)
- In-Memory: 휘발성 데이터
- NoSQL: SQL을 이용한 조회를 하지 않음
- 외부 캐시 또는 Message Broker로 활용
- 서버에서 받는 부하를 클라이언트로 적절히 분산할 수 있다.
- RabbitMQ, Redis를 활용해서 비동기 통신 구현


### 시나리오 1
- 사용자가 해당 위쪽 서버로 로그인 요청
- 해당 서버에서 HttpSession 내부 저장, Session값을 확인해서 사용자 판단
- 두번째 요청을 아래 서버로 보냄
- 사용자 확인X


![image](https://user-images.githubusercontent.com/83503188/160384417-bfb950be-05ba-4534-a6e6-bebefa22b8fd.png)

![image](https://user-images.githubusercontent.com/83503188/160384425-fc5e28b3-f296-4be2-b97c-13b647d99841.png)
- 로그인 정보를 공유하기 위해서 Redis활용 가능
- 로그인 요청이 들어오면 내부에 저장하지 않고(Session은 Client에서 관리) 키 값으로 Redis에 저장
- 두 번째 요청이 아래 서버로 간다고 해도 Redis를 통해 사용자 판단 가능

### 시나리오 2
- producer는 message queue에 요청을 적재
- consumer가 message queue에서 요청을 받아서 확인
- 결과를 돌려주는 방식을 만들기 위해 Redis를 사용
- 처리가 완료된 데이터를 Redis에 적재
- 기존에 요청을 보낸 서버에서 Redis를 통해 조회

![image](https://user-images.githubusercontent.com/83503188/160384445-61e6b08a-183c-4922-80a7-a900d8e3659c.png)


#### Redis 설치
- docker run –name redis-stub -d -p 6379:6379 redis:6-alpine

#### Redis dependency에 추가
> implementation ('org.springframework.boot:spring-boot-starter-data-redis')
> {
>       exclude(group: 'io.lettuce', module: 'lettuce-core')
> }
>
>implementation 'redis.clients:jedis'


### Redis 실행 화면


![image](https://user-images.githubusercontent.com/83503188/160384477-7019e5d8-3007-41ae-93e4-5ac1e5b96b9a.png)

![image](https://user-images.githubusercontent.com/83503188/160384487-9a2db520-4c73-4dc5-8a7c-92075870841c.png)

- 5초 뒤에 success로 바뀌는 모습
- 서버에서 부담할 부하를 클라이언트에 분산시킬 수 있다.


## WebSocket
- RabbitMQ에서 사용하는 AMQP, http와 마찬가지로 Application layer상에 정의된 통신 규약
- 클라이언트와 서버 사이의 양방향 통신을 이루기 위한 가장 간단한 통신 규약

![image](https://user-images.githubusercontent.com/83503188/160384495-7e779cb7-7ab9-4f6f-9f4c-48a946aeac45.png)

![image](https://user-images.githubusercontent.com/83503188/160384508-20665d70-fb3d-4481-9c8b-648889ee4a70.png)

- Upgrade: websocket 헤더 존재


![image](https://user-images.githubusercontent.com/83503188/160384525-272e32f2-04a6-4f97-9b5e-9fe37d7673d4.png)

![image](https://user-images.githubusercontent.com/83503188/160384538-9d3eea76-bb6a-4786-92c5-4bbea39ba203.png)

- 에러 발생할 때 -> 4가지 경우(onOpen, onClose, onMessage, onError)
- 4가지 사건을 기준으로 클라이언트와 서버가 프로그래밍이 진행 -> Event Driven Programming

### STOMP(Simple/Straming Text Oriented Messaging Protocol)
- WebSocket은 handshake를 통해 채널을 하는 것 까지는 정의
- 데이터 형식에 대한 정의는 없음 (제약이 없음)
- 주고받는 데이터의 해석이 어렵다
- STOMP: HTTP에서 사용하는 통신 데이터와 비슷한 데이터 규약을 Sub Protocol
- implementaition ‘org.springframework.boot:spring-boot-starter-websocket’

![image](https://user-images.githubusercontent.com/83503188/160384549-698484ee-a654-4402-b371-7707205d5b0c.png)


#### HTTP vs. WebSocket vs. STOMP

![image](https://user-images.githubusercontent.com/83503188/160384562-c819d6d9-16f3-4a66-a299-5d99c851ddea.png)

## WebClient
![image](https://user-images.githubusercontent.com/83503188/160384575-38a742a3-7b9a-4314-a2ab-f91250d24f5d.png)

- 서버에서 타 서버에 http 요청을 보낼 수 있는 기능을 마련해야함
- OpenAPI 활용, …
- WebClient:
    - Spring에서 http 요청을 보내기 위한 대표적인 Interface
    - 기존의 RestTemplate과 달리 Reactive Programming을 하는데 사용할 수 있다.
      > Reactive Programming
      >
      > 데이터 흐름과 변화 전파에 중점을 둔 프로그래밍 패러다임, 프로그래밍 언어로 정적 또는 동적인 데이터 흐름을 쉽게 표현할 수 있어야하며, 데이터 흐름을 통해 하부 실행 모델이 자동으로 변화를 전파할 수 있는 것을 의미 -> 비동기 작업을 위한 것?
        - 일반적으로 프로그래밍 함수 호출이 되면 처리 후 결과를 리턴, 다음 단계 수행
        - reactive programming은 요청 처리 중에도 해당 데이터에 대해 처리를 하고 쓰레드가 다른 요청을 할 수 있도록 풀어줄 수 있음
    - Dependencies
        - Spring Web
        - Spring Reactive Web

![image](https://user-images.githubusercontent.com/83503188/160384584-a4095d05-ae56-4ba9-92bb-0409d92bed18.png)

- Spring Reative Web -> webflux

#### NCP: Naver Cloud Platform


![image](https://user-images.githubusercontent.com/83503188/160384591-b38b9f04-8d83-4754-8049-8ca31902fcec.png)