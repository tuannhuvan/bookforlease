# Bài tập Spring Boot Thymeleaf nâng cao: Hệ thống quản lý mượn trả sách và tính phí phạt trả muộn

Tài liệu này dùng để giao bài tập cho Junior Java Developer đã học Java Core, Spring Boot MVC, Thymeleaf, Spring Data JPA và MySQL. Phiên bản nâng cấp tập trung vào nghiệp vụ mượn trả sách, trả từng phần, trả muộn, tự động tính phí phạt, quản lý nợ phí và kiểm soát toàn vẹn dữ liệu bằng transaction.

---

## 1. Mục tiêu bài tập

- Xây dựng ứng dụng web server-side bằng **Spring Boot MVC** và **Thymeleaf**, không yêu cầu REST API làm giao diện chính.
- Thiết kế database có quan hệ rõ ràng giữa sách, độc giả, phiếu mượn, chi tiết mượn, phí phạt và thanh toán phí.
- Xử lý nghiệp vụ mượn, trả toàn bộ, trả từng phần, quá hạn, tính phí phạt và thanh toán phí trong transaction.
- Hiển thị form nhập liệu, danh sách, chi tiết, cảnh báo lỗi nghiệp vụ và thông báo thành công trên giao diện Thymeleaf.
- Viết test cho các nghiệp vụ quan trọng, đặc biệt các case trả muộn và rollback dữ liệu.

---

## 2. Công nghệ bắt buộc

| **Nhóm** | **Yêu cầu** |
| :--- | :--- |
| **Ngôn ngữ và framework** | Java 21, Spring Boot 3.x, Maven |
| **Web UI** | Spring MVC, Thymeleaf, HTML, Bootstrap hoặc CSS cơ bản |
| **Database** | MySQL hoặc PostgreSQL, Spring Data JPA, Hibernate |
| **Validation** | Bean Validation, hiển thị lỗi tại form |
| **Migration** | Flyway hoặc Liquibase |
| **Test** | JUnit 5, Mockito, H2 hoặc Testcontainers cho integration test |
| **Không dùng** | Không dùng REST API làm phần giao diện chính, không dùng frontend framework như Angular hoặc React |

---

## 3. Mô hình dữ liệu nâng cấp

Ngoài các bảng `Category`, `Book`, `Member`, `Borrowing` và `BorrowingDetail` của bài cơ bản, phiên bản nâng cấp cần bổ sung thông tin phí phạt và thanh toán.

| **Bảng** | **Trường chính** | **Vai trò** | **Ghi chú nghiệp vụ** |
| :--- | :--- | :--- | :--- |
| **Borrowing** | `borrowedDate`, `dueDate`, `returnedDate`, `status`, `totalFineAmount`, `paidFineAmount`, `unpaidFineAmount` | Quản lý phiếu mượn | Một phiếu có thể trả từng phần. Chỉ hoàn tất khi trả hết sách và xử lý phí phạt theo quy định. |
| **BorrowingDetail** | `quantity`, `returnedQuantity`, `lastReturnedDate`, `lateDays`, `fineAmount` | Theo dõi từng đầu sách | Phí phạt có thể tính theo từng dòng sách để xử lý trả từng phần chính xác. |
| **FinePolicy** | `dailyFineAmount`, `maxFineAmount`, `graceDays`, `active` | Cấu hình phí phạt | Cho phép thay đổi mức phí mà không sửa code. Chỉ một policy active tại một thời điểm. |
| **FinePayment** | `borrowingId`, `amount`, `paymentDate`, `method`, `note` | Ghi nhận thanh toán | Có thể thanh toán một phần hoặc toàn bộ phí phạt. |
| **FineWaiver** | `borrowingId`, `amount`, `reason`, `approvedBy`, `approvedDate` | Miễn hoặc giảm phí | Phần nâng cao có phê duyệt miễn giảm, dùng để đánh giá tư duy nghiệp vụ. |

---

## 4. Trạng thái nghiệp vụ

| **Trạng thái** | **Ý nghĩa** | **Điều kiện chuyển trạng thái** |
| :--- | :--- | :--- |
| `BORROWING` | Đang mượn, chưa trả hết | Tạo phiếu thành công |
| `PARTIALLY_RETURNED` | Đã trả một phần sách | `returnedQuantity` lớn hơn 0 nhưng chưa trả hết |
| `RETURNED` | Đã trả hết sách | Tất cả dòng chi tiết đã trả đủ |
| `OVERDUE` | Đang quá hạn | `dueDate` nhỏ hơn ngày hiện tại và chưa trả hết |
| `FINE_PENDING` | Đã phát sinh phí nhưng chưa thanh toán đủ | `unpaidFineAmount` lớn hơn 0 |
| `FINE_PAID` | Đã thanh toán đủ phí phạt | `unpaidFineAmount` bằng 0 |
| `CANCELLED` | Phiếu bị hủy | Chỉ được hủy khi chưa trả sách và chưa phát sinh phí |

---

## 5. Nghiệp vụ mượn sách

