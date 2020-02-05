package com.springboot.microservices.sample.model;

import lombok.Data;

@Data
public class SampleUser {
	 private String  userId 		; // 사용자 아이
	 private String  userNm 		; // 사용자 이
	 private String  addr 		    ; // 주소 
	 private String  cellPhone 	    ; // 핸드폰 
	 private String  agreeInform    ; // 고객정보 동의 여부 
	 private String  birthDt 	    ; // 생년 월일 

}
