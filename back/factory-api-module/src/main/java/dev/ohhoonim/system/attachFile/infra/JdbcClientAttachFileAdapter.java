package dev.ohhoonim.system.attachFile.infra;

import dev.ohhoonim.system.attachFile.model.*;
import dev.ohhoonim.system.attachFile.port.AttachFileRepositoryPort;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class JdbcClientAttachFileAdapter implements AttachFileRepositoryPort {

    private final JdbcClient jdbcClient;

    @Override
    public Optional<AttachFile> load(AttachFileId id) {
        // 1. 부모(AttachFile) 조회
        var sqlAttach = "SELECT * FROM SYSTEM_ATTACH_FILE WHERE id = :id";
        return jdbcClient.sql(sqlAttach).param("id", id.getRawValue()).query((rs, rowNum) -> {
            // 2. 자식(FileItem) 목록 조회
            List<FileItem> items = loadFileItems(id);

            return AttachFile.reconstitute(id, items, rs.getBoolean("is_linked"),
                    rs.getTimestamp("created_at").toInstant(), rs.getString("created_by"),
                    rs.getTimestamp("modified_at").toInstant(), rs.getString("modified_by"));
        }).optional();
    }

    private List<FileItem> loadFileItems(AttachFileId attachFileId) {
        var sqlItems = "SELECT * FROM SYSTEM_FILE_ITEM WHERE attach_file_id = :attachFileId";
        return jdbcClient.sql(sqlItems).param("attachFileId", attachFileId.getRawValue())
                .query((rs, rowNum) -> new FileItem(new FileItemId(rs.getString("id")),
                        rs.getString("origin_name"), rs.getString("uploaded_path"),
                        rs.getLong("capacity"), rs.getString("extension"),
                        rs.getBoolean("is_removed")))
                .list();
    }

    @Override
    public Optional<FileItem> loadItem(FileItemId itemId) {
        var sql = "SELECT * FROM SYSTEM_FILE_ITEM WHERE id = :id";
        return jdbcClient.sql(sql).param("id", itemId.getRawValue())
                .query((rs, rowNum) -> new FileItem(new FileItemId(rs.getString("id")),
                        rs.getString("origin_name"), rs.getString("uploaded_path"),
                        rs.getLong("capacity"), rs.getString("extension"),
                        rs.getBoolean("is_removed")))
                .optional();
    }

    @Override
    @Transactional
    public void save(AttachFile attachFile) {
        // 부모 저장 (PostgreSQL 전용)
        jdbcClient.sql("""
                INSERT INTO SYSTEM_ATTACH_FILE (id, is_linked, created_at, created_by, modified_at, modified_by)
                VALUES (:id, :isLinked, :createdAt, :createdBy, :modifiedAt, :modifiedBy)
                ON CONFLICT (id) DO UPDATE SET
                    is_linked = EXCLUDED.is_linked,
                    modified_at = EXCLUDED.modified_at,
                    modified_by = EXCLUDED.modified_by
                """).param("id", attachFile.getId().getRawValue())
                .param("isLinked", attachFile.getIsLinked())
                .param("createdAt", Timestamp.from(attachFile.getCreatedAt()))
                .param("createdBy", attachFile.getCreatedBy())
                .param("modifiedAt", Timestamp.from(attachFile.getModifiedAt()))
                .param("modifiedBy", attachFile.getModifiedBy()).update();

        // 자식들 저장
        for (FileItem item : attachFile.getFileItems()) {
            this.saveFileItem(attachFile.getId(), item);
        }
    }

    private void saveFileItem(AttachFileId attachFileId, FileItem item) {
        var sqlItem =
                """
                        INSERT INTO SYSTEM_FILE_ITEM (id, attach_file_id, origin_name, uploaded_path, capacity, extension, is_removed)
                        VALUES (:id, :attachFileId, :originName, :uploadedPath, :capacity, :extension, :isRemoved)
                        ON CONFLICT (id) DO UPDATE SET
                            is_removed = EXCLUDED.is_removed
                        """;

        jdbcClient.sql(sqlItem).param("id", item.fileItemId().getRawValue())
                .param("attachFileId", attachFileId.getRawValue())
                .param("originName", item.originName()).param("uploadedPath", item.uploadedPath())
                .param("capacity", item.capacity()).param("extension", item.extension())
                .param("isRemoved", item.isRemoved()).update();
    }

    @Override
    public List<FileItem> findAllByRemovedStatus(boolean isRemoved) {
        var sql = "SELECT * FROM SYSTEM_FILE_ITEM WHERE is_removed = :isRemoved";
        return jdbcClient.sql(sql).param("isRemoved", isRemoved)
                .query((rs, rowNum) -> new FileItem(new FileItemId(rs.getString("id")),
                        rs.getString("origin_name"), rs.getString("uploaded_path"),
                        rs.getLong("capacity"), rs.getString("extension"),
                        rs.getBoolean("is_removed")))
                .list();
    }

    @Override
    @Transactional
    public void deleteItemMetadata(FileItemId itemId) {
        var sql = "DELETE FROM SYSTEM_FILE_ITEM WHERE id = :id";
        jdbcClient.sql(sql).param("id", itemId.getRawValue()).update();
    }

    @Override
    public List<AttachFile> findUnlinkedOldFiles(Instant threshold) {
        var sql = """
                SELECT * FROM SYSTEM_ATTACH_FILE
                WHERE is_linked = FALSE
                  AND created_at < :threshold
                """;

        return jdbcClient.sql(sql).param("threshold", Timestamp.from(threshold))
                .query((rs, rowNum) -> {
                    AttachFileId id = new AttachFileId(rs.getString("id"));
                    // 자식 아이템들을 함께 로드하여 재구성
                    List<FileItem> items = loadFileItems(id);

                    return AttachFile.reconstitute(id, items, rs.getBoolean("is_linked"),
                            rs.getTimestamp("created_at").toInstant(), rs.getString("created_by"),
                            rs.getTimestamp("modified_at").toInstant(),
                            rs.getString("modified_by"));
                }).list();
    }

    @Override
    public void deleteGroup(AttachFileId id) {
        // 1. 하위 파일 아이템 먼저 삭제 (DB 외래키 ON DELETE CASCADE가 설정되지 않았을 경우를 대비)
        var sqlItems = "DELETE FROM SYSTEM_FILE_ITEM WHERE attach_file_id = :attachFileId";
        jdbcClient.sql(sqlItems).param("attachFileId", id.getRawValue()).update();

        // 2. 부모 그룹 삭제
        var sqlAttach = "DELETE FROM SYSTEM_ATTACH_FILE WHERE id = :id";
        jdbcClient.sql(sqlAttach).param("id", id.getRawValue()).update();
    }
}
