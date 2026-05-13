package com.uit.buddy.service.document.impl;

import com.uit.buddy.dto.request.document.UpdateDocumentContentRequest;
import com.uit.buddy.dto.response.document.DocumentContentResponse;
import com.uit.buddy.dto.response.social.UserSummary;
import com.uit.buddy.dto.websocket.DocumentPresenceMessage;
import com.uit.buddy.dto.websocket.DocumentUpdateMessage;
import com.uit.buddy.entity.document.Document;
import com.uit.buddy.entity.document.ShareDocument;
import com.uit.buddy.entity.user.Student;
import com.uit.buddy.exception.document.DocumentErrorCode;
import com.uit.buddy.exception.document.DocumentException;
import com.uit.buddy.repository.document.DocumentRepository;
import com.uit.buddy.repository.document.ShareDocumentRepository;
import com.uit.buddy.repository.user.StudentRepository;
import com.uit.buddy.service.document.DocumentCollaborationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class DocumentCollaborationServiceImpl implements DocumentCollaborationService {

        private final DocumentRepository documentRepository;
        private final ShareDocumentRepository shareDocumentRepository;
        private final StudentRepository studentRepository;
        private final RedisTemplate<String, Object> redisTemplate;
        private final SimpMessagingTemplate messagingTemplate;

        private static final String PRESENCE_KEY_PREFIX = "doc:presence:";

        @Override
        @Transactional(readOnly = true)
        public DocumentContentResponse getDocumentContent(UUID documentId, String mssv) {
                Document document = findDocumentWithAccessCheck(documentId, mssv);
                return buildDocumentContentResponse(document);
        }

        @Override
        @Transactional
        public DocumentContentResponse updateDocumentContent(
                        UUID documentId,
                        UpdateDocumentContentRequest request,
                        String mssv) {

                Document document = findDocumentWithAccessCheck(documentId, mssv);

                // Last write wins - no version checking
                document.setContent(request.getContent());
                document.setLastEditedBy(mssv);
                document.setLastEditedAt(Instant.now());

                Document savedDocument = documentRepository.save(document);

                log.info("Document {} updated by {}, version: {}", documentId, mssv, savedDocument.getVersion());

                // Broadcast to all connected users that document was updated
                broadcastDocumentUpdate(savedDocument, mssv);

                return buildDocumentContentResponse(savedDocument);
        }

        @Override
        public void joinDocument(UUID documentId, String mssv) {
                findDocumentWithAccessCheck(documentId, mssv);

                String key = PRESENCE_KEY_PREFIX + documentId;
                redisTemplate.opsForSet().add(key, mssv);

                log.info("User {} joined document {}", mssv, documentId);

                broadcastPresenceUpdate(documentId, mssv, true);
        }

        @Override
        public void leaveDocument(UUID documentId, String mssv) {
                String key = PRESENCE_KEY_PREFIX + documentId;
                redisTemplate.opsForSet().remove(key, mssv);

                log.info("User {} left document {}", mssv, documentId);

                broadcastPresenceUpdate(documentId, mssv, false);
        }

        private Document findDocumentWithAccessCheck(UUID documentId, String mssv) {
                Document document = documentRepository.findById(documentId)
                                .orElseThrow(() -> new DocumentException(DocumentErrorCode.FILE_NOT_FOUND));

                if (document.getFileType() != com.uit.buddy.enums.FileType.WORD) {
                        throw new DocumentException(DocumentErrorCode.FILE_ACCESS_DENIED,
                                        "Only text-based files (doc, docx, odt, txt, pdf) support collaborative editing");
                }

                boolean hasAccess = document.getMssv().equals(mssv) ||
                                shareDocumentRepository.existsByDocumentIdAndMssv(documentId, mssv);

                if (!hasAccess) {
                        throw new DocumentException(DocumentErrorCode.FILE_ACCESS_DENIED);
                }

                return document;
        }

        private DocumentContentResponse buildDocumentContentResponse(Document document) {
                List<UserSummary> collaborators = shareDocumentRepository.findByDocumentId(document.getId())
                                .stream()
                                .map(ShareDocument::getRecipient)
                                .map(student -> new UserSummary(
                                                student.getMssv(),
                                                student.getFullName(),
                                                student.getAvatarUrl()))
                                .collect(Collectors.toList());

                return DocumentContentResponse.builder()
                                .id(document.getId())
                                .fileName(document.getFileName())
                                .content(document.getContent())
                                .version(document.getVersion())
                                .owner(new UserSummary(
                                                document.getOwner().getMssv(),
                                                document.getOwner().getFullName(),
                                                document.getOwner().getAvatarUrl()))
                                .lastEditor(
                                                document.getLastEditor() != null
                                                                ? new UserSummary(
                                                                                document.getLastEditor().getMssv(),
                                                                                document.getLastEditor().getFullName(),
                                                                                document.getLastEditor().getAvatarUrl())
                                                                : null)
                                .lastEditedAt(document.getLastEditedAt())
                                .collaborators(collaborators)
                                .createdAt(document.getCreatedAt())
                                .updatedAt(document.getUpdatedAt())
                                .build();
        }

        private void broadcastDocumentUpdate(Document document, String editorMssv) {
                DocumentUpdateMessage message = DocumentUpdateMessage.builder()
                                .documentId(document.getId())
                                .version(document.getVersion())
                                .build();

                messagingTemplate.convertAndSend(
                                "/topic/document/" + document.getId() + "/updates",
                                message);

                log.debug("Broadcasted document update for {}", document.getId());
        }

        private void broadcastPresenceUpdate(UUID documentId, String mssv, boolean isJoining) {
                String key = PRESENCE_KEY_PREFIX + documentId;
                Set<Object> activeMssvs = redisTemplate.opsForSet().members(key);

                List<DocumentPresenceMessage.ActiveUser> activeUsers = activeMssvs.stream()
                                .map(Object::toString)
                                .map(activeMssv -> {
                                        Student activeStudent = studentRepository.findById(activeMssv).orElse(null);
                                        return DocumentPresenceMessage.ActiveUser.builder()
                                                        .mssv(activeMssv)
                                                        .name(activeStudent != null ? activeStudent.getFullName()
                                                                        : activeMssv)
                                                        .avatarUrl(activeStudent != null ? activeStudent.getAvatarUrl()
                                                                        : null)
                                                        .build();
                                })
                                .collect(Collectors.toList());

                DocumentPresenceMessage message = DocumentPresenceMessage.builder()
                                .documentId(documentId)
                                .activeUsers(activeUsers)
                                .totalActive(activeUsers.size())
                                .build();

                messagingTemplate.convertAndSend(
                                "/topic/document/" + documentId + "/presence",
                                message);

                log.debug("Broadcasted presence update for document {}: {} users active",
                                documentId, activeUsers.size());
        }
}
