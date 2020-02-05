
## ICP 배포 
---
sample service6를 ICP 컨테이너에 배포하는 방법이다.
### 배포 디렉토리 구조
배포 디렉토리는 루트 밑에 deployment-k8s이고 4개의 파일이 존재한다. deploy.yaml 파일은 k8s컨테이너의 리소스를 생성한다. Dockerfile은 프로젝트를 도커 이미지로 빌드하기 위한 파일이다. Jenkinsfile-icp는  젠킨스 pipeline 스크립트이다. pipeline-icp.properties 는 파이프라인에서 사용하기 위한 변수를 미리 정의한 환경변수 정의 파일이다.
```code
deployment-k8s
    deploy.yaml                 // kubernetes 리소스
    Dockerfile                  // 도커 빌드
    Jenkinsfile-icp             // 젠킨스 파이프라인
    pipeline-icp.properties     // 파이프라인 설정정보

```


### deploy.yaml
---
kubernetes 리소스를 생성하기 위한 파일로 ingress, service, deployment 를 생성한다.  

#### deplyment
springboot-service를 배포하는 앱에 이름으로 일괄적으로 변경하여 적용한다.  
명명 규칙은 예로 백엑드 서비스의 경우 ****-service 로 하고 프론트엔드의 경우 ****-front로 분리하여 서비스의 목적을 구분하도록 한다.
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: springboot-service
  labels:
    appname: springboot-service
spec:
  selector:
    matchLabels:
      appname: springboot-service
  replicas: 1
  template:
    metadata:
      name: springboot-service
      labels:
        appname: springboot-service
    spec:
      containers:
        - name: springboot-service-container
          image: happycloudpak/springboot-service
          imagePullPolicy: Always
          env:
            - name: PORT
              value: "8081"
          ports:
            - name: port1
              containerPort: 8081
          resources:
            requests:
              cpu: 200m
              memory: 512Mi
            limits:
              cpu: 1000m
              memory: 1024Mi
```

#### service
springboot-service를 deployment와 동일하게 일괄적으로 변경하여 적용한다.  
```yaml
apiVersion: v1
kind: Service
metadata:
  name: svc-springboot-service
spec:
  type: NodePort
  selector:
    appname: springboot-service
  ports:
    - name: port-springboot-service
      port: 8090
      targetPort: 8081
```

#### Ingress
springboot-service를 deployment와 동일하게 일괄적으로 변경하여 적용한다.  
host의 경우 169.56.170.165 부분을 마스터 또는 프록시 IP(Public IP로 접속이 가능한 주소)로 변경해 준다. 이후 springboot-service.169.56.170.165.nip.io 주소로 외부에서 접속이 가능하게 된다.
```yaml
apiVersion: extensions/v1beta1
kind: Ingress
metadata:
  annotations:
    kubernetes.io/ingress.class: nginx
  name: ing-springboot-service
spec:
  rules:
  - host: springboot-service.169.56.170.165.nip.io
    http:
      paths:
      - path: /  
        backend:
          serviceName: svc-springboot-service
          servicePort: 8090
```

#### Dockerfile
---
도커 이미지로 빌드하기 위해 사용되는 파일이다. 

#### java 실행 파일 변경
**springboot-service.jar** 파일의 파일명을 변경해 준다. 자바 빌드 패스에 빌드되는 파일명을 확인하여 동일하게 넣어주면 된다.

**- pom.xml**
```xml
	<build>
		<finalName>springboot-service</finalName>
    </build>
