# HTTP

## Local SSL 생성

```sh
brew install mkcert

mkdir ssl
cd ssl
mkcert dev.jayg.com localhost 127.0.0.1
```

## Hosts 파일에도 추가
```
vi /etc/

127.0.0.1       dev.jayg.com
```

## 이미지 Ref
* https://promptden.com/inspiration/?limit=20
