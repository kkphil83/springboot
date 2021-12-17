node {
	String stageName = ""

	stageName = "Get Source"	
	stage(stageName) {
		echo "**** START : " + stageName
		
		git "http://cloudhr:hyyxKFv1JweidVza5KR4@192.168.229.153:15100/root/cloudhr-portal.git"
		sh "ls -al"	
	}

	//-- 환경변수 파일 읽어서 변수값 셋팅
	def props = readProperties  file:"./deployment-ocp/pipeline-ocp.properties"  // __custom__
	def imageRegistry = props["imageRegistry"]
	def image = props["image"]
	def appname = props["appname"]
	def containername = props["containername"]
	def baseDir = props["baseDir"]
	def baseDeployDir = props["baseDeployDir"]
	def ocpClusterDomain = props["ocpClusterDomain"]
	def appDomain = props["appDomain"]
	def namespace = "hrl"
	def newColor = ""
	def currentColor = ""

	// tag를 재정의 함. 이미지 버전이 달라야 배포시 컨테이너에서 인식
    def now = new Date()
    def tag = now.format("yyMMdd-HHmmss", TimeZone.getTimeZone('Asia/Seoul'))

	// ocp 내부에서 접속가능한 이미지 레지스트리 주소
	def internalRegistry = "image-registry.openshift-image-registry.svc:5000"

	// 숫자인지 Null 인지 확인하여 숫자로 변환
	def toIntOrNull = { it?.isInteger() ? it.toInteger() : null }

	try {	
		stageName = "Build gradle project"
		stage(stageName) {
			echo "\n******* START : " + stageName + "\n"

			sh "/opt/gradle/bin/gradle -b build_linux_nexus.gradle -x test build"
			
			echo "\n****** RESULT ******\n"
			sh "pwd"
			sh "ls -al ${baseDir}/build/libs"	
		}	

		stageName = "Check the current color"	
		stage(stageName) {
			echo "\n******* START : " + stageName + "\n"

			sh "oc login -u admin -p admin https://api-int.${ocpClusterDomain}:6443  --insecure-skip-tls-verify"
			sh "oc config set-context --namespace=${namespace} --current"
			sh "oc get svc ${appname}-svc -o wide | grep svc | grep blue | wc -l > currentColor.txt"
			currentColor = sh(script: 'cat currentColor.txt', returnStdout: true)
			newColor = ""

			if(toIntOrNull(currentColor) > 0){
				currentColor = "blue"
				newColor = "green"
			}else{
				currentColor = "green"
				newColor = "blue"
			}

			echo "\n # Current Color : " + currentColor + "\n #     New Color : " + newColor + "\n"
		}	

		stageName = "Build Container Image"	
		stage(stageName) {
			echo "\n******* START : " + stageName + "\n"

    	    def pw = sh(script: 'oc whoami -t', returnStdout: true)
        	sh "sudo podman login -u admin  ${imageRegistry} --tls-verify=false -p ${pw}"

        	sh "sudo podman build -f ${baseDir}${baseDeployDir}/Dockerfile -t ${imageRegistry}/${namespace}/${image}:${tag} ${WORKSPACE}"
			sh "sudo podman push ${imageRegistry}/${namespace}/${image}:${tag} --tls-verify=false "
			sh "sudo podman tag ${imageRegistry}/${namespace}/${image}:${tag} ${imageRegistry}/${namespace}/${image}:latest"
			sh "sudo podman push ${imageRegistry}/${namespace}/${image}:latest --tls-verify=false"
		}
				
		stageName = "Deploy to OCP"
		stage( stageName ) {
			echo "\n******* START : " + stageName + "\n"

// hrl 네임스페이스에 서비스 배포를 위한 파일명 설정
			def configMapYaml = "configmap.yaml"
			def newDeploymentYaml = "deploy-${namespace}-${newColor}.yaml"
			def curDeploymentYaml = "deploy-${namespace}-${currentColor}.yaml"

// configmap 적용
			sh "oc apply -n ${namespace} -f ${baseDir}${baseDeployDir}/${configMapYaml}"

// 기존에 Deployment 가 존재하는지 확인
			sh "oc get deploy ${appname}-blue -n ${namespace} | grep ${appname} | wc -l > existed.txt"
			def blueExisted = sh(script: 'cat existed.txt', returnStdout: true)
			sh "oc get deploy ${appname}-green -n ${namespace} | grep ${appname} | wc -l > existed.txt"
			def greenExisted = sh(script: 'cat existed.txt', returnStdout: true)
			int existed = blueExisted.toInteger() + greenExisted.toInteger()

			echo "\n******* existed color count : " + existed + "\n"

			if(existed > 0){
				if(existed == 2){				
// 기존에 Blue/Green 모두 존재하는 경우 => 신규 버전 Rolling Update
					echo "\n******* 기존에 Blue/Green 모두 존재하는 경우 => 신규 버전 : " + newColor + " Rolling Update \n"
						
					sh "oc set image deployment/${appname}-${newColor} ${containername}=${internalRegistry}/${namespace}/${image}:${tag} --record -n ${namespace}"
					sh "oc rollout status deployment.v1.apps/${appname}-${newColor} -n ${namespace}"
					sh "oc patch service ${appname}-svc-test --type merge --patch '{\"spec\":{\"selector\":{\"color\":\"${newColor}\"}}}'"
				}else if(existed == 1){
// 기존에 Blue/Green 중 하나만 존재하는 경우 => 신규 버전 배포
					echo "\n******* 기존에 Blue/Green 중 하나만 존재하는 경우 => 신규 버전 : " + newColor + " 배포 \n"

					sh "oc apply -n ${namespace} -f ${baseDir}${baseDeployDir}/${newDeploymentYaml}"
					sh "oc patch service ${appname}-svc-test --type merge --patch '{\"spec\":{\"selector\":{\"color\":\"${newColor}\"}}}'"
				}

				echo "\n****** New Color - Result ******\n"
				sh "oc get deployment -n ${namespace} | grep ${newColor}"
				sh "oc get pod -n ${namespace} | grep ${newColor}"

				echo " TEST URL : http://test.${appname}.${namespace}.${appDomain} \n"

// 신규 버전을 서비스하기 위한 승인 절차
				try {
						timeout(time: 10, unit: 'MINUTES') { 
						input("\n### 신규 버전으로 서비스 하시겠습니까? (대기시간 10분)\n") 
					}
					echo "\n******* 신규 버전으로 서비스합니다. ********\n" 

					sh "oc patch service ${appname}-svc --type merge --patch '{\"spec\":{\"selector\":{\"color\":\"${newColor}\"}}}'"
					sh "oc patch service ${appname}-svc-test --type merge --patch '{\"spec\":{\"selector\":{\"color\":\"${currentColor}\"}}}'"

				} catch (Exception e) { 
					echo "\n******* 기존 버전을 유지합니다. ********\n"
				}

			}else{
// 기존 서비스가 없는 초기 상태인 경우 => Blue/Green 모두 배포
				echo "\n******* 기존 서비스가 없는 초기 상태인 경우 => Blue, Green 모두 배포 \n"

				sh "oc apply -n ${namespace} -f ${baseDir}${baseDeployDir}/${curDeploymentYaml}"
				sh "oc apply -n ${namespace} -f ${baseDir}${baseDeployDir}/${newDeploymentYaml}"
				sh "oc apply -n ${namespace} -f ${baseDir}${baseDeployDir}/svc-test.yaml"
				sh "oc apply -n ${namespace} -f ${baseDir}${baseDeployDir}/svc.yaml"
				sh "oc expose service ${appname}-svc-test --hostname test.${appname}.${namespace}.${appDomain}"
				sh "oc expose service ${appname}-svc --hostname ${appname}.${namespace}.${appDomain}"					
			}

			echo "\n****** RESULT ******\n"
			sh "oc get deployment -n ${namespace} | grep ${appname}"
			sh "oc get pod -n ${namespace} | grep ${appname}"
			sh "oc get svc -n ${namespace} | grep ${appname}"
			sh "oc get route -n ${namespace} | grep ${appname}"

			echo "\n******* FINISH Deploy : SUCCESS \n"
			echo " Service URL : http://${appname}.${namespace}.${appDomain} \n"
			echo " TEST    URL : http://test.${appname}.${namespace}.${appDomain} \n"
		}

	} catch(Exception e) {
		currentBuild.result = "FAILED"
		println(e.toString());
        println(e.getMessage());
        println(e.getStackTrace()); 			
	}
}