```

**- Dockerfile**
```groovy
ENV VERTICLE_FILE springboot-service.jar
# Set the location of the verticles
ENV VERTICLE_HOME /usr/verticles
```

#### java  패키지 파일 복사
단일 프로젝트로 생성된 경우 아래와 같이 디폴트 세팅을 수정할 필요가 없다.
```dockerfile
# __modify__ inner project
COPY ./target/$VERTICLE_FILE $VERTICLE_HOME
# COPY ./springboot-service/target/$VERTICLE_FILE $VERTICLE_HOME
```
프로젝트 하위에 프로젝트가 존재 하는 경우 아래와 같이 사용한다.  
**/프로젝트루트/서브프로젝트** 로 구성된 경우 서브 프로젝트 경로를 추가해 주어야 한다. 샘플 프로젝트는 이와 같은 구조로  **/mvp-springboot-sample/springboot-service** 구조로 하위 프로젝트 주소를 추가해 주었다.
```dockerfile
# __modify__ inner project
# COPY ./target/$VERTICLE_FILE $VERTICLE_HOME
COPY ./springboot-service/target/$VERTICLE_FILE $VERTICLE_HOME
```


#### Jenkinsfile-icp
---
젠킨스 파이프라인으로 다음의 정보를 수정해 주어야 한다.   
- 프로퍼티 파일 위치 정보
- git 저장소 정보
- 이메일 주소
- Maven Repository

#### 프로퍼티 파일위치 변경
젠킨스 파이프라인에서 사용할 환경변수를 프로퍼티로 지정하여 사용할 수 있다. 각 프로젝트 환경에 맞게 위치와 파일명을 변경하면 된다.   
아래 예는 서브프로젝트의 경우로 하위 프로젝트 경로를 추가하였다. 단일 프로젝트의 경우 첫번째 경로를 참조하여 설정하면 된다.
```groovy
		//def props = readProperties  file:"./deployment-k8s/pipeline-icp.properties"  // 단일 프로젝트의 경우
		def props = readProperties  file:"./springboot-service/deployment-k8s/pipeline-icp.properties"  // 서브 프로젝트의 경우 
```

#### git 저장소 정보 
git 저장소 정보를 변경하여 준다.
```groovy
git "https://gitlab.com/happycloudpak/mvp-springboot-sample.git"
```


#### 이메일 정보 변경
git 저장소 정보를 변경하여 준다.
```groovy
def emailRecipients="happycloudpak@gmail.com"
```

#### Maven Repository

메이븐 빌드시 사용하는 메이븐 저장소를 외부 저장소에 두어 케싱처리를 하면 매 빌드시 다운로드하지 않고 진행할 수 있다. 아래는 nfs로 마운트하여 사용하는 예이다.   
서버 패스(/srv/nfs)와 nfs서버 주소(169.56.170.168)를 지정한다.

```groovy
nfsVolume(mountPath: '/root/.m2', serverAddress: '169.56.170.168', serverPath: '/srv/nfs/.m2', readOnly: false)
```

#### pipeline-icp.properties
---
젠킨스 파이프라인에서 사용할 환경설정 정보에 대해 다음의 항목을 수정해 주어야 한다.   
단, 여기서 kubernetes의 리소스 항목은 위에서 설정한 것과 동일해야 한다.   
- namespace 명
- springboot-service 로 된 부분 일괄 변경
- 이미지 저장소 정보
- sonarQube 접속 정보
- 기본 디렉토리 설정
- 외부에 노출되는 주소 변경

```properties
version=0.0.2
# kubernetes namespace 지정
namespace=springboot-service 
# 이미지 명 지정
image=happycloudpak/springboot-service
# pod 앱명 지정
appname=springboot-service
# 컨테이너 명 지정
containername=springboot-service-container
deployment=deploy.yaml

#skipStages="Inspection Code,Image Vulnerability Scanning"
skipStages=""

# don't need change, 이미지 저장소 정보 지정
dockerRegistry=https://index.docker.io/v1/
# kubernetes에서 이미지 저장소 접근하기 위한 auth정보, secret을 생성하고 생성된 명을 지정한다.
credentialRegistry=auth_dockerio


#sonarQube. 주소
icpSonarQubeURL=http://169.56.80.70:31420
# 프로젝트명
icpSonarQubeProject=springboot-service
# 프로젝트 키
icpSonarQubeLogin=135bbd36059e40b75d4e99070eca7d7d8b51f277

# 단일 프로젝트의 경우
# baseDir=.
# 복수 프로젝트의 경우
baseDir=./springboot-service

# 디폴터, 수정할 필요 없음
baseDeployDir=/deployment-k8s

# url 접속 정보, 프락시 또는 마스터 노드 정보를 기입함
icpProxyIP=169.56.170.165.nip.io
```
