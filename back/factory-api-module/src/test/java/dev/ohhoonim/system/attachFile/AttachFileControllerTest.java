package dev.ohhoonim.system.attachFile;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.document;
import static org.springframework.restdocs.mockmvc.MockMvcRestDocumentation.documentationConfiguration;
import static org.springframework.restdocs.operation.preprocess.Preprocessors.prettyPrint;
// import static org.springframework.restdocs.request.RequestDocumentation.partWithName;
// import static org.springframework.restdocs.request.RequestDocumentation.requestParts;
// import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
// import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.restdocs.RestDocumentationContextProvider;
import org.springframework.restdocs.RestDocumentationExtension;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.assertj.MockMvcTester;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import dev.ohhoonim.system.attachFile.activity.AttachFileService;
import dev.ohhoonim.system.attachFile.api.AttachFileController;
import dev.ohhoonim.system.attachFile.model.AttachFileId;
import dev.ohhoonim.system.attachFile.model.FileItem;
import dev.ohhoonim.system.attachFile.model.FileItemId;


@WebMvcTest(AttachFileController.class)
@ExtendWith({RestDocumentationExtension.class})
class AttachFileControllerTest {

    private MockMvcTester mvc;

    @MockitoBean
    private AttachFileService attachFileService;

    @BeforeEach
    void setUp(RestDocumentationContextProvider restDocumentation, WebApplicationContext context) {
        // 1. MockMvc를 먼저 생성하면서 REST Docs 설정을 주입합니다.
        MockMvc mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .apply(documentationConfiguration(restDocumentation).operationPreprocessors()
                        .withRequestDefaults(prettyPrint()).withResponseDefaults(prettyPrint()))
                .build();

        // 2. 생성된 MockMvc를 MockMvcTester로 감쌉니다.
        this.mvc = MockMvcTester.create(mockMvc);
    }

    @Test
    @DisplayName("신규 그룹 업로드: SUCCESS 코드와 data 필드에 26자 ULID가 응답되어야 한다")
    void uploadNewGroupTest() {
        // Given
        var file = new MockMultipartFile("files", "test.txt", "text/plain", "hello".getBytes());

        // When
        var result = mvc.post().uri("/api/attachFile/upload").multipart().file(file).exchange();

        result.assertThat().apply(document("sample-test"
            // , 
            // // 1. 요청 파라미터나 멀티파트 관련 문서화 (필요 시) requestParts(
            //     partWithName("files").description("업로드할 파일 목록")
            // ),
            // // 2. [핵심] 응답 JSON 필드 정의
            // responseFields(
            //     fieldWithPath("code").description("응답 코드 (예: SUCCESS)"),
            //     fieldWithPath("data").description("생성된 파일 그룹의 26자 ULID"),
            //     fieldWithPath("message").description("응답 메시지").optional() // 메시지 필드가 있다면 추가
            // )
        ));

        // Then
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("SUCCESS");
        assertThat(result).bodyJson().extractingPath("$.data").asString().hasSize(26);
    }

    @Test
    @DisplayName("파일 목록 조회: SUCCESS 코드와 data 필드 내에 리스트가 담겨야 한다")
    void searchFilesTest() {
        // Given
        var attachFileId = AttachFileId.Creator.generate();
        var item = new FileItem(FileItemId.Creator.generate(), "vibe.png", "/path", 100L, "png",
                false);

        given(attachFileService.getFilesFromGroup(any(AttachFileId.class)))
                .willReturn(List.of(item));

        // When
        var result = mvc.get().uri("/api/attachFile/{id}", attachFileId.getRawValue()).exchange();

        // Then
        assertThat(result).hasStatusOk();
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("SUCCESS");
        // 핵심: List 데이터는 $.data 배열 내에 위치함
        assertThat(result).bodyJson().extractingPath("$.data[0].originName").isEqualTo("vibe.png");
    }

    @Test
    @DisplayName("단일 파일 다운로드: Resource 반환 시에는 ResponseHandler가 개입하지 않아 순수 데이터가 나와야 한다")
    void downloadTest() {
        // Given
        var fileId = FileItemId.Creator.generate().getRawValue();
        var item = new FileItem(new FileItemId(fileId), "manual", "path", 500L, "pdf", false);
        Resource resource = new ByteArrayResource("PDF-DATA".getBytes());

        given(attachFileService.getFileItem(fileId)).willReturn(item);
        given(attachFileService.downloadFile(fileId)).willReturn(resource);

        // When
        var result = mvc.get().uri("/api/attachFile/download/{fileId}", fileId).exchange();

        // Then
        assertThat(result).hasStatusOk();
        // ResponseHandler의 supports에서 제외되므로 래핑 없이 순수 바디가 나옴
        assertThat(result).hasBodyTextEqualTo("PDF-DATA");

        // 헤더 검증 (Pragmatic하게 수동 추출)
        String contentDisp = result.getResponse().getHeader(HttpHeaders.CONTENT_DISPOSITION);
        assertThat(ContentDisposition.parse(contentDisp).getFilename()).contains("manual.pdf");
    }

    @Test
    @DisplayName("파일 영구 삭제: 204 No Content를 응답하고 공통 래퍼가 없어야 한다")
    void purgeFileTest() {
        // Given
        var fileId = FileItemId.Creator.generate().getRawValue();

        // When
        var result = mvc.delete().uri("/api/attachFile/purge/{fileId}", fileId).exchange();

        // Then
        assertThat(result).hasStatus(HttpStatus.NO_CONTENT);
        // @ResponseStatus(HttpStatus.NO_CONTENT)는 Body가 없으므로 Handler를 타지 않음
        then(attachFileService).should().purgeFile(new FileItemId(fileId));
    }

    @Test
    @DisplayName("예외 발생 시: ResponseHandler에 의해 ERROR 코드와 메시지가 응답되어야 한다")
    void errorHandlingTest() {
        // Given
        given(attachFileService.getFilesFromGroup(any()))
                .willThrow(new RuntimeException("조회 권한이 없습니다."));

        // When
        var result = mvc.get().uri("/api/attachFile/invalid-id").exchange();

        // Then
        assertThat(result).hasStatusOk(); // Handler가 200 OK로 감싸서 반환함
        assertThat(result).bodyJson().extractingPath("$.code").isEqualTo("ERROR");
        assertThat(result).bodyJson().extractingPath("$.message")
                .isEqualTo("올바른 AttachFileId형식이 아닙니다.");
    }
}
