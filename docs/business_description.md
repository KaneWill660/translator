# Business Description — Ứng dụng Translator

**Loại tài liệu:** Mô tả nghiệp vụ sản phẩm (Business Description)
**Phiên bản:** 1.0
**Ngày:** 16/09/2026
**Chủ sở hữu sản phẩm:** dovanthuc10009@gmail.com

---

## 1. Tổng quan

Translator là ứng dụng di động Android dành cho việc **tự học và luyện dịch tiếng Anh cá nhân**, được xây dựng để khai thác kho ngữ liệu luyện dịch Việt → Anh mà người dùng đã tự biên soạn (hiện lưu dưới dạng các tài liệu HTML). Ứng dụng chuyển hóa kho ngữ liệu tĩnh này thành một công cụ luyện tập tương tác, đồng thời bổ sung khả năng tra cứu bản dịch nhanh bằng giọng nói thông qua trí tuệ nhân tạo (AI).

Đây là sản phẩm phục vụ mục đích sử dụng cá nhân, không hướng đến thương mại hóa hay phân phối đại trà ở giai đoạn hiện tại.

## 2. Bối cảnh & vấn đề cần giải quyết

Người dùng đã tự biên soạn một bộ ngữ liệu luyện dịch có cấu trúc (câu tiếng Việt, công thức ngữ pháp tương ứng, đáp án tiếng Anh mẫu) nhưng đang ở dạng file tĩnh, chỉ phù hợp để đọc/in ấn, không hỗ trợ luyện tập chủ động. Việc luyện dịch theo cách thủ công (đọc file, tự che đáp án, tự so sánh) tốn thời gian tổ chức và không thuận tiện khi học mọi lúc mọi nơi trên thiết bị di động.

Bên cạnh đó, người dùng có nhu cầu tra cứu/dịch nhanh các câu phát sinh ngoài kho ngữ liệu có sẵn — ví dụ khi gặp một câu tiếng Việt bất kỳ muốn biết cách diễn đạt tiếng Anh tự nhiên — mà không muốn gõ tay, ưu tiên nói trực tiếp và nhận kết quả ngay.

## 3. Mục tiêu sản phẩm

- Biến kho ngữ liệu luyện dịch sẵn có thành trải nghiệm luyện tập chủ động, có thể thao tác lặp lại (xem câu → tự dịch → đối chiếu đáp án) ngay trên thiết bị di động.
- Cho phép tra cứu bản dịch tiếng Anh tức thời từ giọng nói tiếng Việt, tận dụng AI để đảm bảo chất lượng bản dịch tự nhiên.
- Giữ trải nghiệm đơn giản, không phụ thuộc hạ tầng backend riêng, vận hành ổn định kể cả khi không có kết nối mạng đối với tính năng luyện tập cốt lõi.
- Đảm bảo kho ngữ liệu luôn có thể cập nhật từ xa (khi người dùng bổ sung câu mới) mà không cần cài đặt lại ứng dụng.

## 4. Đối tượng người dùng

Người dùng duy nhất/chính của sản phẩm ở giai đoạn này là chủ sở hữu sản phẩm — một cá nhân đang tự học và luyện dịch tiếng Anh, có khả năng tự biên soạn và mở rộng ngữ liệu học tập theo thời gian.

## 5. Phạm vi sản phẩm

### 5.1. Tính năng cốt lõi

**A. Luyện dịch bằng flashcard (tự chấm)**
Ứng dụng hiển thị ngẫu nhiên một câu tiếng Việt trích từ kho ngữ liệu đã chuẩn hóa. Người dùng tự gõ bản dịch tiếng Anh của mình, sau đó có thể yêu cầu hệ thống hiển thị đáp án mẫu bất cứ lúc nào — kể cả khi chưa nhập gì — để tự đối chiếu và đánh giá mức độ chính xác theo cách chủ quan của bản thân (không có cơ chế chấm điểm tự động đúng/sai). Người dùng có thể chuyển sang câu tiếp theo hoặc quay lại câu vừa xem trong cùng phiên luyện tập.

**B. Dịch nhanh bằng giọng nói (hỗ trợ bởi AI)**
Ứng dụng cho phép người dùng nói một câu tiếng Việt bất kỳ; nội dung được chuyển thành văn bản ngay trên màn hình, sau đó gửi tới dịch vụ AI (OpenAI) để nhận về bản dịch tiếng Anh tự nhiên, hiển thị cho người dùng tham khảo.

