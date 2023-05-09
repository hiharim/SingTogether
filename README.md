# SingTogether

🎤노래방 어플

소개 : 코로나19로 인해서 노래방 방문이 어려워져서 시공간적 제약 없이 비대면으로도 노래방을 즐길 수 있는 플랫폼을 개발하게 되었습니다.
자신이 부른 노래를 녹음,녹화하여 다른 사람들과 공유 할 수 있습니다. 
다른 사용자가 불러놓은 노래게시물에 사용자가 듀엣을 참여하여 노래를 녹음,녹화하고 업로드를 하면 하나의 듀엣 곡으로 만들어 주는 서비스입니다.
그리고 실시간 스트리밍을 통하여 다른 유저들과 실시간으로 소통 할 수 있습니다.


사용 기술 : 
- Apache
- PHP
- MySQL
- Node.js
- Retrofit2
- Socket.io
- FFMPEG
- WebRTC
- MediaRecorder
- MediaProjection
- FCM
- Glide
- Kakao, Google Login API


ffmpeg :
FFmpeg (www.ffmpeg.org) 은 비디오, 오디오, 이미지를 쉽게 인코딩 (Encoding), 디코딩 (Decoding), 먹싱 (Muxing), 디먹싱 (Demuxing) 할 수 있도록 도움을 주는 멀티미디어 프레임워크이다.
- `인코딩`이란 우리가 문서의 용량을 줄이기 위하여 zip 프로그램 (예: 알집, 반디집, 빵집)을 사용해서 문서를 압축하는 것처럼 동영상이나 이미지의 용량을 줄이기 위해서 압축하는 과정을 의미한다.
- `디코딩`이란 zip으로 압축된 워드 문서를 보기 위해서 먼저 zip 프로그램을 압축을 해제해야 하는 것처럼, 압축된 동영상을 재생하기 위하여 압축을 해제하는 과정을 디코딩이라고 부른다.
- `먹싱 (Muxing)`이라는 단어는 여러 입력을 하나로 합치는 과정을 의미한다
- `디먹싱 (Demuxing)`이라는 과정은 하나로 합쳐진 입력을 다시 여러 출력으로 만드는 것을 의미한다.
- 예를 들어 PC에서 다운로드를 받아서 보시는 동영상은 사실 여러 장의 정지 영상과 오디오가 하나의 파일로 (예: *.avi,* .mkv, *.mov) 먹싱되어 있는 것이다. 물론 먹싱되기 전에 정지 영상과 오디오는 각각 인코딩 과정을 통해서 압축이 되어 있는 상태이다.
- 먹싱된 동영상을 재생하기 위해서는 인코딩, 먹싱 과정과 반대로 먼저 디먹싱을 통해서 압축된 정지 영상과 압축된 오디오로 분리한 후 각각의 데이터를 디코딩해야 한다.



- 스크린샷

<img width="200" height="400" src="https://user-images.githubusercontent.com/72755537/236798560-2c735a10-4f14-47be-ac5e-28d8c68aa500.jpeg"/> <img width="200" height="400" src="https://user-images.githubusercontent.com/72755537/236798758-ac5a5eb3-fc77-4454-a730-8bea9526b056.jpeg"/> <img width="200" height="400" src="https://user-images.githubusercontent.com/72755537/236798744-b43dfddd-8695-48b0-92ab-84bcd5d1fd13.jpeg"/>

<img width="200" height="400" src="https://user-images.githubusercontent.com/72755537/236803628-691acf9e-61b8-4b20-8a57-0d780f2a19b9.jpeg"/> <img width="200" height="400" src="https://user-images.githubusercontent.com/72755537/236800436-521706f1-5756-4515-8610-bc072fae0365.jpeg"/> <img width="200" height="400" src="https://user-images.githubusercontent.com/72755537/236800453-b52db320-20b4-4688-9b30-87ea5ef2c91d.jpeg"/>

<img width="200" height="400" src="https://user-images.githubusercontent.com/72755537/236800463-9307c851-7b1f-48fe-be09-8bcba6f6c9d2.jpeg"/> <img width="200" height="400" src="https://user-images.githubusercontent.com/72755537/236800473-aebd0b5c-70ef-451e-a89e-1976032318ce.jpeg"/> <img width="200" height="400" src="https://user-images.githubusercontent.com/72755537/236800485-b80a531a-5109-4d14-9c95-76f1c8198e33.jpeg"/>


<img width="200" height="400" src="https://user-images.githubusercontent.com/72755537/236802534-cfce58f0-022f-4a6c-b615-ac42f287eac0.jpeg"/> <img width="200" height="400" src="https://user-images.githubusercontent.com/72755537/236802753-26e12e44-45d1-4a9a-bccb-3781a52524ed.jpeg"/> <img width="200" height="400" src="https://user-images.githubusercontent.com/72755537/236802814-c2bcd0d9-eb1c-4726-93be-83dac4c00704.jpeg"/>