- Một độc giả chỉ được mượn tối đa 5 cuốn đang chưa trả tại cùng thời điểm.
- Không cho tạo phiếu mới nếu độc giả đang có phiếu quá hạn hoặc còn phí phạt chưa thanh toán.
- Mỗi phiếu phải có ít nhất một đầu sách và không được có hai dòng trùng `bookId`.
- Số lượng mượn phải lớn hơn 0 và không vượt quá `availableQuantity`.
- Khi tạo phiếu thành công, giảm `availableQuantity` của từng sách trong cùng transaction.
- Nếu một dòng sách không hợp lệ, toàn bộ phiếu phải rollback, không được trừ tồn kho một phần.

---

## 6. Nghiệp vụ trả sách nâng cao

- Cho phép trả toàn bộ hoặc trả từng phần theo từng đầu sách.
- Không được trả nhiều hơn số lượng còn đang mượn.
- Khi trả sách, tăng lại `availableQuantity` tương ứng.
- Nếu trả trước hoặc đúng hạn, không phát sinh phí phạt.
- Nếu trả sau `dueDate`, hệ thống tính số ngày trễ và phí phạt tại thời điểm trả.
- Nếu trả từng phần sau hạn, chỉ tính phí cho số lượng sách được trả trong lần đó.
- Nếu phiếu vẫn còn sách chưa trả sau hạn, trạng thái vẫn là `OVERDUE` hoặc `PARTIALLY_RETURNED` tùy quy tắc hiển thị.
- Không cho trả sách với phiếu `RETURNED` hoặc `CANCELLED`.

---

## 7. Công thức tính phí phạt

Sinh viên phải triển khai công thức tính phí bằng service riêng, không viết trực tiếp trong controller. Công thức mặc định:

$$\text{lateDays} = \max(0, \text{actualReturnDate} - \text{dueDate})$$
$$\text{fineAmount} = \text{lateDays} \times \text{returnedQuantity} \times \text{dailyFineAmount}$$

Nếu có `maxFineAmount`, phí của một dòng không được vượt quá mức trần. Nếu có `graceDays`, số ngày trễ chỉ tính sau khi vượt quá số ngày được miễn phạt.

| **Tình huống** | **Due date** | **Ngày trả** | **Kết quả** |
| :--- | :--- | :--- | :--- |
| **Trả đúng hạn** | 2026-10-10 | 2026-10-10 | `lateDays` = 0, `fine` = 0 |
| **Trả muộn 3 ngày, 1 cuốn** | 2026-10-10 | 2026-10-13 | `fine` = 3 × 1 × `dailyFineAmount` |
| **Trả muộn 3 ngày, 2 cuốn** | 2026-10-10 | 2026-10-13 | `fine` = 3 × 2 × `dailyFineAmount` |
| **Có graceDays = 1** | 2026-10-10 | 2026-10-12 | `lateDays` tính phí = 1 |
| **Có maxFineAmount** | 2026-10-01 | 2026-10-30 | `fine` không vượt quá `maxFineAmount` |

---

## 8. Quản lý phí phạt và thanh toán

- Trang chi tiết phiếu mượn phải hiển thị tổng phí phát sinh, phí đã thanh toán, phí được miễn giảm và phí còn phải thu.
- Cho phép ghi nhận thanh toán phí phạt nhiều lần cho một phiếu.
- Không cho nhập số tiền thanh toán nhỏ hơn hoặc bằng 0.
- Không cho thanh toán vượt quá số phí còn phải thu.
- Sau khi thanh toán đủ, trạng thái phí chuyển thành `FINE_PAID`.
- Nếu còn phí chưa thanh toán, độc giả bị chặn tạo phiếu mượn mới.
- **Phần nâng cao:** Cho phép miễn giảm phí với lý do bắt buộc và người duyệt.

---

## 9. Màn hình Thymeleaf bắt buộc

| **Màn hình** | **Route gợi ý** | **Chức năng chính** |
| :--- | :--- | :--- |
| **Danh sách sách** | `GET /books` | Tìm kiếm, phân trang, xem tồn kho còn mượn được |
| **Tạo phiếu mượn** | `GET /borrowings/new`<br>`POST /borrowings` | Chọn độc giả, chọn nhiều sách, nhập số lượng và hạn trả |
| **Danh sách phiếu mượn** | `GET /borrowings` | Lọc theo trạng thái, độc giả, khoảng ngày, phiếu quá hạn |
| **Chi tiết phiếu** | `GET /borrowings/{id}` | Hiển thị thông tin phiếu, chi tiết sách, phí phạt, lịch sử thanh toán |
| **Trả sách** | `GET /borrowings/{id}/return`<br>`POST /borrowings/{id}/return` | Nhập số lượng trả theo từng sách, hiển thị phí phát sinh trước khi xác nhận |
| **Thanh toán phí** | `GET /borrowings/{id}/payments/new`<br>`POST /borrowings/{id}/payments` | Ghi nhận thanh toán phí phạt |
| **Cấu hình phí phạt** | `GET /fine-policies`<br>`POST /fine-policies` | Thiết lập phí mỗi ngày, `graceDays`, `maxFineAmount` |
| **Báo cáo quá hạn** | `GET /reports/overdue` | Danh sách độc giả quá hạn, số ngày trễ, số phí dự kiến |

