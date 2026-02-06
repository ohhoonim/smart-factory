# Smart-Factory

## 스마트팩토리 

스마트팩토리는 제조공정을 처리하는 MES 구현과 제조공정을 뒷받침하는 관련 Back Office 시스템을 구축하는 것을 목표로 합니다.

### 스마트공장 구축지원사업

- 스마트공장사업관리시스템 : https://www.smart-factory.kr/
    - 자료실 : https://www.smart-factory.kr/usr/np/rr/ma/recsroom

## 소스 Folder 구성

- back: back-end 구동 프로젝트 
    - factory-api-module: modules
    - factory-batch: batches
- front: ui, mobile 구동 프로젝트  
    - factory-ui-desktop: ui for desktop
    - factory-ui-mobile: ui for mobile(phone, tablet)
- artifacts
    - spec: 요구사항 정의서, 유즈케이스 명세 
    - ops: 배포 방법, 설정 파일, 인프라 관련 문서
    - qa: 테스트 시나리오, 테스트 결과 리포트

향후 기능 추가에 따른 폴더 추가가 있을 수 있습니다. 개발 편의를 위해 'Multi Root Workspace'를 사용하는 경우 '*.code-workspace'를 작성해주세요. 

## vscode용 code-workspace

목적에 맞게 code-workspace를 작성 한 후 커밋해주시면 됩니다. '.gitignore' 되지 않음. Spring Boot, React와 같이 개발 언어가 다를 경우 'Multi Root Workspace'가 추천되지 않습니다. 

- backend-dev: factory-api-module, factory-batch

## 소스관리

### merge 전략
- [main] 브랜치
    - main에 직접 push를 금지합니다
    - Github에 Pull Request 이후 "Squash & Merge"를 사용합니다. PR하나당 커밋 코멘트 하나입니다.
- [features] 브랜치
    - "features/[이슈번호]" 형태로 브랜치 생성합니다. 
    - 예시) 'features/10'
    - Pull Request 전에 features 브랜치에 main 브랜치 최신화는 필수입니다.
        - main이 자주 변경되는 상황이라면 merge를 추천하고,
        - 해당 branch를 개발자 1인만 사용하는 경우 PR직전 rebase를 추천합니다.
    - (참고) vscode에서 브랜치 생성하기
        - Github Pull Requests 확장을 설치한 후 다음 옵션을 추가해주면 git issue에서 브랜치를 생성시 features/[이슈번호]로 만들어준다. 
        ```json
        "githubIssues.issueBranchTitle": "features/${issueNumber}"
        ``` 


### 코멘트 남기는 법
- '#이슈번호': 해당 이슈로 링크가 걸립니다.
- 'fixed #이슈번호': main 브랜치에 머지하면 해당 이슈가 자동으로 closed 됩니다. 

## 빌드배포 

- 개별 프로젝트 배포 지침을 따릅니다.  
- artifacts/ops 에 작성합니다.


## 라이센스

MIT License