---
name: backend-conventions
description: Use when creating or modifying backend Java code in auth-service or main-service. Covers entity patterns, DTO conventions, service layer patterns, controller structure, security configuration, and Spring Boot best practices for this project.
---

# Backend Conventions

## Entity Patterns

```java
@Entity
@Table(name = "table_name")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MyEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "column_name", nullable = false)
    private String fieldName;

    @Builder.Default
    private Boolean active = true;

    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ChildEntity> children = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
```

## DTO Patterns

Use Java records for all DTOs:

```java
public record MyRequestDto(
    @NotBlank String name,
    @NotNull Long relatedId,
    @Size(max = 500) String description
) {}

public record MyResponseDto(
    Long id,
    String name,
    Long relatedId,
    String description,
    LocalDateTime createdAt
) {}
```

## Service Layer

```java
@Service
@RequiredArgsConstructor
public class MyService {
    private final MyRepository repository;
    private final RelatedService relatedService;

    public List<MyResponseDto> list(String username) {
        return repository.findAllByCreatedBy(username)
            .stream()
            .map(this::toDto)
            .toList();
    }

    public MyResponseDto getById(Long id, String username) {
        MyEntity entity = repository.findByIdAndCreatedBy(id, username)
            .orElseThrow(() -> new EntityNotFoundException("Not found: " + id));
        return toDto(entity);
    }

    @Transactional
    public MyResponseDto create(MyRequestDto dto, String username) {
        MyEntity entity = MyEntity.builder()
            .name(dto.name())
            .createdBy(username)
            .build();
        return toDto(repository.save(entity));
    }

    private MyResponseDto toDto(MyEntity entity) {
        return new MyResponseDto(
            entity.getId(),
            entity.getName(),
            // ... map all fields
        );
    }
}
```

## Controller Patterns

```java
@RestController
@RequestMapping("/api/resources")
@RequiredArgsConstructor
public class MyController {
    private final MyService service;

    @GetMapping
    public List<MyResponseDto> list(@AuthenticationPrincipal String username) {
        return service.list(username);
    }

    @GetMapping("/{id}")
    public MyResponseDto getById(@PathVariable Long id, @AuthenticationPrincipal String username) {
        return service.getById(id, username);
    }

    @PostMapping
    public ResponseEntity<MyResponseDto> create(
            @Valid @RequestBody MyRequestDto dto,
            @AuthenticationPrincipal String username) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(service.create(dto, username));
    }

    @PutMapping("/{id}")
    public MyResponseDto update(
            @PathVariable Long id,
            @Valid @RequestBody MyRequestDto dto,
            @AuthenticationPrincipal String username) {
        return service.update(id, dto, username);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id, @AuthenticationPrincipal String username) {
        service.delete(id, username);
        return ResponseEntity.noContent().build();
    }
}
```

## Repository Patterns

```java
public interface MyRepository extends JpaRepository<MyEntity, Long> {
    List<MyEntity> findAllByCreatedByOrderByCreatedAtDesc(String username);
    
    Optional<MyEntity> findByIdAndCreatedBy(Long id, String username);
    
    @Query("SELECT e FROM MyEntity e WHERE e.createdBy = :username OR e.id IN " +
           "(SELECT m.entityId FROM Member m WHERE m.username = :username AND m.status = 'ACCEPTED')")
    List<MyEntity> findAllAccessible(@Param("username") String username);
}
```

## Error Handling

```java
// 404 Not Found
throw new EntityNotFoundException("Resource not found: " + id);

// 403 Forbidden
throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Access denied");

// 400 Bad Request
throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid state");

// 409 Conflict
throw new ResponseStatusException(HttpStatus.CONFLICT, "Already exists");
```

## Security

- Extract current user: `@AuthenticationPrincipal String username`
- Admin-only endpoints: use `@PreAuthorize("hasRole('ADMIN')")` or configure in `SecurityConfig`
- JWT validation is automatic via `JwtAuthFilter`
- Bot tokens must be encrypted via `BotTokenEncryptionService` before storage

## JSONB Content Blocks

For rich content (text + images), use `ContentBlock` records stored as JSONB:

```java
@JdbcTypeCode(SqlTypes.JSON)
@Column(name = "content_blocks", columnDefinition = "jsonb")
@Builder.Default
private List<ContentBlock> contentBlocks = new ArrayList<>();

// ContentBlock is: record ContentBlock(String type, String content)
// type: "text" or "image"
// content: plain text or image blob ID
```

## Access Control

Use enum-based access levels for hierarchical permissions:

```java
public enum AccessLevel {
    OWNER(0), CO_OWNER(1), CONTRIBUTOR(2), NONE(3);
    
    private final int level;
    
    public boolean isAtLeast(AccessLevel required) {
        return this.level <= required.level;
    }
}
```
