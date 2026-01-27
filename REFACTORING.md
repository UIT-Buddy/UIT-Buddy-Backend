# Refactoring: Từ Inheritance sang Composition

## Thay đổi gì?

### Trước (Inheritance):

```java
// Base class
public abstract class AbstractBaseController {
    @Autowired
    protected ResponseFactory responseFactory;
}

// Controller phải extends
public class UserController extends AbstractBaseController {
    // Tự động có responseFactory
}
```

**Vấn đề:**

- ❌ Tight coupling - Controller phụ thuộc vào base class
- ❌ Khó test - Phải mock cả AbstractBaseController
- ❌ Không linh hoạt - Chỉ kế thừa được 1 class
- ❌ Vi phạm SOLID principles

### Sau (Composition):

```java
// Không cần base class nữa!

@RestController
@RequiredArgsConstructor  // Lombok tự tạo constructor
public class UserController {

    // Inject dependency qua constructor
    private final ResponseFactory responseFactory;

    // Sử dụng như bình thường
    public ResponseEntity<SuccessResponse> getUser() {
        return responseFactory.success("OK");
    }
}
```

**Lợi ích:**

- ✅ Loose coupling - Controller độc lập
- ✅ Dễ test - Mock chỉ ResponseFactory
- ✅ Linh hoạt - Có thể inject nhiều service
- ✅ Tuân thủ SOLID principles
- ✅ Code sạch hơn, dễ hiểu hơn

## Tại sao Composition tốt hơn?

### 1. Dễ test hơn

```java
// Test với Composition
@Test
void testGetUser() {
    ResponseFactory mockFactory = mock(ResponseFactory.class);
    UserController controller = new UserController(mockFactory);

    // Test dễ dàng!
}
```

### 2. Linh hoạt hơn

```java
// Có thể inject nhiều dependency
@RestController
@RequiredArgsConstructor
public class UserController {
    private final ResponseFactory responseFactory;
    private final UserService userService;
    private final EmailService emailService;
    // Không bị giới hạn bởi single inheritance!
}
```

### 3. Tuân thủ SOLID

- **S**ingle Responsibility: Mỗi class có 1 nhiệm vụ
- **O**pen/Closed: Mở rộng bằng composition, không sửa base class
- **L**iskov Substitution: Không áp dụng vì không có inheritance
- **I**nterface Segregation: Inject đúng những gì cần
- **D**ependency Inversion: Phụ thuộc vào abstraction (interface)

## Khi nào dùng Inheritance?

Chỉ dùng khi có quan hệ "IS-A" rõ ràng:

```java
// DTO - OK để dùng inheritance
public abstract class AbstractBaseResponse {
    protected int statusCode;
    protected String message;
}

public class SingleResponse<T> extends AbstractBaseResponse {
    private T data;
}
// SingleResponse IS-A Response ✅
```

## Khi nào dùng Composition?

Khi có quan hệ "HAS-A":

```java
// Controller HAS-A ResponseFactory ✅
public class UserController {
    private final ResponseFactory responseFactory;
}
```

## Best Practices

1. **Favor Composition over Inheritance** - Gang of Four Design Patterns
2. **Inject dependencies qua Constructor** - Dễ test, immutable
3. **Dùng @RequiredArgsConstructor** - Lombok giảm boilerplate
4. **Dùng final** - Đảm bảo dependency không thay đổi

## Kết luận

Refactoring này giúp code:

- 🎯 Dễ maintain
- 🧪 Dễ test
- 🔧 Linh hoạt hơn
- 📚 Tuân thủ best practices
- 🚀 Sẵn sàng scale

**"Composition over Inheritance"** là một trong những nguyên tắc quan trọng nhất trong OOP!