---

## 10. Yêu cầu transaction và concurrency

- Tạo phiếu mượn, trả sách, hủy phiếu và thanh toán phí phải dùng `@Transactional`.
- Khi trả sách phát sinh phí, việc cập nhật `returnedQuantity`, `availableQuantity` và `fineAmount` phải thành công hoặc rollback cùng nhau.
- Nếu hai người cùng mượn cuốn sách cuối cùng, hệ thống không được để `availableQuantity` âm.
- Khuyến khích dùng optimistic locking bằng `@Version` trên `Book` hoặc xử lý update tồn kho có điều kiện.
- **Không được dùng `synchronized` trong controller** để giải quyết concurrency.

---

## 11. Test case bắt buộc

| **Nhóm test** | **Case bắt buộc** | **Kỳ vọng** |
| :--- | :--- | :--- |
| **Mượn sách** | Mượn thành công nhiều đầu sách | Tạo phiếu, trừ tồn kho đúng |
| **Mượn sách** | Một sách không đủ số lượng | Rollback toàn bộ phiếu |
| **Trả sách** | Trả toàn bộ đúng hạn | Không phát sinh phí, phiếu `RETURNED` |
| **Trả sách** | Trả một phần đúng hạn | Phiếu `PARTIALLY_RETURNED`, tồn kho tăng đúng |
| **Trả muộn** | Trả muộn 3 ngày | Tính đúng `lateDays` và `fineAmount` |
| **Trả muộn** | Trả từng phần sau hạn | Chỉ tính phí trên số lượng trả trong lần đó |
| **Phí phạt** | Thanh toán một phần phí | `paidFineAmount` tăng, `unpaidFineAmount` giảm |
| **Phí phạt** | Thanh toán vượt số nợ | Báo lỗi validation hoặc business exception |
| **Ràng buộc** | Độc giả còn nợ phí tạo phiếu mới | Không cho tạo phiếu mới |
| **Concurrency** | Hai request mượn cùng sách còn 1 bản | Chỉ một request thành công |

---

## 12. Thang điểm nâng cấp

| **Tiêu chí** | **Điểm** |
| :--- | :---: |
| Thiết kế database, quan hệ JPA và migration | 15 |
| Màn hình Thymeleaf, form validation và trải nghiệm sử dụng | 15 |
| Nghiệp vụ mượn sách và kiểm soát tồn kho | 15 |
| Nghiệp vụ trả sách, trả từng phần và cập nhật trạng thái | 20 |
| Tính phí phạt, thanh toán phí và chặn mượn khi còn nợ | 15 |
| Transaction, rollback và xử lý concurrency | 10 |
| Test tự động cho các case quan trọng | 10 |
| **Tổng cộng** | **100** |

---

## 13. Điều kiện không đạt

- Project không chạy được hoặc không kết nối được database.
- Không dùng Thymeleaf để thao tác nghiệp vụ chính.
- Không kiểm soát tồn kho khi mượn hoặc trả sách.
- Tính phí phạt sai hoặc hard code rải rác trong controller.
- Không dùng transaction cho mượn, trả, hủy hoặc thanh toán phí.
- Cho phép thanh toán vượt số tiền phải thu hoặc cho mượn khi còn nợ phí.
- Controller chứa phần lớn business logic.
- Không giải thích được cách tính phí và cách rollback khi lỗi.

---

## 14. Câu hỏi review sau khi nộp bài

1. Vì sao nghiệp vụ trả sách và tính phí phạt phải nằm trong cùng một transaction?
2. Nếu trả từng phần sau hạn thì phí nên tính trên toàn bộ phiếu hay trên số lượng trả từng lần? Vì sao?
3. Làm thế nào để không cho `availableQuantity` bị âm khi nhiều người mượn đồng thời?
4. Ưu nhược điểm của optimistic locking và pessimistic locking trong bài này là gì?
5. Vì sao không nên viết công thức tính phí trong controller?
6. Nếu sau này thư viện thay đổi chính sách phí theo từng loại sách thì thiết kế hiện tại cần mở rộng thế nào?
7. Khi nào nên chuyển trạng thái `OVERDUE`, bằng scheduler hay tính động khi hiển thị?
8. Làm thế nào kiểm thử rollback khi trả sách thành công một phần rồi lỗi ở bước tính phí?
9. Vì sao cần bảng `FinePayment` thay vì chỉ lưu `paidFineAmount` trên `Borrowing`?
10. Nếu dữ liệu có một triệu phiếu mượn, cần index và tối ưu truy vấn ở đâu?

---

> **Ghi chú triển khai:** Sinh viên được phép dùng Bootstrap để giao diện dễ nhìn hơn, nhưng điểm chính nằm ở thiết kế nghiệp vụ, transaction, tính đúng phí phạt và chất lượng code Spring Boot MVC.