**C. Đồng bộ ngữ liệu tự động**
Ứng dụng luôn có sẵn một bộ ngữ liệu mặc định ngay từ lần cài đặt đầu tiên, hoạt động không cần mạng. Mỗi lần mở ứng dụng, hệ thống âm thầm kiểm tra xem có phiên bản ngữ liệu mới hơn trên kho lưu trữ trung tâm hay không; nếu có, tự động tải về và cập nhật mà không làm gián đoạn trải nghiệm luyện tập đang diễn ra. Người dùng luôn có thể biết được ngữ liệu hiện tại thuộc phiên bản nào và lần cập nhật gần nhất.

### 5.2. Ngoài phạm vi (giai đoạn 1)

- Không có hệ thống chấm điểm/đánh giá độ chính xác bản dịch tự động.
- Không có tính năng đăng nhập, tài khoản người dùng, hay đồng bộ tiến độ học tập giữa nhiều thiết bị.
- Không xây dựng backend/máy chủ riêng — mọi tích hợp AI gọi trực tiếp tới nhà cung cấp dịch vụ bên thứ ba.
- Không hỗ trợ dịch giọng nói khi mất kết nối mạng (yêu cầu bắt buộc có mạng và khóa truy cập API hợp lệ).
- Không phân phối trên kho ứng dụng công khai (Google Play) ở giai đoạn này.

## 6. Giá trị mang lại

- Rút ngắn thời gian tổ chức và ôn luyện từ ngữ liệu học tập sẵn có, biến tài liệu tĩnh thành công cụ luyện tập chủ động, có thể dùng mọi lúc mọi nơi.
- Duy trì được thói quen luyện dịch nhờ trải nghiệm gọn nhẹ, phản hồi tức thời.
- Mở rộng khả năng tra cứu bản dịch ngoài phạm vi ngữ liệu đã chuẩn bị sẵn, tận dụng sức mạnh AI hiện đại mà không cần công cụ bên ngoài.
- Ngữ liệu học tập có thể tiếp tục được bổ sung/hoàn thiện theo thời gian mà không cần phát hành lại ứng dụng, giúp sản phẩm "sống" cùng quá trình học của người dùng.

## 7. Ràng buộc & giả định

- Chi phí sử dụng dịch vụ AI (OpenAI) do người dùng tự chi trả thông qua khóa API cá nhân, không có cơ chế giới hạn/ước tính chi phí trong phạm vi giai đoạn 1.
- Chất lượng đáp án mẫu trong ngữ liệu phụ thuộc vào dữ liệu gốc do người dùng tự biên soạn; các trường hợp dữ liệu chưa đầy đủ (thiếu đáp án mẫu) sẽ không được đưa vào tính năng luyện tập.
- Nguồn ngữ liệu trung tâm để đồng bộ là kho lưu trữ mã nguồn công khai do người dùng quản lý; tính sẵn sàng của tính năng đồng bộ phụ thuộc vào việc người dùng chủ động cập nhật kho này.

## 8. Tiêu chí thành công

- Người dùng có thể luyện tập với toàn bộ ngữ liệu hợp lệ hiện có mà không gặp lỗi hiển thị hoặc dữ liệu sai lệch.
- Tính năng dịch giọng nói trả về kết quả chính xác, dễ hiểu trong phần lớn các lượt sử dụng thông thường.
- Ứng dụng khởi động và sẵn sàng luyện tập ngay lập tức, không bị trì hoãn bởi thao tác kiểm tra/cập nhật ngữ liệu chạy ngầm.
- Ngữ liệu mới được bổ sung ở kho trung tâm phản ánh vào ứng dụng ở lần mở kế tiếp mà người dùng không cần thao tác thủ công.

## 9. Lộ trình tổng quan

| Giai đoạn | Nội dung |
|---|---|
| Giai đoạn 1 (MVP) | Chuẩn hóa ngữ liệu hiện có, xây dựng tính năng luyện dịch flashcard và tính năng dịch giọng nói qua AI, cơ chế đồng bộ ngữ liệu tự động từ kho trung tâm. |
| Giai đoạn kế tiếp (chưa cam kết) | Cân nhắc bổ sung theo nhu cầu sử dụng thực tế: theo dõi tiến độ luyện tập, mở rộng nguồn ngữ liệu, tối ưu chi phí gọi AI. |

---

*Tài liệu kỹ thuật triển khai chi tiết (kiến trúc, luồng xử lý, wireframe) được quản lý riêng trong kế hoạch triển khai của dự án.*
